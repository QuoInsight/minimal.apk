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

  public String getDeviceID() {
    return android.provider.Settings.Secure.getString(
      getContentResolver(), android.provider.Settings.Secure.ANDROID_ID
    );
  }

  public String joinStringList(String sep, java.util.List<String> lst) {
    String str = "";
    if (lst.size() > 0) {
      for (String s : lst) str = str + s + sep;
      str = str.substring(0, str.length()-sep.length()); // remove the last separator
    }
    return str;
  }

  public java.util.List<String> getPackageList() {
    // https://devofandroid.blogspot.com/2018/02/get-list-of-user-installed-apps-with.html
    android.content.pm.PackageManager pkgMgr = this.getPackageManager();
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

  public java.util.List<String> getSensorList() {
    // http://pages.iu.edu/~rwisman/c490/html/android-sensors.htm
    android.hardware.SensorManager sensorMgr = (android.hardware.SensorManager) this.getSystemService(android.content.Context.SENSOR_SERVICE);
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

  public void launchAppMgr(android.app.Activity parentActivity) {
    try {
      parentActivity.startActivityForResult(new android.content.Intent(
        android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
      ), 0);
    } catch(Exception e) {
      MainActivity.writeMessage((android.content.Context)parentActivity, "OtherActivity.launchAppMgr", e.getMessage());
      return;
    }
  }

  public void launchAppInfo(android.content.Context parentContext, String pkgName) {
    try {
      android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(android.net.Uri.parse("package:" + pkgName));
      parentContext.startActivity(intent);
    } catch(Exception e) {
      MainActivity.writeMessage(parentContext, "OtherActivity.launchAppInfo", e.getMessage());
      return;
    }
  }

  public void launchApp(android.content.Context parentContext, String pkgName) {
    try {
      android.content.Intent intent = parentContext.getPackageManager().getLaunchIntentForPackage(pkgName);
      parentContext.startActivity(intent);
    } catch(Exception e) {
      MainActivity.writeMessage(parentContext, "OtherActivity.launchApp", "[" + pkgName + "] " + e.getMessage());
      return;
    }
  }

  //////////////////////////////////////////////////////////////////////

  public android.widget.EditText makeEditTextSelectableReadOnly(android.widget.EditText edtxt) {
    // https://medium.com/@anna.domashych/selectable-read-only-multiline-text-field-on-android-169c27c55408
    edtxt.setShowSoftInputOnFocus(false);  edtxt.setPadding(10,10,10,10);  edtxt.setBackgroundColor(android.graphics.Color.parseColor("#E8E8E8"));
    edtxt.setHorizontallyScrolling(true);  // android:scrollHorizontally="true" doesn't work

    edtxt.setCustomSelectionActionModeCallback(
      // remove all menu items except "Copy", "Select All", "Share" 
      new android.view.ActionMode.Callback() {
        @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
          try {
            android.view.MenuItem copyText = menu.findItem(android.R.id.copy);
            android.view.MenuItem selectAll = menu.findItem(android.R.id.selectAll);
            android.view.MenuItem shareText = menu.findItem(android.R.id.shareText);
            menu.clear();  menu.add(0, android.R.id.copy, 0, copyText.getTitle());
            menu.add(0, android.R.id.selectAll, 0, selectAll.getTitle());
            menu.add(0, android.R.id.shareText, 0, shareText.getTitle());
          } catch (Exception e) {}
          return true;
        }
        @Override public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {return true; }
        @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) { return false; }
        @Override public void onDestroyActionMode(android.view.ActionMode mode) {}
      }
    );

    try {
      edtxt.setCustomInsertionActionModeCallback(
        // completely block a menu which appears when a user taps on cursor
        new android.view.ActionMode.Callback() {
          @Override public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
          @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
          @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) { return false; }
          @Override public void onDestroyActionMode(android.view.ActionMode mode) { }
        }
      );
    } catch (Exception e) {}

    return edtxt;
  }

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
        launchAppInfo(this, getApplicationContext().getPackageName()); // "com.quoinsight.minimal"
        return true;
      case R.id.main_menu_about:
        MainActivity.launchUrl(this, "https://sites.google.com/site/quoinsight/home/minimal-apk");
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
      MainActivity.writeMessage(this, "OtherActivity.setContentView", e.getMessage());
      return;
    }

    try {

      TextView txt1 = (TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\otheractivity.xml
        txt1.setText("Hello " + getDeviceID() +  " from OtherActivity!\n[" + MainActivity.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        // avoid EditText from gaining focus at Activity startup 
        txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();

      // ((EditText)findViewById(R.id.edit1)).setText( String.join("\n", getPackageList()) );
      // String.join is not support by some versions --> use our local function joinStringList() instead
      java.util.List<String> pkgLst = getPackageList();
      makeEditTextSelectableReadOnly((EditText)findViewById(R.id.edit1)).setText(joinStringList("\n", pkgLst));

      makeEditTextSelectableReadOnly((EditText)findViewById(R.id.edit2)).setText(joinStringList("\n", getSensorList()));

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
              MainActivity.writeMessage(this, "OtherActivity.onItemSelected", e.getMessage());
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
              launchApp(OtherActivity.this, appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              MainActivity.writeMessage(OtherActivity.this, "OtherActivity.launchApp", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appInfo).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              launchAppInfo(OtherActivity.this, appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              MainActivity.writeMessage(OtherActivity.this, "OtherActivity.appInfo", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appUrl).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              MainActivity.launchUrl(v.getContext(), "https://play.google.com/store/apps/details?id=" + appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              MainActivity.writeMessage(OtherActivity.this, "OtherActivity.appUrl", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appMgr).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              launchAppMgr(OtherActivity.this);
            } catch(Exception e) {
              MainActivity.writeMessage(OtherActivity.this, "OtherActivity.launchAppMgr", e.getMessage());
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

      MainActivity.writeMessage(this, "OtherActivity.findViewById", e.getMessage());
      return;

    }

  }

}
