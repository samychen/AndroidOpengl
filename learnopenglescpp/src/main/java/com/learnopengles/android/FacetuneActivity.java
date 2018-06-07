package com.learnopengles.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.learnopengles.android.cpp.R;
import com.learnopengles.android.lesson4.LessonFourNativeRenderer;
import com.learnopengles.android.render.ImageRender;

import java.io.IOException;
import java.io.InputStream;

public class FacetuneActivity extends Activity implements View.OnClickListener{

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
        int width = bmp.getWidth();
        int height = bmp.getHeight();
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
                    final float normalizedX =event.getX() / (float) v.getWidth()*600;
                    final float normalizedY =event.getY() / (float) v.getHeight()*784;

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

        }
    }
}
