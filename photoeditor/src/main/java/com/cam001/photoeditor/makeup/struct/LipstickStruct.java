package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class LipstickStruct extends Template {

	public int lratio;// lipstick �ں�
	public int lgloss;
	public String lcolor;
	public String ltemp;

	public String thumb = null;

	public LipstickStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}
}
