package com.quoinsight.minimal;

/*
  https://medium.com/@lewisjkl/android-httpurlconnection-with-asynctask-tutorial-7ce5bf0245cd
  https://www.smashingmagazine.com/2017/03/simplify-android-networking-volley-http-library/

  https://techblogs.42gears.com/replacement-of-deprecated-asynctask-in-android/
  https://stackoverflow.com/questions/76679399/passing-or-declaring-variable-of-asyncexecutorservice-in-right-way
  https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
*/

public class myAsyncTask {

  private final java.util.concurrent.ExecutorService executor
    = java.util.concurrent.Executors.newSingleThreadExecutor();
  private final android.os.Handler resultHandler 
    = new android.os.Handler(android.os.Looper.getMainLooper());

  public String returnVal = "";
		
  public void onComplete(String result) {
  }

  public myAsyncTask(android.content.Context parentContext) {
    //Relevant Context should be provided to newly created components (whether application context or activity context)
    //getApplicationContext() - Returns the context for all activities running in application 
    //mContext = parentContext.getApplicationContext();
  }

  public void execute(
   final String p_action, final String p_url
  ) {
    executor.execute(new Runnable() {
      @Override public void run() {
        //Background work here
        switch ( p_action ) {
          case "getIcyMetaData":
            try {
              java.util.HashMap<String, String> icyMetaData = commonUtil.getIcyMetaData(p_url);
              if ( icyMetaData.get("icy-metaint")==null ) {
                returnVal = "ERROR: ICY metadata not supported.";
              } else {
                String streamTitle = icyMetaData.get("StreamTitle");  if (streamTitle!=null && streamTitle.length()>0) returnVal = streamTitle; 
                String icyName = icyMetaData.get("icy-name");  if (icyName!=null && icyName.length()>0) returnVal = "[" + icyName + "] " + returnVal;
              }
            } catch (Exception e) {
              returnVal = "ERROR: " + e.getMessage();
            }
            break;
          case "getMediaUrl":
            returnVal = commonUtil.getMediaUrl2(p_url);
            break;
          case "wget":
            java.util.List lines = commonUtil.wgets(p_url);
            for (int i=0; i<lines.size(); i++)
             returnVal+=lines.get(i).toString().trim();
            break;
          default:
            String url = p_action;
            java.net.HttpURLConnection urlConn;
            try {
              //Open a new URL connection 
              urlConn = (java.net.HttpURLConnection) (new java.net.URL(url)).openConnection();
              urlConn.setConnectTimeout(3000); urlConn.setReadTimeout(5000);
              urlConn.setRequestMethod("GET");
              // sets request headers 
              //urlConn.setRequestProperty("Content-Type", "application/json");
              //urlConn.setRequestProperty("Accept", "application/json");
              returnVal += "[HTTP " + String.valueOf(urlConn.getResponseCode()) + "]";
              java.io.InputStream in = null;
              try {
                in = urlConn.getInputStream();
              } catch (Exception e) {
                returnVal += "\n" + "ERROR: " + e.getMessage() + "; url=" + urlConn.getURL();
				return;
              }
              if (in != null) {
                try {
                  java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(
                    new java.io.BufferedInputStream(in), java.nio.charset.StandardCharsets.UTF_8
                  ));
                  String line=null;  while ((line=r.readLine())!=null) returnVal += "\n" + line;
                } catch (Exception e) {
                  returnVal += "\n" + "ERROR: " + e.getMessage() + "; url=" + urlConn.getURL();
                } finally {
                  urlConn.disconnect();
                }
              }
            } catch (java.io.IOException e) {
              e.printStackTrace();
            }
        } // switch
        resultHandler.post(new Runnable() {
          @Override public void run() {
            onComplete(returnVal);
          }
        });
		
      } // run
    }); // executor.execute
  } // doInBackground

}
