#include "GLMakeup.h"
#include "jnilogger.h"
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#define F_SHADER "precision mediump float; varying vec2 textureCoordinate;\
uniform sampler2D texture;\
uniform sampler2D texture2;\
uniform sampler2D PreviewWhitenTexture;\
uniform int level;\
vec3 yuvDecode(vec2 texCoord)\
{\
    float y = (texture2D(texture, texCoord).r -0.0627)* 1.164;\
    vec2 uv = texture2D(texture2, texCoord).ra- 0.5;\
    vec3 rgb = vec3( 1.596 * uv.x, - 0.813 * uv.x - 0.391 * uv.y, 2.018 * uv.y) + y;\
    return rgb;\
}\
void main()\
{\
        if(level>0){\
                vec3 texel = yuvDecode(textureCoordinate);\
                texel = vec3(\
                texture2D(PreviewWhitenTexture, vec2(texel.r, .5)).r,\
                texture2D(PreviewWhitenTexture, vec2(texel.g, .5)).g,\
                texture2D(PreviewWhitenTexture, vec2(texel.b, .5)).b);\
                gl_FragColor = vec4(texel, 1.0);\
        }else{\
            gl_FragColor = vec4(yuvDecode(textureCoordinate), 1.0);\
        }\
}"

#define V_SHADER "uniform mat4 uMVPMatrix;attribute vec4 aPosition;attribute vec2 aTextureCoord;\
varying vec2 textureCoordinate;void main() { gl_Position = uMVPMatrix * aPosition; textureCoordinate = aTextureCoord;}"

