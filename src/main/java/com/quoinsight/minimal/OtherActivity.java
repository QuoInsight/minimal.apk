package com.quoinsight.minimal;
/*
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/OtherActivity.java
*/

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;  

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import android.widget.EditText;

import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;

public class OtherActivity extends android.app.Activity {

  private Compass compass; // Compass.java

  public float[] getOrientation() {
    // https://stackoverflow.com/questions/7046608/getrotationmatrix-and-getorientation-tutorial
    // https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/Compass.java

    /*
    android.hardware.SensorManager sensorManager
      = (android.hardware.SensorManager) this.getSystemService(android.content.Context.SENSOR_SERVICE);
      android.hardware.Sensor gsensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
      android.hardware.Sensor msensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
      sensorManager.registerListener(this, gsensor, android.hardware.SensorManager.SENSOR_DELAY_GAME);
      sensorManager.registerListener(this, msensor, android.hardware.SensorManager.SENSOR_DELAY_GAME);
    */

    float[] rotationMatrix = new float[16], 
      accelVals = new float[3], magVals = new float[3],
        baseOrientation = new float[4];
    baseOrientation[0] = (float) 0;
    if (android.hardware.SensorManager.getRotationMatrix(rotationMatrix, null, accelVals, magVals)) {
      android.hardware.SensorManager.getOrientation(rotationMatrix, baseOrientation);
    }
    return baseOrientation;
  }

  //////////////////////////////////////////////////////////////////////

  @Override public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      setContentView(R.layout.otheractivity);  // --> .\src\main\res\layout\otheractivity.xml
    } catch(Exception e) {
      Toast.makeText(OtherActivity.this, 
        "OtherActivity.setContentView: " +  e.getMessage(),
      Toast.LENGTH_LONG).show();  // .setDuration(int duration)
      //android.util.Log.e("OtherActivity.setContentView", e.getMessage());
      return;
    }

    try {

      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
      );
      TextView txt1 = (TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\otheractivity.xml
        txt1.setText("Hello from OtherActivity!\n[" + sdf.format(new java.util.Date()) + "]");

      TextView txt2 = (TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\otheractivity.xml
        txt2.setClickable(true);
        txt2.setOnClickListener(
          new View.OnClickListener() {
            public void onClick(View v) {
              java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "ss", java.util.Locale.getDefault()
              );          
              ((TextView) v).setText(android.text.Html.fromHtml(
                "Orientation# " + sdf.format(new java.util.Date()) + " :"
                   + "<h2>" + String.valueOf((float)Math.toDegrees(getOrientation()[0])) + "°</h2>"
              ));
            }
          }
        );

      Button button2 = (Button) findViewById(R.id.button2);  // --> .\src\main\res\layout\otheractivity.xml
        button2.setOnClickListener(
          new View.OnClickListener() {
            public void onClick(View v) {
              startActivity(new android.content.Intent(v.getContext(), MainActivity.class));
            }
          }
        );

      Button button9 = (Button) findViewById(R.id.button9);  // --> .\src\main\res\layout\otheractivity.xml
        button9.setOnClickListener(
          new View.OnClickListener() {
            public void onClick(View v) {
              //this.finishAffinity();
              finishAndRemoveTask();
            }
          }
        );

      TextView txt9 = (TextView) findViewById(R.id.txt9);  // --> .\src\main\res\layout\otheractivity.xml
        txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt9.setText(android.text.Html.fromHtml(
          " [ <A href='https://github.com/QuoInsight/minimal.apk'>src</A> ]"
        ));

    } catch(Exception e) {

      Toast.makeText(OtherActivity.this, 
        "OtherActivity.findViewById: " +  e.getMessage(),
      Toast.LENGTH_LONG).show();  // .setDuration(int duration)
      //android.util.Log.e("OtherActivity.findViewById", e.getMessage());
      return;

    }

    try {
      compass = new Compass(this);; // Compass.java

      compass.setListener(new Compass.CompassListener() {
        @Override public void onNewAzimuth(final float azimuth) {
          // UI updates only in UI thread
          // https://stackoverflow.com/q/11140285/444966
          runOnUiThread(new Runnable() {
            @Override public void run() {
              try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                  "ss", java.util.Locale.getDefault()
                );
                TextView txt2 = (TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\otheractivity.xml
                txt2.setText(android.text.Html.fromHtml(
                  "azimuth# " + sdf.format(new java.util.Date()) + " :"
                     + "<h2>" + String.valueOf(Math.round(azimuth)) + "°</h2>"
                ));
              } catch(Exception e) {
                Toast.makeText(OtherActivity.this, 
                  "OtherActivity.compass.run: " +  e.getMessage(),
                Toast.LENGTH_LONG).show();  // .setDuration(int duration)
                //android.util.Log.e("OtherActivity.compass.run", e.getMessage());
                return;
              }
            }
          });
        }
      });

      compass.start();

    } catch(Exception e) {

      Toast.makeText(OtherActivity.this, 
        "OtherActivity.compass: " +  e.getMessage(),
      Toast.LENGTH_LONG).show();  // .setDuration(int duration)
      android.util.Log.e("OtherActivity.compass", e.getMessage());

    }

  }

}
