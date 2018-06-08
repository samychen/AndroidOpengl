#include "NV21Sampler_jni.h"
#include <stdlib.h>
#include "utils/debug.h"

#define SCALED_BUFFER_SIZE (1280*720*3/2)


void nv21_rotate_270(jbyte* inBuf, int width, int height, jbyte* outBuf) //假设width > height
{
	int w, h;
	int offsetUV = width*height;
	jbyte * YSrc,*Usrc,*Ydst,*Udst;
	int widthh  = width>>1;
	int heighth = height>>1;
	YSrc = inBuf;
	Usrc = inBuf + offsetUV;

	Ydst = outBuf;
	Udst = outBuf + offsetUV;

	//Y channel
	for (w=0;w<width;w++,Ydst+=height)//目标高
	{
		jbyte * pYsrc = YSrc+width-1-w;
		for (h=0;h<height;h++,pYsrc+=width)//目标宽
		{
			Ydst[h]   = pYsrc[0];
		}
	}

	//uv channel
	for (w=0;w<widthh;w++,Udst+=height)//目标高
	{
		jbyte * pUsrc = Usrc + width-1 - (w<<1);
		jbyte * pUdst = Udst;
		for (h=0;h<heighth;h++,pUdst+=2,pUsrc+=width)
		{
			pUdst[0] = pUsrc[-1];
			pUdst[1] = pUsrc[0];
		}
	}
}

void nv21_rotate_90(jbyte* inBuf, int width, int height, jbyte* outBuf)
{
	int w, h;
	int offsetUV = width*height;
	jbyte * YSrc,*Usrc,*Ydst,*Udst;
	int widthh  = width>>1;
	int heighth = height>>1;
	YSrc = inBuf;
	Usrc = inBuf + offsetUV;

	Ydst = outBuf;
	Udst = outBuf + offsetUV;

	//Y channel
	for (w=0;w<width;w++,Ydst+=height)//目标高
	{
		jbyte * pYsrc = YSrc+w;
		for (h=height-1;h>=0;h--,pYsrc+=width)//目标宽
		{
			Ydst[h]   = pYsrc[0];
		}
	}

	//uv channel
	for (w=0;w<widthh;w++,Udst+=height)//目标高
	{
		jbyte * pUsrc = Usrc + (w<<1);
		jbyte * pUdst = Udst + height-1;
		for (h=heighth-1;h>=0;h--,pUdst-=2,pUsrc+=width)
		{
			pUdst[-1] = pUsrc[0];
			pUdst[0]  = pUsrc[1];
		}
	}
}

void nv21_rotate_180(jbyte* inBuf, int width, int height, jbyte* outBuf)
{
	int w, h;
	int offsetUV = width*height;
	jbyte * YSrc,*Usrc,*Ydst,*Udst;
	int widthh  = width>>1;
	int heighth = height>>1;
	YSrc = inBuf;
	Usrc = inBuf + offsetUV;

	Ydst = outBuf;
	Udst = outBuf + offsetUV;

	//Y channel
	YSrc += offsetUV;
	for (h=0;h<height;h++,YSrc-=width,Ydst+=width)//目标高
	{
		jbyte * pYsrc = YSrc-1;
		for (w=0;w<width;w++,pYsrc--)//目标宽
		{
			Ydst[w]   = pYsrc[0];
		}
	}

	//uv channel
	Usrc += heighth*width;
	for (h=0;h<heighth;h++,Usrc-=width,Udst+=width)//目标高
	{
		jbyte * pUsrc = Usrc - 2;
		jbyte * pUdst = Udst;
		for (w=0;w<widthh;w++,pUdst+=2,pUsrc-=2)
		{
			pUdst[0]  = pUsrc[0];
			pUdst[1]  = pUsrc[1];
		}
	}
}

JNIEXPORT jint JNICALL Java_com_ts_engine_NV21Sampler_native_1create(
		JNIEnv * env, jobject obj) {
	jbyte* byScaledBuf = (jbyte*)malloc(SCALED_BUFFER_SIZE);
	return (jint)byScaledBuf;
}

JNIEXPORT void JNICALL Java_com_ts_engine_NV21Sampler_native_1destroy(
		JNIEnv * env, jobject obj, jint handle) {
	jbyte* byScaledBuf = (jbyte*)handle;
	if(byScaledBuf) {
		free(byScaledBuf);
	}
}

JNIEXPORT void JNICALL Java_com_ts_engine_NV21Sampler_native_1downSample(
		JNIEnv * env, jobject obj, jint handle, jbyteArray inBuf, jint inWidth, jint inHeight, jbyteArray outBuf, jint sampleSize, jint rotate) {
	jbyte* in = env->GetByteArrayElements(inBuf, 0);
	jbyte* out = env->GetByteArrayElements(outBuf, 0);

	int outWidth = inWidth/sampleSize;
	int outHeight = inHeight/sampleSize;

	jbyte* pScaledBuf = in;
	if(sampleSize>1) {
		pScaledBuf = (jbyte*)handle;;
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
