package com.cam001.photoeditor.beauty.detect;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class FaceDetect implements PreviewDetect{
	
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
		int count =native_detect(mHandle, nv21, width, height);
		if(count<1) {
			return null;
		}
		Rect[] res = new Rect[count];
		for(int i=0; i<count; i++) {
			Rect rect = new Rect();
			native_face_info(mHandle, i, rect, null, null, null);
			res[i] = rect;
		}
		return res;
	}
	
	public FaceInfo[] dectectFeatures(Bitmap bmp) {
		int count =native_detect(mHandle, bmp);
		FaceInfo[] res = new FaceInfo[count];
		for(int i=0; i<count; i++) {
			FaceInfo face = new FaceInfo();
			native_face_info(mHandle, i, face.face, face.eye1, face.eye2, face.mouth);
			if(face.mouth.left==0) { //Mouth detect failed.
				int faceWidth = face.face.width();
				int faceHeight = face.face.height();
				int mouthWidth = faceWidth*1/2;
				int mouthHeight = faceHeight*1/4;
				int mouthLeft = face.face.left + faceWidth*1/4;
				int mouthTop = face.face.top + faceHeight*13/20;
				face.mouth.set(mouthLeft, mouthTop, mouthLeft+mouthWidth, mouthTop+mouthHeight);
			}
			res[i] = face;
//			native_hair_info(mHandle, res[i].hair[0], res[i].hair[1], res[i].hair[2]);
		}
		return res;
	}
	
	private static native int native_create();
	private static native void native_destroy(int handle);
	private static native int native_detect(int handle, byte[] nv21, int width, int height);
	private static native int native_detect(int handle, Bitmap bmp);
	private static native int native_count(int handle);
	private static native int native_face_info(int handle, int index, Rect face, Rect eye1, Rect eye2, Rect mouth);
	private static native int native_hair_info(int handle, Rect hair1, Rect hair2, Rect hair3);
}
