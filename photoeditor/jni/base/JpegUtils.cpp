#include<stdio.h> 
#include<stdlib.h> 
#include<jni.h>
#include<android/log.h>
#include <android/bitmap.h>
#include <time.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include "BGRNV21.h"
#include "debug.h"
#include "jPoint.h"
#include "image-util.h"
#include "tmem.h"

extern "C" {
JNIEXPORT jboolean JNICALL Java_com_edmodo_cropper_CropUtil_crop(JNIEnv* env, jobject obj, jstring file);
JNIEXPORT jboolean JNICALL Java_com_edmodo_cropper_CropUtil_cropusebytes___3BIIIILjava_lang_String_2(JNIEnv* env, jobject obj, jbyteArray jpg, jint left,
		jint top, jint right, jint bottom, jstring rspath);
JNIEXPORT jbyteArray JNICALL Java_com_edmodo_cropper_CropUtil_cropusebytes___3BIIIII(JNIEnv* env, jobject obj, jbyteArray jpg, jint left,
		jint top, jint right, jint bottom, jint rotation);
}
;

long gettime() {
	struct timeval time;
	gettimeofday(&time, NULL);
	return (time.tv_sec * 1000 + time.tv_usec / 1000);
}

//remove thumbnail in exif
//thumbnail format: FF D8 ......FF D9
void removeThumbnailInExif(TUInt8* jpgBufUInt, int* pExifLen, int* pResLen) {
	int thumstart = 0;
	int thumend = 0;
	for (int i = 2; i < *pExifLen + 4; i++) {
		if (jpgBufUInt[i] == 0xFF) {
			if (jpgBufUInt[i + 1] == 0xD8) {
				thumstart = i;
			} else if (jpgBufUInt[i + 1] == 0xD9) {
				thumend = i;
				break;
			}
		} else {
			continue;
		}
	}
	LOGI("removeThumbnailInExif thumstart = %d thumend = %d", thumstart,
			thumend);
	if (thumstart != 0 && thumend != 0 && thumstart < thumend) {
		if (jpgBufUInt[thumstart + 1] == 0xD8) {
			jpgBufUInt[thumstart + 1] = 0xF8;
		}
	}
}

jbyteArray glReadPixelsToJpeg(JNIEnv * env, jobject obj, jint width,
		jint height, jbyteArray jpg) {
	jbyte *jpgBuf = env->GetByteArrayElements(jpg, 0);
	TUInt8* jpgBufUInt = (TUInt8*) jpgBuf;
	int jpgBufLen = env->GetArrayLength(jpg);
	char* rgbaBuf = (char*) malloc(width * height * 4);
	glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, rgbaBuf);
	LOGI("step1:jpgBufLen=%d",jpgBufLen);
	dumpToFile("/sdcard/zhl_3.rgb",(unsigned char*)rgbaBuf, width*height*4);
	RGBA8888_to_RGB888((TUInt8*) rgbaBuf, (TUInt8*) rgbaBuf, width, height);
	dumpToFile("/sdcard/zhl_5.rgb",(unsigned char*)rgbaBuf, width*height*3);
	Ip_Image img;
	img.imageData = rgbaBuf;
	img.width = width;
	img.height = height;
	img.widthStep = width * 3;
	int dstLen;
	char* dstBuf = imageUtil::cvEncodeJpegBuffer(&img, dstLen);
//	dumpToFile("/sdcard/zhl_4.jpg",(unsigned char*)dstBuf, dstLen);
	int exifLen = ((jpgBuf[4] & 0xFF) << 8) | (jpgBuf[5] & 0xFF);
	jbyteArray res;
	int resLen = dstLen + exifLen + 2;
	LOGI("zhl resLen=%d jpgLen=%d, exifLen=%d dstLen=%d", resLen, jpgBufLen,
			exifLen, dstLen);

	removeThumbnailInExif(jpgBufUInt, &exifLen, &resLen);

	if (resLen <= jpgBufLen) {
		memcpy(jpgBuf + exifLen + 4, dstBuf + 2, dstLen - 2);
		res = jpg;
	} else {
		res = env->NewByteArray(resLen);
		env->SetByteArrayRegion(res, 0, exifLen + 4, (jbyte*) jpgBuf);
		env->SetByteArrayRegion(res, exifLen + 4, dstLen - 2,
				(jbyte*) dstBuf + 2);
	}
	free(rgbaBuf);
	free(dstBuf);
	env->ReleaseByteArrayElements(jpg, jpgBuf, 0);
	return res;
}

