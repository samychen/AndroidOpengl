//
// Created by 000 on 2018/6/5.
//

#include "facetune.h"
#include <graphics/GLUtils.h>
#include <android/log.h>
#include <jni.h>
#include <graphics/Matrix.h>
#include "include/beautitune.h"

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
    fFrame = NULL;
    fRender = NULL;
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
    srcTexure = GLUtils::loadTexture("texture/bumpy_bricks_public_domain.jpg");
    glGenTextures(1, &dstTexure);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, mWidth, mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    createFrameBuffer();
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
void facetune::createFrameBuffer(){
    glGenFramebuffers(1, &fFrame);
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);

    glGenRenderbuffers(1, &fRender);
    glBindRenderbuffer(GL_RENDERBUFFER, fRender);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, mWidth, mHeight);
    glBindRenderbuffer(GL_RENDERBUFFER, 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTexure, 0);
    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                              GL_RENDERBUFFER, fRender);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
int facetune::renderCenter(FPOINT center, TFloat radius) {
    int32_t nRes;
    BTType ProType;
    if (!TuneEngine)
    {
        ProType = Smooth;// Smooth; TeethWhite;
        nRes = BeautiTune_Init(&TuneEngine,mWidth,mHeight, ProType);
        if (nRes)
        {
            return -2 ;
        }
        nRes = BeautiTune_PreProcess(TuneEngine, mTextureUniformHandle);
        if (nRes)
        {
            return -3;
        }
    }
    //3.响应每次涂抹动作，单次涂抹产生的参数有（center，radius），擦除动作也会产生同样的参数
    TypePara Para = { 0,Paint };
    if (ProType == TeethWhite)
    {
        Para.BsWork = Paint;//涂抹还是擦除
        nRes = BeautiTune_Process(TuneEngine, mTextureUniformHandle, dstTexure, &center, 15.0f,&Para, ImgBuf);
    }
    else if (ProType == Smooth)
    {
        Para.IsMoreSmooth = 0;//是否需要更强的模糊
        Para.BsWork = Paint;
        nRes = BeautiTune_Process(TuneEngine, mTextureUniformHandle, dstTexure, &center, 15.0f,&Para, ImgBuf);
    }
    else if (ProType == Detail)
    {
        Para.BsWork = Paint;
        nRes = BeautiTune_Process(TuneEngine, mTextureUniformHandle, dstTexure, &center, 15.0f, &Para, ImgBuf);
    }
    return 0;
}
int facetune::releaseEffect() {
    if (TuneEngine)
    {
        BeautiTune_UnInit(TuneEngine);
        dstTexure = srcTexure = NULL;
//        if (dstTexure) delete dstTexure;
//        if (srcTexure) delete srcTexure;
        TuneEngine = NULL;
    }
}
void facetune::draw() {
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(mPointProgramHandle);
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureUniformHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");

    glBindTexture(GL_TEXTURE_2D, srcTexure);

    glActiveTexture(GL_TEXTURE0);
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
    glBindTexture(GL_TEXTURE_2D, 0);
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
Java_com_learnopengles_android_render_ImageRender_nativeRender(JNIEnv *env,
                                                                      jclass type,
                                                                      jfloat x, jfloat y) {
    if (facetuneobj) {
        facetuneobj->renderCenter({x,y},15.0f);
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