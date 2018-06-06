package com.learnopengles.android.render;

import android.app.Activity;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 000 on 2018/6/5.
 */

public class ImageRender implements GLSurfaceView.Renderer{
    static {
        System.loadLibrary("lesson-lib");
    }

    private Activity mActivity;

    public ImageRender(Activity activity) {
        mActivity = activity;
    }
    public static native void nativeSurfaceCreate(AssetManager assetManager);

    public static native void nativeSurfaceChange(int width, int height);

    public static native void nativeDrawFrame();
    public static native void nativeRender(float norX,float norY);

    public void handleTouchPress(float normalizedX, float normalizedY) {

        nativeRender(normalizedX, normalizedY);
    }
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        nativeRender(normalizedX, normalizedY);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        AssetManager assetManager = mActivity.getAssets();
        nativeSurfaceCreate(assetManager);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        nativeSurfaceChange(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        nativeDrawFrame();
    }
}
