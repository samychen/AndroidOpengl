#ifndef __DEBUG_H__
#define __DEBUG_H__

#include <GLES2/gl2.h>
#include <android/log.h>
#include <assert.h>

#define  LOG_TAG    "JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#define  LOGD(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define VALID_YEAR 2015
#define VALID_MONTH 12
int  dateValidate();

#define ASSERT(e) { \
	if(!(e)) { \
		LOGE("Assert failed: %s,%d", __FILE__, __LINE__); \
		assert(false); \
	} \
}

#ifdef __cplusplus
extern "C" {
#endif

void printGLString(const char *name, GLenum s);
void dumpToFile(const char* path, unsigned char* buf, int size);
void dumpToFile2(const char* path, unsigned char* buf, int size);
#ifdef __cplusplus
}
#endif

#endif // __DEBUG_H__
