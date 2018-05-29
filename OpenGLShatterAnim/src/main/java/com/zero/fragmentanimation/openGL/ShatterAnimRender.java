package com.zero.fragmentanimation.openGL;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.zero.fragmentanimation.R;
import com.zero.fragmentanimation.openGL.render.ShatterItemRender;
import com.zero.fragmentanimation.openGL.util.ShaderTools;
import com.zero.fragmentanimation.openGL.util.TextureTools;
import com.zero.fragmentanimation.openGL.util.VaryTools;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 渲染器
 * @author linzewu
 * @date 2017/7/11
 */

public class ShatterAnimRender implements GLSurfaceView.Renderer {
    
    private static final String TAG = "ShatterAnimRender";
    
    private GLSurfaceView mGLSurfaceView;
    
    /* 横轴上的碎片个数 */
    private int mFragNumberX;
    /* 纵轴上的碎片个数 */
    private int mFragNumberY;
    /* 宽高比 */
    private float mRatioValue;
    
    /* 矩阵工具类 */
    private VaryTools mVaryTools;
    /* 程序句柄 */
    private int mProgramPointer;
    /* 纹理句柄 */
    private int mTexturePointer;
    /* 动画执行类 */
    private ValueAnimator mValueAnimator;
    /* 动画进度 */
    private float mAnimFraction;
    
    private ShatterItemRender mFragItemRender;
    
    public ShatterAnimRender(GLSurfaceView glSurfaceView) {
        this.mGLSurfaceView = glSurfaceView;
        this.mVaryTools = new VaryTools();
    }
    
    private ShatterAnimListener mShatterAnimListener;
    public void setShatterAnimListener(ShatterAnimListener shatterAnimListener) {
        this.mShatterAnimListener = shatterAnimListener;
    }
    
