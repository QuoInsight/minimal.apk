package com.quoinsight.minimal;

public class sysUtil {

  //////////////////////////////////////////////////////////////////////

  static final public String getDeviceID(android.content.Context parentContext) {
    return android.provider.Settings.Secure.getString(
      parentContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID
    );
  }

  //////////////////////////////////////////////////////////////////////

  static final public void launchUrl(android.content.Context parentContext, String url) {
    try {
      android.content.Intent intent = new android.content.Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.addCategory(android.content.Intent.CATEGORY_BROWSABLE);
        if ( url.startsWith("file://") ) {
          // !! android.os.FileUriExposedException: "exposed beyond app through Intent.getData()" !!
          intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
          intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        }
        intent.setData(android.net.Uri.parse(url));
        //intent.setDataAndType(android.net.Uri.parse(url), "*/*");
      //parentContext.startActivity(android.content.Intent.createChooser(intent, "Open"));
      parentContext.startActivity(intent);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.launchUrl", e.getMessage());
    }
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
      commonGui.writeMessage((android.content.Context)parentActivity, "sysUtil.launchAppMgr", e.getMessage());
      return;
    }
  }

  static final public void launchAppInfo(android.content.Context parentContext, String pkgName) {
    try {
      android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(android.net.Uri.parse("package:" + pkgName));
      parentContext.startActivity(intent);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.launchAppInfo", e.getMessage());
      return;
    }
  }

  static final public void launchApp(android.content.Context parentContext, String pkgName) {
    try {
      android.content.Intent intent = parentContext.getPackageManager().getLaunchIntentForPackage(pkgName);
      parentContext.startActivity(intent);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.launchApp", "[" + pkgName + "] " + e.getMessage());
      return;
    }
  }

  //////////////////////////////////////////////////////////////////////

  public static android.app.PendingIntent getPendingActivity(
    android.content.Context parentContext, Class<?> targetClass
  ) {
    return android.app.PendingIntent.getActivity(
      parentContext, 0, new android.content.Intent(parentContext, targetClass), 0
    );
  }

  public static android.app.PendingIntent getPendingService(
    android.content.Context parentContext, Class<?> targetClass, String actionString, int requestCode
  ) {
    android.content.Intent intent = new android.content.Intent(parentContext, targetClass);
    intent.setAction(actionString);
    return android.app.PendingIntent.getService(parentContext, requestCode, intent, 0);
  }

  //////////////////////////////////////////////////////////////////////

  /*

    https://developer.android.com/guide/topics/location/strategies.html
    gps –> (GPS, AGPS) Requires the permission android.permission.ACCESS_FINE_LOCATION.
    network –> (AGPS, CellID, WiFi MACID) Requires either of the permissions android.permission.ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION.
    passive –> (CellID, WiFi MACID) Requires the permission android.permission.ACCESS_FINE_LOCATION, although if the GPS is not enabled this provider might only return coarse fixes.

    #  Settings -> Location & security and enable flag "Use wireless networks"
    #  in "My Location" group.

    android.location.LocationListener locListener
     = new android.location.LocationListener() {
      @Override public void onLocationChanged(android.location.Location location) {
        updateLocation(location);
      }
      @Override public void onProviderEnabled(String provider) { }
      @Override public void onProviderDisabled(String provider) { }
      @Override public void onStatusChanged(String provider, int status, android.os.Bundle extras) { } // deprecated !!
    };

    locMgr.requestLocationUpdates(prvdr, 2000, 10, locListener);
    locMgr.removeUpdates(locListener);
  */

  static final public android.location.Location getLastKnownLocation(
    android.content.Context parentContext, String prvdr
  ) {
    try {
     /* // adding android.support.v4 will increase the apk size by 500k !
      if (
        android.support.v4.app.ActivityCompat.checkSelfPermission(
          parentContext, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
      ) {
        commonGui.writeMessage(parentContext, "sysUtil.getLastKnownLocation", "PERMISSION_GRANTED");
      } else {
        commonGui.writeMessage(parentContext, "sysUtil.getLastKnownLocation", "requestPermissions");
        // below will be work without specifying it first in AndroidManifest.xml ??
        android.support.v4.app.ActivityCompat.requestPermissions(
          (android.app.Activity)parentContext,
          new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
          1
        );
        //return (android.location.Location)null;
      }
     */

      // https://github.com/MiCode/Compass/blob/master/src/net/micode/compass/CompassActivity.java
      android.location.LocationManager locMgr
        = (android.location.LocationManager) parentContext.getSystemService(parentContext.LOCATION_SERVICE);
      if (prvdr==null || prvdr.length()==0 ) {
        android.location.Criteria criteria = new android.location.Criteria();
        criteria.setAccuracy(android.location.Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(android.location.Criteria.POWER_LOW);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        // LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        prvdr = locMgr.getBestProvider(criteria, true); // "network" | "gps"
        commonGui.writeMessage(parentContext, "sysUtil.getBestProvider", prvdr);
      }
      return locMgr.getLastKnownLocation(prvdr);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.getLastKnownLocation", e.getMessage());
      return (android.location.Location)null;
    }
  }

  //////////////////////////////////////////////////////////////////////

}