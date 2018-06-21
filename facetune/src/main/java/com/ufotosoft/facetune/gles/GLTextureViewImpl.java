package com.ufotosoft.facetune.gles;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.ufotosoft.facetune.R;
import com.ufotosoft.facetune.scaleutil.DensityUtil;
import com.ufotosoft.facetune.scaleutil.MatrixAnimation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 000 on 2018/6/11.
 */

public class GLTextureViewImpl extends GLTextureView implements View.OnTouchListener, MatrixAnimation.OnRefreshListener {
    private boolean isCanTouch = false;
    private static final float MAX_ZOOM = 8.0f;
    private static final int MAX_DCLICK_TIME = 300;
    private static int MAX_DCLICK_DIS = 50;
    private static boolean sNeedDipToPix = true;

    protected Matrix mMatCanvas = null;
    protected float mLastX0, mLastY0, mLastX1, mLastY1;
    protected float mDownX, mDownY;
    private boolean mIsSingleTouch = false;
    private long mLastTapTime = 0;
    protected MatrixAnimation mAnim = null;
    private Handler mHandler = new Handler();
    private boolean moveFlag;
    private int picwidth, picheight;
    private EffectRender renderer;
    private String TAG = "GLTextureViewImpl";
    private Map<Integer, SparseArray<Float>> pathMap = new HashMap<>();
    private float scale = 1.0f;
    public Map<Integer, SparseArray<Float>> getPathMap() {
        return pathMap;
    }

    public Map<Integer, SparseArray<Float>> getLastPath() {
        Map<Integer, SparseArray<Float>> map = new HashMap<>();
        map.put(0, pathMap.get(pathMap.size() - 1));
        pathMap.remove(pathMap.size() - 1);
        pathcount--;
        return map;
    }

    public void clearPath() {
        pathMap.clear();
        pathcount = 0;
    }

    private int pointcount;
    private int pathcount;

    public void setPicSize(int picwidth, int picheight) {
        this.picwidth = picwidth;
        this.picheight = picheight;
    }

    public void setMoveFlag(boolean moveFlag) {
        this.moveFlag = moveFlag;
    }

    public GLTextureViewImpl(Context context) {
        this(context, null);
    }

