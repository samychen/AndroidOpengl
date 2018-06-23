package com.ufotosoft.facetune.gles;

import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;

import com.ufotosoft.facetune.MainActivity;

import java.lang.ref.WeakReference;
import java.util.Map;

import javax.microedition.khronos.egl.EGL;

/**
 * Created by 000 on 2018/6/11.
 */

public class EffectRender implements GLViewRenderer {
    static {
        System.loadLibrary("beauty-lib");
    }
    private WeakReference<MainActivity> mActivity;
    private int picwidth,picheight;
    private String picpath;
    private String TAG = "EffectRender";
    private Matrix mMat = new Matrix();
    private boolean pauseFlag;

    public void setRadius(float radius) {
        this.radius = radius;
        Log.e(TAG, "setRadius: "+radius );
    }

    private float radius;
    private int srcTextureID;
    private int glProgramId;

    @Override
    public int getTexID() {
        return srcTextureID;
    }

    @Override
    public void setTexID(int id) {
        srcTextureID = id;
    }

    public EffectRender(MainActivity activity, int width, int height, String picpath) {
        mActivity = new WeakReference<MainActivity>(activity);
        this.picwidth = width;
        this.picheight = height;
        this.picpath = picpath;
        setRadius(width*20.0f/600);
        init();
    }
    public native void init();
    public native void destroy();
    public static native void nativeSurfaceCreate(int width, int height, int srcTextureID,int programID);
    public static native void nativeSurfaceChange(int l, int t, int r, int b,int width,int height);
    public static native void nativeDrawFrame();
    public static native void nativereleaseEffect(int type);
    public static native void nativeRender(float norX,float norY,float radius);
    public static native void nativeCompare(int flag);
    public static native void nativeDrawPath(float norX,float norY,float radius);
    public static native void nativeRemoveLastEffect();
    public static native void nativeSetPaint();
    private Map<Integer,SparseArray<Float>> pathMap;
    private boolean resumeFlag;
    // 切换效果
    public void releaseEffect(int type){
        nativereleaseEffect(type);
    }
    // 清除上一条效果
    public void removeLastEffect(){
        nativeRemoveLastEffect();
    }
    public void setPaint(){
        nativeSetPaint();
    }
    public void handleTouchPress(float normalizedX, float normalizedY) {
        nativeRender(normalizedX, normalizedY,radius);
    }
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        nativeRender(normalizedX, normalizedY,radius);
    }
    public void setPath(Map<Integer,SparseArray<Float>> path){
        pathMap = path;
        resumeFlag = true;
        Log.e(TAG, "setPath: "+pathMap.toString() );
    }
    // 比对效果
    public void compare(int flag){
        nativeCompare(flag);
    }
    @Override
    public void onSurfaceCreated() {
        Log.e(TAG, "onSurfaceCreated: getNativeHandle"+ EGL14.eglGetCurrentContext().getNativeHandle());
        srcTextureID = Utils.loadTexture(mActivity.get().getAssets(),picpath);
        glProgramId=ShaderUtils.createProgram(ShaderUtils.readAssetsTextFile(mActivity.get(),"vertex/transform_vertex_shader.glsl"),
                ShaderUtils.readAssetsTextFile(mActivity.get(),"fragment/transform_fragment_shader.glsl"));
        nativeSurfaceCreate(picwidth,picheight,srcTextureID,glProgramId);
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
        nativeDrawFrame();
    }
}