    public void startAnimation(final Bitmap bitmap) {
        /* 主线程 */
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mValueAnimator = ValueAnimator.ofFloat(0f, 2f);
                mValueAnimator.setDuration(1500);
                mValueAnimator.setInterpolator(new LinearInterpolator());
                mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mAnimFraction = (float) animation.getAnimatedValue();
                        mGLSurfaceView.requestRender();
                    }
                });
                mValueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mShatterAnimListener != null) {
                            mShatterAnimListener.onAnimFinish();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mValueAnimator.start();
                if (mShatterAnimListener != null) {
                    mShatterAnimListener.onAnimStart();
                }
            }
        });
        
        /* GL线程处理纹理加载 */
        /* 如果在UI线程里调用渲染器的方法，很容易收到“call to OpenGL ES API with no current context”的警告 */
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mTexturePointer = TextureTools.loadTexture(bitmap);
                mGLSurfaceView.requestRender();
            }
        });
    }
    
    public void stopAnimation() {
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }
    
    public void destroy() {
        mGLSurfaceView = null;
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /* 清屏 */
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        /* 用于启用各种功能, 这里是启用隐藏图形材料的面 */
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        /* 
         * 用于控制多边形的正面是如何决定的
         * GL_CCW 表示窗口坐标上投影多边形的顶点顺序为逆时针方向的表面为正面
         * GL_CW 表示顶点顺序为顺时针方向的表面为正面
         */
        GLES20.glFrontFace(GLES20.GL_CW);
        /* 开启深度测试 */
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        /* 加载顶点着色器以及片元着色器 */
        mProgramPointer = ShaderTools.createProgram(mGLSurfaceView.getContext(), 
                R.raw.shatter_vert, R.raw.shatter_frag);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        /* 设置可视区域 */
        GLES20.glViewport(0, 0, width, height);
        /* 计算宽高比 */
        mRatioValue = (float) width / height;
        /* 设置视角矩阵 */
        mVaryTools.setCamera(0f, 0f, 0f, 0f, 0f, 10f, 0f, 1f, 0f);
        /* 设置投影矩阵 */
        mVaryTools.frustum(-mRatioValue, mRatioValue, -1.0f, 1.0f, 1.0f, 10f);
        
        mFragNumberX = 20;
        mFragNumberY = (int) (mFragNumberX / mRatioValue);
        
        /* GL线程初始化碎片数据 */
        float[] vertData = initPositionData();
        float[] textureData = initTextureData();
        mFragItemRender = new ShatterItemRender(mProgramPointer, vertData, textureData);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        if (mProgramPointer <= 0) {
            Log.d(TAG, "Program is not load.");
            return ;
        } 
        if (mTexturePointer <= 0) {
            Log.d(TAG, "Texture is not load.");
            return ;
        }
        if (mFragItemRender == null) {
            Log.d(TAG, "Frag item render is not init");
            return ;
        }
        
        /* 加载Program到OpenGL环境中 */
        GLES20.glUseProgram(mProgramPointer);
        Log.d(TAG, "load program");
        
        mVaryTools.pushMatrix();
        
        /* 加载动画位移偏移值 */
        int moveDistanceHandle = GLES20.glGetUniformLocation(mProgramPointer, "u_AnimationFraction");
        GLES20.glUniform1f(moveDistanceHandle, mAnimFraction);
        
        /* 加载变换矩阵 */
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgramPointer, "u_MVPMatrix");
        mVaryTools.translate(0, 0, 1.0001f);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mVaryTools.getFinalMatrix(), 0);

        /* 加载纹理 */
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturePointer);
        int textureHandle = GLES20.glGetUniformLocation(mProgramPointer, "u_Texture");
        GLES20.glUniform1i(textureHandle, 0);
        /* 绘制碎片纹理 */
        mFragItemRender.onRefreshRender();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        
        mVaryTools.popMatrix();
    }

    /**
     * 初始化顶点坐标数据
     */
    private float[] initPositionData() {
        /* 每个碎片都是一个正方形, 由6个顶点决定, 每个顶点由x, y, z三个方向决定 */
        float[] positionData = new float[6 * 3 * mFragNumberX * mFragNumberY];

        float height = 1f;
        float width = height * mRatioValue;

        final float stepX = width * 2f / mFragNumberX;
        final float stepY = height * 2f / mFragNumberY;

        final float minPositionX = -width;
        final float minPositionY = -height;

        int positionDataOffset = 0;
        for (int x = 0; x < mFragNumberX; x++) {
            for (int y = 0; y < mFragNumberY; y++) {

                float z = (float) Math.random();
                
                final float x1 = minPositionX + x * stepX;
                final float x2 = x1 + stepX;

                final float y1 = minPositionY + y * stepY;
                final float y2 = y1 + stepY;

                // Define points for a plane.
                final float[] p1 = {x1, y2, z};
                final float[] p2 = {x2, y2, z};
                final float[] p3 = {x1, y1, z};
                final float[] p4 = {x2, y1, z};

                int elementsPerPoint = p1.length;
                final int size = elementsPerPoint * 6;
                final float[] thisPositionData = new float[size];

                int offset = 0;
                // Build the triangles
                //  1---2
                //  | / |
                //  3---4
                for (int i = 0; i < elementsPerPoint; i++) {
                    thisPositionData[offset++] = p1[i];
                }
                for (int i = 0; i < elementsPerPoint; i++) {
                    thisPositionData[offset++] = p3[i];
                }
                for (int i = 0; i < elementsPerPoint; i++) {
                    thisPositionData[offset++] = p2[i];
                }
                for (int i = 0; i < elementsPerPoint; i++) {
                    thisPositionData[offset++] = p3[i];
                }
                for (int i = 0; i < elementsPerPoint; i++) {
                    thisPositionData[offset++] = p4[i];
                }
                for (int i = 0; i < elementsPerPoint; i++) {
                    thisPositionData[offset++] = p2[i];
                }

                System.arraycopy(
                        thisPositionData, 0,
                        positionData, positionDataOffset,
                        thisPositionData.length
                );
                positionDataOffset += thisPositionData.length;
            }
        }

        return positionData;
    }

    /**
     * 初始化纹理坐标数据
     */
    private float[] initTextureData() {
        float[] textureData = new float[6 * 2 * mFragNumberX * mFragNumberY];

        final float stepX = 1f / mFragNumberX;
        final float stepY = 1f / mFragNumberY;
        
        int textureDataOffset = 0;
        for (int x = mFragNumberX - 1; x >= 0; x--) {
            for (int y = mFragNumberY - 1; y >= 0; y--) {
                final float u0 = x * stepX;
                final float v0 = y * stepY;
                final float u1 = u0 + stepX;
                final float v1 = v0 + stepY;

                final int elementsPerPoint = 2;
                final int size = elementsPerPoint * 6;
                final float[] itemFrag = new float[size];

                int offset = 0;
                // Build the triangles
                //  1---2
                //  | / |
                //  3---4
                // Define points for a plane.

                final float[] p1 = {u1, v0};
                final float[] p2 = {u0, v0};
                final float[] p3 = {u1, v1};
                final float[] p4 = {u0, v1};

                for (int i = 0; i < elementsPerPoint; i++)
                    itemFrag[offset++] = p1[i];
                for (int i = 0; i < elementsPerPoint; i++)
                    itemFrag[offset++] = p3[i];
                for (int i = 0; i < elementsPerPoint; i++)
                    itemFrag[offset++] = p2[i];

                for (int i = 0; i < elementsPerPoint; i++)
                    itemFrag[offset++] = p3[i];
                for (int i = 0; i < elementsPerPoint; i++)
                    itemFrag[offset++] = p4[i];
                for (int i = 0; i < elementsPerPoint; i++)
                    itemFrag[offset++] = p2[i];

                System.arraycopy(
                        itemFrag, 0,
                        textureData, textureDataOffset,
                        itemFrag.length
                );
                textureDataOffset += itemFrag.length;
            }
        }

        return textureData;
    }
    
    
}
