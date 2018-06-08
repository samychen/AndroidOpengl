package com.cam001.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class BitmapDiskCache {

	private Context mContext = null;
	
	
	public BitmapDiskCache(Context c) {
		mContext = c;
	}
	
	public void set(int key, Bitmap value) {
		String path = String.valueOf(key);
		FileOutputStream fos = null;
		try {
			fos = mContext.openFileOutput(path, Context.MODE_PRIVATE);
			value.compress(CompressFormat.JPEG, 100, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(fos!=null) Util.closeSilently(fos);
		}
	}
	
	public Bitmap get(int key) {
		String path = String.valueOf(key);
		Bitmap res = null;
		FileInputStream fis = null;
		try {
			fis = mContext.openFileInput(path);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			res = BitmapFactory.decodeStream(fis, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(fis!=null) Util.closeSilently(fis);
		}
		return res;
	}
	
	public void clear() {
		String[] paths = mContext.fileList();
		for(String p: paths) {
			mContext.deleteFile(p);
		}
	}
	
}
