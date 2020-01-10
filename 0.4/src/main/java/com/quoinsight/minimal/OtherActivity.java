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

public class OtherActivity extends android.app.Activity
  // implements android.hardware.SensorEventListener // !! need this for it work correctly with SensorEvent !!
{
  public void writeMessage(String tag, String msg, String...args) {  // varargs
    Toast.makeText(OtherActivity.this, tag + ": " +  msg, Toast.LENGTH_LONG).show();  // .setDuration(int duration)
    //android.util.Log.e(tag, msg);
    return;
  }

  public void msgBox(String title, String msg) {
    android.app.AlertDialog.Builder alrt = new android.app.AlertDialog.Builder(this);
    alrt.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("OK", null).show();
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

  public void launchAppMgr() {
    try {
      startActivityForResult(new android.content.Intent(
        android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
      ), 0);
    } catch(Exception e) {
      writeMessage("OtherActivity.launchAppMgr", e.getMessage());
      return;
    }
  }

  public void launchAppInfo(String pkgName) {
    try {
      android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(android.net.Uri.parse("package:" + pkgName));
      startActivity(intent);
    } catch(Exception e) {
      writeMessage("OtherActivity.launchAppInfo", e.getMessage());
      return;
    }
  }

  public void launchApp(String pkgName) {
    try {
      android.content.Intent intent = this.getPackageManager().getLaunchIntentForPackage(pkgName);
      startActivity(intent);
    } catch(Exception e) {
      writeMessage("OtherActivity.launchApp", "[" + pkgName + "] " + e.getMessage());
      return;
    }
  }

  //////////////////////////////////////////////////////////////////////

  public boolean toggleFlashLight = true;

  //////////////////////////////////////////////////////////////////////

  // Important: To avoid the unnecessary usage of battery,
  // register the listener in the onResume method and de-register on the onPause method.

  @Override protected void onResume() {
    super.onResume();
    //gSensorListener.register(500, 2000);
    //if (gAzimuthTextView!=null) setTextOnSensorChanged(gAzimuthTextView, gSensorManager);
  }
 
  @Override protected void onPause() {
    if (gSensorListener!=null) gSensorListener.unregister();
    super.onPause();
  }

  //////////////////////////////////////////////////////////////////////

  private mySensorListener gSensorListener = null;

  //////////////////////////////////////////////////////////////////////

  public EditText makeEditTextSelectableReadOnly(EditText thisEditText) {
    // https://medium.com/@anna.domashych/selectable-read-only-multiline-text-field-on-android-169c27c55408
    thisEditText.setShowSoftInputOnFocus(false);  thisEditText.setPadding(10,10,10,10);  thisEditText.setBackgroundColor(android.graphics.Color.parseColor("#E8E8E8"));
    thisEditText.setHorizontallyScrolling(true);  // android:scrollHorizontally="true" doesn't work
    thisEditText.setCustomSelectionActionModeCallback(
      // keep "Copy" option only and remove all other menu items
      new android.view.ActionMode.Callback() {
        @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) {
          try {
            android.view.MenuItem copyTxt = menu.findItem(android.R.id.copy);
            android.view.MenuItem selectAll = menu.findItem(android.R.id.selectAll);
            menu.clear();  menu.add(0, android.R.id.copy, 0, copyTxt.getTitle());
            menu.add(0, android.R.id.selectAll, 0, selectAll.getTitle());
          } catch (Exception e) {}
          return true;
        }
        @Override public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) {return true; }
        @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) { return false; }
        @Override public void onDestroyActionMode(android.view.ActionMode mode) {}
      }
    );
    thisEditText.setCustomInsertionActionModeCallback(
      // completely block a menu which appears when a user taps on cursor
      new android.view.ActionMode.Callback() {
        @Override public boolean onCreateActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
        @Override public boolean onPrepareActionMode(android.view.ActionMode mode, android.view.Menu menu) { return false; }
        @Override public boolean onActionItemClicked(android.view.ActionMode mode, android.view.MenuItem item) { return false; }
        @Override public void onDestroyActionMode(android.view.ActionMode mode) { }
      }
    );
    return thisEditText;
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
      writeMessage("OtherActivity.setContentView", e.getMessage());
      return;
    }

    try {

      TextView txt1 = (TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\otheractivity.xml
        txt1.setText("Hello from OtherActivity!\n[" + MainActivity.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        // avoid EditText from gaining focus at Activity startup 
        txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();

      // ((EditText)findViewById(R.id.edit1)).setText( String.join("\n", getPackageList()) );
      java.util.List<String> pkgLst = getPackageList();
      makeEditTextSelectableReadOnly((EditText)findViewById(R.id.edit1)).setText(String.join("\n", pkgLst));

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
              writeMessage("OtherActivity.onItemSelected", e.getMessage());
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
              launchApp( appPackageNames.get(spinner1.getSelectedItem().toString()) );
            } catch(Exception e) {
              writeMessage("OtherActivity.launchApp", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appInfo).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              launchAppInfo( appPackageNames.get(spinner1.getSelectedItem().toString()) );
            } catch(Exception e) {
              writeMessage("OtherActivity.appInfo", e.getMessage());
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
              writeMessage("OtherActivity.appUrl", e.getMessage());
              return;
            }
          }
        }
      );

      findViewById(R.id.appMgr).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              launchAppMgr();
            } catch(Exception e) {
              writeMessage("OtherActivity.launchAppMgr", e.getMessage());
              return;
            }
          }
        }
      );

      try {
        gSensorListener = new mySensorListener(this); // will run into error if this is executed before onCreate() 
        gSensorListener.setHandlers(new mySensorListener.handlers() {
          @Override public void OnMessage(String tag, String msg, String...args) {
            writeMessage(tag, msg, args);
          }
          @Override public void OnAzimuthDataLoaded(final float azimuth) {
            runOnUiThread(new Runnable() { @Override public void run() {
              try {
                ((TextView)findViewById(R.id.txt2)).setText(android.text.Html.fromHtml( // CSS is not supported!
                  "#" + MainActivity.getDateStr("ss") + ":<br>"
                     + "<font size='2em'>✳" + String.valueOf(Math.round(azimuth)) + "°</font>"
                ));
                //gSensorListener.unregister();
              } catch(Exception e) {
                writeMessage("OtherActivity.OnAzimuthDataLoaded", e.getMessage());
                return;
              }
            }});
        }});
      } catch(Exception e) {
        writeMessage("OtherActivity.mySensorListener", e.getMessage());
      }

      TextView txt2 = (TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\otheractivity.xml
        txt2.setClickable(true);
        txt2.setOnClickListener(
          new View.OnClickListener() {
            @Override public void onClick(View v) {
              try {
                if ( gSensorListener.register(20, 2000) ) {
                  writeMessage("OtherActivity.gSensorListener#", "registered");
                }
              } catch(Exception e) {
                writeMessage("OtherActivity.gSensorListener#", e.getMessage());
              }
            }
          }
        );

/*

      TextView txt2 = (TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\otheractivity.xml
        txt2.setClickable(true);
        txt2.setOnTouchListener(
          new View.OnTouchListener() {
            // https://stackoverflow.com/questions/4804798/doubletap-in-android
            private android.view.GestureDetector gestureDetector
              = new android.view.GestureDetector(OtherActivity.this, new android.view.GestureDetector.SimpleOnGestureListener() {
                  @Override public boolean onDoubleTap(android.view.MotionEvent event) {
                    writeMessage("OtherActivity.gestureDetector", "onDoubleTap");
                    try {
                      compass.start();
                    } catch(Exception e) {
                      writeMessage("OtherActivity.onDoubleTap", e.getMessage());
                    } 
                    return super.onDoubleTap(event);
                  }
                });

            @Override public boolean onTouch(View v, android.view.MotionEvent event) {
              synchronized (this) {
                try {
                  compass.stop();  setTextOnSensorChanged((TextView) v, gSensorManager);
                } catch(Exception e) {
                  writeMessage("OtherActivity.onTouch", e.getMessage());
                }

                try {
                  gestureDetector.onTouchEvent(event);
                } catch(Exception e) {
                  writeMessage("OtherActivity.gestureDetector", e.getMessage());
                }
              }
              return true;
            }
          }
        );

*/

      findViewById(R.id.start_compass).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              //gSensorListener.register(500, 30000);
              startActivity(new android.content.Intent(v.getContext(), CompassActivity.class));
            } catch(Exception e) {
              writeMessage("OtherActivity.StartCompass", e.getMessage());
            }
          }
        }
      );

      findViewById(R.id.flashlight).setOnClickListener( // --> .\src\main\res\layout\otheractivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              MainActivity.enableTorchLigth(v.getContext(), toggleFlashLight);
              toggleFlashLight = ! toggleFlashLight;
            } catch(Exception e) {
              writeMessage("OtherActivity.flashlight", e.getMessage());
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

      writeMessage("OtherActivity.findViewById", e.getMessage());
      return;

    }

  }

}
