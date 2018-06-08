package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class BlushStruct extends Template {

	public int bratio;// blush ���
	public String bcolor;
	public String btemp;

	public String thumb = null;

	public BlushStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}
}
