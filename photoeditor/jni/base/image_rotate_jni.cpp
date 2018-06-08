#include <stdlib.h>
#include <jni.h>
#include "debug.h"
#include "image-util.h"
#include "com_cam001_util_ImageUtil.h"

void image_rotate_mirror_x(jbyte* inBuf, int width, int height, int depth, jbyte* outBuf) {
    int w, h;
    int inIndex, outIndex;
    for(h=0; h<height; h++) {
        for(w=0; w<width; w++) {
            outIndex = h*width+w;
            inIndex = h*width+width-1-w;
            outIndex *= depth;
            inIndex *= depth;
            memcpy(outBuf+outIndex, inBuf+inIndex, depth);
        }
    }
}


void image_rotate_mirror_y(jbyte* inBuf, int width, int height, int depth, jbyte* outBuf) {
    int w, h;
    int inIndex, outIndex;
    for(h=0; h<height; h++) {
        for(w=0; w<width; w++) {
            outIndex = h*width+w;
            inIndex = (height-h-1)*width + w;
            outIndex *= depth;
            inIndex *= depth;
            memcpy(outBuf+outIndex, inBuf+inIndex, depth);
        }
    }
}

JNIEXPORT jbyteArray JNICALL Java_com_cam001_util_ImageUtil_native_1rotate
(JNIEnv * env, jclass cls, jbyteArray jpeg, jint rotate, jboolean mirrorX, jboolean mirrorY) {
	jbyte* jpgBuff = env->GetByteArrayElements(jpeg, 0);
    int jpgbuffLen = env->GetArrayLength(jpeg);
    LOGI("decode jpeg jpgbuffLen=%d",jpgbuffLen);
    Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*)jpgBuff, jpgbuffLen);
    if(img == NULL){
        LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
        return NULL;
    }
    LOGI("decode jpeg width=%d,height=%d,widthStep=%d,depth=%d, chl=%d",img->width,img->height,img->widthStep,img->depth, img->nChannels);
    env->ReleaseByteArrayElements(jpeg, jpgBuff, 0);
    
    if(rotate) {
        Ip_Image* tmp = img;
        img = imageUtil::RotateImage(tmp, rotate);
        ipReleaseImage(&tmp);
    }
    if(mirrorX) {
        char* buf = (char*)malloc(img->width*img->height*img->nChannels);
        image_rotate_mirror_x(img->imageData, img->width, img->height, img->nChannels, buf);
        free(img->imageData);
        img->imageData = buf;
    }
    if(mirrorY) {
        char* buf = (char*)malloc(img->width*img->height*img->nChannels);
        image_rotate_mirror_y(img->imageData, img->width, img->height, img->nChannels, buf);
        free(img->imageData);
        img->imageData = buf;
    }
    
    int dstLen;
    LOGI("encode jpeg before");
    char* dstBuf = imageUtil::cvEncodeJpegBuffer(img, dstLen);
    LOGI("encode jpeg after");
    jbyteArray res = env->NewByteArray(dstLen);
    env->SetByteArrayRegion(res, 0, dstLen, (jbyte*) dstBuf);
    imageprocess::ipReleaseImage(&img);
    free(dstBuf);
    return res;
}

#define MIN(a,b) ((a)<(b))?(a):(b)
#define MAX(a,b) ((a)>(b))?(a):(b)
#define CLAMP(v, min, max) MIN(MAX((v),(min)), (max))

JNIEXPORT jbyteArray JNICALL Java_com_cam001_util_ImageUtil_native_1enhance
(JNIEnv * env, jclass cls, jbyteArray jpeg, jfloatArray colorMatrix) {
    jbyte* jpgBuff = env->GetByteArrayElements(jpeg, 0);
    int jpgbuffLen = env->GetArrayLength(jpeg);
    LOGI("decode jpeg jpgbuffLen=%d",jpgbuffLen);
    Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*)jpgBuff, jpgbuffLen);
    if(img == NULL){
        LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
        return NULL;
    }
    LOGI("decode jpeg width=%d,height=%d,widthStep=%d,depth=%d, chl=%d",img->width,img->height,img->widthStep,img->depth, img->nChannels);
    env->ReleaseByteArrayElements(jpeg, jpgBuff, 0);
    
    jfloat* mat = env->GetFloatArrayElements(colorMatrix, 0);
    int x = 0;
    int y = 0;
    unsigned char* pImg = img->imageData;
    int r,g,b;
    while(y<img->height) {
        unsigned char* line = pImg;
        x = 0;
        while(x<img->width) {
            r = (int)(line[0]*mat[0]+line[1]*mat[1]+line[2]*mat[2]+mat[3]+mat[4]);
            g = (int)(line[0]*mat[5]+line[1]*mat[6]+line[2]*mat[7]+mat[8]+mat[9]);
            b = (int)(line[0]*mat[10]+line[1]*mat[11]+line[2]*mat[12]+mat[13]+mat[14]);
            line[0] = (unsigned char)CLAMP(r, 0, 255);
            line[1] = (unsigned char)CLAMP(g, 0, 255);
            line[2] = (unsigned char)CLAMP(b, 0, 255);
            line += img->nChannels;
            x ++;
        }
        pImg += img->widthStep;
        y ++;
    }
    env->ReleaseFloatArrayElements(colorMatrix, mat, 0);
    
    int dstLen;
    LOGI("encode jpeg before");
    char* dstBuf = imageUtil::cvEncodeJpegBuffer(img, dstLen);
    LOGI("encode jpeg after");
    jbyteArray res = env->NewByteArray(dstLen);
    env->SetByteArrayRegion(res, 0, dstLen, (jbyte*) dstBuf);
    imageprocess::ipReleaseImage(&img);
    free(dstBuf);
    return res;
}

JNIEXPORT jbyteArray JNICALL Java_com_cam001_util_ImageUtil_native_1crop
(JNIEnv * env, jclass cls, jbyteArray jpeg, jint left, jint top, jint right, jint bottom) {
	    jbyte* jpgBuff = env->GetByteArrayElements(jpeg, 0);
    int jpgbuffLen = env->GetArrayLength(jpeg);
    LOGI("decode jpeg jpgbuffLen=%d",jpgbuffLen);
    Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*)jpgBuff, jpgbuffLen);
    if(img == NULL){
        LOGI("imageUtil::loadJpegFile(str) failed at %d", __LINE__);
        return NULL;
    }
    LOGI("decode jpeg width=%d,height=%d,widthStep=%d,depth=%d, chl=%d",img->width,img->height,img->widthStep,img->depth, img->nChannels);
    env->ReleaseByteArrayElements(jpeg, jpgBuff, 0);
    
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
  
  