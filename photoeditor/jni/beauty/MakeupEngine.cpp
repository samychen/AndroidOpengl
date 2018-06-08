#define LOG_TAG "MakeupEngine"

#ifndef DEBUG
#define DEBUG
#endif

#include <android/log.h>
#include <android/bitmap.h>
#include <GLES2/gl2.h>
#include <GLES/gl.h>
#include <stdio.h>
#include <stdlib.h>
//#include "com_thundersoft_hz_selfportrait_makeup_engine_MakeupEngine.h"

#include "tcomdef.h"
#include "tmem.h"
#include "terror.h"

#include "makeup.h"
#include "jRect.h"
#include "jPoint.h"
#include "utils/debug.h"


#define  DRAW_BOX

long t1, t2;



long gettime();

int toolsavefile(unsigned char* pData, int size, char* name)
{
    FILE *pf =fopen(name, "wb");
	if(pf != 0){
		fwrite(pData, size, 1, pf);
		fclose(pf);
		return 1;

	   }
	return 0;
}

int toolloadfile(unsigned char* pData, int size, char* name)
{

	FILE *pf =fopen(name, "rb");
	if(pf != 0){
		fread(pData, size, 1, pf);
		fclose(pf);
		return 1;

	   }
	return 0;
}

void drawbox(unsigned char* pRGBA, int linebyte, int left, int top, int right, int bottom)
{

	int i, j;
	unsigned char* pTemp1, *pTemp2;

	pTemp1 = pRGBA+top*linebyte+4*left;
	pTemp2 = pRGBA+bottom*linebyte+4*left;

	for(i=left; i<=right; i++)
	{
        pTemp1[4*i] = 255;
		pTemp2[4*i] = 255;
	}

	pTemp1 = pRGBA+top*linebyte+4*left;
	pTemp2 = pRGBA+top*linebyte+4*right;

	for(j=top; j<=bottom; j++)
	{
        pTemp1[j*linebyte] = 255;
		pTemp2[j*linebyte] = 255;
	}



}

static int worksize = 25*1024*1024;


#define  MAX_DEBLEMISHAREA  100
typedef struct{
   unsigned char* g_pMem;
   THandle g_hMem;
   THandle g_makeuphandle;
   MakeupFeatureParam g_param;
   MakeupDeblemishPara g_deblemishpara[MAX_DEBLEMISHAREA];
   TInt32 g_deblemishnum;
   TRECT  g_FaceRect[MAEUP_MAX_FACE];
   TPOINT  g_EyePoint[MAEUP_MAX_FACE*2];
   TPOINT  g_MouthPoint[MAEUP_MAX_FACE];
}MkParam;

/*
 * Class:     com_thundersoft_hz_selfportrait_makeup_engine_MakeupEngine
 * Method:    Init
 * Signature: ()V
 */
