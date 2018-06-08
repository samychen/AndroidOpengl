package com.cam001.photoeditor.beauty.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.RectF;
import android.view.MotionEvent;

public class CoverWidget implements Widget{

	private RectF mRectDisp = null;
	private Bitmap mBmpDisp = null;
	private NinePatch mNinePatch = null;
	
	public CoverWidget() {
	}
	
	public void setCoverRect(RectF disp) {
		mRectDisp = disp;
	}
	
	public void setCover(Bitmap disp) {
		if(mBmpDisp!=null) {
			mBmpDisp.recycle();
		}
		mBmpDisp = disp;
		byte[] chunck = disp==null?null:disp.getNinePatchChunk();
		if(chunck!=null) {
			mNinePatch = new NinePatch(disp, chunck, null);
		}
	}
	
	public Bitmap getCover() {
		return mBmpDisp;
	}
	
	public void reset() {
		if(mBmpDisp!=null) {
			mBmpDisp.recycle();
		}
		mBmpDisp = null;
	}
	
	@Override
	public void draw(Canvas canvas) {
		if(mNinePatch!=null) {
			mNinePatch.draw(canvas, mRectDisp);
		} else if(mBmpDisp!=null) {
			canvas.drawBitmap(mBmpDisp, null, mRectDisp, null);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return false;
	}

}
