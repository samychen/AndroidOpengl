package com.zero.fragmentanimation.openGL;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * @author linzewu
 * @date 2017/7/11
 */

public class ShatterAnimGLView extends GLSurfaceView {
    
    public ShatterAnimGLView(Context context) {
        super(context);
    }

    public ShatterAnimGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void initGLSurfaceView(Renderer renderer) {
        final ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            /* 设置版本为2.0 */
            setEGLContextClientVersion(2);
            /* 设置颜色缓存为 RGBA */
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            /* 设置渲染器 */
            setRenderer(renderer);
            /* 设置背景透明 */
            getHolder().setFormat(PixelFormat.TRANSPARENT);
            /* 设置渲染模式为GLSurfaceView.RENDERMODE_WHEN_DIRTY,在这个模式下opengl会减少不必要的渲染操作,延长电池的寿命 */
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            /* 设置GLSurfaceView处于窗口的最顶层 */
            setZOrderOnTop(true);
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
//    public void setRenderer(Renderer renderer) {
//        setRenderer(renderer);
//    }
    
}
