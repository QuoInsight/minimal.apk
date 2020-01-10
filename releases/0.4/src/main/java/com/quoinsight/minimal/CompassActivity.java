package com.quoinsight.minimal;
/*
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/CompassActivity.java
*/

import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;  

import android.widget.EditText;

import android.view.Gravity;
import android.view.View;

public class CompassActivity extends android.app.Activity
  // implements android.hardware.SensorEventListener // !! need this for it work correctly with SensorEvent !!
{
  public void writeMessage(String tag, String msg, String...args) {  // varargs
    Toast.makeText(CompassActivity.this, tag + ": " +  msg, Toast.LENGTH_LONG).show();  // .setDuration(int duration)
    //android.util.Log.e(tag, msg);
    return;
  }

  public void msgBox(String title, String msg) {
    android.app.AlertDialog.Builder alrt = new android.app.AlertDialog.Builder(this);
    alrt.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("OK", null).show();
  }

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
      setContentView(R.layout.compassactivity);  // --> .\src\main\res\layout\compassactivity.xml
      // lock the screen orientation here
      setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } catch(Exception e) {
      writeMessage("CompassActivity.setContentView", e.getMessage());
      return;
    }

    try {

      TextView txt1 = (TextView) findViewById(R.id.txt1);  // --> .\src\main\res\layout\compassactivity.xml
        txt1.setText("Hello from CompassActivity!\n[" + MainActivity.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
        // avoid EditText from gaining focus at Activity startup 
        txt1.setFocusable(true);  txt1.setFocusableInTouchMode(true);  txt1.requestFocus();

      try {
        gSensorListener = new mySensorListener(this); // will run into error if this is executed before onCreate() 
        gSensorListener.setHandlers(new mySensorListener.handlers() {
          @Override public void OnMessage(String tag, String msg, String...args) {
            writeMessage(tag, msg, args);
          }
          @Override public void OnAzimuthDataLoaded(final float azimuth) {
            runOnUiThread(new Runnable() { @Override public void run() {
              try {
                //gSensorListener.unregister();
                ((TextView)findViewById(R.id.txt2)).setText(android.text.Html.fromHtml( // CSS is not supported!
                  "#" + MainActivity.getDateStr("ss") + ":<br>"
                     + "<font size='2em'>✳" + String.valueOf(Math.round(azimuth)) + "°</font>"
                ));

                ((android.widget.ImageView)findViewById(R.id.img1)).setRotation(360-azimuth);
                /*
                  // https://stackoverflow.com/questions/8981845/android-rotate-image-in-imageview-by-an-angle
                  android.widget.ImageView img1 = (android.widget.ImageView)findViewById(R.id.img1);
                    // img1.setImageResource(R.drawable.compass);
                  android.graphics.Rect b = img1.getDrawable().getBounds();
                    img1.setScaleType(android.widget.ImageView.ScaleType.MATRIX);
                  android.graphics.Matrix m = new android.graphics.Matrix();
                    m.postRotate(360-azimuth, b.width()/2, b.height()/2);
                */
              } catch(Exception e) {
                writeMessage("CompassActivity.OnAzimuthDataLoaded", e.getMessage());
                return;
              }
            }});
        }});
      } catch(Exception e) {
        writeMessage("CompassActivity.mySensorListener", e.getMessage());
      }

      android.widget.ImageView img1 = (android.widget.ImageView) findViewById(R.id.img1);  // --> .\src\main\res\layout\compassactivity.xml
        img1.setClickable(true);
          android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
          getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
          int h=displayMetrics.heightPixels-20, w=displayMetrics.widthPixels-20;
          if (h > w) { h=w; } else { w=h; }
          //img1.getLayoutParams().height = h;
          android.view.ViewGroup.LayoutParams p = img1.getLayoutParams();
          p.height=h; p.width=w; img1.setLayoutParams(p);
          img1.requestLayout(); // redraw
        img1.setOnClickListener(
          new View.OnClickListener() {
            @Override public void onClick(View v) {
              try {
                if ( gSensorListener.register(20, 2000) ) {
                  writeMessage("CompassActivity.gSensorListener#", "registered");
                }
              } catch(Exception e) {
                writeMessage("CompassActivity.gSensorListener#", e.getMessage());
              }
            }
          }
        );

      TextView txt2 = (TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\compassactivity.xml
        txt2.setClickable(true);
        txt2.setOnClickListener(
          new View.OnClickListener() {
            @Override public void onClick(View v) {
              try {
                if ( gSensorListener.register(20, 2000) ) {
                  writeMessage("CompassActivity.gSensorListener#", "registered");
                }
              } catch(Exception e) {
                writeMessage("CompassActivity.gSensorListener#", e.getMessage());
              }
            }
          }
        );

/*

      TextView txt2 = (TextView) findViewById(R.id.txt2);  // --> .\src\main\res\layout\compassactivity.xml
        txt2.setClickable(true);
        txt2.setOnTouchListener(
          new View.OnTouchListener() {
            // https://stackoverflow.com/questions/4804798/doubletap-in-android
            private android.view.GestureDetector gestureDetector
              = new android.view.GestureDetector(CompassActivity.this, new android.view.GestureDetector.SimpleOnGestureListener() {
                  @Override public boolean onDoubleTap(android.view.MotionEvent event) {
                    writeMessage("CompassActivity.gestureDetector", "onDoubleTap");
                    try {
                      compass.start();
                    } catch(Exception e) {
                      writeMessage("CompassActivity.onDoubleTap", e.getMessage());
                    } 
                    return super.onDoubleTap(event);
                  }
                });

            @Override public boolean onTouch(View v, android.view.MotionEvent event) {
              synchronized (this) {
                try {
                  compass.stop();  setTextOnSensorChanged((TextView) v, gSensorManager);
                } catch(Exception e) {
                  writeMessage("CompassActivity.onTouch", e.getMessage());
                }

                try {
                  gestureDetector.onTouchEvent(event);
                } catch(Exception e) {
                  writeMessage("CompassActivity.gestureDetector", e.getMessage());
                }
              }
              return true;
            }
          }
        );

*/

      findViewById(R.id.start_compass).setOnClickListener( // --> .\src\main\res\layout\compassactivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            try {
              gSensorListener.register(500, 30000);
            } catch(Exception e) {
              writeMessage("CompassActivity.StartCompass", e.getMessage());
            }
          }
        }
      );

      try {
        gSensorListener.register(500, 30000);
      } catch(Exception e) {
        writeMessage("CompassActivity.StartCompass", e.getMessage());
      }

      findViewById(R.id.btnPrev).setOnClickListener( // --> .\src\main\res\layout\compassactivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            startActivity(new android.content.Intent(v.getContext(), MainActivity.class));
          }
        }
      );

      findViewById(R.id.button9).setOnClickListener( // --> .\src\main\res\layout\compassactivity.xml
        new View.OnClickListener() {
          public void onClick(View v) {
            //this.finishAffinity();
            finishAndRemoveTask();
          }
        }
      );

      TextView txt9 = (TextView) findViewById(R.id.txt9);  // --> .\src\main\res\layout\compassactivity.xml
        txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
        txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        txt9.setText(android.text.Html.fromHtml(txt9.getText().toString()));

    } catch(Exception e) {

      writeMessage("CompassActivity.findViewById", e.getMessage());
      return;

    }

  }

}
