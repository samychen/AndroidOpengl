#include "program.h"
#include "jnilogger.h"

static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}
static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
        = glGetError()) {
        LOGE("after %s() glError (0x%x)\n", op, error);
    }
}

static const GLfloat imageVertices[] = {
    -1.0f, -1.0f,
    1.0f, -1.0f,
    -1.0f,  1.0f,
    1.0f,  1.0f,
};


static const GLfloat TextureCoordinates[] = {
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
};


static GLuint esLoadShader ( GLenum type, const char *shaderSrc )
{
    GLuint shader;
    GLint compiled;

    // Create the shader object
    shader = glCreateShader ( type );
    if ( shader == 0 )
        return 0;

    // Load the shader source
    glShaderSource ( shader, 1, &shaderSrc, NULL );

    // Compile the shader
    glCompileShader ( shader );

    // Check the compile status
    glGetShaderiv ( shader, GL_COMPILE_STATUS, &compiled );

    if ( !compiled )
    {
        GLint infoLen = 0;

        glGetShaderiv ( shader, GL_INFO_LOG_LENGTH, &infoLen );

        if ( infoLen > 1 )
        {
            char* infoLog = (char*)malloc (sizeof(char) * infoLen );

            glGetShaderInfoLog ( shader, infoLen, NULL, infoLog );
            LOGE ( "Error compiling shader:\n%s\n", infoLog );

            free ( infoLog );
        }

        glDeleteShader ( shader );
        return 0;
    }

    return shader;

}


GLuint program::esLoadProgram ( TPCChar vertShaderSrc, TPCChar fragShaderSrc )
{
    GLuint vertexShader;
    GLuint fragmentShader;
    GLuint programObject;
    GLint linked;

//    LOGE ( "vertexShader == %s ", vertShaderSrc );
    // Load the vertex/fragment shaders
    vertexShader = esLoadShader ( GL_VERTEX_SHADER, vertShaderSrc );
    if ( vertexShader == 0 ){
        LOGE ( "error: vertexShader == 0 " );
        return 0;
    }

//    LOGE ( "fragmentShader == %s ", fragShaderSrc );
    fragmentShader = esLoadShader ( GL_FRAGMENT_SHADER, fragShaderSrc );
    if ( fragmentShader == 0 )
    {
        glDeleteShader( vertexShader );
        LOGE ( "error: fragmentShader == 0 " );
        return 0;
    }

    // Create the program object
    programObject = glCreateProgram ( );

    if ( programObject == 0 )
        return 0;

    glAttachShader ( programObject, vertexShader );
    checkGlError("glAttachVertexShader");
    glAttachShader ( programObject, fragmentShader );
    checkGlError("glAttachFragmentShader");

    // Link the program
    glLinkProgram ( programObject );
    checkGlError("glLinkProgram");

    // Check the link status
    glGetProgramiv ( programObject, GL_LINK_STATUS, &linked );

    if ( !linked )
    {
        LOGE ( "error: !linked " );
        GLint infoLen = 0;

        glGetProgramiv ( programObject, GL_INFO_LOG_LENGTH, &infoLen );

        if ( infoLen > 1 )
        {
            char* infoLog =(char*) malloc (sizeof(char) * infoLen );

            glGetProgramInfoLog ( programObject, infoLen, NULL, infoLog );
            LOGE ( "Error linking program:\n%s\n", infoLog );

            free ( infoLog );
        }

        glDeleteProgram ( programObject );
        return 0;
    }

    // Free up no longer needed shader resources
    glDeleteShader ( vertexShader );
    glDeleteShader ( fragmentShader );

    return programObject;
}

program::program(TInt32 width, TInt32 height, TPChar vertexShader, TPCChar fragShaderSrc)
{
    mProgramId = esLoadProgram(vertexShader, fragShaderSrc);
    mWidth = width;
    mHeight = height;
    mUsed = TFalse;

    glGenFramebuffers(1, &mFramebuffer);
//    checkGlError("glGenFramebuffers");

}

program::program(TPChar vertexShader, TPCChar fragShaderSrc) {
    mProgramId = esLoadProgram(vertexShader, fragShaderSrc);
    mUsed = TFalse;

    glGenFramebuffers(1, &mFramebuffer);
}

program::~program()
{
    glDeleteFramebuffers(1, &mFramebuffer);
    if(mProgramId)
       glDeleteProgram ( mProgramId );
}


TVoid program::bindTexture(TPCChar name, texture_2d * ptexture,GLenum filter)
{
    GLint attribute;
    int usetexture;

    if(mUsed == TFalse)
        glUseProgram(mProgramId);
    mUsed = TTrue;
    attribute = glGetUniformLocation(mProgramId, name);
    checkGlError("glGetUniformLocation");
    usetexture = ptexture->getTexturename() - GL_TEXTURE0;
    glActiveTexture(ptexture->getTexturename());
    checkGlError("glActiveTexture");
    glBindTexture(GL_TEXTURE_2D, ptexture->getTextureId());
    checkGlError("glBindTexture");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
//    checkGlError("glTexParameteri");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
//    checkGlError("glTexParameteri");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//    checkGlError("glTexParameteri");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//    checkGlError("glTexParameteri");
    glUniform1i(attribute, usetexture);
    checkGlError("glUniform1i");
}

