package com.cam001.photoeditor;

import com.umeng.update.UpdateResponse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

public class AppConfig {

	public static final String SP_KEY_PINCH_DRAG_HELP = "pinch_drag_help";
	public static final String SP_KEY_DEBLEMISH_HELP = "deblemish_help";
	public static final String RES_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	private static final String SP_NAME = "config_pref";
	private static AppConfig mAppConfig = null;
	public BeautyActivity mActivity = null;
	public boolean isUpdate=true;

	public static AppConfig getInstance() {
		if (mAppConfig == null) {
			mAppConfig = new AppConfig();
		}
		return mAppConfig;
	}
	
	public UpdateResponse mUpdateResponeInfo = null;
	public int screenWidth;
	public int screenHeight;
	public Context appContext = null;
	private SharedPreferences mPref = null;
	private String versionName = null;
	private int versionCode = -1;
    public Uri mCurrUri;
    public View mMainView;
	private AppConfig() {

	}

	public boolean isFirstLoadAfterUpdate(String key) {
		boolean bFirstLoad = false;
		if (mPref == null) {
			mPref = appContext.getSharedPreferences(SP_NAME, 0);
		}
		int currenVersion = mPref.getInt(key, 0);

		if (getVersionCode() != currenVersion) {
			Editor editor = mPref.edit();
			editor.putInt(key, getVersionCode());
			editor.commit();
			bFirstLoad = true;
		}

		return bFirstLoad;
	}

	public int getVersionCode() {
		getVersionInfo();
		return versionCode;
	}

	private void getVersionInfo() {
		PackageManager packageManager = appContext.getPackageManager();
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(
					appContext.getPackageName(), 0);
			versionCode = packInfo.versionCode;
			versionName = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
