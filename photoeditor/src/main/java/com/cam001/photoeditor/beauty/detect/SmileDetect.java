package com.cam001.photoeditor.beauty.detect;

import android.graphics.Rect;

public class SmileDetect implements PreviewDetect {


	private int mHandle = 0;
	private Rect[] mFaces = null;
	
	@Override
	public void initialize() {
		mHandle = native_create();
	}

	@Override
	public void uninitialize() {
		native_destroy(mHandle);
	}

	public void setFaces(Rect[] faces) {
		mFaces = faces;
	}
	
	@Override
	public Rect[] detect(byte[] nv21, int width, int height) {
		int count = native_detect(mHandle, nv21, width, height, mFaces);
		if(count<1) {
			return null;
		}
		Rect[] res = new Rect[count];
		for(int i=0; i<count; i++) {
			Rect rect = new Rect();
			native_smile_info(mHandle, i, rect);
			res[i] = rect;
		}
		return res;
	}

	
	private static native int native_create();
	private static native void native_destroy(int handle);
	private static native int native_detect(int handle, byte[] nv21, int width, int height, Rect[] faces);
	private static native int native_count(int handle);
	private static native int native_smile_info(int handle, int index, Rect rect);

}
