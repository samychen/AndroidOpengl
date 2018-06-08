package com.cam001.photoeditor.beauty.makeup.widget;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;

import com.cam001.util.LogUtil;

public class GLRootView extends GLSurfaceView implements GLSurfaceView.Renderer{
	
	//public static final String TAG = "Tom";
	
	public static final float CAMERA_INIT_DIST = 20.0f;
	public static final float SCALE_FACTOR = 0.06f; // ����ϵ��
	public static final float DELTA = 0.0005f;
	public static final float	TEXTURE_NUM_MAX = 2;

	public static float mRadio = 0.0f;
	public static final boolean mbMargin = true;
	
	public static final float	POINT_RADIUS_FACTOR = 0.02f;
	
	public static final int	SURFACE_CREATE_MSG = 0x1000;
	public static final int SURFACE_CHANGE_MSG = 0x1001;
	public static final int	CAPUTRE_SCREEN_MSG = 0x1002;
	public static final int ANIMATION_FINISH	= 0x1003;
	public static final int POINT_INVISIABLE_MSG = 0x1004;
	public static final int POINT_VISIABLE_MSG = 0x1005;
	
	
	private int		mCurrentMsg = 0;

	protected int mWidth = 0;
	protected int mHeight = 0;

	protected float mFovy = 0.0f;
	protected float mWidthFactor = 1f;
	protected float mHeightFactor = 1f;

	private GL11 mGL11;
	
	private byte[] bytes = new byte[0];
	private byte[] bytes2 = new byte[0];
	
	private float[] mModelMatrix = new float[16];
	private float[] mProjMatrix = new float[16];
	private int[]	mViewport = new int[4];
	
	protected float mCameraInitPos[] = new float[3];
	protected float mCameraPos[] = new float[3];
	
	private boolean mIsZooming = false;
	
	private Size mFaceSize = new Size();
	private PointF mFaceCenter = new PointF();
	
	protected float mForeVertexs[] = new float[12];
	private float mBgVertexs[] = null;
	
	protected ImageTexture mBgTex	 	= null;		//��������
	protected List<ImageTexture>	mForeTexList = null;
	protected int		mDisplayTexIndex = 0;		//��ʾ����������
	private	boolean	mIsBgDraw = false;			//�Ƿ���Ⱦ����
	
	private UpdateTextureCallback	mUpdateForeTextureCallback = null;
	private UpdateTextureCallback	mUpdateBgTextureCallback = null;
	private InitTextureCallback		mInitTextureCallback = null;
	
	private ReentrantLock mRenderLock = new ReentrantLock();
	
	private float mDeltaX = 0, mDeltaY = 0, mS = 0;
	private float mCurrentScale = 1.0f;
	private float mNewPos[] = new float[2];
	private float mInitPos[] = new float[3];
	
	private int	mCaptureX = 0;
	private int mCaptureY = 0;
	private int mCaptureWidth = 0;
	private int mCaptureHeight = 0;
	private boolean mbCaptureScreen = false;
	private ByteBuffer mTmpBuffer = null;
	private int[]		mCapturePixels = null;
	private	Bitmap		mCaptureBmp		= null;
	
	private boolean bForceCameraPos = false;
	
	public GLRootView(Context context) {
		super(context);
		
		mFaceSize.width = 0;
		mBgTex = new ImageTexture();
//		initGLView();
		mForeTexList = new ArrayList<ImageTexture>();
		for (int i=0; i<TEXTURE_NUM_MAX; ++i){
			mForeTexList.add(new ImageTexture());
		}
	}

	public GLRootView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	//	Log.v(TAG, "GLRootView(Context context, AttributeSet attrs)");
		
