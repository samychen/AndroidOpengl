//
// Created by 000 on 2018/6/11.
//

#ifndef ANDROIDOPENGL_TEXTUREEFFECT_H
#define ANDROIDOPENGL_TEXTUREEFFECT_H

#include <GLES2/gl2.h>
#include <graphics/Matrix.h>
#include <include/tcomdef.h>
#include <include/beautitune.h>
#include <jni.h>

class textureeffect {
public:

    textureeffect();

    ~textureeffect();

    void create();

    void change(int left, int top, int right, int bottom,int width, int height);

    void draw();

    void createFrameBuffer();

    int renderCenter( FPOINT center,TFloat radius);

    int releaseEffect();

    int copyBuffer();

    int copySrcBuffer();

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

    void destroyTexture() const;
};


#endif //ANDROIDOPENGL_TEXTUREEFFECT_H
