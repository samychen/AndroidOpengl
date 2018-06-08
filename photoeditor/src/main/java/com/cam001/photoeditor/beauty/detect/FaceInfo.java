package com.cam001.photoeditor.beauty.detect;

import android.graphics.Rect;

public class FaceInfo {
	public Rect face = new Rect();
	public Rect eye1 = new Rect();
	public Rect eye2 = new Rect();
	public Rect mouth = new Rect();
	public int[] marks = null;
	public int[] eyeMarks = null;
	public boolean needRefreshOutline = false;
}
