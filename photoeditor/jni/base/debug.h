#ifndef __DEBUG_H__
#define __DEBUG_H__

#include <GLES2/gl2.h>
#include <android/log.h>

#define  LOG_TAG    "GLRenderer_JNI"
#define  LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define VALID_YEAR 2014
#define VALID_MONTH 5
int  dateValidate();

#define ASSERT(e) { \
	if(!(e)) { \
		LOGE("Assert failed: %s,%d", __FILE__, __LINE__); \
	} \
}

#ifdef __cplusplus
extern "C" {
#endif

void dumpToFile(const char* path, unsigned char* buf, int size);

#ifdef __cplusplus
}
#endif

#endif // __DEBUG_H__
