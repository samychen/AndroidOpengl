package com.edmodo.cropper;

import android.graphics.Rect;
import android.graphics.RectF;

import com.cam001.util.ExifUtil;

public class CropUtil {
	static {
		System.loadLibrary("tsutils");
	}

	public CropUtil() {
	}
//	private static native boolean crop(String filepath);
	private static native boolean cropusebytes(byte[] buff, int left, int top, int right, int bottom, String path);
	private static native byte[] cropusebytes(byte[] buff, int left, int top, int right, int bottom, int rotation);
	
//	public boolean tsCrop(String filepath){
//		return crop(filepath);
//	}
	public boolean tsCropusebytes(byte[] buff, RectF rect,String path){
		int left = (int) rect.left,top = (int) rect.top,right=(int) rect.right,bottom=(int) rect.bottom;
		return cropusebytes(buff,left,top,right,bottom,path);
	}
	public byte[] tsCropusebytes(byte[] buff, RectF rect){
		int left = (int) rect.left,top = (int) rect.top,right=(int) rect.right,bottom=(int) rect.bottom;
        int angle = ExifUtil.getOrientation(buff);
		return cropusebytes(buff,left,top,right,bottom, angle);
	}
	public boolean tsCropusebytes(byte[] buff, Rect rect,String path){
		int left = rect.left,top = rect.top,right=rect.right,bottom=rect.bottom;
		return cropusebytes(buff,left,top,right,bottom,path);
	}
	public byte[] tsCropusebytes(byte[] buff, Rect rect){
		int left = rect.left,top = rect.top,right=rect.right,bottom=rect.bottom;
        int angle = ExifUtil.getOrientation(buff);
		return cropusebytes(buff,left,top,right,bottom, angle);
	}
}
