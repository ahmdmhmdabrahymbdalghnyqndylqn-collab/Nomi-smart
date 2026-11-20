package com.nomismart.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Forest = Color(0xFF176B4D)
private val ForestDark = Color(0xFF10543B)
private val Mint = Color(0xFFEAF5EF)
private val Pale = Color(0xFFF6F8F6)
private val Ink = Color(0xFF1D2923)
private val Muted = Color(0xFF7C8982)
private val Line = Color(0xFFE8ECE9)
private val White = Color(0xFFFFFFFF)
private val Amber = Color(0xFFF4B85A)

data class NomiTask(
    val title: String,
    val time: String,
    val label: String,
    val category: String,
    val done: Boolean = false
)

private enum class Page(val title: String) {
    HOME("الرئيسية"), TASKS("مهامي"), MONEY("المال"), SETTINGS("الإعدادات")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NomiApp() }
    }
}

@Composable
fun NomiApp() {
    var page by remember { mutableStateOf(Page.HOME) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val storage = remember { context.getSharedPreferences("nomi", android.content.Context.MODE_PRIVATE) }
    var tasks by remember {
        val rows = org.json.JSONArray(storage.getString("tasks", "[]"))
        mutableStateOf(List(rows.length()) { index ->
            val row = rows.getJSONObject(index)
            NomiTask(row.getString("title"), row.getString("time"), row.getString("label"), row.getString("category"), row.optBoolean("done"))
        })
    }
    LaunchedEffect(tasks) {
        val rows = org.json.JSONArray()
        tasks.forEach { task ->
            rows.put(org.json.JSONObject().put("title", task.title).put("time", task.time)
                .put("label", task.label).put("category", task.category).put("done", task.done))
        }
        storage.edit().putString("tasks", rows.toString()).apply()
    }
    var showAdd by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfWeek.value % 7) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest,
            onPrimary = White,
            secondary = ForestDark,
            background = Pale,
            surface = White,
            onSurface = Ink,
            outlineVariant = Line
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Pale,
                bottomBar = { BottomNavigation(page) { page = it } },
                floatingActionButton = {
                    if (page == Page.HOME || page == Page.TASKS) {
                        FloatingActionButton(
                            onClick = { showAdd = true },
                            containerColor = Forest,
                            contentColor = White,
                            shape = CircleShape
                        ) { Icon(Icons.Outlined.Add, contentDescription = "إضافة مهمة") }
                    }
                }
            ) { padding ->
                when (page) {
                    Page.HOME -> HomeScreen(
                        tasks = tasks,
                        selectedDay = selectedDay,
                        onDaySelected = { selectedDay = it },
                        onTaskDone = { index -> tasks = tasks.mapIndexed { i, t -> if (i == index) t.copy(done = !t.done) else t } },
                        onSeeTasks = { page = Page.TASKS },
                        modifier = Modifier.padding(padding)
                    )
                    Page.TASKS -> TasksScreen(
                        tasks = tasks,
                        onTaskDone = { index -> tasks = tasks.mapIndexed { i, t -> if (i == index) t.copy(done = !t.done) else t } },
                        modifier = Modifier.padding(padding)
                    )
                    Page.MONEY -> MoneyScreen(Modifier.padding(padding))
                    Page.SETTINGS -> SettingsScreen(Modifier.padding(padding))
                }
            }
            if (showAdd) {
                AddTaskDialog(
                    onDismiss = { showAdd = false },
                    onAdd = { title ->
                        tasks = tasks + NomiTask(title, "بدون وقت", "شخصي", "personal")
                        showAdd = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigation(page: Page, onSelect: (Page) -> Unit) {
    NavigationBar(containerColor = White, tonalElevation = 0.dp) {
        val items = listOf(
            Page.HOME to Icons.Outlined.Home,
            Page.TASKS to Icons.Outlined.CheckCircle,
            Page.MONEY to Icons.Outlined.AccountBalanceWallet,
            Page.SETTINGS to Icons.Outlined.Settings
        )
        items.forEach { (item, icon) ->
            NavigationBarItem(
                selected = page == item,
                onClick = { onSelect(item) },
                icon = { Icon(icon, contentDescription = item.title) },
                label = { Text(item.title, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Forest,
                    selectedTextColor = Forest,
                    indicatorColor = Mint,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
            )
        }
    }
}

@Composable
private fun HomeScreen(
    tasks: List<NomiTask>,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    onTaskDone: (Int) -> Unit,
    onSeeTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar"))),
                        color = Muted,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text("صباح الخير 👋", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("خلّينا نرتّب يومك بهدوء.", color = Muted, fontSize = 14.sp)
                }
                Surface(shape = CircleShape, color = White, shadowElevation = 1.dp) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.NotificationsNone, contentDescription = "الإشعارات", tint = Ink)
                    }
                }
            }
        }
        item { DayPicker(selectedDay, onDaySelected) }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = Forest, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("نظرة على يومك", color = White.copy(alpha = .78f), fontSize = 13.sp)
                            Spacer(Modifier.height(5.dp))
                            Text("${tasks.count { !it.done }} مهام متبقية", color = White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.size(50.dp).clip(CircleShape).background(White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = White, modifier = Modifier.size(25.dp))
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    LinearProgressIndicator(
                        progress = { if (tasks.isEmpty()) 0f else tasks.count { it.done }.toFloat() / tasks.size },
                        modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                        color = Color(0xFFB6E3CC),
                        trackColor = White.copy(alpha = .2f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("أنجزت ${tasks.count { it.done }} من ${tasks.size}", color = White.copy(alpha = .78f), fontSize = 12.sp)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("جدول اليوم", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f))
                TextButton(onClick = onSeeTasks) { Text("عرض الكل", color = Forest) }
            }
        }
        items(tasks.take(4).mapIndexed { index, task -> index to task }, key = { it.first }) { (index, task) ->
            TaskCard(task = task, onDone = { onTaskDone(index) })
        }
        item {
            Surface(color = White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Mint), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = Forest)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("اقتراح لجدولك", fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 14.sp)
                        Text("عندك وقت مناسب للمذاكرة بعد المحاضرة.", color = Muted, fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Muted)
                }
            }
        }
        item { MoneyPeekCard() }
    }
}

