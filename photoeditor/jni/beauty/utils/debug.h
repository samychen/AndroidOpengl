#ifndef __DEBUG_H__
#define __DEBUG_H__
#ifndef DEBUG
#define DEBUG
#endif

#include <android/log.h>

#ifdef DEBUG
#define  LOG_TAG    "JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#define LOGI(...)
#define LOGE(...)
#endif //DEBUG



#define ASSERT(e) { \
	if(!(e)) { \
		LOGE("Assert failed: %s,%d", __FILE__, __LINE__); \
	} \
}

#ifdef __cplusplus
extern "C" {
#endif

int  dateValidate();
void dumpToFile(const char* path, unsigned char* buf, int size);

#ifdef __cplusplus
}
#endif

#endif // __DEBUG_H__
