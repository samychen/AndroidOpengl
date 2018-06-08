#ifndef __EGL_GL_CONTEXT_H__
#define __EGL_GL_CONTEXT_H__
#include <EGL/egl.h>

class EglGlContext{
    public:
        static int getMaxSurfaceSize(int *pWidht ,int *pHeight);

        EglGlContext();
        ~EglGlContext();

        void setSurfaceSize(int width, int height);
        int createEGLPbufferContext();
        int destroyEGLContext();

        EGLContext getEglContext(){
            return mEglContext;
        }

        EGLDisplay getEglDisplay(){
            return mEglDisplay;
        }

    private:

        int mSurfaceWidth;
        int mSurfaceHeight;
        EGLDisplay mEglDisplay;
        EGLContext mEglContext;
        EGLSurface mEglSurface;
};

#endif // end __EGL_GL_CONTEXT_H__
