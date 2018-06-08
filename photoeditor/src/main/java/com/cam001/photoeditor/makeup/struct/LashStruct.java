package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class LashStruct extends Template {

	public int elashratio;// eyelash �۽�ë
	public String elashcolor;
	public String elashupper;
	public String elashlower;

	public String thumb = null;

	public LashStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}
}
