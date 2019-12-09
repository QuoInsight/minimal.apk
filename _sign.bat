@echo off
set JAVA_HOME=C:\Program Files\Android\Android Studio\jre
"%JAVA_HOME%\bin\jarsigner.exe" ^
  -verbose -sigalg SHA1withRSA -digestalg SHA1 ^
  -keystore C:\Users\ckl036\.android\quoinsight.apk.keystore.jks ^
  -storepass ****** ^
  -keypass ****** ^
  -signedjar quoinsight.apk ^
  build\outputs\apk\release\gradle-release-unsigned.apk ^
  upload
pause
