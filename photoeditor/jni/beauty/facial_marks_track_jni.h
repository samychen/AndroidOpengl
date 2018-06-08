#ifndef __FACIAL_MARKS_TRACK_JNI_H__
#define __FACIAL_MARKS_TRACK_JNI_H__

#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1create(JNIEnv * env, jobject obj);
	JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1destroy(JNIEnv * env, jobject obj, jint handle);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1figure(JNIEnv * env, jobject obj, jint handle, jobject bmp, jobject faceRect, jintArray marks, jintArray eyeMarks);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1update(JNIEnv * env, jobject obj, jint handle, jobject bmp, jobject faceRect, jobject eye1, jobject eye2, jobject mouth, jintArray marks);
	JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1reset(JNIEnv * env, jobject obj, jint handle);

#ifdef __cplusplus
};
#endif /* __cplusplus */

#endif
