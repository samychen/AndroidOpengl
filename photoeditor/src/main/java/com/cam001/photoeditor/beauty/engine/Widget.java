package com.cam001.photoeditor.beauty.engine;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface Widget {
	void draw(Canvas canvas);
	boolean dispatchTouchEvent(MotionEvent event);
}
