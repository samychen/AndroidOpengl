#ifndef __EDIT_BITMAP_JNI_H__
#define __EDIT_BITMAP_JNI_H__

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1create(JNIEnv * env, jobject obj, jobject bmp);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1destroy(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1reset(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1apply(JNIEnv * env, jobject obj, jint handle, jobject image);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1original(JNIEnv * env, jobject obj, jint handle, jboolean orig);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1save(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_engine_EditBitmap_native_1restore(JNIEnv * env, jobject obj, jint handle);

#ifdef __cplusplus
};
#endif /* __cplusplus */

#endif /* __EDIT_BITMAP_JNI_H__ */