unsigned char pPreviewWhiten[1024];
void resetWhitenArr(int level){
    unsigned char whiten[]={
    0, 0, 0, 255, 0, 0, 0, 255,
    0, 0, 0, 255, 0, 0, 0, 255,
    0, 0, 0, 255, 0, 0, 0, 255,
    0, 0, 0, 255, 0, 0, 0, 255,
    0, 0, 0, 255, 0, 0, 0, 255,
    2, 0, 0, 255, 2, 0, 0, 255,
    2, 1, 2, 255, 2, 2, 2, 255,
    4, 3, 4, 255, 5, 4, 5, 255,
    6, 6, 7, 255, 8, 8, 8, 255,
    10, 10, 10, 255, 11, 11, 11, 255,
    13, 12, 13, 255, 14, 14, 14, 255,
    16, 16, 16, 255, 18, 18, 18, 255,
    21, 20, 20, 255, 23, 22, 22, 255,
    25, 24, 25, 255, 27, 26, 27, 255,
    29, 29, 29, 255, 31, 30, 31, 255,
    33, 32, 33, 255, 35, 34, 35, 255,
    37, 36, 37, 255, 39, 37, 38, 255,
    41, 40, 40, 255, 43, 42, 42, 255,
    45, 44, 45, 255, 48, 46, 47, 255,
    50, 48, 48, 255, 52, 51, 51, 255,
    54, 53, 53, 255, 56, 55, 54, 255,
    58, 56, 57, 255, 60, 58, 58, 255,
    62, 60, 60, 255, 64, 62, 63, 255,
    65, 64, 64, 255, 67, 67, 66, 255,
    69, 69, 68, 255, 70, 70, 70, 255,
    73, 72, 72, 255, 75, 74, 75, 255,
    77, 76, 75, 255, 78, 78, 78, 255,
    81, 80, 80, 255, 82, 81, 83, 255,
    84, 84, 84, 255, 86, 85, 86, 255,
    89, 88, 88, 255, 90, 90, 89, 255,
    93, 92, 91, 255, 94, 94, 94, 255,
    96, 96, 95, 255, 96, 98, 97, 255,
    99, 100, 99, 255, 101, 100, 101, 255,
    103, 103, 102, 255, 105, 105, 104, 255,
    107, 108, 106, 255, 109, 109, 108, 255,
    110, 110, 109, 255, 112, 112, 112, 255,
    114, 115, 114, 255, 117, 116, 116, 255,
    118, 118, 118, 255, 120, 121, 119, 255,
    121, 122, 121, 255, 124, 124, 122, 255,
    125, 126, 125, 255, 127, 128, 126, 255,
    128, 131, 128, 255, 131, 132, 131, 255,
    134, 134, 132, 255, 133, 136, 133, 255,
    136, 138, 137, 255, 138, 140, 138, 255,
    139, 142, 141, 255, 141, 144, 142, 255,
    142, 145, 144, 255, 144, 148, 146, 255,
    146, 150, 148, 255, 148, 151, 151, 255,
    151, 154, 151, 255, 152, 156, 153, 255,
    154, 157, 155, 255, 154, 159, 156, 255,
    156, 160, 158, 255, 158, 162, 160, 255,
    158, 164, 162, 255, 162, 166, 163, 255,
    162, 167, 164, 255, 164, 169, 166, 255,
    165, 170, 167, 255, 167, 171, 170, 255,
    168, 172, 171, 255, 170, 174, 171, 255,
    173, 175, 173, 255, 174, 176, 175, 255,
    174, 177, 176, 255, 176, 178, 178, 255,
    180, 179, 178, 255, 179, 181, 180, 255,
    181, 183, 181, 255, 182, 185, 182, 255,
    184, 186, 183, 255, 185, 186, 184, 255,
    185, 187, 185, 255, 187, 189, 188, 255,
    188, 189, 189, 255, 191, 191, 190, 255,
    190, 192, 191, 255, 193, 193, 193, 255,
    193, 193, 193, 255, 195, 195, 194, 255,
    196, 196, 196, 255, 197, 196, 196, 255,
    198, 198, 197, 255, 199, 199, 199, 255,
    200, 200, 200, 255, 201, 201, 200, 255,
    202, 202, 201, 255, 202, 202, 202, 255,
    203, 204, 203, 255, 204, 205, 204, 255,
    205, 205, 205, 255, 206, 206, 206, 255,
    207, 208, 206, 255, 207, 208, 207, 255,
    208, 208, 208, 255, 209, 209, 209, 255,
    210, 210, 210, 255, 210, 211, 210, 255,
    211, 211, 211, 255, 212, 212, 212, 255,
    212, 213, 212, 255, 214, 214, 213, 255,
    214, 214, 213, 255, 215, 214, 214, 255,
    215, 215, 215, 255, 216, 215, 216, 255,
    217, 216, 216, 255, 217, 217, 217, 255,
    219, 218, 218, 255, 219, 218, 218, 255,
    219, 219, 219, 255, 220, 219, 219, 255,
    220, 220, 220, 255, 221, 220, 220, 255,
    221, 221, 221, 255, 222, 222, 221, 255,
    223, 222, 222, 255, 223, 222, 222, 255,
    223, 223, 223, 255, 224, 223, 223, 255,
    225, 224, 224, 255, 225, 225, 224, 255,
    225, 225, 225, 255, 226, 225, 225, 255,
    227, 226, 226, 255, 227, 227, 226, 255,
    227, 227, 227, 255, 228, 228, 227, 255,
    229, 228, 228, 255, 229, 229, 228, 255,
    229, 229, 229, 255, 230, 230, 229, 255,
    230, 230, 230, 255, 231, 230, 230, 255,
    231, 231, 230, 255, 231, 231, 231, 255,
    232, 231, 231, 255, 232, 232, 232, 255,
    232, 232, 232, 255, 233, 233, 233, 255,
    233, 233, 233, 255, 233, 233, 233, 255,
    234, 233, 234, 255, 234, 234, 234, 255,
    235, 234, 235, 255, 235, 235, 235, 255,
    235, 235, 235, 255, 235, 235, 235, 255,
    236, 236, 236, 255, 237, 236, 236, 255,
    237, 237, 236, 255, 237, 237, 237, 255,
    238, 238, 237, 255, 238, 238, 238, 255,
    238, 239, 237, 255, 239, 238, 239, 255,
    239, 239, 239, 255, 239, 240, 239, 255,
    240, 239, 240, 255, 240, 240, 240, 255,
    241, 240, 240, 255, 241, 240, 241, 255,
    241, 241, 241, 255, 241, 240, 241, 255,
    241, 241, 241, 255, 242, 242, 242, 255,
    242, 242, 242, 255, 242, 243, 243, 255,
    243, 242, 243, 255, 243, 242, 243, 255,
    243, 243, 243, 255, 244, 243, 243, 255,
    244, 243, 243, 255, 244, 244, 244, 255,
    244, 244, 245, 255, 245, 245, 245, 255,
    245, 245, 245, 255, 245, 245, 246, 255,
    245, 245, 246, 255, 246, 246, 246, 255,
    246, 246, 246, 255, 246, 247, 246, 255,
    246, 246, 246, 255, 246, 246, 246, 255,
    247, 247, 247, 255, 248, 248, 247, 255,
    247, 247, 247, 255, 248, 248, 247, 255,
    248, 248, 248, 255, 248, 248, 248, 255,
    249, 249, 249, 255, 249, 249, 249, 255,
    249, 249, 249, 255, 250, 249, 250, 255,
    250, 249, 250, 255, 250, 250, 250, 255,
    250, 250, 250, 255, 251, 250, 250, 255,
    251, 250, 250, 255, 251, 251, 251, 255,
    252, 251, 251, 255, 252, 251, 252, 255,
    252, 251, 252, 255, 253, 252, 253, 255,
    253, 252, 253, 255, 253, 253, 253, 255,
    253, 253, 253, 255, 253, 253, 253, 255,
    253, 253, 253, 255, 254, 254, 254, 255,
    254, 254, 254, 255, 255, 255, 255, 255
    };
    int Levelparam = level; // [0 - 100]
    int curvalue;

    for (int y = 0; y < 1024; y+=4)
    {
        int x = y >> 2;
        curvalue = (whiten[y]-x)* Levelparam / 100 + x;
        pPreviewWhiten[y] = (unsigned char)curvalue;
        curvalue = (whiten[y+1]-x)* Levelparam / 100 + x;
        pPreviewWhiten[y+1] = (unsigned char)curvalue;
        curvalue = (whiten[y+2]-x)* Levelparam / 100 + x;
        pPreviewWhiten[y+2] = (unsigned char)curvalue;

    }
}

