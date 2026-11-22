set -euo pipefail
pkg=com.nomismart.app
sdk=$(find "$ANDROID_HOME/platforms" -name android.jar | sort -V | tail -1)
bt=$(dirname "$(find "$ANDROID_HOME/build-tools" -name aapt | sort -V | tail -1)")
mkdir -p /tmp/qa-classes /tmp/qa-dex
cat > /tmp/AndroidManifest.xml <<'EOF'
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="qa.nomi"><uses-sdk android:minSdkVersion="23" android:targetSdkVersion="28"/><application android:hasCode="true"/><instrumentation android:name="qa.nomi.Check" android:targetPackage="qa.nomi"/></manifest>
EOF
javac -source 8 -target 8 -cp "$sdk" -d /tmp/qa-classes qa/Check.java
"$bt/d8" --lib "$sdk" --min-api 23 --output /tmp/qa-dex /tmp/qa-classes/qa/nomi/Check.class
"$bt/aapt" package -f -M /tmp/AndroidManifest.xml -I "$sdk" -F /tmp/probe.apk
(cd /tmp/qa-dex && zip -q /tmp/probe.apk classes.dex)
keytool -genkeypair -alias test -keystore /tmp/ui-test.jks -storepass android -keypass android -keyalg RSA -dname CN=DisposableTest
"$bt/apksigner" sign --ks /tmp/ui-test.jks --ks-pass pass:android /tmp/probe.apk
adb install /tmp/probe.apk
adb shell wm size 1080x3200
adb shell wm density 250
adb install -g /tmp/old.apk
for phase in before after; do
 if [[ $phase == after ]]; then
  adb shell am force-stop "$pkg"
  adb install -r qa/NOMI-23.apk
  adb shell dumpsys package "$pkg" | grep 'versionCode=23 ' >/dev/null
 fi
 adb shell monkey -p "$pkg" 1 >/dev/null
 sleep 3
 adb shell am instrument -w -e phase "$phase" qa.nomi/.Check | tee "/tmp/result-$phase"
 grep -q 'result=PASS' "/tmp/result-$phase"
done
diff <(grep 'ledger=' /tmp/result-before) <(grep 'ledger=' /tmp/result-after)
