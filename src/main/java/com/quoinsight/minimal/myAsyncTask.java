package com.quoinsight.minimal;

/*
  https://medium.com/@lewisjkl/android-httpurlconnection-with-asynctask-tutorial-7ce5bf0245cd
  https://www.smashingmagazine.com/2017/03/simplify-android-networking-volley-http-library/
*/

public class myAsyncTask extends android.os.AsyncTask<String, Void, String> {

  public interface handlers {
    void onPostExecute(String returnVal);
  }
  private handlers gHandlers = null;
  public void setHandlers(handlers h) {
    gHandlers = h;
  }
 
  private android.content.Context mContext;

  public myAsyncTask(android.content.Context parentContext) {
    //Relevant Context should be provided to newly created components (whether application context or activity context)
    //getApplicationContext() - Returns the context for all activities running in application 
    mContext = parentContext.getApplicationContext();
  }
 
  //Execute this before the request is made 
  @Override protected void onPreExecute() {
    // A toast provides simple feedback about an operation as popup. 
    // It takes the application Context, the text message, and the duration for the toast as arguments
    //android.widget.Toast.makeText(mContext, "Going for the network call..", android.widget.Toast.LENGTH_LONG).show();
  }
 
  //Perform the request in background 
  @Override protected String doInBackground(String... params) {
    String returnVal = "";
    switch ( params[0] ) {
      case "getIcyMetaData":
        try {
          java.util.HashMap<String, String> icyMetaData = commonUtil.getIcyMetaData(params[1]);
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
        returnVal = commonUtil.getMediaUrl2(params[1]);
        break;
      case "wget":
        java.util.List lines = commonUtil.wgets(params[1]);
        for (int i=0; i<lines.size(); i++)
         returnVal+=lines.get(i).toString().trim();
        break;
      default:
        String url = params[0];
        java.net.HttpURLConnection urlConn;
        try {
          //Open a new URL connection 
          urlConn = (java.net.HttpURLConnection) (new java.net.URL(url)).openConnection();
          urlConn.setRequestMethod("GET");
          // sets request headers 
          //urlConn.setRequestProperty("Content-Type", "application/json");
          //urlConn.setRequestProperty("Accept", "application/json");
          returnVal += "[HTTP " + String.valueOf(urlConn.getResponseCode()) + "]";
          java.io.InputStream in;
          try {
            in = urlConn.getInputStream();
          } catch (Exception e) {
            returnVal += "\n" + "ERROR: " + e.getMessage() + "; url=" + urlConn.getURL();
            return returnVal;
          }
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
        } catch (java.io.IOException e) {
          e.printStackTrace();
        }
    }
    return returnVal;
  }
 
  //Run this once the background task returns. 
  @Override protected void onPostExecute(String returnVal) {
    //Print the response code as toast popup 
    //android.widget.Toast.makeText(
    //  mContext, "[myAsyncTask.onPostExecute] Response: " + returnVal,
    //  android.widget.Toast.LENGTH_LONG
    //).show();
    if (gHandlers != null) gHandlers.onPostExecute(returnVal);
  }
}
