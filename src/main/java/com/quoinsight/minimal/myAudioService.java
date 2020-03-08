package com.quoinsight.minimal;

/*
  https://developer.android.com/guide/topics/media/mediaplayer
  <uses-permission android:name="android.permission.INTERNET" /><!--for MediaPlayer streaming-->
  RadioService.java C:\Data\adm\mobile\Android\apk\_src\radio-player\app\src\main\java\com\mcakir\radio\player
*/

public class myAudioService extends android.app.Service
  implements android.media.AudioManager.OnAudioFocusChangeListener,
    android.media.MediaPlayer.OnPreparedListener,
      android.media.MediaPlayer.OnErrorListener
{

  public static android.media.MediaPlayer mPlayer;
  public static com.google.android.exoplayer2.SimpleExoPlayer exoPlayer;
  public static android.support.v4.media.session.MediaSessionCompat mediaSession;
  public static android.support.v4.app.NotificationManagerCompat notificationManager;

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
    if (intent.getAction().equals("com.quoinsight.minimal.myAudioServicePlayAction")) {
      try {
        String url = intent.getStringExtra("url");
         if (url==null || url.length()==0) url = "http://stm.rthk.hk:80/radio1";
        android.net.Uri audioUri = android.net.Uri.parse(url);

        exoPlayer = com.google.android.exoplayer2.ExoPlayerFactory.newSimpleInstance(
          this, new com.google.android.exoplayer2.trackselection.DefaultTrackSelector(new android.os.Handler()),
          new com.google.android.exoplayer2.DefaultLoadControl()
        );
        //exoPlayer.addListener(this);

        com.google.android.exoplayer2.source.MediaSource audioSource
         = new com.google.android.exoplayer2.source.ExtractorMediaSource(
            audioUri, new com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory("QuoInsight/1.0"),
             new com.google.android.exoplayer2.extractor.DefaultExtractorsFactory(), null, null
           );
        exoPlayer.prepare(audioSource); // prepareAsync() not applicable for exoPlayer
        exoPlayer.setPlayWhenReady(true);

          android.support.v4.app.NotificationCompat.Builder builder
           = commonGui.createNotificationBuilder(
               this, "QuoInsight#ChannelID", "QuoInsight#Channel", "QuoInsight.Minimal"
             );

          android.app.PendingIntent pendingStopIntent = sysUtil.getPendingService(
            this, myAudioService.class, "com.quoinsight.minimal.myAudioServiceStopAction", 3
          );

          builder.setSmallIcon(android.R.drawable.stat_sys_headset) // this is the only user-visible content that's required.
            .setContentText("playing...") // body text
            .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_HIGH)
            .setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC) // android.app.Notification.VISIBILITY_PUBLIC
            .setContentIntent( // default action when the user taps the notification
              android.app.PendingIntent.getActivity(
                this, 0, new android.content.Intent(this, MainActivity.class), 0
              )
            )
            .addAction(
              android.R.drawable.ic_media_play, "play",
              // sysUtil.getPendingActivity(this, OtherActivity.class)
              sysUtil.getPendingService(
                this, myAudioService.class, "com.quoinsight.minimal.myAudioServicePlayAction", 1
              )
            )
            .addAction(
              android.R.drawable.ic_media_pause, "stop", pendingStopIntent
              // sysUtil.getPendingActivity(this, CompassActivity.class)
            )
            .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
              .setShowActionsInCompactView(0,1)
            )
            .setAutoCancel(false)
          ;

          commonGui.cancelNotification(this, 1001);
          //commonGui.submitNotification(this, builder, 1001); // this will not show in lockscreen regardless of the options
          myAudioService.this.startForeground(1001, builder.build()); // this shows in lockscreen correctly

        // Do something with your TransportControls
        //final TransportControls controls = mediaSession.getController().getTransportControls();
        //((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1, noti);
    
       /*
        builder.setAutoCancel(false)
          .setContentTitle("playing...")
          .setContentText("QuoInsight.Minimal")
          .setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC)
          //.setLargeIcon(largeIcon)
          //.setContentIntent(pendingIntent)
          .setSmallIcon(android.R.drawable.stat_sys_headset)
          //.addAction(icon, "pause", action)
          .addAction(R.drawable.ic_stop_white, "stop", stopAction)
          .setPriority(android.support.v4.app.NotificationCompat.PRIORITY_HIGH)
          .setWhen(System.currentTimeMillis())
          .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(exoPlayer.getMediaSession().getSessionToken()) // myAudioService.this.getMediaSession()
            .setShowActionsInCompactView(0, 1)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopAction)
          );

        try {
          //myAudioService.this.startForeground(1001, notification);
        } catch(Exception e) {
          commonGui.writeMessage(this, "myAudioService.startForeground", e.getMessage());
        }
       */

       /*
        com.google.android.exoplayer2.PlayerNotificationManager playerNotificationManager
         = com.google.android.exoplayer2.PlayerNotificationManager.createWithNotificationChannel(
            context, "CHANNEL", "QuoInsight.Minimal", 1000,
            new com.google.android.exoplayer2.PlayerNotificationManager.MediaDescriptionAdapter() {
              @Override public String getCurrentContentTitle(com.google.android.exoplayer2.Player player) {
                //return title[player.getCurrentWindowIndex()];
              }
              @Nullable @Override public String getCurrentContentText(
                com.google.android.exoplayer2.Player player
              ) { 
                // return title[player.getCurrentWindowIndex()]; 
              }
              @Nullable @Override public android.app.PendingIntent createCurrentContentIntent(
                com.google.android.exoplayer2.Player player
              ) {
                //Intent intent = new Intent(context,ExoPlayerActivity.class);
                //return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
              }
              @Nullable @Override public android.graphics.Bitmap getCurrentLargeIcon(
                com.google.android.exoplayer2.Player player, com.google.android.exoplayer2.PlayerNotificationManager.BitmapCallback callback
              ) {
                //Resources res = getResources();
                //Bitmap bmp = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
                //return bmp;
              }
            }
           );

        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
          @Override public void onNotificationStarted(int notificationId, Notification notification) { startForeground(notificationId,notification); }
          @Override public void onNotificationCancelled(int notificationId) { stopSelf(); }
        });

        playerNotificationManager.setPlayer(player);
       */

       /*
        mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
        mPlayer.setDataSource(url);
        mPlayer.prepareAsync(); // might take long! (for buffering, etc)
       */

      } catch (Exception e) {
        commonGui.writeMessage(this, "myAudioService.onStartCommand", e.getMessage());
      } finally {
        //mPlayer.release();
        //mPlayer = null;
      }

    } else if (intent.getAction().equals("com.quoinsight.minimal.myAudioServiceStopAction")) {

      exoPlayer.stop();
      // exoPlayer.release();

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
    try {
      mediaSession = new android.support.v4.media.session.MediaSessionCompat(
        this, this.getClass().getSimpleName()
      );
    } catch(Exception e) {
      commonGui.writeMessage(this, "myAudioService.MediaSessionCompat", e.getMessage());
    }
    notificationManager = android.support.v4.app.NotificationManagerCompat.from(this);
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

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
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

//        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(getUserAgent());

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
