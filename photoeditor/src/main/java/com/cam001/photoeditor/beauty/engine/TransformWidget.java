package com.cam001.photoeditor.beauty.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class TransformWidget implements Widget{
	
	protected Bitmap mBmpDisp = null;
	protected Matrix mMatDisp = null;
	
	protected float mCenterX = 0.0f;
	protected float mCenterY = 0.0f;
	
	protected RectF mRectSrc = null;
//	protected RectF mRectDst = null;
	
	protected RectF mRecDisp = null;
	protected Paint mPaint = null;
	
	public TransformWidget(Bitmap display) {
		if(display==null) {
			throw new NullPointerException();
		}
		mBmpDisp = display;
		mMatDisp = new Matrix();
		mRectSrc = new RectF(0.0f, 0.0f, mBmpDisp.getWidth(), mBmpDisp.getHeight());
//		mRectDst = new RectF(mRectSrc);
		mCenterX = mRectSrc.centerX();
		mCenterY = mRectSrc.centerY();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		if(mRecDisp!=null) {
			canvas.clipRect(mRecDisp);
		}
		canvas.drawBitmap(mBmpDisp, mMatDisp, mPaint);
		canvas.restore();
	}

	public void setDisplayRect(RectF rect) {
		if(mRecDisp!=null) {
			Matrix m = new Matrix();
			m.setRectToRect(mRecDisp, rect, ScaleToFit.FILL);
			mMatDisp.postConcat(m);
		}
		mRecDisp = rect;
	}
	
	public void move(float x, float y) {
		mMatDisp.postTranslate(x, y);
		mCenterX += x;
		mCenterY += y;
	}

	public void rotate(float degrees) {
		mMatDisp.postRotate(degrees,mCenterX,mCenterY);
	}
	
	public void scale(float x, float y) {
		mMatDisp.postScale(x, y, mCenterX,mCenterY);
	}
	
	public boolean isPointInDisp(float x, float y) {
		Matrix inverse = new Matrix();
		mMatDisp.invert(inverse);
//		mMatDisp.mapRect(mRectDst, mRectSrc);
		float[] points = new float[]{x, y};
		inverse.mapPoints(points);
		return mRectSrc.contains(points[0], points[1]);
	}
	
}
