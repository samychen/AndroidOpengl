package com.cam001.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.cam001.photoeditor.R;
import com.cam001.util.ToastUtil;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class QQTool {
	private static final String TAG = "QQTool";
	private Context mContext;
	public String mFilePath = null;
	private Tencent mTencent = null;
	private Handler mHandler = new Handler();
	
	public interface QQConfig {
		static final String APP_ID = "1104311928";
		static final String APP_SECRET = "bGMzOnwJts8RCrLS";
	}
	public void Upload(final String filePath) {
		mFilePath = filePath;
		final Bundle bundle = new Bundle();
		bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, filePath);
		bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME,mContext.getResources()
				.getString(R.string.app_name));
		bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
				QQShare.SHARE_TO_QQ_TYPE_IMAGE);
		bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, 0x00);
		new Thread(new Runnable() {
			@Override
			public void run() {
				mTencent.shareToQQ((Activity) mContext, bundle,
						new IUiListener() {

							@Override
							public void onCancel() {
								Log.d("doSendtoQQ", "onCancel=");
							}

							@Override
							public void onComplete(Object arg0) {
								Log.d("doSendtoQQ", "onComplete=");
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										ToastUtil.showShortToast(mContext,
												R.string.share_status_success);
									}
								});
							}

							@Override
							public void onError(UiError arg0) {
								Log.d("doSendtoQQ", "onError=" + arg0.errorCode
										+ ", " + arg0.errorMessage);
								if (arg0.errorCode == -6){
//									showShareErr(arg0.errorMessage);
								}
								
							}
						});
			}
		}).start();
	}

	public boolean isEmpty() {
		return false;
	}

	public QQTool(Context mContext) {
		super();
		this.mContext = mContext;
		mTencent = Tencent.createInstance(QQConfig.APP_ID,
				mContext);
	}

	private static QQTool mQQTool = null;

	public static QQTool getInstance(Context mContext) {
		if (mQQTool == null) {
			mQQTool = new QQTool(mContext);
		}
		return mQQTool;
	}
	
	public static Location getLocation(Context context) {
		Location location = null;
		LocationManager loctionManager = null;
		loctionManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		location = loctionManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		return location;
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (null != mTencent)
			mTencent.onActivityResult(requestCode, resultCode, data);
	}
}