		mFaceSize.width = 0;
		mBgTex = new ImageTexture();
//		initGLView();
		mForeTexList = new ArrayList<ImageTexture>();
		for (int i=0; i<TEXTURE_NUM_MAX; ++i){
			mForeTexList.add(new ImageTexture());
		}
	}
	
	public void initGLView() {

		mCameraPos[0] = 0.0f;
		mCameraPos[1] = 0.0f;
		mCameraPos[2] = CAMERA_INIT_DIST+1;
		
		mCameraInitPos[0] = mCameraPos[0];
		mCameraInitPos[1] = mCameraPos[1];
		mCameraInitPos[2] = mCameraPos[2];

		this.getHolder().setFormat(PixelFormat.RGBA_8888);
//		setEGLContextFactory(new ContextFactory());
//		setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 8, 0));
//		Log.v(TAG, "start Render");
		setRenderer(this);
	}
	
	private static class ContextFactory implements
	EGLContextFactory {
		
		public EGLContext createContext(EGL10 egl, EGLDisplay display,
				EGLConfig eglConfig) {
	//		Log.w(TAG, "creating OpenGL ES 2.0 context");
			checkEglError("Before eglCreateContext", egl);
			EGLContext context = egl.eglCreateContext(display, eglConfig,
					EGL10.EGL_NO_CONTEXT, null);
			checkEglError("After eglCreateContext", egl);
			return context;
		}
		
		public void destroyContext(EGL10 egl, EGLDisplay display,
				EGLContext context) {
				egl.eglDestroyContext(display, context);
			}
		}
		
		private static void checkEglError(String prompt, EGL10 egl) {
			int error;
			while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
				LogUtil.logE("OPENGL", String.format("%s: EGL error: 0x%x", prompt, error));
			}
		}
		
		private static class ConfigChooser implements
			EGLConfigChooser {
		
		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
			mRedSize = r;
			mGreenSize = g;
			mBlueSize = b;
			mAlphaSize = a;
			mDepthSize = depth;
			mStencilSize = stencil;
		}
		
		/*
		 * This EGL config specification is used to specify 2.0 rendering. We
		 * use a minimum size of 4 bits for red/green/blue, but will perform
		 * actual matching in chooseConfig() below.
		 */
		private static int[] s_configAttribs2 = { EGL10.EGL_RED_SIZE, 8,
				EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8,
				EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_SURFACE_TYPE,
				EGL10.EGL_WINDOW_BIT,
				// EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
				EGL10.EGL_NONE };
		
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		
			/*
			 * Get the number of minimally matching EGL configurations
			 */
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);
		
			int numConfigs = num_config[0];
		
			if (numConfigs <= 0) {
				throw new IllegalArgumentException(
						"No configs match configSpec");
			}
		
			/*
			 * Allocate then read the array of minimally matching EGL configs
			 */
			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs,
					num_config);
		
			/*
			 * Now return the "best" one
			 */
			return chooseConfig(egl, display, configs);
		}
		
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);
		
				// We need at least mDepthSize and mStencilSize bits
				if (d < mDepthSize || s < mStencilSize)
					continue;
		
				// We want an *exact* match for red/green/blue/alpha
				int r = findConfigAttrib(egl, display, config,
						EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config,
						EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config,
						EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config,
						EGL10.EGL_ALPHA_SIZE, 0);
		
				if (r == mRedSize && g == mGreenSize && b == mBlueSize
						&& a == mAlphaSize)
					return config;
			}
			return null;
		}
		
		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {
		
			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}
		
		// Subclasses can adjust these values:
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
		private int[] mValue = new int[1];
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		mRenderLock.lock();
		
		GL11 gl11 = (GL11) gl;
		mGL11 = gl11;
		gl11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		render(gl11);
		getConvertMatrix(gl11);
		
		if (mbCaptureScreen){
			mbCaptureScreen = false; 
			
			if (null != mCaptureBmp){
				Jni_method.copyScreen(mCaptureX, mHeight-mCaptureHeight-mCaptureY, mCaptureWidth, mCaptureHeight, mCaptureBmp);
				mCaptureBmp = null;
			}
			
			if(mCapturePixels != null){
			    IntBuffer intbuf = IntBuffer.wrap(mCapturePixels);
				mGL11.glReadPixels(mCaptureX, mHeight-mCaptureHeight-mCaptureY, mCaptureWidth, mCaptureHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, intbuf);
			    convertPixels(mCapturePixels, mCaptureWidth, mCaptureHeight);
			    mCapturePixels = null;
			}
			
			if(mTmpBuffer!=null){
				mGL11.glReadPixels(mCaptureX, mHeight-mCaptureHeight-mCaptureY, mCaptureWidth, mCaptureHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, mTmpBuffer);
			   convertPixels(mTmpBuffer.array(), mCaptureWidth, mCaptureHeight);
			   mTmpBuffer = null;
			}

			
			setCurrentMsg(CAPUTRE_SCREEN_MSG);
			
		}

		if (null != mInvalidateListener){
				mInvalidateListener.onChange(getCurrentMsg());
		}
		setCurrentMsg(0);
		
		mRenderLock.unlock();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	//	Log.v(TAG, "onSurfaceChanged");
		if (width > height){ 	//��������²�����Ⱦ
			return ;
		}
		
		mWidth = width;
		mHeight = height;
		mRadio = (float) width / height;

		GL11 gl11 = (GL11) gl;
		mGL11 = gl11;

		gl11.glViewport(0, 0, width, height);

		gl11.glMatrixMode(GL11.GL_PROJECTION);
		gl11.glLoadIdentity();

		float tmp = (float) Math.atan(1.0 / (CAMERA_INIT_DIST));
		mFovy = (float) (tmp * 180 * 2 / Math.PI);
		perspective(mFovy, mRadio, 1.0f, 100.0f);

		gl11.glMatrixMode(GL11.GL_MODELVIEW);
		gl11.glLoadIdentity();

		gl11.glEnable(GL11.GL_CULL_FACE); // ���ö�������Ϊ��ʱ��
		gl11.glFrontFace(GL11.GL_CCW);
		gl11.glCullFace(GL11.GL_BACK);
		
		mBgVertexs = new float[]{
			-mRadio, -1, 1,
			mRadio, -1, 1,
			-mRadio, 1, 1,
			mRadio, 1, 1
		};
		
		if (null != mInitTextureCallback){
			mInitTextureCallback.onLoad();
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//		Log.v(TAG, "onSurfaceCreated");
		mGL11 = (GL11) gl;
		
		for (int i=0; i<TEXTURE_NUM_MAX; ++i){
			mForeTexList.get(i).setGLContext(gl);
		}
		mBgTex.setGLContext(mGL11);

		mGL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);
		mGL11.glClearColor(0, 0, 0, 0);
		mGL11.glShadeModel(GL11.GL_SMOOTH);

		mGL11.glEnable(GL11.GL_TEXTURE_2D);
		mGL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE,
				GL11.GL_REPLACE);

		mGL11.glClearDepthf(1.0f);
		mGL11.glEnable(GL11.GL_DEPTH_TEST);
		mGL11.glDepthFunc(GL11.GL_LEQUAL);
		mGL11.glEnable(GL11.GL_SCISSOR_TEST);

		this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
	
	/***************************���ŵ��ⲿ�ĺ���****************************************/
	
	public void setUpdateForeTexCallback(UpdateTextureCallback l){
		mUpdateForeTextureCallback = l;
	}
	
	public void setUpdateBgTexCallback(UpdateTextureCallback l){
		mUpdateBgTextureCallback = l;
	}
	
	public void setInitTextureCallback(InitTextureCallback l){
		mInitTextureCallback = l;
	}
	
	public void setDisplayTextureIndex(int index){
		mDisplayTexIndex = index;
	}
	
	public void invalidate(int index){
		mDisplayTexIndex = index;
		requestRender();
	}
	
	public float[] getCameraPos(){
		return new float[]{mCameraPos[0], mCameraPos[1], mCameraPos[2]};
	}
	
	 
	public void setCameraPos(float[] pos){
		mCameraPos[0] = pos[0];
		mCameraPos[1] = pos[1];
		mCameraPos[2] = pos[2];
		bForceCameraPos = true;
	}
	
	public void setInitRect(Rect rect){
		mFaceCenter.x = (rect.left + rect.right) / 2;
		mFaceCenter.y = (rect.top + rect.bottom) / 2;
		mFaceSize.width = rect.right - rect.left;
		mFaceSize.height = rect.bottom - rect.top;
	}
	
	/**
	 * ��һ�μ���ǰ��ͼƬ
	 * @param bmp
	 */
	public void loadForeTexture(Bitmap bmp, int index) {
		
		 
		
		if (index >= TEXTURE_NUM_MAX || index < 0) {
			try {
				throw new Exception("mForeTexList out of index");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ;
		};
		
		ImageTexture mForeTex = mForeTexList.get(index);
		
		if (mForeTex.isAvailable())
			mForeTex.release();
		mForeTex.load(bmp);

		mDisplayTexIndex = index;
		
		float position[] = new float[4];
		calcPosition(mForeTex.texSize, position);
		mForeVertexs[0] = position[2];
		mForeVertexs[1] = position[1];
		mForeVertexs[2] = 1;
		mForeVertexs[3] = position[3];
		mForeVertexs[4] = position[1];
		mForeVertexs[5] = 1;
		mForeVertexs[6] = position[2];
		mForeVertexs[7] = position[0];
		mForeVertexs[8] = 1;
		mForeVertexs[9] = position[3];
		mForeVertexs[10] = position[0];
		mForeVertexs[11] = 1;

		 if (0 == mFaceSize.width){
			 return;
		 }
		
		 float sx = (float)mFaceCenter.x/mForeTex.texSize.width;
		 float sy = (float)mFaceCenter.y/mForeTex.texSize.height;
		 mCameraInitPos[0] = position[2] + (position[3] - position[2])*sx;
		 mCameraInitPos[1] = position[0] + (position[1] - position[0])*sy;
		
		 float sz = (float)(mFaceSize.width)/(mForeTex.texSize.width);
		 mCameraInitPos[2] = (sz * CAMERA_INIT_DIST) + 1;
		 
		 if(bForceCameraPos == false)
		 {
			 mCameraPos[0] = mCameraInitPos[0];
			 mCameraPos[1] = mCameraInitPos[1];
			 mCameraPos[2] = mCameraInitPos[2];
		 }
		
		 
		 if (null != mUpdateForeTextureCallback){
				mUpdateForeTextureCallback.onFinish(bmp);
		 }
	}

	/**
	 * ����ʾ��ͼƬ
	 * 
	 * @param bmp
	 */
	public void updateForeTexture(final Bitmap bmp, final int index) {
		
	 
		if(bmp == null || bmp.isRecycled() == true){
			return;
		}
		
		synchronized (bytes){
			queueEvent(new Runnable(){
				@Override
				public void run() {
					if (index >= TEXTURE_NUM_MAX || index < 0) {
						try {
							throw new Exception("mForeTexList out of index");
						} catch (Exception e) {
							e.printStackTrace();
						}
						return ;
					};
					
					ImageTexture mForeTex = mForeTexList.get(index);
					
					if (mForeTex.isAvailable())
						mForeTex.release();
					mForeTex.load(bmp);
					mDisplayTexIndex = index;
					if (null != mUpdateForeTextureCallback){
						mUpdateForeTextureCallback.onFinish(bmp);
					}
					
					requestRender();
				}
			});
		}
	}
	
	/**
	 * ���±���ͼƬ
	 * @param bmp
	 */
	public void updateBgTex(final Bitmap bmp){
		synchronized (bytes){
			queueEvent(new Runnable(){
				@Override
				public void run() {
					if (mBgTex.isAvailable())
						mBgTex.release();
					mBgTex.load(bmp);
					if (null != mUpdateBgTextureCallback){
						mUpdateBgTextureCallback.onFinish(bmp);
					}
					requestRender();
				}
			});
		}
	}
	
	/**
	 * ���ñ���ͼƬ
	 * @param bmp
	 */
	public void setBackground(Bitmap bmp){
		mIsBgDraw = true;
		if (mBgTex.isAvailable())
			mBgTex.release();
		mBgTex.load(bmp);
		if (null != mUpdateBgTextureCallback){
			mUpdateBgTextureCallback.onFinish(bmp);
		}
	}
	
	/**
	 * �ƶ�ͼƬ
	 * @param dx
	 * @param dy
	 */
	public void onTranslate(float dx, float dy) {
		mIsZooming = false;
		setCurrentMsg(SURFACE_CHANGE_MSG);
		startTransAnimation(dx, dy);
	}
	
	public void onLeaveAnimation() {
		LogUtil.logV("Tom", "onLeaveAnimation");
		mIsZooming = false;
		startAnimation();
	}

	/**
	 * ����ͼƬ
	 * @param point
	 * @param value
	 */
	public void onImageScale(PointF point, float value){
		LogUtil.logV("Tom", "onScale******");
		mCurrentScale = value;
		mIsZooming = true;
		mNewPos[0] = point.x;
		mNewPos[1] = point.y;
		
		setCurrentMsg(SURFACE_CHANGE_MSG);
		
		requestRender();
	}
	
	/**
	 * ����Camera��ԭ��
	 */
	public void onRevert() {
		mIsZooming = false;
		mNewPos[0] = mWidth / 2;
		mNewPos[1] = mHeight / 2;
		mCameraPos[0] = mCameraInitPos[0];
		mCameraPos[1] = mCameraInitPos[1];
		mCameraPos[2] = mCameraInitPos[2];

		requestRender();
	}
	
	/**
	 * ������Ļ
	 * @param x ��Ļ�ϵ�x���
	 * @param y ��Ļ�ϵ�y���
	 * @param w ��ȡ�Ŀ��
	 * @param h ��ȡ�ĸ߶�
	 * @param pixels ��ȡ���Ľ���ŵĻ���
	 */
	public void copyScreen(int x, int y, int w, int h, ByteBuffer pixels){
		mbCaptureScreen = true;
		mTmpBuffer = pixels;
		mCaptureX = x;
		mCaptureY = y;
		mCaptureWidth = w;
		mCaptureHeight = h;
		
		requestRender();
	}
	
	public void copyScreen(int x, int y, int w, int h, int[] pixels){
		mbCaptureScreen = true;
		mCapturePixels = pixels;
		mCaptureX = x;
		mCaptureY = y;
		mCaptureWidth = w;
		mCaptureHeight = h;
		
		requestRender();
	}
	
	public void copyScreen(int x, int y, int width, int height, Bitmap bmp){
		mbCaptureScreen = true;
		mCaptureBmp = bmp;
		mCaptureX = x;
		mCaptureY = y;
		mCaptureWidth = width;
		mCaptureHeight = height;
		
		requestRender();
	}
	
	/*******************************************************************/
	private void calcPosition(Size size, float[] positon) {
		int bmpW = size.width;
		int bmpH = size.height;

		if (bmpW == 0 || bmpH == 0) {
			positon[0] = 0;
			positon[1] = 0;
			positon[2] = 0;
			positon[3] = 0;
			return;
		}

		float top, bottom, left, right;

		if (bmpW / bmpH >= mRadio) { // ͼƬ��ȳ���ȫ��
			float h = (2 * mRadio * bmpH) / (float) bmpW;
			left = -mRadio;
			right = mRadio;
			top = h / 2;
			bottom = -h / 2;
		} else {
			float w = (2 * bmpW) / (float) bmpH;
			top = 1f;
			bottom = -1f;
			left = -w / 2;
			right = w / 2;
		}
		
		if (mbMargin){
			top *= mHeightFactor;
			bottom *= mHeightFactor;
			left *= mWidthFactor;
			right *= mWidthFactor;
		}

		positon[0] = top;
		positon[1] = bottom;
		positon[2] = left;
		positon[3] = right;

	}
	
	private void getConvertMatrix(GL11 gl){
		gl.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, mModelMatrix, 0);
		gl.glGetIntegerv(GL11.GL_VIEWPORT, mViewport, 0);
		gl.glGetFloatv(GL11.GL_PROJECTION_MATRIX, mProjMatrix, 0);
	}
	
	private void render(GL11 gl) {
		gl.glLoadIdentity();
		
		/**draw background*/
		if (mIsBgDraw){
			gl.glPushMatrix();
			gl.glMatrixMode(GL11.GL_MODELVIEW);
			gl.glLoadIdentity();
			ownGLULookAt(0, 0, CAMERA_INIT_DIST+1,
					0, 0, 0f, 0f, 1.0f, 0.0f);
			drawBg(gl);
			gl.glPopMatrix();
		}

		/**draw fore picture*/
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();
		ownGLULookAt(mCameraPos[0], mCameraPos[1], mCameraPos[2],
				mCameraPos[0], mCameraPos[1], 0f, 0f, 1.0f, 0.0f);
		
		if (mIsZooming) {
			convertScreenToOpengl(gl, mInitPos, mNewPos);
			float py1 = (float) ((mCameraPos[2]-1) * Math.tan(mFovy * Math.PI
					/ 360.0)) * 2;
			float px1 = py1 * mRadio;
			float sy = (mInitPos[1] - mCameraPos[1]) / py1;
			float sx = (mInitPos[0] - mCameraPos[0]) / px1;

			float py = py1/mCurrentScale;//(float) ((1 - zDistance) * Math.tan(mFovy * PI / 360.0)) * 2;
			float px = py * mRadio;
			float dy = mInitPos[1] - sy * py;
			float dx = mInitPos[0] - sx * px;
			
			boolean isChange = true;
			float zDistance = (float) (py / (Math.tan(mFovy * Math.PI / 360.0) * 2) + 1);//zDistance;
			if (zDistance > CAMERA_INIT_DIST+1) {
				zDistance = CAMERA_INIT_DIST+1;
				isChange = false;
			}

			if (zDistance < 2) {
				zDistance = 2.0f;
				isChange = false;
			}

			if (isChange){
				mCameraPos[0] = dx;
				mCameraPos[1] = dy;
				mCameraPos[2] = zDistance;
			}else{
				mCameraPos[2] = zDistance;
			}
			
			calcDeltaDistance();
		}


		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();
		ownGLULookAt(mCameraPos[0], mCameraPos[1], mCameraPos[2],
				mCameraPos[0], mCameraPos[1], 0f, 0f, 1.0f, 0.0f);

		drawRect(gl);
	}
	
	private void drawRect(GL11 gl) {
		int x = (int) (mWidth * ((1-mWidthFactor)/2));
		int y = (int) (mHeight * ((1-mHeightFactor)/2));
		gl.glPushMatrix();
		gl.glEnable(GL11.GL_SCISSOR_TEST);
		gl.glScissor(x, y, (int)(mWidth*mWidthFactor), (int)(mHeight*mHeightFactor));
		gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glBindTexture(GL11.GL_TEXTURE_2D, mForeTexList.get(mDisplayTexIndex).getTexId());
		float[] texCoord = new float[] { 0, mForeTexList.get(mDisplayTexIndex).mNormalizedHeight,
				mForeTexList.get(mDisplayTexIndex).mNormalizedWidth, mForeTexList.get(mDisplayTexIndex).mNormalizedHeight, 0,
				0, mForeTexList.get(mDisplayTexIndex).mNormalizedWidth, 0, };
		FloatBuffer texCoordsBuf = floatToBuffer(texCoord);
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texCoordsBuf);

		gl.glColor4f(0, 0, 1.0f, 0.5f);
		FloatBuffer qVertexBuf = floatToBuffer(mForeVertexs);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, qVertexBuf);
		gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		
		drawOwnObject(gl);

		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glDisable(GL11.GL_SCISSOR_TEST);
		gl.glPopMatrix();
	}
	
	protected void drawOwnObject(GL11 gl){
	}
	
	private void drawBg(GL11 gl){
		gl.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glBindTexture(GL11.GL_TEXTURE_2D, mBgTex.getTexId());
		float[] texCoord = new float[] { 0, mBgTex.mNormalizedHeight,
				mBgTex.mNormalizedWidth, mBgTex.mNormalizedHeight, 0,
				0, mBgTex.mNormalizedWidth, 0, };
		FloatBuffer texCoordsBuf = floatToBuffer(texCoord);
		gl.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texCoordsBuf);

		FloatBuffer qVertexBuf = floatToBuffer(mBgVertexs);
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, qVertexBuf);
		gl.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);

		gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL11.GL_VERTEX_ARRAY);
	}
	
	private void startTransAnimation(float dx, float dy){
		// ��ȡ��ǰͶӰ��ʾƽ��Ĵ�С
		float py = (float) ((1 - mCameraPos[2]) * Math.tan(mFovy * Math.PI
				/ 360.0)) * 2;
		float px = py * mRadio;

		// ����Cameraʵ���ƶ��ľ���
		float cx = dx / mWidth * px;
		float cy = -dy / mHeight * py;

		mCameraPos[0] += cx;
		mCameraPos[1] += cy;
		
		calcDeltaDistance();
		
		requestRender();
	}
	
	private void calcDeltaDistance(){
		float py = (float) ((mCameraPos[2]-1) * Math.tan(mFovy * Math.PI
				/ 360.0)) * 2;
		float px = py * mRadio;
		
		if (mbMargin){
			px *= mWidthFactor;
			py *= mHeightFactor;
		}

		float left = mCameraPos[0] - px / 2;
		float right = mCameraPos[0] + px / 2;
		float top = mCameraPos[1] + py / 2;
		float bottom = mCameraPos[1] - py / 2;
		
		if (right > mForeVertexs[3] && left > mForeVertexs[0]){
			mDeltaX = mForeVertexs[3] - right;
		}
		
		if (right < mForeVertexs[3] && left < mForeVertexs[0]){
			mDeltaX = mForeVertexs[0] - left;
		}
		
		if (top > mForeVertexs[7] && bottom > mForeVertexs[1]){
			mDeltaY = mForeVertexs[7] - top;
		}
		
		if (top < mForeVertexs[7] && bottom < mForeVertexs[1]){
			mDeltaY = mForeVertexs[1] - bottom;
		}
		
		if (px > mForeVertexs[3] - mForeVertexs[0]){
			mDeltaX = -mCameraPos[0];
		}
		
		if (py > mForeVertexs[7] - mForeVertexs[1]){
			mDeltaY = -mCameraPos[1];
		}
		
		if (Math.abs(mDeltaX) > 0){
			mS = Math.abs(mDeltaY / mDeltaX);
		}else{
			mS = 1;
		}
	}
	
	private void startAnimation(){
		while (!compareTo(Math.abs(mDeltaX),0) || !compareTo(Math.abs(mDeltaY),0)){
			if (mDeltaX > 0){
				mCameraPos[0] += DELTA;
				mDeltaX -= DELTA;
				if (mDeltaX < 0)
					mDeltaX = 0;
			}
			
			if (mDeltaX < 0){
				mCameraPos[0] -= DELTA;
				mDeltaX += DELTA; 
				if (mDeltaX > 0){
					mDeltaX = 0;
				}
			}
			
			if (mDeltaY > 0){
				mCameraPos[1] += DELTA*mS;
				mDeltaY -= DELTA * mS;
				if (mDeltaY < 0){
					mDeltaY = 0;
				}
			}
			
			if (mDeltaY < 0){
				mCameraPos[1] -= DELTA*mS;
				mDeltaY += DELTA * Math.abs(mS);
				if (mDeltaY > 0){
					mDeltaY = 0;
				}
			}
			
			requestRender();
		}
		
		setCurrentMsg(ANIMATION_FINISH);
		//requestRender();
	}

	public void convertOpenglToScreen(float[] dest, float[] origin){
//		float[] in = new float[4];
		float[] out = new float[4];
//		in[0] = origin[0];
//		in[1] = origin[1];
//		in[2] = origin[2];
//		in[3] = 1.0f;
		
//		transform_point(out, mModelMatrix, in);
//		transform_point(in, mProjMatrix, out);
//		
//		if (in[3] == 0){
//			return ;
//		}
//		
//		in[0] = in[0]/in[3];
//		in[1] = in[1]/in[3];
//		in[2] = in[2]/in[3];
//		in[3] = 1.0f;
//		
//		dest[0] = mViewport[0] + (1 + in[0]) * mViewport[2] / 2;
//		dest[1] = mViewport[1] + (1 + in[1]) * mViewport[3] / 2;
//		float d = (1 + in[2])/2;
//		dest[0] /= d;
//		dest[1] /= d;
		
//		mModelMatrix[12] = - mModelMatrix[12];
		
//		float[] modelMatrix = new float[16];
//		for (int i=0; i<16; ++i){
//			if (i == 12){
//				modelMatrix[i] = -mModelMatrix[i];
//			}
//			else{
//				modelMatrix[i] = mModelMatrix[i];
//			}
//		}
		GLU.gluProject(origin[0], origin[1], origin[2], mModelMatrix, 0, mProjMatrix, 0, 
				mViewport, 0, out, 0);
//		dest[0] = mViewport[2] - out[0];
		dest[0] = out[0];
 		dest[1] = mViewport[3] - out[1];
	}
	
	public void convertScreenToOpengl(float[] point3d,
			float[] screenPoint) {
		float p0[] = new float[4];
		float p1[] = new float[4];
		float p[] = new float[3];
		float realy = mViewport[3] - screenPoint[1];
		GLU.gluUnProject(screenPoint[0], realy, 0f, mModelMatrix, 0, mProjMatrix, 0,
				mViewport, 0, p0, 0);
		GLU.gluUnProject(screenPoint[0], realy, 1f, mModelMatrix, 0, mProjMatrix, 0,
				mViewport, 0, p1, 0);
		float origin0[] = new float[3];
		float origin1[] = new float[3];
		origin0[0] = p0[0] / p0[3];
		origin0[1] = p0[1] / p0[3];
		origin0[2] = p0[2] / p0[3];
		origin1[0] = p1[0] / p1[3];
		origin1[1] = p1[1] / p1[3];
		origin1[2] = p1[2] / p1[3];
		calcPoint3d(origin0, origin1, 1, p);
		point3d[0] = p[0];
		point3d[1] = p[1];
		point3d[2] = 1.0f;
	}
	
	protected void calcPoint3d(float[] origin1, float[] origin2, float z, float[] dest) {
		float dz = origin2[2] - origin1[2];
		if (0 == dz){
			dest[0] = 0;
			dest[1] = 0;
		}else{
			dest[0] = ((origin2[0]-origin1[0]) * z - origin2[0]*origin1[2] + origin1[0]*origin2[2]) / dz;
			dest[1] = ((origin2[1]-origin1[1]) * z - origin2[1]*origin1[2] + origin1[1]*origin2[2]) / dz;
		}
		
		dest[2] = 1;
	}
	
	/**
	 * ת����Ļ��굽opengl���
	 * 
	 * @param point3d
	 * @param screenPoint
	 */
	public void convertScreenToOpengl(GL11 gl, float[] point3d,
			float[] screenPoint) {
		float params[] = new float[16];
		int params2[] = new int[4];
		float params3[] = new float[16];
		gl.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, params, 0);
		gl.glGetIntegerv(GL11.GL_VIEWPORT, params2, 0);
		gl.glGetFloatv(GL11.GL_PROJECTION_MATRIX, params3, 0);

		float p0[] = new float[4];
		float p1[] = new float[4];
		float p[] = new float[3];
		float realy = params2[3] - screenPoint[1] - 1;
		GLU.gluUnProject(screenPoint[0], realy, 0f, params, 0, params3, 0,
				params2, 0, p0, 0);
		GLU.gluUnProject(screenPoint[0], realy, 1f, params, 0, params3, 0,
				params2, 0, p1, 0);
		float origin0[] = new float[3];
		float origin1[] = new float[3];
		origin0[0] = p0[0] / p0[3];
		origin0[1] = p0[1] / p0[3];
		origin0[2] = p0[2] / p0[3];
		origin1[0] = p1[0] / p1[3];
		origin1[1] = p1[1] / p1[3];
		origin1[2] = p1[2] / p1[3];
		calcPoint3d(origin0, origin1, 1, p);
		point3d[0] = p[0];
		point3d[1] = p[1];
		point3d[2] = 1.0f;
	}
	
	protected void setCurrentMsg(int msg){
		synchronized (bytes2){
			mCurrentMsg = msg;
		}
	}
	
	protected int getCurrentMsg(){
		synchronized (bytes){
			return mCurrentMsg;
		}
	}
	
	private static void convertPixels(byte[] array, int w, int h){
		byte tmp;
		
		int line = 0;
		 
		w = w*4;
		int size = h*w;
		
		for (int i=0; i<h/2; ++i){
			   line += w;
			for (int j=0; j<w; ++j){
				tmp = array[line + j];
				array[line + j] = array[size-line + j];
				array[size-line + j] = tmp;
			}
		}
 	}
	
	 
	
	private static void convertPixels(int[] array, int w, int h){
		int tmp;
		long t1 = System.currentTimeMillis();
		int line = 0;
		int size = h*w;
		for (int i=0; i<h/2; ++i){
			line += w;
			for (int j=0; j<w; ++j){
				tmp = array[line + j];
				array[line + j] = convertOrder(array[size-line + j]);
				array[size-line+ j] = convertOrder(tmp);
			}
		}
		
		 
		
	//	Log.v(TAG, "time == " + (System.currentTimeMillis() - t1));
 	}
	
	private static int convertOrder(int value){
		int b3 = (value>>24)& 0xff;	//A
		int b2 = (value>>16)& 0xff;	//R
		int b1 = (value>>8)& 0xff;	//G
		int b0 = (value)& 0xff;		//B
		
		return b2 | (b1 ) << 8 | (b0 ) << 16 | (b3 ) << 24;
	}
	
	
	/*****************************GLU method implements********************************/
	public void CrossProd(float x1, float y1, float z1, float x2, float y2,
			float z2, float res[]) {
		res[0] = y1 * z2 - y2 * z1;
		res[1] = x2 * z1 - x1 * z2;
		res[2] = x1 * y2 - x2 * y1;
	}

	public void PointPord(float x1, float y1, float z1, float x2, float y2,
			float z2, float res[]) {
		res[0] = x1 * x2 + y1 * y2 + z1 * z2;
	}

	public void ownGLULookAt(float ex, float ey, float ez, float lx, float ly,
			float lz, float ux, float uy, float uz) {
		float d[] = new float[3];
		d[0] = lx - ex;
		d[1] = ly - ey;
		d[2] = lz - ez;

		float model1, model2;
		model1 = (float) Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
		model2 = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);

		// normalized d and u.
		if (0 != model1) {
			d[0] = d[0] / model1;
			d[1] = d[1] / model1;
			d[2] = d[2] / model1;
		}

		if (0 != model2) {
			ux = ux / model2;
			uy = uy / model2;
			uz = uz / model2;
		}

		float r[] = new float[3];
		float u[] = new float[3];

		CrossProd(d[0], d[1], d[2], ux, uy, uz, r);
		CrossProd(r[0], r[1], r[2], d[0], d[1], d[2], u);
		float x[] = new float[1];
		float y[] = new float[1];
		float z[] = new float[1];
		PointPord(-ex, -ey, -ez, r[0], r[1], r[2], x);
		PointPord(-ex, -ey, -ez, u[0], u[1], u[2], y);
		PointPord(ex, ey, ez, d[0], d[1], d[2], z);

		float M[] = new float[] { r[0], u[0], -d[0], 0, r[1], u[1], -d[1], 0,
				r[2], u[2], -d[2], 0, x[0], y[0], z[0], 1 };
		FloatBuffer m = floatToBuffer(M);
		mGL11.glMultMatrixf(m);
	}

	public void perspective(float fovy, float aspect, float zNear, float zFar) {
		float xmin, xmax, ymin, ymax;

		ymax = (float) (zNear * Math.tan(fovy * Math.PI / 360.0f));
		ymin = -ymax;
		xmax = ymax * aspect;
		xmin = ymin * aspect;

		mGL11.glFrustumf(xmin, xmax, ymin, ymax, zNear, zFar);
	}

	public static FloatBuffer floatToBuffer(float[] a) {
		FloatBuffer mBuffer;
		// �ȳ�ʼ��buffer������ĳ���*4����Ϊһ��floatռ4���ֽ�
		ByteBuffer mbb = ByteBuffer.allocateDirect(a.length * 4);
		// ����������nativeOrder
		mbb.order(ByteOrder.nativeOrder());
		mBuffer = mbb.asFloatBuffer();
		mBuffer.put(a);
		mBuffer.position(0);
		return mBuffer;
	}
	
	public static boolean compareTo(float x, float y){
		if (x - y < 0.000001f)
			return true;
		else
			return false;
	}
	
	
	public static class Size {
		public int width;
		public int height;

		public Size(int w, int h) {
			width = w;
			height = h;
		}

		public Size() {
			width = 0;
			height = 0;
		}
	}
	
	public interface UpdateTextureCallback{
		public void onFinish(Bitmap bmp);
	}
	
	public interface InitTextureCallback{
		public void onLoad();
	}
	
	public interface CameraChangedListener{
		public void onChange(int type);
	}
	
	private CameraChangedListener mInvalidateListener = null;
	public void setInvalidateListener(CameraChangedListener l){
		mInvalidateListener = l;
	}

}
