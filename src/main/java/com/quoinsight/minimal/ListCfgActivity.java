package com.quoinsight.minimal;

public class ListCfgActivity extends android.app.Activity
{

  //////////////////////////////////////////////////////////////////////

  public android.content.SharedPreferences gSharedPref;

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
      setContentView(R.layout.listcfgactivity);  // --> .\src\main\res\layout\listcfgactivity.xml
    } catch(Exception e) {
      commonGui.writeMessage(this, "ListCfgActivity.setContentView", e.getMessage());
      return;
    }

    try {

      android.widget.TextView txt1 = (android.widget.TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\ListCfgActivity.xml
        txt1.setText("Hello " + sysUtil.getDeviceID(this) +  " from ListCfgActivity!\n[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        // avoid EditText from gaining focus at Activity startup 
        txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();

      // ((android.widget.EditText)findViewById(R.id.edit1)).setText( String.join("\n", sysUtil.getPackageList(this)) );
      // String.join() is not support by some versions --> use our local function joinStringList() instead
      //java.util.List<String> pkgLst = sysUtil.getPackageList(this);
      //String strList = commonUtil.joinStringList("\n", pkgLst);

      gSharedPref = ((android.app.Activity)this).getSharedPreferences(getApplicationContext().getPackageName(), android.content.Context.MODE_PRIVATE);
      String stationList = gSharedPref.getString("stationList",null);
      if (stationList==null || stationList.length()==0) {
        stationList = MainActivity.getInstance().getStreamUrls();
      }
      ((android.widget.EditText)findViewById(R.id.edit1)).setText( stationList );

      // "💾Save 🔄Update ⛝Close"

      findViewById(R.id.btnUpdate).setOnClickListener( // --> .\src\main\res\layout\ListCfgActivity.xml
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            try {
              String stationList = ((android.widget.EditText)findViewById(R.id.edit1)).getText().toString();
              android.content.SharedPreferences.Editor prefEditor = gSharedPref.edit();
                prefEditor.putString("stationList", stationList);
                prefEditor.apply(); // commit();
              MainActivity.getInstance().reloadSpinner1Adapter();
              //this.finishAffinity();
              finishAndRemoveTask();
            } catch(Exception e) { }
          }
        }
      );

      android.widget.TextView txt9 = (android.widget.TextView) findViewById(R.id.txt9);  // --> .\src\main\res\layout\ListCfgActivity.xml
        txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt9.setText(android.text.Html.fromHtml(txt9.getText().toString()));

    } catch(Exception e) {

      commonGui.writeMessage(this, "ListCfgActivity.findViewById", e.getMessage());
      return;

    }

  }

}
