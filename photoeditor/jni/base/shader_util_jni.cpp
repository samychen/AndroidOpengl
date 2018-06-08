#include<stdio.h> 
#include<stdlib.h> 
#include<jni.h>
#include<android/log.h>
#include <android/bitmap.h>
#include <time.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "image-util.h"

#include "debug.h"
#include "zoom.h"
#include "jPoint.h"

extern "C" {
	JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glReadPixelsToBitmap(JNIEnv * env, jobject obj, jobject bmp);
	JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glTexImage2DJpeg(JNIEnv * env, jobject obj, jbyteArray jpg, jobject size);
	JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glReadPixelsToFile(JNIEnv * env, jobject obj, jobject warterMark, jint outW, jint outH, jstring path);
    JNIEXPORT jbyteArray JNICALL Java_com_cam001_gles_ShaderUtil_glReadPixelsToJpeg(JNIEnv * env, jobject obj, jint width, jint height, jbyteArray buf);
	JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glTexImage2DBitmap(JNIEnv * env, jobject obj, jobject bmp);
	JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glTexImage2D(JNIEnv * env, jobject obj, jbyteArray data, jint start, jint format, jint width, jint height);
};


JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glReadPixelsToBitmap(JNIEnv * env, jobject obj, jobject bmp) {
	AndroidBitmapInfo  info;
	void *pPixels;

	ASSERT(AndroidBitmap_getInfo(env, bmp, &info) == ANDROID_BITMAP_RESUT_SUCCESS);
	LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels) == ANDROID_BITMAP_RESUT_SUCCESS);

	glReadPixels(0, 0, info.width, info.height, GL_RGBA, GL_UNSIGNED_BYTE, pPixels);

	AndroidBitmap_unlockPixels(env, bmp);
}

void resizeImageLimitTex(Ip_Image* img) {
    LOGI("resizeImageLimitTex <-----");
    int maxTextureSize;
    glGetIntegerv(GL_MAX_TEXTURE_SIZE, &maxTextureSize);
    int maxLen = img->width>img->height?img->width:img->height;
    if(maxLen>maxTextureSize) {
        int sample = (maxLen+maxTextureSize-1)/maxTextureSize;
        int newW = img->width/sample;
        int newH = img->height/sample;
        int newStride = (newW*3+3)/4*4;
        char* newData = (char*)malloc(newStride*newH);
        char* newLine = newData;
        char* oldLine = img->imageData;
        int rowNum = 0;
        int colNum = 0;
        char* newPix, *oldPix;
        LOGI("new size:%dx%d, stride=%d, sample=%d", newW, newH, newStride, sample);
        while(rowNum<newH) {
            colNum = 0;
            newPix = newLine;
            oldPix = oldLine;
            while(colNum<newW) {
                memcpy(newPix, oldPix, 3);
                newPix += 3;
                oldPix += 3*sample;
                colNum ++;
            }
            newLine += newStride;
            oldLine += img->widthStep*sample;
            rowNum ++;
        }
        free(img->imageData);
        img->imageData = newData;
        img->widthStep = newStride;
        img->width = newW;
        img->height = newH;
    }
    LOGI("resizeImageLimitTex ----->");
}

void resizeImageAlaign4(Ip_Image* img) {
    if(img->widthStep%4) {
        int newStride = (img->widthStep/4+1)*4;
        char* newData = (char*) malloc(newStride*img->height);
        char* lineOld = img->imageData;
        char* lineNew = newData;
        int rowNum = 0;
        while(rowNum<img->height) {
            memcpy(lineNew, lineOld, img->widthStep);
            lineOld += img->widthStep;
            lineNew += newStride;
            rowNum ++;
        }
        free(img->imageData);
        img->imageData = newData;
        img->widthStep = newStride;
    }
}

JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glTexImage2DJpeg(JNIEnv * env, jobject obj, jbyteArray jpg, jobject size) {
    LOGI("glTexImage2DJpeg <-----");
	jbyte* jpgbuff = env->GetByteArrayElements(jpg, 0);
	int jpgbuffLen = env->GetArrayLength(jpg);
	Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*)jpgbuff, jpgbuffLen);
	resizeImageLimitTex(img);
	resizeImageAlaign4(img);
	if(img == NULL){
			LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
			return;
		}
	LOGI("glTexImage2DJpeg width=%d,height=%d,widthStep=%d,depth=%d",img->width,img->height,img->widthStep,img->depth);
	env->ReleaseByteArrayElements(jpg, jpgbuff, 0);
	int format = GL_RGB;
	if(img->nChannels==1) {
	    format = GL_LUMINANCE;
	}
    glTexImage2D(GL_TEXTURE_2D, 0, format, img->width, img->height, 0, format, GL_UNSIGNED_BYTE, img->imageData);

    jPoint jSize(env, size);
    jSize.setX(img->width);
    jSize.setY(img->height);

    ipReleaseImage(&img);
    LOGI("glTexImage2DJpeg ----->");
}

