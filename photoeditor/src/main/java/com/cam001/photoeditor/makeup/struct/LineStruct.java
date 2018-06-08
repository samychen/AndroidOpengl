package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class LineStruct extends Template {

	public int elineratio;//eyaline ����
	public String elinercolor;
	public String elinerupper;
	public String elinerlower;
	
	public String thumb = null;
	
	public LineStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}

}
