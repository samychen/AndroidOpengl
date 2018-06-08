#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <stdio.h>
#include <stdlib.h>
#include "tcomdef.h"

#ifndef _Included_texture_2d
#define _Included_texture_2d


class texture_2d {

public:

    texture_2d(GLvoid* pImg, TInt32 w, TInt32 h, GLint format, TInt32 texturename,GLenum texturetype = GL_UNSIGNED_BYTE);
    texture_2d();

    ~texture_2d();

    void genTexture(TInt32 w, TInt32 h, GLint format, TInt32 texturename,GLenum texturetype = GL_UNSIGNED_BYTE);
    void subImage(GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLenum type, const GLvoid* pixels);
    TInt32 getWidth();
    TInt32 getHeight();
    TInt32 getColorFormart();

    TInt32 getTexturename();
    GLuint getTextureId();


private:
    GLvoid* mpPixel;
    GLuint mTextureId;
    TInt32 mTexturename;
    TInt32 mWidth;
    TInt32 mHeight;
    TInt32 mColorFormat; // only support RGBA now

};

#endif
