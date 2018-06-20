package com.ufotosoft.facetune;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.ufotosoft.facetune.gles.EffectRender;
import com.ufotosoft.facetune.gles.GLTextureViewImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener,View.OnTouchListener{
    private GLTextureViewImpl mGLTextureView;
    private static final String TAG = "FacetuneActivity";
    EffectRender effectRender;
    private int width,height;
    private boolean moveFlag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //屏幕
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e("GLTextureViewImpl", "屏幕高:" + dm.heightPixels);
        //应用区域
        Rect outRect1 = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        Log.e("GLTextureViewImpl", "应用区顶部" + outRect1.top);
        Log.e("GLTextureViewImpl", "应用区高" + outRect1.height());
        //View绘制区域
        Rect outRect2 = new Rect();
        getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect2);
        Log.e("GLTextureViewImpl", "View绘制区域顶部-错误方法：" + outRect2.top);   //不能像上边一样由outRect2.top获取，这种方式获得的top是0，可能是bug吧
        int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();   //要用这种方法
        Log.e("GLTextureViewImpl", "View绘制区域顶部-正确方法：" + viewTop);//不同手机不同
        Log.e("GLTextureViewImpl", "View绘制区域高度：" + outRect2.height());
        /**
         * 获取标题栏高度-方法1
         * 标题栏高度 = View绘制区顶端位置 - 应用区顶端位置(也可以是状态栏高度，获取状态栏高度方法3中说过了)
         * */
        int titleHeight1 = viewTop - outRect1.top;
        Log.e("GLTextureViewImpl", "标题栏高度-方法1：" + titleHeight1);
        /**
         * 获取标题栏高度-方法2
         * 标题栏高度 = 应用区高度 - View绘制区高度
         * */
        int titleHeight2 = outRect1.height() - outRect2.height();
        Log.e("GLTextureViewImpl", "标题栏高度-方法2：" + titleHeight2);
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
                    effectRender.compare(0);
                }
            });
            mGLTextureView.requestRender();
        } else if (event.getAction()==MotionEvent.ACTION_DOWN){
            mGLTextureView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    effectRender.compare(1);
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