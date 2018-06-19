//
// Created by xingkong on 2017/8/14.
//

#ifndef NATIVEGL_DEMO2_GLRENDERS_H
#define NATIVEGL_DEMO2_GLRENDERS_H

#include "GLRenderer.h"
#include "gl_params.h"
#include <include/tcomdef.h>
#include <include/beautitune.h>
#include <GLES2/gl2.h>
#include <graphics/Matrix.h>
class GLrenderS : public GLRenderer{
    GLint POSITION_SIZE = 2;
    GLint TEX_COORD_SIZE = 2;
    GLfloat VERTEXPOSITION[] =
            {
                    0.0f,  0.0f,
                    1.0f,  -1.0f,
                    -1.0f, -1.0f,
                    -1.0f, 1.0f,
                    1.0f,  1.0f,
                    1.0f,  -1.0f
            };
    GLfloat COORDINATEPOSITION[] =
            {
                    0.5f, 0.5f,
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f
            };
    GLfloat mOriMartrix[] =
            {
                    1.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 1.0f,
            };
    GLfloat flipYMartrix[] =
            {
                    1.0f, 0.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, 0.0f, 1.0f,
            };
private:
    GLsizei mWidth;
    GLsizei mHeight;
    GLsizei left;
    GLsizei top;
    GLsizei right;
    GLsizei bottom;

    GLuint mMVPMatrixHandle;
    GLuint mPositionHandle;
    GLuint mTextureLocation;
    GLuint mTextureCoordinateHandle;

    GLuint dstTexure;
    GLuint fFrame ;
    GLuint mPointProgramHandle;

    THandle TuneEngine;
    TByte *ImgBuf;
public:
    GLrenderS();
    ~GLrenderS();
    void loadTexture();
    void cal_pixel();
    void createFrameBuffer();
    BTType ProType;
    int initEffect;
    BSWork bsWork;
    int hasEffect;
    int isMoreSmooth;
    int picwidth;
    int picheight;
    int radius;
    char *picpath;
    GLuint srcTexure;
    int mCompareFlag;
    //实现OpenGL渲染上下文的回调函数
    virtual void SurfaceCreate();
    virtual void SurfaceChange(int width, int height);
    virtual void change(int left, int top, int right, int bottom,int width, int height);
    virtual void DrawFrame();
};


#endif //PHASEDARRAY2_0_GLRENDERS_H
