package com.cam001.photoeditor.beauty.engine;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.cam001.photoeditor.beauty.detect.FaceDetect;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.detect.FacialMarksTrack;
import com.cam001.util.Util;

/**
 * Editor�õ���Bitmap�����ࡣ
 * ���÷���
 * ��Bitmap�����ô���ΪmBitmap�����ò�ͬ�ķ��������mBitmap��ָ���ͼ����ݡ�
 * Ϊ�˽�ʡVM�ڴ棬Java�������һ��ͼ����ݣ�������ݻ�����JNI�㡣
 * 
 * @author hlzhao
 */
public class EditBitmap {
	
	private static final int MAX_SAVE_COUNT = 1;
	
	private Bitmap mBitmap;
	private int mHandle;
	private int mHeight;
	private int mWidth;
	private int mSaveCount = 0;
	
	private FaceInfo[] mFaces = null;

	public EditBitmap(Bitmap bitmap) {
		mBitmap = bitmap.copy(Config.ARGB_8888, true);
		bitmap.recycle();
		mWidth = mBitmap.getWidth();
		mHeight = mBitmap.getHeight();
		mHandle = native_create(mBitmap);
		Util.Assert(mHandle != 0);
	}

	public FaceInfo[] getFaces() {
		if(mFaces==null) {
			setToOriginal(true);
			FaceDetect fd = new FaceDetect();
			fd.initialize();
			mFaces = fd.dectectFeatures(mBitmap);
			fd.uninitialize();
			FacialMarksTrack md  = new FacialMarksTrack();
			md.initialize();
			md.dectectFeatures(mBitmap, mFaces);
			md.uninitialize();
			setToOriginal(false);
		}
		return mFaces;
	}
	
	public boolean updateOutline() {
		if(mFaces!=null && mFaces[0]!=null&&mFaces[0].needRefreshOutline) {
			setToOriginal(true);
			FacialMarksTrack md  = new FacialMarksTrack();
			md.initialize();
			md.updateFeatures(mBitmap, mFaces[0]);
			md.uninitialize();
			setToOriginal(false);
			mFaces[0].needRefreshOutline = false;
			return true;
		}
		return false;
	}
	
	public void setFaces(FaceInfo[] faces) {
		mFaces = faces;
	}
	
	public Bitmap getBitmap() {
		return this.mBitmap;
	}

	public int getHeight() {
		return this.mHeight;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public void recycle() {
		if (this.mBitmap != null) {
			this.mBitmap.recycle();
			this.mBitmap = null;
		}
		if(mHandle!=0) {
			native_destroy(mHandle);
			mHandle = 0;
		}
	}
	
	public void apply(Bitmap bmp) {
		native_apply(mHandle, bmp);
	}
	
	/**
	 * ��Bitmap��ݻָ�����ʼ����������ʱ����ݣ�
	 * @param bOrignal
	 */
	public void setToOriginal (boolean bOrignal) {
		native_original(mHandle, bOrignal);
	}
	
	/**
	 * Reset the bitmap to original.
	 */
	public void reset() {
		native_reset(mHandle);
	}
	
	/**
	 * Save the current bitmap and can be restored.
	 */
	public void save() {
		if(mSaveCount>=MAX_SAVE_COUNT) {
			throw new RuntimeException("Can not save: save count exceed limit.");
		}
		mSaveCount ++;
		native_save(mHandle);
	}
	
	/**
	 * Restore the saved bitmap.
	 */
	public void restore() {
		if(mSaveCount==0) {
			throw new RuntimeException("Can not restore: save count is 0.");
		}
		mSaveCount --;
		native_restore(mHandle);
	}
	
	private static native int native_create(Bitmap bmp);
	private static native void native_destroy(int handle);
	private static native void native_reset(int handle);
	private static native void native_original(int handle, boolean bOrig);
	private static native void native_apply(int handle, Bitmap bmp);
	private static native void native_save(int handle);
	private static native void native_restore(int handle);
}