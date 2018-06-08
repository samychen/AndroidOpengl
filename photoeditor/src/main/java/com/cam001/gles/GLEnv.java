package com.cam001.gles;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.graphics.SurfaceTexture;

import com.cam001.util.DebugUtil;

public class GLEnv implements Runnable{
	
	private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
	private static final int EGL_OPENGL_ES2_BIT = 4;
	private EGLContext mEglContext;
	private EGLDisplay mEglDisplay;
	private EGLSurface mEGLSurface;
	private EGL10 mEgl;
	
	private LinkedList<Runnable> mEvents = null;
	private Object mLock = null;
	private boolean mIsRunning = false;
	private Thread mThread = null;
	
	private SurfaceTexture mSurfaceTexture = null;
	
	public GLEnv() {
		mEvents = new LinkedList<Runnable>();
		mLock = new Object();
	}
	
	public GLEnv(SurfaceTexture surfaceTexture) {
		this();
		mSurfaceTexture = surfaceTexture;
	}
	
	public void destroy() {
		if(mIsRunning) {
			synchronized (mLock) {
				mIsRunning = false;
				mLock.notify();
			}
		}
		if(mThread!=null) {
			try {
				mThread.join();
				mThread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void queueEvent(Runnable r) {
		if(mThread==null) {
			mIsRunning = true;
			mThread = new Thread(this);
			mThread.start();
		}
		synchronized (mLock) {
			mEvents.add(r);
			mLock.notify();
		}
	}
	
	@Override
	public void run() {
		initOpenGL();
		while(mIsRunning || !mEvents.isEmpty()) {
			while(mEvents.isEmpty()) {
				try {
					synchronized (mLock) {
						mLock.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Runnable r = null;
			synchronized (mLock) {
				r = mEvents.poll();
			}
			if(r==null) continue;
	    	mEgl.eglMakeCurrent(mEglDisplay, mEGLSurface, mEGLSurface,
					mEglContext);
	    	checkEglError("After eglMakeCurrent", mEgl);
	    	r.run();
	    	mEgl.eglSwapBuffers(mEglDisplay, mEGLSurface);
		}
		uninitOpenGL();
	}

	private void initOpenGL() {
		int []version = new int[2];
		int[] num_config = new int[1];
		EGLConfig[] configs = new EGLConfig[1];
		int[] configSpec = {
		 EGL10.EGL_RED_SIZE,            8,
		 EGL10.EGL_GREEN_SIZE,          8,
		 EGL10.EGL_BLUE_SIZE,           8,
		 EGL10.EGL_ALPHA_SIZE,			8,
		 EGL10.EGL_SURFACE_TYPE,     EGL10.EGL_WINDOW_BIT,
		 EGL10.EGL_RENDERABLE_TYPE,  EGL_OPENGL_ES2_BIT, 
		 EGL10.EGL_NONE
		 };
		mEgl = (EGL10) EGLContext.getEGL();
		
		mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		mEgl.eglInitialize(mEglDisplay, version);
		checkEglError("After eglInitialize", mEgl);
		
		mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1, num_config);
		checkEglError("After eglChooseConfig", mEgl);
		
		EGLConfig mEglConfig = configs[0];
		
		int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };        
		mEglContext = mEgl.eglCreateContext( mEglDisplay,
				mEglConfig,
				EGL10.EGL_NO_CONTEXT,
				attrib_list);
		checkEglError("After eglCreateContext", mEgl);
		
		int[] glattribList = new int[] {
				EGL10.EGL_WIDTH, 1,
				EGL10.EGL_HEIGHT, 1,
				EGL10.EGL_NONE
	        };
		if(mSurfaceTexture==null) {
			mEGLSurface = mEgl.eglCreatePbufferSurface(mEglDisplay, mEglConfig,  glattribList);
		} else {
			mEGLSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, mSurfaceTexture, null);
		}
		mEgl.eglMakeCurrent(mEglDisplay, mEGLSurface, mEGLSurface, mEglContext);
		checkEglError("After eglMakeCurrent", mEgl);
	}
	
	private void uninitOpenGL() {
		mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
				EGL10.EGL_NO_CONTEXT);
		checkEglError("After eglMakeCurrent", mEgl);
		mEgl.eglDestroyContext(mEglDisplay, mEglContext);
		mEgl.eglDestroySurface(mEglDisplay, mEGLSurface);
		mEgl.eglTerminate(mEglDisplay);
	}
	
	private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            DebugUtil.logE("checkEglError", "%s: EGL error: 0x%x", prompt, error);
        }
    }
	
}
