package com.quoinsight.minimal;
/*
  In many apps, there's no need to work with an application class directly.
  If we do want a custom application class, we start by creating a new class
  which extends android.app.Application
*/
public class myApp extends android.app.Application {
	@Override
	public void onCreate() {
      super.onCreate();
      new UCEHandler.Builder(this).build(); // https://github.com/RohitSurwase/UCE-Handler
	}
	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	}
	@Override
	public void onLowMemory() {
	    super.onLowMemory();
	}
}