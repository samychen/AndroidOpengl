package com.learnopengles.android.scaleutil;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by JarvisLau on 2018/5/29.
 * Description :
 */

public class ScaleGestureBinder extends ScaleGestureDetector {

    ScaleGestureBinder(Context context, ScaleGestureListener scaleGestureListener) {
        super(context, scaleGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

}