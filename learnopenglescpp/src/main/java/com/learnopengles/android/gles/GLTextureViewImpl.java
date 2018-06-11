package com.learnopengles.android.gles;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by 000 on 2018/6/11.
 */

public class GLTextureViewImpl extends GLTextureView implements View.OnTouchListener{
    private boolean isCanTouch = false;
    private int point_num = 0;//当前触摸的点数
    public static final float SCALE_MAX = 8.0f; //最大的缩放比例
    private static final float SCALE_MIN = 1.0f;

    private double oldDist = 0;
    private double moveDist = 0;
    //针对控件的坐标系，即控件左上角为原点
    private double moveX = 0;
    private double moveY = 0;

    private double downX = 0;
    private double downY = 0;
    //针对屏幕的坐标系，即屏幕左上角为原点
    private double moveRawX = 0;
    private double moveRawY = 0;
    //是否选中移动或者没有选中效果
    private boolean moveFlag;
    private int picwidth,picheight;
    private EffectRender renderer;
    public void setPicSize(int picwidth,int picheight) {
        this.picwidth = picwidth;
        this.picheight = picheight;
    }

    public void setMoveFlag(boolean moveFlag) {
        this.moveFlag = moveFlag;
    }

    public GLTextureViewImpl(Context context) {
        this(context,null);
    }

    public GLTextureViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }
    public void setRenderer(EffectRender renderer) {
        this.renderer = renderer;
        super.setRenderer(renderer);
    }

    public void setIsCanTouch(boolean canTouch) {
        isCanTouch = canTouch;
    }
    @Override
    protected void init() {
        super.init();
        setOpaque(true);//This method indicates whether the content of this TextureView is opaque
    }

    @Override
    protected void onGLDraw() {
        //预留
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isCanTouch) {
            return false;
        }
        if (moveFlag){//允许移动
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    point_num = 1;
                    downX = event.getX();
                    downY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    point_num = 0;
                    downX = 0;
                    downY = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (point_num == 1) {
                        //只有一个手指的时候才有移动的操作
                        float lessX = (float) (downX - event.getX());
                        float lessY = (float) (downY - event.getY());
                        moveX = event.getX();
                        moveY = event.getY();
                        moveRawX = event.getRawX();
                        moveRawY = event.getRawY();
                        setSelfPivot(lessX, lessY);
                        //setPivot(getPivotX() + lessX, getPivotY() + lessY);
                    } else if (point_num == 2) {
                        //只有2个手指的时候才有放大缩小的操作
                        moveDist = spacing(event);
                        double space = moveDist - oldDist;
                        float scale = (float) (getScaleX() + space / getWidth());
                        if (scale > SCALE_MIN && scale < SCALE_MAX) {
                            setScale(scale);
                        } else if (scale < SCALE_MIN) {
                            setScale(SCALE_MIN);
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);//两点按下时的距离
                    point_num += 1;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    point_num -= 1;
                    break;
            }
        } else {//处理效果
            if (event != null) {
                final float normalizedX =event.getX() / (float) v.getWidth()*picwidth;
                final float normalizedY =event.getY() / (float) v.getHeight()*picheight;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.handleTouchPress(
                                    normalizedX, normalizedY);
                        }
                    });
                    requestRender();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.handleTouchDrag(
                                    normalizedX, normalizedY);
                        }
                    });
                    requestRender();
                }
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    /**
     * 触摸使用的移动事件
     *
     * @param lessX
     * @param lessY
     */
    private void setSelfPivot(float lessX, float lessY) {
        float setPivotX = 0;
        float setPivotY = 0;
        setPivotX = getPivotX() + lessX;
        setPivotY = getPivotY() + lessY;
        Log.e("lawwingLog", "setPivotX:" + setPivotX + "  setPivotY:" + setPivotY
                + "  getWidth:" + getWidth() + "  getHeight:" + getHeight());
        if (setPivotX < 0 && setPivotY < 0) {
            setPivotX = 0;
            setPivotY = 0;
        } else if (setPivotX > 0 && setPivotY < 0) {
            setPivotY = 0;
            if (setPivotX > getWidth()) {
                setPivotX = getWidth();
            }
        } else if (setPivotX < 0 && setPivotY > 0) {
            setPivotX = 0;
            if (setPivotY > getHeight()) {
                setPivotY = getHeight();
            }
        } else {
            if (setPivotX > getWidth()) {
                setPivotX = getWidth();
            }
            if (setPivotY > getHeight()) {
                setPivotY = getHeight();
            }
        }
        setPivot(setPivotX, setPivotY);
    }

    /**
     * 计算两个点的距离
     *
     * @param event
     * @return
     */
    private double spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    /**
     * 平移画面，当画面的宽或高大于屏幕宽高时，调用此方法进行平移
     *
     * @param x
     * @param y
     */
    public void setPivot(float x, float y) {
        setPivotX(x);
        setPivotY(y);
    }

    /**
     * 设置放大缩小
     *
     * @param scale
     */
    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    /**
     * 初始化比例，也就是原始比例
     */
    public void setInitScale() {
        setScaleX(1.0f);
        setScaleY(1.0f);
        setPivot(getWidth() / 2, getHeight() / 2);
    }
}
