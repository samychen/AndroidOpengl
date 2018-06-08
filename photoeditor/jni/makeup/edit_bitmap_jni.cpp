#include "edit_bitmap_jni.h"
#include <stdlib.h>
#include <android/bitmap.h>
#include "utils/debug.h"

typedef struct edit_bitmap {
	jobject bitmap;
	//Indicate the bitmap is original or not.
	//If the bitmap is original, the pPixels stores the edited bitmap pixels.
	//If the bitmap is NOT original, the pPixels stores the original bitmap pixels.
	jboolean bOriginal;
	void* pPixels;
	void* pSaveCache;
	int width;
	int height;
	int stride;
} EditBitmap;

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1create(
		JNIEnv * env, jobject obj, jobject bmp) {
	AndroidBitmapInfo  info;
	void* pPixels;
	int pixLen;
	EditBitmap* pEditBitmap;

	LOGI("EditBitmap create <-----");
   	ASSERT(AndroidBitmap_getInfo(env, bmp, &info) == ANDROID_BITMAP_RESUT_SUCCESS);
	LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels) == ANDROID_BITMAP_RESUT_SUCCESS);

	pixLen = info.stride*info.height;

	pEditBitmap = (EditBitmap*)malloc(sizeof(EditBitmap));
	pEditBitmap->bitmap = (jobject)(env->NewGlobalRef(bmp));
	pEditBitmap->bOriginal = JNI_FALSE;
	pEditBitmap->width = info.width;
	pEditBitmap->height = info.height;
	pEditBitmap->stride = info.stride;
	pEditBitmap->pSaveCache = 0;
	pEditBitmap->pPixels = malloc(pixLen);
	memcpy(pEditBitmap->pPixels, pPixels, pixLen);

	AndroidBitmap_unlockPixels(env, bmp);
	LOGI("EditBitmap create ----->");
	return (jint)pEditBitmap;
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1destroy(
		JNIEnv * env, jobject obj, jint handle) {
	LOGI("EditBitmap destroy <-----");
	EditBitmap* pEditBitmap = (EditBitmap*)handle;
	env->DeleteGlobalRef(pEditBitmap->bitmap);
	free(pEditBitmap->pPixels);
	if(pEditBitmap->pSaveCache) {
		free(pEditBitmap->pSaveCache);
	}
	free(pEditBitmap);
	LOGI("EditBitmap destroy ----->");
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1reset(
		JNIEnv * env, jobject obj, jint handle) {
	AndroidBitmapInfo  info;
	void* pBmpPixels;
	int pixLen;
	EditBitmap* pEditBitmap;
	LOGI("EditBitmap reset <-----");
	//Set original false first.
	Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1original(env, obj, handle, JNI_FALSE);
	pEditBitmap = (EditBitmap*)handle;
	pixLen = pEditBitmap->stride*pEditBitmap->height;
	ASSERT(AndroidBitmap_lockPixels(env, pEditBitmap->bitmap, &pBmpPixels) == ANDROID_BITMAP_RESUT_SUCCESS);
	memcpy(pBmpPixels, pEditBitmap->pPixels, pixLen);

	AndroidBitmap_unlockPixels(env, pEditBitmap->bitmap);
	LOGI("EditBitmap reset ----->");
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1apply(
		JNIEnv * env, jobject obj, jint handle, jobject bmp) {
	AndroidBitmapInfo  info;
	void *pDstPixels, *pSrcPixels;
	int pixLen;
	EditBitmap* pEditBitmap;

	pEditBitmap = (EditBitmap*)handle;
   	ASSERT(AndroidBitmap_getInfo(env, bmp, &info) == ANDROID_BITMAP_RESUT_SUCCESS);
	LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pSrcPixels) == ANDROID_BITMAP_RESUT_SUCCESS);
	ASSERT(pEditBitmap->width==info.width);
	ASSERT(pEditBitmap->height==info.height);
	ASSERT(pEditBitmap->stride==info.stride);
	ASSERT(AndroidBitmap_lockPixels(env, pEditBitmap->bitmap, &pDstPixels) == ANDROID_BITMAP_RESUT_SUCCESS);

	pixLen = info.stride*info.height;
	memcpy(pDstPixels, pSrcPixels, pixLen);

	AndroidBitmap_unlockPixels(env, bmp);
	AndroidBitmap_unlockPixels(env, pEditBitmap->bitmap);
}
JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1original(
		JNIEnv * env, jobject obj, jint handle, jboolean orig) {
	void *pPixels, *pBmpPixels;
	EditBitmap* pEditBitmap;
	char* pLine;
	int idx;

	pEditBitmap = (EditBitmap*)handle;
	if(pEditBitmap->bOriginal==orig) {
		return;
	}

	ASSERT(AndroidBitmap_lockPixels(env, pEditBitmap->bitmap, &pBmpPixels) == ANDROID_BITMAP_RESUT_SUCCESS);
	pPixels = pEditBitmap->pPixels;
	pLine = (char*)malloc(pEditBitmap->stride);
	for(idx=0; idx<pEditBitmap->height; idx++) {
		memcpy(pLine, pPixels, pEditBitmap->stride);
		memcpy(pPixels, pBmpPixels, pEditBitmap->stride);
		memcpy(pBmpPixels, pLine, pEditBitmap->stride);

		pPixels = (char*)pPixels + pEditBitmap->stride;
		pBmpPixels = (char*)pBmpPixels + pEditBitmap->stride;
	}
	pEditBitmap->bOriginal = orig;

	free(pLine);
	AndroidBitmap_unlockPixels(env, pEditBitmap->bitmap);
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1save(
		JNIEnv * env, jobject obj, jint handle) {
	void* pBmpPixels;
	EditBitmap* pEditBitmap;
	int pixLen;

	pEditBitmap = (EditBitmap*)handle;
	ASSERT(pEditBitmap->pSaveCache==0);

	ASSERT(AndroidBitmap_lockPixels(env, pEditBitmap->bitmap, &pBmpPixels) == ANDROID_BITMAP_RESUT_SUCCESS);
	pixLen = pEditBitmap->stride*pEditBitmap->height;
	pEditBitmap->pSaveCache = malloc(pixLen);
	memcpy(pEditBitmap->pSaveCache, pBmpPixels, pixLen);

	AndroidBitmap_unlockPixels(env, pEditBitmap->bitmap);
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_EditBitmap_native_1restore(
		JNIEnv * env, jobject obj, jint handle) {
	void* pBmpPixels;
	EditBitmap* pEditBitmap;
	int pixLen;

	pEditBitmap = (EditBitmap*)handle;
	ASSERT(pEditBitmap->pSaveCache!=0);

	ASSERT(AndroidBitmap_lockPixels(env, pEditBitmap->bitmap, &pBmpPixels) == ANDROID_BITMAP_RESUT_SUCCESS);
	pixLen = pEditBitmap->stride*pEditBitmap->height;
	memcpy(pBmpPixels, pEditBitmap->pSaveCache, pixLen);
	LOGI("pixLen=%d", pixLen);

	free(pEditBitmap->pSaveCache);
	pEditBitmap->pSaveCache = 0;
	AndroidBitmap_unlockPixels(env, pEditBitmap->bitmap);
}
