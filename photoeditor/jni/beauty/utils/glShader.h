#ifndef __GLSHADER_H__
#define __GLSHADER_H__

#include <GLES2/gl2.h>
#include <EGL/egl.h>

#define SHADER_BUFFER_SIZE	(4*1024)

#ifdef __cplusplus
extern "C" {
#endif

void checkGlError(const char* op);
GLuint createProgram(const char* pVertexSource, const char* pFragmentSource);
GLuint createProgramWithCipher(char* pVertexCypher, char* pFragmentCypher);
GLuint createTexture();

#ifdef __cplusplus
}
#endif

#endif // __GLSHADER_H__
