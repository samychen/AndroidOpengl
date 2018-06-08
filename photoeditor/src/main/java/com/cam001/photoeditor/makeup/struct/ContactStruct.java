package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class ContactStruct extends Template {

	public int cratio;//��ͫ ContactLen
	public String ctemp;
	
	public String thumb = null;
	
	public ContactStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}
}
