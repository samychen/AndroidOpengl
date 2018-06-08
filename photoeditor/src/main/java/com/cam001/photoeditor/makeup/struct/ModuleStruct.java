package com.cam001.photoeditor.makeup.struct;

import android.graphics.Bitmap;

public class ModuleStruct extends Template {

	public int blush;//index 
	public int eyelash;
	public int eyeline;
	public int shadow;
	public int contact;
	public int lipstick;
	
	public int bratio = -1;
	public int lashratio = -1;
	public int lineratio = -1;
	public int shadowratio = -1;
	public int conratio = -1;
	public int lipratio = -1;
	
	public String thumb;

	public ModuleStruct(String path) {
		super(path);
	}

	@Override
	public Bitmap getThumbnail() {
		return super.getThumbnail(thumb);
	}
}
