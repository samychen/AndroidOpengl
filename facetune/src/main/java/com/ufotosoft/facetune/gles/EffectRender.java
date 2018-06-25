package com.ufotosoft.facetune.gles;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import com.ufotosoft.facetune.MainActivity;
import java.lang.ref.WeakReference;
import java.util.Map;

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
    private float radius;
    private int srcTextureID;
    private int glProgramId;

    public void setRadius(float radius) {
        this.radius = radius;
        Log.e(TAG, "setRadius: "+radius );
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
    private boolean clearLastEffect;
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
        lastX = normalizedX;
        lastY = normalizedY;
//        nativeRender(normalizedX, normalizedY,radius);
    }
    private float lastX,lastY;
    public void handleTouchDrag(float normalizedX, float normalizedY) {
        nativeRender(normalizedX, normalizedY,radius);
//        float length = (float) Math.sqrt((lastX-normalizedX)*(lastX-normalizedX)+(lastY-normalizedY)*(lastY-normalizedY));
//        if (length>16){
//            int size = (int) (length / 5);
//            float deltaX = (normalizedX - lastX)/size;
//            float deltaY = (normalizedY - lastY)/size;
//            for (int i = 0; i < size; i++) {
//                Log.e(TAG, "handleTouchDrag: " );
//                nativeRender(normalizedX+i*deltaX, normalizedY+i*deltaY,radius);
//            }
//        }
        lastX = normalizedX;
        lastY = normalizedY;
    }
    public void setPath(Map<Integer,SparseArray<Float>> path){
        clearLastEffect = true;
        pathMap = path;
    }
    // 比对效果
    public void compare(int flag){
        nativeCompare(flag);
    }
    @Override
    public void onSurfaceCreated() {
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
        Log.e(TAG, "onDrawFrame: " );
        if (clearLastEffect){
            int size = pathMap.size();
            for (int i = 0; i < size; i++) {
                SparseArray<Float> onePath = pathMap.get(i);
                if (onePath==null){
                    break;
                }
                int size2 = onePath.size();
                for (int j = 0; j < size2; j++) {
                    float x = onePath.get(j);
                    float y = onePath.get(j+1);
                    j++;
                    nativeDrawPath(x,y,radius);
                }
            }
            clearLastEffect = false;
        }
        nativeDrawFrame();
    }
}
