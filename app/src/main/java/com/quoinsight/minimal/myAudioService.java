package com.quoinsight.minimal;

/*
  https://developer.android.com/guide/topics/media/mediaplayer
  <uses-permission android:name="android.permission.INTERNET" /><!--for MediaPlayer streaming-->
  RadioService.java C:\Data\adm\mobile\Android\apk\_src\radio-player\app\src\main\java\com\mcakir\radio\player

  https://sourcegraph.com/github.com/google/ExoPlayer/-/blob/RELEASENOTES.md
*/

public class myAudioService extends android.app.Service
  implements android.media.AudioManager.OnAudioFocusChangeListener,
    android.media.MediaPlayer.OnPreparedListener,
      android.media.MediaPlayer.OnErrorListener
{

  public String mName = "",  mUrl = "http://stm.rthk.hk:80/radio1",  mUrl2 = mUrl;
  // http://live4.tdm.com.mo:1935/live/_definst_/rch2.live/playlist.m3u8
  // https://aifmmobile.secureswiftcontent.com/memorystreams/HLS/rtm-ch020/rtm-ch020-96000.m3u8

  public android.media.MediaPlayer mPlayer = null;
  public android.os.Handler mTimeoutHandler = null;

  public int mWndIdx = -1;
  public String mState = "",  mIcyMetaData = "";
  public androidx.media3.exoplayer.ExoPlayer exoPlayer = null;
  public androidx.media3.exoplayer.source.ConcatenatingMediaSource exoMediaSource = null;

  public String mLastNotification = "";
  //public androidx.media3.session.MediaSession mediaSession;
  //public android.support.v4.app.NotificationManagerCompat notificationManager;
  public androidx.core.app.NotificationManagerCompat notificationManager;

  //////////////////////////////////////////////////////////////////////

  public myAudioService() {
    // constructor

    // below is consider as an unsafe implementation of TrustManager - in violation of the Device and Network Abuse policy
    // commonUtil.disableSSLCertValidation();  // support https://streams.pacifica.org:9000/kpfa 
  }

  //////////////////////////////////////////////////////////////////////

  public void writeMessage(String tag, String msg, String...args) {  // varargs
    android.widget.Toast toast = android.widget.Toast.makeText(
      myAudioService.this, tag + ": " +  msg, android.widget.Toast.LENGTH_LONG
    ); toast.setGravity(android.view.Gravity.CENTER, 0, 0); toast.show();
    // toast.setDuration(int duration);
    // android.util.Log.e(tag, msg);
    return;
  }

  //////////////////////////////////////////////////////////////////////

  public void submitForegroundNotification(int ntfnID, String sbj, String msg, String state) {
    String thisNotification = String.valueOf(ntfnID) + "|" + sbj + "|" + msg;
    if ( thisNotification.equals(myAudioService.this.mLastNotification) ) {
      return;
    } else {
      myAudioService.this.mLastNotification = thisNotification;
    }

    // max text length in Notification.MediaStyle CompactView : 40 characters ?? 
    if (sbj.length()>25 || msg.length()>25) this.writeMessage("ntfnID#"+String.valueOf(ntfnID), sbj+" "+msg);

    /*
      !! under the latest Android versions, the notification will be quitely ignored and not showing up unless the following !!
      !! is included in AndroidManifest.xml : <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>        !!
      !! and checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) is explicitely triggered as well            !!
    */
    boolean allowed = sysUtil.getPermission(MainActivity.getInstance(), android.Manifest.permission.POST_NOTIFICATIONS);

    androidx.core.app.NotificationCompat.Builder builder
      = commonGui.createNotificationBuilder(this, "QuoInsight#ChannelID", "QuoInsight#Channel", "QuoInsight.Minimal");
    builder.setSmallIcon(android.R.drawable.stat_sys_headset) // this is the only user-visible content that's required.
      .setContentTitle(sbj)
      .setContentText(msg) // body text
      .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
      .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC) // android.app.Notification.VISIBILITY_PUBLIC
      .setOngoing(true)
	  .setContentIntent( // default action when the user taps the notification
        android.app.PendingIntent.getActivity(
          this, 0, new android.content.Intent(this, MainActivity.class),
		  android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
      );
    if ( state.equals("") || state.equals("IDLE") || state.equals("ENDED")  || state.equals("STOPPED") ) {
      builder.addAction(
        android.R.drawable.ic_media_play, "play", sysUtil.getPendingService(
          this, myAudioService.class, "com.quoinsight.minimal.myAudioServicePlayAction", 1
        ) // sysUtil.getPendingActivity(this, OtherActivity.class)
      );
    } else {
      builder.addAction(
        android.R.drawable.ic_media_pause, "stop", sysUtil.getPendingService(
          this, myAudioService.class, "com.quoinsight.minimal.myAudioServiceStopAction", 3
        ) // sysUtil.getPendingActivity(this, CompassActivity.class)
      );
    }
    android.app.PendingIntent pendingQuitIntent
     = sysUtil.getPendingService(this, myAudioService.class, "com.quoinsight.minimal.myAudioServiceQuitAction", 3);
    builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
      .setShowActionsInCompactView(0) // (0,1,2)
      .setShowCancelButton(true) // not compatible with latest version
      .setCancelButtonIntent(pendingQuitIntent)
    ).setAutoCancel(false);

    commonGui.cancelNotification(this, ntfnID);
    //commonGui.submitNotification(this, builder, ntfnID); // this will not show in lockscreen regardless of the options
    myAudioService.this.startForeground(ntfnID, builder.build()); // this shows in lockscreen correctly

  }

  //////////////////////////////////////////////////////////////////////

  public androidx.media3.exoplayer.source.MediaSource getExoMediaSource(android.net.Uri audioUri) {
    // androidx.media3.exoplayer.source.MediaSource exoMediaSource = null;
    androidx.media3.datasource.DataSource.Factory dataSourceFactory = null;

    String uriPath = audioUri.getPath().toLowerCase();;
    if ( uriPath.endsWith(".m3u")||uriPath.endsWith(".m3u8") ) {
      dataSourceFactory = new androidx.media3.datasource.DefaultDataSource.Factory(this);
      return new androidx.media3.exoplayer.hls.HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
        androidx.media3.common.MediaItem.fromUri(audioUri)
      );
    }

    String uriScheme = audioUri.getScheme();
	if ( uriScheme.equals("http") || uriScheme.equals("https") ) {
      dataSourceFactory = new androidx.media3.datasource.DefaultHttpDataSource.Factory();
      //  "QuoInsight/1.0", null, 3000, 5000, true // allowCrossProtocolRedirects=true support https://stream.rcs.revma.com/55tyxsy4qtzuv?1589079394
    } else if ( uriScheme.equals("file") || uriScheme.equals("content") ) {
      dataSourceFactory = new androidx.media3.datasource.FileDataSource.Factory();
    }
    return new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(
     dataSourceFactory, new androidx.media3.extractor.DefaultExtractorsFactory()
    ).createMediaSource(
	 androidx.media3.common.MediaItem.fromUri(audioUri)
	);
  }

  //public androidx.media3.exoplayer.source.MediaSource getExoProgressiveMediaSource(android.net.Uri audioUri) {
  //  return new androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(
  //    new androidx.media3.datasource.DefaultHttpDataSourceFactory("QuoInsight/1.0")
  //  ).createMediaSource(audioUri);
  //}

  public void loadExoPlayerMediaSource(androidx.media3.exoplayer.ExoPlayer exoPlayer, android.net.Uri audioUri) {
    //androidx.media3.exoplayer.source.MediaSource audioSource = getExoMediaSource(audioUri);  // audioUri.toString()
    //exoPlayer.prepare(audioSource); // prepareAsync() not applicable for exoPlayer
 
    this.exoMediaSource = new androidx.media3.exoplayer.source.ConcatenatingMediaSource();
    this.exoMediaSource.addMediaSource( getExoMediaSource(audioUri) );
    myAudioService.this.mUrl2 = audioUri.toString();

    exoPlayer.prepare(this.exoMediaSource);
    exoPlayer.setPlayWhenReady(true);
  }

  public void concatExoPlayerMediaSource(androidx.media3.exoplayer.ExoPlayer exoPlayer, android.net.Uri audioUri) {
    // 2.8.0 (2018-05-03)
    // Merged DynamicConcatenatingMediaSource into ConcatenatingMediaSource and deprecated DynamicConcatenatingMediaSource.
    myAudioService.this.exoMediaSource.addMediaSource(
      getExoMediaSource(audioUri)
    );
    myAudioService.this.mUrl2 = audioUri.toString();
  }

  public String concatExoPlayerNextMediaSource() {
    String stateInf = "";
    if ( commonUtil.isOtherPlaylistUrl(myAudioService.this.mUrl) && !commonUtil.isOtherPlaylistUrl(myAudioService.this.mUrl2) ) {
      // just add next track here
      stateInf += ":mUrl2=" + myAudioService.this.mUrl2;
      String nextUrl = commonUtil.getNextUrl(myAudioService.this.mUrl2);
      if ( !nextUrl.equals(myAudioService.this.mUrl2) ) {
        try {
          stateInf += ":concatExoPlayerMediaSource:" + nextUrl;
          concatExoPlayerMediaSource(exoPlayer, android.net.Uri.parse(nextUrl));
          myAudioService.this.mUrl2 = nextUrl;
          stateInf += ":added";
        } catch(Exception e) {
          commonGui.writeMessage(myAudioService.this, "concatExoPlayerMediaSource: "+myAudioService.this.mUrl2, e.getMessage() );
        }
      }
    }
    return stateInf;
  }

  //////////////////////////////////////////////////////////////////////

  public void onTimeout(final int timeout) {
    /*
      !! Java integer literals default to int and not long !!
      !! if it is declared as final long timeout, and not invoked with (long)1234
      !! it will end up crashing with NoSuchMethodError !!
    */
    try {
      if ( this.mTimeoutHandler != null ) {
        this.mTimeoutHandler.removeCallbacksAndMessages(null);
      }
      this.mTimeoutHandler = new android.os.Handler();
        this.mTimeoutHandler.postDelayed(new Runnable(){@Override public void run(){ // !! must use this to avoid issue for UI thread !!
          try {
            myAudioService.this.loadIcyMetaData();
          } catch(Exception e) { 
            writeMessage("myAudioService.onTimeout", e.getMessage());
          }
          onTimeout(timeout); // schedule next instance
        }}, timeout);
    } catch(Exception e) {
      commonGui.writeMessage(myAudioService.this, "myAudioService.onTimeout()", "ERROR: " + e.getMessage());
    }
    return;
  }

  //////////////////////////////////////////////////////////////////////

  public void loadIcyMetaData() {
    try {
      if ( myAudioService.this.mIcyMetaData!=null
        && myAudioService.this.mIcyMetaData.startsWith("ERROR: ICY metadata not supported")
      ) return;
      myAsyncTask asyncTask = new myAsyncTask(this) {
        @Override public void onComplete(String returnVal) {
            //myAudioService.this.writeMessage("getIcyMetaData", returnVal);
            if ( returnVal==null || returnVal.length()==0 ) {
              return; // skip
            } else if ( returnVal.startsWith("ERROR: ICY metadata not supported") ) {
              myAudioService.this.mIcyMetaData = returnVal;
              if ( myAudioService.this.mTimeoutHandler != null ) {
                myAudioService.this.mTimeoutHandler.removeCallbacksAndMessages(null);
              }
              return;
            } else if ( returnVal.startsWith("ERROR:") ) {
              return; // skip | ignore error
            } else {
              myAudioService.this.mIcyMetaData = returnVal;
              myAudioService.this.submitForegroundNotification(1001, myAudioService.this.mName, returnVal, myAudioService.this.mState);
            }
        } // onComplete
	  };
      asyncTask.execute("getIcyMetaData", myAudioService.this.mUrl2);
    } catch(Exception e) {
      commonGui.writeMessage(myAudioService.this, "myAudioService.loadIcyMetaData()", "ERROR: " + e.getMessage());
    }
  }

  //////////////////////////////////////////////////////////////////////

  public androidx.media3.common.Player.Listener newExoEventListener() {
    return new androidx.media3.common.Player.Listener() {

      @Override public void onPositionDiscontinuity(int reason) {
        // this is never triggered for 988
if (true) return;
        String s_reason = "";  switch (reason) {
          case androidx.media3.common.Player.DISCONTINUITY_REASON_AUTO_TRANSITION :
            // Discontinuity to or from an ad within one period in the timeline.
            // Automatic playback transition from one period in the timeline to the next.
            s_reason = "AUTO_TRANSITION";     break;
          case androidx.media3.common.Player.DISCONTINUITY_REASON_INTERNAL :
            // Discontinuity introduced internally by the source.
            s_reason = "INTERNAL";  break;
          case androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK :
            // Seek within the current period or to another period.
            s_reason = "SEEK";     break;
          case androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT :
            // Seek adjustment due to being unable to seek to the requested position or because the seek was permitted to be inexact.
            s_reason = "SEEK_ADJUSTMENT";     break;
          default :
            s_reason = String.valueOf(reason);
        }
        myAudioService.this.loadIcyMetaData();
        String stateInf = s_reason;
        try {
          // onPositionDiscontinuity(PERIOD_TRANSITION) is called every time the window or period index changed
          // so below is guaranteed to contain the new index ??
          int currWndIdx = exoPlayer.getCurrentWindowIndex();
          stateInf += "; getCurrentWindowIndex:" + String.valueOf(myAudioService.this.mWndIdx) + " --> " + String.valueOf(currWndIdx);
          // typically the first trigger during a session is under PERIOD_TRANSITION with getCurrentWindowIndex: -1 --> 1

          if ( myAudioService.this.mWndIdx!=currWndIdx ) {
            myAudioService.this.mWndIdx = currWndIdx;
            if ( commonUtil.isOtherPlaylistUrl(myAudioService.this.mUrl) && !commonUtil.isOtherPlaylistUrl(myAudioService.this.mUrl2) ) {
              String concatInf = myAudioService.this.concatExoPlayerNextMediaSource(); // just add next track here
              // stateInf += "; " + concatInf;
            }
          }
          //commonGui.writeMessage(myAudioService.this, "onPositionDiscontinuity", stateInf);
        } catch(Exception e) {
          commonGui.writeMessage(myAudioService.this, "onPositionDiscontinuity", "ERROR: " + e.getMessage() + "; " + stateInf);
        }
      }

    /*
      @Override public void onTimelineChanged(androidx.media3.exoplayer.Timeline timeline, Object manifest, int reason) {
        // this is only triggered for one time as PREPARED during the initial stage of a session for 988
        String s_reason = "";  switch (reason) {
          case androidx.media3.exoplayer.Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED :
            // Timeline and manifest changed as a result of a player initialization with new media.
            // typically, this is closely linked to onPlayerStateChanged(STATE_BUFFERING-->STATE_READY)
            s_reason = "PLAYLIST_CHANGED";  break;
            s_reason = "RESET";     break;
          default :
            s_reason = String.valueOf(reason);
        }
        // commonGui.writeMessage(myAudioService.this, "onTimelineChanged", s_reason);
      }
    */

      @Override public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}
      @Override public void onLoadingChanged(boolean isLoading) {}
      //@Override public void onSeekProcessed() {}

      @Override public void onPlaybackParametersChanged(androidx.media3.common.PlaybackParameters playbackParameters) {}
      @Override public void onRepeatModeChanged(int repeatMode) {}

    /*
      @Override public void onTracksChanged(androidx.media3.exoplayer.source.TrackGroupArray trackGroups, androidx.media3.exoplayer.trackselection.TrackSelectionArray trackSelections) {
        // this is triggered initially but not when song changes on 988
        try {
          boolean txtInfFound = false;
          String metaDataString = "";
          for ( int i = 0; i < trackGroups.length; i++ ) {
            androidx.media3.exoplayer.source.TrackGroup trackGroup = trackGroups.get(i);
            for ( int j = 0; j < trackGroup.length; j++ ) {
              androidx.media3.exoplayer.Format trackFormat = trackGroup.getFormat(j);
              metaDataString += " | id:" + trackFormat.id;
              if (trackFormat.language!=null) metaDataString+="; language:"+trackFormat.language;
              androidx.media3.extractor.metadata.Metadata trackMetadata = trackFormat.metadata;
              if ( trackMetadata == null ) continue;
              for (int k = 0; k < trackMetadata.length(); k++) {
                androidx.media3.extractor.metadata.Metadata.Entry entry = trackMetadata.get(k);
                if ( entry instanceof androidx.media3.extractor.metadata.id3.TextInformationFrame ) {
                  // http://id3.org/id3v2.4.0-frames
                  androidx.media3.extractor.metadata.id3.TextInformationFrame txtInf = (androidx.media3.extractor.metadata.id3.TextInformationFrame) entry;
                  if (txtInf.value!=null) {
                    txtInfFound = true;
                    String eName="";  switch(txtInf.id) {
                      case "TALB": eName="album"; break;
                      case "TIT2": eName="title"; break;
                      case "TPE1": eName="artist"; break;
                      default : eName=txtInf.id.toString();
                    }
                    metaDataString += eName + ":" + txtInf.value + ":" + txtInf.description + " | ";
                  }
                } else metaDataString += entry.toString() + " | ";
              }
            }
          }
          //    [ | id:null ]    [ | id:0 | id:1 ]    [ | id:1/256 | id:1/257 ] 
          if (txtInfFound) commonGui.writeMessage(myAudioService.this, "exoPlayer.onTracksChanged", "metaData: " + metaDataString);
        } catch (Exception e) {
          commonGui.writeMessage(myAudioService.this, "exoPlayer.onTracksChanged", "ERROR: " + e.getMessage());
        }
      }
    */

      //@Override public void onIsPlayingChanged(boolean isPlaying) { }
        // ?? playback is paused, ended, suppressed, or the player
        // is buffering, stopped or failed. Check player.getPlaybackState,
        // player.getPlayWhenReady, player.getPlaybackError and
        // player.getPlaybackSuppressionReason for details.

      @Override public void onMetadata(androidx.media3.common.Metadata metaData) {
        String metaDataString = "";
		try {
          for (int i=0; i<metaData.length(); i++) {
            // [PRIV: owner=com.apple.streaming.transportStreamTimestamp]
            metaDataString += metaData.get(i).toString() + " | ";
          }
          if ( metaDataString.length()>0 && !metaDataString.equals("PRIV: owner=com.apple.streaming.transportStreamTimestamp | ") ) {
            myAudioService.this.writeMessage("exoPlayer.onMetadata", "metaData: " + metaDataString);
          }
        } catch (Exception e) {
          commonGui.writeMessage(myAudioService.this, "exoPlayer.onMetadata", "ERROR: " + e.getMessage());
        }
      }

      @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        try {
          switch (playbackState) {
            case androidx.media3.exoplayer.ExoPlayer.STATE_IDLE :
              myAudioService.this.mState = "IDLE";       break;
            case androidx.media3.exoplayer.ExoPlayer.STATE_BUFFERING :
              myAudioService.this.mState = "BUFFERING";  break;
            case androidx.media3.exoplayer.ExoPlayer.STATE_READY :
              myAudioService.this.mState = "READY";
              break;
            case androidx.media3.exoplayer.ExoPlayer.STATE_ENDED :
              myAudioService.this.mState = "ENDED";      break;
            default :
              myAudioService.this.mState = String.valueOf(playbackState);
          }

          //commonGui.writeMessage(myAudioService.this, "exoPlayer.EventListener", "playbackState: " + myAudioService.this.mState);
          myAudioService.this.submitForegroundNotification(1001, myAudioService.this.mName, "-"+myAudioService.this.mState+"-", myAudioService.this.mState);

          //int currWndIdx = myAudioService.this.exoPlayer.getCurrentWindowIndex();
          //String mDescr = myAudioService.this.exoPlayer.getMediaDescriptionAtQueuePosition(currWndIdx);
          //myAudioService.this.submitForegroundNotification(1001, mDescr, , "-"+myAudioService.this.mState+"-", myAudioService.this.mState);

          if ( myAudioService.this.mState!=null && myAudioService.this.mState.equals("READY") ) {
//if (true) return; // crashed when continue with the below block, even those are dummy functions only !!
            myAudioService.this.loadIcyMetaData();
            myAudioService.this.onTimeout(6500);
          }
        } catch (Exception e) {
          commonGui.writeMessage(myAudioService.this, "exoPlayer.onPlayerStateChanged", "ERROR: " + e.getMessage());
        }
      }

      @Override public void onPlayerError(androidx.media3.common.PlaybackException error) {
        //if (error.type == androidx.media3.exoplayer.PlaybackException.TYPE_SOURCE) {
          Throwable cause = error.getCause();
          writeMessage(
            "exoPlayer.PlaybackException",
            "[URL]: " + myAudioService.this.mUrl2  + " [ERROR]: " + cause.getMessage()
          );
          /*
           cause.getMessage() ==> "Response code: 302"
            fix: https://github.com/google/ExoPlayer/issues/1190
            passing allowCrossProtocolRedirects=true to DefaultHttpDataSourceFactory()
          */
if (true) return;
          if (cause instanceof androidx.media3.datasource.HttpDataSource.HttpDataSourceException) {
            // An HTTP error occurred.
            androidx.media3.datasource.HttpDataSource.HttpDataSourceException httpError
              = (androidx.media3.datasource.HttpDataSource.HttpDataSourceException) cause;
            // This is the request for which the error occurred.
            androidx.media3.datasource.DataSpec requestDataSpec = httpError.dataSpec;
            // It's possible to find out more about the error both by casting and by
            // querying the cause.
            if (httpError instanceof androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException) {
              // Cast to InvalidResponseCodeException and retrieve the response code,
              // message and headers.
            } else {
              // Try calling httpError.getCause() to retrieve the underlying cause,
              // although note that it may be null.
            }
          }
        //} else { // if (error.type)
        //  commonGui.writeMessage(
        //    myAudioService.this, "exoPlayer.onPlayerError",
        //      "[URL]: " + myAudioService.this.mUrl2  + " [ERROR]: " + error.getMessage()
        //  );
        //} // if (error.type)
      }

    };

  }

  //////////////////////////////////////////////////////////////////////

  public androidx.media3.exoplayer.ExoPlayer startExoPlayer(String s_name, String s_url) {
    String name = s_name.trim(),  url = s_url.trim();

    myAudioService.this.mWndIdx = -1;
    myAudioService.this.mState = "";
    myAudioService.this.mIcyMetaData = "";
    myAudioService.this.mLastNotification = "";
    if ( this.mTimeoutHandler != null ) {
      this.mTimeoutHandler.removeCallbacksAndMessages(null);
    }

    if ( commonUtil.isOtherPlaylistUrl(url) ) {
      // !! must use AsyncTask in Android for any direct HTTP request, else it will throw exception errors !!
      myAsyncTask asyncTask = new myAsyncTask(this) {
        @Override public void onComplete(String returnVal) {
            String url = returnVal;
            if ( !commonUtil.isOtherPlaylistUrl(url) ) {
              myAudioService.this.exoPlayer = myAudioService.this.startExoPlayer(myAudioService.this.mName, url);
              String stateInf = myAudioService.this.concatExoPlayerNextMediaSource();
              // myAudioService.this.writeMessage("concatExoPlayerNextMediaSource", stateInf);
            } else {
              myAudioService.this.writeMessage("Invalid m3u8", returnVal);
            }
        } // onComplete
	  };
      asyncTask.execute("getMediaUrl", url);
      return this.exoPlayer;
    }
    android.net.Uri audioUri = android.net.Uri.parse(url);

    if (this.exoPlayer!=null) {
      try {
        this.exoPlayer.stop();
        this.exoPlayer.release();
        this.exoPlayer = null;
      } catch (Exception e) {}
    }

// writeMessage("url", url);  if (true) return myAudioService.this.exoPlayer;

    this.exoPlayer
     = (new androidx.media3.exoplayer.SimpleExoPlayer.Builder(myAudioService.this)).build();
    // = androidx.media3.exoplayer.ExoPlayerFactory.newSimpleInstance(
    //  this, new androidx.media3.exoplayer.trackselection.DefaultTrackSelector(),
    //  new androidx.media3.exoplayer.DefaultLoadControl()
    //);
    // = androidx.media3.exoplayer.ExoPlayerFactory.newInstance(
    //    this, new androidx.media3.exoplayer.trackselection.DefaultTrackSelector()
    //);

    this.exoPlayer.addListener( newExoEventListener() );

    /*
    this.exoPlayer.addMetadataOutput(
      // https://github.com/m-cakir/radio-player/issues/20
      // https://github.com/googleads/googleads-ima-android-dai/issues/13
      new androidx.media3.extractor.metadata.MetadataOutput() {
        @Override public void onMetadata(androidx.media3.exoplayer.metadata.Metadata metaData) {
          String metaDataString = "";
          for (int i=0; i<metaData.length(); i++) {
            // [PRIV: owner=com.apple.streaming.transportStreamTimestamp]
            metaDataString += metaData.get(i).toString() + " | ";
          }
          if ( metaDataString.length()>0 && !metaDataString.equals("PRIV: owner=com.apple.streaming.transportStreamTimestamp | ") ) {
            myAudioService.this.writeMessage("exoPlayer.onMetadata", "metaData: " + metaDataString);
          }
        }
      }
    );
	*/

    try {
      if ( audioUri.getScheme().equals("file") ) this.exoPlayer.setRepeatMode(androidx.media3.common.Player.REPEAT_MODE_ALL);
      loadExoPlayerMediaSource(exoPlayer, audioUri);
      submitForegroundNotification(1001, name, "starting...", myAudioService.this.mState);
    } catch (Exception e) {
      commonGui.writeMessage(
        myAudioService.this, "startSimpleExoPlayer",
          "[URL]: " + audioUri.toString()  + " [ERROR]: " + e.getMessage()
      );
    }

    // Do something with your TransportControls
    //final TransportControls controls = mediaSession.getController().getTransportControls();
    //((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, noti);

   /*
    builder.setAutoCancel(false)
      //.setLargeIcon(largeIcon)
      .setSmallIcon(android.R.drawable.stat_sys_headset)
      .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
        .setMediaSession(exoPlayer.getMediaSession().getSessionToken()) // myAudioService.this.getMediaSession()
        .setShowActionsInCompactView(0, 1)
        .setShowCancelButton(true)
        .setCancelButtonIntent(stopAction)
      );
   */

   /*
    mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
    mPlayer.setDataSource(url);
    mPlayer.prepareAsync(); // might take long! (for buffering, etc)
   */

    return exoPlayer;
  }

  //////////////////////////////////////////////////////////////////////

  public void initMediaPlayer() {
    // ...initialize the MediaPlayer here...
    mPlayer.setOnErrorListener(this);
  }

  @Override public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
    // ... react appropriately ...
    // The MediaPlayer has moved to the Error state, must be reset!
    return true;
  }

  public int onStartCommand(android.content.Intent intent, int flags, int startId) {
    /*
      !! must define the matching intent/actions in AndroidManifest.xml !!
      <service android:name="myAudioService">
        <intent-filter>
          <action android:name="com.quoinsight.minimal.myAudioServicePlayAction" />
          <action android:name="com.quoinsight.minimal.myAudioServiceStopAction" />
          <action android:name="com.quoinsight.minimal.myAudioServiceQuitAction" />
        </intent-filter>
      </service>
    */

    if ( this.mTimeoutHandler != null ) {
      this.mTimeoutHandler.removeCallbacksAndMessages(null);
    }

    String thisIntentAction = intent.getAction();
    switch(thisIntentAction) {
      case "com.quoinsight.minimal.myAudioServicePlayAction":
        try {
          String name = intent.getStringExtra("name");
           if (name==null||name.length()==0) name=this.mName; else this.mName=name.trim();
          String url = intent.getStringExtra("url");
           if (url==null||url.length()==0) url=this.mUrl; else this.mUrl=url.trim();
          exoPlayer = this.startExoPlayer(name, url);
        } catch (Exception e) {
          commonGui.writeMessage(this, "myAudioServicePlayAction", e.getMessage());
        } finally {
          //mPlayer.release();
          //mPlayer = null;
        }
        break;
      case "com.quoinsight.minimal.myAudioServiceStopAction":
        try {
          exoPlayer.stop();
          // exoPlayer.release();
        } catch (Exception e) {
          commonGui.writeMessage(this, "myAudioServiceStopAction", e.getMessage());
        }
        break;
      case "com.quoinsight.minimal.myAudioServiceQuitAction":
        if ( exoPlayer != null ) {
          try {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
          } catch (Exception e) {
            commonGui.writeMessage(this, "myAudioServiceQuitAction", e.getMessage());
          }
        }
        try {
          this.stopForeground(true);
          commonGui.cancelNotification(this, 1001);
        } catch (Exception e) {
          commonGui.writeMessage(this, "myAudioServiceQuitAction.stopForeground", e.getMessage());
        }
        break;
      default:
        commonGui.writeMessage(this, "myAudioService.onStartCommand", "Invalid intent action: " + thisIntentAction);
    }

    return 0;
  }

  /** Called when MediaPlayer is ready */
  public void onPrepared(android.media.MediaPlayer player) {
    player.start();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    try {
      if ( this.mTimeoutHandler != null ) {
        this.mTimeoutHandler.removeCallbacksAndMessages(null);
      }
      if (mPlayer != null) mPlayer.release();
      if (exoPlayer != null) { 
        exoPlayer.release();
        // exoPlayer.removeListener(this);
      }
    } catch(Exception e) {}
  }

  public class LocalBinder extends android.os.Binder {
    public myAudioService getService() {
      return myAudioService.this;
    }
  }

  //@android.support.annotation.Nullable 
  @Override public android.os.IBinder onBind(android.content.Intent intent) {
    return (new LocalBinder());
  }

  @Override public void onAudioFocusChange(int focusChange) { }

  @Override public void onCreate() {
    super.onCreate();
    //strAppName = getResources().getString(R.string.app_name);
    //audioManager = (android.media.AudioManager) getSystemService(android.content.Context.AUDIO_SERVICE);
    mPlayer = new android.media.MediaPlayer();
    mPlayer.setOnPreparedListener(this);
    /*
    try {
      mediaSession = new android.media.session.MediaSessionCompat(
        this, this.getClass().getSimpleName()
      );
    } catch(Exception e) {
      commonGui.writeMessage(this, "myAudioService.MediaSessionCompat", e.getMessage());
    }
    */
    notificationManager = androidx.core.app.NotificationManagerCompat.from(this);
 }

}

