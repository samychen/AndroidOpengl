#include "facial_marks_track_jni.h"
#include "ts-facial-outline.h"
#include <android/bitmap.h>
#include "jRect.h"
#include "jPoint.h"
#include "utils/debug.h"
#include "utils/BGRNV21.h"
#include <time.h>
#include <stdlib.h>

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1create(
		JNIEnv * env, jobject obj) {
	return (jint)sakFacialTrack_create();
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1destroy(
		JNIEnv * env, jobject obj, jint handle) {
	THandle hdl = (THandle)handle;
	sakFacialTrack_destroy(hdl);
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1figure(
		JNIEnv * env, jobject obj, jint handle, jobject bmp, jobject faceRect, jintArray marks, jintArray eyeMarks) {
	THandle hdl = (THandle)handle;
	AndroidBitmapInfo info;
	void* pPixels;
	LOGI("FacialMarksTrack figure <-----");
	TRECT rect;
	jRect jrect(env, faceRect);
	rect.left = jrect.getLeft();
	rect.top = jrect.getTop();
	rect.right = jrect.getRight();
	rect.bottom = jrect.getBottom();

	int* pMarks = env->GetIntArrayElements(marks, 0);
	int marksLen = env->GetArrayLength(marks);
	int* pEyeMarks = env->GetIntArrayElements(eyeMarks, 0);

	ASSERT(AndroidBitmap_getInfo(env, bmp, &info)
			== ANDROID_BITMAP_RESUT_SUCCESS);
	LOGI("width = %d, height = %d, stride = %d", info.width, info.height,
			info.stride);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels)
			== ANDROID_BITMAP_RESUT_SUCCESS);

	TUInt8* pNV21 = (TUInt8*) malloc((info.width * info.height * 3 + 1) / 2);
//	LOGI("pPixels=0x%08X  pNV21=0x%08X", pPixels, pNV21);
	long t = clock();
	RGBA8888_to_NV21((TUInt8*) pPixels, pNV21, info.width, info.height,
			info.stride);
//	LOGI("NV21 cost %d",(clock()-t));
	TSOFFSCREEN os;
	os.u32PixelArrayFormat = TS_PAF_NV21;
	os.i32Width = info.width;
	os.i32Height = info.height;
	os.ppu8Plane[0] = (TUInt8*) pNV21;
	os.ppu8Plane[1] = (TUInt8*) pNV21 + info.width * info.height;
	os.ppu8Plane[2] = os.ppu8Plane[3] = 0;
	os.pi32Pitch[0] = info.width;
	os.pi32Pitch[1] = info.width;

	t = clock();
//	LOGI("Figure rect=%d, %d, %d, %d", rect.left, rect.top, rect.right, rect.bottom);
	int errorCode = sakFacialTrack_figure(hdl, &os, rect, 0);
	LOGI("Figure cost %d",(clock()-t));
	if(errorCode!=0) {
		LOGE("sakFacialTrack_figure failed: %d", errorCode);
	}
	sakFacialTrack_setProperty(hdl, (const TPChar)"landmark-type", "default");
	double* tmp = (double*)malloc(marksLen*sizeof(double));
	sakFacialTrack_getProperty(hdl, (const TPChar)"landmarks", (void*)tmp);
	for(int i=0; i<marksLen; i++) {
		pMarks[i] = (int)tmp[i];
		LOGE("Figure mark======= %d,%d",i,pMarks[i]);
	}
	free(tmp);
//	sakFacialTrack_setProperty(hdl, (const TPChar)"landmark-type", "eyes4");
//	sakFacialTrack_getProperty(hdl, (const TPChar)"landmarks", (void*)pEyeMarks);

	free(pNV21);
	AndroidBitmap_unlockPixels(env, bmp);
	env->ReleaseIntArrayElements(marks, pMarks, 0);
	env->ReleaseIntArrayElements(eyeMarks, pEyeMarks, 0);
	LOGI("FacialMarksTrack figure ----->");
}


JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1update(
		JNIEnv * env, jobject obj, jint handle, jobject bmp, jobject faceRect, jobject eye1, jobject eye2, jobject mouth, jintArray marks) {
	THandle hdl = (THandle)handle;
	AndroidBitmapInfo info;
	void* pPixels;
	LOGI("FacialMarksTrack update <-----");
	TRECT rect;
	jRect jrect(env, faceRect);
	rect.left = jrect.getLeft();
	rect.top = jrect.getTop();
	rect.right = jrect.getRight();
	rect.bottom = jrect.getBottom();

	int* pMarks = env->GetIntArrayElements(marks, 0);

	ASSERT(AndroidBitmap_getInfo(env, bmp, &info)
			== ANDROID_BITMAP_RESUT_SUCCESS);
	LOGI("width = %d, height = %d, stride = %d", info.width, info.height,
			info.stride);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels)
			== ANDROID_BITMAP_RESUT_SUCCESS);

	TUInt8* pNV21 = (TUInt8*) malloc((info.width * info.height * 3 + 1) / 2);
//	LOGI("pPixels=0x%08X  pNV21=0x%08X", pPixels, pNV21);
	long t = clock();
	RGBA8888_to_NV21((TUInt8*) pPixels, pNV21, info.width, info.height,
			info.stride);
//	LOGI("NV21 cost %d",(clock()-t));
	TSOFFSCREEN os;
	os.u32PixelArrayFormat = TS_PAF_NV21;
	os.i32Width = info.width;
	os.i32Height = info.height;
	os.ppu8Plane[0] = (TUInt8*) pNV21;
	os.ppu8Plane[1] = (TUInt8*) pNV21 + info.width * info.height;
	os.ppu8Plane[2] = os.ppu8Plane[3] = 0;
	os.pi32Pitch[0] = info.width;
	os.pi32Pitch[1] = info.width;

	t = clock();
	TPOINT pEye1, pEye2, pMouth;
	jPoint jpoint(env, eye1);
	pEye1.x = jpoint.getX();
	pEye1.y = jpoint.getY();
	jPoint jpoint2(env, eye2);
	pEye2.x = jpoint2.getX();
	pEye2.y = jpoint2.getY();
	jPoint jpoint3(env, mouth);
	pMouth.x = jpoint3.getX();
	pMouth.y = jpoint3.getY();
	int errorCode = sakFacialTrack_figure_ex(hdl, &os, rect, pEye1, pEye2, pMouth, 0);
	if(errorCode!=0) {
		LOGE("sakFacialTrack_figure_ex failed: %d", errorCode);
	}
	sakFacialTrack_setProperty(hdl, (const TPChar)"landmark-type", "default");
	sakFacialTrack_getProperty(hdl, (const TPChar)"landmarks", (void*)pMarks);

	free(pNV21);
	AndroidBitmap_unlockPixels(env, bmp);
	env->ReleaseIntArrayElements(marks, pMarks, 0);
	LOGI("FacialMarksTrack update ----->");
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FacialMarksTrack_native_1reset(
		JNIEnv * env, jobject obj, jint handle) {
	THandle hdl = (THandle)handle;
	sakFacialTrack_reset(hdl);
}
