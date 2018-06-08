package com.cam001.photoeditor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cam001.service.AdAgent;
import com.cam001.stat.StatApi;
import com.cam001.util.LogUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;

import java.lang.ref.WeakReference;

public class BaseActivity extends Activity {

	private static final String TAG = "BaseActivity";

	private static final int SCREENON_DELAY = 2 * 60 * 1000; // 2min

	public static class BaseHandler extends Handler {

		private WeakReference<BaseActivity> mRefActivity = null;

		public BaseHandler(BaseActivity activity) {
			mRefActivity = new WeakReference<BaseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseActivity activity = mRefActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}

	}

	protected AppConfig mConfig = AppConfig.getInstance();

	public BaseHandler mHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Display disp = getWindowManager().getDefaultDisplay();
		mConfig.screenWidth = disp.getWidth();
		mConfig.screenHeight = disp.getHeight();
		mHandler = new BaseHandler(this);
		if(mConfig.isUpdate && AdAgent.instance().needUpdate()) {
			UmengUpdateAgent.update(this);
		}
	}

	@Override
	protected void onPause() {
		StatApi.onPause(this);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (mUpdateDialog!=null && mUpdateDialog.isShowing()){
			mConfig.isUpdate=false;
		}
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mConfig.isUpdate){
			checkUpdate();
		}
		StatApi.onResume(this);
	}

	private Dialog mUpdateDialog = null;
	private void checkUpdate() {
		Log.d("checkUpdate", "start");
		if (!Util.isNetworkAvailable(mConfig.appContext)) {
			return;
		}
		if (mConfig.mUpdateResponeInfo != null) {
			if (mUpdateDialog == null) {
				if (mConfig.mUpdateResponeInfo.hasUpdate) {
					showUpdateDialog();
				}
			}
			return;
		}

		UmengUpdateAgent.setUpdateOnlyWifi(false);
		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

			@Override
			public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
				if (updateStatus == 0 && updateInfo != null) {
					mConfig.mUpdateResponeInfo = updateInfo;
					if (mConfig.mUpdateResponeInfo != null && mUpdateDialog == null) {
						if (mConfig.mUpdateResponeInfo.hasUpdate) {
							showUpdateDialog();
						}
					}
				}
			}
		});
	}

	private void doUpdate() {
		if (!Util.isNetworkAvailable(mConfig.appContext)) {
			// add alert
			ToastUtil.showShortToast(this,
					R.string.common_network_error);
			return;
		}
		UmengUpdateAgent.startDownload(this,
				mConfig.mUpdateResponeInfo);
	}

	private void showUpdateDialog() {
		mUpdateDialog = new Dialog(this, R.style.Theme_dialog);
		mUpdateDialog.setContentView(R.layout.dialog_update);
		((TextView) mUpdateDialog.findViewById(R.id.update_version))
				.setText(mConfig.mUpdateResponeInfo.version);
		((TextView) mUpdateDialog.findViewById(R.id.update_exitor))
				.setText(mConfig.mUpdateResponeInfo.updateLog);

		mUpdateDialog.findViewById(R.id.confirm_button_cancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						closeDialog(mUpdateDialog);
						mConfig.isUpdate=false;
						if(AdAgent.instance().forceUpdate()) {
							finish();
						}
					}
				});
		mUpdateDialog.findViewById(R.id.confirm_button_confirm).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						closeDialog(mUpdateDialog);
						mConfig.isUpdate=false;
						doUpdate();
					}
				});
		mUpdateDialog.setCanceledOnTouchOutside(false);
		if(!isFinishing()){
			mUpdateDialog.show();
		}
	}

	private void closeDialog(Dialog dialog) {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	@Override
	public void finish() {
		super.finish();
	}

	public void finishWithoutAnim() {
		super.finish();
	}

	public void startActivityWithoutAnim(Intent i) {
		super.startActivityForResult(i, -1);
	}

	// @Override
	// public void startActivity(Intent intent) {
	// super.startActivity(intent);
	// overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
	// }

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG.ACTIVITY_FINISH:
			finish();
			break;
		case MSG.ACTIVITY_START:
			Intent i = (Intent) msg.obj;
			startActivityForResult(i, msg.arg1);
			break;
		case MSG.ACTIVITY_CLEAR_SCREENON:
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			break;
		case MSG.ACTIVITY_SHOW_TOAST:
			Util.Assert(msg.arg1 != 0);
			if (msg.arg2 == 0) {
				msg.arg2 = Toast.LENGTH_SHORT;
			}
			ToastUtil.showToast(mConfig.appContext, msg.arg2, msg.arg1);
			break;
		case MSG.ACTIVITY_ASYNC_JOB:
			Util.Assert(msg.obj instanceof Runnable);
			Util.startBackgroundJob(this, null,
					getResources().getString(R.string.edt_dlg_wait),
					(Runnable) msg.obj, mHandler);
			break;
		case MSG.ACTIVITY_SYNC_JOB:
			Util.Assert(msg.obj instanceof Runnable);
			final Dialog dialog = new Dialog(this, R.style.Theme_dialog);
			dialog.setContentView(R.layout.camera_panel_progress);
			dialog.show();
			final Runnable r = (Runnable) msg.obj;
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					r.run();
					dialog.dismiss();
				}

			}, 50);
			break;
		default:
			LogUtil.logE(TAG, "invalaid message %d", msg.what);
			Util.Assert(false);
			break;
		}
	}

	protected void resetScreenOn() {
		mHandler.removeMessages(MSG.ACTIVITY_CLEAR_SCREENON);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	protected void keepScreenOn() {
		mHandler.removeMessages(MSG.ACTIVITY_CLEAR_SCREENON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	protected void keepScreenOnAwhile() {
		mHandler.removeMessages(MSG.ACTIVITY_CLEAR_SCREENON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mHandler.sendEmptyMessageDelayed(MSG.ACTIVITY_CLEAR_SCREENON,
				SCREENON_DELAY);
	}

}
