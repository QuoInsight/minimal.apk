package com.quoinsight.minimal;

import android.app.Activity;
import android.content.Intent;

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;  

import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;

import android.os.BatteryManager;

public class MainActivity extends Activity {
  @Override public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
    );

    TextView txt1 = new TextView(this);
      txt1.setGravity(Gravity.CENTER_HORIZONTAL);
      txt1.setText("Hello world!\n[" + sdf.format(new java.util.Date()) + "]");

    BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
    int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

    TextView txt2 = new TextView(this);
      txt2.setGravity(Gravity.CENTER_HORIZONTAL);
      txt2.setText("Battery Level:\n" + Integer.toString(batLevel) + "%");
      txt2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 52);

    Button button1 = new Button(this);
      button1.setText("澳門電台");
      button1.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(android.net.Uri.parse("http://live4.tdm.com.mo:1935/live/_definst_/rch2.live/playlist.m3u8"));
            startActivity(intent);
          }
        }
      );

    Button button9 = new Button(this);
      button9.setText("Quit");
      button9.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            Toast.makeText(
              MainActivity.this, "Hellow World!", Toast.LENGTH_LONG
            ).show();  // .setDuration(int duration)

            try { Thread.sleep(3000); } catch(InterruptedException e) {}
            //this.finishAffinity();
            finishAndRemoveTask();
            // System.exit(0);
          }
        }
      );

    LinearLayout layout = new LinearLayout(this);
      layout.setGravity(Gravity.CENTER);  // Gravity.CENTER
      layout.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
      );
      layout.addView(txt1, params);
      layout.addView(button1, params);
      layout.addView(txt2, params);
      layout.addView(button9, params);

    setContentView(layout);
  }
}
