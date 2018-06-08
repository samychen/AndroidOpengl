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

int checkGlError(const char* op);
static GLint POSITION_DATA_SIZE = 2;
static GLint TEXURE_COORD_SIZE = 2;
static const GLfloat POSITION[] =
        {
                0.0f,  0.0f,
                1.0f,  -1.0f,
                -1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f,  1.0f,
                1.0f,  -1.0f
        };
static const GLfloat COORDINATE[] =
        {
                0.5f, 0.5f,
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };
static const GLfloat mGrayMatrix[] =
        {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        };
static const GLfloat uMatrix[] =
        {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
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
    initEffect = 0;
    hasEffect = 0;
    isMoreSmooth = 0;
    mCompareFlag = 0;
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
    srcTexure = GLUtils::loadTexture(picpath);
}
void facetune::createFrameBuffer(){
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
    glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GL_FLOAT, GL_FALSE, 0, POSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEXURE_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATE);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, uMatrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    checkGlError("createFrameBuffer");
}
int facetune::copyBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, picwidth, picheight);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, srcTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
    glUseProgram(mPointProgramHandle);
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
    glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GL_FLOAT, GL_FALSE, 0, POSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEXURE_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATE);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, uMatrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    checkGlError("createFrameBuffer");
    return 0;
}
int facetune::copySrcBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, picwidth, picheight);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
    glUseProgram(mPointProgramHandle);
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
    glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GL_FLOAT, GL_FALSE, 0, POSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEXURE_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATE);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, uMatrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    checkGlError("createFrameBuffer");
    return 0;
}
void facetune::change(int width, int height) {
    mWidth = width;
    mHeight = height;
    glViewport(0, 0, mWidth, mHeight);

}
int facetune::renderCenter(FPOINT center, TFloat radius) {
    int32_t nRes;
    if (!initEffect){
        return -1;
    }
    if (!TuneEngine)
    {
        LOGE("init width=%d,height=%d",mWidth,mHeight);
        nRes = BeautiTune_Init(&TuneEngine,picwidth,picheight, ProType);
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
    }
    //3.响应每次涂抹动作，单次涂抹产生的参数有（center，radius），擦除动作也会产生同样的参数
    TypePara Para = { 0 };
    if (ProType == TeethWhite)
    {
        LOGE("TeethWhite srcTexure=%d,dstTexure=%d,center.x=%lf,center.y=%lf",srcTexure,dstTexure,center.x,center.y);
        Para.BsWork = bsWork;//涂抹还是擦除
        nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, 10.0f,&Para, ImgBuf);
        LOGE("process success");
    }
    else if (ProType == Smooth)
    {
        LOGE("Smooth srcTexure=%d,dstTexure=%d,center.x=%lf,center.y=%lf",srcTexure,dstTexure,center.x,center.y);
        Para.IsMoreSmooth = isMoreSmooth;//是否需要更强的模糊
        Para.BsWork = bsWork;
        nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, 10.0f,&Para, ImgBuf);
    }
    else if (ProType == Detail)
    {
        LOGE("Detail srcTexure=%d,dstTexure=%d,center.x=%lf,center.y=%lf",srcTexure,dstTexure,center.x,center.y);
        Para.BsWork = bsWork;
        nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, 10.0f, &Para, ImgBuf);
    }
    draw();
    return 0;
}
int facetune::releaseEffect() {
    if (TuneEngine)
    {
        LOGE("releaseeffect");
        BeautiTune_UnInit(TuneEngine);
        TuneEngine = NULL;
    }
    return 0;
}
int checkGlError(const char* op) {
    int res = 0;
    for (GLint error = glGetError(); error; error
                                                    = glGetError()) {
        LOGE("after %s() glError (0x%x)\n", op, error);
        res = 1;
    }
    return res;
}
void facetune::draw() {
    glViewport(0,0,mWidth,mHeight);
    if (mCompareFlag==0){
        //render To Texure
        glBindFramebuffer(GL_FRAMEBUFFER, 0);//绑定到默认纹理，渲染最后的纹理
        glBindTexture(GL_TEXTURE_2D, 0);
        glClearColor(0, 0, 0, 1);
        //render TO window
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        checkGlError("draw");
        glUseProgram(mPointProgramHandle);
        mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
        mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
        mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
        glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GL_FLOAT, GL_FALSE, 0, POSITION);
        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(mTextureCoordinateHandle, TEXURE_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATE);
        glEnableVertexAttribArray(mTextureCoordinateHandle);
        glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, mGrayMatrix);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, dstTexure);
        glUniform1i(mTextureLocation, 0);
    } else {
        glBindTexture(GL_TEXTURE_2D, 0);
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
        glUniformMatrix4fv(mMVPMatrixHandle, 1, GL_FALSE, mGrayMatrix);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, srcTexure);
        glUniform1i(mTextureLocation, 0);
    }
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    glDeleteFramebuffers(1, &fFrame);
}
facetune *facetuneobj;

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeSurfaceCreate(JNIEnv *env, jclass type,
                                                                      jobject assetManager,jint picwidth_,jint picheight_, jstring picpath_) {
    const char *picpath = env->GetStringUTFChars(picpath_, 0);
    GLUtils::setEnvAndAssetManager(env, assetManager);
    if (facetuneobj) {
        delete facetuneobj;
        facetuneobj = NULL;
    }
    facetuneobj = new facetune();
    facetuneobj->picwidth = picwidth_;
    facetuneobj->picheight = picheight_;
    facetuneobj->picpath = (char *) picpath;
    facetuneobj->create();
    env->ReleaseStringUTFChars(picpath_, picpath);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeSurfaceChange(JNIEnv *env,
                                                                                    jclass type,
                                                                                    jint width,
                                                                                    jint height) {
    if (facetuneobj) {
        facetuneobj->change(width, height);
        facetuneobj->createFrameBuffer();
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeRender(JNIEnv *env,
                                                                      jclass type,
                                                                      jfloat x, jfloat y) {
    if (facetuneobj) {
        int ret=facetuneobj->renderCenter({x,y},15.0f);
        facetuneobj->mCompareFlag = 0;
        LOGE("ret=%d",ret);
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
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativereleaseEffect(JNIEnv *env,
                                                                  jclass type,jint effecttype_) {
    if (facetuneobj) {
        LOGE("release");
        facetuneobj->initEffect = 1;
        if (effecttype_==1){
            facetuneobj->releaseEffect();
            facetuneobj->ProType = TeethWhite;
            facetuneobj->bsWork = Paint;
            facetuneobj->hasEffect = 1;
            facetuneobj->copyBuffer();
        } else if (effecttype_==2){
            facetuneobj->releaseEffect();
            facetuneobj->ProType = Smooth;
            facetuneobj->bsWork = Paint;
            facetuneobj->hasEffect = 1;
            facetuneobj->copyBuffer();
        } else if (effecttype_==3){
            facetuneobj->releaseEffect();
            facetuneobj->ProType = Smooth;
            facetuneobj->bsWork = Paint;
            facetuneobj->hasEffect = 1;
            facetuneobj->isMoreSmooth = 1;
            facetuneobj->copyBuffer();
        } else if (effecttype_==4){
            facetuneobj->releaseEffect();
            facetuneobj->ProType = Detail;
            facetuneobj->bsWork = Paint;
            facetuneobj->hasEffect = 1;
            facetuneobj->copyBuffer();
        } else if (effecttype_==5){
            //之前是否选择其他按钮
            if ( facetuneobj->hasEffect == 0){
                facetuneobj->initEffect = 0;
            } else {
                facetuneobj->bsWork = Erase;
            }
        } else if (effecttype_==6){
            facetuneobj->releaseEffect();
            facetuneobj->copySrcBuffer();
        } else if (effecttype_==7){
            facetuneobj->copyBuffer();
        }
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_render_ImageRender_nativeCompare(JNIEnv *env, jclass type,jint actionup_) {
    if (facetuneobj) {
        facetuneobj->mCompareFlag = actionup_;
    }
}