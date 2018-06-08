#include "tsmakeuprt_engine_jni.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>
#include <time.h>
#include <android/bitmap.h>
#include <android/asset_manager_jni.h>
#include "utils/debug.h"
#include "utils/BGRNV21.h"
#include "ts-detect-object.h"
#include "ts-facial-outline.h"
#include "cosmetic.h"
#include "tsoffscreen.h"
#include "FaceBeautify.h"
#include "jRect.h"
#include "jPoint.h"

typedef struct {
	THandle facialDetectHandle;
	THandle facialOutlineHandle;
	THandle makeupHandle;
	AAssetManager* asset;
	int rotate;
	TDouble *landmarks;
	TPOINT* lm;
	TSOFFSCREEN* gImageOs;
	unsigned char* m_cbFrameBuf;
	CFaceBeautify* m_faceHandle;
} MkParam;
int nLandMarks;
TRECT m_facerect = { 0 };

void ucamebeautifyInit(MkParam* param)
{
	int nWidth = param->gImageOs->i32Width;
	int nHeight = param->gImageOs->i32Height;
	int bufSize = nWidth*nHeight*3/2;
	if(!param->m_cbFrameBuf) {
		param->m_cbFrameBuf = (unsigned char*)malloc(bufSize);
		 memset(param->m_cbFrameBuf, 0, bufSize);

	}
	if(param->m_faceHandle==0) {
		param->m_faceHandle = new CFaceBeautify();
	}
}
void ucamebeautifyUninit(MkParam* param)
{
	if(param->m_cbFrameBuf != 0) {
		free(param->m_cbFrameBuf);
		param->m_cbFrameBuf = 0;
	}
	if(param->m_faceHandle!=0) {
		param->m_faceHandle->Exit();
		param->m_faceHandle = 0;
	}
}

TSOFFSCREEN* create_offscreen(JNIEnv * env, jobject bmp, int format) {
	LOGI("Step1------------>");
	TSOFFSCREEN* res;
	AndroidBitmapInfo info;
	void* pPixels;
	res = (TSOFFSCREEN*)malloc(sizeof(TSOFFSCREEN));
	ASSERT(AndroidBitmap_getInfo(env, bmp, &info)==ANDROID_BITMAP_RESUT_SUCCESS);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, bmp, &pPixels)== ANDROID_BITMAP_RESUT_SUCCESS);

	info.width = info.width&(~1);
	info.height = info.height&(~1);
	res->u32PixelArrayFormat = format;
	res->i32Width = info.width;
	res->i32Height = info.height;
	LOGI("Step2------------>widthxheight=%dx%d", res->i32Width, res->i32Height);
	switch(format) {
	case TS_PAF_NV21:
		res->pi32Pitch[0] = res->i32Width;
		res->pi32Pitch[1] = res->i32Width;
		res->ppu8Plane[0] = (TUInt8*)malloc(res->pi32Pitch[0]*res->i32Height
				+res->pi32Pitch[1]*res->i32Height/2);
		res->ppu8Plane[1] = res->ppu8Plane[0] + res->pi32Pitch[0]*res->i32Height;
		RGBA8888_to_NV21((TUInt8*) pPixels, res->ppu8Plane[0], info.width, info.height,info.stride);
		break;
	case TS_PAF_RGB24_B8G8R8:
		res->pi32Pitch[0] = res->i32Width*3;
		res->ppu8Plane[0] = (TUInt8*)malloc(res->pi32Pitch[0]*res->i32Height);
		ARGB2RGB((TUInt8*) pPixels, res->ppu8Plane[0], info.width, info.height);
		break;
	case TS_PAF_GRAY:
		res->pi32Pitch[0] = res->i32Width;
		res->ppu8Plane[0] = (TUInt8*)malloc(res->pi32Pitch[0]*res->i32Height);
		ARGB2GRAY((TUInt8*) pPixels, res->ppu8Plane[0], info.width, info.height);
		break;
	default:
		ASSERT(0);
		break;
	}
	AndroidBitmap_unlockPixels(env, bmp);
	return res;
}

void destory_offscreen(TSOFFSCREEN* os) {
	if(os==NULL) return;
	if(os->ppu8Plane[0]) {
		free(os->ppu8Plane[0]);
	}
	free(os);
}

