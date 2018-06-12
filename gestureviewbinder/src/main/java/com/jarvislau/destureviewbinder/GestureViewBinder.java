package com.jarvislau.destureviewbinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by JarvisLau on 2018/5/30.
 * Description :
 */

public class GestureViewBinder {

    private ScaleGestureBinder scaleGestureBinder;
    private ScrollGestureBinder scrollGestureBinder;
    private ScaleGestureListener scaleGestureListener;
    private ScrollGestureListener scrollGestureListener;
    private View targetView;
    private ViewGroup viewGroup;
    private boolean isScaleEnd = true;

    private boolean isFullGroup = false;

    public static GestureViewBinder bind(Context context, ViewGroup viewGroup, View targetView) {
        return new GestureViewBinder(context, viewGroup, targetView);
    }

    private GestureViewBinder(Context context, ViewGroup viewGroup, View targetView) {
        this.targetView = targetView;
        this.viewGroup = viewGroup;
        scaleGestureListener = new ScaleGestureListener(targetView, viewGroup);
        scrollGestureListener = new ScrollGestureListener(targetView, viewGroup);
        scaleGestureBinder = new ScaleGestureBinder(context, scaleGestureListener);
        scrollGestureBinder = new ScrollGestureBinder(context, scrollGestureListener);
        targetView.setClickable(false);
        final int width = viewGroup.getWidth();
        final int height = viewGroup.getHeight();
        viewGroup.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 1 && isScaleEnd) {
                    Log.e("test", "onTouch: group width="+width+"height="+height);
                    Log.e("test", "onTouch: x="+event.getX()+"y="+event.getY() );
                    return scrollGestureBinder.onTouchEvent(event);
                } else if (event.getPointerCount() == 2 || !isScaleEnd) {
                    isScaleEnd = event.getAction() == MotionEvent.ACTION_UP;
                    if (isScaleEnd) {
                        scaleGestureListener.onActionUp();
                    }
                    scrollGestureListener.setScale(scaleGestureListener.getScale());
                    if (event.getAction()==MotionEvent.ACTION_UP){
                        if (scaleGestureListener.getScale()<1){
                            scrollGestureListener.setScale(1.0f);
                        }
                    }
                    return scaleGestureBinder.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    private void fullGroup() {
        targetView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                        float viewWidth = targetView.getWidth();
                        float viewHeight = targetView.getHeight();
                        float groupWidth = viewGroup.getWidth();
                        float groupHeight = viewGroup.getHeight();
                        ViewGroup.LayoutParams layoutParams = targetView.getLayoutParams();
                        float widthFactor = groupWidth / viewWidth;
                        float heightFactor = groupHeight / viewHeight;
                        if (viewWidth < groupWidth && widthFactor * viewHeight <= groupHeight) {
                            layoutParams.width = (int) groupWidth;
                            layoutParams.height = (int) (widthFactor * viewHeight);
                        } else if (viewHeight < groupHeight && heightFactor * viewWidth <= groupWidth) {
                            layoutParams.height = (int) groupHeight;
                            layoutParams.width = (int) (heightFactor * viewWidth);
                        }
                        targetView.setLayoutParams(layoutParams);
                        return true;
                    }
                });
    }

    public boolean isFullGroup() {
        return isFullGroup;
    }

    public void setFullGroup(boolean fullGroup) {
        isFullGroup = fullGroup;
        scaleGestureListener.setFullGroup(fullGroup);
        scrollGestureListener.setFullGroup(fullGroup);
        fullGroup();
    }
}