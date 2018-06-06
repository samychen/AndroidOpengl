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
    fFrame = 0;
    TuneEngine = NULL;
    mPositionHandle = 0;
    mPointProgramHandle = 0;
    grayProgram = 0;
    ImgBuf = NULL;
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
        LOGE("Could not create program");
        return;
    }
    const char *vertex2 = GLUtils::openTextFile("vertex/gray_vertex_shader.glsl");
    const char *fragment2 = GLUtils::openTextFile("fragment/gray_frag_shader.glsl");
    grayProgram = GLUtils::createProgram(&vertex2, &fragment2);
    if (!grayProgram) {
        LOGE("Could not create gray program");
        return;
    }
    mModelMatrix = new Matrix();
    mMVPMatrix = new Matrix();
    srcTexure = GLUtils::loadTexture("texture/bumpy_bricks_public_domain.jpg");
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
    glViewport(0, 0, mWidth, mHeight);
    glGenTextures(1, &dstTexure);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, mWidth, mHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
}
int suggestTexSize(int size)
{
    int texSize = 1;
    while(true)
    {
        texSize <<= 1;
        if(texSize >= size)
            break ;
    }
    return texSize;
}
void facetune::change(int width, int height) {
    mWidth = width;//suggestTexSize(width);
    mHeight = height;//suggestTexSize(height);
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
    BTType ProType = Smooth;
    if (!TuneEngine)
    {
        LOGE("init width=%d,height=%d",mWidth,mHeight);
        nRes = BeautiTune_Init(&TuneEngine,mWidth,mHeight, ProType);
        if (nRes)
        {
            return -2 ;
        }
        LOGE("init success");
        nRes = BeautiTune_PreProcess(TuneEngine, srcTexure);
        if (nRes)
        {
            return -3;
        }
        LOGE("prepross success");
        //3.响应每次涂抹动作，单次涂抹产生的参数有（center，radius），擦除动作也会产生同样的参数
        TypePara Para = { 0 };
        if (ProType == TeethWhite)
        {
            LOGE("TeethWhite srcTexure=%d,dstTexure=%d,center.x=%lf,center.y=%lf",srcTexure,dstTexure,center.x,center.y);
            Para.BsWork = Paint;//涂抹还是擦除
            nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, 15.0f,&Para, ImgBuf);
            LOGE("process success");
        }
        else if (ProType == Smooth)
        {
            LOGE("Smooth");
            Para.IsMoreSmooth = 1;//是否需要更强的模糊
            Para.BsWork = Paint;
            nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, 15.0f,&Para, ImgBuf);
        }
        else if (ProType == Detail)
        {
            LOGE("Detail");
            Para.BsWork = Paint;
            nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, 15.0f, &Para, ImgBuf);
        }
        draw();
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
    //render To Texure
    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(mPointProgramHandle);
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
    glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GL_FLOAT, GL_FALSE, 0, POSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEXURE_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATE);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    mModelMatrix->identity();
    mMVPMatrix->multiply(*mViewMatrix, *mModelMatrix);
    mMVPMatrix->multiply(*mProjectionMatrix, *mMVPMatrix);
    glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, mMVPMatrix->mData);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
//    glDisableVertexAttribArray(mPositionHandle);
//    glDisableVertexAttribArray(mTextureCoordinateHandle);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);//绑定到默认纹理，渲染最后的纹理
    glBindTexture(GL_TEXTURE_2D, 0);
    //render TO window
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//    glUseProgram(grayProgram);
//    GLint mGrayVertexLocation = glGetAttribLocation(grayProgram, "aPosition");
//    GLint mGrayCoordLocation = glGetAttribLocation(grayProgram, "aCoord");
//    GLint mGrayMatrixLocation = glGetUniformLocation(grayProgram, "uMatrix");
//    GLint mGrayTexureLocation = glGetUniformLocation(grayProgram, "uTexture");
//    glVertexAttribPointer(mGrayVertexLocation, POSITION_DATA_SIZE, GL_FLOAT, GL_FALSE, 0, POSITION);
//    glEnableVertexAttribArray(mGrayVertexLocation);
//    glVertexAttribPointer(mGrayCoordLocation, TEXURE_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATE);
//    glEnableVertexAttribArray(mGrayCoordLocation);
//    glUniformMatrix4fv(mGrayMatrixLocation, 1, GL_FALSE,mMVPMatrix->mData);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
//    glUniform1i(mGrayTexureLocation, 0);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
//    glDisableVertexAttribArray(mGrayVertexLocation);
//    glDisableVertexAttribArray(mGrayCoordLocation);
    glDisableVertexAttribArray(mPositionHandle);
    glDisableVertexAttribArray(mTextureCoordinateHandle);
    glDeleteTextures(1, &dstTexure);
    glDeleteFramebuffers(1, &fFrame);
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
        int ret=facetuneobj->renderCenter({x,y},15.0f);
        LOGE("ret=%d",ret);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeDrawFrame(JNIEnv *env,
                                                                                jclass type) {

    if (facetuneobj) {
        facetuneobj->createFrameBuffer();
        facetuneobj->draw();
    }
}