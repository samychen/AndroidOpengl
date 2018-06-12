package com.learnopengles.android.scaleutil;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by 000 on 2018/6/11.
 */

public class GestureView extends FrameLayout {

    private ViewDragHelper mViewDragHelper;//用于处理子View 的滑动
    private ScaleGestureDetector mGesture;//用与处理双手的缩放手势
    private float mZoomScale = 1.0f;//默认的缩放比为1
    private Context mContext;


    public GestureView(Context context) {
        this(context, null);
    }

    public GestureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        initViewDrag();
        mGesture = new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            //随着手势操作，回调的方法，
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float previousSpan = detector.getPreviousSpan();//缩放发生前的两点距离
                float currentSpan = detector.getCurrentSpan();//缩放发生时的两点距离
                if (previousSpan < currentSpan)//放大
                {
                    mZoomScale = mZoomScale + (currentSpan - previousSpan) / previousSpan;
                } else {
                    mZoomScale = mZoomScale - (previousSpan - currentSpan) / previousSpan;
                }
                //确保放大最多为2倍，最少不能小于原图
                if (mZoomScale > 2) {
                    mZoomScale = 2;
                } else if (mZoomScale < 1) {
                    mZoomScale = 1;
                }
                setScaleX(mZoomScale);
                setScaleY(mZoomScale);
                //这里调用的是本自定义View的方法，是对本自定义view进行的缩放
                /*在这里调用getChildView（index）的进行缩放，虽然控件显示大小改变了，但是在ViewDragHelper的回调方法中获得的View child的getWidth（）和getHeigit（）是原来的大小，不会发生改变*/
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            /**

             * @param detector
             */
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {


            }
        });
    }

    //将需要的view放置在布局中
    public void addChildView(final View view) {
        //保证控件只有一个子View，方便操作

        if (getChildCount() != 0) {
            throw new IllegalStateException("this view can only have one child!");
        } else {
            addView(view);
            //确保子view永远铺满控件
            FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //将事件交给ViewDragHelper和ScaleGestureDetector 处理
        mViewDragHelper.processTouchEvent(event);
        return mGesture.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //由ViewDragHelper处理拦截
        return mViewDragHelper.shouldInterceptTouchEvent(ev);


    }

    private void initViewDrag() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                //这里的return true表示自view可以滑动，false表示不处理滑动
                return true;

            }

            /*这里是控制子view左右滑动的回调，child为本自定义view的子控件，left表示意图从手指操作子view从左边界滑动的距离，大于0表示向右移动，小于0表示向左移动，是从手指的移动测出的理论值。方法的返回值表示实际上控件移动的距离，可以用返回值控制边界*/
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                //放大后控件的宽度-主布局的宽度 因为View chlid的控件使用的是match_parent,所以直接用父布局的宽取值
                if (left < (getWidth() - getWidth() * mZoomScale) / 2) {
                    return (int) ((getWidth() - getWidth() * mZoomScale) / 2);
                }
                if (left > ((mZoomScale * getWidth() - getWidth()) / 2)) {
                    return (int) ((mZoomScale * getWidth() - getWidth()) / 2);
                }
                return left;
                /** 这里进行边界处理，因为这里实际最大能放大2倍，那么实际会有3/4的左右滑动空间才会把子View（child）完全划出可见范围，这里为了保证view的可见性，使用1/2确保子view始终在可见范围之内*/
            }

            /*这个方法是控制上下滑动的，跟上面的方法意义一样，只是方向上不一样，top表示从顶部边界计算的距离，向下为正，向上为负*/
            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {

                if (top < (getHeight() - getHeight() * mZoomScale) / 2) {
                    return ((int) (getHeight() - getHeight() * mZoomScale) / 2);
                }
                if (top > (mZoomScale * getHeight() - getHeight()) / 2) {
                    return (int) ((mZoomScale * getHeight() - getHeight()) / 2);
                }
                return top;
            }
        });
    }
}
