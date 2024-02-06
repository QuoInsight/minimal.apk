package com.quoinsight.minimal;

public class myContentObserver extends android.database.ContentObserver {

  public interface handlers {
    void OnChange(boolean selfChange, android.net.Uri uri);
  }
  private handlers gHandlers = null;
  public void setHandlers(handlers h) {
    gHandlers = h;
  }

  // [ https://stackoverflow.com/questions/6896746/is-there-a-broadcast-action-for-volume-changes/17398781 ]
  public android.content.Context parentContext = null;
  public myContentObserver(android.content.Context context, android.os.Handler handler) {
    super(handler);
    parentContext = context;
  }

  public myContentObserver(android.os.Handler handler) {
    super(handler);
  }

  @Override public boolean deliverSelfNotifications() {
    return super.deliverSelfNotifications(); 
  }
  // If a Handler was supplied to the ContentObserver constructor,
  // then a call to the onChange(boolean) method is posted to the handler's message queue.
  // Otherwise, the onChange(boolean) method is invoked immediately on this thread. 
  @Override public void onChange(boolean selfChange, android.net.Uri uri) {
    super.onChange(selfChange);
    // [ content://settings/system/volume_music_speaker ]
    if (gHandlers != null) gHandlers.OnChange(selfChange, uri);
    //commonGui.writeMessage(parentContext, "myContentObserver.onChange", "Settings change detected");
  }

  public void register(android.net.Uri uri) {
    try {
      this.parentContext.getApplicationContext().getContentResolver().registerContentObserver(
        (uri==null) ? android.provider.Settings.System.CONTENT_URI : uri, true, this
      );
    } catch(Exception e) {}
  }

  public void unregister() {
    try {
      this.parentContext.getApplicationContext().getContentResolver().unregisterContentObserver(this);
    } catch(Exception e) {}
  }

}
