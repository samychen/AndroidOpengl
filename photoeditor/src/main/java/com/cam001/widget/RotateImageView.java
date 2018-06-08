package com.cam001.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.cam001.util.DensityUtil;

/**
 * Created by hzhao on 2015/6/2.
 */
public class RotateImageView extends ImageView
        implements MatrixAnimation.OnRefreshListener {

    protected Matrix mMatCanvas = null;

    protected float mLastX0, mLastY0, mLastX1, mLastY1;
    protected float mDownX, mDownY;
    private boolean mIsSingleTouch = false;
    protected MatrixAnimation mAnim = null;
    private int mRotation = 0;
    private boolean mMirrorX = false;
    private boolean mMirrorY = false;

    protected int mImgWidth, mImgHeight;
    private float mRotateScale = 1.0f;

    public RotateImageView(Context context) {
        super(context);
        init();
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mMatCanvas = new Matrix();
        mAnim = new MatrixAnimation();
    }


    @Override
    public void setImageBitmap(Bitmap bmp) {
        super.setImageBitmap(bmp);
        mImgWidth = bmp.getWidth();
        mImgHeight = bmp.getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAnim.isAnimating()) return false;
        if (super.onTouchEvent(event)) {
            return true;
        }
        boolean bHandled = false;
        int pCount = event.getPointerCount();
        switch (pCount) {
            case 2:
                if (mIsSingleTouch) {
                    mIsSingleTouch = false;
                }
                bHandled = handleMultiTouchEvent(event);
                break;
            default:
                break;
        }
        if (bHandled) invalidate();
        return bHandled;
    }

    protected boolean handleMultiTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastX0 = event.getX(0);
                mLastY0 = event.getY(0);
                mLastX1 = event.getX(1);
                mLastY1 = event.getY(1);
                break;
            case MotionEvent.ACTION_MOVE:
                float x0 = event.getX(0);
                float y0 = event.getY(0);
                float x1 = event.getX(1);
                float y1 = event.getY(1);

                float offsetX = (x0 - mLastX0 + x1 - mLastX1) / 2.0f;
                float offsetY = (y0 - mLastY0 + y1 - mLastY1) / 2.0f;
                mMatCanvas.postTranslate(offsetX, offsetY);
                float rotate = getRotate(x0, y0, x1, y1);
                mMatCanvas.postRotate(rotate, (x0 + x1) / 2, (y0 + y1) / 2);
                mRotation += rotate;

                mLastX0 = x0;
                mLastY0 = y0;
                mLastX1 = x1;
                mLastY1 = y1;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                ensureTransform();
                break;
            default:
                break;
        }
        return true;
    }

    private void ensureTransform() {

    }

    public void rotate(int angle) {
        mRotation += angle + 360;
        mRotation %= 360;
        float targetScale = 1.0f;
        if (mRotation % 180 == 90) {
            float imgRatio = mImgWidth / (float) mImgHeight;
            float viwRatio = getWidth() / (float) getHeight();
            float orign = imgRatio > viwRatio ? mImgHeight / (float) getHeight() : mImgWidth / (float) getWidth();
            imgRatio = mImgHeight / (float) mImgWidth;
            float dest = imgRatio > viwRatio ? mImgWidth / (float) getHeight() : mImgHeight / (float) getWidth();
            targetScale = dest / orign;
        }
        mRotateScale = targetScale;
        doRotate();
    }

    public void mirrorX() {
        if (mRotation % 180 == 0) {
            mMirrorX = !mMirrorX;
        } else {
            mMirrorY = !mMirrorY;
        }
        doRotate();
    }

    public void mirrorY() {
        if(mRotation%180 == 0) {
            mMirrorY = !mMirrorY;
        } else {
            mMirrorX = !mMirrorX;
        }
        doRotate();
    }

    private void doRotate() {
        float centerX = getWidth() / 2.0f;
        float centerY = getHeight() / 2.0f;
        Matrix mat = new Matrix();
        mat.postTranslate(-centerX, -centerY);
        mat.postScale(mRotateScale, mRotateScale);
        mat.postScale(mMirrorX ? -1 : 1, mMirrorY ? -1 : 1);
        mat.postRotate(mRotation);
        mat.postTranslate(centerX, centerY);
        transformTo(mat);
    }

    private float getRotate(float x0, float y0, float x1, float y1) {
        double a1 = mLastY0 - mLastY1;
        double b1 = mLastX0 - mLastX1;
        double angle1 = Math.atan(a1 / b1);
        if (b1 < 0) {
            angle1 += Math.PI;
        }

        double a2 = y0 - y1;
        double b2 = x0 - x1;
        double angle2 = Math.atan(a2 / b2);
        if (b2 < 0) {
            angle2 += Math.PI;
        }

        double degree = (angle2 - angle1) * 180 / Math.PI;

        return (float) degree;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.concat(mMatCanvas);
        super.onDraw(canvas);
    }

    private void transformTo(Matrix matDst) {
        mAnim.startAnimation(mMatCanvas, matDst, this);
    }

    @Override
    public void onRefresh(Matrix mat) {
        postInvalidate();
    }

    public int getRotate() {
        return mRotation;
    }

    public boolean getMirrorX() {
        return mMirrorX;
    }

    public boolean getMirrorY() {
        return mMirrorY;
    }
}
