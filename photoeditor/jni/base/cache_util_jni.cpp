#include <stdio.h>

extern "C" {
	JNIEXPORT void JNICALL Java_com_cam001_util_CacheUtil_glReadPixelsToBitmap(JNIEnv * env, jobject obj, jobject bmp);
};
