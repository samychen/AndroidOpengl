package com.cam001.photoeditor.beauty.detect;

import android.graphics.Rect;

public interface OnDetectListener {
	void onDetectFace(Rect[] faces);
	void onDetectGesture(Rect[] faces);
	void onDetectSmile(Rect[] faces);
	void onDetectSound(float volumn, int interval);
}
