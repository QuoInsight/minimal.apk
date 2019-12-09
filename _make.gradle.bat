@echo off
set JAVA_HOME="C:\Program Files\Android\Android Studio\jre"
set ANDROID_HOME=C:\Data\adm\mobile\Android\sdk
set GRADLE_EXE="C:\Program Files\Android\Android Studio\plugins\android\lib\templates\gradle\wrapper\gradlew.bat"

rem set JAVA_EXE=%JAVA_HOME%\bin\java.exe
rem C:\Data\adm\mobile\Android\sdk\build-tools\29.0.0
rem C:\Users\ckl036\.gradle\caches\transforms-2\files-2.1\f74e6ff14c4a8225c29151fa50076617\aapt2-3.5.2-5435860-windows

start notepad build.gradle
echo Next: run gradle ...
pause
call %GRADLE_EXE% assembleRelease

rem #Execution failed for task ':installDebug'.
rem # com.android.builder.testing.api.DeviceException: No connected devices!

start C:\Data\adm\mobile\Android\apk\gradle\build\outputs\apk
pause