    public GLTextureViewImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initAnim();
    }

    private void initAnim() {
        mMatCanvas = new Matrix();
        mAnim = new MatrixAnimation();
        if (sNeedDipToPix) {
            MAX_DCLICK_DIS = DensityUtil.dip2px(getContext(), MAX_DCLICK_DIS);
            sNeedDipToPix = false;
        }
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
        setOpaque(true);
    }

    @Override
    protected void onGLDraw() {
        //???
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureAvailable(surface, width, height);
        setLayoutParams(new FrameLayout.LayoutParams(width,picheight*width/picwidth, Gravity.CENTER));
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: " + pathMap.toString());
        renderer.setPath(pathMap);
        super.onResume();
    }

    @Override
    public void onPause() {
        mMatCanvas = new Matrix();
        setTransform(mMatCanvas);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isCanTouch) {
            return false;
        }
        if (mAnim.isAnimating()) return false;
        if (super.onTouchEvent(event)) {
            return true;
        }
        boolean bHandled = false;
        int pCount = event.getPointerCount();
        if (pCount == 2) {
            if (mIsSingleTouch) {
                mIsSingleTouch = false;
            }
            bHandled = handleMultiTouchEvent(event);
            if (bHandled) invalidate();
            return bHandled;
        }
        if (moveFlag) {//�ƶ�
            if (super.onTouchEvent(event)) {
                return true;
            }
            bHandled = false;
            Log.e(TAG, "onTouch: top="+v.getTop() );
            int count = event.getPointerCount();
            switch (count) {
                case 1:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mIsSingleTouch = true;
                    } else if (!mIsSingleTouch) {
                        return false;
                    }
                    bHandled = handleSingleTouhEvent(event);
                    break;
                default:
                    break;
            }
            if (bHandled) invalidate();
            return bHandled;
        } else {
            if (event != null) {
                float[] mat = new float[9];
                Matrix src = mMatCanvas;
                Matrix dst = new Matrix();
                src.invert(dst);
                dst.getValues(mat);
                if (mat[0]!=1){
                    renderer.setRadius(picwidth*20.0f/600*mat[0]*2);
                }
                //将event的x，y值映射到view中心
                float x1 = mat[0] * event.getX() + mat[1] * event.getY() + mat[2];
                float y1 = mat[3] * event.getX() + mat[4] * event.getY() + mat[5];
                final float normalizedX = x1 / (v.getWidth()*1.0f) * picwidth;
                final float normalizedY = y1 / (v.getHeight()*1.0f) * picheight;
                Log.e(TAG, "onTouch: x="+normalizedX+"y="+normalizedY );
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.handleTouchPress(
                                    normalizedX, normalizedY);
                        }
                    });
                    requestRender();
                    SparseArray<Float> path = new SparseArray<>();
                    path.put(pointcount++, normalizedX);
                    path.put(pointcount++, normalizedY);
                    pathMap.put(pathcount, path);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            renderer.handleTouchDrag(
                                    normalizedX, normalizedY);
                        }
                    });
                    requestRender();
                    pathMap.get(pathcount).put(pointcount++, normalizedX);
                    pathMap.get(pathcount).put(pointcount++, normalizedY);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    pointcount = 0;
                    pathcount++;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    protected boolean handleSingleTouhEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getDownTime() - mLastTapTime < MAX_DCLICK_TIME
                        && Math.abs(mLastX0 - event.getX(0)) < MAX_DCLICK_DIS
                        && Math.abs(mLastY0 - event.getY(0)) < MAX_DCLICK_DIS) {
                    mHandler.removeCallbacks(mRunSingleClick);
                    handleDoubleClick();
                    return true;
                }
                mLastTapTime = event.getDownTime();
                mLastX0 = mDownX = event.getX(0);
                mLastY0 = mDownY = event.getY(0);
                return true;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "handleSingleTouhEvent: ACTION_MOVE" + getScale(mMatCanvas));
                float x0 = event.getX(0);
                float y0 = event.getY(0);
                float offsetX = x0 - mLastX0;
                float offsetY = y0 - mLastY0;
                mMatCanvas.postTranslate(offsetX, offsetY);
                setTransform(mMatCanvas);
                postInvalidate();
                mLastX0 = x0;
                mLastY0 = y0;
                return true;
            case MotionEvent.ACTION_UP:
                ensureTransform();
                if (event.getEventTime() - event.getDownTime() < MAX_DCLICK_TIME
                        && Math.abs(mDownX - event.getX(0)) < MAX_DCLICK_DIS
                        && Math.abs(mDownY - event.getY(0)) < MAX_DCLICK_DIS) {
                    mHandler.postDelayed(mRunSingleClick, MAX_DCLICK_TIME);
                }
                return true;
        }
        return false;
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
                scale = getScale(x0, y0, x1, y1);

                mMatCanvas.postScale(scale, scale, (x0 + x1) / 2, (y0 + y1) / 2);
                scale = getScale(mMatCanvas);
                if (scale > MAX_ZOOM) {
                    scale = MAX_ZOOM / scale;
                    mMatCanvas.postScale(scale, scale, (x0 + x1) / 2, (y0 + y1) / 2);
                }
                setTransform(mMatCanvas);
                postInvalidate();
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

    protected boolean handleDoubleClick() {
        if (mMatCanvas.isIdentity()) {
            Matrix mat = new Matrix(mMatCanvas);
            mat.setScale(MAX_ZOOM, MAX_ZOOM, getWidth() / 2f, getHeight() / 2f);
            transformTo(mat);
        } else {
            transformToIdentify();
        }
        return true;
    }

    private Runnable mRunSingleClick = new Runnable() {
        @Override
        public void run() {
            handleSingleClick();
        }
    };

    protected boolean handleSingleClick() {
        return false;
    }

    private void ensureTransform() {
        float[] pos = new float[]{0, 0, getWidth(), getHeight()};
        Log.e(TAG, "ensureTransform: " + mMatCanvas.toShortString());
        mMatCanvas.mapPoints(pos);
        Log.e(TAG, "l=" + pos[0] + " t=" + pos[1] + " r=" + pos[2] + " b=" + pos[3]);
        if (getWidth() > (pos[2] - pos[0])) {
            Log.e(TAG, "transformToIdentify: ");
            transformToIdentify();
            return;
        }
        float offsetX = 0.0f;
        float offsetY = 0.0f;
        if (pos[0] > 0) {
            offsetX = -pos[0];
        } else if (pos[2] < getWidth()) {
            offsetX = getWidth() - pos[2];
        }
        if (pos[1] > 0) {
            offsetY = -pos[1];
        } else if (pos[3] < getHeight()) {
            offsetY = getHeight() - pos[3];
        }
        if (Math.abs(offsetX) > 0f || Math.abs(offsetY) > 0f) {
            Matrix dst = new Matrix(mMatCanvas);
            dst.postTranslate(offsetX, offsetY);
            transformTo(dst);
        }
    }

    private float getScale(float x0, float y0, float x1, float y1) {
        double o = Math.sqrt((mLastX0 - mLastX1) * (mLastX0 - mLastX1) + (mLastY0 - mLastY1) * (mLastY0 - mLastY1));
        double n = Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
        return (float) (n / o);
    }

    public boolean onBackPressed() {
        if (mAnim.isAnimating()) return true;
        if (!mMatCanvas.isIdentity()) {
            transformToIdentify();
            return true;
        }
        return false;
    }

    public float getScale(Matrix m) {
        float[] v = new float[9];
        m.getValues(v);
        return v[0];
    }

    public void transformToIdentify() {
        Matrix adjustsMartrix = new Matrix();
        transformTo(adjustsMartrix);
    }

    private void transformTo(Matrix matDst) {
        mAnim.startAnimation(mMatCanvas, matDst, this);
    }

    public Matrix getScaleMatrix() {
        return mMatCanvas;
    }

    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onRefresh(Matrix mat) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTransform(mMatCanvas);
                postInvalidate();
            }
        });
    }
}
