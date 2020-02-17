package com.quoinsight.minimal;
/*
  adapted from https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/Compass.java 
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/mySensorListener.java

  http://pages.iu.edu/~rwisman/c490/html/android-sensors.htm
  https://www.vogella.com/tutorials/AndroidSensor/article.html

  https://stackoverflow.com/questions/11101146/listener-vs-handler-in-android
*/

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class mySensorListener implements SensorEventListener {
  private static final String TAG = "mySensorListener";

  public interface handlers {
    void OnMessage(String tag, String msg, String...args);
    void OnOrientationDataLoaded(float[] returnVals);
    void OnAccel2g(float gForce);
  }

  private handlers gHandlers = null;
  public void setHandlers(handlers h) {
    gHandlers = h;
  }

  public void writeMessage(String tag, String msg, String...args) {  // varargs
    if (gHandlers != null) gHandlers.OnMessage(tag, msg, args);
    //android.util.Log.e(tag, msg);
    return;
  }

  private SensorManager gSensorMgr = null;
  public mySensorListener(android.content.Context parentContext) {
    gSensorMgr = (SensorManager) parentContext.getSystemService(Context.SENSOR_SERVICE);

    gSensorAccuracy.put( "sensorTypeLookup#" + String.valueOf(android.hardware.Sensor.TYPE_ACCELEROMETER), "ACCELEROMETER" );
    gSensorAccuracy.put( "sensorTypeLookup#" + String.valueOf(android.hardware.Sensor.TYPE_MAGNETIC_FIELD), "MAGNETIC_FIELD" );
    gSensorAccuracy.put( "sensorTypeLookup#" + String.valueOf(android.hardware.Sensor.TYPE_ORIENTATION), "ORIENTATION" );
    gSensorAccuracy.put( "sensorTypeLookup#" + String.valueOf(android.hardware.Sensor.TYPE_ACCELEROMETER), "ACCELEROMETER" );

    gSensorAccuracy.put( "accuracyTypeLookup#" + String.valueOf(android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW), "LOW" );
    gSensorAccuracy.put( "accuracyTypeLookup#" + String.valueOf(android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM), "MEDIUM" );
    gSensorAccuracy.put( "accuracyTypeLookup#" + String.valueOf(android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH), "HIGH" );
    gSensorAccuracy.put( "accuracyTypeLookup#" + String.valueOf(android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE), "UNRELIABLE" );
  }

  public java.util.Date lastDataTimeStamp = new java.util.Date();
  public String gSensorTypes = "orient";  // orient==ORIENTATION | acclMgnt==ACCELERO_MAGNETIC
  public int gTimeInterval = 500; // milliseconds
  
  public boolean register(final String sensorTypes, final int timeInterval, final long timeout) {
    try { unregister(); } catch(Exception e) {}

    int sensorType = -1;
    gTimeInterval = timeInterval;  int delayRate = timeInterval * 1000;
    // delayRate = SensorManager.SENSOR_DELAY_GAME; // == 20,000 microseconds

    //gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delayRate);
    //gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), delayRate);

    if ( gSensorTypes.contains("orient") ) {  // deprecated
      sensorType = android.hardware.Sensor.TYPE_ORIENTATION;
      gSensorAccuracy.remove(String.valueOf(sensorType));
      if (! gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(sensorType), delayRate)) {
        writeMessage("mySensorEventListener.register", "failed on TYPE_ORIENTATION");
        return false;
      }
    }

    if ( gSensorTypes.contains("acclmgnt") ) {
      sensorType = android.hardware.Sensor.TYPE_ACCELEROMETER;
      gSensorAccuracy.remove(String.valueOf(sensorType));
      if (gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(sensorType), delayRate)) {
        sensorType = android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
        gSensorAccuracy.remove(String.valueOf(sensorType));
        if (! gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(sensorType), delayRate)) {
          writeMessage("mySensorEventListener.register", "failed on TYPE_MAGNETIC_FIELD");
          return false;
        }
      } else {
        writeMessage("mySensorEventListener.register", "failed on TYPE_ACCELEROMETER");
        return false;
      }
    }

    unregisterOnTimeout(timeout);  // (long)(10*delay);
    return true;
  }

  public void unregister() {
    gSensorMgr.unregisterListener(this);
  }

  // !! Do not use ReentrantLock() which will return immediately if the current thread already owns the lock !!
  //public final Object waitObj1 = new Object(); 
  public void unregisterOnTimeout(final long timeout) {
    // final long p_timeout = timeout;
    // if ( true ) return;
    final android.os.Handler handler = new android.os.Handler();
      handler.postDelayed(new Runnable(){@Override public void run(){ // !! must use this to avoid issue for UI thread !!
        try {
          unregister();
          writeMessage("mySensorEventListener.unregisterOnTimeout", "Done");
        } catch(Exception e) { 
          writeMessage("mySensorEventListener.unregisterOnTimeout", e.getMessage());
        }
      }}, timeout);
    return;

    /*
      !! runOnUiThread with waitObj1.wait() will block the UI thread !! 
      //android.os.AsyncTask.execute(new Runnable(){@Override public void run(){ // !! may clash if use in UI thread !!
      runOnUiThread(new Runnable(){@Override public void run(){ // !! may use this for UI thread, but waitObj1.wait() will block the UI thread !!
        synchronized(waitObj1) { // must have this, else waitObj1.wait() will run into error: object not locked by thread before wait()
          try {
            waitObj1.wait(timeout);  // timeout − the maximum time to wait in milliseconds
            gSensorMgr.unregisterListener(setTextAndUnregisterOnSensorChanged);
            writeMessage("unregisterListenerOnTimeout", "Done");
          } catch(Exception e) { 
            writeMessage("unregisterListenerOnTimeout", e.getMessage());
          }
        }
      }});
    */

    /*
      // !! do not use the below for UI thread, this may clash the current UI thread !! 
      (new Thread(){public void run(){
        ...
      }}).start();
      (new Thread(new Runnable(){public void run(){
        ...
      }})).start();
    */
  }

