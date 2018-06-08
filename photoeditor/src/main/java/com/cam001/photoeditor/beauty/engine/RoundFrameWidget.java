package com.cam001.photoeditor.beauty.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.view.MotionEvent;


public class RoundFrameWidget implements Widget{

	private Bitmap mBmpDisp = null;
	private Bitmap mBmpFram = null;
	private RectF mRectDisp = null;
	
	private Path mPathRound = null;

	private Paint mPntFram = null;
	private Paint mPntDisp = null;
	
	private int mBorderWeight = 0;
	private int mBorderRadius = 0;
	
	public RoundFrameWidget() {
	}
	
	public void setDisplayBitmap(Bitmap disp) {
		mBmpDisp = disp;
	}
	
	public void setDisplayRect(RectF rect) {
		mRectDisp = rect;
		refreshBorderPath();
	}
	
	public void setFrameBitmap(Bitmap frame) {
		mBmpFram = frame;
		if(frame==null) {
			mPntFram = null;
			return;
		}

		mPntFram = new Paint();
		BitmapShader shader = new BitmapShader(frame, TileMode.REPEAT, TileMode.REPEAT);
		mPntFram.setShader(shader);
		
		mPntDisp = new Paint();
		mPntDisp.setAntiAlias(true);
		mPntDisp.setStyle(Style.STROKE);
		mPntDisp.setColor(Color.LTGRAY);
	}
	
	public Bitmap getFrameBitmap() {
		return mBmpFram;
	}
	
	public int getFrameBorderWeight() {
		return mBorderWeight;
	}
	
	public int getFrameBorderRadius() {
		return mBorderRadius;
	}
	
	/**
	 * Set border size and round radius, from 0 to 100.
	 */
	public void setFrameBorder(int weight, int radius) {
		if(mBorderWeight==weight && mBorderRadius==radius) {
			return;
		}
		mBorderWeight = weight;
		mBorderRadius = radius;
		refreshBorderPath();
	}
	
	/**
	 * Set border size, from 0 to 100.
	 */
	public void setFrameBorderWeight(int weight) {
		mBorderWeight = weight;
		refreshBorderPath();
	}
	
	/**
	 * Set round radius, from 0 to 100.
	 */
	public void setFrameBorderRadius(int radius) {
		mBorderRadius = radius;
		refreshBorderPath();
	}
	
	private void refreshBorderPath() {
		if(mPathRound==null) {
			mPathRound = new Path();
		} 
		mPathRound.reset();
		RectF rect = new RectF();
		float boderWeight = Math.min(mRectDisp.width(), mRectDisp.height());
		boderWeight = boderWeight/2.0f*mBorderWeight/100.0f;
		rect.left = mRectDisp.left + boderWeight;
		rect.top = mRectDisp.top + boderWeight;
		rect.right = mRectDisp.right - boderWeight;
		rect.bottom = mRectDisp.bottom - boderWeight;
		float boderRadius = Math.max(mRectDisp.width(), mRectDisp.height());
		boderRadius = boderRadius/2.0f*mBorderRadius/100.0f;
		mPathRound.addRoundRect(rect, boderRadius, boderRadius, Path.Direction.CW);
	}
	
	public void reset() {
		setFrameBorder(0, 0);
		mPathRound = null;
		setFrameBitmap(null);
	}
	
	@Override
	public void draw(Canvas canvas) {
		if(mBmpFram==null) { //No Frame
			if(mBmpDisp!=null && !mBmpDisp.isRecycled()) {
				canvas.drawBitmap(mBmpDisp, null, mRectDisp, mPntDisp);
			}
			return;
		}
		//Has Frame
		canvas.save();
		if(mPntFram!=null) {
			canvas.drawRect(mRectDisp,mPntFram);
		}
		if(mPathRound!=null) {
			canvas.clipPath(mPathRound);
		}
		if(mBmpDisp!=null) {
			canvas.drawBitmap(mBmpDisp, null, mRectDisp, mPntDisp);
		}
		canvas.restore();
		if(mPathRound!=null && mPntDisp!=null) {
			canvas.drawPath(mPathRound, mPntDisp);
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return false;
	}

}
