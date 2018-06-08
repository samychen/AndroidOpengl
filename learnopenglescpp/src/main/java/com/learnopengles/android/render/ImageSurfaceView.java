package com.learnopengles.android.render;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by 000 on 2018/6/8.
 */

public class ImageSurfaceView extends GLSurfaceView {
    ImageRender imageRender;
    public ImageSurfaceView(Context context) {
        super(context);
        //为了可以激活log和错误检查，帮助调试3D应用，需要调用setDebugFlags()。
        this.setDebugFlags(DEBUG_CHECK_GL_ERROR|DEBUG_LOG_GL_CALLS);
    }
    public void setParam(Activity activity, int width, int heitht, String picpath){
        imageRender = new ImageRender(activity,width,heitht,picpath);
        this.setRenderer(imageRender);
    }
    public boolean onTouchEvent(final MotionEvent event){
        this.queueEvent(new Runnable() {
            @Override
            public void run() {
                //TODO:
            }
        });
        return true;
    }
}
