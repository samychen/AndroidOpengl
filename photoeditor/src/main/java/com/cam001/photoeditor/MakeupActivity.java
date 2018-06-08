package com.cam001.photoeditor;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cam001.photoeditor.makeup.Util.ResUtil;
import com.cam001.photoeditor.makeup.editor.EditorMainCateAdapter;
import com.cam001.photoeditor.makeup.editor.EditorViewBlush;
import com.cam001.photoeditor.makeup.editor.EditorViewCateBase;
import com.cam001.photoeditor.makeup.editor.EditorViewContact;
import com.cam001.photoeditor.makeup.editor.EditorViewLash;
import com.cam001.photoeditor.makeup.editor.EditorViewLine;
import com.cam001.photoeditor.makeup.editor.EditorViewLipstick;
import com.cam001.photoeditor.makeup.editor.EditorViewModule;
import com.cam001.photoeditor.makeup.editor.EditorViewShadow;
import com.cam001.photoeditor.makeup.engine.ResDataStyle;
import com.cam001.photoeditor.makeup.engine.TsMakeuprtEngine;
import com.cam001.photoeditor.makeup.point.EyePointActivity;
import com.cam001.photoeditor.makeup.point.TsFacePointActivity;
import com.cam001.photoeditor.makeup.struct.BlushStruct;
import com.cam001.photoeditor.makeup.struct.ContactStruct;
import com.cam001.photoeditor.makeup.struct.LashStruct;
import com.cam001.photoeditor.makeup.struct.LineStruct;
import com.cam001.photoeditor.makeup.struct.LipstickStruct;
import com.cam001.photoeditor.makeup.struct.ModuleStruct;
import com.cam001.photoeditor.makeup.struct.ShadowStruct;
import com.cam001.stat.StatApi;
import com.cam001.util.BitmapUtil;
import com.cam001.util.CompatibilityUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ImageUtil;
import com.cam001.util.LogUtil;
import com.cam001.util.StorageUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;
import com.cam001.widget.ScaledImageView;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

