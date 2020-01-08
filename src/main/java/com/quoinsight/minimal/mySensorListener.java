package com.quoinsight.minimal;
/*
  adapted from https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/Compass.java 
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/mySensorListener.java

  http://pages.iu.edu/~rwisman/c490/html/android-sensors.htm
  https://www.vogella.com/tutorials/AndroidSensor/article.html
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
    void OnAzimuthDataLoaded(float azimuth);
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
  }

  public java.util.Date lastDataTimeStamp = new java.util.Date();

  public int gTimeInterval = 500; // milliseconds
  public boolean register(final int timeInterval, final long timeout) {
    try { unregister(); } catch(Exception e) {}

    gTimeInterval = timeInterval;  int delayRate = timeInterval * 1000;
    // delayRate = SensorManager.SENSOR_DELAY_GAME; // == 20,000 microseconds

    //gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delayRate);
    //gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), delayRate);

    if (gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER), delayRate)) {
      if (gSensorMgr.registerListener(this, gSensorMgr.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD), delayRate)) {
        unregisterOnTimeout(timeout);  // (long)(10*delay);
        return true;
      } else {
        writeMessage("mySensorEventListener.register", "failed on TYPE_MAGNETIC_FIELD");
        return false;
      }
    } else {
      writeMessage("mySensorEventListener.register", "failed on TYPE_ACCELEROMETER");
      return false;
    }
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

  private float[] gAccelVals = new float[3], gMagVals = new float[3];

  private float getAzimuthValueFromSensorData(
    android.hardware.SensorManager sensorMgr,
    float[] accelVals, float[] magVals, float azimuthFix
  ) {
    // https://stackoverflow.com/questions/7046608/getrotationmatrix-and-getorientation-tutorial
    // https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/Compass.java
    float azimuthValue = -1f;
    try {
      synchronized (this) {
        float[] rotationMatrix = new float[16], baseOrientation = new float[4];

        if (sensorMgr.getRotationMatrix(rotationMatrix, null, accelVals, magVals)) {
          sensorMgr.getOrientation(rotationMatrix, baseOrientation);
          azimuthValue = (float) Math.toDegrees(baseOrientation[0]); // orientation
          azimuthValue = (azimuthValue + azimuthFix + 360) % 360;
        } else {
          // getRotationMatrix() failed, perhaps accelVals/magVals not yet ready
        }
      }
    } catch(Exception e) {
      writeMessage("mySensorEventListener.getAzimuthValueFromSensorData", e.getMessage());
    }
    return azimuthValue;
  }

  @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }

  @Override public void onSensorChanged(SensorEvent event) {
    final float alpha = 0f, azimuthFix = 0f;
    java.util.Date thisDataTimeStamp = new java.util.Date();

    // ignore/skip this to reduce the update frequency
    // if ( thisDataTimeStamp.getTime() - lastDataTimeStamp.getTime() < gTimeInterval ) return;

    synchronized (this) {
      switch ( event.sensor.getType() ) {
        /*
          https://www.built.io/blog/applying-low-pass-filter-to-android-sensor-s-readings
           applying low-pass/high-cut filter with a simple filter constant (alpha)
           in order to remove the noises/data with value above the cutoff limit
          https://developer.android.com/guide/topics/sensors/sensors_motion
           alpha = tau/(tau+dT) [The code sample uses an alpha value of 0.8 for demonstration purposes]
           [alpha==1-α], is this correct ?? it is different from what explained in wikipedia !!
          https://en.wikipedia.org/wiki/Low-pass_filter
           α = dT/(dT+RC) == 0..1
             dT == event delivery rate ( 20ms ==> 20/1000 == 0.02)
             RC == time-constant, also called "tau" == 1/(2*pi*fc)
                   fc==cut off frequency; many people set tau=0.2 ??!!
          alpha=0 ==> no filter applies !
        */
        case android.hardware.Sensor.TYPE_ACCELEROMETER:
          gAccelVals[0] = (1-alpha)*event.values[0] + alpha*gAccelVals[0];
          gAccelVals[1] = (1-alpha)*event.values[1] + alpha*gAccelVals[1];
          gAccelVals[2] = (1-alpha)*event.values[2] + alpha*gAccelVals[2];
          break;
        case android.hardware.Sensor.TYPE_MAGNETIC_FIELD:
          gMagVals[0] = (1-alpha)*event.values[0] + alpha*gMagVals[0];
          gMagVals[1] = (1-alpha)*event.values[1] + alpha*gMagVals[1];
          gMagVals[2] = (1-alpha)*event.values[2] + alpha*gMagVals[2];
          break;
        default:
          return;
      }
      float azimuth = getAzimuthValueFromSensorData(gSensorMgr, gAccelVals, gMagVals, azimuthFix);
      if (azimuth >= 0) {
        lastDataTimeStamp = thisDataTimeStamp;
        if (gHandlers != null) gHandlers.OnAzimuthDataLoaded(azimuth);
      }
    }
  }

}
