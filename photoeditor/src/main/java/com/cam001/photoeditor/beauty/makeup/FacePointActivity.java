package com.cam001.photoeditor.beauty.makeup;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.cam001.photoeditor.BaseActivity;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.engine.EditBitmap;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.makeup.widget.MagnifierView;
import com.cam001.util.LogUtil;
import com.cam001.util.Util;

public class FacePointActivity extends BaseActivity
	implements View.OnClickListener, 
	View.OnTouchListener {
	
	private static final String TAG = "FacePointActivity";
	public static final String EXTRA_TARGET_MODE = "target_mode";

	private MagnifierView magniferview;
	private EditBitmap mEditBitmap = null;
	private FaceInfo[] mFaces = null;
	private ImageView mImageView = null;
	private RelativeLayout mDispLayout = null;
	private View mHelpView = null;
	
	private Matrix mMatImage = null;
	private RectF mRectImageDisp = new RectF();
	
	private ImageView mEye1 = null;
	private ImageView mEye2 = null;
	private ImageView mMouth = null;
	
	private Bitmap mBmpEye = null;
	private Bitmap mBmpMouth = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEditBitmap = EditEngine.getInstance().getEditBitmap();
		mFaces = mEditBitmap.getFaces();
		if(mFaces==null || mFaces.length<1) {
			mFaces = new FaceInfo[1];
			mFaces[0] = FacePointUtil.createDefaultFace(mEditBitmap.getWidth(), mEditBitmap.getHeight());
		}
		initControls();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(mMatImage!=null) return;
		if(mImageView.getWidth()==0) return;
		
		mMatImage = new Matrix();
		RectF viewRect = new RectF(0,0,mImageView.getWidth(),mImageView.getHeight());
		Bitmap bmp = mEditBitmap.getBitmap();
		RectF bmpRect = new RectF(0,0,bmp.getWidth(),bmp.getHeight());
		mMatImage.setRectToRect(bmpRect, viewRect, ScaleToFit.CENTER);
		mImageView.setImageMatrix(mMatImage);
		mMatImage.mapRect(mRectImageDisp, bmpRect);
		invalidFacePoints(mFaces[0]);
	}

	private void initControls() {
		setContentView(R.layout.face_point_activity);
		findViewById(R.id.face_button_back).setOnClickListener(this);
		findViewById(R.id.face_button_done).setOnClickListener(this);
		mImageView = (ImageView) findViewById(R.id.face_display_view);
		mImageView.setImageBitmap(mEditBitmap.getBitmap());
		mImageView.setOnTouchListener(this);
		mHelpView = findViewById(R.id.face_help);
		
		magniferview = new MagnifierView(getApplicationContext());
		magniferview.setDisplayView(mImageView);
		
		
		mDispLayout = (RelativeLayout) findViewById(R.id.face_display_layout);
		mDispLayout.addView(magniferview, 0);
		
		mBmpEye = BitmapFactory.decodeResource(getResources(), R.drawable.posting_eye);
		mBmpMouth = BitmapFactory.decodeResource(getResources(), R.drawable.posting_mouth);
		
		mEye1 = new ImageView(mConfig.appContext);
		mEye1.setImageBitmap(mBmpEye);
		mEye2 = new ImageView(mConfig.appContext);
		mEye2.setImageBitmap(mBmpEye);
		mMouth = new ImageView(mConfig.appContext);
		mMouth.setImageBitmap(mBmpMouth);
		
		LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mDispLayout.addView(mEye1,param);
		param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mDispLayout.addView(mEye2,param);
		param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mDispLayout.addView(mMouth,param);
		
		magniferview.bringToFront();
//		 MobclickAgent
//			.onEventBegin(this, "Begin to facePointMakeUp");
	}
	
	private void invalidFacePoints(FaceInfo face) {
		FaceInfo mappedFace = mapFaceBmpToView(face);
		LayoutParams params = (LayoutParams) mEye1.getLayoutParams();
		params.leftMargin = mappedFace.eye1.centerX() - mEye1.getWidth()/2;
		params.topMargin = mappedFace.eye1.centerY() - mEye1.getHeight()/2;
		params = (LayoutParams) mEye2.getLayoutParams();
		params.leftMargin = mappedFace.eye2.centerX() - mEye2.getWidth()/2;
		params.topMargin = mappedFace.eye2.centerY() - mEye2.getHeight()/2;
		params = (LayoutParams) mMouth.getLayoutParams();
		params.leftMargin = mappedFace.mouth.centerX() - mMouth.getWidth()/2;
		params.topMargin = mappedFace.mouth.centerY() - mMouth.getHeight()/2;
		mDispLayout.requestLayout();
	}
	
	private FaceInfo mapFaceBmpToView(FaceInfo face) {
		FaceInfo mappedFace = new FaceInfo();
		RectF src = new RectF(face.face);
		RectF dst = new RectF();
		mMatImage.mapRect(dst, src);
		mappedFace.face = Util.RectFtoRect(dst);
		
		src.set(face.eye1);
		mMatImage.mapRect(dst, src);
		mappedFace.eye1 = Util.RectFtoRect(dst);
		
		src.set(face.eye2);
		mMatImage.mapRect(dst, src);
		mappedFace.eye2 = Util.RectFtoRect(dst);
		
		src.set(face.mouth);
		mMatImage.mapRect(dst, src);
		mappedFace.mouth = Util.RectFtoRect(dst);
		return mappedFace;
	}
	
	private FaceInfo mapFaceViewToBmp(FaceInfo face) {
		Matrix matView = new Matrix();
		mMatImage.invert(matView);
		FaceInfo mappedFace = new FaceInfo();
		RectF src = new RectF(face.face);
		RectF dst = new RectF();
		matView.mapRect(dst, src);
		mappedFace.face = Util.RectFtoRect(dst);
		
		src.set(face.eye1);
		matView.mapRect(dst, src);
		mappedFace.eye1 = Util.RectFtoRect(dst);
		
		src.set(face.eye2);
		matView.mapRect(dst, src);
		mappedFace.eye2 = Util.RectFtoRect(dst);
		
		src.set(face.mouth);
		matView.mapRect(dst, src);
		mappedFace.mouth = Util.RectFtoRect(dst);
		return mappedFace;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.face_button_back:
			setResult(RESULT_CANCELED);
			finish();
			 
			break;
		case R.id.face_button_done:
			onBtnDoneClick();
			break;
		default:
			break;
		}
