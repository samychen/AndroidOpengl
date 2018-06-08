package com.cam001.photoeditor.makeup.engine;

import android.graphics.Bitmap;
import android.hardware.Camera.CameraInfo;
import android.util.Log;

import com.cam001.photoeditor.beauty.detect.NV21Sampler;

public class TsViewEngine {
	static {
		System.loadLibrary("tsview_jni");
	}
	private int mHandle = 0;
	private static TsViewEngine mTsViewEngine = null;
	private NV21Sampler mNV21Sampler = null;

	public static TsViewEngine getInstance() {
		if (mTsViewEngine == null)
			mTsViewEngine = new TsViewEngine();
		return mTsViewEngine;
	}

	private TsViewEngine() {

	}

	public void initHandle(int nWidth, int nHeight) {
		mNV21Sampler = new NV21Sampler();
		mHandle = init(nWidth, nHeight);
		Log.d("Engine", ""+mHandle);
	}

	public void uninitHandle() {
		Log.d("Engine", ""+mHandle);
		uninit(mHandle);
		mNV21Sampler.destroy();
	}

	private byte[] mTransformBuff = null;

	public int shiftracking(byte[] data, int width, int height,
			int devorientation, int disorientation, int cameraId) {
		int size = width * height * 3 / 2;
		if (mTransformBuff == null || mTransformBuff.length < size) {
			mTransformBuff = new byte[size];
		}
		int rotation = 0;
		if (cameraId == CameraInfo.CAMERA_FACING_FRONT)
			rotation = (720 - devorientation - disorientation) % 360;
		else
			rotation = (devorientation + disorientation) % 360;
		mNV21Sampler.downSample(data, width, height, mTransformBuff, 1,
				rotation);
		if (rotation % 180 != 0) {
			width = width + height;
			height = width - height;
			width = width - height;
		}
		return shiftracking(mTransformBuff, width, height);
	}

	public int shiftracking(byte[] data, int width, int height) {
		return shiftracking(mHandle, data, width, height);
	}

	public void tsWork() {
		tsWork(mHandle);
	}

	public int[] getInfo() {
		int[] results = new int[3];
		getInfo(mHandle, results);
		return results;
	}

	public void getFrame(int index, Bitmap bmp) {
		getFrame(mHandle, index, bmp);
	}

	private static native int init(int width, int height);

	private static native int uninit(int handle);

	private static native int shiftracking(int handle, byte[] nv21, int width,
			int height);

	private static native int tsWork(int handle);

	private static native int getInfo(int handle, int[] infos);

	private static native int getFrame(int handle, int index, Bitmap bmp);
}
