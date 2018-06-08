package com.cam001.photoeditor.beauty.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

public abstract class GLTransformWidget extends TransformWidget{

	public GLTransformWidget(Bitmap display) {
		super(display);
	}

	@Override
	public void draw(Canvas canvas) {
		
	}
	
	@Override
	public void setDisplayRect(RectF rect) {
		
	}
	
	@Override
	public void move(float x, float y) {
		
	}
	
	@Override
	public void rotate(float degrees) {
		
	}
	
	@Override
	public void scale(float x, float y) {
		
	}
	
	@Override
	public boolean isPointInDisp(float x, float y) {
		return false;
	}
	
	
	private static native void native_draw();
	private static native void native_move(float x, float y);
	private static native void native_rotate(float degrees);
	private static native void native_scale(float x, float y);
	private static native boolean native_isPointInDisp(float x, float y);
	
}
