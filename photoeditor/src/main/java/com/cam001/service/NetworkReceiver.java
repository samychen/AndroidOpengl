package com.cam001.service;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context c, Intent i) {
        ConnectivityManager manager = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);  
//        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
//        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if(activeInfo!=null && activeInfo.isConnected()) {
        	if(!isServiceStarted(c)) {
        		Intent intent = new Intent(c, MainService.class);
        		c.startService(intent);
        	}
        }
	}

	public static boolean isServiceStarted(Context c) {
		ActivityManager myManager = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals("com.cam001.service")) {
				return true;
			}
		}
		return false;
	}
	
}
