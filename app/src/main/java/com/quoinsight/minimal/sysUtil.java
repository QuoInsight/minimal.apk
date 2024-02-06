package com.quoinsight.minimal;

public class sysUtil {

  //////////////////////////////////////////////////////////////////////

  static final public String getDeviceID(android.content.Context parentContext) {
    return android.provider.Settings.Secure.getString(
      parentContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID
    );
  }

  //////////////////////////////////////////////////////////////////////

  static final public boolean getPermission(android.app.Activity parentActivity, String permission) {
	if (parentActivity == null) parentActivity = MainActivity.getInstance();
	if ( androidx.core.content.ContextCompat.checkSelfPermission(
      //android.app.Activity.getApplicationContext()
	  parentActivity.getApplicationContext(), permission
	) == android.content.pm.PackageManager.PERMISSION_GRANTED ) {
      return true;
	} else {
	  //pushNotificationPermissionLauncher.launch(permission);
      androidx.core.app.ActivityCompat.requestPermissions(parentActivity, new String[]{permission},
        0 // this is a custom requestCode allowing the program to match this in the callback function
      );
	}
    return false;  
  }

  //////////////////////////////////////////////////////////////////////

  static final public void shareText(android.content.Context parentContext, String txt) {
    try {
      android.content.Intent intent = new android.content.Intent();
        // [ https://developer.android.com/training/sharing/send ]
        intent.setAction(android.content.Intent.ACTION_SEND);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, txt);
        intent.setType("text/plain");
      parentContext.startActivity(android.content.Intent.createChooser(intent, null));
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.shareText", e.getMessage());
    }
  }

  static final public void openFileWith(android.content.Context parentContext, String filePath) {
    try {
      android.content.Intent intent = new android.content.Intent();
        /*
          !! will need to use FileProvider.getUriForFile() to avoid the below error
          !! android.os.FileUriExposedException: "exposed beyond app through Intent.getData()"
        */
        intent.addFlags(
          android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
           // | android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        );
        //android.net.Uri contentUri = android.net.Uri.parse(url);
        //android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
        android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
          parentContext, parentContext.getApplicationContext().getPackageName()+".fprvdr", new java.io.File(filePath)
        );
        /*
          FileProvider is a special subclass of ContentProvider
          that facilitates secure sharing of files associated
          with an app by creating a content:// Uri for a file
          instead of a file:/// Uri.
          e.g. "file:///storage/emulated/0/Download"
               "content://downloads/all_downloads"
               "content://downloads/public_downloads"
               "content://downloads/my_downloads"
          file:///storage/emulated/0/Android/data/com.android.chrome/files/Download/icon.png
          # notepad C:\\usr2\apk\gradle\src\main\res\xml\provider_paths.xml
          #  error if the path is not declared in provider_paths.xml:
          #   !! Failed to find configured root that contains <...> !!
        */
        commonGui.writeMessage(parentContext, "sysUtil.openFileWith", contentUri.toString());
        parentContext.grantUriPermission(
          parentContext.getPackageName(), contentUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
        intent.setData(contentUri);
      parentContext.startActivity(android.content.Intent.createChooser(intent, null));
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.openFileWith", e.getMessage());
    }
  }

  static final public void launchUrl(android.content.Context parentContext, String url) {
    try {
      if ( url.startsWith("file://") ) {
        // launchUrl with "file://..." will cause android.os.FileUriExposedException:
        //   !!  "exposed beyond app through Intent.getData()"  !!
        // will need to handle this differently
        //   may add the below in Application.onCreate() as a workaround in diagnotic mode 
        //   StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        //   StrictMode.setVmPolicy(builder.build());
        //   !! above overrides builder.detectFileUriExposure()
        // !! should use FileProvider.getUriForFile() instead !!
        openFileWith(parentContext, url.replace("file://", ""));
        return;
      }
      android.content.Intent intent = new android.content.Intent();
        /*
         intent = new android.content.Intent("com.android.browser");
           intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
           intent.setComponent(new android.content.ComponentName("com.android.browser", "com.android.browser.BrowserActivity"));
           intent.setData(android.net.Uri.parse(url));
           // Unable to find explicit activity class {}; have you declared this activity in your AndroidManifest.xml?
         parentContext.startActivity(intent);
        */
        // [ https://developer.android.com/guide/components/intents-common#ViewUrl ]
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.addCategory(android.content.Intent.CATEGORY_BROWSABLE);
        intent.setData(android.net.Uri.parse(url));
        //intent.setDataAndType(android.net.Uri.parse(url), "*/*");
      //parentContext.startActivity(android.content.Intent.createChooser(intent, "Open"));
      parentContext.startActivity(intent);
    } catch(Exception e) {
      commonGui.writeMessage(parentContext, "sysUtil.launchUrl", e.getMessage());
    }
  }

  //////////////////////////////////////////////////////////////////////

  static final public int getMusicVolume(android.content.Context parentContext) {
    try {
      // [ https://developer.android.com/reference/android/media/AudioManager ]
      android.media.AudioManager audMgr
        = (android.media.AudioManager) parentContext.getSystemService(parentContext.AUDIO_SERVICE);
      int minVol = audMgr.getStreamMinVolume(android.media.AudioManager.STREAM_MUSIC);
      int maxVol = audMgr.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
      int curVol = audMgr.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
      return (int)(100.0*((1.0*curVol)/(maxVol-minVol-0.0)));
    } catch(Exception e) {}
    return -1;
  }

  static final public int setMusicVolume(android.content.Context parentContext, int volLevel) {
    try {
      // [ https://developer.android.com/reference/android/media/AudioManager ]
      android.media.AudioManager audMgr
        = (android.media.AudioManager) parentContext.getSystemService(parentContext.AUDIO_SERVICE);
      int minVol = audMgr.getStreamMinVolume(android.media.AudioManager.STREAM_MUSIC);
      int maxVol = audMgr.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
      audMgr.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, (int)((volLevel/100.0)*(maxVol-minVol)), 0);
      return audMgr.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
    } catch(Exception e) {}
    return -1;
  }

  static final public int setMusicVolSeekBar(android.widget.SeekBar volSeekBar) {
    try {
      volSeekBar.setProgress(getMusicVolume(volSeekBar.getContext()));
    } catch(Exception e) {}
    return -1;
  }

  static final public int adjustMusicVolume(android.content.Context parentContext, int adjOpt) {
    try {
      // [ https://developer.android.com/reference/android/media/AudioManager ]
      android.media.AudioManager audMgr = (android.media.AudioManager) parentContext.getSystemService(parentContext.AUDIO_SERVICE);
      audMgr.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, adjOpt, android.media.AudioManager.FLAG_SHOW_UI);
      // android.media.AudioManager.ADJUST_SAME : MUTE=-100|LOWER=-1|SAME=0|RAISE=+1|UNMUTE=+100|TOGGLE=+101
      // ADJUST_SAME|ADJUST_RAISE|ADJUST_LOWER|ADJUST_TOGGLE_MUTE|ADJUST_MUTE|ADJUST_UNMUTE

      int minVol = audMgr.getStreamMinVolume(android.media.AudioManager.STREAM_MUSIC);
      int maxVol = audMgr.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
      int curVol = audMgr.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
      commonGui.writeMessage(
        parentContext, "sysUtil.adjustMusicVolume",
        "min="+String.valueOf(minVol) + "; vol=" + String.valueOf(curVol) + "; max="+String.valueOf(maxVol)
      );
      return 0;
    } catch(Exception e) {}
    return -1;
  }

  //////////////////////////////////////////////////////////////////////

  static final public int getBatteryLevel(android.content.Context parentContext) {
    try {
      android.os.BatteryManager battMgr
        = (android.os.BatteryManager) parentContext.getSystemService(parentContext.BATTERY_SERVICE);
      return battMgr.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY);
    } catch(Exception e) {}
    return -1;
  }

  //////////////////////////////////////////////////////////////////////

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
      parentContext, 0, new android.content.Intent(parentContext, targetClass),
	  android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
    );
  }

  public static android.app.PendingIntent getPendingService(
    android.content.Context parentContext, Class<?> targetClass, String actionString, int requestCode
  ) {
    android.content.Intent intent = new android.content.Intent(parentContext, targetClass);
    intent.setAction(actionString);
    return android.app.PendingIntent.getService(parentContext, requestCode, intent,
      android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
	);
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
