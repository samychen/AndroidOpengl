//
// Created by xingkong on 2017/8/14.
//

#include "GLrenderS.h"
#include <graphics/GLUtils.h>
#include <android/log.h>
#define LOG_TAG "textureeffect"
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

GLrenderS::GLrenderS(){
    fFrame = 0;
    TuneEngine = NULL;
    mPositionHandle = 0;
    mPointProgramHandle = 0;
    ImgBuf = NULL;
    initEffect = 0;
    hasEffect = 0;
    isMoreSmooth = 0;
    mCompareFlag = 0;
}
GLrenderS::~GLrenderS(){

}

//读取texture
void GLrenderS::loadTexture(){
    srcTexure = GLUtils::loadTexture(picpath);
}

void GLrenderS::cal_pixel() {

}

void GLrenderS::SurfaceCreate() {
    const char *vertex = GLUtils::openTextFile("vertex/transform_vertex_shader.glsl");
    const char *fragment = GLUtils::openTextFile("fragment/transform_fragment_shader.glsl");
    mPointProgramHandle = GLUtils::createProgram(&vertex, &fragment);
    if (!mPointProgramHandle) {
        LOGE("Could not create program");
        return;
    }
    loadTexture();
}

void GLrenderS::SurfaceChange(int w,int h) {

}
void GLrenderS::change(int l, int t, int r, int b,int w,int h){
    left = l;
    top = t;
    right = r;
    bottom = b;
    mWidth = w;
    mHeight = h;
    createFrameBuffer();
}
void GLrenderS::createFrameBuffer(){
    glGenFramebuffers(1, &fFrame);
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, picwidth, picheight);
    glGenTextures(1, &dstTexure);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, picwidth, picheight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
    glUseProgram(mPointProgramHandle);
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
    glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, flipYMartrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
}
void GLrenderS::DrawFrame(){
    glViewport(left, top, right, bottom);
    if (mCompareFlag==0){
        //render To Texure
        glBindFramebuffer(GL_FRAMEBUFFER, 0);//绑定到默认纹理，渲染最后的纹理
        glBindTexture(GL_TEXTURE_2D, 0);
        glClearColor(1, 1, 1, 1);
        //render TO window
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mPointProgramHandle);
        mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
        mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
        mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
        glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
        glEnableVertexAttribArray(mTextureCoordinateHandle);
        glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, mOriMartrix);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, dstTexure);
        glUniform1i(mTextureLocation, 0);
    } else {// compare时显示原纹理
        glBindTexture(GL_TEXTURE_2D, 0);
        glClearColor(1, 1, 1, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mPointProgramHandle);
        mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
        mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
        mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
        glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
        glEnableVertexAttribArray(mTextureCoordinateHandle);
        glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, mOriMartrix);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, srcTexure);
        glUniform1i(mTextureLocation, 0);
    }
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    glDisableVertexAttribArray(mPositionHandle);
    glDisableVertexAttribArray(mTextureCoordinateHandle);
}

