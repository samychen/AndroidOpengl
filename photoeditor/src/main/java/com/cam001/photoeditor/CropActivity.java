package com.cam001.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.cam001.util.BitmapUtil;
import com.cam001.util.DebugUtil;
import com.cam001.util.ExifUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ImageUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;
import com.edmodo.cropper.CropImageView;
import com.umeng.analytics.MobclickAgent;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class CropActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "CropActivity";
	public static final int MAX_IMAGE_WIDTH = 1024;
	public static final int MAX_IMAGE_HEIGHT = 1024;
	private CropImageView mCropImageView = null;
	private Bitmap mOriginalBmp = null;
	private Handler mHandler = new Handler();
	private int mOriginalW = -1, mOriginalH = -1;
	// cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
	private Uri mUri = null;
	private View mSelectedView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);
		mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
		findViewById(R.id.btn_crop_cancel).setOnClickListener(this);
		findViewById(R.id.btn_crop_sure).setOnClickListener(this);
		findViewById(R.id.btn_crop_1_1).setOnClickListener(this);
		findViewById(R.id.btn_crop_free).setOnClickListener(this);
		findViewById(R.id.btn_crop_3_4).setOnClickListener(this);
		findViewById(R.id.btn_crop_4_3).setOnClickListener(this);

		mSelectedView = findViewById(R.id.btn_crop_free);

//		Util.startBackgroundJob(this, null, null, new Runnable() {
//			@Override
//			public void run() {
				if (!loadImage()) {
					ToastUtil.showShortToast(mConfig.appContext, R.string.invalid_file);
					finish();
					return;
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mCropImageView.setImageBitmap(mOriginalBmp);
						mCropImageView.setGuidelines(2);
						mCropImageView.setFixedAspectRatio(false);
						mSelectedView.setSelected(true);
						((View)mCropImageView.getParent()).invalidate();
					}
				});
//			}
//		}, mHandler);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_crop_cancel:
			MobclickAgent.onEvent(CropActivity.this, "button_click",
					"btn_crop_cancel");
//			if (mIvCropResult.getVisibility() == View.VISIBLE) {
//				mCropImageView.setVisibility(View.VISIBLE);
//				mIvCropResult.setVisibility(View.GONE);
//				if (mConfig.mBmpResult != null
//						&& !mConfig.mBmpResult.isRecycled()) {
//					mConfig.mBmpResult.recycle();
//					mConfig.mBmpResult = null;
//				}
//				return;
//			}
			finish();
			break;
		case R.id.btn_crop_sure:
			Util.startBackgroundJob(CropActivity.this, "", "", new Runnable() {
				@Override
				public void run() {
					RectF rect = mCropImageView.getTransformRectF(mOriginalW,
							mOriginalH);
					String path= FileUtil.getPath(CropActivity.this,mUri);
					mConfig.mCurrUri= ImageUtil.crop(CropActivity.this, mUri, rect,path);
					setResult(RESULT_OK, new Intent().setData(mConfig.mCurrUri));
					finish();
				}
			}, mHandler);

			MobclickAgent.onEvent(CropActivity.this, "button_click",
					"btn_crop_ok");
			break;
		case R.id.btn_crop_1_1:
			MobclickAgent.onEvent(CropActivity.this, "button_click",
					"btn_crop_1_1");
			mSelectedView.setSelected(false);
			v.setSelected(true);
			mSelectedView = v;
			mCropImageView.setFixedAspectRatio(true);
			mCropImageView.setAspectRatio(10, 10);
			break;
		case R.id.btn_crop_free:
			MobclickAgent.onEvent(CropActivity.this, "button_click",
					"btn_crop_free");
			mSelectedView.setSelected(false);
			v.setSelected(true);
			mSelectedView = v;
			mCropImageView.setFixedAspectRatio(false);
			break;
			case R.id.btn_crop_3_4:
				MobclickAgent.onEvent(CropActivity.this, "button_click",
						"btn_crop_3_4");
				mSelectedView.setSelected(false);
				v.setSelected(true);
				mSelectedView = v;
				mCropImageView.setFixedAspectRatio(true);
				mCropImageView.setAspectRatio(3, 4);
				break;
			case R.id.btn_crop_4_3:
				MobclickAgent.onEvent(CropActivity.this, "button_click",
						"btn_crop_4_3");
				mSelectedView.setSelected(false);
				v.setSelected(true);
				mSelectedView = v;
				mCropImageView.setFixedAspectRatio(true);
				mCropImageView.setAspectRatio(4, 3);
				break;
		}
	}
	public static final String PATH = "path";
	private boolean loadImage() {
		mUri = getIntent().getData();
		if(mUri==null) {
			return false;
		}
		mOriginalBmp = loadImage(mUri);
		if (mOriginalBmp == null)
			return false;
		return true;
	}

	public Bitmap loadImage(Uri uri) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		InputStream is;
		try {
			is = getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(is, null, opts);
			mOriginalW = opts.outWidth;
			mOriginalH = opts.outHeight;
            int orientation = ExifUtil.getOrientation(uri, mConfig.appContext);
            if(orientation%180==90) {
                mOriginalW = opts.outHeight;
                mOriginalH = opts.outWidth;
            }
		} catch (FileNotFoundException e) {
			DebugUtil.logE(TAG, "loadImage(uri) getOriginal width&height fatal", e);
		}
		Bitmap bmp = BitmapUtil.getBitmap(uri, this, MAX_IMAGE_WIDTH,
				MAX_IMAGE_WIDTH);
		return bmp;
	}

	@Override
	protected void onDestroy() {
		if (mOriginalBmp != null && !mOriginalBmp.isRecycled()) {
			mOriginalBmp.recycle();
			mOriginalBmp = null;
		}
		super.onDestroy();
	}
}
