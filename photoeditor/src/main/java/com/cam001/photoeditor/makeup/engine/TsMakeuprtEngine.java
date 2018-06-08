package com.cam001.photoeditor.makeup.engine;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.cam001.service.LogUtil;
import com.cam001.util.BitmapUtil;
import com.cam001.util.CompatibilityUtil;
import com.cam001.util.DebugUtil;
import com.cam001.util.Util;

import java.io.IOException;
import java.io.InputStream;

public class TsMakeuprtEngine {

	private static final String TAG = "TsMakeuprtEngine";
	static {
		try {
			System.loadLibrary("Jnijpegdecode");
			System.loadLibrary("tsmakeuprt_jni");
		} catch (UnsatisfiedLinkError e) {
			String path = "/data/thundersoft/makeup/";
			System.load(path+"libJnijpegdecode.so");
			System.load(path+"libtsmakeuprt_jni.so");
		}
	}
	private int mHandle = 0;
	private static TsMakeuprtEngine mTsMakeuprtEngine = null;
	private int mWidth = 0;
	private int mHeight = 0;
	public boolean isChanged = false;

	private EditBitmap mEditBmp = null;
	private Point[] mOutline = null;

	public static TsMakeuprtEngine getInstance() {
		if (mTsMakeuprtEngine == null)
			mTsMakeuprtEngine = new TsMakeuprtEngine();
		return mTsMakeuprtEngine;
	}

	private TsMakeuprtEngine() {

	}

	public void initHandle(AssetManager asset, int mode) {
		if(mHandle == 0)
			mHandle = init(asset, mode);
//		Log.d("Engine", "" + mHandle);
	}

	public void uninitHandle() {
//		Log.d("Engine", "" + mHandle);
		if(mEditBmp!=null) {
			mEditBmp.recycle();
			mEditBmp = null;
		}
		if(mHandle != 0)
			uninit(mHandle);
		mHandle = 0;
	}

	private boolean mLoadResource = false;

	public Point[] faceTracking(byte[] data, int width, int height, int rotate) {
		if(mHandle==0) {
			DebugUtil.logE("TsMakeuprtEngine", "Already destroy~!");
			return null;
		}
		if(mIsAttendToDump) {
			dumpFrame(data, width, height, rotate);
		}
		mWidth = width;
		mHeight = height;
		LogUtil.startLogTime("facetracking");
		Point[] res =  facetracking(mHandle, data, width, height, rotate);
		LogUtil.stopLogTime("facetracking");
		return res;
	}

	public void makeup(byte[] data, int width, int height, Point[] points){
		if(mHandle==0) {
			return;
		}
		facemakeuprt(mHandle, data, width, height, points);
	}

	public boolean load(Context context, Uri uri) {
		Bitmap bmp = loadImage(context, uri);
		if (bmp != null) {
			mEditBmp = new EditBitmap(bmp);
			mOutline = faceDetect(mEditBmp.getBitmap());
		}
		if(mOutline!=null && mOutline.length>0) {
			return true;
		}
		return false;
	}

	public boolean load(Context context, String path) {
		Bitmap bmp = loadImage(context, path);
		if(bmp!=null) {
			mEditBmp = new EditBitmap(bmp);
			mOutline = faceDetect(mEditBmp.getBitmap());
		}
		if(mOutline!=null && mOutline.length>0) {
			return true;
		}
		return false;
	}

	private Bitmap loadImage(Context context, Uri uri) {
		Bitmap bmp = null;
		if(CompatibilityUtil.low512MMemory())
			bmp = BitmapUtil.getBitmap(uri, context, 1080, 1920);
		else
			bmp = BitmapUtil.getBitmap(uri, context, 720, 1280);
		if (bmp.getWidth() % 2 == 0) {
			return bmp;
		}
		Bitmap cropped = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth() / 2 * 2,
				bmp.getHeight());
		bmp.recycle();
		return cropped;
	}

	private Bitmap loadImage(Context context, String path) {
		Bitmap bmp = null;
		if (path != null && path.startsWith("gallery/")) {
			InputStream is = null;
			try {
				is = context.getAssets().open(path);
				bmp = BitmapFactory.decodeStream(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				Util.closeSilently(is);
			}
		} else {
			if(CompatibilityUtil.low512MMemory())
				bmp = BitmapUtil.getBitmap(path, 1080, 1920);
			else
				bmp = BitmapUtil.getBitmap(path, 720, 1280);
		}
		if (bmp == null)
			return null;
		if (bmp.getWidth() % 2 == 0) {
			return bmp;
		}
		Bitmap cropped = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth() / 2 * 2,
				bmp.getHeight());
		bmp.recycle();
		return cropped;
	}

	public void setFace(Rect face, Point leye, Point reye, Point mouth) {
		mOutline = faceDetect(getOriginalBitmap(), face, leye, reye, mouth);
	}

	public Point[] getOutline() {
		return mOutline;
	}

	public void setOutline(Point[] p) {
		mOutline = p;
	}

	private Point[] faceDetect(Bitmap bmp){
		return faceDetect(bmp, null, null, null, null);
	}

	private Point[] faceDetect(Bitmap bmp, Rect face, Point leye, Point reye, Point mouth){
		if(mHandle==0) {
			return null;
		}
		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();
		if(leye!=null && reye!=null && leye.x>reye.x) {
			Point p = leye;
			leye = reye;
			reye = p;
		}
		Point[] points = jnifacedetect(mHandle, bmp, face, leye, reye, mouth);
		showLog(points);
		return points;
	}

	public Bitmap makeup(){
		if(mHandle==0 || mOutline==null) {
			return null;
		}
		facemakeuprtbmp(mHandle, mOutline, getResultBitmap());
		return getResultBitmap();
	}

	public Bitmap getOriginalBitmap() {
		mEditBmp.setToOriginal(true);
		return mEditBmp.getBitmap();
	}

	public Bitmap getResultBitmap() {
		mEditBmp.setToOriginal(false);
		return mEditBmp.getBitmap();
	}

	public void loadResource(ResDataStyle style, int part, boolean isStatic){
		if(mHandle==0) {
			return;
		}
		makeuploadresource(mHandle, part, style, isStatic);
		isChanged = true;
	}

	private void showLog(Point[] points){
		if(points != null && points.length > 0){
			for(int i = 0; i < points.length; i++){
				Log.d(TAG, "Poing["+i+"]="+"("+points[i].x+","+points[i].y+")");
			}
		}else
			Log.d(TAG, "showLog points null or length == 0");
	}

	private boolean mIsAttendToDump = false;
	private int mDumpIndex = 0;
	private void dumpFrame(byte[] frame, int width, int height, int rotate) {
		String path = "/sdcard/dump/"+mDumpIndex+"_R"+rotate+"_"+width+"x"+height+".nv21";
		Util.dumpToFile(frame, path);
		mDumpIndex ++;
		mIsAttendToDump = false;
	}

	public void dumpFrame() {
		mIsAttendToDump = true;
	}

	public boolean ismHandlerEmpty() {
		return (mHandle == 0) ? true : false;
	}

	private static native int init(AssetManager asset, int mode);

	private static native void uninit(int handle);

	private static native void makeuploadresource(int handle, int part, ResDataStyle style, boolean isStatic);

	private static native Point[] facetracking(int handle, byte[] data,
											   int width, int height, int rotate);

	private static native void facemakeuprt(int handle, byte[] data, int width, int height, Point[] points);

	private static native Point[] jnifacedetect(int handle, Bitmap bmp, Rect face, Point leye, Point reye, Point mouth);

	private static native void facemakeuprtbmp(int handle, Point[] points, Bitmap dstbmp);
}
