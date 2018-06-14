//
// Created by 000 on 2018/6/11.
//

#include "textureeffect.h"
#include <graphics/GLUtils.h>
#include <android/log.h>
#include <jni.h>
#include <graphics/Matrix.h>
#include "include/beautitune.h"
#define LOG_TAG "textureeffect"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

int checkError(const char* op);
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
textureeffect::textureeffect() {
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
textureeffect::~textureeffect() {

}
int textureeffect::changeMartrix(float *mat) {
    for (int i = 0; i < sizeof(mOriMartrix)/ sizeof(mOriMartrix[0]); ++i) {
        mOriMartrix[i] = mat[i];
        flipYMartrix[i] = mat[i];
    }
    flipYMartrix[4] = -mOriMartrix[4];
    return 0;
}

void textureeffect::create() {
    const char *vertex = GLUtils::openTextFile("vertex/transform_vertex_shader.glsl");
    const char *fragment = GLUtils::openTextFile("fragment/transform_fragment_shader.glsl");
    mPointProgramHandle = GLUtils::createProgram(&vertex, &fragment);
    if (!mPointProgramHandle) {
        LOGE("Could not create program");
        return;
    }
    srcTexure = GLUtils::loadTexture(picpath);
    checkError("create");
}
void textureeffect::createFrameBuffer(){
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
    checkError("createFrameBuffer");
    glViewport(left, top, right, bottom);
}
// 切换效果前把目的纹理内容拷贝到源纹理
int textureeffect::copyBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, right, bottom);
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
    glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, flipYMartrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    checkError("createFrameBuffer");
    return 0;
}
int textureeffect::copySrcBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, right, bottom);
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
    glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, flipYMartrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    checkError("createFrameBuffer");
    return 0;
}
void textureeffect::change(int l, int t, int r, int b) {
    left = l;
    top = t;
    right = r;
    bottom = b;
    checkError("change");
}
int textureeffect::renderCenter(FPOINT center, TFloat radius) {
    int32_t nRes;
    if (!initEffect){
        return -1;
    }
    if (!TuneEngine)
    {
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
    checkError("create");
    return 0;
}
int textureeffect::releaseEffect() {
    if (TuneEngine)
    {
        LOGE("releaseeffect");
        BeautiTune_UnInit(TuneEngine);
        TuneEngine = NULL;
    }
    return 0;
}
int checkError(const char* op) {
    int res = 0;
    for (GLint error = glGetError(); error; error
                                                    = glGetError()) {
        LOGE("after %s() glError (0x%x)\n", op, error);
        res = 1;
    }
    return res;
}
void textureeffect::draw() {
//    glViewport(0,0,mWidth,mHeight);
    if (mCompareFlag==0){
        //render To Texure
        glBindFramebuffer(GL_FRAMEBUFFER, 0);//绑定到默认纹理，渲染最后的纹理
        glBindTexture(GL_TEXTURE_2D, 0);
        glClearColor(0, 0, 0, 1);
        //render TO window
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        checkError("draw");
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
    } else {
        glBindTexture(GL_TEXTURE_2D, 0);
        glClearColor(0, 0, 0, 1);
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
    glDeleteFramebuffers(1, &fFrame);
    checkError("draw");
}
textureeffect *textureeffectobj;

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeSurfaceCreate(JNIEnv *env, jclass type,
                                                                      jobject assetManager,jint picwidth_,jint picheight_, jstring picpath_) {
    const char *picpath = env->GetStringUTFChars(picpath_, 0);
    GLUtils::setEnvAndAssetManager(env, assetManager);
    if (textureeffectobj) {
        delete textureeffectobj;
        textureeffectobj = NULL;
    }
    textureeffectobj = new textureeffect();
    textureeffectobj->picwidth = picwidth_;
    textureeffectobj->picheight = picheight_;
    textureeffectobj->picpath = (char *) picpath;
    textureeffectobj->create();
    env->ReleaseStringUTFChars(picpath_, picpath);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeSurfaceChange(JNIEnv *env,
                                                                      jclass type,
                                                                      jint left_, jint top_,jint right_,jint bottom_) {
    if (textureeffectobj) {
        textureeffectobj->change(left_, top_,right_,bottom_);
        textureeffectobj->createFrameBuffer();
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeRender(JNIEnv *env,
                                                               jclass type,
                                                               jfloat x, jfloat y) {
    if (textureeffectobj) {
        int ret=textureeffectobj->renderCenter({x,y},15.0f);
        textureeffectobj->mCompareFlag = 0;
        LOGE("ret=%d",ret);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeDrawFrame(JNIEnv *env,
                                                                  jclass type) {

    if (textureeffectobj) {
        textureeffectobj->draw();
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativereleaseEffect(JNIEnv *env,
                                                                      jclass type,jint effecttype_) {
    if (textureeffectobj) {
        LOGE("release");
        textureeffectobj->initEffect = 1;
        if (effecttype_==1){
            textureeffectobj->releaseEffect();
            textureeffectobj->ProType = TeethWhite;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_==2){
            textureeffectobj->releaseEffect();
            textureeffectobj->ProType = Smooth;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_==3){
            textureeffectobj->releaseEffect();
            textureeffectobj->ProType = Smooth;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->isMoreSmooth = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_==4){
            textureeffectobj->releaseEffect();
            textureeffectobj->ProType = Detail;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_==5){
            //之前是否选择其他按钮
            if ( textureeffectobj->hasEffect == 0){
                textureeffectobj->initEffect = 0;
            } else {
                textureeffectobj->bsWork = Erase;
            }
        } else if (effecttype_==6){
            textureeffectobj->releaseEffect();
            textureeffectobj->copySrcBuffer();
        } else if (effecttype_==7){
            textureeffectobj->copyBuffer();
        }
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeCompare(JNIEnv *env, jclass type,jint actionup_) {
    if (textureeffectobj) {
        textureeffectobj->mCompareFlag = actionup_;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeTransform(JNIEnv *env, jclass type,
                                                                 jfloatArray martrix_) {
    jfloat *martrix = env->GetFloatArrayElements(martrix_, NULL);
    if (textureeffectobj) {
        textureeffectobj->changeMartrix(martrix);
    }
    env->ReleaseFloatArrayElements(martrix_, martrix, 0);
}