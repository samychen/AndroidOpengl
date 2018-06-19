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

    private ViewDragHelper mViewDragHelper;//���ڴ�����View �Ļ���
    private ScaleGestureDetector mGesture;//���봦��˫�ֵ���������
    private float mZoomScale = 1.0f;//Ĭ�ϵ����ű�Ϊ1
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
            //�������Ʋ������ص��ķ�����
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float previousSpan = detector.getPreviousSpan();//���ŷ���ǰ���������
                float currentSpan = detector.getCurrentSpan();//���ŷ���ʱ���������
                if (previousSpan < currentSpan)//�Ŵ�
                {
                    mZoomScale = mZoomScale + (currentSpan - previousSpan) / previousSpan;
                } else {
                    mZoomScale = mZoomScale - (previousSpan - currentSpan) / previousSpan;
                }
                //ȷ���Ŵ����Ϊ2�������ٲ���С��ԭͼ
                if (mZoomScale > 2) {
                    mZoomScale = 2;
                } else if (mZoomScale < 1) {
                    mZoomScale = 1;
                }
                setScaleX(mZoomScale);
                setScaleY(mZoomScale);
                //������õ��Ǳ��Զ���View�ķ������ǶԱ��Զ���view���е�����
                /*���������getChildView��index���Ľ������ţ���Ȼ�ؼ���ʾ��С�ı��ˣ�������ViewDragHelper�Ļص������л�õ�View child��getWidth������getHeigit������ԭ���Ĵ�С�����ᷢ���ı�*/
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

    //����Ҫ��view�����ڲ�����
    public void addChildView(final View view) {
        //��֤�ؼ�ֻ��һ����View���������

        if (getChildCount() != 0) {
            throw new IllegalStateException("this view can only have one child!");
        } else {
            addView(view);
            //ȷ����view��Զ�����ؼ�
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
        //���¼�����ViewDragHelper��ScaleGestureDetector ����
        mViewDragHelper.processTouchEvent(event);
        return mGesture.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //��ViewDragHelper��������
        return mViewDragHelper.shouldInterceptTouchEvent(ev);


    }

    private void initViewDrag() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                //�����return true��ʾ��view���Ի�����false��ʾ��������
                return true;

            }

            /*�����ǿ�����view���һ����Ļص���childΪ���Զ���view���ӿؼ���left��ʾ��ͼ����ָ������view����߽绬���ľ��룬����0��ʾ�����ƶ���С��0��ʾ�����ƶ����Ǵ���ָ���ƶ����������ֵ�������ķ���ֵ��ʾʵ���Ͽؼ��ƶ��ľ��룬�����÷���ֵ���Ʊ߽�*/
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                //�Ŵ��ؼ��Ŀ��-�����ֵĿ�� ��ΪView chlid�Ŀؼ�ʹ�õ���match_parent,����ֱ���ø����ֵĿ�ȡֵ
                if (left < (getWidth() - getWidth() * mZoomScale) / 2) {
                    return (int) ((getWidth() - getWidth() * mZoomScale) / 2);
                }
                if (left > ((mZoomScale * getWidth() - getWidth()) / 2)) {
                    return (int) ((mZoomScale * getWidth() - getWidth()) / 2);
                }
                return left;
                /** ������б߽紦����Ϊ����ʵ������ܷŴ�2������ôʵ�ʻ���3/4�����һ����ռ�Ż����View��child����ȫ�����ɼ���Χ������Ϊ�˱�֤view�Ŀɼ��ԣ�ʹ��1/2ȷ����viewʼ���ڿɼ���Χ֮��*/
            }

            /*��������ǿ������»����ģ�������ķ�������һ����ֻ�Ƿ����ϲ�һ����top��ʾ�Ӷ����߽����ľ��룬����Ϊ��������Ϊ��*/
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
