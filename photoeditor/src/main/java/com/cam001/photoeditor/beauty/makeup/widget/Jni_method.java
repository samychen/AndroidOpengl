package com.cam001.photoeditor.beauty.makeup.widget;

import android.graphics.Bitmap;

public class Jni_method {
	static{
		
		System.loadLibrary("glimageview");
	}
	
	public static native void	loadTexture(Bitmap bmp, int id, float[] normalsize);
	public static native void	copyScreen(int x, int y, int width, int height, Bitmap bmp);
}