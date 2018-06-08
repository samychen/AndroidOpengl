package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class ShadowStruct extends Template {

	public int eshaderatio;// eyeshadow ��Ӱ
	public int eshaderfratio;
	public String eshadercolor;
	public String eshadertemp;

	public String thumb = null;

	public ShadowStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}
}
