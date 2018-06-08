package com.cam001.photoeditor.beauty.engine;

import com.cam001.photoeditor.AppConfig;
import com.cam001.util.BitmapDiskCache;

import android.content.Context;
import android.graphics.Bitmap;

public class EditorHistory {

	private static EditorHistory sInstance = null;
	
	public static EditorHistory getInstance() {
		if(sInstance==null) {
			sInstance = new EditorHistory(AppConfig.getInstance().appContext);
		}
		return sInstance;
	}
 	
	private BitmapDiskCache mCache = null;;
	private int mIndex = -1;
	private int mCount = 0;
	
	private EditorHistory(Context c) {
		mCache = new BitmapDiskCache(c);
	}
	
	public void addHistory(Bitmap bmp) {
		mIndex ++;
		mCache.set(mIndex, bmp);
		mCount = mIndex+1;
	}
	
	public boolean hasPrevious() {
		if(mIndex>0) {
			return true;
		}
		return false;
	}
	
	public boolean hasNext() {
		if(mIndex<mCount-1) {
			return true;
		}
		return false;
	}
	
	public Bitmap original(boolean bOriginal) {
		Bitmap res = null;
		if(bOriginal) {
			res = mCache.get(0);
		} else {
			res = mCache.get(mIndex);
		}
		return res;
	}
	
	public Bitmap previous() {
		if(!hasPrevious()) {
			return null;
		}
		mIndex--;
		return mCache.get(mIndex);
	}
	
	public Bitmap next() {
		if(!hasNext()) {
			return null;
		}
		mIndex++;
		return mCache.get(mIndex);
	}
	
	public void clear() {
		mCache.clear();
		mIndex = -1;
		mCount = 0;
	}
	
}
