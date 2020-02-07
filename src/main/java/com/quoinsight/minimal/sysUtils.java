package com.quoinsight.minimal;

public class sysUtils {

  //////////////////////////////////////////////////////////////////////

  static final public String getDeviceID(android.content.Context parentContext) {
    return android.provider.Settings.Secure.getString(
      parentContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID
    );
  }

  //////////////////////////////////////////////////////////////////////

  static final public void launchUrl(android.content.Context parentContext, String url) {
    android.content.Intent intent = new android.content.Intent();
      intent.setAction(android.content.Intent.ACTION_VIEW);
      intent.addCategory(android.content.Intent.CATEGORY_BROWSABLE);
      intent.setData(android.net.Uri.parse(url));
    parentContext.startActivity(intent);
  }

  static final public int getBatteryLevel(android.content.Context parentContext) {
    try {
      android.os.BatteryManager battMgr
        = (android.os.BatteryManager) parentContext.getSystemService(parentContext.BATTERY_SERVICE);
      return battMgr.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY);
    } catch(Exception e) {}
    return -1;
  }

  static final public void enableTorchLigth(android.content.Context parentContext, boolean enabled) {
    android.hardware.camera2.CameraManager camMgr
      = (android.hardware.camera2.CameraManager)
          parentContext.getSystemService(parentContext.CAMERA_SERVICE);
    try {
      String cameraId = camMgr.getCameraIdList()[0];
      // camera.getParameters().getFlashMode()=="FLASH_MODE_TORCH" ?
      camMgr.setTorchMode(cameraId, enabled); // 🔦
    } catch(Exception e) {
      android.util.Log.e("MainActivity.enableTorchLigth", e.getMessage());
    }
  }

  //////////////////////////////////////////////////////////////////////

  static final public java.util.List<String> getPackageList(android.content.Context parentContext) {
    // https://devofandroid.blogspot.com/2018/02/get-list-of-user-installed-apps-with.html
    android.content.pm.PackageManager pkgMgr = parentContext.getPackageManager();
    java.util.List<String> pkgLst = new java.util.ArrayList<String>();
    for (android.content.pm.ApplicationInfo appInfo
      : pkgMgr.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
    ) {
      String pkgName = appInfo.packageName;
      if ( pkgName.startsWith("com.coloros.")||pkgName.startsWith("com.oppo.") ) {
        // skip Oppo apps/packages
      } else if ( (appInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ) {
        // skip system packages
      } else {
        String appName = appInfo.loadLabel(pkgMgr).toString().trim();
        pkgLst.add( appName + " [" + pkgName + "]" );
      }
      //Log.d(TAG, "Installed package :" + appInfo.packageName);
      //Log.d(TAG, "Source dir : " + appInfo.sourceDir);
      //Log.d(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(appInfo.packageName)); 
    }
    java.util.Collections.sort(pkgLst, String.CASE_INSENSITIVE_ORDER);  // java.util.Collections.reverseOrder()
    return pkgLst;
  }

  static final public java.util.List<String> getSensorList(android.content.Context parentContext) {
    // http://pages.iu.edu/~rwisman/c490/html/android-sensors.htm
    android.hardware.SensorManager sensorMgr = (android.hardware.SensorManager) parentContext.getSystemService(android.content.Context.SENSOR_SERVICE);
    java.util.List<String> sensorLst = new java.util.ArrayList<String>();

    final String[] typeArr = new String[] {
      "ACCELEROMETER", "MAGNETIC_FIELD", "ORIENTATION", "GYROSCOPE", "LIGHT", "PRESSURE", "TEMPERATURE", "PROXIMITY", "GRAVITY",
      "LINEAR_ACCELERATION", "ROTATION_VECTOR", "RELATIVE_HUMIDITY", "AMBIENT_TEMPERATURE", "MAGNETIC_FIELD_UNCALIBRATED",
      "GAME_ROTATION_VECTOR", "GYROSCOPE_UNCALIBRATED", "SIGNIFICANT_MOTION", "STEP_DETECTOR", "STEP_COUNTER",
      "GEOMAGNETIC_ROTATION_VECTOR", "HEART_RATE", "#22", "#23", "#24", "#25", "#26", "#27", "POSE_6DOF", "STATIONARY_DETECT",
      "MOTION_DETECT", "HEART_BEAT", "#32", "#33", "LOW_LATENCY_OFFBODY_DETECT", "ACCELEROMETER_UNCALIBRATED"
    };
    for (android.hardware.Sensor s : sensorMgr.getSensorList(android.hardware.Sensor.TYPE_ALL)) {
      int t = s.getType();  String sensorType = (t>0 && t<=typeArr.length) ? typeArr[t-1] : ("#"+Integer.toString(t));
      sensorLst.add(sensorType + ": " + s.getName());
    }

    java.util.Collections.sort(sensorLst, String.CASE_INSENSITIVE_ORDER);  // java.util.Collections.reverseOrder()
    return sensorLst;
  }

  //////////////////////////////////////////////////////////////////////

  static final public void launchAppMgr(android.app.Activity parentActivity) {
    try {
      parentActivity.startActivityForResult(new android.content.Intent(
        android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
      ), 0);
    } catch(Exception e) {
      commonGui.writeMessage((android.content.Context)parentActivity, "OtherActivity.launchAppMgr", e.getMessage());
      return;
    }
  }

  static final public void launchAppInfo(android.content.Context parentContext, String pkgName) {
    try {
      android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(android.net.Uri.parse("package:" + pkgName));
      parentContext.startActivity(intent);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "OtherActivity.launchAppInfo", e.getMessage());
      return;
    }
  }

  static final public void launchApp(android.content.Context parentContext, String pkgName) {
    try {
      android.content.Intent intent = parentContext.getPackageManager().getLaunchIntentForPackage(pkgName);
      parentContext.startActivity(intent);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "OtherActivity.launchApp", "[" + pkgName + "] " + e.getMessage());
      return;
    }
  }

  //////////////////////////////////////////////////////////////////////

}