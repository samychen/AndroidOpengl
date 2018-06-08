package com.cam001.service;

import java.util.HashMap;
import android.util.Log;

public class LogUtil {
	
	public static boolean DEBUG = false;
	
	private static HashMap<String, Long> mMap = null;
	
	static {
		DEBUG = true;
		if(DEBUG) {
			mMap = new HashMap<String, Long>();
		}
	}
	
	public static void logV(String tag, String msg, Object...args) {
		if(!DEBUG) {
			return;
		}
		if(args!=null && args.length>0) {
			msg = String.format(msg, args);
		}
		Log.v(tag, "zhl "+msg);
	}
	
	public static void logE(String tag, String msg, Object...args) {
		if(!DEBUG) {
			return;
		}
		if(args!=null) {
			msg = String.format(msg, args);
		}
		Log.e(tag, "zhl "+msg);
	}
	
	public static void logE(String tag, String msg, Exception e) {
		if(e!=null) {
			msg = msg+e.getMessage();
		}
		logE(tag, msg);
	}
	
	public static void logE(String tag, Exception e) {
		String msg = e.getMessage();
		logE(tag, msg);
	}
	
	public static void startLogTime(String tag) {
		if(!DEBUG) {
			return;
		}
		if(mMap.containsKey(tag)) {
//			throw new RuntimeException("startLogTime error. Tag "+tag+" already start.");
			return;
		}
		long time = System.currentTimeMillis();
		mMap.put(tag, time);
	}
	
	public static void stopLogTime(String tag) {
		if(!DEBUG) {
			return;
		}
		if(!mMap.containsKey(tag)) {
//			throw new RuntimeException("stopLogTime error. Tag "+tag+" not started.");
			return;
		}
		long time = System.currentTimeMillis();
		long startTime = mMap.get(tag);
		mMap.remove(tag);
		Log.v("Performance", tag+" cost "+(time-startTime)+"ms");
	}
	
}
