package com.quoinsight.minimal;
/*
  # inspired by https://czak.pl/2016/01/13/minimal-android-project.html
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/MainActivity.java
*/

public class MainActivity extends android.app.Activity {

  public boolean toggleFlashLight = true;
  public myAudioService audioSvc;

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

    android.widget.TextView txt1 = new android.widget.TextView(this);
      txt1.setGravity(android.view.Gravity.CENTER_HORIZONTAL);  // txt1.setText("Hello world!\n[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
      txt1.setText(android.text.Html.fromHtml(
        "Hello world!<br><small><small>[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]</small></small>"
          + "<br><font size='1.75em'>" + commonUtil.getChineseDateStr() + "</font>"
      )); // Hello world\n[2020-01-09 十二月十五⁄30巳时 09:06:21] --> Hello world\n[2020-01-09 09:06:21]\n十二月十五⁄30巳时

    final android.widget.Spinner spinner1 = new android.widget.Spinner(this);
      final java.util.Hashtable<String, String> radioStations = new java.util.Hashtable<String, String>();
        // https://docs.google.com/spreadsheets/d/1cj66AnWNgJ3GqDTIQBWeUEsapjp_Zk37v11iwoa8xzM/edit#gid=0
        radioStations.put("SleepRadio", "http://149.56.234.138:8169/;");
        radioStations.put("澳門電台", "http://live4.tdm.com.mo:1935/live/_definst_/rch2.live/playlist.m3u8");
        radioStations.put("AiFM", "https://aifmmobile.secureswiftcontent.com/memorystreams/HLS/rtm-ch020/rtm-ch020.m3u8");
        radioStations.put("香港電台第一台", "http://stm.rthk.hk:80/radio1");
      android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
        this, android.R.layout.simple_spinner_item,
        //new String[] { "SleepRadio", "澳門電台", "AiFM", "港台" }
        new java.util.ArrayList<String>(radioStations.keySet())
      );
      spinner1.setAdapter(adapter);
     /*
      spinner1.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(
          android.widget.AdapterView<?> parent, android.view.View view, int position, long id
        ) {
          // android.util.Log.v("item", (String) parent.getItemAtPosition(position));
          android.widget.Toast.makeText(MainActivity.this, 
            "selected item: " + (String)parent.getItemAtPosition(position),
          android.widget.Toast.LENGTH_LONG).show();  // .setDuration(int duration)
        }
        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {
          // TODO Auto-generated method stub
        }
      });
     */

    android.widget.Button button1 = new android.widget.Button(this);
      button1.setAllCaps(false);  button1.setText("🌐");  
      //button1.setText("🎧 OpenUrl 📻"); // 📻 🎤 🎧 🔈 🔉 🔊 🕨 🕩 🕪
      button1.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            sysUtil.launchUrl(v.getContext(), radioStations.get(spinner1.getSelectedItem().toString()));
          }
        }
      );

    android.widget.Button button2 = new android.widget.Button(this);
      button2.setAllCaps(false);
      button2.setText(" ▶ "); // ▶ ⏹ 🔈 🔉 🔊 🕨 🕩 🕪
      button2.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            try {

              String url = "http://live4.tdm.com.mo:1935/live/_definst_/rch2.live/playlist.m3u8";
              url = "http://129.232.169.93/proxy/arrowline?codec=mp3";
              url = "http://stm.rthk.hk:80/radio1";
              url = radioStations.get(spinner1.getSelectedItem().toString());

              android.content.Intent playbackAction
               = new android.content.Intent("com.quoinsight.minimal.myAudioServicePlayAction");
                playbackAction.setPackage(MainActivity.this.getPackageName());
                 playbackAction.putExtra("url", url);
              startService(playbackAction);

              if (true) return;

              android.support.v4.app.NotificationCompat.Builder builder
               = commonGui.createNotificationBuilder(
                   MainActivity.this, "QuoInsight#ChannelID", "QuoInsight#Channel", "QuoInsight.Minimal"
                 );

              builder.setSmallIcon(android.R.drawable.stat_sys_headset) // this is the only user-visible content that's required.
                .setContentText("playing...") // body text
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_HIGH)
                .setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC) // android.app.Notification.VISIBILITY_PUBLIC
                .setContentIntent( // default action when the user taps the notification
                  android.app.PendingIntent.getActivity(
                    MainActivity.this, 0,
                    new android.content.Intent(MainActivity.this, MainActivity.class), 0
                  )
                )
                .addAction(
                  android.R.drawable.ic_media_play, "play", sysUtil.getPendingActivity(MainActivity.this, OtherActivity.class)
                )
                .addAction(
                  android.R.drawable.ic_media_pause, "stop", sysUtil.getPendingActivity(MainActivity.this, CompassActivity.class)
                )
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                  .setShowActionsInCompactView(0,1)
                )
                .setAutoCancel(false)
              ;

              commonGui.cancelNotification(MainActivity.this, 1001);
              commonGui.submitNotification(MainActivity.this, builder, 1001);  // this does not show in lockscreen regardless of the options

              if (true) return;

              builder.setSmallIcon(android.R.drawable.stat_sys_headset) // this is the only user-visible content that's required.
                .setContentText("playing...") // body text
                .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent( // default action when the user taps the notification
                  android.app.PendingIntent.getActivity(
                    MainActivity.this, 0,
                    new android.content.Intent(MainActivity.this, MainActivity.class), 0
                  )
                )
                //.setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC)
                //.addAction(R.drawable.ic_stop_white, "stop", pendingIntent)
                .addAction(
                  android.R.drawable.ic_media_pause, "stop", sysUtil.getPendingService(
                    MainActivity.this, myAudioService.class, "com.quoinsight.minimal.myAudioServiceStopAction", 3
                  )
                )
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                  .setShowActionsInCompactView(0)
                )
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
              ;

              commonGui.submitNotification(MainActivity.this, builder, 1001);  // this does not show in lockscreen regardless of the options

              if (true) return;

             /*
              android.os.Handler handler = new android.os.Handler();
              com.google.android.exoplayer2.trackselection.TrackSelector trackSelector = new com.google.android.exoplayer2.trackselection.DefaultTrackSelector(handler);
              com.google.android.exoplayer2.LoadControl loadControl = new com.google.android.exoplayer2.DefaultLoadControl();
              com.google.android.exoplayer2.SimpleExoPlayer exoPlayer = com.google.android.exoplayer2.ExoPlayerFactory.newSimpleInstance(MainActivity.this, trackSelector, loadControl);

              android.net.Uri audioUri = android.net.Uri.parse(url);
              com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory dataSourceFactory = new com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory("ExoPlayerDemo");
              com.google.android.exoplayer2.extractor.ExtractorsFactory extractor = new com.google.android.exoplayer2.extractor.DefaultExtractorsFactory();
              com.google.android.exoplayer2.source.MediaSource audioSource = new com.google.android.exoplayer2.source.ExtractorMediaSource(audioUri, dataSourceFactory, extractor, null, null);
              exoPlayer.prepare(audioSource);
              exoPlayer.setPlayWhenReady(true);
             */

              // notificationManager = new MediaNotificationManager(this);

             /*
              // https://stackoverflow.com/questions/48196181/how-to-put-media-buttons-on-the-lockscreen-with-exoplayer
              // https://github.com/google/ExoPlayer/issues/3247 [add SimpleExoPlayerView Controls on Lock Screen and Notifications]
              // https://medium.com/google-exoplayer/the-mediasession-extension-for-exoplayer-82b9619deb2d
              android.support.v4.media.session.MediaSessionCompat mediaSession
               = android.support.v4.media.session.MediaSessionCompat(MainActivity.this, "myAudioPlayer");
              com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector mediaSessionConnector
               = new com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector(mediaSession);
              mediaSessionConnector.setPlayer(exoPlayer);
             */

             // https://stackoverflow.com/questions/12821216/android-mediaplayer-takes-long-time-to-prepare-and-buffer

             /*
              android.media.MediaPlayer mPlayer = new android.media.MediaPlayer();
              mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
              mPlayer.setDataSource(url);
              mPlayer.prepare(); // might take long! (for buffering, etc)
              mPlayer.start();
             */

             /*
              // C:\Data\adm\mobile\Android\apk\_src\radio-player\app\src\main\java\com\mcakir\radio\player\MediaNotificationManager.java
              // deprecated: startService(new android.content.Intent("com.quoinsight.minimal.myAudioServicePlayAction"));

              //android.content.Intent playbackAction
              //  = new android.content.Intent(audioSvc, myAudioService.class);
              //playbackAction.setAction("com.quoinsight.minimal.myAudioServicePlayAction");
             */

            } catch(Exception e) {
              commonGui.writeMessage(MainActivity.this, "MainActivity.playbackAction", e.getMessage());
            }
          }
        }
      );

    android.widget.TextView txt2 = new android.widget.TextView(this);
      txt2.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
      txt2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 72);
      txt2.setText("🔋" + Integer.toString(sysUtil.getBatteryLevel(this)) + "%");
      txt2.setClickable(true);
      txt2.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            // android.widget.TextView txt2 = (android.widget.TextView) v; // findViewById(v.getId());
            ((android.widget.TextView) v).setText(android.text.Html.fromHtml(
              "<small><small><small><small># " + commonUtil.getDateStr("ss") + " :</small></small></small></small>"
                 + "<br>🔋" + Integer.toString(sysUtil.getBatteryLevel(v.getContext())) + "%"
            ));
          }
        }
      );

    android.widget.Button btnNext = new android.widget.Button(this);
      btnNext.setText("⎘ Next");
      btnNext.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            startActivity(new android.content.Intent(v.getContext(), OtherActivity.class));
          }
        }
      );

    android.widget.Button btnCompass = new android.widget.Button(this);
      btnCompass.setAllCaps(false);
      btnCompass.setText("✳ Start Compass 🧭");
      btnCompass.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            startActivity(new android.content.Intent(v.getContext(), CompassActivity.class));
          }
        }
      );

    android.widget.Button btnTorch = new android.widget.Button(this);
      btnTorch.setAllCaps(false);
      btnTorch.setText("🔦 FlashLight");
      btnTorch.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
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

    android.widget.Button button9 = new android.widget.Button(this);
      button9.setText("⎊ Quit"); // ⏻ ≡  [🚪←🚶] 𓁆 𝍇去 𝌶逃
      button9.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            android.widget.Toast.makeText(
              MainActivity.this, "closing...", android.widget.Toast.LENGTH_LONG
            ).show();  // .setDuration(int duration)

            //try { Thread.sleep(3000); } catch(InterruptedException e) {}

            quit(MainActivity.this);
          }
        }
      );

    android.widget.EditText edit1 = new android.widget.EditText(this);
      edit1.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
      edit1.setText("testing ...");
      edit1.setOnTouchListener(new android.view.View.OnTouchListener(){
        @Override public boolean onTouch(android.view.View view, android.view.MotionEvent motionEvent) {
          view.getParent().requestDisallowInterceptTouchEvent(true);
          switch (motionEvent.getAction() & android.view.MotionEvent.ACTION_MASK){
            case android.view.MotionEvent.ACTION_UP:
              view.getParent().requestDisallowInterceptTouchEvent(false);
              break;
          }
          return false;
        }
      });

    android.widget.TextView txt9 = new android.widget.TextView(this);
      txt9.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
      txt9.setLinksClickable(true);  // do not setAutoLinkMask !! txt9.setAutoLinkMask(android.text.util.Linkify.ALL);
      txt9.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
      txt9.setText(android.text.Html.fromHtml(
        " [ <A href='https://github.com/QuoInsight/minimal.apk'>src</A> ] "
         + " [ <A href='https://github.com/QuoInsight/minimal.apk/raw/master/bin/quoinsight.apk'>apk</A> ] "
           + " [ <A href='https://play.google.com/store/apps/details?id=com.quoinsight.minimal'>store</A> ] "
          + " [ <A href='https://sites.google.com/site/quoinsight/home/minimal-apk'>about</A> ] "
      ));

    // startActivityForResult(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    // launchApp("com.otherapp.package");

    android.widget.ScrollView scrollable = new android.widget.ScrollView(this);
      scrollable.setLayoutParams(new android.widget.ScrollView.LayoutParams(
        android.widget.ScrollView.LayoutParams.MATCH_PARENT, android.widget.ScrollView.LayoutParams.MATCH_PARENT
      ));
      android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);  layout.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
          android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.addView(txt1, params);
        android.widget.LinearLayout layout2 = new android.widget.LinearLayout(this);
          layout2.setOrientation(android.widget.LinearLayout.HORIZONTAL);  layout2.setGravity(android.view.Gravity.CENTER);
          layout2.addView(spinner1, params);  layout2.addView(button2, params);  layout2.addView(button1, params);
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

    //android.widget.LinearLayout.LayoutParams p = button2.getLayoutParams();
    //p.width = (int) commonGui.dp2px(commonGui.getResourceDisplayMetrics(this), 30);
    //button2.setLayoutParams(p);
  }

}
