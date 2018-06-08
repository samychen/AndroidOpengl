#ifndef __NV21SAMPLER_JNI_H__
#define __NV21SAMPLER_JNI_H__

#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

	JNIEXPORT jint JNICALL Java_com_ts_engine_NV21Sampler_native_1create(JNIEnv * env, jobject obj);
	JNIEXPORT void JNICALL Java_com_ts_engine_NV21Sampler_native_1destroy(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT void JNICALL Java_com_ts_engine_NV21Sampler_native_1downSample(JNIEnv * env, jobject obj, jint handle, jbyteArray inBuf, jint inWidth, jint inHeight, jbyteArray outBuf, jint sampleSize, jint rotate);

#ifdef __cplusplus
};
#endif /* __cplusplus */

#endif
