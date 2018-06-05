//
// Created by 000 on 2018/6/5.
//

#include "facetune.h"
#include <graphics/GLUtils.h>
#include <android/log.h>
#include <jni.h>
#include <graphics/Matrix.h>

#define LOG_TAG "facetune"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

static GLint POSITION_DATA_SIZE = 2;
static GLint TEXURE_COORD_SIZE = 2;
static const GLfloat POSITION[] =
        {
                0.0f, 0.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f,
                1.0f, 1.0f,
                -1.0f,1.0f,
                -1.0f,-1.0f
        };
static const GLfloat COORDINATE[] =
        {
                0.5f, 0.5f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f,0.0f,
                0.0f,1.0f
        };

facetune::facetune() {
    mWidth = 0;
    mHeight = 0;
    mViewMatrix = NULL;
    mModelMatrix = NULL;
    mProjectionMatrix = NULL;
    mMVPMatrix = NULL;

    mPositionHandle = 0;
    mPointProgramHandle = 0;
}
facetune::~facetune() {
    delete mModelMatrix;
    mModelMatrix = NULL;
    delete mViewMatrix;
    mViewMatrix = NULL;
    delete mProjectionMatrix;
    mProjectionMatrix = NULL;
}
void facetune::create() {
    const char *vertex = GLUtils::openTextFile("vertex/facetune_vertex_shader.glsl");
    const char *fragment = GLUtils::openTextFile("fragment/facetune_frag_shader.glsl");
    mPointProgramHandle = GLUtils::createProgram(&vertex, &fragment);
    if (!mPointProgramHandle) {
        LOGD("Could not create program");
        return;
    }
    mModelMatrix = new Matrix();
    mMVPMatrix = new Matrix();
    mTextureDataHandle = GLUtils::loadTexture("texture/bumpy_bricks_public_domain.jpg");
    float eyeX = 0.0f;
    float eyeY = 0.0f;
    float eyeZ = 1.5f;
    float centerX = 0.0f;
    float centerY = 0.0f;
    float centerZ = -5.0f;
    float upX = 0.0f;
    float upY = 1.0f;
    float upZ = 0.0f;
    mViewMatrix = Matrix::newLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
}
void facetune::change(int width, int height) {
    mWidth = width;
    mHeight = height;
    glViewport(0, 0, mWidth, mHeight);
    float ratio = (float) width / height;
    float left = -ratio;
    float right = ratio;
    float bottom = -1.0f;
    float top = 1.0f;
    float near = 1.0f;
    float far = 10.0f;
    mProjectionMatrix = Matrix::newFrustum(left, right, bottom, top, near, far);
}
void facetune::draw() {
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(mPointProgramHandle);
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureUniformHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureDataHandle);
    glUniform1i(mTextureUniformHandle, 0);
    glVertexAttribPointer(
            mPositionHandle,
            POSITION_DATA_SIZE,
            GL_FLOAT,
            GL_FALSE,
            0,
            POSITION
    );
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(
            mTextureCoordinateHandle,
            TEXURE_COORD_SIZE,
            GL_FLOAT,
            GL_FALSE,
            0,
            COORDINATE
    );
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    mModelMatrix->identity();
    mMVPMatrix->multiply(*mViewMatrix, *mModelMatrix);
    mMVPMatrix->multiply(*mProjectionMatrix, *mMVPMatrix);
    glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, mMVPMatrix->mData);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
}
facetune *facetuneobj;

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeSurfaceCreate(JNIEnv *env,
                                                                                    jclass type,
                                                                                    jobject assetManager) {
    GLUtils::setEnvAndAssetManager(env, assetManager);
    if (facetuneobj) {
        delete facetuneobj;
        facetuneobj = NULL;
    }
    facetuneobj = new facetune();
    facetuneobj->create();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeSurfaceChange(JNIEnv *env,
                                                                                    jclass type,
                                                                                    jint width,
                                                                                    jint height) {
    if (facetuneobj) {
        facetuneobj->change(width, height);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeDrawFrame(JNIEnv *env,
                                                                                jclass type) {

    if (facetuneobj) {
        facetuneobj->draw();
    }
}