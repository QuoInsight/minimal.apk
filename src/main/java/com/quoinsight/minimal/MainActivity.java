package com.quoinsight.minimal;
/*
  # inspired by https://czak.pl/2016/01/13/minimal-android-project.html
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/MainActivity.java
*/

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;  

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import android.widget.EditText;

import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.view.View;

public class MainActivity extends android.app.Activity {

  public boolean toggleFlashLight = true;

  static final public void quit(final android.app.Activity parentActivity) {
    android.app.AlertDialog.Builder alrt = new android.app.AlertDialog.Builder((android.content.Context)parentActivity);
    alrt.setMessage("Are you sure?").setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
      @Override public void onClick(android.content.DialogInterface dialog, int which) {
        /*
        switch (which) {
          case android.content.DialogInterface.BUTTON_POSITIVE:
            //Yes button clicked
            break;
        }
        */

        //parentActivity.finishAffinity();
        if (android.os.Build.VERSION.SDK_INT >= 21)
          parentActivity.finishAndRemoveTask(); else parentActivity.finish();
        /*
          https://stackoverflow.com/questions/22166282/close-application-and-remove-from-recent-apps
          Note: this won't address the availability of "force stop" in the application info.
            Android allows you to force stop an application even if it does not have any processes running.
            Force stop puts the package into a specific stopped state, where it can't receive broadcast events.
        */

        // System.exit(0);
      }
    }).setNegativeButton("No", null).show();
  }

  //////////////////////////////////////////////////////////////////////

  // ⋮OptionsMenu vs. ≡NavigationDrawer
  private static final int NEW_MENU_ID=android.view.Menu.FIRST+1;
  @Override public boolean onCreateOptionsMenu(android.view.Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, 99, 0, "⏏ Quit"); 
    return true;
  }
  @Override public boolean onOptionsItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case 99:
        quit(this);
        return true;
      default:
        break;
    }
    return false;
  }

  //////////////////////////////////////////////////////////////////////

  @Override public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    TextView txt1 = new TextView(this);
      txt1.setGravity(Gravity.CENTER_HORIZONTAL);  // txt1.setText("Hello world!\n[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
      txt1.setText(android.text.Html.fromHtml(
        "Hello world!<br><small><small>[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]</small></small>"
          + "<br><font size='1.75em'>" + commonUtil.getChineseDateStr() + "</font>"
      )); // Hello world\n[2020-01-09 十二月十五⁄30巳时 09:06:21] --> Hello world\n[2020-01-09 09:06:21]\n十二月十五⁄30巳时

    final Spinner spinner1 = new Spinner(this);
      final java.util.Hashtable<String, String> radioStations = new java.util.Hashtable<String, String>();
        // https://docs.google.com/spreadsheets/d/1cj66AnWNgJ3GqDTIQBWeUEsapjp_Zk37v11iwoa8xzM/edit#gid=0
        radioStations.put("SleepRadio", "http://149.56.234.138:8169/;");
        radioStations.put("澳門電台", "http://live4.tdm.com.mo:1935/live/_definst_/rch2.live/playlist.m3u8");
        radioStations.put("AiFM", "https://aifmmobile.secureswiftcontent.com/memorystreams/HLS/rtm-ch020/rtm-ch020.m3u8");
        radioStations.put("香港電台第一台", "http://stm.rthk.hk:80/radio1");
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        this, android.R.layout.simple_spinner_item,
        //new String[] { "SleepRadio", "澳門電台", "AiFM", "港台" }
        new java.util.ArrayList<String>(radioStations.keySet())
      );
      spinner1.setAdapter(adapter);
      spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(
          AdapterView<?> parent, View view, int position, long id
        ) {
            // android.util.Log.v("item", (String) parent.getItemAtPosition(position));
            Toast.makeText(MainActivity.this, 
              "selected item: " + (String)parent.getItemAtPosition(position),
            Toast.LENGTH_LONG).show();  // .setDuration(int duration)
        }

        @Override public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub
        }
      });

    Button button1 = new Button(this);
      button1.setText("OpenUrl");
      button1.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            sysUtil.launchUrl(v.getContext(), radioStations.get(spinner1.getSelectedItem().toString()));
          }
        }
      );

    TextView txt2 = new TextView(this);
      txt2.setGravity(Gravity.CENTER_HORIZONTAL);
      txt2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 72);
      txt2.setText("🔋" + Integer.toString(sysUtil.getBatteryLevel(this)) + "%");
      txt2.setClickable(true);
      txt2.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            // TextView txt2 = (TextView) v; // findViewById(v.getId());
            ((TextView) v).setText(android.text.Html.fromHtml(
              "<small><small><small><small># " + commonUtil.getDateStr("ss") + " :</small></small></small></small>"
                 + "<br>🔋" + Integer.toString(sysUtil.getBatteryLevel(v.getContext())) + "%"
            ));
          }
        }
      );

    Button btnNext = new Button(this);
      btnNext.setText("⎘ Next");
      btnNext.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            startActivity(new android.content.Intent(v.getContext(), OtherActivity.class));
          }
        }
      );

    Button btnCompass = new Button(this);
      btnCompass.setAllCaps(false);
      btnCompass.setText("✳ Start Compass 🧭");
      btnCompass.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            startActivity(new android.content.Intent(v.getContext(), CompassActivity.class));
          }
        }
      );

    Button btnTorch = new Button(this);
      btnTorch.setAllCaps(false);
      btnTorch.setText("🔦 FlashLight");
      btnTorch.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              sysUtil.enableTorchLigth(v.getContext(), toggleFlashLight);
              toggleFlashLight = ! toggleFlashLight;
            } catch(Exception e) {
              commonGui.writeMessage(MainActivity.this, "MainActivity.flashlight", e.getMessage());
              return;
            }
          }
        }
      );

    Button button9 = new Button(this);
      button9.setText("⎊ Quit"); // ⏻ ≡  [🚪←🚶] 𓁆 𝍇去 𝌶逃
      button9.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            Toast.makeText(
              MainActivity.this, "closing...", Toast.LENGTH_LONG
            ).show();  // .setDuration(int duration)

            //try { Thread.sleep(3000); } catch(InterruptedException e) {}

            quit(MainActivity.this);
          }
        }
      );

    EditText edit1 = new EditText(this);
      edit1.setGravity(Gravity.CENTER_HORIZONTAL);
      edit1.setText("testing ...");
      edit1.setOnTouchListener(new View.OnTouchListener(){
        @Override public boolean onTouch(View view, android.view.MotionEvent motionEvent) {
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
      txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
      txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
      txt9.setText(android.text.Html.fromHtml(
        " [ <A href='https://github.com/QuoInsight/minimal.apk'>src</A> ] "
         + " [ <A href='https://play.google.com/store/apps/details?id=com.quoinsight.minimal'>install</A> ] "
          + " [ <A href='https://sites.google.com/site/quoinsight/home/minimal-apk'>about</A> ] "
      ));

    // startActivityForResult(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    // launchApp("com.otherapp.package");

    ScrollView scrollable = new ScrollView(this);
      scrollable.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
      LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);  layout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.addView(txt1, params);
        LinearLayout layout2 = new LinearLayout(this);
          layout2.setOrientation(LinearLayout.HORIZONTAL);  layout2.setGravity(Gravity.CENTER);
          layout2.addView(spinner1, params);  layout2.addView(button1, params);
        layout.addView(layout2, params);
        layout.addView(txt2, params);
        layout.addView(btnCompass, params);
        layout.addView(btnTorch, params);
        layout.addView(btnNext, params);
        layout.addView(button9, params);
        layout.addView(edit1, params);
        layout.addView(txt9, params);
      scrollable.addView(layout);
      // avoid EditText from gaining focus at Activity startup 
      txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();
    setContentView(scrollable);
  }

}
