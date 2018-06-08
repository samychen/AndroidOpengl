package com.cam001.gles;

import java.nio.FloatBuffer;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.opengl.GLES20;

public class Program {

	protected static final float VERTEXT_COOD[] = {
		-1.0f, -1.0f,
		1.0f, -1.0f,
		-1.0f,  1.0f,
		1.0f,  1.0f,
	};


	protected static final float TEXTURE_COOD[] = {
		0.0f, 0.0f,
		1.0f, 0.0f,
		0.0f, 1.0f,
		1.0f, 1.0f,
	};
	
	private int mProgram = 0;
	
	public Program(String vertext, String fragment) {
		mProgram = ShaderUtil.createProgram(vertext, fragment);
		ShaderUtil.checkGlError("Program.init");
	}
	
	public boolean hasUniform(String uniform) {
		GLES20.glUseProgram(mProgram);
		return GLES20.glGetUniformLocation(mProgram, uniform)!=-1;
	}
	
	public void setUniform1i(String uniform, int value) {
		GLES20.glUseProgram(mProgram);
		int location = GLES20.glGetUniformLocation(mProgram, uniform);
		if(location>-1) {
			GLES20.glUniform1i(location, value);
		}
		ShaderUtil.checkGlError("Program.setUniform1i");
	}
	
	public void setUniformNf(String uniform, float[] value) {
		switch(value.length) {
		case 1:
			setUniform1f(uniform, value[0]);
			break;
		case 2:
			setUniform2f(uniform, value[0], value[1]);
			break;
		case 3:
			setUniform3f(uniform, value[0], value[1], value[2]);
			break;
		}
	}
	
	public void setUniform1f(String uniform, float value) {
		GLES20.glUseProgram(mProgram);
		int location = GLES20.glGetUniformLocation(mProgram, uniform);
		if(location>-1) {
			GLES20.glUniform1f(location, value);
		}
		ShaderUtil.checkGlError("Program.setUniform1f");
	}
	
	public void setUniform2f(String uniform, float v1, float v2) {
		GLES20.glUseProgram(mProgram);
		int location = GLES20.glGetUniformLocation(mProgram, uniform);
		if(location>-1) {
			GLES20.glUniform2f(location, v1, v2);
		}
		ShaderUtil.checkGlError("Program.setUniform2f");
	}
	
	public void setUniform3f(String uniform, float v1, float v2, float v3) {
		GLES20.glUseProgram(mProgram);
		int location = GLES20.glGetUniformLocation(mProgram, uniform);
		if(location>-1) {
			GLES20.glUniform3f(location, v1, v2, v3);
		}
		ShaderUtil.checkGlError("Program.setUniform3f");
	}
	
	public void setUniformMatrix3fv(String uniform, float[] mat) {
		GLES20.glUseProgram(mProgram);
		int location = GLES20.glGetUniformLocation(mProgram, uniform);
		if(location>-1) {
			GLES20.glUniformMatrix3fv(location, 1, false, mat, 0);
		}
		ShaderUtil.checkGlError("Program.setUniformMatrix3fv");
	}
	
	protected void setVetextAttribPointer(String attr, float[] value) {
		GLES20.glUseProgram(mProgram);
		int location = GLES20.glGetAttribLocation(mProgram, attr);
		if(location>-1) {
			GLES20.glEnableVertexAttribArray(location);
			FloatBuffer ptr = ShaderUtil.floatToBuffer(value);
			GLES20.glVertexAttribPointer(location, 2, GLES20.GL_FLOAT, false, 0, ptr);
		}
		ShaderUtil.checkGlError("Program.setVetextAttribPointer");
	}
	
	public void draw() {
		GLES20.glUseProgram(mProgram);
		ShaderUtil.checkGlError("Program.draw1");
		GLES20.glClearColor(0, 0, 0, 1);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		ShaderUtil.checkGlError("Program.draw2");
		
		setVetextAttribs();
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		ShaderUtil.checkGlError("Program.draw");
	}
	
	protected void setVetextAttribs() {
		setVetextAttribPointer("aPosition", VERTEXT_COOD);
		setVetextAttribPointer("aTextureCoord", TEXTURE_COOD);
	}
	
	public void readPixels(Bitmap outBmp) {
		int width = outBmp.getWidth();
		int height = outBmp.getHeight();
		
		FBO fbo = new FBO();
		fbo.initFBO();
		fbo.setTexSize(width, height);
		fbo.bindFrameBuffer();
		
		GLES20.glViewport(0, 0, width, height);
		draw();
		ShaderUtil.glReadPixelsToBitmap(outBmp);
		
		fbo.unbindFrameBuffer();
		fbo.uninitFBO();
	}
	
	public void recycle() {
		GLES20.glDeleteProgram(mProgram);
		ShaderUtil.checkGlError("Program.recycle");
	}
	
	private HashMap<String, Integer> mTexMap = new HashMap<String, Integer>();
	public void setUniformTexture(String uniform, Texture tex) {
		GLES20.glUseProgram(mProgram);
		int texIndex = 0;
		if(mTexMap.containsKey(uniform)) {
			texIndex = mTexMap.get(uniform);
		} else {
			texIndex = mTexMap.size();
			mTexMap.put(uniform, texIndex);
		}
		tex.bind(texIndex);
		setUniform1i(uniform, texIndex);
	}
	
}
