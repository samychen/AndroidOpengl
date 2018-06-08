package com.cam001.util;

import java.util.HashMap;
import android.util.Log;

import com.cam001.photoeditor.BuildConfig;

public class DebugUtil {
	
	private static HashMap<String, Long> mMap = null;
	
	static {
		if(BuildConfig.DEBUG) {
			mMap = new HashMap<String, Long>();
		}
	}

    public static void ASSERT(boolean b) {
        if(b) return;
        if(BuildConfig.DEBUG) {
            throw new RuntimeException("Assert failed!");
        } else {
            reportError("Assert failed!");
        }
    }

	public static void logV(String tag, String msg, Object...args) {
        if(BuildConfig.DEBUG) {
            if (args != null && args.length > 0) {
                msg = String.format(msg, args);
            }
            Log.v(tag, msg);
        }
	}
	
	public static void logE(String tag, String msg, Object...args) {
            if (args != null) {
                msg = String.format(msg, args);
            }
            Log.e(tag, msg);
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
        if(BuildConfig.DEBUG) {
            if (mMap.containsKey(tag)) {
//			throw new RuntimeException("startLogTime error. Tag "+tag+" already start.");
                return;
            }
            long time = System.currentTimeMillis();
            mMap.put(tag, time);
        }
	}
	
	public static void stopLogTime(String tag) {
        if(BuildConfig.DEBUG) {
            if (!mMap.containsKey(tag)) {
//			throw new RuntimeException("stopLogTime error. Tag "+tag+" not started.");
                return;
            }
            long time = System.currentTimeMillis();
            long startTime = mMap.get(tag);
            mMap.remove(tag);
            Log.v("Performance", tag + " cost " + (time - startTime) + "ms");
        }
	}
	
	public static void reportError(Throwable e) {
        if(BuildConfig.DEBUG) {
            throw new RuntimeException(e);
		} else {
            logE("DebugUtil","reportError",e);
//            MobclickAgent.reportError(AppConfig.getInstance().appContext,
//                    "Catched Exception:"+getStackTrace(e));
		}
	}
	
	public static void reportError(String e) {
        if(BuildConfig.DEBUG) {
            throw new RuntimeException(e);
		} else {
            logE("DebugUtil","reportError",e);
//            MobclickAgent.reportError(AppConfig.getInstance().appContext,
//                    "reportError:"+e);
		}
	}
	
	private static String getStackTrace(Throwable e) {
		StringBuilder trace = new StringBuilder();
		trace.append(e.getMessage());
		for(StackTraceElement elemnt: e.getStackTrace()) {
			trace.append("	at ");
			trace.append(elemnt.getClassName());
			trace.append('.');
			trace.append(elemnt.getMethodName());
			trace.append('(');
			trace.append(elemnt.getFileName());
			trace.append(':');
			trace.append(elemnt.getLineNumber());
			trace.append(')');
			trace.append('\n');
		}
		return trace.toString();
	}
	
}
