package com.learnopengles.android.gles;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import java.util.Map;

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
    public static native void nativeSurfaceChange(int l, int t, int r, int b,int width,int height);
    public static native void nativeDrawFrame();
    public static native void nativereleaseEffect(int type);
    public static native void nativeRender(float norX,float norY);
    public static native void nativeCompare(int flag);
    private Map<Integer,SparseArray<Float>> pathMap;
    private boolean resumeFlag;
    public void releaseEffect(int type){
        nativereleaseEffect(type);
    }
    public void handleTouchPress(float normalizedX, float normalizedY) {
        nativeRender(normalizedX, normalizedY);
    }
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        nativeRender(normalizedX, normalizedY);
    }
    public void setPath(Map<Integer,SparseArray<Float>> path){
        Log.e(TAG, "setPath: "+path.toString() );
        pathMap = path;
        resumeFlag = true;
    }
    @Override
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated: " );
        AssetManager assetManager = mActivity.getAssets();
        nativeSurfaceCreate(assetManager,picwidth,picheight,picpath);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        RectF rectDst = new RectF();
        mMat.setRectToRect(new RectF(0,0,picwidth, picheight), new RectF(0,0,width,height), Matrix.ScaleToFit.CENTER);
        mMat.mapRect(rectDst, new RectF(0,0,picwidth, picheight));
        nativeSurfaceChange((int)rectDst.left,(int)rectDst.top,(int)rectDst.width(),(int)rectDst.height(),width,height);
    }

    @Override
    public void onDrawFrame() {
        Log.e(TAG, "onDrawFrame: "+ pathMap.size());
        // 将保存的效果绘制上去
        int size = pathMap.size();
        if (resumeFlag){
            for (int i = 0; i < size; i++) {
                SparseArray<Float> onePath = pathMap.get(i);
                int size2 = onePath.size();
                Log.e(TAG, "onDrawFrame: size2="+size2 );
                for (int j = 0; j < size2; j++) {
                    float x = onePath.get(j);
                    float y = onePath.get(j);
                    Log.e(TAG, "onDrawFrame: x="+x+"y="+y );
                    j++;
                    nativeRender(x,y);
                }
                resumeFlag = false;
            }
        }
        nativeDrawFrame();
    }
}
