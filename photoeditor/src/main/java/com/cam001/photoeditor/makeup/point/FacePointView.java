package com.cam001.photoeditor.makeup.point;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.cam001.photoeditor.R;
import com.cam001.widget.ScaledImageView;

public class FacePointView extends ScaledImageView{

	public static interface OnFacePointChangeListenter {
		void onFacePointChange(float[] facepoints);
		void onFacePointDown(int index);
		void onFacePointUp(int index);
		void onFacePointMove(float x, float y);
	}
	
	private Bitmap mBmpPoint = null, mBmpCurrentPoint = null;;
	
	private float[] mFacePoint = null;
	protected boolean mbShow  = true;
	private Matrix mMatPoint = null;
	
	private float[] mMappedFacePoint = null;
	private int mDownPointIndex = -1;
	
	protected TsMagnifierView mZoomer = null;
	private OnFacePointChangeListenter mlFaceChange = null;
	private Paint mPaint = null;
	
	public FacePointView(Context context) {
		super(context);
		loadResource();
	}
	
	public FacePointView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadResource();
	}
	
	public void setOnFacePointChangeListener(OnFacePointChangeListenter l) {
		mlFaceChange = l;
	}
	
	private void loadResource() {
		if(mBmpPoint==null) {
			mBmpPoint = BitmapFactory.decodeResource(getResources(), R.drawable.face_point);
		}
		if(mBmpCurrentPoint==null) {
			mBmpCurrentPoint = BitmapFactory.decodeResource(getResources(), R.drawable.select_point);
		}
		if(mZoomer==null) {
			mZoomer = new TsMagnifierView(getContext());
			mZoomer.setDisplayView(this);
			mZoomer.onSizeChanged(getWidth(), getHeight(), 0, 0);
			mZoomer.setCircleResource(mBmpPoint);
		}
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}
	
	public void setFacePoints(float[] points) {
		mFacePoint = points;
		mMappedFacePoint = new float[points.length];
		mapFaceToPoints();
	}
	
	public float[] getFacePoints() {
		mapPointsToFace();
		return mFacePoint;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
		if(!mbShow) return;
		drawFacePoint(canvas);
		mZoomer.DoDraw(canvas);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(mZoomer!=null) {
			mZoomer.onSizeChanged(w, h, oldw, oldh);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private void drawFacePoint(Canvas c) {
		if(mDownPointIndex>-1) {
			//Do not modify the points.
		} else {
			mapFaceToPoints();
		}
		int w = 10;
		int h = 10;
//		int w = mBmpPoint.getWidth();
//		int h = mBmpPoint.getHeight();
		float scale = getScale(mMatCanvas);
		w *= scale;
		h *= scale;
		float dx = w/2.0f;
		float dy = h/2.0f;
		int pointCount = mMappedFacePoint.length/2;
		for(int i=0; i<pointCount; i++) {
//			c.drawBitmap(mBmpPoint, mMappedFacePoint[2*i]-dx, mMappedFacePoint[2*i+1]-dy, null);
//			c.drawBitmap(mBmpPoint, null, new RectF(mMappedFacePoint[2*i]-dx, mMappedFacePoint[2*i+1]-dy, mMappedFacePoint[2*i]+dx, mMappedFacePoint[2*i+1]+dy), null);
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(Color.parseColor("#f3cd00"));
			mPaint.setStrokeWidth(0.0f);
			c.drawCircle(mMappedFacePoint[2*i], mMappedFacePoint[2*i+1], dx, mPaint);
			mPaint.setColor(Color.WHITE);
			mPaint.setStyle(Style.STROKE);
			mPaint.setStrokeWidth(2.0f*scale);
			c.drawCircle(mMappedFacePoint[2*i], mMappedFacePoint[2*i+1], dx, mPaint);
		}
	}
	public void drawFacePointForMagnifier(Canvas c) {
		if(mDownPointIndex>-1) {
			//Do not modify the points.
		} else {
			mapFaceToPoints();
		}
//		float dx = 10.0f;
//		float dy = 10.0f;
		float dx = mBmpPoint.getWidth()/2.0f;
		float dy = mBmpPoint.getHeight()/2.0f;
		int pointCount = mMappedFacePoint.length/2;
		c.save();
		c.scale(0.5f, 0.5f);
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);
		for(int i=0; i<pointCount; i++) {
			if(mDownPointIndex == i){
//				c.drawBitmap(mBmpCurrentPoint, 2*(mMappedFacePoint[2*i])-dx, 2*(mMappedFacePoint[2*i+1])-dy, null);
			}
			else{
//				paint.setStyle(Style.FILL);
//				paint.setColor(Color.parseColor("#f3cd00"));
//				paint.setStrokeWidth(0.0f);
//				c.drawCircle(mMappedFacePoint[2*i], mMappedFacePoint[2*i+1], dx, paint);
//				paint.setColor(Color.WHITE);
//				paint.setStyle(Style.STROKE);
//				paint.setStrokeWidth(2.0f*2);
//				c.drawCircle(mMappedFacePoint[2*i], mMappedFacePoint[2*i+1], dx, paint);				
				c.drawBitmap(mBmpPoint, 2*(mMappedFacePoint[2*i])-dx, 2*(mMappedFacePoint[2*i+1])-dy, null);
			}
		}
		c.restore();
	}
	
	private void mapFaceToPoints() {
		mMatPoint = new Matrix(getImageMatrix());
		mMatPoint.postConcat(mMatCanvas);
		mMatPoint.mapPoints(mMappedFacePoint, mFacePoint);
	}
	
	private void mapPointsToFace() {
		Matrix matInverse = new Matrix();
		mMatPoint.invert(matInverse);
		matInverse.mapPoints(mFacePoint, mMappedFacePoint);
	}
	
	/**
	 * Ensure the eye point in the range of the bitmap.
	 * @param index
	 */
	private void ensurePointInBitmap(int index) {
		float dx, dy;
		float x, y;
		x = mFacePoint[2*index];
		y = mFacePoint[2*index+1];
		if(x<0) dx = -x;
		else if(x>mImgWidth) dx = mImgWidth-x;
		else dx = 0;
		if(y<0) dy = -y;
		else if(y>mImgHeight) dy = mImgHeight-y;
		else dy = 0;
		mFacePoint[2*index] += dx;
		mFacePoint[2*index+1] += dy;
	}
	
	@Override
	protected boolean handleSingleTouhEvent(MotionEvent event) {
		if (mbShow) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownPointIndex = inFacePoint(event.getX(0), event.getY(0));
				if(mDownPointIndex<0) break;
				mLastX0 = event.getX(0);
				mLastY0 = event.getY(0);
				mbShow = false;
				event.setLocation(mMappedFacePoint[2*mDownPointIndex], mMappedFacePoint[2*mDownPointIndex+1]);
				mZoomer.DispachTouchEvent(event);
				mbShow = true;
				if(mlFaceChange!=null) {
					mlFaceChange.onFacePointDown(mDownPointIndex);
					mlFaceChange.onFacePointMove(mLastX0, mLastY0);
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				if(mDownPointIndex<0) {
					break;
				}
				mMappedFacePoint[2*mDownPointIndex] += event.getX(0)-mLastX0;
				mMappedFacePoint[2*mDownPointIndex+1] += event.getY(0)-mLastY0;
				mLastX0 = event.getX(0);
				mLastY0 = event.getY(0);
				event.setLocation(mMappedFacePoint[2*mDownPointIndex], mMappedFacePoint[2*mDownPointIndex+1]);
				mZoomer.DispachTouchEvent(event);
				if(mlFaceChange!=null) {
					mlFaceChange.onFacePointMove(mLastX0, mLastY0);
				}
				return true;
			case MotionEvent.ACTION_UP:
				if(mDownPointIndex<0) {
					break;
				}
				mapPointsToFace();
				ensurePointInBitmap(mDownPointIndex);
				if(mlFaceChange!=null) {
					mlFaceChange.onFacePointUp(mDownPointIndex);
					mlFaceChange.onFacePointChange(mMappedFacePoint);
					mlFaceChange.onFacePointMove(mLastX0, mLastY0);
				}
				event.setLocation(mMappedFacePoint[2*mDownPointIndex], mMappedFacePoint[2*mDownPointIndex+1]);
				mZoomer.DispachTouchEvent(event);
				mDownPointIndex = -1;
				return true;
			}
		}
		return super.handleSingleTouhEvent(event);
	}
	
	@Override
	protected boolean handleMultiTouchEvent(MotionEvent event) {
		if(mDownPointIndex>-1) {				
			if(mlFaceChange!=null) {
				mlFaceChange.onFacePointUp(mDownPointIndex);
			}
			MotionEvent e = MotionEvent.obtain(event);
			e.setAction(MotionEvent.ACTION_UP);
			mZoomer.DispachTouchEvent(e);
			mDownPointIndex = -1;
		}
		return super.handleMultiTouchEvent(event);
	}
	
//	@Override
//	protected boolean handleSingleClick() {
//		return true;
//	}
	
	private int inFacePoint(float x, float y) {
		int res = -1;
		int pointCount = mMappedFacePoint.length/2;
		float distance = Integer.MAX_VALUE;
		for(int i=0; i<pointCount; i++) {
			boolean isIn = new RectF(mMappedFacePoint[2*i]-mBmpPoint.getWidth()/2.0f, 
					mMappedFacePoint[2*i+1]-mBmpPoint.getHeight()/2.0f,
					mMappedFacePoint[2*i]+mBmpPoint.getWidth()/2.0f,
					mMappedFacePoint[2*i+1]+mBmpPoint.getHeight()/2.0f)
			.contains(x, y);
			if(isIn) {
				float dis = (mMappedFacePoint[2*i]-x)*(mMappedFacePoint[2*i]-x)
						+ (mMappedFacePoint[2*i+1]-y)*(mMappedFacePoint[2*i+1]-y);
				if(dis<distance) {
					res = i;
					distance = dis;
				}
			}
		}
		return res;
	}
	
}
