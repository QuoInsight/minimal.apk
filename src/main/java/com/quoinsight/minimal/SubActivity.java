package com.quoinsight.minimal;
/*
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/SubActivity.java
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

public class SubActivity extends Activity {
  @Override public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
    );

    TextView txt1 = new TextView(this);
      txt1.setGravity(Gravity.CENTER_HORIZONTAL);
      txt1.setText("Hello from SubActivity!\n[" + sdf.format(new java.util.Date()) + "]");

    Button button2 = new Button(this);
      button2.setText("Previous");
      button2.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            startActivity(new Intent(v.getContext(), MainActivity.class));
          }
        }
      );

    Button button9 = new Button(this);
      button9.setText("Quit");
      button9.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            Toast.makeText(
              SubActivity.this, "Hellow World!", Toast.LENGTH_LONG
            ).show();  // .setDuration(int duration)

            try { Thread.sleep(3000); } catch(InterruptedException e) {}
            //this.finishAffinity();
            finishAndRemoveTask();
            // System.exit(0);
          }
        }
      );

    TextView txt9 = new TextView(this);
      txt9.setGravity(Gravity.CENTER_HORIZONTAL);
      txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
      txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
      txt9.setText(android.text.Html.fromHtml(
        " [ <A href='https://github.com/QuoInsight/minimal.apk'>src</A> ]"
      ));

    LinearLayout layout = new LinearLayout(this);
      layout.setGravity(Gravity.CENTER);  // Gravity.CENTER
      layout.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
      );
      layout.addView(txt1, params);
      layout.addView(button2, params);
      layout.addView(button9, params);
      layout.addView(txt9, params);

    setContentView(layout);
  }
}