extern "C" {
JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_Init
  (JNIEnv *, jclass)
{
		MkParam* handle = (MkParam*) malloc(sizeof(MkParam));
		memset(handle, 0, sizeof(MkParam));
		if(handle->g_pMem != 0)
		free(handle->g_pMem);

	handle->g_pMem = (unsigned char*)malloc(worksize);
	if(handle->g_pMem != 0)
	{
		handle->g_hMem = TMemMgrCreate(handle->g_pMem, worksize);
		if(makeup_init( handle->g_hMem, &(handle->g_makeuphandle)) != 0)
         LOGE("makeup_init error");
	}else
	{
         LOGE("no memory");
	}

//	if(makeup_init(0, &g_makeuphandle) != 0) LOGE("makeup_init error");
	LOGE("makeup_init ok");
	return (int)handle;
}

/*
 * Class:     com_thundersoft_hz_selfportrait_makeup_engine_MakeupEngine
 * Method:    UnInit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_UnInit
  (JNIEnv * env, jclass thiz, jint param)
{
	MkParam* handle = (MkParam*)param;
	if(handle->g_pMem != 0 && handle->g_makeuphandle != 0){

		makeup_done(handle->g_makeuphandle);
		free(handle->g_pMem);
		handle->g_pMem = 0;
		handle->g_makeuphandle = 0;
	}

	LOGE("makeup_done ok");
	free(handle);
}


/*
 * Class:     com_thundersoft_hz_selfportrait_makeup_engine_MakeupEngine
 * Method:    ResetParameter
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_ResetParameter
(JNIEnv * env, jclass thiz, jint param)
{
	MkParam* handle = (MkParam*)param;
	TMemSet(&(handle->g_param), 0, sizeof(handle->g_param));
	TMemSet(handle->g_deblemishpara, 0, MAX_DEBLEMISHAREA*sizeof(MakeupDeblemishPara));
	handle->g_deblemishnum = 0;

	LOGE("ResetParameter ok");

}


JNIEXPORT void JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_ReplaceImage
  (JNIEnv * env, jclass thiz,  jint param,jobject bmp, jintArray facenumarray, jobjectArray facerect, jobjectArray eyepoint, jobjectArray mouthpoint, jintArray marks, jintArray eyeMarks, jboolean bSkinDetect)
{
	MkParam* handle = (MkParam*)param;
	unsigned char* pRGBA;
    AndroidBitmapInfo  info;
    void*              pixels;

   	if (AndroidBitmap_getInfo(env, bmp, &info) < 0)
	{
		LOGE("AndroidBitmap_getInfo failed");
	}
	else
	{
		LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);

		if (AndroidBitmap_lockPixels(env, bmp, &pixels) < 0) {
			LOGE("AndroidBitmap_lockPixels() failed ! error");
		}

		if (info.format == ANDROID_BITMAP_FORMAT_RGB_565)
		{
			LOGE("format is ANDROID_BITMAP_FORMAT_RGB_565");

		}

		if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888)
		{
			LOGI("format is ANDROID_BITMAP_FORMAT_RGBA_8888");
			pRGBA = (unsigned char*)pixels;
		}
// 		{
// 			char name[64];
// 			sprintf(name, "/sdcard/temp_%d_%d.RGBA", info.stride/4, info.height);
// 			toolsavefile(pRGBA, info.stride*info.height, name);
//
// 		}
       	TSOFFSCREEN Image;
		TInt32 facenum = 0;
        TRECT  *pFaceRect = (TRECT  *)(handle->g_FaceRect);
		TPOINT  *pEyePoint = (TPOINT  *)(handle->g_EyePoint);
		TPOINT  *pMouthPoint= (TPOINT  *)(handle->g_MouthPoint);

		TInt32 *pFaceNum;
		if (!facenumarray) {LOGE("invalid args facenumarray!");}
    pFaceNum = (TInt32 *)env->GetPrimitiveArrayCritical(facenumarray,0);
		facenum = *pFaceNum;
		if (facenumarray)
            env->ReleasePrimitiveArrayCritical(facenumarray,pFaceNum,0);

		jclass rectclazz = env->FindClass("android/graphics/Rect");
		jclass pointclazz = env->FindClass("android/graphics/Point");

    		if(facenum > 0)
		{
              int i;
			  for(i=0; i<facenum; i++)
			  {
					jobject rectobj = env->GetObjectArrayElement(facerect, i);
					jRect fRect(env, rectobj);
					pFaceRect[i].left = fRect.getLeft();
					pFaceRect[i].top = fRect.getTop();
					pFaceRect[i].right = fRect.getRight();
					pFaceRect[i].bottom = fRect.getBottom();
			  }

			  for(i=0; i<facenum; i++)
			  {

				  jobject point = env->GetObjectArrayElement(eyepoint, i*2);
					jPoint fPoint(env, point);
					pEyePoint[i*2].x = fPoint.getX();
					pEyePoint[i*2].y = fPoint.getY();

				  point = env->GetObjectArrayElement(eyepoint, i*2+1);
					jPoint fPoint1(env, point);
					pEyePoint[i*2+1].x = fPoint1.getX();
					pEyePoint[i*2+1].y = fPoint1.getY();

			  }

			  for(i=0; i<facenum; i++)
			  {

				  jobject point = env->GetObjectArrayElement(mouthpoint, i);
					jPoint fPoint(env, point);
					pMouthPoint[i].x = fPoint.getX();
					pMouthPoint[i].y = fPoint.getY();
			  }
		}

        jint *pMarks = NULL;
        jint *pEyeMarks = NULL;
        if(marks!=NULL) {
        	pMarks = env->GetIntArrayElements(marks, 0);
        }
        if(eyeMarks!=NULL) {
        	pEyeMarks = env->GetIntArrayElements(eyeMarks, 0);
        }

        Image.i32Width = info.width;
		Image.i32Height = info.height;
		Image.ppu8Plane[0] = pRGBA;
		Image.pi32Pitch[0] = info.stride;
		Image.u32PixelArrayFormat = TS_PAF_RGB32_R8G8B8A8;

		LOGE("Load Img info.width =  %d, info.height %d", info.width, info.height);


		t1 = gettime();

				LOGE("facenum = %d",facenum);
		LOGE("pFaceRect = %d,%d,%d,%d",pFaceRect->left,pFaceRect->top,pFaceRect->right,pFaceRect->bottom);
		LOGE("pEyePoint = %d,%d",pEyePoint->x,pEyePoint->y);
		LOGE("pMouthPoint = %d,%d",pMouthPoint->x,pMouthPoint->y);

		if(bSkinDetect) {
			LOGI("makeup_replace_image \n");
			if(makeup_replace_image(handle->g_makeuphandle,
				&Image, &facenum, pFaceRect, pEyePoint, pMouthPoint, (TInt32*)pMarks, (TInt32*)pEyeMarks)    != 0)
				LOGE("makeup_load_image error \n");
		} else {
			LOGI("makeup_replace_image_withoutsd \n");
			if(makeup_replace_image_withoutsd(handle->g_makeuphandle,
				&Image, &facenum, pFaceRect, pEyePoint, pMouthPoint, (TInt32*)pMarks, (TInt32*)pEyeMarks)    != 0)
				LOGE("makeup_load_image error \n");
		}


		t2 = gettime();

	    LOGE("Load Img Find %d face, cost %d ms\n", facenum, t2-t1);
//		pFaceNum[0] = facenum;

        LOGE("[Ln%d] Load Img ", __LINE__);

	    env->DeleteLocalRef(rectclazz);
		env->DeleteLocalRef(pointclazz);

        if(marks!=NULL) {
        	env->ReleaseIntArrayElements(marks, pMarks, 0);
        }
        if(eyeMarks!=NULL) {
        	env->ReleaseIntArrayElements(eyeMarks, pEyeMarks, 0);
        }
		AndroidBitmap_unlockPixels(env, bmp);

		LOGE("[Ln%d] Load Img ", __LINE__);


		TMemSet(&(handle->g_param), 0, sizeof(handle->g_param));
		TMemSet((handle->g_deblemishpara), 0, MAX_DEBLEMISHAREA*sizeof(MakeupDeblemishPara));

		handle->g_deblemishnum = 0;
        LOGE("[Ln%d] Load Img ", __LINE__);

	}


}






const int FEATUREMODE_DEBLEMISh = 0x01;
const int FEATUREMODE_WHITENFACE = 0x02;
const int FEATUREMODE_SOFTENFACE = 0x03;
const int FEATUREMODE_TRIMFACE = 0x04;
const int FEATUREMODE_BIGEYE = 0x05;
const int FEATUREMODE_FACESKIN = 0x06;
const int FEATUREMODE_EYEBAG = 0x07;
const int FEATUREMODE_DARKCIRCLE = 0x08;
const int FEATUREMODE_BRIGHTEYE = 0x09;



/*
 * Class:     com_thundersoft_hz_selfportrait_makeup_engine_MakeupEngine
 * Method:    ManageImgae
 * Signature: (Landroid/graphics/Bitmap;Lcom/thundersoft/hz/selfportrait/makeup/engine/FeatureInfo;)I
 */
JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_ManageImgae
  (JNIEnv * env, jclass thiz, jint param,jobject bmp, jobject featureinfo)
{
	MkParam* handle = (MkParam*)param;
	AndroidBitmapInfo  info;
    void*              pixels;
	int nWorkMode;
	int nIntensity;
	TInt32 nAeraNum = 0;
    unsigned char* pRGBA;
	jobject point;
	int nSkinColorType;

	if (AndroidBitmap_getInfo(env, bmp, &info) < 0)
	{
		LOGE("AndroidBitmap_getInfo failed");
	}
	else
	{
		LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);

		if (AndroidBitmap_lockPixels(env, bmp, &pixels) < 0) {
			LOGE("AndroidBitmap_lockPixels() failed ! error");
		}

		if (info.format == ANDROID_BITMAP_FORMAT_RGB_565)
		{
			LOGE("format is ANDROID_BITMAP_FORMAT_RGB_565");

		}

		if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888)
		{
			LOGI("format is ANDROID_BITMAP_FORMAT_RGBA_8888");
			pRGBA = (unsigned char*)pixels;
		}

        TSOFFSCREEN Image;
		Image.i32Width = info.width;
		Image.i32Height = info.height;
		Image.ppu8Plane[0] = pRGBA;
		Image.pi32Pitch[0] = info.stride;
		Image.u32PixelArrayFormat = TS_PAF_RGB32_R8G8B8A8;

		jclass featureclazz = env->FindClass("com/cam001/photoeditor/beauty/makeup/engine/FeatureInfo");
		jclass pointclazz = env->FindClass("android/graphics/Point");
	    jfieldID xId = env->GetFieldID(pointclazz, "x", "I");
		jfieldID yId =  env->GetFieldID(pointclazz, "y", "I");
		jmethodID mid = env->GetMethodID(featureclazz,"GetMod","()I");
		jmethodID mid2 = env->GetMethodID(featureclazz,"GetDeblemishNum","()I");
		jmethodID mid3 = env->GetMethodID(featureclazz,"GetDeblemishArea","(I)Landroid/graphics/Point;");
		jmethodID mid4 = env->GetMethodID(featureclazz,"GetDeblemishRadius","(I)I");
		jmethodID mid5 = env->GetMethodID(featureclazz,"SetDeblemishRadius","(II)V");
//		jmethodID midCb = env->GetMethodID(featureclazz,"getSkinColorCb","()I");
//		jmethodID midCr = env->GetMethodID(featureclazz,"getSkinColorCr","()I");
		jmethodID midCt = env->GetMethodID(featureclazz,"GetSkinFoundationType","()I");
				nSkinColorType = env->CallIntMethod(featureinfo, midCt);


		nWorkMode = env->CallIntMethod(featureinfo,mid);
        mid = env->GetMethodID(featureclazz,"GetIntensity","()I");
		nIntensity = env->CallIntMethod(featureinfo,mid);

		 LOGE("nWorkMode = %d\n", nWorkMode);
		 LOGE("nIntensity = %d\n", nIntensity);



		handle->g_param.bNewDeblemish = TFalse;
		switch(nWorkMode)
		{
		case FEATUREMODE_DEBLEMISh:
            nAeraNum = env->CallIntMethod(featureinfo,mid2);
			handle->g_param.pDeblemishParaNum = &(handle->g_deblemishnum);
			handle->g_param.pDeblemishPara =handle-> g_deblemishpara;
			if(nAeraNum != 0){
		  	   handle->g_param.DeBlemish_Enable = TTrue;
			  handle-> g_deblemishnum = nAeraNum;

			   for(int i=0; i<nAeraNum; i++)
			   {

				   point = env->CallObjectMethod(featureinfo, mid3, i);
                   handle->g_deblemishpara[i].selectpoint.x = env->GetIntField(point, xId);
                  handle-> g_deblemishpara[i].selectpoint.y = env->GetIntField(point, yId);
                  handle-> g_deblemishpara[i].deblemishRadius = env->CallIntMethod(featureinfo, mid4, i);
				   LOGE("DoDeblemish selectpoint [%d, %d]\n",(int)(handle-> g_deblemishpara[i].selectpoint.x), (int)(handle->g_deblemishpara[i].selectpoint.y));
				   LOGE("DoDeblemish deblemishRadius = %d\n", (int)(handle->g_deblemishpara[i].deblemishRadius));
				   env->DeleteLocalRef(point);

				   handle->g_param.bNewDeblemish = TTrue;

			   }

			}else {
               nAeraNum = 0;
               handle->g_param.DeBlemish_Enable = TFalse;
			}
			LOGE("DoDeblemish nAeraNum = %d\n",(int)nAeraNum);
			break;

		case FEATUREMODE_WHITENFACE:
			handle->g_param.SkinWhiteningLevel = nIntensity;
			if(nIntensity == 0)
				handle->g_param.SkinWhiten_Enable = TFalse;
			else
				handle->g_param.SkinWhiten_Enable = TTrue;
			break;
		case FEATUREMODE_FACESKIN:
			handle->g_param.SkinColorLevel = nIntensity;
			  handle->g_param.SkinColorType = nSkinColorType;
			if(nIntensity == 0)
				handle->g_param.SkinColor_Enable = TFalse;
			else
				handle->g_param.SkinColor_Enable = TTrue;
//				g_param.SkinColorMask.Cb = env->CallIntMethod(featureinfo,midCb);
//				g_param.SkinColorMask.Cr = env->CallIntMethod(featureinfo,midCr);
			break;
		case FEATUREMODE_SOFTENFACE:
			handle->g_param.SkinCleanLevel = nIntensity;
			if(nIntensity == 0)
				handle->g_param.SkinClean_Enable = TFalse;
			else
				handle->g_param.SkinClean_Enable = TTrue;

			break;

		case FEATUREMODE_TRIMFACE:
			handle->g_param.TrimFaceLevel = nIntensity;
			if(nIntensity == 0)
				handle->g_param.TrimFace_Enable = TFalse;
			else
				handle->g_param.TrimFace_Enable = TTrue;

			break;


		case FEATUREMODE_BIGEYE:

			handle->g_param.BigEyegLevel = nIntensity;
			if(nIntensity == 0)
				handle->g_param.BigEye_Enable = TFalse;
			else
				handle->g_param.BigEye_Enable = TTrue;

			break;

		case FEATUREMODE_EYEBAG:
	       handle-> g_param.EyeBagLevel = nIntensity;
	        if (nIntensity == 0)
		       handle->  g_param.EyeBag_Enable = TFalse;
	        else
		       handle->  g_param.EyeBag_Enable = TTrue;
	      break;
		case FEATUREMODE_DARKCIRCLE:
			handle->g_param.DarkCircleLevel = nIntensity;
			if(nIntensity==0) {
				handle->g_param.DarkCircle_Enable = TFalse;
			} else {
				handle->g_param.DarkCircle_Enable = TTrue;
			}
			break;
		case FEATUREMODE_BRIGHTEYE:
			handle->g_param.BrightEyeLevel = nIntensity;
			if(nIntensity==0) {
				handle->g_param.BrightEye_Enable = TFalse;
			} else {
				handle->g_param.BrightEye_Enable = TTrue;
			}
			break;
		}

        LOGE("before makeup_effect");
		t1 = gettime();
		int res = makeup_effect(
			handle->g_makeuphandle,   &(handle->g_param), &Image);
		t2 = gettime();

		if(res != TOK)
			LOGE("makeup_effect error, return %d\n", res);
		else
            LOGE("makeup_effect OK, cost %d ms\n", (int)(t2-t1));



		if(handle->g_param.bNewDeblemish == TTrue) //µÃµ½ÐÂµÄµã
		{
            int i = handle->g_deblemishnum-1;
			point = env->CallObjectMethod(featureinfo, mid3, i);
			env->SetIntField(point, xId, handle->g_param.pDeblemishPara[i].selectpoint.x);
			env->SetIntField(point, yId,handle-> g_param.pDeblemishPara[i].selectpoint.y);
			env->CallVoidMethod(featureinfo, mid5, i, handle->g_param.pDeblemishPara[i].deblemishRadius);
			LOGE("DoDeblemish selectpoint [%d, %d], radius[%d]\n",(int)(handle->g_deblemishpara[i].selectpoint.x), (int)(handle->g_deblemishpara[i].selectpoint.y), (int)(handle->g_param.pDeblemishPara[i].deblemishRadius));
		    env->DeleteLocalRef(point);
		}


		env->DeleteLocalRef(pointclazz);
		env->DeleteLocalRef(featureclazz);



		AndroidBitmap_unlockPixels(env, bmp);

	}

	return 0;
}