void addWarterMark(char* pImage, int width, int height, int stride, JNIEnv * env, jobject warterMark) {
    if(!warterMark) return;
	AndroidBitmapInfo info;
	void* pMark;
	ASSERT(AndroidBitmap_getInfo(env, warterMark, &info)==ANDROID_BITMAP_RESUT_SUCCESS);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, warterMark, &pMark)== ANDROID_BITMAP_RESUT_SUCCESS);
	int markW = info.width;
	int markH = info.height;
	int margin = (width+height)/100;
	int dstH = (width+height)/60;
	int dstW = dstH*markW/markH;
	int dstX = width - dstW - margin;
	int dstY = height - dstH - margin;
	char* pDstMark = (char*) malloc(dstW*dstH*4);
    zoomRGBA((char*)pMark, markW, markH, info.stride, pDstMark, dstW, dstH);

    char* lineImg = pImage + dstY*stride + dstX*3;
    char* lineMark = pDstMark;
    int rowNum = 0;
    int r, g, b, a;
    while(rowNum<dstH) {
        int colNum = 0;
        char* pixImg = lineImg;
        char* pixMark = lineMark;
        while(colNum<dstW) {
            a = pixMark[3];
            if(a>0) {
                r = (pixImg[0]*(255-a) + pixMark[0]*a)>>8;
                g = (pixImg[1]*(255-a) + pixMark[1]*a)>>8;
                b = (pixImg[2]*(255-a) + pixMark[2]*a)>>8;
                pixImg[0] = r>255?255:r;
                pixImg[1] = g>255?255:g;
                pixImg[2] = b>255?255:b;
            }
            colNum ++;
            pixImg += 3;
            pixMark += 4;
        }
        rowNum ++;
        lineImg += stride;
        lineMark += dstW*4;
    }

    free(pDstMark);
	AndroidBitmap_unlockPixels(env, warterMark);
}

void RGBA2RGB(char* pImage, int width, int height, int stride) {
    char* line = pImage;
    int rowNum = 0;
    while(rowNum<height) {
        int colNum = 0;
        char* pixRGBA = line;
        char* pixRGB = line;
        while(colNum<width) {
            memcpy(pixRGB, pixRGBA, 3);
            pixRGB += 3;
            pixRGBA += 4;
            colNum ++;
        }
        line += stride;
        rowNum++;
    }
}

JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glReadPixelsToFile(JNIEnv * env, jobject obj,
jobject warterMark, jint outW, jint outH, jstring path) {
	const char* szPath = env->GetStringUTFChars(path, 0);

	char* pImage = (char*)malloc(outW*outH*4);
	glReadPixels(0, 0, outW, outH, GL_RGBA, GL_UNSIGNED_BYTE, pImage);
	RGBA2RGB(pImage, outW, outH, outW*4);
	Ip_Image img;
    img.imageData = pImage;
    img.width = outW;
    img.height = outH;
    img.widthStep = img.width * 4;
    img.nChannels = 3;
    addWarterMark(pImage, outW, outH, outW*4, env, warterMark);
    int dstLen;
    char* dstBuf = imageUtil::cvEncodeJpegBuffer(&img, dstLen);
    dumpToFile(szPath, (unsigned char*)dstBuf, dstLen);
    free(dstBuf);

    free(pImage);
    env->ReleaseStringUTFChars(path, szPath);
}

JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glTexImage2DBitmap(
    JNIEnv * env, jobject obj, jobject bmp) {
    AndroidBitmapInfo  info;
    void *pPixels;

    ASSERT(AndroidBitmap_getInfo(env, bmp, &info) == ANDROID_BITMAP_RESUT_SUCCESS);
    LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);
    ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
    ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels) == ANDROID_BITMAP_RESUT_SUCCESS);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,info.width, info.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pPixels);

    AndroidBitmap_unlockPixels(env, bmp);
}


JNIEXPORT void JNICALL Java_com_cam001_gles_ShaderUtil_glTexImage2D(
		JNIEnv * env, jobject obj, jbyteArray data, jint start, jint format, jint width, jint height) {
	jbyte* buf = env->GetByteArrayElements(data, 0);
	glTexImage2D(GL_TEXTURE_2D, 0, format,width, height, 0, format, GL_UNSIGNED_BYTE, buf+start);
	env->ReleaseByteArrayElements(data, buf, 0);
}

JNIEXPORT JNIEXPORT jbyteArray JNICALL Java_com_cam001_gles_ShaderUtil_glReadPixelsToJpeg
    (JNIEnv * env, jobject obj, jint outW, jint outH, jbyteArray buf) {
    jbyte* jpgBuff = env->GetByteArrayElements(buf, 0);
    int jpgbuffLen = env->GetArrayLength(buf);
        
    char* pImage = (char*)malloc(outW*outH*4);
    glReadPixels(0, 0, outW, outH, GL_RGBA, GL_UNSIGNED_BYTE, pImage);
    RGBA2RGB(pImage, outW, outH, outW*4);
    Ip_Image img;
    img.imageData = pImage;
    img.width = outW;
    img.height = outH;
    img.widthStep = img.width * 4;
    img.nChannels = 3;
    
    int dstLen;
    LOGI("encode jpeg before");
    char* dstBuf = imageUtil::cvEncodeJpegBuffer(&img, dstLen);
    LOGI("encode jpeg after");
    jbyteArray res = env->NewByteArray(dstLen);
    env->SetByteArrayRegion(res, 0, dstLen, (jbyte*) dstBuf);
    free(pImage);
    free(dstBuf);
    env->ReleaseByteArrayElements(buf, jpgBuff, 0);
    return res;
}
