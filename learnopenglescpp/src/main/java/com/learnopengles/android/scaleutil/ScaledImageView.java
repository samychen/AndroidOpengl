package com.learnopengles.android.scaleutil;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ScaledImageView extends ImageView 
	implements MatrixAnimation.OnRefreshListener{

	private static final float MAX_ZOOM = 3.0f;
	private static final int MAX_DCLICK_TIME = 300;
	private static int MAX_DCLICK_DIS = 50;
	private static boolean sNeedDipToPix = true;
	
	protected Matrix mMatCanvas = null;

	protected float mLastX0,mLastY0,mLastX1,mLastY1;
	protected float mDownX,mDownY;
	private boolean mIsSingleTouch = false;
	private long mLastTapTime = 0;
	protected MatrixAnimation mAnim = null;
	
	private Handler mHandler = new Handler();
	
	protected int mImgWidth,mImgHeight;
	private String TAG = "ScaledImageView";

	public ScaledImageView(Context context) {
		super(context);
		init();
	}
	
	public ScaledImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mMatCanvas = new Matrix();
		mAnim = new MatrixAnimation();
		if(sNeedDipToPix) {
			MAX_DCLICK_DIS = DensityUtil.dip2px(getContext(), MAX_DCLICK_DIS);
			sNeedDipToPix = false;
		}
	}

	@Override
	public void setImageBitmap(Bitmap bmp) {
		super.setImageBitmap(bmp);
		mImgWidth = bmp.getWidth();
		mImgHeight = bmp.getHeight();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.e(TAG, "onDraw: " +mMatCanvas.toShortString());
		canvas.concat(mMatCanvas);
		super.onDraw(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mAnim.isAnimating()) return false;
		if(super.onTouchEvent(event)) {
			return true;
		}
		boolean bHandled = false;
		int pCount = event.getPointerCount();
		switch(pCount) {
		case 1:
			if(event.getAction()==MotionEvent.ACTION_DOWN) {
				mIsSingleTouch = true;
			} else if(!mIsSingleTouch) {
				return false;
			}
			bHandled = handleSingleTouhEvent(event);
			break;
		case 2:
			if(mIsSingleTouch) {
				mIsSingleTouch = false;
			}
			bHandled = handleMultiTouchEvent(event);
			break;
		default:
			break;
		}
		if(bHandled) invalidate();
		return bHandled;
	}
	
	protected boolean handleSingleTouhEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(event.getDownTime()-mLastTapTime<MAX_DCLICK_TIME
					&& Math.abs(mLastX0-event.getX(0))<MAX_DCLICK_DIS
					&& Math.abs(mLastY0-event.getY(0))<MAX_DCLICK_DIS) {
				mHandler.removeCallbacks(mRunSingleClick);
				handleDoubleClick();
				return true;
			}
			mLastTapTime = event.getDownTime();
			mLastX0 = mDownX = event.getX(0);
			mLastY0 = mDownY = event.getY(0);
			return true;
		case MotionEvent.ACTION_MOVE:
			float x0 = event.getX(0);
			float y0 = event.getY(0);			
			float offsetX = x0-mLastX0;
			float offsetY = y0-mLastY0;
			mMatCanvas.postTranslate(offsetX, offsetY);
			mLastX0 = x0;
			mLastY0 = y0;
			return true;
		case MotionEvent.ACTION_UP:
			ensureTransform();
			if(event.getEventTime()-event.getDownTime()<MAX_DCLICK_TIME
					&& Math.abs(mDownX-event.getX(0))<MAX_DCLICK_DIS
					&& Math.abs(mDownY-event.getY(0))<MAX_DCLICK_DIS) {
				mHandler.postDelayed(mRunSingleClick, MAX_DCLICK_TIME);
			}
			return true;
		}
		return false;
	}
	
	protected boolean handleMultiTouchEvent(MotionEvent event) {
		switch(event.getAction()& MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_POINTER_DOWN:
			mLastX0 = event.getX(0);
			mLastY0 = event.getY(0);
			mLastX1 = event.getX(1);
			mLastY1 = event.getY(1);
			break;
		case MotionEvent.ACTION_MOVE:
			float x0 = event.getX(0);
			float y0 = event.getY(0);
			float x1 = event.getX(1);
			float y1 = event.getY(1);
			
			float offsetX = (x0-mLastX0 + x1-mLastX1)/2.0f;
			float offsetY = (y0-mLastY0 + y1-mLastY1)/2.0f;
			mMatCanvas.postTranslate(offsetX, offsetY);
			float scale = getScale(x0, y0, x1, y1);
			mMatCanvas.postScale(scale, scale, (x0+x1)/2, (y0+y1)/2);
			scale = getScale(mMatCanvas);
			if(scale>MAX_ZOOM) {
				scale = MAX_ZOOM/scale;
				mMatCanvas.postScale(scale, scale, (x0+x1)/2, (y0+y1)/2);
			}
			
			mLastX0 = x0;
			mLastY0 = y0;
			mLastX1 = x1;
			mLastY1 = y1;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			ensureTransform();
			break;
		default:
			break;
		}
		return true;
	}
	
	protected boolean handleDoubleClick() {
		if(mMatCanvas.isIdentity()) {
			Matrix mat = new Matrix(mMatCanvas);
			mat.setScale(MAX_ZOOM, MAX_ZOOM, getWidth()/2f, getHeight()/2f);
			transformTo(mat);
		} else {
			transformToIdentify();
		}
		return true;
	}
	
	private Runnable mRunSingleClick = new Runnable(){
		@Override
		public void run() {
			handleSingleClick();
		}
	};
	
	protected boolean handleSingleClick() {
		return false;
	}
	
	private void ensureTransform() {
		float[] pos = new float[]{0, 0, getWidth(), getHeight()};
		mMatCanvas.mapPoints(pos);
		System.out.println("l="+pos[0]+" t="+pos[1]+" r="+pos[2]+" b="+pos[3]);
		if(getWidth()>(pos[2]-pos[0])) {
			transformToIdentify();
			return;
		}
		float offsetX = 0.0f;
		float offsetY = 0.0f;
		if(pos[0]>0) {
			offsetX = -pos[0];
		} else if(pos[2]<getWidth()) {
			offsetX = getWidth() - pos[2];
		}
		if(pos[1]>0) {
			offsetY = -pos[1];
		} else if(pos[3]<getHeight()){
			offsetY = getHeight() - pos[3];
		}
		if(Math.abs(offsetX)>0f || Math.abs(offsetY)>0f) {
			Matrix dst = new Matrix(mMatCanvas);
			dst.postTranslate(offsetX, offsetY);
			transformTo(dst);
		}
		Log.e(TAG, "ensureTransform: "+mMatCanvas.toShortString() );
	}
	
	private float getScale(float x0, float y0, float x1, float y1) {
		double o = Math.sqrt((mLastX0-mLastX1)*(mLastX0-mLastX1) +(mLastY0-mLastY1)*(mLastY0-mLastY1));
		double n = Math.sqrt((x0-x1)*(x0-x1) +(y0-y1)*(y0-y1));
		return (float)(n/o);
	}

	public boolean onBackPressed() {
		if(mAnim.isAnimating()) return true;
		if(!mMatCanvas.isIdentity()) {
			transformToIdentify();
			return true;
		}
		return false;
	}
	
	public float getScale(Matrix m) {
		float[] v = new float[9];
		m.getValues(v);
		return v[0];
	}
	
	public void transformToIdentify() {
		Matrix matDst = new Matrix();
		transformTo(matDst);
	}
	
	public void transformTo(float x, float y) {
		Matrix mat = new Matrix();
		float[] tmp = new float[]{x, y, mImgWidth/2.0f, mImgHeight/2.0f};
		getImageMatrix().mapPoints(tmp);
		mat.postTranslate(tmp[2]-tmp[0], tmp[3]-tmp[1]);
		mat.postScale(MAX_ZOOM, MAX_ZOOM, getWidth()/2.0f, getHeight()/2.0f);
		transformTo(mat);
	}
	
	private void transformTo(Matrix matDst) {
		Log.e(TAG, "transformTo: "+matDst.toShortString() );
		mAnim.startAnimation(mMatCanvas, matDst, this);
	}

	public Matrix getScaleMatrix() {
		return mMatCanvas;
	}

	@Override
	public void onRefresh(Matrix mat) {
		Log.e(TAG, "onRefresh: mat="+mat.toShortString() );
		postInvalidate();
	}
}
