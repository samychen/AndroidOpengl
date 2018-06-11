package com.learnopengles.android.gles;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * Created by 000 on 2018/6/11.
 */

public class EffectRender implements GLViewRenderer {
    static {
        System.loadLibrary("lesson-lib");
    }

    private Activity mActivity;
    private int picwidth,picheight;
    private String picpath;
    private String TAG = "EffectRender";

    public EffectRender(Activity activity,int width,int height,String picpath) {
        mActivity = activity;
        this.picwidth = width;
        this.picheight = height;
        this.picpath = picpath;
    }
    public static native void nativeSurfaceCreate(AssetManager assetManager, int width, int height, String picpath);
    public static native void nativeSurfaceChange(int width, int height);
    public static native void nativeDrawFrame();
    public static native void nativereleaseEffect(int type);
    public static native void nativeRender(float norX,float norY);
    public static native void nativeCompare(int flag);
    public void releaseEffect(int type){
        nativereleaseEffect(type);
    }
    public void handleTouchPress(float normalizedX, float normalizedY) {
        Log.e(TAG, "handleTouchPress() called with: normalizedX = [" + normalizedX + "], normalizedY = [" + normalizedY + "]");
        nativeRender(normalizedX, normalizedY);
    }
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        Log.e(TAG, "handleTouchPress() called with: normalizedX = [" + normalizedX + "], normalizedY = [" + normalizedY + "]");
        nativeRender(normalizedX, normalizedY);
    }
    @Override
    public void onSurfaceCreated() {
        AssetManager assetManager = mActivity.getAssets();
        nativeSurfaceCreate(assetManager,picwidth,picheight,picpath);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        nativeSurfaceChange(width, height);
    }

    @Override
    public void onDrawFrame() {
        nativeDrawFrame();
    }
}