public class MakeupActivity extends BaseActivity implements OnClickListener,
		EditorMainCateAdapter.EditorMainItemListener {

	public static final String INTENT_EXTRA_PATH = "path";
	public static final int REQUEST_ADJUSTPOINT = 0x00011001;
	public static final int REQUEST_ADJUSTFACE = 0x00011002;

	private ImageView mIvCompare = null, mIvAdjustPoints = null;
	private ImageView mTvBack = null, mTvSave = null;
	private ScaledImageView mIvOriginal = null;
	private TsMakeuprtEngine mTsEngine = TsMakeuprtEngine.getInstance();
	private ModuleStruct[] mStyles = null;
	private ShadowStruct[] mShadows = null;
	private LineStruct[] mLines = null;
	private LashStruct[] mLashes = null;
	private ContactStruct[] mContacts = null;
	private BlushStruct[] mBlushes = null;
	private LipstickStruct[] mLips = null;
	private ResDataStyle mCurrStyle = null;
	private int mIndexModule = 1, mIndexShadow = -1, mIndexBlush = -1,
			mIndexContact = -1, mIndexLash = -1, mIndexLine = -1,
			mIndexLipstick = -1;
	private Uri mUri = null;
	private String mFilePath = null;
	private GridView mGvBottom = null;
	private RelativeLayout mRlCateGroup = null;
	public AppConfig mConfig = AppConfig.getInstance();
	public RelativeLayout.LayoutParams mParams = null;
	private Dialog mUpdateDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		mParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		Display disp = getWindowManager().getDefaultDisplay();
		mConfig.screenWidth = disp.getWidth();
		mConfig.screenHeight = disp.getHeight();
		mTsEngine.initHandle(getAssets(), 0);
		setContentView(R.layout.activity_makeuprt);
		initControls();
		// mStyles = decodeFromAssets(this);
		Util.startBackgroundJob(MakeupActivity.this, "", "", new Runnable() {

			@Override
			public void run() {

				Intent intent = getIntent();
				mUri = intent.getData();
				if(Util.LowMemory(MakeupActivity.this)){
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							ToastUtil.showShortToast(MakeupActivity.this, R.string.low_mem_toast);
							finish();
						}
					});

					return;
				} else {
					boolean bLoad = false;
					if (mUri != null) {
						bLoad = mTsEngine.load(mConfig.appContext, mUri);
					}
					mFilePath = intent.getStringExtra(INTENT_EXTRA_PATH);
					if (mFilePath != null) {
						bLoad = mTsEngine.load(mConfig.appContext, mFilePath);
					}

					final Parcelable[] p = intent
							.getParcelableArrayExtra(TsFacePointActivity.INTENT_EXTRA_POINT);

					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Bitmap bmp = mTsEngine.getOriginalBitmap();
							if (bmp != null)
								mIvOriginal.setImageBitmap(bmp);
							else {
								ToastUtil.showShortToast(MakeupActivity.this,
										R.string.crop_decode_bmp_err);
								finish();
								return;
							}
						}
					});

					if (p != null && p.length > 0) {
						Point[] outline = new Point[p.length];
						for (int i = 0; i < p.length; i++) {
							outline[i] = (Point) p[i];
						}
						mTsEngine.setOutline(outline);
					}
					if (bLoad){
						mLines = ResUtil
								.decodeLineFromAssets(MakeupActivity.this);
						mLashes = ResUtil
								.decodeLashFromAssets(MakeupActivity.this);
						mLips = ResUtil
								.decodeLipstickFromAssets(MakeupActivity.this);
						mBlushes = ResUtil
								.decodeBlushFromAssets(MakeupActivity.this);
						mContacts = ResUtil
								.decodeContactFromAssets(MakeupActivity.this);
						mShadows = ResUtil
								.decodeShadowFromAssets(MakeupActivity.this);
						mStyles = ResUtil
								.decodeModuleFromAssets(MakeupActivity.this);

						mHandler.sendEmptyMessage(0x00001);
					} else {
						Intent i = new Intent();
						i.setData(mUri);
						i.putExtra(INTENT_EXTRA_PATH, mFilePath);
						i.setClass(MakeupActivity.this,
								EyePointActivity.class);
						startActivityForResult(i, REQUEST_ADJUSTFACE);
					}
				}
			}
		}, mHandler);
		StatApi.updateOnlineConfig(this);
		String param = StatApi.getParam(this, StatApi.APPACKAGE_NAME);
		if (param != null) {
			SharedPreferences sp = getSharedPreferences(StatApi.APPACKAGE_NAME,Activity.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString(StatApi.APPACKAGE_NAME, param);
			editor.commit();
		}
		StatApi.setOnlineConfigureListener(new UmengOnlineConfigureListener() {

			@Override
			public void onDataReceived(JSONObject arg0) {
				try {
					String param = arg0.getString(StatApi.APPACKAGE_NAME);
					if (param != null) {
						SharedPreferences sp = getSharedPreferences(StatApi.APPACKAGE_NAME,Activity.MODE_PRIVATE);
						Editor editor = sp.edit();
						editor.putString(StatApi.APPACKAGE_NAME, param);
						editor.commit();
					}
				} catch (Exception e) {
				}
			}
		});
	}

	@Override
	protected void onResume() {
		if(mTsEngine.ismHandlerEmpty()){
			finish();
			return;
		}
		super.onResume();
	}

	class DecodeTask extends AsyncTask<Void, Void, Object> {

		@Override
		protected Object doInBackground(Void... params) {
			LogUtil.startLogTime("bug");
			mLines = ResUtil.decodeLineFromAssets(MakeupActivity.this);
			mLashes = ResUtil.decodeLashFromAssets(MakeupActivity.this);
			mLips = ResUtil.decodeLipstickFromAssets(MakeupActivity.this);
			mBlushes = ResUtil.decodeBlushFromAssets(MakeupActivity.this);
			mContacts = ResUtil.decodeContactFromAssets(MakeupActivity.this);
			mShadows = ResUtil.decodeShadowFromAssets(MakeupActivity.this);

			mStyles = ResUtil.decodeModuleFromAssets(MakeupActivity.this);
			LogUtil.stopLogTime("bug");
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			// if (mStyles == null) {
			// return;
			// }
			changeMode(mIndexModule);
			mRlCateGroup.removeAllViews();
			EditorViewModule editorCate = new EditorViewModule(
					MakeupActivity.this, mStyles, mIndexModule);
			mRlCateGroup.addView(editorCate, mParams);
		}

	}

	private void initControls() {
		mIvOriginal = (ScaledImageView) findViewById(R.id.imageview_original);
		mTvBack = (ImageView) findViewById(R.id.back);
		mTvSave = (ImageView) findViewById(R.id.save);
		mIvCompare = (ImageView) findViewById(R.id.compare);
		mIvAdjustPoints = (ImageView) findViewById(R.id.adjustpoints);
		mGvBottom = (GridView) findViewById(R.id.gridview_bottom);
		mRlCateGroup = (RelativeLayout) findViewById(R.id.edit_cate_viewgroup);
		mTvBack.setOnClickListener(this);
		mTvSave.setOnClickListener(this);
		mIvCompare.setOnTouchListener(mCompareListener);
		mIvAdjustPoints.setOnClickListener(this);
		EditorMainCateAdapter editorMainCateAdapter = new EditorMainCateAdapter(
				this, mGvBottom);
		mGvBottom.setAdapter(editorMainCateAdapter);
		editorMainCateAdapter.setHighlight(EditorMainCateAdapter.DefaultSelect);
	}

	private OnTouchListener mCompareListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				mIvOriginal.setImageBitmap(mTsEngine.getResultBitmap());
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				mIvOriginal.setImageBitmap(mTsEngine.getOriginalBitmap());
				break;
			}
			return true;
		}
	};

	@Override
	protected void onDestroy() {
		mTsEngine.uninitHandle();
		mUpdateDialog=null;
		super.onDestroy();
	}

	private Bitmap loadImage(Uri uri) {
		Bitmap bmp = null;
		if(CompatibilityUtil.low512MMemory())
			bmp = BitmapUtil.getBitmap(uri, getApplicationContext(), 1080, 1920);
		else
			bmp = BitmapUtil.getBitmap(uri, getApplicationContext(), 720, 1280);

		if (bmp.getWidth() % 2 == 0) {
			return bmp;
		}
		Bitmap cropped = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth() / 2 * 2,
				bmp.getHeight());
		bmp.recycle();
		return cropped;
	}

	private Bitmap loadImage(String path) {
		Bitmap bmp = null;
		if (path != null && path.startsWith("gallery/")) {
			InputStream is = null;
			try {
				is = getAssets().open(path);
				bmp = BitmapFactory.decodeStream(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Util.closeSilently(is);
			}
		} else {
			if(CompatibilityUtil.low512MMemory())
				bmp = BitmapUtil.getBitmap(path, 1080, 1920);
			else
				bmp = BitmapUtil.getBitmap(path, 720, 1280);
		}
		if (bmp == null)
			return null;
		if (bmp.getWidth() % 2 == 0) {
			return bmp;
		}
		Bitmap cropped = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth() / 2 * 2,
				bmp.getHeight());
		bmp.recycle();
		return cropped;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.adjustpoints:
			Intent intent = new Intent(getApplicationContext(),
					TsFacePointActivity.class);
			startActivityForResult(intent, REQUEST_ADJUSTPOINT);
			break;
		case R.id.back:
			if (mTsEngine.isChanged) {
				showBackDialog();
				return;
			}
			finish();
			break;
		case R.id.save:
			if(!StorageUtil.checkStorage()) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ToastUtil.showShortToast(getApplicationContext(), R.string.common_no_storage);
					}
				});
				return;
			}

			Util.startBackgroundJob(this, "", "Please wait...", new Runnable() {
				@Override
				public void run() {
					Bitmap bmp = mTsEngine.getResultBitmap();
					if (bmp == null)
						bmp = mTsEngine.getOriginalBitmap();

					if (mUri==null) {
						mUri=Uri.parse("file://" +mFilePath);
					}

					String path = FileUtil.getPath(MakeupActivity.this, mUri);
					mConfig.mCurrUri=ImageUtil.save(MakeupActivity.this, mUri, bmp,path);
					setResult(RESULT_OK, new Intent().setData(mConfig.mCurrUri));
					finish();
				}
			}, mHandler);
			break;
		}
	}
