//
// Created by 000 on 2018/6/11.
//

#ifndef ANDROIDOPENGL_TEXTUREEFFECT_H
#define ANDROIDOPENGL_TEXTUREEFFECT_H

#include <GLES2/gl2.h>
#include <include/tcomdef.h>
#include <include/beautitune.h>
#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#define LOG_TAG "textureeffect"
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

class textureeffect {
public:

    textureeffect();

    ~textureeffect();

    void change(int left, int top, int right, int bottom,int width, int height);

    void draw();

    void createFrameBuffer();

    int initGLEffect();

    int renderCenter( FPOINT center,TFloat radius);

    int releaseEffect();

    int copyBuffer();

    int copySrcBuffer();

    void destroyTexture();

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
    GLuint mPointProgramHandle;
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
    THandle TuneEngine;
    TByte *ImgBuf;

};


#endif //ANDROIDOPENGL_TEXTUREEFFECT_H
