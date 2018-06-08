package com.cam001.photoeditor.beauty.makeup.widget;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.util.AttributeSet;

public class GLImageView extends GLRootView{
	
	 
	private float[]	mPointPos = null;
	private float[] mOpenglPos = null;
	
	 
	public GLImageView(Context context) {
		super(context);
		
		 
	}

	public GLImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		 
	}
	
	 
	 
	/**
	 * ת��ͼƬ��굽opengl���
	 * @param pos3d
	 * @param posPic
	 */
	public void calcOpenglPosByPicturePos(float[] pos3d, float[] posPic){
		float sx = (float)posPic[0]/mForeTexList.get(mDisplayTexIndex).texSize.width;
		float sy = (float)posPic[1]/mForeTexList.get(mDisplayTexIndex).texSize.height;
		
		pos3d[0] = mForeVertexs[0] + (mForeVertexs[3] - mForeVertexs[0])*sx;
		pos3d[1] = mForeVertexs[7] + (mForeVertexs[1] - mForeVertexs[7])*sy;
		pos3d[2] = 1.0f;
	}
	
	public void convertOpenglToPicture(float[] posPic, float[] pos3d){
		float sx = (float)(pos3d[0] - mForeVertexs[0])/(mForeVertexs[3] - mForeVertexs[0]);
		float sy = (float)(pos3d[1] - mForeVertexs[7])/(mForeVertexs[1] - mForeVertexs[7]);
		
		posPic[0] = mForeTexList.get(mDisplayTexIndex).texSize.width * sx;
		posPic[1] = mForeTexList.get(mDisplayTexIndex).texSize.height * sy;
	}
	
	/**
	 * ת��ͼƬ���㵽��Ļ����
	 * @param screenPos
	 * @param posPic
	 */
	public void convertPicturePointToScreenPoint(float[] screenPoint, float[] picPoint){
		float pos3d[] = new float[3];
		float screen2d[] = new float[2];
		float pic2d[] = new float[2];
		
		int size = screenPoint.length /2 ;
		
		for(int i=0; i<size; i++)
		{
			pic2d[0] = picPoint[2*i];
			pic2d[1] = picPoint[2*i+1];
			calcOpenglPosByPicturePos(pos3d, pic2d);
			convertOpenglToScreen(screen2d, pos3d);
			screenPoint[2*i] = screen2d[0];
			screenPoint[2*i+1] = screen2d[1];
		}
		
	}
	
	/**
	 * ת����Ļ���㵽ͼƬ����
	 * @param picPoint
	 * @param screenPoint
	 * @param index		screenPoint������
	 */
	public void convertScreenPointToPicture(float[] picPoint, float[] screenPoint, int index){
		float pos3d[] = new float[3];
		float screen2d[] = new float[2];
		float pic2d[] = new float[2];
		
		screen2d[0] = screenPoint[2*index];
		screen2d[1] = screenPoint[2*index+1];
		
		convertScreenToOpengl(pos3d, screen2d);
		convertOpenglToPicture(pic2d, pos3d);
		
		picPoint[2*index] = pic2d[0];
		picPoint[2*index+1] = pic2d[1];
	}
	
	 

	@Override
	protected void drawOwnObject(GL11 gl) {
		super.drawOwnObject(gl);
		
		 
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		super.onSurfaceCreated(gl, config);
 
	}

}
