package com.quoinsight.minimal;
/*
  # inspired by https://czak.pl/2016/01/13/minimal-android-project.html
  # thisSource: https://github.com/QuoInsight/minimal.apk/edit/master/src/main/java/com/quoinsight/minimal/MainActivity.java
*/

public class MainActivity extends android.app.Activity {

  public android.widget.TextView txt2 = null;
  public boolean toggleFlashLight = true;
  public myAudioService audioSvc = null;

  public android.content.SharedPreferences gSharedPref;

  private static MainActivity thisInstance;
  public static MainActivity getInstance() {
    return thisInstance;
  }

  // Hashtable is in random order: https://beginnersbook.com/2014/06/how-to-sort-hashtable-in-java/
  public java.util.LinkedHashMap<String, String> streamUrls = new java.util.LinkedHashMap<String, String>();
  public android.widget.ArrayAdapter<String> spinner1Adapter = null;
  // public android.widget.Spinner spinner1 = null;

  public java.util.LinkedHashMap<String, String> reloadStreamUrls(android.content.SharedPreferences gSharedPref, String prefName) {
    String stationList = gSharedPref.getString(prefName,null);
    streamUrls.clear();
    if (stationList==null || stationList.length()==0) {
      // https://docs.google.com/spreadsheets/d/1cj66AnWNgJ3GqDTIQBWeUEsapjp_Zk37v11iwoa8xzM/edit#gid=0
      streamUrls.put("澳門", "http://live4.tdm.com.mo:1935/live/_definst_/rch2.live/playlist.m3u8");
      streamUrls.put("港台#1", "http://stm.rthk.hk:80/radio1");
      streamUrls.put("新城知讯", "http://metroradio-lh.akamaihd.net/i/997_h@349799/index_48_a-p.m3u8");
      streamUrls.put("佛山", "https://live-hls-fs.linker.cc/fshs/fs_ts_01.m3u8");
      streamUrls.put("AiFM", "https://aifmmobile.secureswiftcontent.com/memorystreams/HLS/rtm-ch020/rtm-ch020.m3u8");
      streamUrls.put("988", "http://starrfm.rastream.com/starrfm-988.android");
      streamUrls.put("南非", "http://129.232.169.93/proxy/arrowline?codec=mp3");
      streamUrls.put("Surabaya", "http://streaming.stratofm.com:8300/;stream.nsv");
      streamUrls.put("三藩市", "http://50.7.71.27:9731/;?icy=http");
      streamUrls.put("Buddhist", "http://15913.live.streamtheworld.com/SAM11AAC025.mp3");
      streamUrls.put("良友", "http://listen2.txly1.net:8000/ly729_a");
      streamUrls.put("天主", "http://dreamsiteradiocp2.com:8038/;");
      streamUrls.put("大愛", "https://streamingv2.shoutcast.com/daai-radio_128.aac");
      streamUrls.put("cello", "http://streams.calmradio.com:4628/stream");
      streamUrls.put("piano", "https://pianosolo.streamguys1.com/live");
      streamUrls.put("华乐", "http://radio2.chinesemusicworld.com/;");
      streamUrls.put("rainforest", "https://music.wixstatic.com/mp3/e7f4d3_4ce223112471435c86d2292ddb4a6e7c.mp3");
      streamUrls.put("birds", "http://strm112.1.fm/brazilianbirds_mobile_mp3");
      streamUrls.put("SleepRadio", "http://149.56.234.138:8169/;");
    } else {
      for(String s : stationList.split("\\r?\\n")) {
        String[] a = s.split("\\|");
        if (a.length > 1) streamUrls.put(a[0], a[1]);
      }
    }
    return streamUrls;
  }

  public String getStreamUrls() {
    String strList = "";
    for (String k : this.streamUrls.keySet()) {
      strList += k + "|" + this.streamUrls.get(k) + "\n";
    }
    return strList;
  }

  public void reloadSpinner1Adapter() {
    java.util.LinkedHashMap<String, String> streamUrls = reloadStreamUrls(gSharedPref, "stationList");
    android.widget.ArrayAdapter<String> adapter = spinner1Adapter;
    java.util.ArrayList<String> list = new java.util.ArrayList<String>(streamUrls.keySet()); 
    // String[] stringArr = commonUtil.getKeysetStringArr(streamUrls);
    try {
      adapter.clear();  adapter.addAll(list);
      adapter.notifyDataSetChanged(); // optional, as the dataset change should trigger this by default
    } catch(Exception e) {
      commonGui.writeMessage(MainActivity.this, "MainActivity.reloadSpinner1Adapter", e.getMessage());
    }
  }

