package com.quoinsight.minimal;
/*
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/OtherActivity.java
*/

import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import android.view.View;

public class OtherActivity extends android.app.Activity
  // implements android.hardware.SensorEventListener // !! need this for it work correctly with SensorEvent !!
{

  //////////////////////////////////////////////////////////////////////

  @Override public boolean onCreateOptionsMenu(android.view.Menu menu) {
    super.onCreateOptionsMenu(menu); // ⋮OptionsMenu vs. ≡NavigationDrawer
    android.view.MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);  // [.\src\main\res\menu\menu.xml]
    return true;
  }
  @Override public boolean onOptionsItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case R.id.main_menu_settings:
        return true;
      case R.id.main_menu_appInfo:
        sysUtils.launchAppInfo(this, getApplicationContext().getPackageName()); // "com.quoinsight.minimal"
        return true;
      case R.id.main_menu_about:
        sysUtils.launchUrl(this, "https://sites.google.com/site/quoinsight/home/minimal-apk");
        return true;
      case R.id.main_menu_quit:
        //this.finishAffinity();
        finishAndRemoveTask();
        return true;
      default:
        break;
    }
    return false;
  }

  //////////////////////////////////////////////////////////////////////

  @Override public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      setContentView(R.layout.otheractivity);  // --> .\src\main\res\layout\otheractivity.xml
    } catch(Exception e) {
      commonGui.writeMessage(this, "OtherActivity.setContentView", e.getMessage());
      return;
    }

    try {

      TextView txt1 = (TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\otheractivity.xml
        txt1.setText("Hello " + sysUtils.getDeviceID(this) +  " from OtherActivity!\n[" + commonUtils.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        // avoid EditText from gaining focus at Activity startup 
        txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();

      // ((EditText)findViewById(R.id.edit1)).setText( String.join("\n", sysUtils.getPackageList(this)) );
      // String.join() is not support by some versions --> use our local function joinStringList() instead
      java.util.List<String> pkgLst = sysUtils.getPackageList(this);
      commonGui.makeEditTextSelectableReadOnly((EditText)findViewById(R.id.edit1)).setText(commonUtils.joinStringList("\n", pkgLst));

      commonGui.makeEditTextSelectableReadOnly((EditText)findViewById(R.id.edit2)).setText(commonUtils.joinStringList("\n", sysUtils.getSensorList(this)));

      final Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
        final java.util.Hashtable<String, String> appPackageNames = new java.util.Hashtable<String, String>();
         /*
          // https://stackoverflow.com/questions/9371942/dictionary-in-android-resources
          String[] strPackageNames = getResources().getStringArray(R.array.otherpkgnames);  // --> .\src\main\res\values\strings.xml
          for(String s : strPackageNames) {
            String[] a = s.split(":");
            appPackageNames.put(a[0], a[1]);
          }
         */
          for(String s : pkgLst) {
            String[] a = s.split(" \\[");
            appPackageNames.put(a[0], a[1].substring(0, a[1].length()-1));
          }
        java.util.ArrayList<String> selectList = new java.util.ArrayList<String>(appPackageNames.keySet());
          java.util.Collections.sort(selectList, String.CASE_INSENSITIVE_ORDER);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
          this, android.R.layout.simple_spinner_item, selectList
          //new String[] { "SleepRadio", "澳門電台", "AiFM", "港台" }
        );
        spinner1.setAdapter(adapter);
        //adapter.setDropDownViewResource(R.layout.xxx); // [spinner_textview_align]
       /*
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            try {
              // !! below is no good, use android:theme="@style/spinner1" in XML instead !!
              float factor = view.getContext().getResources().getDisplayMetrics().density; // ==2.0 
              //int w = ??.getPaint().measureText((String)parent.getItemAtPosition(position)) + 20;
              int w = (int)(factor *(20 + ((String)parent.getItemAtPosition(position)).length()*12));
              android.view.ViewGroup.LayoutParams p = view.getLayoutParams();
              p.width = w;  view.setLayoutParams(p);
            } catch(Exception e) {
              commonGui.writeMessage(this, "OtherActivity.onItemSelected", e.getMessage());
              return;
            }
          }
          @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
       */

      findViewById(R.id.launchApp).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              sysUtils.launchApp(OtherActivity.this, appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.launchApp", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appInfo).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              sysUtils.launchAppInfo(OtherActivity.this, appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.appInfo", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appUrl).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              sysUtils.launchUrl(v.getContext(), "https://play.google.com/store/apps/details?id=" + appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.appUrl", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appMgr).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              sysUtils.launchAppMgr(OtherActivity.this);
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.launchAppMgr", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.btnPrev).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            startActivity(new android.content.Intent(v.getContext(), MainActivity.class));
          }
        }
      );

      findViewById(R.id.button9).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
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
        txt9.setText(android.text.Html.fromHtml(txt9.getText().toString()));

    } catch(Exception e) {

      commonGui.writeMessage(this, "OtherActivity.findViewById", e.getMessage());
      return;

    }

  }

}
