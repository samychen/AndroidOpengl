#include "texture_2d.h"
#include <android/bitmap.h>

static int toolsavefile(unsigned char* pData, int size, char* name)
{
    FILE *pf =fopen(name, "wb");
    if(pf != 0){
        fwrite(pData, size, 1, pf);
        fclose(pf);
        return 1;

       }
    return 0;
}

static int toolloadfile(unsigned char* pData, int size, char* name)
{

    FILE *pf =fopen(name, "rb");
    if(pf != 0){
        fread(pData, size, 1, pf);
        fclose(pf);
        return 1;

       }
    return 0;
}


texture_2d::texture_2d(GLvoid* pImg, TInt32 w, TInt32 h, GLint format, TInt32 texturename,GLenum texturetype)
{
    mpPixel = pImg;

    genTexture(w, h, format, texturename, texturetype);

//    checkGlError("glBindTexture");
    glTexImage2D(GL_TEXTURE_2D, 0, format, w, h, 0, format, texturetype, pImg);

    glBindTexture(GL_TEXTURE_2D, 0);
}

texture_2d::texture_2d():mWidth(0),mHeight(0),mTexturename(0),mColorFormat(0),mTextureId(0){

}

texture_2d::~texture_2d()
{
    glDeleteTextures(1, &mTextureId);
}

void texture_2d::genTexture(TInt32 w, TInt32 h, GLint format, TInt32 texturename,GLenum texturetype){
    mWidth = w;
    mHeight = h;
    mTexturename = texturename;
    mColorFormat = format;

    glGenTextures(1, &mTextureId);
    glBindTexture(GL_TEXTURE_2D, mTextureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    // This is necessary for non-power-of-two textures
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
}

void texture_2d::subImage(GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLenum type, const GLvoid* pixels){
    glBindTexture(GL_TEXTURE_2D, mTextureId);
    glTexSubImage2D (GL_TEXTURE_2D, 0, xoffset,  yoffset, width, height, format, type, pixels);
    glBindTexture(GL_TEXTURE_2D, 0);
}

TInt32 texture_2d::getWidth(){
    return mWidth;
}

TInt32 texture_2d::getHeight(){
    return mHeight;
}

TInt32 texture_2d::getColorFormart(){
    return mColorFormat;
}

TInt32 texture_2d::getTexturename(){
    return mTexturename;
}

GLuint texture_2d::getTextureId(){
    return mTextureId;
}
