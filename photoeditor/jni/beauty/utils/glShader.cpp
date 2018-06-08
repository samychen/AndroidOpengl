#include "glShader.h"
#include "debug.h"
#include "des.h"
#include <stdlib.h>

#define OBFUSCATED_STRING "中科创达（杭州）科技有限公司,版权所有。ThunderSoft.Inc CopyRight@2012 All rights reserved. "
#define OBFUSCATED_SIZE 	64
#define DES_KEY_SIZE 		8


void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}


GLuint loadShader(GLenum shaderType, const char* pSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char* buf = (char*) malloc(infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d:\n%s\n",
                            shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint createProgram(const char* pVertexSource, const char* pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

	LOGE("-----createProgram-------");

    GLuint program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        glLinkProgram(program);
		 checkGlError("glLinkProgram");
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char* buf = (char*) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
	else
	{
		LOGE("glCreateProgram error");
	}

	glDeleteShader(vertexShader);
	glDeleteShader(pixelShader);
	glValidateProgram (program);
	checkGlError("glValidateProgram");
    return program;
}

/*
GLuint createProgramWithCipher(char* pVertexCipher, char* pFragmentCipher) {
	//**********Initialize KeyString**********************
	char cbObfKey[OBFUSCATED_SIZE];
	memset(cbObfKey, 0, OBFUSCATED_SIZE);
	int cpySize = strlen(OBFUSCATED_STRING)>OBFUSCATED_SIZE
			?OBFUSCATED_SIZE:strlen(OBFUSCATED_STRING);
	memcpy(cbObfKey, OBFUSCATED_STRING, cpySize);

	//**********Obfuscate the KeyString to DES Key***********
	char cbDesKey[DES_KEY_SIZE];
	memset(cbDesKey, 0, DES_KEY_SIZE);
	cbDesKey[0] = ~cbObfKey[6];
	cbDesKey[1] = cbObfKey[10] | cbObfKey[18];
	cbDesKey[2] = cbObfKey[23] | cbObfKey[26];
	cbDesKey[3] = cbObfKey[53] ^ ~cbObfKey[56];
	cbDesKey[4] = cbObfKey[17] | cbObfKey[61];
	cbDesKey[5] = ~cbObfKey[20] | cbObfKey[40];
	cbDesKey[6] = cbObfKey[34] ^ cbObfKey[37];
	cbDesKey[7] = ~cbObfKey[45];
	//***************************************************

	for(int i=0; i<8; i++) {
		LOGI("0x%02X\n",cbDesKey[i]);
	}

	char* cbVertexShader = (char*)malloc(SHADER_BUFFER_SIZE);
	char* cbFragmentShader = (char*)malloc(SHADER_BUFFER_SIZE);
	DES_DecryptBuffer(pVertexCipher, cbDesKey,cbVertexShader, SHADER_BUFFER_SIZE);
	DES_DecryptBuffer(pFragmentCipher, cbDesKey,cbFragmentShader, SHADER_BUFFER_SIZE);
	LOGI(cbVertexShader);
	LOGI(cbFragmentShader);
	GLuint nProgram = createProgram(cbVertexShader, cbFragmentShader);
	free(cbVertexShader);
	free(cbFragmentShader);
	return nProgram;
}
*/
GLuint createTexture() {
	GLuint nTex;
	glGenTextures(1, &nTex);
	glBindTexture(GL_TEXTURE_2D, nTex);

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

	return nTex;
}
