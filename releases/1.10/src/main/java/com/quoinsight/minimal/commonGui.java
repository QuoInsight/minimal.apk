package com.quoinsight.minimal;

public class commonGui {

  //////////////////////////////////////////////////////////////////////

  static final public void writeMessage(android.content.Context parentContext, String tag, String msg, String...args) {  // varargs
    android.widget.Toast.makeText(
      parentContext, tag + ": " +  msg,
        android.widget.Toast.LENGTH_LONG
    ).show();  // .setDuration(int duration)
    //android.util.Log.e(tag, msg);
    return;
  }

  static final public void msgBox(android.content.Context parentContext, String title, String msg) {
    android.app.AlertDialog.Builder alrt = new android.app.AlertDialog.Builder(parentContext);
    alrt.setTitle(title).setMessage(msg).setCancelable(false).setPositiveButton("OK", null).show();
  }

  //////////////////////////////////////////////////////////////////////

  // commonGui.createNotificationChannel(MainActivity.this, "QuoInsight", "QuoInsight", "QuoInsight.Minimal");

  public static void createNotificationChannel(
    android.content.Context parentContext, String channelID, String name, String description
  ) {
    // https://developer.android.com/training/notify-user/build-notification
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      android.app.NotificationChannel ntfnChannel
       = new android.app.NotificationChannel(
           channelID, name, android.app.NotificationManager.IMPORTANCE_HIGH
         );
      ntfnChannel.setDescription(description);  ntfnChannel.setSound(null, null);
      ntfnChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);
      // Register the channel with the system; you can't change the importance or other notification behaviors after this
      android.app.NotificationManager ntfnManager
       = parentContext.getSystemService(android.app.NotificationManager.class);
      ntfnManager.createNotificationChannel(ntfnChannel);
    }
  }

  public static android.support.v4.app.NotificationCompat.Builder createNotificationBuilder(
    android.content.Context parentContext, String channelID, String name, String description
  ) {
    createNotificationChannel(parentContext, channelID, name, description);
    android.support.v4.app.NotificationCompat.Builder ntfnBuilder
     = new android.support.v4.app.NotificationCompat.Builder(parentContext, channelID);
    return ntfnBuilder;
  }

  public static void submitNotification(
    android.content.Context parentContext,
    android.support.v4.app.NotificationCompat.Builder ntfnBuilder,
    int ntfnID
  ) {
    // notificationId is a unique int for each notification that you must define
    // android.app.Notification ntfn = ntfnBuilder.build();
    android.support.v4.app.NotificationManagerCompat ntfnManager
     = android.support.v4.app.NotificationManagerCompat.from(parentContext);
    ntfnManager.notify(ntfnID, ntfnBuilder.build());
  }

  public static void cancelNotification(android.content.Context parentContext, int ntfnID) {
    // notificationId is a unique int for each notification that you must define
    // android.app.NotificationManager
    android.support.v4.app.NotificationManagerCompat ntfnManager
     = android.support.v4.app.NotificationManagerCompat.from(parentContext);
    ntfnManager.cancel(ntfnID);
  }

  //////////////////////////////////////////////////////////////////////

  static final public android.widget.EditText makeEditTextSelectableReadOnly(android.widget.EditText edtxt) {
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

  public static android.util.DisplayMetrics getDefaultDisplayMetrics(android.app.Activity parentActivity) {
    android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
    parentActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    return displayMetrics;
  }

  public static android.util.DisplayMetrics getResourceDisplayMetrics(android.content.Context parentContext) {
    return parentContext.getResources().getDisplayMetrics();
  }

  //////////////////////////////////////////////////////////////////////

  public static float px2dp(android.util.DisplayMetrics displayMetrics, float px) {
    return (px / displayMetrics.density);
    /*
      TypedValue.applyDimension converts an unpacked complex data value holding a dimension
       to its final floating point value", i.e., the return value is always in px.
      ! Below will be incorrect and will just return the same value !
      return (float)android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_PX, px, displayMetrics
      );
    */
  }

  public static float dp2px(android.util.DisplayMetrics displayMetrics, float dp) {
    return (dp * displayMetrics.density);
    //return (float)android.util.TypedValue.applyDimension(
    //  android.util.TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics
    //);
  }

  public static float[] getVwSzDimensionPx(android.view.View v) {
    android.util.DisplayMetrics m = getResourceDisplayMetrics(v.getContext());
    android.view.ViewGroup.LayoutParams p = v.getLayoutParams();
    return new float[] {dp2px(m, p.width), dp2px(m, p.height)};
  }

  public static void overlayImgVw(android.widget.ImageView img, android.graphics.Bitmap bmp) {
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
    //img.invalidate(); img.draw(canvas); // this does not seem to change img
    img.setImageBitmap(bmp1); // this works correctly, and capture the changes
  }

  //////////////////////////////////////////////////////////////////////

  public static void canvasDrawLine(android.graphics.Canvas canvas, float az, float x0, float y0) {
    double radius = (x0+y0)/2.0, radian = (az>90) ? Math.toRadians(450.0-az) : Math.toRadians(90.0-az);
    float x = (float)(radius*Math.cos(radian)), y = (float)(radius*Math.sin(radian));

    android.graphics.Paint paint
     = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
      paint.setColor(android.graphics.Color.RED);  paint.setStrokeWidth(5);

    canvas.drawLine(x0-x, y0+y, x0+x, y0-y, paint); // inverse y-axis
    return;
  }

  //////////////////////////////////////////////////////////////////////

}
