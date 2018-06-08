package com.cam001.gles;

import java.nio.IntBuffer;

import com.cam001.util.DebugUtil;

import android.opengl.GLES20;

public class FBO {
	private IntBuffer mFBOTexture = null;
	private IntBuffer mFBOFrameBuffer = null;
	
	private Texture mTexWrapper = null;
	
	private int mWidth = 0;
	private int mHeight = 0;
	
	public void initFBO() {
    	mFBOFrameBuffer = IntBuffer.allocate(1);
    	GLES20.glGenFramebuffers(1, mFBOFrameBuffer);

    	mFBOTexture = IntBuffer.allocate(1);
    	GLES20.glGenTextures(1, mFBOTexture);
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mFBOTexture.get(0));
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    	ShaderUtil.checkGlError("FBO.initFBO");
	}
	
	public void setTexSize(int width, int height) {
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mFBOTexture.get(0));
    	GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
    	mWidth = width;
    	mHeight = height;
    	mTexWrapper = new Texture(mFBOTexture.get(0), mWidth, mHeight);
        ShaderUtil.checkGlError("FBO.setTexSize");
	}
	
	public void bindFrameBuffer() {
    	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBOFrameBuffer.get(0));

    	GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFBOTexture.get(0), 0);
//    	checkGlError("CreateFBO");
    	int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
    	if(status!=GLES20.GL_FRAMEBUFFER_COMPLETE) {
    		DebugUtil.logE("", "glCheckFramebufferStatus=%d", status);
    	}
    	GLES20.glViewport(0, 0, mWidth, mHeight);
    	ShaderUtil.checkGlError("FBO.bindFrameBuffer");
	}
	
	public int getTexName() {
		return mFBOTexture.get(0);
	}
	
	public Texture getTexture() {
		return mTexWrapper;
	}
	
	public void unbindFrameBuffer() {
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    	ShaderUtil.checkGlError("FBO.unbindFrameBuffer");
	}
	
	public void uninitFBO() {
    	GLES20.glDeleteTextures(1, mFBOTexture);
    	GLES20.glDeleteFramebuffers(1, mFBOFrameBuffer);
    	ShaderUtil.checkGlError("FBO.uninitFBO");
	}
}