  //////////////////////////////////////////////////////////////////////

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
    menu.add(0, 88, 0, "⚙ Settings"); 
    menu.add(0, 99, 0, "⏏ Quit"); 
    return true;
  }
  @Override public boolean onOptionsItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case 88:
        startActivity(new android.content.Intent(this.getApplicationContext(), ListCfgActivity.class));
        return true;
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

    thisInstance = this;
    gSharedPref = ((android.app.Activity)this).getSharedPreferences(getApplicationContext().getPackageName(), android.content.Context.MODE_PRIVATE);

    android.widget.TextView txt1 = new android.widget.TextView(this);
      txt1.setGravity(android.view.Gravity.CENTER_HORIZONTAL);  // txt1.setText("Hello world!\n[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]");
      txt1.setText(android.text.Html.fromHtml(
        "Hello world!<br><small><small>[" + commonUtil.getDateStr("yyyy-MM-dd HH:mm:ss") + "]</small></small>"
          + "<br><font size='1.75em'>" + commonUtil.getChineseDateStr() + "</font>"
      )); // Hello world\n[2020-01-09 十二月十五⁄30巳时 09:06:21] --> Hello world\n[2020-01-09 09:06:21]\n十二月十五⁄30巳时
      txt1.setClickable(true);
      txt1.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            startActivity(new android.content.Intent(v.getContext(), CalendarActivity.class));
          }
        }
      );

    this.txt2 = new android.widget.TextView(this);
      txt2.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
      txt2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 72);
      txt2.setText("🔋" + Integer.toString(sysUtil.getBatteryLevel(this)) + "%");
      txt2.setClickable(true);
      txt2.setOnClickListener(
        new android.view.View.OnClickListener() {
          public void onClick(android.view.View v) {
            // android.widget.TextView txt2 = (android.widget.TextView) v; // findViewById(v.getId());
            ((android.widget.TextView) v).setText(android.text.Html.fromHtml(
              // "<small><small><small><small># " + commonUtil.getDateStr("ss") + " :</small></small></small></small><br>"
              "🔋" + Integer.toString(sysUtil.getBatteryLevel(v.getContext())) + "%"
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
      ));  // scrollable.setFillViewport(true);
      android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);  layout.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
          android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layout.addView(txt1, params);
        android.widget.LinearLayout playerLayout = (android.widget.LinearLayout)android.view.View.inflate(this, R.layout.mainplayer, null);
        layout.addView(playerLayout, params);  // load mainplayer.xml
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

    ////////////////////////////////////////////////////////////////////

      final android.widget.Spinner spinner1 =  (android.widget.Spinner)findViewById(R.id.spinner1);
        spinner1Adapter = new android.widget.ArrayAdapter<String>(
          this, android.R.layout.simple_spinner_item
        );  spinner1.setAdapter(spinner1Adapter);
        reloadSpinner1Adapter();
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

      android.widget.Button button1 = (android.widget.Button)findViewById(R.id.openUrl);
        button1.setAllCaps(false);  button1.setText("🌐");  
        //button1.setText("🎧 OpenUrl 📻"); // 📻 🎤 🎧 🔈 🔉 🔊 🕨 🕩 🕪
        button1.setOnClickListener(
          new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
              sysUtil.launchUrl(v.getContext(), MainActivity.this.streamUrls.get(spinner1.getSelectedItem().toString()));
            }
          }
        );

      android.widget.Button button2 = (android.widget.Button)findViewById(R.id.play);
        button2.setAllCaps(false);
        button2.setText("▶"); // ▶ ⏹ 🔈 🔉 🔊 🕨 🕩 🕪
        button2.setOnClickListener(
          new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
              try {
                String selectedItem = spinner1.getSelectedItem().toString();
                android.content.Intent playbackAction
                 = new android.content.Intent("com.quoinsight.minimal.myAudioServicePlayAction");
                  playbackAction.setPackage(MainActivity.this.getPackageName());
                   playbackAction.putExtra("name", selectedItem);
                   playbackAction.putExtra("url", MainActivity.this.streamUrls.get(selectedItem));
                startService(playbackAction);
              } catch(Exception e) {
                commonGui.writeMessage(MainActivity.this, "MainActivity.playbackAction", e.getMessage());
              }
            }
          }
        );

      android.widget.Button button3 = (android.widget.Button)findViewById(R.id.stop);
        button3.setAllCaps(false);
        button3.setText("⏹"); // ▶ ⏹ 🔈 🔉 🔊 🕨 🕩 🕪
        button3.setOnClickListener(
          new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
              try {
                android.content.Intent playbackStopAction
                 = new android.content.Intent("com.quoinsight.minimal.myAudioServiceStopAction");
                  playbackStopAction.setPackage(MainActivity.this.getPackageName());
                MainActivity.this.startService(playbackStopAction);
              } catch(Exception e) {
                commonGui.writeMessage(MainActivity.this, "MainActivity.playbackStopAction", e.getMessage());
              }
            }
          }
        );

    ////////////////////////////////////////////////////////////////////

    //android.widget.LinearLayout.LayoutParams p = button2.getLayoutParams();
    //p.width = (int) commonGui.dp2px(commonGui.getResourceDisplayMetrics(this), 30);
    //button2.setLayoutParams(p);

  }

  @Override protected void onResume() {
    super.onResume();
    txt2.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 72);
    txt2.setText("🔋" + Integer.toString(sysUtil.getBatteryLevel(this)) + "%");
  }

}
