package com.cam001.photoeditor.beauty.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.MotionEvent;

import com.cam001.util.BitmapUtil;


public class EditGLEngine extends EditEngine{

	protected EditGLEngine(Context context) {
		super(context);
	}

	@Override
	public boolean loadImage(Uri uri) {
		if(mEditBmp!=null) {
			mEditBmp.recycle();
			mEditBmp = null;
		}
		try {
			Bitmap bmp = BitmapUtil.getBitmap(uri, mContext, MAX_IMAGE_WIDTH, MAX_IMAGE_WIDTH);
			mEditBmp = new EditBitmap(bmp);
//			Bitmap stampCtrl = BitmapFactory.decodeResource(mContext.getResources(), RES_STAMP_CTRL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static void initGL() {
		initialize();
	}
	
	public static void uninitGL() {
		uninitialize();
	}
	
	@Override
	public void setDispViewSize(int width, int height) {
		setViewSize(width, height);
	}
	
	@Override
	public void addStamp(int resid) {
		Bitmap stamp = BitmapFactory.decodeResource(mContext.getResources(), resid);
		mStamp = stamp;
	}
	
	private Bitmap mStamp = null;
	
	@Override
	public void draw(Canvas canvas) {
		if(mEditBmp!=null) {
			native_loadImage(mEditBmp.getBitmap());
			mEditBmp.recycle();
			mEditBmp = null;
		}
		if(mStamp!=null) {
			addDecoration(mStamp);
			mStamp.recycle();
			mStamp = null;
		}
		draw();
	}
	
	private float mLastX,mLastY;
	private boolean mbTouchCtrl = false;
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastX = event.getX();
			mLastY = event.getY();
			if(isPointInWidget(mLastX, mLastY)) {
				bHandled = true;
				mbTouchCtrl = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(mbTouchCtrl) {
			float x = event.getX();
			float y = event.getY();
			translate(x-mLastX, y-mLastY);
			mLastX = x;
			mLastY = y;
			mbTouchCtrl = true;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(mbTouchCtrl) {
				mbTouchCtrl = false;
				bHandled = true;
			}
			break;
		}
		return bHandled;
	}
	
	static {
		System.loadLibrary("edit_engine_jni");
	}
	
	private static native void initialize();
	private static native void uninitialize();
	private static native void native_loadImage(Bitmap image);
	private static native void setViewSize(int width, int height);
	private static native void draw();
	private static native void addDecoration(Bitmap image);
	private static native void translate(float x, float y);
	private static native boolean isPointInWidget(float x, float y);
}