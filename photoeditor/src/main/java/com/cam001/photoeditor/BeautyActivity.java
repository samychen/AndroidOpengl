package com.cam001.photoeditor;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Toast;

import com.cam001.photoeditor.beauty.ConfirmDialog;
import com.cam001.photoeditor.beauty.EditorViewBase;
import com.cam001.photoeditor.beauty.EditorViewDeblemish;
import com.cam001.photoeditor.beauty.EditorViewEnlargeEyes;
import com.cam001.photoeditor.beauty.EditorViewEyeBag;
import com.cam001.photoeditor.beauty.EditorViewEyeCircle;
import com.cam001.photoeditor.beauty.EditorViewEyeShine;
import com.cam001.photoeditor.beauty.EditorViewFaceColor;
import com.cam001.photoeditor.beauty.EditorViewFaceSoften;
import com.cam001.photoeditor.beauty.EditorViewFaceTrim;
import com.cam001.photoeditor.beauty.EditorViewFaceWhiten;
import com.cam001.photoeditor.beauty.EditorViewMain;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.engine.EditorHistory;
import com.cam001.photoeditor.beauty.makeup.FacePointActivity;
import com.cam001.photoeditor.beauty.makeup.FacePointUtil;
import com.cam001.photoeditor.beauty.makeup.engine.MakeupEngine;
import com.cam001.service.LogUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ImageUtil;
import com.cam001.util.Util;
import com.umeng.analytics.MobclickAgent;