int detect_face_rect(THandle fd_handle, TSOFFSCREEN* image, TRECT* outFaceRect, int rotate) {
	tsDetectObject_setImage(fd_handle, image, rotate);
	int count = tsDetectObject_detect(fd_handle, (char*) "face");
	LOGI("CrazyFaceEngine_detect_face count=%d", count);
	if(count>0) {
		tsDetectObject_object(fd_handle, 0, outFaceRect);
	} else { // If no face is detected, we use the whole picture as a face.
		outFaceRect->left = image->i32Width/8;
		outFaceRect->top = image->i32Height/8;
		outFaceRect->right = image->i32Width-outFaceRect->left;
		outFaceRect->bottom = image->i32Height-outFaceRect->top;
	}
	return count;
}

jobjectArray detectface(JNIEnv* env, jint handle, TSOFFSCREEN* os,
		TRECT* face=0, TPOINT* leye=0, TPOINT* reye=0, TPOINT* mouth=0, int rotate=0){
	clock_t start;
	start = clock();
	MkParam* param = (MkParam*) handle;
	TRESULT err;
	if(rotate!=param->rotate) {
		clock_t c1 = clock();
		tsDetectObject_reset(param->facialDetectHandle);
		clock_t c2 = clock();
		LOGI("tsDetectObject_reset cost %d ms", (int)(c2-c1)*1000/CLOCKS_PER_SEC);
		param->rotate = rotate;
	}

	int face_count = 1;
	if(face) {
		memcpy(&m_facerect, face, sizeof(TRECT));
	} else {
		face_count = detect_face_rect(param->facialDetectHandle, os, &m_facerect, rotate);
	}
		if (face_count > 0) {
			if(leye&&reye&&mouth) {
				err = tsFacialOutline_figure_ex(param->facialOutlineHandle, os, m_facerect,
						*leye, *reye, *mouth, NULL, rotate);
			} else {
				err = tsFacialOutline_figure(param->facialOutlineHandle, os, m_facerect,
					NULL, rotate);
			}
		}
		LOGI("Step4------------>tsDetectObject_detect face[0]=%d,%d,%d,%d :::%d",
				m_facerect.left, m_facerect.top, m_facerect.right, m_facerect.bottom,
				(int )(param->facialOutlineHandle));
		jobjectArray jpointarray = NULL;
		LOGI("Step5------------>err=%d count=%d", err, face_count);
		if (err == TOK && face_count > 0) {
			LOGI("Step6------------>tsFacialOutline_figure ok");
			jclass pointclass = env->FindClass("android/graphics/Point");
			jfieldID field_x = env->GetFieldID(pointclass, "x", "I");
			jfieldID field_y = env->GetFieldID(pointclass, "y", "I");
			tsFacialOutline_getProperty(param->facialOutlineHandle, "landmarks",
					(void*) (param->landmarks));
			jpointarray = env->NewObjectArray(nLandMarks, pointclass, NULL);
			for (int i = 0; i < nLandMarks; i++) {
				jobject jobj = env->AllocObject(pointclass);
				env->SetIntField(jobj, field_x,
						(int) ((TDouble*) param->landmarks)[i * 2]);
				env->SetIntField(jobj, field_y,
						(int) ((TDouble*) param->landmarks)[i * 2 + 1]);
				env->SetObjectArrayElement(jpointarray, i, jobj);
			}
		}
		LOGI("TsMakeuprtEngine_facetracking cost=%lf ms",
				(double )(clock() - start) * 1000 / CLOCKS_PER_SEC);
		return jpointarray;
}

void facebeautify(MkParam* param){
//	LOGI("facebeautify into");
	ucamebeautifyInit(param);
	if(param->m_cbFrameBuf != 0){
		TSOFFSCREEN pInput = {0};
		pInput.u32PixelArrayFormat = TSFB_FMT_YUV420LP_VUVU;
		pInput.i32Width = param->gImageOs->i32Width;
		pInput.i32Height = param->gImageOs->i32Height;
		pInput.pi32Pitch[0] = pInput.i32Width;
		pInput.pi32Pitch[1] = pInput.i32Width;
		pInput.ppu8Plane[0] = param->m_cbFrameBuf;
		pInput.ppu8Plane[1] = (unsigned char*) pInput.ppu8Plane[0]
				+ pInput.i32Width * pInput.i32Height;
		memcpy(pInput.ppu8Plane[0], (param->gImageOs->ppu8Plane)[0], pInput.i32Width*pInput.i32Height*3/2);

//		m_facerect.left = 0;
//		m_facerect.right = nWidth;
//		m_facerect.top = 0;
//		m_facerect.bottom = nHeight;
		param->gImageOs->u32PixelArrayFormat = TSFB_FMT_YUV420LP_VUVU;
		int res = param->m_faceHandle->Work(&pInput, param->gImageOs, TNull, m_facerect, 70, 100, true);
		LOGI("facebeautify Work=%d", res);
		memcpy(pInput.ppu8Plane[0], (param->gImageOs->ppu8Plane)[0], pInput.i32Width*pInput.i32Height*3/2);
		param->gImageOs->u32PixelArrayFormat = TS_PAF_NV21;
//		dumpToFile("/sdcard/facebeautify960x1280.nv21", (unsigned char*)(pInput.ppu8Plane[0]), pInput.i32Width*pInput.i32Height*3/2);
//		LOGI("facebeautify out");
	}
}

