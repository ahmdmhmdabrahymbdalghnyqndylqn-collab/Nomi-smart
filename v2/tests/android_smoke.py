"""Run real APK UI tests on a fresh emulator, preserving diagnostic evidence."""
import sys, re, time, subprocess, pathlib, xml.etree.ElementTree as ET, unicodedata
variant=sys.argv[1]; pkg='com.nomismart.preview.'+variant
out=pathlib.Path('qa-screenshots')/variant;out.mkdir(parents=True,exist_ok=True)
def adb(*args): return subprocess.check_output(['adb',*args],timeout=45)
def tree():
    adb('shell','uiautomator','dump','/sdcard/nomi-test.xml')
    raw=adb('shell','cat','/sdcard/nomi-test.xml')
    (out/'last-ui.xml').write_bytes(raw)
    return ET.fromstring(raw)
def norm(s):
    return ''.join(c for c in unicodedata.normalize('NFKD',s or '') if not unicodedata.combining(c)).strip()
def tap(n):
    x,y,a,b=map(int,re.findall(r'\d+',n.get('bounds','')))
    adb('shell','input','tap',str((x+a)//2),str((y+b)//2))
    time.sleep(.8)
def launcher_dialog(root):
    nodes=list(root.iter('node'))
    texts=' '.join(n.get('text','') for n in nodes)
    if "Pixel Launcher isn't responding" in texts or "Launcher3 isn't responding" in texts:
        for n in nodes:
            if n.get('text')=='Close app':
                tap(n);print('Dismissed unrelated launcher ANR',flush=True);return True
    return False
def find(label=None,cls=None):
    end=time.monotonic()+100
    while time.monotonic()<end:
        try:
            root=tree()
            if launcher_dialog(root):continue
            for n in root.iter('node'):
                if n.get('package')!=pkg:continue
                if (label is not None and norm(label) in (norm(n.get('text')),norm(n.get('content-desc')))) or (cls and n.get('class')==cls):return n
        except (subprocess.SubprocessError,ET.ParseError):pass
        time.sleep(1)
    raise AssertionError('Missing visible control: '+str(label or cls))
def shot(name):
    (out/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
def open_route(path,title):
    adb('shell','am','start','-W','-a','android.intent.action.VIEW','-d','nomiv2'+variant+'://'+path,pkg)
    find(title);find('تغيير المظهر')
def capture(name):
    shot(name)
    (out/(name+'.xml')).write_bytes(adb('shell','cat','/sdcard/nomi-test.xml'))
    print('Captured '+name,flush=True)
try:
    adb('shell','settings','put','global','anr_show_background','0')
    adb('shell','am','force-stop','com.google.android.apps.nexuslauncher')
    adb('install','-r','apk/Nomi-'+variant+'-Preview.apk')
    adb('shell','pm','clear',pkg)
    adb('logcat','-c')
    print(adb('shell','am','start','-W','-n',pkg+'/.MainActivity').decode(),flush=True)
    find('أهلًا بك في نومي');capture('login-light')
    tap(find(cls='android.widget.EditText'));adb('shell','input','text','0791234567');adb('shell','input','keyevent','4')
    tap(find('ابدأ استخدام نومي'));find('أسبوعك');capture('home-light')
    tap(find('تغيير المظهر'));find('أسبوعك');capture('home-dark')
    routes={'tasks':'المهام','money':'الأموال','alarms':'المنبه','voice':'احكي مع نومي','settings':'الإعدادات','history':'السجل السابق','suggestions':'اقتراحات نومي','prayer':'الصلاة والأذان','weather':'الطقس','reports':'التقارير','notifications':'الإشعارات','add':'إضافة'}
    for route,title in routes.items():
        open_route(route,title);capture(route+'-dark')
        tap(find('تغيير المظهر'));find(title);capture(route+'-light')
        tap(find('تغيير المظهر'))
    print('Smoke test completed for '+variant,flush=True)
except Exception:
    try:
        shot('failure')
        print(adb('shell','dumpsys','activity','activities').decode(errors='replace')[-8000:],flush=True)
    except Exception:pass
    raise
finally:
    try:
        (out/'logcat.txt').write_bytes(adb('logcat','-d','-t','1800'))
        (out/'activity.txt').write_bytes(adb('shell','dumpsys','activity','activities'))
    except Exception:pass
