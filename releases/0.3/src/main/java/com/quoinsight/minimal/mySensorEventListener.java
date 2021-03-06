package com.quoinsight.minimal;
/*
  adapted from https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/mySensorEventListener.java 
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/SensorEventListener.java

  http://pages.iu.edu/~rwisman/c490/html/android-sensors.htm
  https://www.vogella.com/tutorials/AndroidSensor/article.html
*/

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class mySensorEventListener implements SensorEventListener {
  private static final String TAG = "mySensorEventListener";

  public interface listenerAction {
    void OnAzimuthDataLoaded(float azimuth);
  }

  private listenerAction gAction;

  private SensorManager sensorManager;
  private Sensor gsensor;
  private Sensor msensor;

  private float[] mGravity = new float[3];
  private float[] mGeomagnetic = new float[3];
  private float[] R = new float[9];
  private float[] I = new float[9];

  private float azimuth;
  private float azimuthFix;

  public java.util.Date lastDataTimeStamp = new java.util.Date();
  public int timeInterval = 500; // milliseconds,

  public mySensorEventListener(Context context) {
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
  }

  public void start() {
    int delayRate = SensorManager.SENSOR_DELAY_GAME; // == 20,000 microseconds
    delayRate = timeInterval * 1000;
    sensorManager.registerListener(this, gsensor, delayRate);
    sensorManager.registerListener(this, msensor, delayRate);
  }

  public void stop() {
    sensorManager.unregisterListener(this);
  }

  public void setAzimuthFix(float fix) {
    azimuthFix = fix;
  }

  public void resetAzimuthFix() {
    setAzimuthFix(0);
  }

  public void setListenerAction(listenerAction action) {
    gAction = action;
  }

  @Override public void onSensorChanged(SensorEvent event) {
    final float alpha = 0.97f;
    java.util.Date thisDataTimeStamp = new java.util.Date();
    if ( thisDataTimeStamp.getTime() - lastDataTimeStamp.getTime() < timeInterval ) {
      // ignore/skip this to reduce the update frequency
      return;
    }

    synchronized (this) {
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        mGravity[0] = alpha*mGravity[0] + (1-alpha)*event.values[0];
        mGravity[1] = alpha*mGravity[1] + (1-alpha)*event.values[1];
        mGravity[2] = alpha*mGravity[2] + (1-alpha)*event.values[2];
        // mGravity = event.values;
        // Log.e(TAG, Float.toString(mGravity[0]));
      }

      if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        // mGeomagnetic = event.values;
        mGeomagnetic[0] = alpha*mGeomagnetic[0] + (1-alpha)*event.values[0];
        mGeomagnetic[1] = alpha*mGeomagnetic[1] + (1-alpha)*event.values[1];
        mGeomagnetic[2] = alpha*mGeomagnetic[2] + (1-alpha)*event.values[2];
        // Log.e(TAG, Float.toString(event.values[0]));
      }

      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
      if (success) {
        float orientation[] = new float[3];
        SensorManager.getOrientation(R, orientation);
        // Log.d(TAG, "azimuth (rad): " + azimuth);
        azimuth = (float) Math.toDegrees(orientation[0]); // orientation
        azimuth = (azimuth + azimuthFix + 360) % 360;
        // Log.d(TAG, "azimuth (deg): " + azimuth);

        lastDataTimeStamp = thisDataTimeStamp;
        if (gAction != null) gAction.OnAzimuthDataLoaded(azimuth);
      }
    }
  }

  @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }
}
