package com.quoinsight.minimal;
/*
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/OtherActivity.java
*/

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
        sysUtil.launchAppInfo(this, getApplicationContext().getPackageName()); // "com.quoinsight.minimal"
        return true;
      case R.id.main_menu_about:
        sysUtil.launchUrl(this, "https://sites.google.com/site/quoinsight/home/minimal-apk");
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

      android.widget.TextView txt1 = (android.widget.TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\otheractivity.xml
        txt1.setText("Hello " + sysUtil.getDeviceID(this) +  " from OtherActivity!\n[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        // avoid EditText from gaining focus at Activity startup 
        txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();

      // ((android.widget.EditText)findViewById(R.id.edit1)).setText( String.join("\n", sysUtil.getPackageList(this)) );
      // String.join() is not support by some versions --> use our local function joinStringList() instead
      java.util.List<String> pkgLst = sysUtil.getPackageList(this);
      commonGui.makeEditTextSelectableReadOnly(
        (android.widget.EditText)findViewById(R.id.edit1)
      ).setText(commonUtil.joinStringList("\n", pkgLst));

      commonGui.makeEditTextSelectableReadOnly(
        (android.widget.EditText)findViewById(R.id.edit2)
      ).setText(commonUtil.joinStringList("\n", sysUtil.getSensorList(this)));

      final android.widget.Spinner spinner1 = (android.widget.Spinner) findViewById(R.id.spinner1);
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
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
          this, android.R.layout.simple_spinner_item, selectList
          //new String[] { "SleepRadio", "澳門電台", "AiFM", "港台" }
        );
        spinner1.setAdapter(adapter);
        //adapter.setDropDownViewResource(R.layout.xxx); // [spinner_textview_align]
       /*
        spinner1.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
          @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
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
          @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
       */

      findViewById(R.id.launchApp).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            try {
              sysUtil.launchApp(OtherActivity.this, appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.launchApp", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appInfo).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            try {
              sysUtil.launchAppInfo(OtherActivity.this, appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.appInfo", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appUrl).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            try {
              sysUtil.launchUrl(v.getContext(), "https://play.google.com/store/apps/details?id=" + appPackageNames.get(spinner1.getSelectedItem().toString()));
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.appUrl", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appMgr).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            try {
              sysUtil.launchAppMgr(OtherActivity.this);
            } catch(Exception e) {
              commonGui.writeMessage(OtherActivity.this, "OtherActivity.launchAppMgr", e.getMessage());
              return;
            }
          }
        }
      );

      android.widget.TextView txt2 = (android.widget.TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\otheractivity.xml
        String otherInfo = "<br>Build.VERSION.SDK_INT: " + String.valueOf(android.os.Build.VERSION.SDK_INT);

        android.util.DisplayMetrics displayMetrics = commonGui.getDefaultDisplayMetrics(this);
        otherInfo += "<br>defaultDispMetrics: " + String.valueOf(displayMetrics.widthPixels) + "x" + String.valueOf(displayMetrics.heightPixels);

        displayMetrics = commonGui.getResourceDisplayMetrics( this ); // ((android.view.View)txt1).getContext()
        otherInfo += "<br>resourceDispMetrics: " + String.valueOf(displayMetrics.widthPixels) + "x" + String.valueOf(displayMetrics.heightPixels)
          + "<br>1dp=" + String.valueOf(commonGui.dp2px(displayMetrics, 1)) + "px<br>";

        txt2.setText( android.text.Html.fromHtml(otherInfo) ); // CSS is not supported!
        txt2.setClickable(true);  txt2.setOnClickListener(
          new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
              android.widget.TextView txt = (android.widget.TextView) v;

              float[] widthHeight = commonGui.getVwSzDimensionPx(v);
              String otherInfo = "<br>getVwSzDimensionPx(v): " + String.valueOf(widthHeight[0]) + "x" + String.valueOf(widthHeight[1]);
              otherInfo += "<br>v.getMeasuredSize: " + String.valueOf(v.getMeasuredWidth()) + "x" + String.valueOf(v.getMeasuredHeight());

              txt.setText( android.text.Html.fromHtml(otherInfo) ); // CSS is not supported!
            }
          }
        );


      findViewById(R.id.btnPrev).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            startActivity(new android.content.Intent(v.getContext(), MainActivity.class));
          }
        }
      );

      findViewById(R.id.button9).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            //this.finishAffinity();
            finishAndRemoveTask();
          }
        }
      );

      android.widget.TextView txt9 = (android.widget.TextView) findViewById(R.id.txt9);  // --> .\src\main\res\layout\otheractivity.xml
        txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt9.setText(android.text.Html.fromHtml(txt9.getText().toString()));

    } catch(Exception e) {

      commonGui.writeMessage(this, "OtherActivity.findViewById", e.getMessage());
      return;

    }

  }

}
