//
// Created by 000 on 2018/6/11.
//
#include "textureeffect.h"

int checkError(const char* op) {
    int res = 0;
    for (GLint error = glGetError(); error; error
                                                    = glGetError()) {
        LOGE("after %s() glError (0x%x)\n", op, error);
        res = 1;
    }
    return res;
}
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

void textureeffect::createFrameBuffer(){
    glGenFramebuffers(1, &fFrame);
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, picwidth, picheight);
    glGenTextures(1, &dstTexure);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, picwidth, picheight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
    glUseProgram(mPointProgramHandle);
    getAttrLocation();
    glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, flipYMartrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    glBindTexture(GL_TEXTURE_2D, 0);
}
// 切换效果前把目的纹理内容拷贝到源纹理
int textureeffect::copyBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
//    glViewport(0, 0, mWidth, mHeight);
    glViewport(0, 0, picwidth, picheight);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, srcTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
    glUseProgram(mPointProgramHandle);
    getAttrLocation();
    glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, flipYMartrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glDisableVertexAttribArray(mPositionHandle);
    glDisableVertexAttribArray(mTextureCoordinateHandle);
    return 0;
}
// 取消效果后把源纹理内存拷贝到目的纹理
int textureeffect::copySrcBuffer() {
    glBindFramebuffer(GL_FRAMEBUFFER, fFrame);
    glViewport(0, 0, picwidth, picheight);
    glBindTexture(GL_TEXTURE_2D, dstTexure);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, dstTexure, 0);
    GLenum status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE)
        LOGE( "FBO Initialization Failed.");
    glUseProgram(mPointProgramHandle);
    getAttrLocation();
    glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
    glEnableVertexAttribArray(mPositionHandle);
    glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
    glEnableVertexAttribArray(mTextureCoordinateHandle);
    glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, flipYMartrix);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, srcTexure);
    glUniform1i(mTextureLocation, 0);
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glDisableVertexAttribArray(mPositionHandle);
    glDisableVertexAttribArray(mTextureCoordinateHandle);
    return 0;
}

void textureeffect::change(int l, int t, int r, int b,int w,int h) {
    left = l;
    top = t;
    right = r;
    bottom = b;
    mWidth = w;
    mHeight = h;
    checkError("change");
}
int textureeffect::initGLEffect() {
    int32_t nRes = 0;
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
        nRes = BeautiTune_PreProcess(TuneEngine, srcTexure);
        if (nRes)
        {
            return -3;
        }
    }
    return nRes;
}
int textureeffect::renderCenter(FPOINT center, TFloat radius) {
    LOGE("startrender");
    int32_t nRes = 0;
    //3.响应每次涂抹动作，单次涂抹产生的参数有（center，radius），擦除动作也会产生同样的参数
    TypePara Para = { 0 };
    if (ProType == TeethWhite)
    {
        Para.BsWork = bsWork;//涂抹还是擦除
        nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, radius,&Para, ImgBuf);
    }
    else if (ProType == Smooth)
    {
        Para.IsMoreSmooth = isMoreSmooth;//是否需要更强的模糊
        Para.BsWork = bsWork;
        nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, radius,&Para, ImgBuf);
    }
    else if (ProType == Detail)
    {
        Para.BsWork = bsWork;
        nRes = BeautiTune_Process(TuneEngine, srcTexure, dstTexure, &center, radius, &Para, ImgBuf);
    }
    draw();
    return nRes;
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

void textureeffect::draw() {
    LOGE("startdraw");
    glViewport(left, top, right, bottom);
    if (mCompareFlag==0){
        //render To Texure
        glBindFramebuffer(GL_FRAMEBUFFER, 0);//绑定到默认纹理，渲染最后的纹理
        glClearColor(1, 1, 1, 1);
        //render TO window
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mPointProgramHandle);
//        getAttrLocation();
        glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
        glEnableVertexAttribArray(mTextureCoordinateHandle);
        glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, mOriMartrix);
        glActiveTexture(GL_TEXTURE0);
//        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, dstTexure);
        glUniform1i(mTextureLocation, 0);
    } else {// compare时显示原纹理
//        glBindTexture(GL_TEXTURE_2D, 0);
        glClearColor(1, 1, 1, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(mPointProgramHandle);
//        getAttrLocation();
        glVertexAttribPointer(mPositionHandle, POSITION_SIZE, GL_FLOAT, GL_FALSE, 0, VERTEXPOSITION);
        glEnableVertexAttribArray(mPositionHandle);
        glVertexAttribPointer(mTextureCoordinateHandle, TEX_COORD_SIZE, GL_FLOAT, GL_FALSE, 0, COORDINATEPOSITION);
        glEnableVertexAttribArray(mTextureCoordinateHandle);
        glUniformMatrix3fv(mMVPMatrixHandle, 1, GL_FALSE, mOriMartrix);
        glActiveTexture(GL_TEXTURE0);
//        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, srcTexure);
        glUniform1i(mTextureLocation, 0);
    }
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    glDisableVertexAttribArray(mPositionHandle);
    glDisableVertexAttribArray(mTextureCoordinateHandle);
    LOGE("end Draw");//调用java层render.onDraw
}

void textureeffect::getAttrLocation() {
    mMVPMatrixHandle = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
    mPositionHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_Position");
    mTextureLocation = (GLuint) glGetUniformLocation(mPointProgramHandle, "u_Texture");
    mTextureCoordinateHandle = (GLuint) glGetAttribLocation(mPointProgramHandle, "a_TexCoordinate");
}

void textureeffect::destroyTexture() {
    glDeleteFramebuffers(1, &fFrame);
    glDeleteTextures(1,&srcTexure);
    glDeleteTextures(1,&dstTexure);
}