//		MobclickAgent
//		.onEventEnd(this, "End to facePointMakeUp");
	}
	
	private float mDownX, mDownY;
	private int mMarginLeft, mMarginTop;
	private LayoutParams mTouchedParam = null;
	private View mTouchedView = null;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mDownY = event.getY();
			if(isPointInView(mDownX, mDownY, mEye1)) {
				mTouchedView = mEye1;
				mTouchedParam = (LayoutParams) mEye1.getLayoutParams();
				magniferview.setCircleResource(mBmpEye);
			} else if(isPointInView(mDownX, mDownY, mEye2)) {
				mTouchedView = mEye2;
				mTouchedParam = (LayoutParams) mEye2.getLayoutParams();
				magniferview.setCircleResource(mBmpEye);
			} else if(isPointInView(mDownX, mDownY, mMouth)) {
				mTouchedView = mMouth;
				mTouchedParam = (LayoutParams) mMouth.getLayoutParams();
				magniferview.setCircleResource(mBmpMouth);
			} else {
				return false;
			}
			mMarginLeft = mTouchedParam.leftMargin;
			mMarginTop = mTouchedParam.topMargin;
			mHelpView.setVisibility(View.GONE);
			mDispLayout.requestLayout();
			break;
		case MotionEvent.ACTION_MOVE:
			if(mTouchedParam==null) return false;
			mTouchedParam.leftMargin = mMarginLeft + (int)(event.getX()-mDownX);
			mTouchedParam.topMargin = mMarginTop + (int)(event.getY()-mDownY);
			ensurePointPosition();
			mDispLayout.requestLayout();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(mTouchedParam==null) return false;
			mTouchedParam = null;
			mTouchedView = null;
			break;
		default:
			break;
		}
		if(mTouchedParam!=null) {
			float x = mTouchedParam.leftMargin + mTouchedView.getWidth()/2.0f;
			float y = mTouchedParam.topMargin + mTouchedView.getHeight()/2.0f;
			event.setLocation(x, y);
		}
		return magniferview.DispachTouchEvent(event);
	}
	
	private void ensurePointPosition() {
		mTouchedParam.leftMargin = Math.max(mTouchedParam.leftMargin,
				(int)mRectImageDisp.left);
		mTouchedParam.leftMargin = Math.min(mTouchedParam.leftMargin,
				(int)mRectImageDisp.right - mTouchedView.getWidth());
		mTouchedParam.topMargin = Math.max(mTouchedParam.topMargin,
				(int)mRectImageDisp.top);
		mTouchedParam.topMargin = Math.min(mTouchedParam.topMargin,
				(int)mRectImageDisp.bottom - mTouchedView.getHeight());
	}
	
	private boolean isPointInView(float x, float y, View view) {
		LayoutParams params = (LayoutParams) view.getLayoutParams();
		return x > params.leftMargin &&
				y > params.topMargin &&
				x < params.leftMargin + view.getWidth() &&
				y < params.topMargin + view.getHeight();
	}
	
	private void onBtnDoneClick() {
		LayoutParams params = (LayoutParams) mEye1.getLayoutParams();
		int eye1_x = params.leftMargin + mEye1.getWidth()/2;
		int eye1_y = params.topMargin + mEye1.getHeight()/2;
		params = (LayoutParams) mEye2.getLayoutParams();
		int eye2_x = params.leftMargin + mEye2.getWidth()/2;
		int eye2_y = params.topMargin + mEye2.getHeight()/2;
		params = (LayoutParams) mMouth.getLayoutParams();
		int mouth_x = params.leftMargin + mMouth.getWidth()/2;
		int mouth_y = params.topMargin + mMouth.getHeight()/2;
		FaceInfo faceInView = FacePointUtil.createFace(eye1_x, eye1_y, eye2_x, eye2_y, mouth_x, mouth_y);
		FaceInfo faceInBmp = mapFaceViewToBmp(faceInView);
		FaceInfo[] faces = new FaceInfo[]{faceInBmp};
		mEditBitmap.setFaces(faces);
		LogUtil.logV(TAG, "Done Face:" + faces[0].face + " eye1:" + faces[0].eye1 + " eye2:" + faces[0].eye2 + " mouth:" + faces[0].mouth);
		setResult(RESULT_OK, getIntent());
		finish();
	}

}
