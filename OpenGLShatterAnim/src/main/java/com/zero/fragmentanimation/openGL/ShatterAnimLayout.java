package com.zero.fragmentanimation.openGL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author linzewu
 * @date 2017/7/11
 */

public class ShatterAnimLayout extends FrameLayout {
    
    private ShatterAnimGLView mFragAnimGLView;
    private ShatterAnimRender mFragAnimRender;
    
    public ShatterAnimLayout(Context context) {
        super(context);
        initView();
    }

    public ShatterAnimLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ShatterAnimLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(mFragAnimGLView);
    }
    
    private void initView() {
        mFragAnimGLView = new ShatterAnimGLView(getContext());
        mFragAnimRender = new ShatterAnimRender(mFragAnimGLView);
        mFragAnimGLView.initGLSurfaceView(mFragAnimRender);
    }
    
    public void startAnimation() {
        /* 对视图内容进行截图 */
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        super.draw(canvas);
        mFragAnimRender.startAnimation(bitmap);
        
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < getChildCount(); i++) {
                    if (getChildAt(i) != mFragAnimGLView) {
                        getChildAt(i).setVisibility(GONE);
                    }
                }
            }
        });
    }
    
    public void stopAnimation() {
        mFragAnimRender.stopAnimation();
    }

    public void onResume() {
        if (mFragAnimGLView != null) {
            mFragAnimGLView.onResume();   
        }
    }
    
    public void onPause() {
        if (mFragAnimGLView != null) {
            mFragAnimGLView.onPause();
        }
    }
    
    public void onDestroy() {
        if (mFragAnimRender != null) {
            mFragAnimRender.destroy();
        }
    }
    
}
