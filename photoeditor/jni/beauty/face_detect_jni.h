#ifndef __FACE_DETECT_JNI_H__
#define __FACE_DETECT_JNI_H__

#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1create(JNIEnv * env, jobject obj);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1destroy(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1detect__I_3BII(JNIEnv * env, jobject obj, jint handle, jbyteArray nv21, jint width, jint height);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1detect__ILandroid_graphics_Bitmap_2(JNIEnv * env, jobject obj, jint handle, jobject bitmap);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1count(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1face_1info(JNIEnv * env, jobject obj, jint handle, jint index, jobject face, jobject eye1, jobject eye2, jobject mouth);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1hair_1info(JNIEnv * env, jobject obj, jint handle, jobject hair1, jobject hair2, jobject hair3);

#ifdef __cplusplus
};
#endif /* __cplusplus */

#endif
