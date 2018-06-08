package com.cam001.photoeditor.beauty.makeup.widget;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.opengl.GLUtils;

import com.cam001.service.LogUtil;


public class ImageTexture {
	
	public static final String TAG = "ImageTextue";
	
	public float mNormalizedWidth = 0.0f;
	public float mNormalizedHeight = 0.0f;
	
	public int texture[] = new int[1];
	public GLRootView.Size texSize = new GLRootView.Size();
	private IntBuffer intBuf = IntBuffer.allocate(1);
	private GL10 mGL10;
	private boolean mIsAvailable;		//�����Ƿ����
	public float mLeft, mRight, mTop, mBottom;
	
	public ImageTexture(GL10 gl, Bitmap bitmap){
		mGL10 = gl;
		mIsAvailable = false;

		if (null == bitmap){
			texture[0] = -1;
			return ;
		}
		
		texSize.width = bitmap.getWidth();
		texSize.height = bitmap.getHeight();

		gl.glGenTextures(1, intBuf);
		texture = intBuf.array();
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		int glError = gl.glGetError();
		if (glError != GL10.GL_NO_ERROR){
			LogUtil.logV(TAG, "##########Texture creation fail, glError " + glError);
			mIsAvailable = false;
			texSize.width = 0;
			texSize.height = 0;
			return ;
		}
		
//		texSize.width = bitmap.getWidth();
//		texSize.height = bitmap.getHeight();
		mIsAvailable = true;
	}
	
	public ImageTexture(){
		mGL10 = null;
		mIsAvailable = false;
	}
	
	public void setGLContext(GL10 gl){
		mGL10 = gl;
	}
	
	public boolean isAvailable(){
		return mIsAvailable;
	}
	
	public int getTexId(){
		return texture[0];
	}
	
	public void setVertex(float l, float r, float t, float b){
		mLeft = l;
		mRight = r;
		mBottom = b;
		mTop = t;
	}
	
	public float[] getVertex(){
		return new float[]{
			mLeft, mBottom, 1,
			mRight, mBottom, 1,
			mLeft, mTop, 1,
			mRight, mTop, 1
		};
	}
	
	public void load(Bitmap bitmap){
		if (null == bitmap || bitmap.isRecycled())
			return ;
		
		mGL10.glGenTextures(1, intBuf);
		texture = intBuf.array();
		
		float[] normalSize = new float[2];
		Jni_method.loadTexture(bitmap, texture[0], normalSize);
		
		mNormalizedWidth = normalSize[0];
		mNormalizedHeight = normalSize[1];
		
		texSize.width = bitmap.getWidth();
		texSize.height = bitmap.getHeight();
//		
//		Bitmap bitmap2 = powOfBmp(bitmap);
////		bitmap.recycle();
//		float sd = (float)bitmap2.getDensity() / bitmap.getDensity();
//		
//		mNormalizedWidth = (float)texSize.width / bitmap2.getWidth() * sd;
//		mNormalizedHeight = (float)texSize.height / bitmap2.getHeight() * sd;
//		
//		mGL10.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
////		mGL10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
////		mGL10.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
//		mGL10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
//		mGL10.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
////		mGL10.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
//		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap2, 0);
//		bitmap2.recycle();
		
		int glError = mGL10.glGetError();
		if (glError != GL10.GL_NO_ERROR){
			LogUtil.logV(TAG, "##########Texture creation fail, glError " + glError);
			mIsAvailable = false;
			texSize.width = 0;
			texSize.height = 0;
			return ;
		}
		
		mIsAvailable = true;
	}
	
	public void release(){
		mGL10.glDeleteTextures(1, texture, 0);
		mIsAvailable = false;
	}
	
	public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h){
	  	  
	  	  int  src_w = bitmap.getWidth();
	  	  int  src_h = bitmap.getHeight();
	  	  float scale_w = ((float)dst_w)/src_w;
	  	  float  scale_h = ((float)dst_h)/src_h;
	  	  Matrix  matrix = new Matrix();
	  	  matrix.postScale(scale_w, scale_h);
	  	  Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);
	  	  
	  	  return dstbmp;
	}
	
	public Bitmap powOfBmp(Bitmap bmp){
		Bitmap result = null;
		
		int width = (int) ccNextPOT(bmp.getWidth());
		int height = (int) ccNextPOT(bmp.getHeight());
		
//		int len = Math.max(width, height);
		
		result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		
//		Paint red = new Paint();
//		red.setColor(Color.RED);
//		canvas.drawRect(new Rect(0, 0, result.getWidth(), result.getHeight()), red);
		
		canvas.drawBitmap(bmp, 0, 0, null);
		
		return result;
	}
	
	public long ccNextPOT(long x)
	{
		x = x - 1;
		x = x | (x >> 1);
		x = x | (x >> 2);
		x = x | (x >> 4);
		x = x | (x >> 8);
		x = x | (x >>16);
		return x + 1;
	}

}