@Composable
private fun DayPicker(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    val firstDay = LocalDate.now().with(DayOfWeek.SUNDAY)
    val days = (0..6).map { firstDay.plusDays(it.toLong()).format(DateTimeFormatter.ofPattern("EEEEE", Locale("ar"))) }
    val dates = (0..6).map { firstDay.plusDays(it.toLong()).format(DateTimeFormatter.ofPattern("d", Locale("ar"))) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { index, day ->
            val selected = selectedDay == index
            Column(
                modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) Mint else Color.Transparent)
                    .clickable { onDaySelected(index) }.padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(day, color = if (selected) Forest else Muted, fontSize = 11.sp)
                Spacer(Modifier.height(5.dp))
                Text(dates[index], color = if (selected) Forest else Ink, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                Spacer(Modifier.height(5.dp))
                Box(Modifier.size(4.dp).clip(CircleShape).background(if (selected) Forest else Color.Transparent))
            }
        }
    }
}

@Composable
private fun TaskCard(task: NomiTask, onDone: () -> Unit) {
    val accent = when (task.category) { "study" -> Color(0xFF7B92D7); "work" -> Forest; else -> Amber }
    Surface(color = White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(44.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(task.time, color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Text(task.title, color = if (task.done) Muted else Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(task.label, color = accent, fontSize = 11.sp)
            }
            Surface(
                onClick = onDone,
                shape = CircleShape,
                color = if (task.done) Forest else White,
                border = if (task.done) null else androidx.compose.foundation.BorderStroke(1.dp, Line)
            ) {
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    if (task.done) Icon(Icons.Outlined.Check, contentDescription = "مكتملة", tint = White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TasksScreen(tasks: List<NomiTask>, onTaskDone: (Int) -> Unit, modifier: Modifier = Modifier) {
    var filter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "دراسة", "عمل", "شخصي")
    Column(modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("مهامي", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("رتّب أولوياتك وتابع إنجازك", color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { item ->
                    FilterChip(selected = filter == item, onClick = { filter = item }, label = { Text(item) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Mint, selectedLabelColor = Forest))
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val shown = tasks.withIndex().filter { filter == "الكل" || it.value.label == filter }
            items(shown, key = { it.index }) { entry -> TaskCard(entry.value) { onTaskDone(entry.index) } }
            item {
                Surface(color = Mint, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Forest)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("جدولك الأسبوعي", fontWeight = FontWeight.SemiBold, color = ForestDark)
                            Text("محاضراتك ودوامك ومواعيدك بمكان واحد", fontSize = 12.sp, color = Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoneyScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Text("المال", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("تابع مصروفك بطريقة بسيطة", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        Surface(color = Forest, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp)) {
                Text("المتبقي من ميزانيتك", color = White.copy(alpha = .78f), fontSize = 13.sp)
                Spacer(Modifier.height(7.dp))
                Text("٢٤٥٫٧٠ د.أ", color = White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                LinearProgressIndicator(progress = { .62f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Color(0xFFB6E3CC), trackColor = White.copy(alpha = .2f))
                Spacer(Modifier.height(8.dp))
                Text("صرفت ٦٢٪ من ميزانيتك الشهرية", color = White.copy(alpha = .82f), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FinanceStat("الدخل", "٨٥٠ د.أ", "هذا الشهر", Modifier.weight(1f), Forest)
            FinanceStat("المصروف", "٣٢٤٫٣٠ د.أ", "هذا الشهر", Modifier.weight(1f), Color(0xFFB97832))
        }
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("آخر الحركات", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink, modifier = Modifier.weight(1f))
            TextButton(onClick = {}) { Text("عرض الكل", color = Forest) }
        }
        FinanceRow("سوبرماركت", "اليوم · ١١:٢٠ ص", "− ١٨٫٥٠ د.أ", "🛒")
        FinanceRow("مواصلات", "أمس · ٤:٤٠ م", "− ٣٫٠٠ د.أ", "🚕")
        FinanceRow("راتب", "١ نوفمبر", "+ ٨٥٠ د.أ", "↗")
    }
}

@Composable
private fun FinanceStat(title: String, amount: String, caption: String, modifier: Modifier = Modifier, color: Color) {
    Surface(color = White, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text(amount, color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(caption, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FinanceRow(title: String, subtitle: String, amount: String, emoji: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Pale), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 18.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
        Text(amount, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
    HorizontalDivider(color = Line)
}

@Composable
private fun MoneyPeekCard() {
    Surface(color = White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Mint), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = Forest)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("مصروفك هذا الشهر", fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 14.sp)
                Text("تابع ميزانيتك من قسم المال", color = Muted, fontSize = 12.sp)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    var reminders by remember { mutableStateOf(true) }
    var automaticPlan by remember { mutableStateOf(true) }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Text("الإعدادات", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("خصص Nomi Smart على طريقتك", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))
        Surface(color = White, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                SettingRow(Icons.Outlined.PersonOutline, "الملف الشخصي", "الاسم ونمط الاستخدام")
                HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                SettingRow(Icons.Outlined.WorkOutline, "مساحاتك", "شخصي · دراسة · عمل")
                HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                SettingToggle(Icons.Outlined.NotificationsNone, "التذكيرات", "تنبيهات مهامك ومواعيدك", reminders) { reminders = it }
                HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                SettingToggle(Icons.Outlined.CalendarMonth, "ترتيب الجدول تلقائيًا", "حسب عاداتك المتكررة", automaticPlan) { automaticPlan = it }
                HorizontalDivider(color = Line, modifier = Modifier.padding(horizontal = 16.dp))
                SettingRow(Icons.Outlined.Tune, "الخصوصية والبيانات", "تحكم بما يتعلمه التطبيق")
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Nomi Smart · الإصدار 1.0", color = Muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().clickable {}.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Forest)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Muted)
    }
}

@Composable
private fun SettingToggle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Forest)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked, colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = Forest))
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("مهمة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("شو بدك تعمل؟") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onAdd(title.trim()) }) { Text("إضافة", color = Forest) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء", color = Muted) } },
        containerColor = White
    )
}
