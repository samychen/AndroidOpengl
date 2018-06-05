//
// Created by 000 on 2018/6/5.
//

#ifndef ANDROIDOPENGL_FACETUNE_H
#define ANDROIDOPENGL_FACETUNE_H

#include <GLES2/gl2.h>
#include <graphics/Matrix.h>

class facetune {

public:

    facetune();

    ~facetune();

    void create();

    void change(int width, int height);

    void draw();
private:

    GLsizei mWidth;
    GLsizei mHeight;

    Matrix *mViewMatrix;
    Matrix *mModelMatrix;
    Matrix *mProjectionMatrix;
    Matrix *mMVPMatrix;

    GLuint mMVPMatrixHandle;
    GLuint mPositionHandle;
    GLuint mTextureUniformHandle;
    GLuint mTextureCoordinateHandle;
    GLuint mTextureDataHandle;

    GLuint mPointProgramHandle;

};


#endif //ANDROIDOPENGL_FACETUNE_H
