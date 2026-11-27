"""Smoke test on a fresh Android emulator: real APK, local sign-in, both themes, screenshot evidence."""
import os, sys, re, time, subprocess, pathlib, xml.etree.ElementTree as ET
variant=sys.argv[1]; pkg='com.nomismart.preview.'+variant
out=pathlib.Path('qa-screenshots')/variant;out.mkdir(parents=True,exist_ok=True)
def adb(*args): return subprocess.check_output(['adb',*args],timeout=35)
def tree():
    adb('shell','uiautomator','dump','/sdcard/nomi-test.xml')
    return ET.fromstring(adb('shell','cat','/sdcard/nomi-test.xml'))
def find(label=None,cls=None):
    for _ in range(15):
        try:
            for n in tree().iter('node'):
                if (label is not None and (n.get('text')==label or n.get('content-desc')==label)) or (cls and n.get('class')==cls):return n
        except (subprocess.SubprocessError,ET.ParseError):pass
        time.sleep(1)
    raise AssertionError('Missing visible control: '+str(label or cls))
def tap(n):
    x,y,a,b=map(int,re.findall(r'\d+',n.get('bounds','')))
    adb('shell','input','tap',str((x+a)//2),str((y+b)//2))
    time.sleep(1)
def shot(name):
    (out/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'))
def open_route(path):
    adb('shell','am','start','-W','-a','android.intent.action.VIEW','-d','nomiv2'+variant+'://'+path,pkg)
    time.sleep(4)
try:
    adb('install','-r','apk/Nomi-'+variant+'-Preview.apk')
    adb('shell','pm','clear',pkg)
    adb('shell','monkey','-p',pkg,'-c','android.intent.category.LAUNCHER','1')
    find('أهلًا بك في نومي');shot('login-light')
    tap(find(cls='android.widget.EditText'));adb('shell','input','text','0791234567');adb('shell','input','keyevent','4')
    tap(find('ابدأ استخدام نومي'));find('أسبوعك');shot('home-light')
    tap(find('تغيير المظهر'));find('أسبوعك');shot('home-dark')
    for route in ['tasks','money','alarms','voice','settings','history','suggestions','prayer','weather','reports','notifications']:
        open_route(route);find('تغيير المظهر');shot(route+'-dark')
        tap(find('تغيير المظهر'));find('تغيير المظهر');shot(route+'-light')
        tap(find('تغيير المظهر'))
    open_route('voice');shot('voice-dark-final')
    print('Smoke test completed for '+variant)
except Exception:
    shot('failure');raise
