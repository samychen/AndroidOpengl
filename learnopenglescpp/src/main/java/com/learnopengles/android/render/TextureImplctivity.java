package com.learnopengles.android.render;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.learnopengles.android.cpp.R;
import com.learnopengles.android.gles.EffectRender;
import com.learnopengles.android.gles.GLTextureViewImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class TextureImplctivity extends Activity implements View.OnClickListener,View.OnTouchListener{
    private GLTextureViewImpl mGLTextureView;
    private static final String TAG = "FacetuneActivity";
    EffectRender effectRender;
    private int width,height;
    private boolean moveFlag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_implctivity);
        mGLTextureView = (GLTextureViewImpl) findViewById(R.id.gl_texture_view);
        findViewById(R.id.button_teethwhite).setOnClickListener(this);
        findViewById(R.id.button_smooth).setOnClickListener(this);
        findViewById(R.id.button_bigsmooth).setOnClickListener(this);
        findViewById(R.id.button_detail).setOnClickListener(this);
        findViewById(R.id.button_erase).setOnClickListener(this);
        findViewById(R.id.button_refuse).setOnClickListener(this);
        findViewById(R.id.button_refuse_last).setOnClickListener(this);
        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.button_move).setOnClickListener(this);
        findViewById(R.id.img_compare).setOnTouchListener(this);
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
        width = bmp.getWidth();
        height = bmp.getHeight();
        Log.e(TAG, "bmp width=: "+bmp.getWidth()+"height="+bmp.getHeight() );
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bmp.recycle();
        }
        effectRender = new EffectRender(this,width,height,picpath );
        mGLTextureView.setIsCanTouch(true);
        mGLTextureView.setMoveFlag(moveFlag);
        mGLTextureView.setRenderer(effectRender);
        mGLTextureView.setPicSize(width,height);
        mGLTextureView.setActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_teethwhite:
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(1);
                    }
                });
                if (moveFlag){
                    moveFlag = !moveFlag;
                    mGLTextureView.setMoveFlag(moveFlag);
                }
                break;
            case R.id.button_smooth:
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(2);
                    }
                });
                if (moveFlag){
                    moveFlag = !moveFlag;
                    mGLTextureView.setMoveFlag(moveFlag);
                }
                break;
            case R.id.button_bigsmooth:
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(3);
                    }
                });
                if (moveFlag){
                    moveFlag = !moveFlag;
                    mGLTextureView.setMoveFlag(moveFlag);
                }
                break;
            case R.id.button_detail:
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(4);
                    }
                });
                if (moveFlag){
                    moveFlag = !moveFlag;
                    mGLTextureView.setMoveFlag(moveFlag);
                }
                break;
            case R.id.button_erase:
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(5);
                    }
                });
                if (moveFlag){
                    moveFlag = !moveFlag;
                    mGLTextureView.setMoveFlag(moveFlag);
                }
                break;
            case R.id.button_refuse:
                //TODO-保存后点击取消只能取消保存后的效果，onResume回来时还是取消了所有效果
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(6);
                    }
                });
                mGLTextureView.requestRender();
                mGLTextureView.clearPath();
                break;
            case R.id.button_refuse_last:
                //擦除最后一条效果模式
                final Map<Integer, SparseArray<Float>> lastPath = mGLTextureView.getLastPath();
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.removeLastEffect();//先切换到擦除模式
                        //绘制最后一条路径
                        effectRender.setPath(lastPath);
                    }
                });
                mGLTextureView.requestRender();
                break;
            case R.id.button_save:
                mGLTextureView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        effectRender.releaseEffect(7);
                    }
                });
                mGLTextureView.requestRender();
                break;
            case R.id.button_move:
                moveFlag = !moveFlag;
                mGLTextureView.setMoveFlag(moveFlag);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_UP){
            mGLTextureView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    EffectRender.nativeCompare(0);
                }
            });
            mGLTextureView.requestRender();
        } else if (event.getAction()==MotionEvent.ACTION_DOWN){
            mGLTextureView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    EffectRender.nativeCompare(1);
                }
            });
            mGLTextureView.requestRender();
        }
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mGLTextureView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLTextureView.onPause();
        effectRender.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
