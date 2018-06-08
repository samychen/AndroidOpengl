//
// Created by 000 on 2018/6/5.
//

#ifndef ANDROIDOPENGL_FACETUNE_H
#define ANDROIDOPENGL_FACETUNE_H

#include <GLES2/gl2.h>
#include <graphics/Matrix.h>
#include <include/tcomdef.h>
#include <include/beautitune.h>

class facetune {

public:

    facetune();

    ~facetune();

    void create();

    void change(int width, int height);

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
    Matrix *mViewMatrix;
    Matrix *mModelMatrix;
    Matrix *mProjectionMatrix;
    Matrix *mMVPMatrix;

    GLuint mMVPMatrixHandle;
    GLuint mPositionHandle;
    GLuint grayProgram;
    GLuint mTextureLocation;
    GLuint mTextureCoordinateHandle;

    GLuint dstTexure;
    GLuint fFrame ;
    GLuint mPointProgramHandle;

    THandle TuneEngine;
    TByte *ImgBuf;
};


#endif //ANDROIDOPENGL_FACETUNE_H
