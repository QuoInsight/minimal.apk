package com.quoinsight.minimal;
/*
  # inspired by https://czak.pl/2016/01/13/minimal-android-project.html
*/

import android.app.Activity;
import android.content.Intent;

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

public class MainActivity extends Activity {
  @Override public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
    );

    TextView txt1 = new TextView(this);
      txt1.setGravity(Gravity.CENTER_HORIZONTAL);
      txt1.setText("Hello world!\n[" + sdf.format(new java.util.Date()) + "]");

    Spinner spinner1 = new Spinner(this);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        this, android.R.layout.simple_spinner_item,
        new String[] { "澳門電台", "AiFM", "港台" }
      );
      spinner1.setAdapter(adapter);
      spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(
          AdapterView<?> parent, View view, int position, long id
        ) {
            // Log.v("item", (String) parent.getItemAtPosition(position));
            Toast.makeText(MainActivity.this, 
              "selected item: " + (String)parent.getItemAtPosition(position),
            Toast.LENGTH_LONG).show();  // .setDuration(int duration)
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
      });

    Button button1 = new Button(this);
      button1.setText("OpenUrl");
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

    android.os.BatteryManager bm = (android.os.BatteryManager)getSystemService(BATTERY_SERVICE);
    int batLevel = bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY);

    TextView txt2 = new TextView(this);
      txt2.setGravity(Gravity.CENTER_HORIZONTAL);
      txt2.setText("Battery Level:\n" + Integer.toString(batLevel) + "%");
      txt2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 60);

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

    EditText edit1 = new EditText(this);
      edit1.setGravity(Gravity.CENTER_HORIZONTAL);
      edit1.setText("testing ...");
      edit1.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, android.view.MotionEvent motionEvent) {
          view.getParent().requestDisallowInterceptTouchEvent(true);
          switch (motionEvent.getAction() & android.view.MotionEvent.ACTION_MASK){
            case android.view.MotionEvent.ACTION_UP:
              view.getParent().requestDisallowInterceptTouchEvent(false);
              break;
          }
          return false;
        }
      });

    TextView txt9 = new TextView(this);
      txt9.setGravity(Gravity.CENTER_HORIZONTAL);
      txt9.setAutoLinkMask(android.text.util.Linkify.ALL);  txt9.setLinksClickable(true);
      txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
      txt9.setText(android.text.Html.fromHtml(
        "[ <A href=\"https://github.com/QuoInsight/minimal.apk\">src</A> ]"
      ));

    LinearLayout layout = new LinearLayout(this);
      layout.setGravity(Gravity.CENTER);  // Gravity.CENTER
      layout.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
      );
      layout.addView(txt1, params);
      layout.addView(spinner1, params);
      layout.addView(button1, params);
      layout.addView(txt2, params);
      layout.addView(button9, params);
      layout.addView(edit1, params);
      layout.addView(txt9, params);

    setContentView(layout);
  }
}