//	@Override
//	public void finish() {
//		if(mSaveUri != null){
//			Intent intent = new Intent();
//			intent.setClass(getApplicationContext(), GalleryActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//		}
//		super.finish();
//	}


	public void changeMode(int index) {
		ResDataStyle style = new ResDataStyle();
		mIndexModule = index;
		mIndexShadow = mStyles[index].shadow;
		mIndexLine = mStyles[index].eyeline;
		mIndexContact = mStyles[index].contact;
		mIndexLash = mStyles[index].eyelash;
		mIndexBlush = mStyles[index].blush;
		mIndexLipstick = mStyles[index].lipstick;
		style.setStyle(mShadows[mIndexShadow], mLines[mIndexLine],
				mContacts[mIndexContact], mLashes[mIndexLash],
				mBlushes[mIndexBlush], mLips[mIndexLipstick]);
		if(mStyles[index].bratio != -1){
			style.bratio = mStyles[index].bratio;
		}
		if(mStyles[index].lashratio != -1){
			style.elashratio = mStyles[index].lashratio;
		}
		if(mStyles[index].lineratio != -1){
			style.elineratio = mStyles[index].lineratio;
		}
		if(mStyles[index].shadowratio != -1){
			style.eshaderatio = mStyles[index].shadowratio;
		}
		if(mStyles[index].conratio != -1){
			style.cratio = mStyles[index].conratio;
		}
		if(mStyles[index].lipratio != -1){
			style.lratio = mStyles[index].lipratio;
		}
		mCurrStyle = style;
		mTsEngine.loadResource(mCurrStyle, ResDataStyle.FULL, true);
		doMakeuprt();
	}

	public void doMakeuprt() {
		mTsEngine.makeup();
		mIvOriginal.setImageBitmap(mTsEngine.getResultBitmap());
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x00001:
				changeMode(mIndexModule);
				mRlCateGroup.removeAllViews();
				EditorViewModule editorCate = new EditorViewModule(
						MakeupActivity.this, mStyles, mIndexModule);
				mRlCateGroup.addView(editorCate, mParams);
				break;
			}
		}

	};
	private Uri mSaveUri = null;

	public Uri save() {
		long dateTaken = System.currentTimeMillis();
		String name = String.format("%d", dateTaken) + ".jpg";
//		 String[] temp = mFilePath.split("/");
//		 try {
//		 name = temp[temp.length-1]+"_R.jpg";
//		 } catch (Exception e) {
//		 }
		if (mUri != null) {
			mFilePath = StorageUtil.getRealPath(getContentResolver(), mUri);
		}
		if (mFilePath != null) {
			String[] temp = mFilePath.split("/");
			String imgName = temp[temp.length - 1];
			if (imgName.endsWith(".jpg")) {
				String substring = imgName.substring(0, imgName.length() - 4);
				name = substring + "_"+String.format("%d", dateTaken) + "_R.jpg";
			}
		}
		String path = StorageUtil.DIRECTORY + "/" + name;
		StorageUtil.ensureOSXCompatible();
		Bitmap bmp = mTsEngine.getResultBitmap();
		if (bmp == null)
			bmp = mTsEngine.getOriginalBitmap();
		if (mSaveUri == null || mTsEngine.isChanged) {
			mSaveUri = StorageUtil.addImage(getContentResolver(), path,
					dateTaken, null, 0, bmp);
			mTsEngine.isChanged = false;
		}
		return mSaveUri;
	}

	@Override
	public void onBackPressed() {
		if (mIvOriginal.onBackPressed()) {
			return;
		}
		if (mTsEngine.isChanged) {
			showBackDialog();
			return;
		}

		super.onBackPressed();
	}

	private Dialog mDialog = null;

	private void showBackDialog() {
		if (mDialog == null) {
			mDialog = new Dialog(this, R.style.Theme_dialog);
			mDialog.setContentView(R.layout.editor_back_dialog);
			mDialog.findViewById(R.id.back_btn_cancel).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							if (mDialog != null) {
								mDialog.dismiss();
							}
						}
					});
			mDialog.findViewById(R.id.back_btn_sure).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							if (mDialog != null) {
								mDialog.dismiss();
							}
							finish();
						}
					});
		}
		mDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_ADJUSTPOINT:
				mTsEngine.makeup();
				mIvOriginal.setImageBitmap(mTsEngine.getResultBitmap());
				break;
			case REQUEST_ADJUSTFACE:
				Rect face = (Rect) data
						.getParcelableExtra(EyePointActivity.INTENT_EXTRA_FACERECT);
				LogUtil.logV("", "step " + face);
				Point leye = (Point) data
						.getParcelableExtra(EyePointActivity.INTENT_EXTRA_LEYEPOINT);
				Point reye = (Point) data
						.getParcelableExtra(EyePointActivity.INTENT_EXTRA_REYEPOINT);
				Point mouth = (Point) data
						.getParcelableExtra(EyePointActivity.INTENT_EXTRA_MOUTHPOINT);
				LogUtil.logV("", "step " + face);
				mTsEngine.setFace(face, leye, reye, mouth);
				if(mTsEngine.getOutline()!=null){
					new DecodeTask().execute();
				}else{
					ToastUtil.showShortToast(this,R.string.error_comment);
					finish();
				}
				break;
			}
		} else if (requestCode == REQUEST_ADJUSTFACE) {
			finish();
		}
	}

	private int mMainCate = -1;

	@Override
	public void onMainItemClick(int position) {
		Log.d("bug", "position=" + position);
		if (mMainCate != position) {
			mMainCate = position;
			mRlCateGroup.removeAllViews();
			EditorViewCateBase editorCate = null;
			switch (position) {
			case 0:
				editorCate = new EditorViewModule(this, mStyles, mIndexModule);
				break;
			case 1:
				editorCate = new EditorViewShadow(this, mCurrStyle, mShadows,
						mIndexShadow);
				break;
			case 2:
				editorCate = new EditorViewLine(this, mCurrStyle, mLines,
						mIndexLine);
				break;
			case 3:
				editorCate = new EditorViewContact(this, mCurrStyle, mContacts,
						mIndexContact);
				break;
			case 4:
				editorCate = new EditorViewLash(this, mCurrStyle, mLashes,
						mIndexLash);
				break;
			case 5:
				editorCate = new EditorViewBlush(this, mCurrStyle, mBlushes,
						mIndexBlush);
				break;
			case 6:
				editorCate = new EditorViewLipstick(this, mCurrStyle, mLips,
						mIndexLipstick);
				break;
			}
			if (editorCate != null)
				mRlCateGroup.addView(editorCate, mParams);
		}
	}

	public ResDataStyle getStyle() {
		return mCurrStyle;
	}

	public void setStyle(ResDataStyle style) {
		mCurrStyle = style;
	}

	public void setmIndexShadow(int mIndexShadow) {
		this.mIndexShadow = mIndexShadow;
	}

	public void setmIndexBlush(int mIndexBlush) {
		this.mIndexBlush = mIndexBlush;
	}

	public void setmIndexContact(int mIndexContact) {
		this.mIndexContact = mIndexContact;
	}

	public void setmIndexLash(int mIndexLash) {
		this.mIndexLash = mIndexLash;
	}

	public void setmIndexLine(int mIndexLine) {
		this.mIndexLine = mIndexLine;
	}

	public void setmIndexLipstick(int mIndexLipstick) {
		this.mIndexLipstick = mIndexLipstick;
	}

	public void setIndexModule(int mSel) {
		this.mIndexModule = mSel;
	}

}