GLMakeup::GLMakeup():mProgram(NULL),mLevel(-1),mWhitenTexture(NULL),mYTexture(NULL), mUVTexture(NULL){
}

void GLMakeup::release() {
    deleteC(mProgram);
    deleteC(mWhitenTexture);
    deleteC(mYTexture);
    deleteC(mUVTexture);
}

GLMakeup::~GLMakeup() {
    release();
}

void GLMakeup::surfaceChanged(int surfaceWidth, int surfaceHeight){
    glEnable(GL_TEXTURE_2D);
    release();
}

int GLMakeup::skinWhiten(void *yBuf, void *uvBuf, int width, int height,float *matrix, int matsize,int level,void *outPixels){

    int uvWidth = width / 2;
    int uvHeight = height / 2;

    if(NULL == mProgram){
        mProgram = new program(width, height, V_SHADER, F_SHADER);
    }
    // Y texture
    if(NULL == mYTexture){
        mYTexture = new texture_2d(NULL, width, height, GL_LUMINANCE, GL_TEXTURE0);
    }
    // YU texture
    if(NULL == mUVTexture){
        mUVTexture = new texture_2d(NULL, uvWidth, uvHeight, GL_LUMINANCE_ALPHA, GL_TEXTURE1);
    }
    if(NULL==mWhitenTexture){
        mWhitenTexture = new texture_2d(pPreviewWhiten, 256, 1, GL_RGBA, GL_TEXTURE2, GL_UNSIGNED_BYTE);
    }

    mProgram->useprogram();

    mYTexture->subImage(0, 0, width, height, GL_LUMINANCE, GL_UNSIGNED_BYTE, yBuf);
    mUVTexture->subImage(0, 0, uvWidth, uvHeight, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, uvBuf);
    //change level
    if(mLevel!=level){
        resetWhitenArr(level);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 256, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, pPreviewWhiten);
        mWhitenTexture->subImage(0, 0, 256, 1, GL_RGBA, GL_UNSIGNED_BYTE, pPreviewWhiten);
        mLevel=level;
    }

    //Y texture
    mProgram->bindTexture("texture", mYTexture);
    //UV texture
    mProgram->bindTexture("texture2", mUVTexture);

    //PreviewWhiten texture
    mProgram->bindTexture("PreviewWhitenTexture", mWhitenTexture);

    mProgram->set_uniform_v("uMVPMatrix", matrix, matsize);
    mProgram->set_uniform_1i("level", level);

    mProgram->dowork(NULL, (TByte*)outPixels);

    mProgram->unbindTexture(mWhitenTexture);
    mProgram->unbindTexture(mUVTexture);
    mProgram->unbindTexture(mYTexture);

    LOGD("out %s---->level:%d",__FUNCTION__,level);
    return 0;
}
