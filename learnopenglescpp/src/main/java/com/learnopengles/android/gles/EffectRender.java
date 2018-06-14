package com.learnopengles.android.gles;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.RectF;
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

    private Matrix mMat = new Matrix();

    public EffectRender(Activity activity,int width,int height,String picpath) {
        mActivity = activity;
        this.picwidth = width;
        this.picheight = height;
        this.picpath = picpath;
    }
    public static native void nativeSurfaceCreate(AssetManager assetManager, int width, int height, String picpath);
    public static native void nativeSurfaceChange(int left, int top, int right, int bottom);
    public static native void nativeDrawFrame();
    public static native void nativereleaseEffect(int type);
    public static native void nativeRender(float norX,float norY);
    public static native void nativeCompare(int flag);
    public static native void nativeTransform(float[] martrix);
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
    public void transformMartrix(float[] martrix){
        nativeTransform(martrix);
    }
    @Override
    public void onSurfaceCreated() {
        AssetManager assetManager = mActivity.getAssets();
        nativeSurfaceCreate(assetManager,picwidth,picheight,picpath);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        RectF rectDst = new RectF();
        Log.e(TAG, "onSurfaceChanged() called with: width = [" + width + "], height = [" + height + "] picwidth="+picwidth+"picheight="+picheight);
        mMat.setRectToRect(new RectF(0,0,picwidth, picheight), new RectF(0,0,width,height), Matrix.ScaleToFit.CENTER);
        mMat.mapRect(rectDst, new RectF(0,0,picwidth, picheight));
        Log.e(TAG, "onSurfaceChanged:left= "+rectDst.left+"right="+rectDst.right+"top="+rectDst.top+"bottom="+rectDst.bottom+"width="+rectDst.width()+"height="+rectDst.height() );
        nativeSurfaceChange((int)rectDst.left,(int)rectDst.top,(int)rectDst.width() ,(int)rectDst.height());
    }

    @Override
    public void onDrawFrame() {
        nativeDrawFrame();
    }
}
