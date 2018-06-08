#ifndef __BITMAP_UTIL_JNI_H__
#define __BITMAP_UTIL_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_init(JNIEnv * env,
		jobject obj, jobject asset, jint mode);

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_uninit(JNIEnv* env,
		jobject obj, jint handle);

JNIEXPORT jobjectArray JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_facetracking(
		JNIEnv* env, jobject obj, jint handle, jbyteArray imgarr, jint width,
		jint height, jint rotate);

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_facemakeuprt(
		JNIEnv* env, jobject obj, jint handle, jbyteArray imgarr, jint width,
		jint height, jobjectArray jpointarr);

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_makeuploadresource(
		JNIEnv* env, jobject obj, jint handle, jint part, jobject style,
		jboolean isStatic);

JNIEXPORT jobjectArray JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_jnifacedetect(
		JNIEnv* env, jobject obj, jint handle, jobject bmp, jobject face, jobject leye, jobject reye, jobject mouth);

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_facemakeuprtbmp__I_3Landroid_graphics_Point_2Landroid_graphics_Bitmap_2(
		JNIEnv* env, jobject obj, jint handle, jobjectArray jpointarr,
		jobject dstbmp);

#ifdef __cplusplus
}
;
#endif /* __cplusplus */

#endif /* __BITMAP_UTIL_JNI_H__ */
