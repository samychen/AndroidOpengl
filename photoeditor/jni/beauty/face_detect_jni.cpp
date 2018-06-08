#include "face_detect_jni.h"
#include <android/bitmap.h>
#include "utils/debug.h"
#include "utils/BGRNV21.h"
#include "jRect.h"
#include "tmem.h"
#include "ts-detect-object.h"
#include <time.h>
#include <stdlib.h>
#define printRect(rect) LOGI("%d,%d,%dx%d", (int)(rect).left, (int)(rect).top, (int)((rect).right-(rect).left+1), (int)((rect).bottom-(rect).top+1))

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1create(
		JNIEnv * env, jobject obj) {
	LOGI("FaceDetect native_create <-----");
	THandle fd_handle = sakDetectObject_create();
	LOGI("FaceDetect native_create ----->");
	return (jint) fd_handle;
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1destroy(JNIEnv * env, jobject obj, jint handle) {
	LOGI("FaceDetect native_destory <-----");
	THandle fd_handle = (THandle)handle;
	sakDetectObject_destroy(fd_handle);
	LOGI("FaceDetect native_destory ----->");
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1detect__I_3BII(
		JNIEnv * env, jobject obj, jint handle, jbyteArray nv21, jint width,
		jint height) {
	THandle fd_handle = (THandle) handle;
	//	TUInt8* buf = (TUInt8*)(env->GetPrimitiveArrayCritical(nv21, 0));
	jbyte* buf = env->GetByteArrayElements(nv21, 0);
	TSOFFSCREEN os;
	os.u32PixelArrayFormat = TS_PAF_NV21;
	os.i32Width = width;
	os.i32Height = height;
	os.ppu8Plane[0] = (TUInt8*) buf;
	os.ppu8Plane[1] = (TUInt8*) buf + width * height;
	os.ppu8Plane[2] = os.ppu8Plane[3] = 0;
	os.pi32Pitch[0] = width;
	os.pi32Pitch[1] = width;
	sakDetectObject_setImage(fd_handle, &os);
	jint count = sakDetectObject_detect(fd_handle, (char*) "face", 0);
	env->ReleaseByteArrayElements(nv21, buf, 0);
	return count;
	//  env->ReleasePrimitiveArrayCritical(nv21, buf, 0);
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1detect__ILandroid_graphics_Bitmap_2(
		JNIEnv * env, jobject obj, jint handle, jobject bmp) {
	AndroidBitmapInfo info;
	void* pPixels;
	THandle fd_handle = (THandle) handle;

	ASSERT(AndroidBitmap_getInfo(env, bmp, &info)
			== ANDROID_BITMAP_RESUT_SUCCESS);
	LOGI("width = %d, height = %d, stride = %d", info.width, info.height,
			info.stride);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels)
			== ANDROID_BITMAP_RESUT_SUCCESS);

	TUInt8* pNV21 = (TUInt8*) malloc((info.width * info.height * 3 + 1) / 2);
//	LOGI("pPixels=0x%08X  pNV21=0x%08X", pPixels, pNV21);
	RGBA8888_to_NV21((TUInt8*) pPixels, pNV21, info.width, info.height,
			info.stride);
	TSOFFSCREEN os;
	os.u32PixelArrayFormat = TS_PAF_NV21;
	os.i32Width = info.width;
	os.i32Height = info.height;
	os.ppu8Plane[0] = (TUInt8*) pNV21;
	os.ppu8Plane[1] = (TUInt8*) pNV21 + info.width * info.height;
	os.ppu8Plane[2] = os.ppu8Plane[3] = 0;
	os.pi32Pitch[0] = info.width;
	os.pi32Pitch[1] = info.width;
	long t = clock();
	sakDetectObject_setImage(fd_handle, &os);
//	LOGI("Detect setImage cost: %d",(clock()-t));
	t = clock();
	jint count = sakDetectObject_detect(fd_handle, (char*) "face", 0);
//	LOGI("Detect face cost: %d",(clock()-t));
	free(pNV21);
	AndroidBitmap_unlockPixels(env, bmp);
	return count;
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1count(
		JNIEnv * env, jobject obj, jint handle) {
	THandle fd_handle = (THandle) handle;
	return 0;
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1face_1info(
		JNIEnv * env, jobject obj, jint handle, jint index, jobject face,
		jobject eye1, jobject eye2, jobject mouth) {
	THandle fd_handle = (THandle) handle;
	TRECT rect,facerect;
	sakDetectObject_object(fd_handle, index, &facerect);
	jRect r(env, face);
	r.setLeft(facerect.left);
	r.setTop(facerect.top);
	r.setRight(facerect.right);
	r.setBottom(facerect.bottom);

	if(eye1 != NULL || eye2 != NULL) {
		long t = clock();
		jint eyeCount = sakDetectObject_detect(fd_handle, (char*) "eye", &facerect);
//		LOGI("Detect eye count:%d cost: %d",eyeCount,(clock()-t));
		if (eye1 != NULL && eyeCount > 0) {
			sakDetectObject_object(fd_handle, 0, &rect);
			jRect r(env, eye1);
			r.setLeft(rect.left);
			r.setTop(rect.top);
			r.setRight(rect.right);
			r.setBottom(rect.bottom);
		}
		if (eye2 != NULL && eyeCount > 1) {
			sakDetectObject_object(fd_handle, 1, &rect);
			jRect r(env, eye2);
			r.setLeft(rect.left);
			r.setTop(rect.top);
			r.setRight(rect.right);
			r.setBottom(rect.bottom);
		}
	}
	if(mouth != NULL) {
		long t = clock();
		jint mouthCount = sakDetectObject_detect(fd_handle, (char*) "mouth", &facerect);
//		LOGI("Detect mouth count:%d cost: %d",mouthCount,(clock()-t));
		if (mouthCount > 0) {
			sakDetectObject_object(fd_handle, 0, &rect);
			jRect r(env, mouth);
			r.setLeft(rect.left);
			r.setTop(rect.top);
			r.setRight(rect.right);
			r.setBottom(rect.bottom);
		}
	}

}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_FaceDetect_native_1hair_1info(
		JNIEnv * env, jobject obj, jint handle, jobject hair1, jobject hair2, jobject hair3) {
	THandle fd_handle = (THandle) handle;
	int cou=0;
	TRECT* pRect=0;
//	pRect=sakDetectObject_getHairRects(fd_handle, &cou);
//	LOGI("Count=%d, rect=%d", cou, pRect);
	ASSERT(cou==3);
	jRect r1(env, hair1);
	r1.setLeft(pRect[0].left);
	r1.setTop(pRect[0].top);
	r1.setRight(pRect[0].right);
	r1.setBottom(pRect[0].bottom);
	jRect r2(env, hair2);
	r2.setLeft(pRect[1].left);
	r2.setTop(pRect[1].top);
	r2.setRight(pRect[1].right);
	r2.setBottom(pRect[1].bottom);
	jRect r3(env, hair3);
	r3.setLeft(pRect[2].left);
	r3.setTop(pRect[2].top);
	r3.setRight(pRect[2].right);
	r3.setBottom(pRect[2].bottom);
}