public class BeautyActivity extends BaseActivity
	implements Runnable {
	private static final String TAG = "BeautyActivity";

	public static final int REQUEST_CODE_CROP = 0;
	public static final int REQUEST_CODE_FACE = 1;

	private EditEngine mEngine = null;
	public EditorViewBase mEditorView = null;
	private Thread mLoadThread = null;
	
	private ImageSaver mImageSaver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(mConfig.mActivity != null){
			if (mConfig.mActivity != BeautyActivity.this){
				mConfig.mActivity.finish();
			}
		}
		mConfig.mActivity = BeautyActivity.this;

		MobclickAgent.updateOnlineConfig(this);

		EditorHistory.getInstance().clear();
		EditEngine.destroy();

		mEditorView = new EditorViewMain(this,true);

		setContentView(mEditorView);

		mEditorView.setHandler(mHandler);
		
		mEngine = EditEngine.getInstance();
		mLoadThread = new Thread(this);
		mLoadThread.start();
		
		mImageSaver = new ImageSaver();

		Util.initializeScreenBrightness(getWindow(), getContentResolver());
	}

	private Uri mUri;
	private boolean loadImage() {
		boolean res = false;
		if (getIntent().getData() != null) {
			mUri= getIntent().getData();
			if (mUri.getScheme().equalsIgnoreCase("file")) {
				res = mEngine.loadImage(mUri.getPath());
			} else {
				res = mEngine.loadImage(mUri);
			}
		} else {
			LogUtil.logE(TAG, "No image iput, exit.");
			mHandler.sendEmptyMessage(MSG.ACTIVITY_FINISH);
		}
        
		mEditorView.mDispView.postInvalidate();
		return res;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// if (hasFocus)
		// {
		//
		// mEditorView.controlCallManulAnimation();
		// }
		//

		super.onWindowFocusChanged(hasFocus);
	}

	private void loadFaces() {
		MakeupEngine.Init_Lib();
		FaceInfo[] faces = mEngine.getEditBitmap().getFaces();
		
		if (faces == null || faces.length<1) {
			faces = new FaceInfo[1];
			int bmpWidth = mEngine.getEditBitmap().getWidth();
			int bmpHeight = mEngine.getEditBitmap().getHeight();
			faces[0] = FacePointUtil.createDefaultFace(bmpWidth, bmpHeight);
			mEngine.getEditBitmap().setFaces(faces);
		}

		Bitmap bmp = mEngine.getEditBitmap().getBitmap();
		MakeupEngine.ReloadFaceInfo(bmp, faces[0], true);
	}

	@Override
	public void finish() {
		if(mLoadThread!=null && mLoadThread.isAlive()) {
			Util.startBackgroundJob(this, null,
					getResources().getString(R.string.edt_dlg_wait),
					new Runnable() {
						@Override
						public void run() {
							Util.joinThreadSilently(mLoadThread);
							mLoadThread = null;
							mHandler.sendEmptyMessage(MSG.ACTIVITY_FINISH);
						}
					}, mHandler);
			return;
		}
		
		if (mLoadThread != null) {
			mLoadThread = null;
		}

		super.finish();
	}
	
	@Override
	protected void onPause() {
		mEditorView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mEditorView.onResume();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		if (mEditorView.onBackPressed()) {
			return;
		}

		if (mEditorView.isModified()) {
			ConfirmDialog dlgTmep = null;
//			if (mEditorView.isModified()) {
//				dlgTmep = new ConfirmDialog(this, R.string.edt_lnl_cancelmsg);
//			} else {
				dlgTmep = new ConfirmDialog(this, R.string.edt_lnl_quitmsg);
//			}

			final ConfirmDialog dlg = dlgTmep;
			dlg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.confirm_button_sure:
						dlg.dismiss();
						if (mEditorView.onBackPressed()) {
							return;
						}
						mHandler.sendEmptyMessage(MSG.ACTIVITY_FINISH);
						setMobclickAgent();
						break;
					case R.id.confirm_button_cancel:
						dlg.dismiss();
						break;
					default:
						break;
					}
				}
			});
			dlg.show();
			return;
		}

		super.onBackPressed();
	}

	private void setMobclickAgent() {
//		MobclickAgent.onEventEnd(BeautyActivity.this, "End to MainMakeUp");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_FACE:
				loadFaces();
				int targetMode = data.getIntExtra(
						FacePointActivity.EXTRA_TARGET_MODE, 0);
				switchEditorMode(targetMode, 0);
				break;
			default:

				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressLint("NewApi")
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case MSG.EDITOR_SAVE:
				Util.startBackgroundJob(this, "", "", new Runnable() {
					@Override
					public void run() {
						String path = FileUtil.getPath(BeautyActivity.this, mUri);
						mConfig.mCurrUri = ImageUtil.save(BeautyActivity.this, mUri, mEngine.saveToBitmap(), path);
						setResult(RESULT_OK, new Intent().setData(mConfig.mCurrUri));
						finish();
					}
				}, mHandler);
				break;
			case MSG.EDITOR_SWITCH_MODE:
//			if (msg.arg2 == RESULT_OK) { // If edit bitmap changed, reset saved
//											// URI.
//				mSavedUri = null;
//			}
				switchEditorMode(msg.arg1, msg.arg2);
				break;
			case MSG.EDITOR_CANCEL:
				onBackPressed();
				break;
			case MSG.EDITOR_REFRESH_MAKEUP:
				loadFaces();
				break;
			case MSG.HIDE_PERCENT_TXT:
				if (mEditorView.mPercentTxt.getVisibility() == View.VISIBLE) {
					mEditorView.startPercentPushOnAnim();
				}
				break;
			case MSG.EDIT_MORE:
				String packageName = MobclickAgent.getConfigParams(this,
						"show_package_name");
				if (packageName != null && !packageName.isEmpty()) {
					if (packageName.equals("com.ucamera.ucam")) {
						goToUcameraStore(packageName);
					}
				} else {
					goToUcameraStore("com.ucamera.ucam");
				}
				break;
			default:
				super.handleMessage(msg);
		}
	}
	
	private void goToUcameraStore(String appPackage){
		try {
			if(appPackage.equals("com.ucamera.ucam")){
				if(Util.isAppInstalled(this, "com.ucamera.ucam")){
					Util.launchApplication(this, "com.ucamera.ucam", "com.ucamera.ucam.CameraActivity");
					return;
				}
			}
			
			Uri uri = null;
			uri = Uri.parse("market://details?id="
					+ "com.ucamera.ucam");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this,
					R.string.text_not_installed_market_app,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void switchEditorMode(final int mode, final int result) {
		if(mLoadThread!=null && mLoadThread.isAlive()) {
			Util.startBackgroundJob(this, null,
					getResources().getString(R.string.edt_dlg_wait),
					new Runnable() {
						@Override
						public void run() {
							Util.joinThreadSilently(mLoadThread);
							mLoadThread = null;
							mHandler.post(new Runnable(){
								@Override
								public void run() {
									switchEditorMode(mode, result);
								}
							});
						}
					}, mHandler);
			return;
		}

		EditorViewBase lastView = mEditorView;
		switch (mode) {
		case EditEngine.EDIT_MODE_MAIN:
			if (mEditorView instanceof EditorViewMain) {
				return;
			}
			mEditorView = new EditorViewMain(this,false);
			break;
		case EditEngine.EDIT_MODE_FACEWHITEN:
			if (mEditorView instanceof EditorViewFaceWhiten) {
				return;
			}
			mEditorView = new EditorViewFaceWhiten(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_FACESOFTEN:
			if (mEditorView instanceof EditorViewFaceSoften) {
				return;
			}
			mEditorView = new EditorViewFaceSoften(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_FACECOLOR:
			if (mEditorView instanceof EditorViewFaceColor) {
				return;
			}
			mEditorView = new EditorViewFaceColor(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_FACETRIM:
			if (mEditorView instanceof EditorViewFaceTrim) {
				return;
			}
			mEditorView = new EditorViewFaceTrim(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_EYEBAG:
			if (mEditorView instanceof EditorViewEyeBag) {
				return;
			}
			mEditorView = new EditorViewEyeBag(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_ENLAGEEYES:
			if (mEditorView instanceof EditorViewEnlargeEyes) {
				return;
			}
			mEditorView = new EditorViewEnlargeEyes(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_BRIGHTEYES:
			if (mEditorView instanceof EditorViewEyeShine) {
				return;
			}
			mEditorView = new EditorViewEyeShine(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_EYECIRCLE:
			if (mEditorView instanceof EditorViewEyeCircle) {
				return;
			}
			mEditorView = new EditorViewEyeCircle(mConfig.appContext);
			break;
		case EditEngine.EDIT_MODE_DEBLEMISH:
			if (mEditorView instanceof EditorViewDeblemish) {
				return;
			}
			mEditorView = new EditorViewDeblemish(mConfig.appContext);
			break;
		default:
			throw new RuntimeException();
		}
		mEditorView.setHandler(mHandler);
		mHandler.sendEmptyMessageDelayed(MSG.HIDE_PERCENT_TXT, mEditorView.DEPLAY_TIME);
		mEngine.setEditMode(mode);
		lastView.startLeaveAnimation(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setContentView(mEditorView);
						mEditorView.startEnterAnimation(null);
					}

				});
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}
		});
	}

	@Override
	public void run() {
		if (loadImage()) {
			EditorHistory.getInstance().addHistory(mEngine.getEditBitmap().getBitmap());
			loadFaces();
		} else {
			Message msg = Message.obtain(mHandler,
					MSG.ACTIVITY_SHOW_TOAST,
					R.string.edt_tst_load_failed,
					Toast.LENGTH_SHORT);
			msg.sendToTarget();
			mHandler.sendEmptyMessage(MSG.ACTIVITY_FINISH);
		}
	}
	
	public class ImageSaver implements Runnable{
		
		private Thread mSaveThread = null;
		private Uri mSavedUri = null;
		
		public ImageSaver(){};
		
		public void saveAsync() {
			waitSaveDone();
			mSaveThread = new Thread(this);
			mSaveThread.start();
		}

		public boolean isSaveDone() {
			if(mSaveThread==null) return true;
			if(mSaveThread.isAlive()) return false;
			return true;
		}
		
		public void waitSaveDone() {
			if(isSaveDone()) return;
			try {
				mSaveThread.join();
			} catch (InterruptedException e) {
			}
			mSaveThread = null;
		}
		
		public void waitSaveDoneWithDialog(final Runnable afterSaveDone) {
			Util.startBackgroundJob(BeautyActivity.this, "", "", new Runnable(){
				@Override
				public void run() {
					waitSaveDone();
					mHandler.post(afterSaveDone);
				}
			}, mHandler);
		}
		
		public Uri getSavedUri() {
			waitSaveDone();
			return mSavedUri;
		}
		
		public String getSavedPath() {
			waitSaveDone();
			return mEngine.savePath;
		}
		
		@Override
		public void run() {
			if (mEditorView.isModified()) {
				Uri uri = mEngine.saveToUri();
				mSavedUri = uri;
				mEditorView.onSave();
			}
		}
	}
}
