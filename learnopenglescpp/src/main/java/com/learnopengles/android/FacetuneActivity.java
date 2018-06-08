package com.learnopengles.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.learnopengles.android.cpp.R;
import com.learnopengles.android.render.ImageRender;

import java.io.IOException;
import java.io.InputStream;

public class FacetuneActivity extends Activity implements View.OnClickListener,View.OnTouchListener{

    private GLSurfaceView mGLSurfaceView;
    private static final String TAG = "FacetuneActivity";
    ImageRender imageRender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facetune);
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        findViewById(R.id.button_teethwhite).setOnClickListener(this);
        findViewById(R.id.button_smooth).setOnClickListener(this);
        findViewById(R.id.button_bigsmooth).setOnClickListener(this);
        findViewById(R.id.button_detail).setOnClickListener(this);
        findViewById(R.id.button_erase).setOnClickListener(this);
        findViewById(R.id.button_refuse).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.img_compare).setOnTouchListener(this);
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
        String picpath = "texture/face.jpg";
        InputStream in = null;
        AssetManager assetManager = getAssets();
        try {
            in = assetManager.open(picpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeStream(in, null, op);
        final int width = bmp.getWidth();
        final int height = bmp.getHeight();
        Log.e(TAG, "bmp width=: "+bmp.getWidth()+"height="+bmp.getHeight() );
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bmp.recycle();
        }
        imageRender = new ImageRender(this,width,height,picpath);
        if (supportsEs2) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(imageRender);
        } else {
            return;
        }
        mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    final float normalizedX =event.getX() / (float) v.getWidth()*width;
                    final float normalizedY =event.getY() / (float) v.getHeight()*height;
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mGLSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                imageRender.handleTouchPress(
                                        normalizedX, normalizedY);
                            }
                        });
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        mGLSurfaceView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                imageRender.handleTouchDrag(
                                        normalizedX, normalizedY);
                            }
                        });
                    }
                    //
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        // 手指压下屏幕
                        case MotionEvent.ACTION_DOWN:
                            mode = MODE_DRAG;
                            // 记录ImageView当前的移动位置
//                            currentMatrix = mGLSurfaceView.getMatrix();

//                    currentMatrix.set(imageView.getImageMatrix());
                            startPoint.set(event.getX(), event.getY());
                            Log.e(TAG, "onTouch: "+currentMatrix.toShortString() );
                            break;
                        // 手指在屏幕上移动，改事件会被不断触发
                        case MotionEvent.ACTION_MOVE:
                            // 拖拉图片
                            if (mode == MODE_DRAG) {
                                float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                                float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                                // 在没有移动之前的位置上进行移动
                                matrix.set(currentMatrix);
                                matrix.postTranslate(dx, dy);
                                Log.e(TAG, "onTouch: "+matrix.toShortString() );
                            }
                            // 放大缩小图片
                            else if (mode == MODE_ZOOM) {
                                float endDis = distance(event);// 结束距离
                                if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                                    float scale = endDis / startDis;// 得到缩放倍数
                                    matrix.set(currentMatrix);
                                    matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                                    Log.e(TAG, "onTouch: "+matrix.toShortString() );
                                }
                            }
                            break;
                        // 手指离开屏幕
                        case MotionEvent.ACTION_UP:
                            // 当触点离开屏幕，但是屏幕上还有触点(手指)
                        case MotionEvent.ACTION_POINTER_UP:
                            mode = 0;
                            break;
                        // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                        case MotionEvent.ACTION_POINTER_DOWN:
                            mode = MODE_ZOOM;
                            /** 计算两个手指间的距离 */
                            startDis = distance(event);
                            /** 计算两个手指间的中间点 */
                            if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                                midPoint = mid(event);
                                //记录当前ImageView的缩放倍数
//                        currentMatrix.set(imageView.getImageMatrix());
                                currentMatrix = mGLSurfaceView.getMatrix();
                                Log.e(TAG, "onTouch: "+matrix.toShortString() );
                            }
                            break;
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        imageRender.destroy();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_teethwhite:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(1);
                    }
                });
                break;
            case R.id.button_smooth:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(2);
                    }
                });
                break;
            case R.id.button_bigsmooth:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(3);
                    }
                });
                break;
            case R.id.button_detail:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(4);
                    }
                });
                break;
            case R.id.button_erase:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(5);
                    }
                });
                break;
            case R.id.button_refuse:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(6);
                    }
                });
                break;
            case R.id.button_save:
                mGLSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        imageRender.releaseEffect(7);
                    }
                });
                break;
            default:
                break;
        }
    }
    /** 记录是拖拉照片模式还是放大缩小照片模式 */
    private int mode = 0;// 初始状态
    /** 拖拉照片模式 */
    private static final int MODE_DRAG = 1;
    /** 放大缩小照片模式 */
    private static final int MODE_ZOOM = 2;
    /** 用于记录开始时候的坐标位置 */
    private PointF startPoint = new PointF();
    /** 用于记录拖拉图片移动的坐标位置 */
    private Matrix matrix = new Matrix();
    /** 用于记录图片要进行拖拉时候的坐标位置 */
    private Matrix currentMatrix = new Matrix();
    /** 两个手指的开始距离 */
    private float startDis;
    /** 两个手指的中间点 */
    private PointF midPoint;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_UP){
            Log.e(TAG, "action up" );
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    ImageRender.nativeCompare(0);
                }
            });
        } else if (event.getAction()==MotionEvent.ACTION_DOWN){
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    ImageRender.nativeCompare(1);
                }
            });
        }
        return true;
    }
    /** 计算两个手指间的距离 */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        /** 使用勾股定理返回两点之间的距离 */
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /** 计算两个手指间的中间点 */
    private PointF mid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
}