JNIEXPORT jboolean JNICALL Java_com_edmodo_cropper_CropUtil_crop(JNIEnv* env, jobject obj, jstring file){
	const char* str;
	LOGI("decode jpeg step1");
	str = env->GetStringUTFChars(file, false);
	if(str == NULL)
		return false;
	LOGI("decode jpeg path=%s",str);
	Ip_Image* img = imageUtil::loadJpegFile(str);
	if(img == NULL){
		LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
		return false;
	}
	LOGI("decode jpeg width=%d,height=%d,widthStep=%d,depth=%d",img->width,img->height,img->widthStep,img->depth);
	env->ReleaseStringUTFChars(file,str);
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_edmodo_cropper_CropUtil_cropusebytes___3BIIIILjava_lang_String_2(JNIEnv* env, jobject obj, jbyteArray jpg, jint left,
		jint top, jint right, jint bottom, jstring rspath){
	LOGI("decode jpeg step1");
	jbyte* jpgbuff = env->GetByteArrayElements(jpg, 0);
	int jpgbuffLen = env->GetArrayLength(jpg);
	LOGI("decode jpeg jpgbuffLen=%d",jpgbuffLen);
	Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*)jpgbuff, jpgbuffLen);

	if(img == NULL){
			LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
			return false;
		}
	LOGI("decode jpeg width=%d,height=%d,widthStep=%d,depth=%d",img->width,img->height,img->widthStep,img->depth);
	env->ReleaseByteArrayElements(jpg, jpgbuff, 0);
	char* previousData = img->imageData;
	int oldWidth = img->width;
	int newWidth = right-left,newHeight = bottom-top;
	LOGI("decode jpeg newWidth=%d,newHeight=%d",newWidth,newHeight);
	char* newJpegPixels = (char*)malloc(newWidth*newHeight*3);
	char* whereToGet = previousData + (top*oldWidth+left)*3;
	char* whereToPut = newJpegPixels;
	LOGI("crop jpeg before");
	for(int y = top; y < bottom; y++){
		memcpy(whereToPut, whereToGet, 3*newWidth);
		whereToPut +=3*newWidth;
		whereToGet +=3*oldWidth;
	}
	LOGI("crop jpeg after");
	Ip_Image img2;
	img2.imageData = newJpegPixels;
	img2.width = newWidth;
	img2.height = newHeight;
	img2.widthStep = newWidth * 3;
	int dstLen;
	LOGI("encode jpeg");
	char* dstBuf = imageUtil::cvEncodeJpegBuffer(&img2, dstLen);
	const char* rs;
	rs = env->GetStringUTFChars(rspath, false);
	LOGI("encode jpeg path=%s",rs);
	dumpToFile(rs,(unsigned char*)dstBuf, dstLen);
	env->ReleaseStringUTFChars(rspath,rs);
	imageprocess::ipReleaseImage(&img);
	free(newJpegPixels);
	free(dstBuf);
	return true;
}

JNIEXPORT jbyteArray JNICALL Java_com_edmodo_cropper_CropUtil_cropusebytes___3BIIIII(JNIEnv* env, jobject obj, jbyteArray jpg, jint left,
		jint top, jint right, jint bottom, jint rotation){
	LOGI("decode jpeg step1 %d, %d, %d, %d", left, top, right, bottom);
	jbyte* jpgbuff = env->GetByteArrayElements(jpg, 0);
	int jpgbuffLen = env->GetArrayLength(jpg);
	LOGI("decode jpeg jpgbuffLen=%d",jpgbuffLen);
	Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*)jpgbuff, jpgbuffLen);
	if(rotation) {
	    Ip_Image* tmp = img;
	    img = imageUtil::RotateImage(tmp, rotation);
	    ipReleaseImage(&tmp);
	}
	if(img == NULL){
			LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
			return NULL;
		}
	LOGI("decode jpeg width=%d,height=%d,widthStep=%d,depth=%d, chl=%d",img->width,img->height,img->widthStep,img->depth, img->nChannels);
	env->ReleaseByteArrayElements(jpg, jpgbuff, 0);
	LOGI("crop jpeg before");
	if(left<0) left = 0;
	if(top<0) top = 0;
	if(right>img->width) right = img->width;
	if(bottom>img->height) bottom = img->height;
	Ip_Image img2;
	memcpy(&img2, img, sizeof(Ip_Image));
	img2.width = right - left;
	img2.height = bottom - top;
	img2.widthStep = img->widthStep;
	img2.imageData = img->imageData + top*img->widthStep + left*img->nChannels;
	int dstLen;
	LOGI("encode jpeg before");
	char* dstBuf = imageUtil::cvEncodeJpegBuffer(&img2, dstLen);
	LOGI("encode jpeg after");
	jbyteArray res = env->NewByteArray(dstLen);
	env->SetByteArrayRegion(res, 0, dstLen, (jbyte*) dstBuf);
	imageprocess::ipReleaseImage(&img);
	free(dstBuf);
	return res;
}

