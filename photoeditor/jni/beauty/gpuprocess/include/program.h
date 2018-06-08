#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <stdio.h>
#include <stdlib.h>
#include "tcomdef.h"
#include "texture_2d.h"

#ifndef _Included_program
#define _Included_program

class program {
public:
    program(TInt32 width, TInt32 height, TPChar vertexShader, TPCChar fragShaderSrc);
    program(TPChar vertexShader, TPCChar fragShaderSrc);

    program() {};
    virtual ~program();

    TInt32 dowork(texture_2d * pDsttexture, TByte* pOutData); // if pOutData != null, readpixel to pOutData
    TInt32 save(TPCChar* name);
    TVoid useprogram();
    TVoid bindTexture(TPCChar name, texture_2d * ptexture,GLenum filter = GL_NEAREST);
    TVoid bindTexture(TPCChar name, GLuint texId, GLuint texName, GLenum filter = GL_NEAREST);
    TVoid unbindTexture(texture_2d * ptexture);
    TBool has_uniform( TPCChar name );
    TVoid set_uniform_v(char* name, float value[], int length);
    TVoid set_uniform_1i( TPCChar name, TInt32 value );
    TVoid set_uniform_1f( TPCChar name, float value );
    TVoid set_uniform_2f( TPCChar name, float x, float y );
    TVoid set_uniform_3f( TPCChar name, float x, float y, float z );



protected:
    GLuint esLoadProgram ( TPCChar vertShaderSrc, TPCChar fragShaderSrc );

    GLuint mProgramId;

    GLuint mFramebuffer;

    TBool mUsed;

    TInt32 mWidth;
    TInt32 mHeight;

};
#endif