/*
  // Important: To avoid the unnecessary usage of battery,
  // register the listener in the onResume method and de-register on the onPause method.

  @Override protected void onResume() {
    super.onResume();
    //if (gAzimuthTextView!=null) setTextOnSensorChanged(gAzimuthTextView, gSensorMgr);
  }
 
  @Override protected void onPause() {
    unregisterListener();  // gSensorMgr.unregisterListener(this);
    super.onPause();
  }
*/

  static java.util.Hashtable<String, String> gSensorAccuracy = new java.util.Hashtable<String, String>();
  private float[] gAccelVals = {-1f,-1f,-1f}, gMagVals = {-1f,-1f,-1f};  // new float[3]
  public final float gAccelAlpha = 0.8f, gMagAlpha = 0f;  // 0 ==> no filter applies !

  private float[] getDeviceOrientation(
    android.hardware.SensorManager sensorMgr, float[] accelVals, float[] magVals
  ) {
    // https://stackoverflow.com/questions/7046608/getrotationmatrix-and-getorientation-tutorial
    float[] returnVals = new float[3];  returnVals[0] = -9f;  // range of values is -π to π
    try {
      synchronized (this) {
        float[] rotationMatrix = new float[16];  // getRotationMatrix() returns either as a 3x3 or 4x4 row-major matrix depending on the length of the passed array
        if (sensorMgr.getRotationMatrix(rotationMatrix, null, accelVals, magVals)) {
          sensorMgr.getOrientation(rotationMatrix, returnVals);
          return returnVals;
        } else {
          // getRotationMatrix() failed, perhaps accelVals/magVals not yet ready
        }
      }
    } catch(Exception e) {
      writeMessage("mySensorEventListener.getDeviceOrientation", e.getMessage());
    }
    return returnVals;
  }

  private float getAzimuthValueFromSensorData(
    android.hardware.SensorManager sensorMgr,
    float[] accelVals, float[] magVals, float azimuthFix
  ) {
    // https://stackoverflow.com/questions/7046608/getrotationmatrix-and-getorientation-tutorial
    // https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/Compass.java
    float azimuthValue = -1f;
    try {
      float[] orientation = getDeviceOrientation(sensorMgr, accelVals, magVals);
      azimuthValue = (float) Math.toDegrees(orientation[0]);
      azimuthValue = (azimuthValue + azimuthFix + 360) % 360;
    } catch(Exception e) {
      writeMessage("mySensorEventListener.getAzimuthValueFromSensorData", e.getMessage());
    }
    return azimuthValue;
  }

  @Override public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
    String sensorTypeId = String.valueOf(sensor.getType());
    String accuracyTypeId = String.valueOf(accuracy);

    gSensorAccuracy.put( sensorTypeId, accuracyTypeId );

    /*
      String sensorType = "?";  switch ( sensor.getType() ) {
        case android.hardware.Sensor.TYPE_ACCELEROMETER: sensorType = "ACCELEROMETER"; break;
        case android.hardware.Sensor.TYPE_MAGNETIC_FIELD: sensorType = "MAGNETIC_FIELD"; break;
        case android.hardware.Sensor.TYPE_ORIENTATION: sensorType = "ORIENTATION"; break;
      }
      String accuracyType = "?";  switch ( accuracy ) {
        case android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW: accuracyType = "LOW"; break;
        case android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM: accuracyType = "MEDIUM"; break;
        case android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH: accuracyType = "HIGH"; break;
        case android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE: accuracyType = "UNRELIABLE"; break;
      }
    */

    String sensorType = gSensorAccuracy.get( "sensorTypeLookup#" + sensorTypeId );
      if (sensorType==null) sensorType = sensorTypeId;
    String accuracyType = gSensorAccuracy.get( "accuracyTypeLookup#" + accuracyTypeId );
      if (accuracyType==null) accuracyType = accuracyTypeId;
    gSensorAccuracy.put(sensorType, accuracyType);

    writeMessage("mySensorEventListener.onAccuracyChanged", sensorType+":"+accuracyType);
  }

  @Override public void onSensorChanged(SensorEvent event) {
    int sensorType = event.sensor.getType();
    float alpha = 0f;  float[] values = event.values.clone();
    java.util.Date thisDataTimeStamp = new java.util.Date();

    // ignore/skip this to reduce the update frequency
    // if ( thisDataTimeStamp.getTime() - lastDataTimeStamp.getTime() < gTimeInterval ) return;

    if (! gSensorAccuracy.containsKey(String.valueOf(sensorType))) {
      // https://stackoverflow.com/a/60256265/2940478
      onAccuracyChanged(event.sensor, event.accuracy);
    }

    synchronized (this) {
      switch ( sensorType ) {
        /*
          https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
           applying low-pass/high-cut filter with a simple filter constant (alpha)
           in order to remove the noises/data with value above the cutoff limit
          https://developer.android.com/guide/topics/sensors/sensors_motion
           alpha = tau/(tau+dT) [The code sample uses an alpha value of 0.8 for demonstration purposes]
           !! in wikipedia α==1-alpha !!
          https://en.wikipedia.org/wiki/Low-pass_filter
           α = dT/(dT+RC) == 0..1
             dT == event delivery rate ( 20ms ==> 20/1000 == 0.02)
             RC == time-constant, also called "tau" == 1/(2*𝜋*fc) ==> fc=50 ??!!
                   fc==cut off frequency; many people set tau=0.2 ??!!

          https://developer.android.com/guide/topics/sensors/sensors_position#sensors-pos-orient
            You don't usually need to perform any data processing or filtering
            of the device's raw orientation angles other than translating the
            sensor's coordinate system to your application's frame of reference.

          alpha=0 ==> no filter applies !
          alpha=0.8 ==> the previous output weights for 80% of the new output, and the new input (event.values) weights for 20%
                     ==> new value = (80% from old data) + (20% from new data)
          https://stackoverflow.com/questions/35190839/type-accelerometer-remove-the-gravity-the-meaning-of-t-tdt/35199741
        */

        case android.hardware.Sensor.TYPE_ACCELEROMETER:
          /*
            all values are in SI units (m/s^2) and measure contact forces
            values[0,1,2] == force applied by the device on the x,y,z-axis
          */
          alpha = (gAccelVals[0] >= 0) ? gAccelAlpha : 0;
          gAccelVals[0] = (1-alpha)*values[0] + alpha*gAccelVals[0];
          gAccelVals[1] = (1-alpha)*values[1] + alpha*gAccelVals[1];
          gAccelVals[2] = (1-alpha)*values[2] + alpha*gAccelVals[2];

          float gForce = (values[0]*values[0] + values[1]*values[1] + values[2]*values[2])
                       / (gSensorMgr.GRAVITY_EARTH * gSensorMgr.GRAVITY_EARTH);
          if (gHandlers != null && gForce >= 2) gHandlers.OnAccel2g(gForce);
          break;

        case android.hardware.Sensor.TYPE_MAGNETIC_FIELD:
          /*
            all values are in micro-Tesla (µT) and measure the ambient magnetic field
            values[0,1,2] == the ambient magnetic field in the x,y,z-axis; note: z-axis is inverted.
          */
          alpha = (gMagVals[0] >= 0) ? gMagAlpha : 0;
          gMagVals[0] = (1-alpha)*values[0] + alpha*gMagVals[0];
          gMagVals[1] = (1-alpha)*values[1] + alpha*gMagVals[1];
          gMagVals[2] = (1-alpha)*values[2] + alpha*gMagVals[2];
          break;

        case android.hardware.Sensor.TYPE_ORIENTATION:
          // deprecated: all values are angles in degrees instead of radians!
          if (gHandlers != null) {
            // converts the values toRadian for compatibility with output from the newer SensorManager.getOrientation()
            values[0] = (float)Math.toRadians((double)values[0]); // azimuth: 0° to 359°
            values[1] = (float)Math.toRadians((double)values[1]); // pitch (翘起): -180° (straight up) to 180° (upside/head down)
            values[2] = (float)Math.toRadians((double)values[2]); // roll (侧翻): -90° (facing left) to 90° (right)
            gHandlers.OnOrientationDataLoaded(values);
          }
          break;

        default:
          return;
      }

      float[] orientationData = getDeviceOrientation(gSensorMgr, gAccelVals, gMagVals);
      if (orientationData[0] > -3.141) {  // range of values is -π to π
        lastDataTimeStamp = thisDataTimeStamp;
        if (gHandlers != null) gHandlers.OnOrientationDataLoaded(orientationData);
      }
    }
  }

}
