package com.cam001.util;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.cam001.photoeditor.R;

public class Util {

	private static final String TAG = "Util";
	private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;
	public static void initializeScreenBrightness(Window win, ContentResolver resolver) {
		// Overright the brightness settings if it is automatic
		int mode = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
			WindowManager.LayoutParams winParams = win.getAttributes();
			winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
			win.setAttributes(winParams);
		}
	}

    public static void Assert(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }
	
    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }
    
    public static void joinThreadSilently(Thread t) {
    	if(t == null) return;
    	try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public static void closeSilently(ParcelFileDescriptor c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }
    
    public static void dumpToFile(byte[] data, String path) {
    	FileOutputStream os = null;
    	try {
			os = new FileOutputStream(path);
			os.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeSilently(os);
		}
    }
    
    public static void dumpNv21ToJpeg(byte[] nv21, int width, int height, String path) {
    	FileOutputStream os = null;
    	try {
			os = new FileOutputStream(path);
			Rect rect = new Rect(0,0,width,height);
			YuvImage img = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
			img.compressToJpeg(rect, 85, os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeSilently(os);
		}
    }
    
    public static boolean isUriValid(Uri uri, ContentResolver resolver) {
        if (uri == null) return false;

        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            if (pfd == null) {
                Log.e(TAG, "Fail to open URI. URI=" + uri);
                return false;
            }
            pfd.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    
    public static void viewUri(Uri uri, Context context) {
        if (!isUriValid(uri, context.getContentResolver())) {
            Log.e(TAG, "Uri invalid. uri=" + uri);
            return;
        }

    	Intent intent = new Intent();
    	intent.setData(uri);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        try {
        	intent.setAction("com.android.action.REVIEW");
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
            	intent.setAction(Intent.ACTION_VIEW);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "review image fail. uri=" + uri, e);
            }
        }
    }
    
    public static Rect RectFtoRect(RectF r) {
    	return new Rect((int)r.left, (int)r.top, (int)r.right, (int)r.bottom);
    }
	
	private static class BackgroundJob implements Runnable {

		private final Dialog mDialog;
		private final Runnable mJob;
		private final Handler mHandler;
		private final Runnable mCleanupRunner = new Runnable() {
			public void run() {
				if (mDialog.getWindow() != null)
					mDialog.dismiss();
			}
		};

		public BackgroundJob(Activity activity, Runnable job,
				Dialog dialog, Handler handler) {
			mDialog = dialog;
			mJob = job;
			mHandler = handler;
		}

		public void run() {
			try {
				mJob.run();
			} finally {
				mHandler.post(mCleanupRunner);
			}
		}
	}
	
    public static void startBackgroundJob(Activity activity,
            String title, String message, Runnable job, Handler handler) {
        // Make the progress dialog uncancelable, so that we can gurantee
        // the thread will be done before the activity getting destroyed.
    	  Dialog dialog = new Dialog(activity, R.style.Theme_dialog);
          dialog.setContentView(R.layout.camera_panel_progress);
          dialog.setCancelable(false);
          dialog.show();
        new Thread(new BackgroundJob(activity, job, dialog, handler)).start();
    }
    
    public static void goToAppStore(Context context, String appPackage){
		try {
			Uri uri = null;
			uri = Uri.parse("market://details?id=" + appPackage);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
//			Toast.makeText(context,
//					R.string.text_not_installed_market_app,
//					Toast.LENGTH_SHORT).show();
		}
	}
    
    public static boolean isNetworkAvailable(Context mContext) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info[] = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static boolean isAppInstalled(Context context, String packagename) {
		PackageManager pm = context.getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(packagename, 0);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	public static void launchApplication(Context context, String packagename, String lauchAcitivity) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		ComponentName componentName = null;
		componentName = new ComponentName(packagename,lauchAcitivity);
		intent.setComponent(componentName);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static boolean LowMemory(Context context) {
		final ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(info);
		Log.i("bug", "system remain mem:" + Formatter.formatFileSize(context, info.availMem));
		Log.i("bug", "system is in lowmem��" + info.lowMemory);
		Log.i("bug", "low memory when <" + Formatter.formatFileSize(context, info.threshold));
//        Log.i("bug","system remain mem:"+(info.availMem>>10)+"k");
//        Log.i("bug","system is in lowmem��"+info.lowMemory);
//        Log.i("bug","low memory when <"+(info.threshold>>10)+"k");
		return info.lowMemory;
	}
}