JNIEXPORT jint JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_init(JNIEnv * env,
		jobject obj, jobject asset, jint mode) {// param: mode 1->facetracking 2->bmpdetect
//	LOGI("TsMakeuprtEngine_init <-----");
//	MkParam param = { 0 };
	MkParam* handle = (MkParam*) malloc(sizeof(MkParam));
	memset(handle, 0, sizeof(MkParam));
	handle->asset = AAssetManager_fromJava(env, asset);
	if(mode==1) { //Preview tracking
		handle->facialDetectHandle = tsDetectObject_create(TS_OT_FLAG_TK);
		handle->facialOutlineHandle = tsFacialOutline_create(TS_FO_TYPE_REALTIME);
		TsSize s = {160, 120};
  	tsDetectObject_setProperty(handle->facialDetectHandle, "face", "MaxSize", &s);
  	tsFacialOutline_setProperty(handle->facialOutlineHandle, "landmark-type", "cosmetic_rt");
	} else {			//Bitmap detect
		handle->facialDetectHandle = tsDetectObject_create();
		handle->facialOutlineHandle = tsFacialOutline_create(TS_FO_TYPE_FINE);
		TSFO_Size s = {1000, 1000};
		tsFacialOutline_setProperty(handle->facialOutlineHandle, "max-size", &s);
		tsFacialOutline_setProperty(handle->facialOutlineHandle, "landmark-type", "cosmetic");
	}

	tsFacialOutline_getProperty(handle->facialOutlineHandle, "landmarks-number",
			&nLandMarks);
	handle->landmarks = (TDouble *) malloc(2 * nLandMarks * sizeof(TDouble));
	handle->lm = (TPOINT*) malloc(nLandMarks * sizeof(TPOINT));
	for(int i = 0; i < nLandMarks; i++){
		handle->lm[i].x = 0;
		handle->lm[i].y = 0;
	}
	//Make Up initiation
	TMU_Init(&(handle->makeupHandle));

//	LOGI("TsMakeuprtEngine_init ----->nLandMarks = %d, handle=%d", nLandMarks,
//			(int )handle);
	return (jint) handle;
}
JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_uninit(JNIEnv* env,
		jobject obj, jint handle) {
//	LOGI("TsMakeuprtEngine_uninit 1<-----");
	MkParam* param = (MkParam*) handle;
	if (param->facialDetectHandle) {
//		LOGI("TsMakeuprtEngine_uninit 2<-----");
		tsDetectObject_destroy((param->facialDetectHandle));
		param->facialDetectHandle = 0;
	}
//	LOGI("TsMakeuprtEngine_uninit 3<-----");
	if (param->facialOutlineHandle) {
		tsFacialOutline_destroy((param->facialOutlineHandle));
		param->facialOutlineHandle = 0;
	}
	if(param->makeupHandle) {
		TMU_UnInit(param->makeupHandle);
		param->makeupHandle = 0;
	}
	if (param->landmarks) {
		free(param->landmarks);
		param->landmarks = 0;
	}
	if (param->lm) {
		free(param->lm);
		param->lm = 0;
	}
	if(param->gImageOs){
		destory_offscreen(param->gImageOs);
		param->gImageOs = 0;
	}
	ucamebeautifyUninit(param);
	free(param);
//	LOGI("TsMakeuprtEngine_uninit -----> ");
}
//<----use bmp detect
JNIEXPORT jobjectArray JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_jnifacedetect(
		JNIEnv* env, jobject obj, jint handle, jobject bmp, jobject face, jobject leye, jobject reye, jobject mouth){
	MkParam* param = (MkParam*) handle;
	if(param->gImageOs){
		destory_offscreen(param->gImageOs);
		param->gImageOs = 0;
	}
	param->gImageOs = create_offscreen(env, bmp, TS_PAF_NV21);
	TRECT* faceRect = 0;
	TPOINT* leyePoint = 0;
	TPOINT* reyePoint = 0;
	TPOINT* mouthPoint = 0;
	if(face) {
		jRect jR(env, face);
		faceRect = (TRECT*)malloc(sizeof(TRECT));
		faceRect->left = jR.getLeft();
		faceRect->top = jR.getTop();
		faceRect->right = jR.getRight();
		faceRect->bottom = jR.getBottom();
		LOGI("step faceRect=%d,%d,%d,%d", faceRect->left,faceRect->top,faceRect->right,faceRect->bottom);
	}
	if(leye) {
		jPoint jP(env, leye);
		leyePoint = (TPOINT*)malloc(sizeof(TPOINT));
		leyePoint->x = jP.getX();
		leyePoint->y = jP.getY();

	}
	if(reye) {
		jPoint jP(env, reye);
		reyePoint = (TPOINT*)malloc(sizeof(TPOINT));
		reyePoint->x = jP.getX();
		reyePoint->y = jP.getY();
	}
	if(mouth) {
		jPoint jP(env, mouth);
		mouthPoint = (TPOINT*)malloc(sizeof(TPOINT));
		mouthPoint->x = jP.getX();
		mouthPoint->y = jP.getY();
	}
	jobjectArray res = detectface(env, handle, param->gImageOs, faceRect, leyePoint, reyePoint, mouthPoint);
	facebeautify(param);
	if(faceRect) free(faceRect);
	return res;
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_facemakeuprtbmp__I_3Landroid_graphics_Point_2Landroid_graphics_Bitmap_2(
		JNIEnv* env, jobject obj, jint handle, jobjectArray jpointarr, jobject dstbmp) {
	AndroidBitmapInfo info;
	void* pPixels;
	ASSERT(AndroidBitmap_getInfo(env, dstbmp, &info)==ANDROID_BITMAP_RESUT_SUCCESS);
	ASSERT(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888);
	ASSERT(AndroidBitmap_lockPixels(env, dstbmp, &pPixels)== ANDROID_BITMAP_RESUT_SUCCESS);

	MkParam* param = (MkParam*) handle;
	int pointlength = env->GetArrayLength(jpointarr);
	jclass pointclass = env->FindClass("android/graphics/Point");
	jfieldID field_x = env->GetFieldID(pointclass, "x", "I");
	jfieldID field_y = env->GetFieldID(pointclass, "y", "I");
//	TPOINT* oldp = (TPOINT*) malloc(pointlength * sizeof(TPOINT));
	for (int i = 0; i < pointlength; i++) {
		jobject obj = env->GetObjectArrayElement(jpointarr, i);
		int p_x = env->GetIntField(obj, field_x);
		int p_y = env->GetIntField(obj, field_y);
//		oldp[i].x = param->lm[i].x;
//		oldp[i].y = param->lm[i].y;
		param->lm[i].x = p_x;
		param->lm[i].y = p_y;
	}
//	TSM_AdjustLipPoint(TNull,param->lm,oldp);
//	dumpToFile("/sdcard/facebeautify960x1280.nv21", (unsigned char*)((gImageOs->ppu8Plane)[0]), gImageOs->i32Width*gImageOs->i32Height*3/2);
	memcpy((param->gImageOs->ppu8Plane)[0], param->m_cbFrameBuf, param->gImageOs->i32Width*param->gImageOs->i32Height*3/2);
	clock_t start;
	start = clock();

//	char path[256];
//	sprintf(path,"/sdcard/zhl0_%dx%d.nv21", gImageOs->i32Width,gImageOs->i32Height);
//	dumpToFile(path, (unsigned char*)((gImageOs->ppu8Plane)[0]), gImageOs->i32Width*gImageOs->i32Height*3/2);
	TRESULT nres = TMU_MakeUpStyle(param->makeupHandle, param->gImageOs, param->gImageOs, param->lm,
			nLandMarks,TSM_STATIC_PIC, 0); //�����ױ����
	if (nres) {
		LOGI("TMU_MakeUpStyle err %d", nres);
	}
//	sprintf(path,"/sdcard/zhl1_%dx%d.nv21", gImageOs->i32Width,gImageOs->i32Height);
//	dumpToFile(path, (unsigned char*)((gImageOs->ppu8Plane)[0]), gImageOs->i32Width*gImageOs->i32Height*3/2);
	LOGI("TsMakeuprtEngine_facemakeuprt cost=%lf ms",
			(double )(clock() - start) * 1000 / CLOCKS_PER_SEC);
	NV21_to_RGBA8888((TUInt8*)((param->gImageOs->ppu8Plane)[0]), param->gImageOs->i32Width, param->gImageOs->i32Height, (TInt32)((param->gImageOs->pi32Pitch)[0]), (TUInt8*)pPixels, info.stride);
//	LOGI("NV21_to_RGBA8888 ------->");
//	sprintf(path,"/sdcard/zhl1_%dx%d.rgba", gImageOs->i32Width,gImageOs->i32Height);
//	dumpToFile(path, (unsigned char*)(pPixels), gImageOs->i32Width*gImageOs->i32Height*4);
	AndroidBitmap_unlockPixels(env, dstbmp);
}

//use bmp detect ---->


//<--- nv21 tracking
//int gIndex = 0;
JNIEXPORT jobjectArray JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_facetracking(
		JNIEnv* env, jobject obj, jint handle, jbyteArray nv21, jint width,
		jint height, jint rotate) {
	LOGI("Step1------------>");
	jbyte* nv21buff = env->GetByteArrayElements(nv21, 0);
	TSOFFSCREEN os = { 0 };
	os.u32PixelArrayFormat = TS_PAF_NV21;
	os.i32Width = width;
	os.i32Height = height;
	os.ppu8Plane[0] = (TUInt8*) nv21buff;
	os.ppu8Plane[1] = (TUInt8*) nv21buff + width * height;
	os.ppu8Plane[2] = os.ppu8Plane[3] = 0;
	os.pi32Pitch[0] = os.pi32Pitch[1] = width;
	LOGI("Step2------------>widthxheight=%dx%d", width, height);
	jobjectArray res = detectface(env, handle, &os, 0, 0, 0, 0, rotate);
	env->ReleaseByteArrayElements(nv21, nv21buff, 0);
	return res;
}
JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_makeuploadresource(JNIEnv* env, jobject obj, jint handle, jint part, jobject style, jboolean isStatic){
	MkParam* param = (MkParam*)handle;
	//load resource
//	jclass styleclass = env->FindClass("com/ts/engine/ResDataStyle");
//	jclass styleclass = env->GetObjectClass(style);
	jStyle nstyle(env, style);
	LOGI("AAsset param->asset=%X", (int)(param->asset));
	if(isStatic)
		TMU_MakeUpStyleLoad((param->makeupHandle), part, nstyle, TSM_STATIC_PIC, param->asset);
	else
		TMU_MakeUpStyleLoad((param->makeupHandle), part, nstyle, TSM_REAL_TIME_PREVIEW, param->asset);
}

JNIEXPORT void JNICALL Java_com_cam001_photoeditor_makeup_engine_TsMakeuprtEngine_facemakeuprt(
		JNIEnv* env, jobject obj, jint handle, jbyteArray imgarr, jint width,
		jint height, jobjectArray jpointarr) {
	LOGI("Into Java_com_ts_engine_TsMakeuprtEngine_facemakeuprt");
	clock_t start;
	start = clock();
	MkParam* param = (MkParam*) handle;
	jbyte* jpgbuff = env->GetByteArrayElements(imgarr, 0);
	int pointlength = env->GetArrayLength(jpointarr);
	jclass pointclass = env->FindClass("android/graphics/Point");
	jfieldID field_x = env->GetFieldID(pointclass, "x", "I");
	jfieldID field_y = env->GetFieldID(pointclass, "y", "I");
	for (int i = 0; i < pointlength; i++) {
		jobject obj = env->GetObjectArrayElement(jpointarr, i);
		int p_x = env->GetIntField(obj, field_x);
		int p_y = env->GetIntField(obj, field_y);
		param->lm[i].x = p_x;
		param->lm[i].y = p_y;
	}
	TSOFFSCREEN src = { 0 };
	src.i32Width = width;
	src.i32Height = height;
	src.pi32Pitch[0] = src.pi32Pitch[1] = width;
	src.ppu8Plane[0] = (TUInt8*) jpgbuff;
	src.ppu8Plane[1] = (TUInt8*) jpgbuff + width * height;
	src.ppu8Plane[2] = src.ppu8Plane[3] = 0;
	src.u32PixelArrayFormat = TS_PAF_NV21;
	TRESULT nres = TMU_MakeUpStyle(param->makeupHandle, &src, &src, param->lm,
			nLandMarks,TSM_REAL_TIME_PREVIEW, param->rotate); //ʵʱ��ױ����
	LOGI("Into Java_com_ts_engine_TsMakeuprtEngine_facemakeuprt step2");
	if (nres) {
		LOGI("TMU_MakeUpStyle err %d", nres);
	}
	LOGI("TsMakeuprtEngine_facemakeuprt cost=%lf ms",
			(double )(clock() - start) * 1000 / CLOCKS_PER_SEC);
	env->ReleaseByteArrayElements(imgarr, jpgbuff, 0);
}
//nv21 tracking---->

__attribute ((constructor)) void so_main()	{
#ifdef __DEBUG__
	if(!dateValidate()) abort();
#endif
}
