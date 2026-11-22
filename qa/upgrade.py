import re
import subprocess
import time
import unicodedata
import uiautomator2 as u2

pkg = "com.nomismart.app"
def adb(*args):
    return subprocess.check_output(["adb", *args], text=True)

d = u2.connect()
def button(*names):
    selector = d(textMatches="^(?:" + "|".join(map(re.escape, names)) + ")$")
    selector.click(timeout=15)

def form_inputs():
    fields = d(className="android.widget.EditText")
    assert fields.wait(timeout=12)
    return fields

def enter(field, value):
    field.set_text(value)
    if "mInputShown=true" in adb("shell","dumpsys","input_method"):
        d.press("back")

def contents():
    return " ".join(re.findall(r'(?:text|content-desc)="([^"]+)"', d.dump_hierarchy()))

def wait_text(value):
    for _ in range(20):
        s = contents()
        if value in s:
            return s
        time.sleep(.5)
    raise AssertionError("Missing "+value+" in "+s[:2000])

def task(title):
    button("المهام","Tasks")
    enter(form_inputs()[0], title)
    button("إضافة مهمة","Add task")
    wait_text(title)

def money():
    button("المال","Money")

def normalized(s):
    return "".join(str(unicodedata.decimal(c)) if c.isdecimal() else c for c in s).replace("٫",".")

# Tall viewport keeps the form, ledger and navigation visible together.
adb("shell","wm","size","1080x3200")
adb("shell","wm","density","250")
assert "Success" in adb("install","-g","/tmp/old.apk")
d.app_start(pkg)
fields = form_inputs()
enter(fields[0], "Nomi QA")
enter(fields[1], "0770000000")
button("اختر الدولة","Select country")
button("الأردن","Jordan")
button("الدخول","Continue")
task("NomiBefore")
check = d(descriptionMatches="إنجاز|Complete")
if not check.exists:
    check = d(textMatches="إنجاز|Complete")
if not check.exists:
    check = d(className="android.widget.CheckBox")[-1]
check.click()
assert check.info["checked"]
money()
fields = form_inputs()
# Date, description, amount and month may each have an EditText role.
desc = d(descriptionMatches="الوصف|Description")
amount = d(descriptionMatches=".*(?:المبلغ|Amount).*")
editable = [w for w in fields if w.info.get("className") == "android.widget.EditText"]
if desc.exists and amount.exists:
    enter(desc,"NomiPayment")
    enter(amount,"42")
else:
    empty = [w for w in editable if not w.info.get("text")]
    assert len(empty)==2
    enter(empty[0],"NomiPayment")
    enter(empty[1],"42")
button("إضافة حركة","Add transaction")
before = normalized(wait_text("NomiPayment"))
assert "42" in before
before_date=adb("shell","date","+%Y-%m-%d").strip()
uid_before=adb("shell","stat","-c","%u","/data/data/"+pkg).strip()
d.app_stop(pkg)
assert "Success" in adb("install","-r","qa/NOMI-23.apk")
assert re.search(r"versionCode=23\b",adb("shell","dumpsys","package",pkg))
assert adb("shell","stat","-c","%u","/data/data/"+pkg).strip()==uid_before
d.app_start(pkg)
button("المهام","Tasks")
wait_text("NomiBefore")
assert d(className="android.widget.CheckBox",checked=True).exists
money()
after=normalized(wait_text("NomiPayment"))
assert "42" in after and before==after, (before,after)
assert before_date==adb("shell","date","+%Y-%m-%d").strip()
task("NomiAfter")
print("Verified in-place upgrade, completion state, ledger contents and post-upgrade write.")
