#include "EglGlContext.h"
#include "jnilogger.h"

int EglGlContext::getMaxSurfaceSize(int *pWidht ,int *pHeight){
    EGLBoolean returnValue;
    EGLConfig myConfig = {0};

    EGLint context_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
    EGLint configAttribs[] =
    {
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_NONE
    };
    EGLint majorVersion;
    EGLint minorVersion;
    EGLint w, h;

    EGLDisplay dpy;
    EGLint numConfig = 0;

    dpy = eglGetDisplay(EGL_DEFAULT_DISPLAY);

    if (dpy == EGL_NO_DISPLAY)
    {
        LOGE("function:%s,,eglGetDisplay returned EGL_NO_DISPLAY.\n",__FUNCTION__);
        return -1;
    }

    returnValue = eglInitialize(dpy, &majorVersion, &minorVersion);

    if (returnValue != EGL_TRUE)
    {
        LOGE("function:%s,,eglInitialize failed\n",__FUNCTION__);
        return -1;
    }

    eglChooseConfig(dpy, configAttribs, &myConfig, 1, &numConfig);

    EGLint maxPbufW = 0, maxPbufH =0;
    eglGetConfigAttrib(dpy, myConfig,EGL_MAX_PBUFFER_WIDTH, &maxPbufW);
    eglGetConfigAttrib(dpy, myConfig,EGL_MAX_PBUFFER_HEIGHT, &maxPbufH);

   *pWidht = maxPbufW;
   *pHeight = maxPbufH;

   eglTerminate(dpy);
   dpy = NULL;
   return 0;
}

EglGlContext::EglGlContext()
:mEglDisplay(EGL_NO_DISPLAY),mEglContext(EGL_NO_CONTEXT),mEglSurface(EGL_NO_SURFACE),
  mSurfaceWidth(0),mSurfaceHeight(0){
}

void EglGlContext::setSurfaceSize(int width, int height){
    mSurfaceWidth = width;
    mSurfaceHeight = height;
}

EglGlContext::~EglGlContext() {
}

int EglGlContext::createEGLPbufferContext(){
    int ret = EGL_TRUE;
    EGLint majorVersion, minorVersion;
    EGLint dspConfigAttribs[]={
            EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_NONE
        };
    EGLConfig myConfig = {0};
    EGLint numConfig = 0;
    mEglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if(EGL_NO_DISPLAY == mEglDisplay){
        LOGE("%s,,EGL Error NO Display",__FUNCTION__);
        return -1;
    }
    ret = eglInitialize(mEglDisplay, &majorVersion, &minorVersion);
    LOGD("%s,,EGL majorVersion : %d,, minorVersion : %d---->",__FUNCTION__, majorVersion, minorVersion);
    if(EGL_TRUE != ret){
        LOGE("%s,,eglInitialize failed ",__FUNCTION__);
        return -1;
    }
    eglChooseConfig(mEglDisplay, dspConfigAttribs, &myConfig, 1, &numConfig);

    LOGD("%s,,EGL PBufferSize : %d x %d ---->",__FUNCTION__, mSurfaceWidth, mSurfaceHeight);

    EGLint pbufferAttribs[]={
            EGL_WIDTH, mSurfaceWidth,
            EGL_HEIGHT, mSurfaceHeight,
            EGL_NONE
    };

    mEglSurface = eglCreatePbufferSurface(mEglDisplay, myConfig, pbufferAttribs);
    if(EGL_NO_SURFACE == mEglSurface){
        LOGE("%s,,EGL Error NO Surface",__FUNCTION__);
        return -1;
    }

    EGLint contextAttribs[] ={EGL_CONTEXT_CLIENT_VERSION,2, EGL_NONE};
    mEglContext = eglCreateContext(mEglDisplay, myConfig, EGL_NO_CONTEXT, contextAttribs);
    if(EGL_NO_CONTEXT == mEglContext){
        LOGE("%s,,EGL Error NO Context",__FUNCTION__);
        return -1;
    }

    ret = eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
    if(EGL_TRUE != ret){
        LOGE("%s,,eglMakeCurrent failed ",__FUNCTION__);
        return -1;
    }

    EGLint PbufW = 0, PbufH = 0;
    eglQuerySurface(mEglDisplay, mEglSurface, EGL_WIDTH, &PbufW);
    eglQuerySurface(mEglDisplay, mEglSurface, EGL_HEIGHT, &PbufH);
    LOGD("after eglQuerySurface w: %d h: %d\n", PbufW, PbufH);
    return ret;
}

int EglGlContext::destroyEGLContext(){
    if(EGL_NO_DISPLAY == mEglDisplay){
        return -1;
    }
    if(EGL_NO_CONTEXT != mEglContext){
        eglDestroyContext(mEglDisplay, mEglContext);
        mEglContext = EGL_NO_CONTEXT;
    }
    if(EGL_NO_SURFACE != mEglSurface){
        eglDestroySurface(mEglDisplay, mEglSurface);
        mEglSurface = EGL_NO_SURFACE;
    }
    if(EGL_NO_DISPLAY != mEglDisplay){
        eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglTerminate(mEglDisplay);
        mEglDisplay = EGL_NO_DISPLAY;
    }
    return 0;
}
