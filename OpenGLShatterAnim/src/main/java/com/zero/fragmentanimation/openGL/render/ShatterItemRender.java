package com.zero.fragmentanimation.openGL.render;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author linzewu
 * @date 2017/7/11
 */

public class ShatterItemRender implements IShatterItemRender {
    
    FloatBuffer mVertFloatBuffer;
    FloatBuffer mTextureFloatBuffer;
    int mProgramPointer;
    int mDataSize;
    
    public ShatterItemRender(int programPointer, float[] vertData, float[] textureData) {
        mProgramPointer = programPointer;
        mVertFloatBuffer = getFloatBuffer(vertData);
        mTextureFloatBuffer = getFloatBuffer(textureData);
        mDataSize = vertData.length / 3;
    }
    
    @Override
    public void onRefreshRender() {
        if (mProgramPointer <= 0) {
            return ;
        }
        /* 清屏 */
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        /* 赋值顶点坐标 */
        int positionCoordinateHandle = GLES20.glGetAttribLocation(mProgramPointer, "a_Position");
        GLES20.glEnableVertexAttribArray(positionCoordinateHandle);
        GLES20.glVertexAttribPointer(positionCoordinateHandle,
                3,       /* 一个顶点3个坐标值 */
                GLES20.GL_FLOAT,
                false,
                0, 
                mVertFloatBuffer);
//        GLES20.glDisableVertexAttribArray(positionCoordinateHandle);

        /* 赋值纹理坐标 */
        int textureCoordinateHandle = GLES20.glGetAttribLocation(mProgramPointer, "a_TexCoordinate");
        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 
                2, 
                GLES20.GL_FLOAT,
                false,
                0, 
                mTextureFloatBuffer
        );
//        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
        /* 绘制碎片 */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mDataSize);
    }

    @Override
    public void onReleaseRender() {
        mVertFloatBuffer.clear();
        mTextureFloatBuffer.clear();
    }   
    
    
    private FloatBuffer getFloatBuffer(float[] data) {
        final FloatBuffer floatBuffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(data);
        floatBuffer.position(0);
        return floatBuffer;
    }
}