/*
 * Class:     com_thundersoft_hz_selfportrait_makeup_engine_MakeupEngine
 * Method:    SetParameter
 * Signature: (Lcom/thundersoft/hz/selfportrait/makeup/engine/FeatureInfo;)I
 */
JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_SetParameter
   (JNIEnv * env, jclass thiz, jint param, jobject featureinfo)
{
	MkParam* handle = (MkParam*)param;

	int nWorkMode;
	int nIntensity;
	TInt32 nAeraNum = 0;
    unsigned char* pRGBA;
	jobject point;

	jclass featureclazz = env->FindClass("com/cam001/photoeditor/beauty/makeup/engine/FeatureInfo");
	jclass pointclazz = env->FindClass("android/graphics/Point");
	jfieldID xId = env->GetFieldID(pointclazz, "x", "I");
	jfieldID yId =  env->GetFieldID(pointclazz, "y", "I");
	jmethodID mid = env->GetMethodID(featureclazz,"GetMod","()I");
	jmethodID mid2 = env->GetMethodID(featureclazz,"GetDeblemishNum","()I");
	jmethodID mid3 = env->GetMethodID(featureclazz,"GetDeblemishArea","(I)Landroid/graphics/Point;");
	jmethodID mid4 = env->GetMethodID(featureclazz,"GetDeblemishRadius","(I)I");
	jmethodID mid5 = env->GetMethodID(featureclazz,"SetDeblemishRadius","(II)V");

	jmethodID midCb = env->GetMethodID(featureclazz,"getSkinColorCb","()I");
	jmethodID midCr = env->GetMethodID(featureclazz,"getSkinColorCr","()I");


	nWorkMode = env->CallIntMethod(featureinfo,mid);
	mid = env->GetMethodID(featureclazz,"GetIntensity","()I");
	nIntensity = env->CallIntMethod(featureinfo,mid);

	LOGE("nWorkMode = %d\n", nWorkMode);
	LOGE("nIntensity = %d\n", nIntensity);



	handle->g_param.bNewDeblemish = TFalse;
	switch(nWorkMode)
	{
	case FEATUREMODE_DEBLEMISh:
		nAeraNum = env->CallIntMethod(featureinfo,mid2);
		handle->g_param.pDeblemishParaNum = &(handle->g_deblemishnum);
		handle->g_param.pDeblemishPara =handle-> g_deblemishpara;
		if(nAeraNum != 0){
			handle->g_param.DeBlemish_Enable = TTrue;
			handle->g_deblemishnum = nAeraNum;

			for(int i=0; i<nAeraNum; i++)
			{

				point = env->CallObjectMethod(featureinfo, mid3, i);
				handle->g_deblemishpara[i].selectpoint.x = env->GetIntField(point, xId);
				handle->g_deblemishpara[i].selectpoint.y = env->GetIntField(point, yId);
				handle->g_deblemishpara[i].deblemishRadius = env->CallIntMethod(featureinfo, mid4, i);
				LOGE("DoDeblemish selectpoint [%d, %d]\n",(int)(handle-> g_deblemishpara[i].selectpoint.x),(int)(handle->g_deblemishpara[i].selectpoint.y));
				LOGE("DoDeblemish deblemishRadius = %d\n",(int)(handle->g_deblemishpara[i].deblemishRadius));
				env->DeleteLocalRef(point);

				handle->g_param.bNewDeblemish = TTrue;

			}

		}else
			nAeraNum = 0;

		LOGE("DoDeblemish nAeraNum = %d\n", (int)nAeraNum);
		break;

	case FEATUREMODE_WHITENFACE:
		handle->g_param.SkinWhiteningLevel = nIntensity;
		if(nIntensity == 0)
			handle->g_param.SkinWhiten_Enable = TFalse;
		else
			handle->g_param.SkinWhiten_Enable = TTrue;
		break;

	case FEATUREMODE_SOFTENFACE:
		handle->g_param.SkinCleanLevel = nIntensity;
		if(nIntensity == 0)
		handle->g_param.SkinClean_Enable = TFalse;
		else
		handle->g_param.SkinClean_Enable = TTrue;

		break;

	case FEATUREMODE_FACESKIN:
		handle->g_param.SkinColorLevel = nIntensity;
		if(nIntensity == 0)
			handle->g_param.SkinColor_Enable = TFalse;
		else
			handle->g_param.SkinColor_Enable = TTrue;
			handle->g_param.SkinColorMask.Cb = env->CallIntMethod(featureinfo,midCb);
			handle->g_param.SkinColorMask.Cr = env->CallIntMethod(featureinfo,midCr);
		break;

	case FEATUREMODE_TRIMFACE:
		handle->g_param.TrimFaceLevel = nIntensity;
		if(nIntensity == 0)
			handle->g_param.TrimFace_Enable = TFalse;
		else
			handle->g_param.TrimFace_Enable = TTrue;

		break;


	case FEATUREMODE_BIGEYE:

		handle->g_param.BigEyegLevel = nIntensity;
		if(nIntensity == 0)
			handle->g_param.BigEye_Enable = TFalse;
		else
			handle->g_param.BigEye_Enable = TTrue;

		break;
	case FEATUREMODE_EYEBAG:
		handle->g_param.EyeBagLevel = nIntensity;
				if(nIntensity == 0)
					handle->g_param.EyeBag_Enable = TFalse;
				else
					handle->g_param.EyeBag_Enable = TTrue;
		break;
	case FEATUREMODE_DARKCIRCLE:
		handle->g_param.DarkCircleLevel = nIntensity;
		if(nIntensity==0) {
			handle->g_param.DarkCircle_Enable = TFalse;
		} else {
			handle->g_param.DarkCircle_Enable = TTrue;
		}
		break;
	case FEATUREMODE_BRIGHTEYE:
		handle->g_param.BrightEyeLevel = nIntensity;
		if(nIntensity==0) {
			handle->g_param.BrightEye_Enable = TFalse;
		} else {
			handle->g_param.BrightEye_Enable = TTrue;
		}
		break;
	}

	env->DeleteLocalRef(pointclazz);
	env->DeleteLocalRef(featureclazz);

	return 0;
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_beauty_makeup_engine_MakeupEngine_TakeEffect
  (JNIEnv * env, jclass thiz, jobject bmp)//not used yet
  {/*

	AndroidBitmapInfo  info;
    void*              pixels;
    unsigned char* pRGBA;

	if (AndroidBitmap_getInfo(env, bmp, &info) < 0)
	{
		LOGE("AndroidBitmap_getInfo failed");
	}
	else
	{
		LOGI("width = %d, height = %d, stride = %d", info.width, info.height, info.stride);

		if (AndroidBitmap_lockPixels(env, bmp, &pixels) < 0) {
			LOGE("AndroidBitmap_lockPixels() failed ! error");
		}

		if (info.format == ANDROID_BITMAP_FORMAT_RGB_565)
		{
			LOGE("format is ANDROID_BITMAP_FORMAT_RGB_565");

		}

		if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888)
		{
			LOGI("format is ANDROID_BITMAP_FORMAT_RGBA_8888");
			pRGBA = (unsigned char*)pixels;
		}

        TSOFFSCREEN Image;
		Image.i32Width = info.width;
		Image.i32Height = info.height;
		Image.ppu8Plane[0] = pRGBA;
		Image.pi32Pitch[0] = info.stride;
		Image.u32PixelArrayFormat = TS_PAF_RGB32_R8G8B8A8;

		t1 = gettime();
		int res = makeup_effect(
			g_makeuphandle,   &g_param, &Image);
		t2 = gettime();

		if(res != TOK)
			LOGE("makeup_effect error, return %d\n", res);
		else
            LOGE("makeup_effect OK, cost %d ms\n", t2-t1);

		AndroidBitmap_unlockPixels(env, bmp);

	}*/

	return 0;
}
}
