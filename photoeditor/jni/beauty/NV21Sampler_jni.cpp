#include "NV21Sampler_jni.h"
#include <stdlib.h>
#include "utils/debug.h"

#define SCALED_BUFFER_SIZE (960*720*3/2)

void nv21_rotate_90(jbyte* inBuf, int width, int height, jbyte* outBuf) {
	int w, h;
	int offsetUV = width*height;
	//Y
	for(h=0; h<width; h++) {
		for(w=0; w<height; w++) {
			outBuf[h*height+w] = inBuf[(height-w)*width+h];
		}
	}
	//UV
	inBuf += offsetUV;
	outBuf += offsetUV;
	int hWidth = width>>1;
	int hHeight = height>>1;
	for(h=0; h<hWidth; h++) {
		for(w=0; w<hHeight; w++) {
			outBuf[h*height+(w<<1)] = inBuf[(hHeight-w)*width+(h<<1)];
			outBuf[h*height+(w<<1)+1] = inBuf[(hHeight-w)*width+(h<<1)+1];
		}
	}
}

void nv21_rotate_270(jbyte* inBuf, int width, int height, jbyte* outBuf) {
	int w, h;
	int offsetUV = width*height;
	for(h=0; h<width; h++) {
		for(w=0; w<height; w++) {
			outBuf[h*height+w] = inBuf[w*width+width-h];
		}
	}
	//UV
	inBuf += offsetUV;
	outBuf += offsetUV;
	int hWidth = width>>1;
	int hHeight = height>>1;
	for(h=0; h<hWidth; h++) {
		for(w=0; w<hHeight; w++) {
			outBuf[h*height+(w<<1)] = inBuf[w*width+width-(h<<1)];
			outBuf[h*height+(w<<1)+1] = inBuf[w*width+width-(h<<1)+1];
		}
	}
}

void nv21_rotate_180(jbyte* inBuf, int width, int height, jbyte* outBuf) {
	int w, h;
	int offsetUV = width*height;
	for(h=0; h<height; h++) {
		for(w=0; w<width; w++) {
			outBuf[h*width+w] = inBuf[(height-h)*width+width-w];
		}
	}

	//UV
	inBuf += offsetUV;
	outBuf += offsetUV;
	int hWidth = width>>1;
	int hHeight = height>>1;
	for(h=0; h<hHeight; h++) {
		for(w=0; w<hWidth; w++) {
			outBuf[h*width+(w<<1)] = inBuf[(hHeight-h)*width+width-(w<<1)];
			outBuf[h*width+(w<<1)+1] = inBuf[(hHeight-h)*width+width-(w<<1)+1];
		}
	}
}
JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_detect_NV21Sampler_native_1create(
		JNIEnv * env, jobject obj) {
	jbyte* byScaledBuf = (jbyte*)malloc(SCALED_BUFFER_SIZE);
	return (jint)byScaledBuf;
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_detect_NV21Sampler_native_1destroy(
		JNIEnv * env, jobject obj, jint handle) {
	jbyte* byScaledBuf = (jbyte*)handle;
	if(byScaledBuf) {
		free(byScaledBuf);
	}
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_detect_NV21Sampler_native_1downSample(
		JNIEnv * env, jobject obj, jint handle, jbyteArray inBuf, jint inWidth, jint inHeight, jbyteArray outBuf, jint sampleSize, jint rotate) {
	jbyte* in = env->GetByteArrayElements(inBuf, 0);
	jbyte* out = env->GetByteArrayElements(outBuf, 0);

	int outWidth = inWidth/sampleSize;
	int outHeight = inHeight/sampleSize;

	jbyte* pScaledBuf = (jbyte*)handle;;
	jbyte* pInLine = in;
	jbyte* pOutLine = pScaledBuf;
	jbyte* pIn = pInLine;
	jbyte* pOut = pOutLine;

	int count = 0;
	int w,h;
	for(h=0; h<outHeight; h++) {
		pIn = pInLine;
		pOut = pOutLine;
		for(w=0; w<outWidth; w++) {
			*pOut = *pIn;
			pOut ++;
			pIn += sampleSize;
		}
		pInLine += sampleSize*inWidth;
		pOutLine += outWidth;
	}

	for(h=0; h<outHeight/2; h++) {
		pIn = pInLine;
		pOut = pOutLine;
		for(w=0; w<outWidth; w+=2) {
			*pOut = *pIn;
			pOut ++;
			*pOut = *(pIn+1);
			pOut ++;

			pIn += sampleSize*2;
		}
		pInLine += sampleSize*inWidth;
		pOutLine += outWidth;
	}

	switch(rotate) {
	case 90:
		nv21_rotate_90(pScaledBuf, outWidth, outHeight, out);
		break;
	case 180:
		nv21_rotate_180(pScaledBuf, outWidth, outHeight, out);
		break;
	case 270:
		nv21_rotate_270(pScaledBuf, outWidth, outHeight, out);
		break;
	default:
		memcpy(out, pScaledBuf, outWidth*outHeight*3/2);
		break;
	}

//	jbyte* pInYLine = in;
//	jbyte* pOutYLine = out;
//	jbyte* pInY = pInYLine;
//	jbyte* pOutY = pOutYLine;
//
//	jbyte* pInVULine = in + inWidth*inHeight;
//	jbyte* pOutVULine = out + outWidth*outHeight;
//	jbyte* pInVU = pInVULine;
//	jbyte* pOutVU = pOutVULine;
//
//	int count = 0;
//	int w,h;
//
//	for(h=0; h<outHeight>>1; h++) {
//		pInY = pInYLine;
//		pOutY = pOutYLine;
//
//		pInVU = pInVULine;
//		pOutVU = pOutVULine;
//		for(w=0; w<outWidth; w+=2) {
//			*pOutY = *pInY;
//			pOutY ++;
//			pInY += sampleSize;
//			*pOutY = *pInY;
//			pOutY ++;
//			pInY += sampleSize;
//
//			*pOutVU = *pInVU;
//			pOutVU ++;
//			*pOutVU = *(pInVU+1);
//			pOutVU ++;
//
//			pInVU += sampleSize<<1;
//		}
//		for(w=0; w<outWidth; w++) {
//			*pOutY = *pInY;
//			pOutY ++;
//			pInY += sampleSize;
//		}
//		pInYLine += sampleSize*inWidth<<1;
//		pOutYLine += outWidth<<1;
//
//		pInVULine += sampleSize*inWidth;
//		pOutVULine += outWidth;
//	}

	env->ReleaseByteArrayElements(inBuf, in, 0);
	env->ReleaseByteArrayElements(outBuf, out, 0);
}