/*
package com.quoinsight.minimal;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.ExoPlayerFactory;
import androidx.media3.exoplayer.PlaybackParameters;
import androidx.media3.exoplayer.Player;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.exoplayer.Timeline;
import androidx.media3.extractor.DefaultExtractorsFactory;
import androidx.media3.exoplayer.source.ExtractorMediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelectionArray;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.exoplayer.upstream.DefaultDataSourceFactory;
import androidx.media3.exoplayer.upstream.DefaultHttpDataSourceFactory;
import androidx.media3.exoplayer.util.Util;
import com.mcakir.radio.R;
import org.greenrobot.eventbus.EventBus;
public class RadioService extends Service implements Player.EventListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY = "com.mcakir.radio.player.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.mcakir.radio.player.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.mcakir.radio.player.ACTION_STOP";
    private final IBinder iBinder = new LocalBinder();
    private Handler handler;
    private final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private SimpleExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private boolean onGoingCall = false;
    private TelephonyManager telephonyManager;
    private WifiManager.WifiLock wifiLock;
    private AudioManager audioManager;
    private MediaNotificationManager notificationManager;
    private String status;
    private String strAppName;
    private String strLiveBroadcast;
    private String streamUrl;
    public class LocalBinder extends Binder {
        public RadioService getService() {
            return RadioService.this;
        }
    }
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pause();
        }
    };
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if(state == TelephonyManager.CALL_STATE_OFFHOOK
                    || state == TelephonyManager.CALL_STATE_RINGING){
                if(!isPlaying()) return;
                onGoingCall = true;
                stop();
            } else if (state == TelephonyManager.CALL_STATE_IDLE){
                if(!onGoingCall) return;
                onGoingCall = false;
                resume();
            }
        }
    };
    private MediaSessionCompat.Callback mediasSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPause() {
            super.onPause();
            pause();
        }
        @Override
        public void onStop() {
            super.onStop();
            stop();
            notificationManager.cancelNotify();
        }
        @Override
        public void onPlay() {
            super.onPlay();
            resume();
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        strAppName = getResources().getString(R.string.app_name);
        strLiveBroadcast = getResources().getString(R.string.live_broadcast);
        onGoingCall = false;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = new MediaNotificationManager(this);
        wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mcScPAmpLock");
        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "...")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, strLiveBroadcast)
                .build());
        mediaSession.setCallback(mediasSessionCallback);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        handler = new Handler();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.addListener(this);
        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        status = PlaybackStatus.IDLE;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(TextUtils.isEmpty(action))
            return START_NOT_STICKY;
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            stop();
            return START_NOT_STICKY;
        }
        if(action.equalsIgnoreCase(ACTION_PLAY)){
            transportControls.play();
        } else if(action.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if(action.equalsIgnoreCase(ACTION_STOP)){
            transportControls.stop();
        }
        return START_NOT_STICKY;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        if(status.equals(PlaybackStatus.IDLE))
            stopSelf();
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(final Intent intent) {
    }
    @Override
    public void onDestroy() {
        pause();
        exoPlayer.release();
        exoPlayer.removeListener(this);
        if(telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        notificationManager.cancelNotify();
        mediaSession.release();
        unregisterReceiver(becomingNoisyReceiver);
        super.onDestroy();
    }
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                exoPlayer.setVolume(0.8f);
                resume();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stop();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (isPlaying())
                    exoPlayer.setVolume(0.1f);
                break;
        }
    }
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                status = PlaybackStatus.LOADING;
                break;
            case Player.STATE_ENDED:
                status = PlaybackStatus.STOPPED;
                break;
            case Player.STATE_IDLE:
                status = PlaybackStatus.IDLE;
                break;
            case Player.STATE_READY:
                status = playWhenReady ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED;
                break;
            default:
                status = PlaybackStatus.IDLE;
                break;
        }
        if(!status.equals(PlaybackStatus.IDLE))
            notificationManager.startNotify(status);
        EventBus.getDefault().post(status);
    }
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }
    @Override
    public void onLoadingChanged(boolean isLoading) {
    }
    @Override
    public void onPlayerError(ExoPlaybackException error) {
        EventBus.getDefault().post(PlaybackStatus.ERROR);
    }
    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }
    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }
    @Override
    public void onPositionDiscontinuity(int reason) {
    }
    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }
    @Override
    public void onSeekProcessed() {
    }
    public void play(String streamUrl) {
        this.streamUrl = streamUrl;
        if (wifiLock != null && !wifiLock.isHeld()) {
            wifiLock.acquire();
        }
//      DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(getUserAgent());
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, getUserAgent(), BANDWIDTH_METER);
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(new DefaultExtractorsFactory())
                .createMediaSource(Uri.parse(streamUrl));
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }
    public void resume() {
        if(streamUrl != null)
            play(streamUrl);
    }
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
        audioManager.abandonAudioFocus(this);
        wifiLockRelease();
    }
    public void stop() {
        exoPlayer.stop();
        audioManager.abandonAudioFocus(this);
        wifiLockRelease();
    }
    public void playOrPause(String url){
        if(streamUrl != null && streamUrl.equals(url)){
            if(!isPlaying()){
                play(streamUrl);
            } else {
                pause();
            }
        } else {
            if(isPlaying()){
                pause();
            }
            play(url);
        }
    }
    public String getStatus(){
        return status;
    }
    public MediaSessionCompat getMediaSession(){
        return mediaSession;
    }
    public boolean isPlaying(){
        return this.status.equals(PlaybackStatus.PLAYING);
    }
    private void wifiLockRelease(){
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }
    private String getUserAgent(){
        return Util.getUserAgent(this, getClass().getSimpleName());
    }
}
*/
