package com.cam001.photoeditor.beauty.detect;


import android.graphics.Bitmap;
import android.graphics.Rect;

import com.cam001.util.LogUtil;

public class FaceTrack implements PreviewDetect{

	private int mHandle = 0;
	
	@Override
	public void initialize() {
		mHandle = native_create();
	}

	@Override
	public void uninitialize() {
		native_destroy(mHandle);
	}

	@Override
	public Rect[] detect(byte[] nv21, int width, int height) {
		LogUtil.startLogTime("FaceTrack detect");
		native_detect(mHandle, nv21, width, height);
		int count = native_count(mHandle);
		if(count<1) {
			return null;
		}
		Rect[] res = new Rect[count];
		for(int i=0; i<count; i++) {
			Rect rect = new Rect();
			native_face_info(mHandle, i, rect, null, null, null);
			res[i] = rect;
		}
		LogUtil.stopLogTime("FaceTrack detect");
		return res;
	}
	
	public FaceInfo[] dectectFeatures(Bitmap bmp) {
		native_detect(mHandle, bmp);
		int count = native_count(mHandle);
		FaceInfo[] res = new FaceInfo[count];
		for(int i=0; i<count; i++) {
			FaceInfo face = new FaceInfo();
			native_face_info(mHandle, i, face.face, face.eye1, face.eye2, face.mouth);
			res[i] = face;
		}
		return res;
	}
	
	private static native int native_create();
	private static native void native_destroy(int handle);
	private static native void native_detect(int handle, byte[] nv21, int width, int height);
	private static native void native_detect(int handle, Bitmap bmp);
	private static native int native_count(int handle);
	private static native int native_face_info(int handle, int index, Rect face, Rect eye1, Rect eye2, Rect mouth);

}
