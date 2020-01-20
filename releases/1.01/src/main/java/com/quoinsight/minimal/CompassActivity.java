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
    Toast.makeText(CompassActivity.this, tag + ": " +  msg, Toast.LENGTH_SHORT).show();  // .setDuration(int duration)
    //android.util.Log.e(tag, msg);
    return;
  }

  public void msgBox(String title, String msg) {
    android.app.AlertDialog.Builder alrt = new android.app.AlertDialog.Builder(this);
    alrt.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("OK", null).show();
  }

  //////////////////////////////////////////////////////////////////////

  public float[] getCurrentSolarPosition(double latitue, double longitude) {
    double azimuth=0, altitude=0;  // "#sun@0°Az✳/0°Alt△"

    java.util.Calendar calendar = java.util.Calendar.getInstance();
    /*
      Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));

      Unless you are going to perform Date/Time related calculations, there is no point in instantiating Calendar with given TimeZone.
      After calling Calendar's getTime() method, you will receive Date object, which is timezone-less either way (GMT based, actually).

      //SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");  Date dateTime = sdf.parse("22-01-2015 10:20:56");
      //java.util.Date dateTime = new java.util.Date((new java.util.Date()).getTime()+8*60*60*1000);  
      //Calendar calendar = Calendar.getInstance();  calendar.setTime(dateTime);
      calendar.add(Calendar.HOUR_OF_DAY, 8);
    */

    // https://github.com/LocusEnergy/solar-calculations
    com.locusenergy.solarcalculations.SolarCalculations solarCalc
      = new com.locusenergy.solarcalculations.SolarCalculations(latitue, longitude);
    azimuth = solarCalc.calcSolarAzimuth(calendar); // timezone does not matter here !
      azimuth = (azimuth<180) ? azimuth+180 : azimuth-180; // convert south-based azimuth to north-based
    altitude = 90-solarCalc.calcSolarZenith(calendar); // elevation|altitude

    /*
      ** javax.annotation NOT COMPATIBLE WITH ANDROID !? https://github.com/shred/commons-suncalc
    */

    /*
      // https://github.com/florianmski/SunCalc-Java
      com.florianmski.suncalc.models.SunPosition sunPos
        = com.florianmski.suncalc.SunCalc.getSunPosition(
            java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")), // must specify timezone as UTC
              latitue, longitude
          );
      azimuth = Math.toDegrees(sunPos.getAzimuth());
      altitude = Math.toDegrees(sunPos.getAltitude());
    */

    return new float[] {(float)azimuth, (float)altitude};
  }

  public float[] getCurrentLunarPosition(double latitue, double longitude) {
    double azimuth=0, altitude=0;  // "#moon@0°Az✳/0°Alt△"

    // https://github.com/florianmski/SunCalc-Java
    com.florianmski.suncalc.models.MoonPosition lunarPos
      = com.florianmski.suncalc.SunCalc.getMoonPosition(
          java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")), // must specify timezone as UTC
            latitue, longitude
        );
    azimuth = Math.toDegrees(lunarPos.getAzimuth());
    altitude = Math.toDegrees(lunarPos.getAltitude());

    return new float[] {(float)azimuth, (float)altitude};
  }

  //////////////////////////////////////////////////////////////////////

  public static float px2dp(android.util.DisplayMetrics displayMetrics, float px) {
    return (float)android.util.TypedValue.applyDimension(
      android.util.TypedValue.COMPLEX_UNIT_PX, px, displayMetrics
    );
  }

  public static float dp2px(android.util.DisplayMetrics displayMetrics, float dp) {
    return (float)android.util.TypedValue.applyDimension(
      android.util.TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics
    );
  }

  public float[] getVwSzDimensionPx(android.view.View v) {
    android.util.DisplayMetrics m = v.getContext().getResources().getDisplayMetrics();
    android.view.ViewGroup.LayoutParams p = v.getLayoutParams();
    return new float[] {dp2px(m, p.width), dp2px(m, p.height)};
  }

  public double[] getCoordinates(double radian, double radius, double x0, double y0) {
    // http://mathcentral.uregina.ca/QQ/database/QQ.09.06/h/tim1.html
    // radian <==> east-based counterclockwise
    double x1 = radius*Math.cos(radian), y1 = radius*Math.sin(radian);
    return new double[] {x0+x1, y0+y1};
  }

  public double[] getCoordinatesN0(double azimuth, double radius, double x0, double y0) {
    // convert north-based clockwise bearing to east-based anticlockwise
    // 360-((azimuth<90) ? azimuth+270 : azimuth-90)
    double azimuthE0 = (azimuth>90) ? 450-azimuth : 90-azimuth;
    return getCoordinates(azimuthE0*(Math.PI/180), radius, x0, y0);
  }

  public float[] getCanvasCoordinates(float azimuth, float radius, float x0, float y0) {
    double[] coordinates = getCoordinatesN0(azimuth, radius, x0, -y0); // inverse y-axis
    return new float[] {(float)coordinates[0], -(float)coordinates[1]}; // inverse y-axis
  }

  public void drawMarkersOnCompassImg(android.widget.ImageView img, float[] solarPos, float[] lunarPos) {
    android.graphics.drawable.Drawable drawable = img.getDrawable();
    android.util.DisplayMetrics displayMetrics = img.getContext().getResources().getDisplayMetrics();

    // float[] widthHeight = getDrawDimensionPx(drawable); // no good https://stackoverflow.com/questions/4680499/how-to-get-the-width-and-height-of-an-android-widget-imageview
    float x0=drawable.getIntrinsicWidth()/2, y0=drawable.getIntrinsicHeight()/2, radius=x0*0.85f, mkrSz=30f;

    android.graphics.Bitmap
      bmp0 = ((android.graphics.drawable.BitmapDrawable)drawable).getBitmap(),
      bmp1 = bmp0.copy(android.graphics.Bitmap.Config.ARGB_8888, true); // Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas
      canvas = new android.graphics.Canvas(bmp1);
    android.graphics.Paint
      paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

    /*
      use pixels as UOM for drawing objects on the Canvas in android !
    */

    float[] coordinates = getCanvasCoordinates(solarPos[0], radius*(1-Math.abs(solarPos[1])/90), x0, y0);
    paint.setColor(android.graphics.Color.RED);  canvas.drawCircle(coordinates[0], coordinates[1], mkrSz, paint);
    paint.setTextSize(40);  canvas.drawText("☼🌞︎V♀🌕︎🌝︎", coordinates[0]+50, coordinates[1]+50, paint);

    coordinates = getCanvasCoordinates(lunarPos[0], radius*(1-Math.abs(lunarPos[1])/90), x0, y0);
    paint.setColor(android.graphics.Color.YELLOW);  canvas.drawCircle(coordinates[0], coordinates[1], mkrSz, paint);

    if ( lunarPos[1] > 0 ) {
      paint.setStyle(android.graphics.Paint.Style.STROKE);  paint.setStrokeWidth(mkrSz/2);
      paint.setColor(android.graphics.Color.BLUE);  canvas.drawCircle(coordinates[0], coordinates[1], mkrSz, paint);
    }

    //img.invalidate(); img1.draw(canvas); // this does not seem to change img1
    img.setImageBitmap(bmp1); // this works correctly, and capture the changes
  }

  public void overlayImgVw(android.widget.ImageView img, android.graphics.Bitmap bmp) {
    android.graphics.drawable.Drawable drawable = img.getDrawable();
    android.util.DisplayMetrics displayMetrics = img.getContext().getResources().getDisplayMetrics();
    // bmp = android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.img1);

    android.graphics.Bitmap
      bmp0 = ((android.graphics.drawable.BitmapDrawable)drawable).getBitmap(),
      bmp1 = bmp0.copy(android.graphics.Bitmap.Config.ARGB_8888, true); // Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas
      canvas = new android.graphics.Canvas(bmp1);
    android.graphics.Paint
      paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

    canvas.drawBitmap(bmp, 0/*left*/, 0/*top*/, null);
    //img.invalidate(); img1.draw(canvas); // this does not seem to change img1
    img.setImageBitmap(bmp1); // this works correctly, and capture the changes
  }

  //////////////////////////////////////////////////////////////////////

  // Important: To avoid the unnecessary usage of battery,
  // register the listener in the onResume method and de-register on the onPause method.

  @Override protected void onResume() {
    super.onResume();
    //gSensorListener.register(50, 2000);
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
          @Override public void OnOrientationDataLoaded(final float[] orienationData) {
            runOnUiThread(new Runnable() { @Override public void run() {
              try {
                float azimuth = (float)Math.toDegrees(orienationData[0]);
                float azimuthFix = 0f;  azimuth = (azimuth + azimuthFix + 360) % 360;

                //gSensorListener.unregister();
                ((TextView)findViewById(R.id.txt2)).setText(android.text.Html.fromHtml( // CSS is not supported!
                  "#" + MainActivity.getDateStr("ss") + ":<br>"
                     + "<font size='2em'>✳" + String.valueOf(Math.round(azimuth)) + "°</font>"
                ));

                android.widget.ImageView img1 = (android.widget.ImageView)findViewById(R.id.img1);
                 img1.setVisibility(android.view.View.GONE);  img1.requestLayout(); // redraw
                   img1.setRotation(360-azimuth);  // img1.setRotationX(360-(float)Math.toDegrees(orienationData[1]));  img1.setRotationY(360-(float)Math.toDegrees(orienationData[2]));
                 img1.setVisibility(android.view.View.VISIBLE);  img1.requestLayout(); // redraw

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
                writeMessage("CompassActivity.OnOrientationDataLoaded", e.getMessage());
                return;
              }
            }});
          }
          @Override public void OnAccel2g(final float gForce) {
            writeMessage("OtherActivity.OnAccel2g", "device shaken at " + String.valueOf(Math.round(gForce)) + "g");
          }
        });
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

          float[] solarPos = getCurrentSolarPosition(5.2960, 100.2752); // Penang International Airport
          float[] lunarPos = getCurrentLunarPosition(5.2960, 100.2752); // Penang International Airport
          txt1.setText( txt1.getText().toString()
            + "\n#sun@" + String.valueOf(Math.round(solarPos[0])) + "°Az✳"
              + "/" + String.valueOf(Math.round(solarPos[1])) + "°Alt△"
            + "\n#moon@" + String.valueOf(Math.round(lunarPos[0])) + "°Az✳"
              + "/" + String.valueOf(Math.round(lunarPos[1])) + "°Alt△"
          );
          drawMarkersOnCompassImg(img1, solarPos, lunarPos);

          //overlayImgVw(img1, android.graphics.BitmapFactory.decodeResource(img1.getContext().getResources(), R.drawable.icon)); 

          //img1.requestLayout(); //just redraw, not needed as setImageBitmap is done above
        img1.setOnClickListener(
          new View.OnClickListener() {
            @Override public void onClick(View v) {
              try {
                if ( gSensorListener.register(50, 2000) ) {
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
                if ( gSensorListener.register(50, 2000) ) {
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
              gSensorListener.register(300, 30000);
            } catch(Exception e) {
              writeMessage("CompassActivity.StartCompass", e.getMessage());
            }
          }
        }
      );

      // start compass directly from onCreate
      try {
        gSensorListener.register(300, 30000);
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
