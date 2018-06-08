package com.cam001.gles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.GLUtils;

public class Texture {

	private int mTexName = 0;
	private int mWidth = 0;
	private int mHeight = 0;
	
	public Texture() {
		mTexName = ShaderUtil.createTexture();
		ShaderUtil.checkGlError("Texture.init");
	}
	
	protected Texture(int texName, int width, int height) {
		mTexName = texName;
		mWidth = width;
		mHeight = height;
	}
	
	public void load(Bitmap bmp) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
//        ShaderUtil.glTexImage2DBitmap(bmp);
		ShaderUtil.checkGlError("Texture.load");
		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();
	}

    public void load(byte[] img) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
        Point size = new Point();
        ShaderUtil.glTexImage2DJpeg(img, size);
        ShaderUtil.checkGlError("Texture.load");
        mWidth = size.x;
        mHeight = size.y;
    }

    public void loadWidthMipmap(Bitmap bmp) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        ShaderUtil.checkGlError("Texture.load");
        mWidth = bmp.getWidth();
        mHeight = bmp.getHeight();
    }
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
    public void bind(int i) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
        ShaderUtil.checkGlError("Texture.bind");
    }

    public void unbind(int i) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        ShaderUtil.checkGlError("Texture.unbind");
    }
	
	public int getTexName() {
		return mTexName;
	}
	
	public void recycle() {
		GLES20.glDeleteTextures(1, new int[]{mTexName}, 0);
		ShaderUtil.checkGlError("Texture.recycle");
	}
	
}