TVoid program::bindTexture(TPCChar name, GLuint texId, GLuint texName, GLenum filter) {
    GLint attribute;
    int usetexture;

       if(mUsed == TFalse) {
        glUseProgram(mProgramId);
       }
    mUsed = TTrue;
    attribute = glGetUniformLocation(mProgramId, name);
    checkGlError("glGetUniformLocation");
    usetexture = texName - GL_TEXTURE0;
    glActiveTexture(texName);
    checkGlError("glActiveTexture");
    glBindTexture(GL_TEXTURE_2D, texId);
    checkGlError("glBindTexture");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
//    checkGlError("glTexParameteri");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
//    checkGlError("glTexParameteri");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//    checkGlError("glTexParameteri");
//    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//    checkGlError("glTexParameteri");
    glUniform1i(attribute, usetexture);
    checkGlError("glUniform1i");
}

TVoid program::unbindTexture(texture_2d * ptexture) {
    glActiveTexture(ptexture->getTexturename());
    checkGlError("glActiveTexture");
    glBindTexture(GL_TEXTURE_2D, 0);
    checkGlError("glBindTexture");
}

TBool program::has_uniform( TPCChar name ) {
    return glGetUniformLocation(mProgramId, name) != -1;
}

TVoid program::set_uniform_v(char* name, float value[], int length)
{
    int handler = glGetUniformLocation(mProgramId, name);
    if (handler == -1)
        return;

    switch (length) {
        case 1 :
            glUniform1fv(handler, 1, value);
            break;
        case 2 :
            glUniform2fv(handler, 1, value);
            break;
        case 3 :
            glUniform3fv(handler, 1, value);
            break;
        case 4 :
            glUniform4fv(handler, 1, value);
            break;
        case 16 :
            glUniformMatrix4fv(handler, 1, false, value);
            break;
    }
}

TVoid program::set_uniform_1i( TPCChar name, TInt32 value ) {
    GLint location = glGetUniformLocation(mProgramId, name);
    if (location >= 0) {
        glUniform1i(location, value);
        //assert(glGetError() == GL_NO_ERROR);
    }
}


TVoid program::set_uniform_1f( TPCChar name, float value ) {
    GLint location = glGetUniformLocation(mProgramId, name);
    if (location >= 0) {
        glUniform1f(location, value);
        //assert(glGetError() == GL_NO_ERROR);
    }
}


TVoid program::set_uniform_2f( TPCChar name, float x, float y ) {
    GLint location = glGetUniformLocation(mProgramId, name);
    if (location >= 0) {
        glUniform2f(location, x, y);
        //assert(glGetError() == GL_NO_ERROR);
    }
}


TVoid program::set_uniform_3f( TPCChar name, float x, float y, float z ) {
    GLint location = glGetUniformLocation(mProgramId, name);
    if (location >= 0) {
        glUniform3f(location, x, y, z);
        //assert(glGetError() == GL_NO_ERROR);
    }
}


TInt32 program::save(TPCChar* name)
{


}

TVoid program::useprogram()
{

//    if(mUsed == TFalse)
        glUseProgram(mProgramId);
    mUsed = TTrue;
}

TInt32 program::dowork(texture_2d * pDsttexture, TByte* pOutData)
{

//    LOGD("in %s---->",__FUNCTION__);
    GLint PositionAttribute, TextureCoordinateAttribute;

    if(mUsed == TFalse)
        glUseProgram(mProgramId);
    mUsed = TTrue;

    if(NULL!=pDsttexture){
        glBindFramebuffer(GL_FRAMEBUFFER, mFramebuffer);
    //    checkGlError("glBindFramebuffer");
        glActiveTexture(pDsttexture->getTexturename());
        glBindTexture(GL_TEXTURE_2D, pDsttexture->getTextureId());
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,pDsttexture->getTextureId(), 0);
//        LOGD("glFrameBuffer---->frameBuf:%d,,activeTex: %x,,TextureId:%d",
//                mFramebuffer,pDsttexture->getTexturename(), pDsttexture->getTextureId());
    }
//    checkGlError("glFramebufferTexture2D");

//     glViewport(0, 0, mWidth, mHeight);
//    float MVPMatrix[] = {1, 0.0, 0.0, 0.0, 0, 1, 0.0, 0.0, 0, 0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0};

    PositionAttribute = glGetAttribLocation(mProgramId, "aPosition");
    TextureCoordinateAttribute = glGetAttribLocation(mProgramId, "aTextureCoord");

    glClearColor(0, 0, 0, 1);
    glClear(GL_COLOR_BUFFER_BIT);

    glEnableVertexAttribArray(PositionAttribute);
    glVertexAttribPointer(PositionAttribute, 2, GL_FLOAT, 0, 0, imageVertices);
    glEnableVertexAttribArray(TextureCoordinateAttribute);
    glVertexAttribPointer(TextureCoordinateAttribute, 2, GL_FLOAT, 0, 0, TextureCoordinates);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    checkGlError("glDrawArrays");

    if(pOutData != NULL){
//        LOGD("glReadPixels---->size: %d x %d", mWidth, mHeight);
        glReadPixels(0, 0, mWidth, mHeight, GL_RGBA, GL_UNSIGNED_BYTE, pOutData);
//        LOGD("glReadPixels---->end");
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
//    LOGD("out %s---->",__FUNCTION__);
//    glBindFramebuffer(GL_FRAMEBUFFER, 0);
}
