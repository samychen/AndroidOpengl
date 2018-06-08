/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tool.h"
#include "terror.h"
#include "makeup.h"
#include "tskindetect.h"
#include "facewhiten.h"
//#include "FaceDetection.h"
#include "ts-detect-object.h"
#include "TxSkinSmooth.h"
#include "tdeblemish.h"
#include "facewarp.h"
#include "TBilateraDenoise.h"
#include "sak-facial-track.h"
#include "jRect.h"
#include "tsFaceBeauty.h"
#include "ts_makeup_engine.h"
extern "C" SAK_EXPORTS void sakFacialTrack_get_benw_mask_from_landmarks(TSOFFSCREEN* mask, long* landmarks);



typedef struct _MakeupHandle
{
 THandle hMem;

 TSOFFSCREEN srcImg; // input YUV data
 TSOFFSCREEN dstTempImg[2]; // output YUV data, 锟斤拷锟斤拷锟皆硷拷锟皆硷拷锟斤拷幕锟斤拷锟斤拷锟斤拷锟揭拷锟斤拷锟斤拷诖锟斤拷锟斤拷锟斤拷锟��7
 TInt32 FaceNum;
 TInt32 FaceMaskScale;
 TRECT * pFaceRect;
 TPOINT * pEyePoint;
 TPOINT * pMouthPoint;
 TInt32 * pEyeRadius;
 TInt32* pMarks;
 TInt32* pEyeMarks;

 TMASK   FaceMask;
 TSOFFSCREEN	 featMask;

 THandle hFaceDection;
 THandle hSkinDetector;
 THandle hDeblemish;
 THandle hWarp;
 THandle hFacialMarks;
 TBool bFaceSkin;
	
	
} MakeupHandle;


#define SAFE_FREE(p) if (TNull != (p)) { TMemFree(hMem, p); (p) = TNull; }

#ifdef DEBUG
long tcost1, tcost2;
#endif
 
TBool CreateNV21ImageSpace(THandle hMem, TSOFFSCREEN* pSrcImg, TSOFFSCREEN* pDstImg){
      
	pDstImg->i32Width = pSrcImg->i32Width&(~7);
	pDstImg->i32Height = pSrcImg->i32Height&(~7);
	pDstImg->pi32Pitch[0] = pDstImg->i32Width;
    pDstImg->pi32Pitch[1] = pDstImg->pi32Pitch[2] = pDstImg->i32Width;
	pDstImg->u32PixelArrayFormat = TS_PAF_NV21;


	LOGE("[Ln%d] CreateNV21ImageSpace\n", __LINE__);

	if(pDstImg->ppu8Plane[0] != TNull)
		TMemFree(hMem, pDstImg->ppu8Plane[0]); //锟斤拷锟斤拷图锟脚碉拄1�7

	pDstImg->ppu8Plane[0] = TNull;

	LOGE("[Ln%d] CreateNV21ImageSpace\n", __LINE__);
	LOGE("pDstImg->pi32Pitch[0]: %d\n", pDstImg->pi32Pitch[0]);
	LOGE("pDstImg->i32Height: %d\n", pDstImg->i32Height);
	LOGE("pDstImg->pi32Pitch[1]: %d\n",pDstImg->pi32Pitch[1]);
	 


	pDstImg->ppu8Plane[0] = (TUInt8* )TMemAlloc(hMem, pDstImg->pi32Pitch[0]*pDstImg->i32Height+pDstImg->pi32Pitch[1]*pDstImg->i32Height/2);
    if(pDstImg->ppu8Plane[0] == TNull)
		return TFalse;
	
	LOGE("[Ln%d] CreateNV21ImageSpace\n", __LINE__);


	pDstImg->ppu8Plane[1] = pDstImg->ppu8Plane[2] = pDstImg->ppu8Plane[0]+pDstImg->pi32Pitch[0]*pDstImg->i32Height;


	return TTrue;
    

}





TInt32 makeup_init( /* return TOK if success, otherwise fail */
				   THandle hMem,	      /* [in] memory handle */
                  THandle *pHandle)  /* [out] the address of engine handle */
{
	LOGI("makeup_init <-----");
	int res;

	if (TNull == pHandle) 
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}
   MakeupHandle* handle = (MakeupHandle *)TMemAlloc(hMem, sizeof(MakeupHandle));
   if(handle == TNull)
   {

	   LOGE("[Ln%d] memory failed\n", __LINE__);
    	return TERR_NO_MEMORY;
   }
   TMemSet(handle, 0, sizeof(MakeupHandle));
   handle->hMem = hMem ;
   handle->pFaceRect = (TRECT *)TMemAlloc(hMem, sizeof(TRECT)*MAEUP_MAX_FACE);
   handle->pEyePoint = (TPOINT *)TMemAlloc(hMem, sizeof(TPOINT)*MAEUP_MAX_FACE*2);
   handle->pMouthPoint = (TPOINT *)TMemAlloc(hMem, sizeof(TPOINT)*MAEUP_MAX_FACE);
   handle->pEyeRadius = (TInt32 *)TMemAlloc(hMem, sizeof(TInt32)*MAEUP_MAX_FACE);
   TMemSet(handle->pEyeRadius, 0, sizeof(TInt32)*MAEUP_MAX_FACE);
   handle->pMarks = (TInt32*)TMemAlloc(hMem, kSAK_FACIAL_TRACK_NUM*2*sizeof(TInt32));
   TMemSet(handle->pMarks, 0, kSAK_FACIAL_TRACK_NUM*2*sizeof(TInt32));
   handle->pEyeMarks = (TInt32*)TMemAlloc(hMem, 16*sizeof(TInt32));
   TMemSet(handle->pEyeMarks, 0, 16*sizeof(TInt32));
   // FD intial 
  /* if(res = InitFDData(hMem,  &(handle->hFaceDection)) != TOK)
	  {
	   LOGE("[Ln%d] InitFDData falied, return %d \n", __LINE__, res);
	   SAFE_FREE(handle);
	   return res;
	  }
   */

   handle->hFaceDection = sakDetectObject_create();
   if( handle->hFaceDection == TNull)
   {
	   LOGE("[Ln%d] InitFDData falied, return %d \n", __LINE__, res);
	   SAFE_FREE(handle);
	   return 1;
   }
   handle->hFacialMarks = NULL;
    // 锟斤拷色锟斤拷锟��7
//    if(res = InitSkinDetector(hMem, &(handle->hSkinDetector)) != TOK)
//    {
// 	   LOGE("[Ln%d] InitSkinDetector falied", __LINE__);
// 	   SAFE_FREE(handle);
// 	   return res;
//    }


   //锟斤拷锟��7
   if(InitDeblemish(hMem, &(handle->hDeblemish)) != TOK)
   {
         LOGE("[Ln%d] InitDeblemish failed\n", __LINE__);
         SAFE_FREE(handle);
    	 return TERR_UNKNOWN;
	}
   
   *pHandle = handle;

   LOGI("makeup_init ----->");
   return TOK;
}


TInt32 makeup_replace_image( /* return TOK if success, otherwise fail */
							THandle hHandle,          /* [in] engine handle */ 
							LPTSOFFSCREEN pImage,		/* [in] input image, only support TS_PAF_RGB24_R8G8B8/TS_PAF_RGB32_R8G8B8A8  */
							TInt32 *pFaceNum,
							TRECT  *pFaceRect,        /* [in] face rectangle array*/ 
							TPOINT  *pEyePoint,         /* [in] eye rectangle array*/ 
							TPOINT  *pMouthPoint,		/* [in] mouth rectangle array*/
							TInt32  *pMarks,
							TInt32  *pEyeMarks)
   
{


	LOGI("makeup_replace_image <-----");
	MakeupHandle* handle = (MakeupHandle*)hHandle;
	int width =  pImage->i32Width;
	int height = pImage->i32Height;
	int facenum = 0;
	TLong lScale = 0, lMaxV, lMaskW, lMaskH;
	int i;
    TRESULT res;

	if (TNull == hHandle || TNull == pImage || TNull == pFaceNum || TNull == pFaceRect ||TNull == pFaceRect || TNull == pFaceRect) 
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
        LOGE("hHandle = %d",hHandle);
		LOGE("pImage = %d",pImage);
		LOGE("pFaceNum = %d",pFaceNum);
		LOGE("pFaceRect = %d",pFaceRect);
		LOGE("pFaceRect = %d",pFaceRect);
		LOGE("pFaceRect = %d",pFaceRect);
		return TERR_INVALID_PARAM;
	}
		LOGE("pFaceNum = %d",&pFaceNum);
		LOGE("pFaceRect = %d,%d,%d,%d",pFaceRect->left,pFaceRect->top,pFaceRect->right,pFaceRect->bottom);
		LOGE("pEyePoint = %d,%d",pEyePoint->x,pEyePoint->y);
		LOGE("pMouthPoint = %d,%d",pMouthPoint->x,pMouthPoint->y);


	if(pImage->u32PixelArrayFormat != TS_PAF_RGB24_R8G8B8 && pImage->u32PixelArrayFormat != TS_PAF_RGB32_R8G8B8A8)
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}

	 

     LOGE("[Ln%d]Before create img\n", __LINE__);

	//Convert Image
	CreateNV21ImageSpace(handle->hMem, pImage, &(handle->srcImg));
	if(handle->srcImg.ppu8Plane[0] == TNull)
	{
		LOGE("[Ln%d] memory failed\n", __LINE__);
    	return TERR_NO_MEMORY;
	}
	LOGE("[Ln%d]Do create img\n", __LINE__);

	CreateNV21ImageSpace(handle->hMem, pImage, &(handle->dstTempImg[0]));
	if(handle->dstTempImg[0].ppu8Plane[0] == TNull)
	{
		LOGE("[Ln%d] memory failed\n", __LINE__);
			return TERR_NO_MEMORY;
	}

 
    LOGE("[Ln%d]Before color convert\n", __LINE__);

    if(pImage->u32PixelArrayFormat == TS_PAF_RGB24_R8G8B8)
	   RGB888_to_NV21(pImage->ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.ppu8Plane[1],
                      pImage->i32Width, pImage->i32Height, pImage->pi32Pitch[0], handle->srcImg.i32Width,  handle->srcImg.i32Height);
    if(pImage->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8)
	   RGBA8888_to_NV21(pImage->ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.ppu8Plane[1],
                      pImage->i32Width, pImage->i32Height, pImage->pi32Pitch[0], handle->srcImg.i32Width,  handle->srcImg.i32Height);


   LOGE("[Ln%d] After color convert\n", __LINE__);

	//FD
// 	{
// 		 char name[64];
// 		 sprintf(name, "/sdcard/device_%d_%d.NV21", handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height);
//          savefile( handle->srcImg.ppu8Plane[0], handle->srcImg.pi32Pitch[0]*handle->srcImg.i32Height*3/2, name);
//   
// 	}
	

	/*DetectedFaceInfo faceinfo = RAFaceDetection(handle->hMem, handle->hFaceDection,  8, 
					 handle->srcImg.ppu8Plane[0], handle->srcImg.i32Width, handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height );
    */

//#ifdef DEBUG
//   tcost1 = gettime();
//#endif
   //facenum = sakFaceDetect_detect(handle->hFaceDection, &handle->srcImg);
	facenum = *pFaceNum;
//#ifdef DEBUG
//   tcost2 = gettime();
//   LOGE("sakFaceDetect_detect cost %d \n", tcost2-tcost1); 
//#endif


	if(facenum > MAEUP_MAX_FACE)
		facenum = MAEUP_MAX_FACE;

	LOGE("[Ln%d] facenum = %d\n", __LINE__, facenum);


	handle->FaceNum = facenum;
	if(facenum == 0)
	{
    	 return TOK;

	}

    for(i=0; i<facenum; i++)
	{

     handle->pFaceRect[i].left = pFaceRect[i].left;
     handle->pFaceRect[i].right = pFaceRect[i].right;
	   handle->pFaceRect[i].top = pFaceRect[i].top;
	   handle->pFaceRect[i].bottom = pFaceRect[i].bottom;
    
	   handle->pEyePoint[2*i].x = pEyePoint[2*i].x;
	   handle->pEyePoint[2*i].y = pEyePoint[2*i].y;

	   handle->pEyePoint[2*i+1].x = pEyePoint[2*i+1].x;
	   handle->pEyePoint[2*i+1].y = pEyePoint[2*i+1].y;

	   if(handle->pEyeRadius[2*i] == 0)
		   handle->pEyeRadius[2*i] = (handle->pEyePoint[2*i+1].x - handle->pEyePoint[2*i].x)/2;
	   if(handle->pEyeRadius[2*i] == 0)
		   handle->pEyeRadius[2*i+1] = (handle->pEyePoint[2*i+1].x - handle->pEyePoint[2*i].x)/2;

	   handle->pMouthPoint[i].x = pMouthPoint[i].x;
	   handle->pMouthPoint[i].y = pMouthPoint[i].y;


	}

 

	/*for(i=0; i<facenum; i++)
	{
		pEyePoint[2*i].x = handle->pEyePoint[2*i].x = 287;
		pEyePoint[2*i].y = handle->pEyePoint[2*i].y = 303;
	
		pEyePoint[2*i+1].x  = handle->pEyePoint[2*i+1].x = 414;
		pEyePoint[2*i+1].y =  handle->pEyePoint[2*i+1].y = 303;
	 
			
	}

	for(i=0; i<facenum; i++)
	{
	    pMouthPoint[i].x = handle->pMouthPoint[i].x = 355;
        pMouthPoint[i].y = handle->pMouthPoint[i].y = 438;
		
	}



	if(facenum > 0)
	{
		int i;
		for(i=0; i<facenum; i++)
			LOGE("[%d] face: [%d, %d, %d, %d]\n", i, pFaceRect[i].left, pFaceRect[i].top, pFaceRect[i].right, pFaceRect[i].bottom );
	}


	TMemFree(handle->hMem, faceinfo.DetectedFace);
 */
  
 	//FACE MASK 
    

	if(handle->FaceMask.pData != TNull)
		TMemFree(handle->hMem, handle->FaceMask.pData);

	handle->FaceMask.pData = TNull;

	lMaxV = (handle->srcImg.i32Width > handle->srcImg.i32Height) ? handle->srcImg.i32Width : handle->srcImg.i32Height;
	while(lMaxV > 80)
	{
		lMaxV >>= 1;
		lScale++;
	}
	lMaskW = handle->srcImg.i32Width >> lScale;
	lMaskH = handle->srcImg.i32Height >> lScale;
    handle->FaceMaskScale = lScale;

	LOGE("[Ln%d] lMaskW =%d, lMaskH=%d\n", __LINE__, lMaskW, lMaskH);


	handle->FaceMask.lWidth    = lMaskW;
	handle->FaceMask.lHeight   = lMaskH;
	handle->FaceMask.lMaskLine = (lMaskW + 3) & 0xfffffffc;
	handle->FaceMask.pData     = (TByte*)TMemAlloc(handle->hMem, handle->FaceMask.lMaskLine*handle->FaceMask.lHeight);
	if(handle->FaceMask.pData == TNull)
	{
      
        LOGE("[Ln%d] handle->FaceMask.pData = TNUll\n", __LINE__);
    	 return TERR_NO_MEMORY;
	}

 
 
	{

		LOGE("[Ln%d] Before InitSkinDetector handle->hMem=%d %d\n",handle->hMem,__LINE__);
		if(res = InitSkinDetector(handle->hMem, &(handle->hSkinDetector)) != TOK)
		{
			LOGE("[Ln%d] InitSkinDetector falied \n", __LINE__);
			
		//	return res;
		}else{
			LOGE("[Ln%d] Before DoSkinDetect\n", __LINE__);
			int size = handle->srcImg.i32Width*handle->srcImg.i32Height*3/2;
			char path[256];
			sprintf(path, "/sdcard/zhl_%dx%d.nv21",handle->srcImg.i32Width,handle->srcImg.i32Height);
			savefile(handle->srcImg.ppu8Plane[0],size, path);
			if(res = DoSkinDetect(handle->hSkinDetector, &(handle->srcImg), handle->pFaceRect, 
				handle->FaceNum , &(handle->FaceMask)) != TOK)
			{
				LOGE("[Ln%d] DoSkinDetect = failed, res =%d\n", __LINE__, res);
				TMemSet(handle->FaceMask.pData, 0, handle->FaceMask.lMaskLine*handle->FaceMask.lHeight);
				handle->bFaceSkin = TFalse;
			//	return res;
			}else{
				LOGE("[Ln%d] DoSkinDetect OK\n", __LINE__);
				handle->bFaceSkin = TTrue;

				LOGE("[Ln%d] Before UninitSkinDetector\n", __LINE__);
				UninitSkinDetector(&(handle->hSkinDetector));

				 TxMask mask;
				 mask.nWidth = handle->FaceMask.lWidth;
				 mask.nHeight = handle->FaceMask.lHeight;
				 mask.nPitch = handle->FaceMask.lMaskLine;
				 mask.pData = handle->FaceMask.pData;
				TxMaskSmooth(&mask, 2);
			}
		}
		
	
	}

	{
		TPOINT facekp[3];
		facekp[0].x = handle->pEyePoint[0].x;
		facekp[0].y = handle->pEyePoint[0].y;
		facekp[1].x = handle->pEyePoint[1].x;
		facekp[1].y = handle->pEyePoint[1].y;
		facekp[2].x = handle->pMouthPoint[0].x;
		facekp[2].y = handle->pMouthPoint[0].y;
		if(handle->hWarp != NULL)
			TS_FaceWarp_Uninit(handle->hWarp);

		LOGE("facekp[0].x=%d\n", facekp[0].x);
		LOGE("facekp[0].y=%d\n", facekp[0].y);
		LOGE("facekp[1].x=%d\n", facekp[1].x);
		LOGE("facekp[1].y=%d\n", facekp[1].y);
		LOGE("facekp[2].x=%d\n", facekp[2].x);
		LOGE("facekp[2].y=%d\n", facekp[2].y);


		 LOGE("TS_FaceWarp_Init handle->hWarp=%X handle->hMem=%X handle->dstTempImg[0]=%X",handle->hWarp,handle->hMem,handle->dstTempImg[0]);
		res = TS_FaceWarp_Init(&(handle->hWarp),handle->hMem,&(handle->dstTempImg[0]),facekp);	
		if (TERR_NONE != res)
		{
			LOGE("TS_FaceWarp_Init failed\n");
			return res;
		 }
	}
       LOGE("TS_FaceWarp_Init OK");

//       if(pMarks!=NULL) {
//    	   TMemCpy(handle->pMarks, pMarks, kSAK_FACIAL_TRACK_NUM*2*sizeof(TInt32));
//
//           if(handle->featMask.ppu8Plane[0]!=NULL) {
//        	   TMemFree(handle->hMem, handle->featMask.ppu8Plane[0]);
//        	   handle->featMask.ppu8Plane[0]=NULL;
//           }
//           handle->featMask.u32PixelArrayFormat = TS_PAF_GRAY;
//           handle->featMask.i32Width = handle->srcImg.i32Width;
//           handle->featMask.i32Height = handle->srcImg.i32Height;
//           handle->featMask.pi32Pitch[0] = handle->srcImg.i32Width;
//           handle->featMask.ppu8Plane[0] = (TUInt8*)TMemAlloc(handle->hMem, handle->featMask.pi32Pitch[0]*handle->featMask.i32Height);
//           sakFacialTrack_get_benw_mask_from_landmarks(&(handle->featMask), pMarks);
//       }
//       if(pEyeMarks!=NULL) {
//    	   TMemCpy(handle->pEyeMarks, pEyeMarks, 16*sizeof(TInt32));
//       }
 
       LOGI("makeup_replace_image ----->");
	 return TOK;
}


TInt32 makeup_replace_image_withoutsd( /* return TOK if success, otherwise fail */
							THandle hHandle,          /* [in] engine handle */ 
							LPTSOFFSCREEN pImage,		/* [in] input image, only support TS_PAF_RGB24_R8G8B8/TS_PAF_RGB32_R8G8B8A8  */
							TInt32 *pFaceNum,
							TRECT  *pFaceRect,        /* [in] face rectangle array*/ 
							TPOINT  *pEyePoint,         /* [in] eye rectangle array*/ 
							TPOINT  *pMouthPoint,		/* [in] mouth rectangle array*/
							TInt32  *pMarks,
							TInt32  *pEyeMarks)

   
{
	LOGI("makeup_replace_image_withoutsd <-----");
	MakeupHandle* handle = (MakeupHandle*)hHandle;
	int width =  pImage->i32Width;
	int height = pImage->i32Height;
	int facenum = 0;
	TLong lScale = 0, lMaxV, lMaskW, lMaskH;
	int i;
    TRESULT res;

	if (TNull == hHandle || TNull == pImage || TNull == pFaceNum || TNull == pFaceRect ||TNull == pFaceRect || TNull == pFaceRect) 
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
        LOGE("hHandle = %d",hHandle);
		LOGE("pImage = %d",pImage);
		LOGE("pFaceNum = %d",pFaceNum);
		LOGE("pFaceRect = %d",pFaceRect);
		LOGE("pFaceRect = %d",pFaceRect);
		LOGE("pFaceRect = %d",pFaceRect);
		return TERR_INVALID_PARAM;
	}
		LOGE("pFaceNum = %d",&pFaceNum);
		LOGE("pFaceRect = %d,%d,%d,%d",pFaceRect->left,pFaceRect->top,pFaceRect->right,pFaceRect->bottom);
		LOGE("pEyePoint = %d,%d",pEyePoint->x,pEyePoint->y);
		LOGE("pMouthPoint = %d,%d",pMouthPoint->x,pMouthPoint->y);


	if(pImage->u32PixelArrayFormat != TS_PAF_RGB24_R8G8B8 && pImage->u32PixelArrayFormat != TS_PAF_RGB32_R8G8B8A8)
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}

	 

     LOGE("[Ln%d]Before create img\n", __LINE__);

	//Convert Image
	CreateNV21ImageSpace(handle->hMem, pImage, &(handle->srcImg));
	if(handle->srcImg.ppu8Plane[0] == TNull)
	{
		LOGE("[Ln%d] memory failed\n", __LINE__);
    	return TERR_NO_MEMORY;
	}
	LOGE("[Ln%d]Do create img\n", __LINE__);

	CreateNV21ImageSpace(handle->hMem, pImage, &(handle->dstTempImg[0]));
	if(handle->dstTempImg[0].ppu8Plane[0] == TNull)
	{
		LOGE("[Ln%d] memory failed\n", __LINE__);
			return TERR_NO_MEMORY;
	}

 
    LOGE("[Ln%d]Before color convert\n", __LINE__);

    if(pImage->u32PixelArrayFormat == TS_PAF_RGB24_R8G8B8)
	   RGB888_to_NV21(pImage->ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.ppu8Plane[1],
                      pImage->i32Width, pImage->i32Height, pImage->pi32Pitch[0], handle->srcImg.i32Width,  handle->srcImg.i32Height);
    if(pImage->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8)
	   RGBA8888_to_NV21(pImage->ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.ppu8Plane[1],
                      pImage->i32Width, pImage->i32Height, pImage->pi32Pitch[0], handle->srcImg.i32Width,  handle->srcImg.i32Height);


   LOGE("[Ln%d] After color convert\n", __LINE__);

	//FD
// 	{
// 		 char name[64];
// 		 sprintf(name, "/sdcard/device_%d_%d.NV21", handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height);
//          savefile( handle->srcImg.ppu8Plane[0], handle->srcImg.pi32Pitch[0]*handle->srcImg.i32Height*3/2, name);
//   
// 	}
	

	/*DetectedFaceInfo faceinfo = RAFaceDetection(handle->hMem, handle->hFaceDection,  8, 
					 handle->srcImg.ppu8Plane[0], handle->srcImg.i32Width, handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height );
    */

//#ifdef DEBUG
//   tcost1 = gettime();
//#endif
   //facenum = sakFaceDetect_detect(handle->hFaceDection, &handle->srcImg);
	facenum = *pFaceNum;
//#ifdef DEBUG
//   tcost2 = gettime();
//   LOGE("sakFaceDetect_detect cost %d \n", tcost2-tcost1); 
//#endif


	if(facenum > MAEUP_MAX_FACE)
		facenum = MAEUP_MAX_FACE;

	LOGE("[Ln%d] facenum = %d\n", __LINE__, facenum);


	handle->FaceNum = facenum;
	if(facenum == 0)
	{
    	 return TOK;

	}

    for(i=0; i<facenum; i++)
	{

     handle->pFaceRect[i].left = pFaceRect[i].left;
     handle->pFaceRect[i].right = pFaceRect[i].right;
	   handle->pFaceRect[i].top = pFaceRect[i].top;
	   handle->pFaceRect[i].bottom = pFaceRect[i].bottom;
    
	   handle->pEyePoint[2*i].x = pEyePoint[2*i].x;
	   handle->pEyePoint[2*i].y = pEyePoint[2*i].y;

	   handle->pEyePoint[2*i+1].x = pEyePoint[2*i+1].x;
	   handle->pEyePoint[2*i+1].y = pEyePoint[2*i+1].y;

	   if(handle->pEyeRadius[2*i] == 0)
		   handle->pEyeRadius[2*i] = (handle->pEyePoint[2*i+1].x - handle->pEyePoint[2*i].x)/2;
	   if(handle->pEyeRadius[2*i] == 0)
		   handle->pEyeRadius[2*i+1] = (handle->pEyePoint[2*i+1].x - handle->pEyePoint[2*i].x)/2;

	   handle->pMouthPoint[i].x = pMouthPoint[i].x;
	   handle->pMouthPoint[i].y = pMouthPoint[i].y;


	}

 

	/*for(i=0; i<facenum; i++)
	{
		pEyePoint[2*i].x = handle->pEyePoint[2*i].x = 287;
		pEyePoint[2*i].y = handle->pEyePoint[2*i].y = 303;
	
		pEyePoint[2*i+1].x  = handle->pEyePoint[2*i+1].x = 414;
		pEyePoint[2*i+1].y =  handle->pEyePoint[2*i+1].y = 303;
	 
			
	}

	for(i=0; i<facenum; i++)
	{
	    pMouthPoint[i].x = handle->pMouthPoint[i].x = 355;
        pMouthPoint[i].y = handle->pMouthPoint[i].y = 438;
		
	}



	if(facenum > 0)
	{
		int i;
		for(i=0; i<facenum; i++)
			LOGE("[%d] face: [%d, %d, %d, %d]\n", i, pFaceRect[i].left, pFaceRect[i].top, pFaceRect[i].right, pFaceRect[i].bottom );
	}


	TMemFree(handle->hMem, faceinfo.DetectedFace);
 */
  
 
	

	{
		TPOINT facekp[3];
		facekp[0].x = handle->pEyePoint[0].x;
		facekp[0].y = handle->pEyePoint[0].y;
		facekp[1].x = handle->pEyePoint[1].x;
		facekp[1].y = handle->pEyePoint[1].y;
		facekp[2].x = handle->pMouthPoint[0].x;
		facekp[2].y = handle->pMouthPoint[0].y;
		if(handle->hWarp != NULL)
			TS_FaceWarp_Uninit(handle->hWarp);

		LOGE("facekp[0].x=%d\n", facekp[0].x);
		LOGE("facekp[0].y=%d\n", facekp[0].y);
		LOGE("facekp[1].x=%d\n", facekp[1].x);
		LOGE("facekp[1].y=%d\n", facekp[1].y);
		LOGE("facekp[2].x=%d\n", facekp[2].x);
		LOGE("facekp[2].y=%d\n", facekp[2].y);


		 LOGE("TS_FaceWarp_Init handle->hWarp=%X handle->hMem=%X handle->dstTempImg[0]=%X",handle->hWarp,handle->hMem,handle->dstTempImg[0]);
		res = TS_FaceWarp_Init(&(handle->hWarp),handle->hMem,&(handle->dstTempImg[0]),facekp);	
		if (TERR_NONE != res)
		{
			LOGE("TS_FaceWarp_Init failed\n");
			return res;
		 }
	}
       LOGE("TS_FaceWarp_Init OK");

       if(pMarks!=NULL) {
    	   TMemCpy(handle->pMarks, pMarks, kSAK_FACIAL_TRACK_NUM*2*sizeof(TInt32));
       }
       if(pEyeMarks!=NULL) {
    	   TMemCpy(handle->pEyeMarks, pEyeMarks, 16*sizeof(TInt32));
       }
 
       LOGI("makeup_replace_image_withoutsd ----->");
	 return TOK;
 


}

TInt32 makeup_effect( /* return TOK if success, otherwise fail */
					 THandle hHandle,         /* [in] engine handle */  
					 MakeupFeatureParam* pParam,  /* [in] parameters */  
                     LPTSOFFSCREEN pImage) /* [out] output image, same content but different size as loaded image */ 
{

	MakeupHandle* handle = (MakeupHandle*)hHandle;
    TRESULT res;

    LOGI("makeup_effect <-----");
	if (TNull == hHandle || TNull == pImage || TNull == pParam ) 
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}
	TSMakeupData pIn, pOut;
	TRECT rcFace;
	int frameLen=(handle->srcImg).i32Width*(handle->srcImg).i32Height;
	pIn.frameWidth = (handle->srcImg).i32Width;
	pIn.frameHeight = (handle->srcImg).i32Height;
	pIn.yBuf = (handle->srcImg).ppu8Plane[0];
	pIn.uvBuf = (handle->srcImg).ppu8Plane[0] + frameLen;

	frameLen =(handle->dstTempImg[0]).i32Width*(handle->dstTempImg[0]).i32Height;
	pOut.frameWidth = (handle->dstTempImg[0]).i32Width;
	pOut.frameHeight = (handle->dstTempImg[0]).i32Height;
	pOut.yBuf = (handle->dstTempImg[0]).ppu8Plane[0];
	pOut.uvBuf = (handle->dstTempImg[0]).ppu8Plane[0] + frameLen;
	rcFace.left = (handle->pFaceRect)->left;
	rcFace.right = (handle->pFaceRect)->right;
	rcFace.top = (handle->pFaceRect)->top;
	rcFace.bottom = (handle->pFaceRect)->bottom;


	if(handle->FaceNum == 0){
		LOGE("[Ln%d] FaceNum = 0\n", __LINE__);
	}
	if(handle->srcImg.ppu8Plane == TNull){
		LOGE("[Ln%d] srcImg.ppu8Plane = NULll\n", __LINE__);
	}
	if(handle->dstTempImg[0].ppu8Plane == TNull){
		LOGE("[Ln%d] dstTempImg[0].ppu8Plane = NULL\n", __LINE__);
	}

	if (handle->FaceNum == 0 || handle->srcImg.ppu8Plane == TNull || handle->dstTempImg[0].ppu8Plane == TNull) 
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}


    if(pImage->u32PixelArrayFormat != TS_PAF_RGB24_R8G8B8 && pImage->u32PixelArrayFormat != TS_PAF_RGB32_R8G8B8A8)
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}

    LOGE("[Ln%d] makeup_effect\n", __LINE__);  
 
#ifdef DEBUG
	{
 
		 char name[64];
		 static int num = 0;
		// num ++;
#ifdef WIN32
         sprintf(name, "d:\\/%d_%d_%d.NV21", num, handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height);
#else
		 sprintf(name, "/sdcard/%d_%d_%d.NV21", num, handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height);

#endif
//		 LOGE(name);
 //	 	 savefile( handle->srcImg.ppu8Plane[0], handle->srcImg.pi32Pitch[0]*handle->srcImg.i32Height*3/2, name);
		 LOGE("handle->srcImg.ppu8Plane[0] = 0x%x\n", handle->srcImg.ppu8Plane[0]); 
		 LOGE("SkinClean_Enable = %d\n", pParam->SkinClean_Enable); 
		 LOGE("SkinCleanLevel = %d\n", pParam->SkinCleanLevel); 
		 LOGE("SkinWhiten_Enable = %d\n", pParam->SkinWhiten_Enable); 
	     LOGE("SkinWhiteningLevel = %d\n", pParam->SkinWhiteningLevel); 
		 LOGE("pParam->DeBlemish_Enable = %d\n", pParam->DeBlemish_Enable); 
		 if(pParam->DeBlemish_Enable == 1)
		 {
			 LOGE("pDeblemishParaNum = %d\n", pParam->pDeblemishParaNum[0]); 
			 for(int i=0; i<pParam->pDeblemishParaNum[0]; i++)
			 {
				 LOGE("\t %d point,level %d, [%d, %d] \n", pParam->pDeblemishPara[i].deblemishRadius, pParam->pDeblemishPara[i].selectpoint.x,
					 pParam->pDeblemishPara[i].selectpoint.y); 
			 }
			 
		 }
		 

	}
#endif

	// 锟斤拷锟斤拷顺锟斤拷 锟斤拷锟斤拷

	// copy 丄1�7锟斤拄1�7
	TMemCpy(handle->dstTempImg[0].ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.pi32Pitch[0]*handle->srcImg.i32Height+
 		handle->srcImg.pi32Pitch[1]*handle->srcImg.i32Height/2);

	  LOGE("[Ln%d] makeup_effect\n", __LINE__);  

	 // 0: 锟斤拷锟斤拷 warp

	  if(pParam->TrimFace_Enable == TFalse)
	  {

		  TS_WarpFace_Reset(handle->hWarp);
	  }

	// 1 磨皮

	if(pParam->SkinClean_Enable == TTrue && handle->bFaceSkin != TFalse)
	{
        //锟斤拷锟斤拷
		//
		  LOGE("[Ln%d] Makeup Before TsEffectSkinDenoise, level %d\n", __LINE__, pParam->SkinCleanLevel);  
  		  LOGE("FaceMask.pData: %x\n", handle->FaceMask.pData);  
  		  LOGE("FaceMask.lWidth: %d\n",  handle->FaceMask.lWidth);  
  		  LOGE("FaceMask.lHeight: %d\n", handle->FaceMask.lHeight);  
  		  LOGE("FaceMask.lMaskLine: %d\n",  handle->FaceMask.lMaskLine);  
         LOGE("pFaceRect:[%d, %d, %d, %d] \n",  handle->pFaceRect->left, handle->pFaceRect->top, handle->pFaceRect->right, handle->pFaceRect->bottom);  
		
		 int level = pParam->SkinCleanLevel;

		 if(false) { //(level < 60){
			 TxImage src, dst;
			 TxMask mask;
			 TxRect rect = {0, 0, 0, 0};
			 src.ePixfmt = TXE_PIX_FMT_NV21;
			 src.nWidth = handle->srcImg.i32Width;
			 src.nHeight = handle->srcImg.i32Height;
			 src.pPlane[0] = handle->srcImg.ppu8Plane[0];
			 src.pPlane[1] = handle->srcImg.ppu8Plane[1];
			 src.nPitch[0] = handle->srcImg.pi32Pitch[0];
			 src.nPitch[1] = handle->srcImg.pi32Pitch[1];
			 dst.ePixfmt = TXE_PIX_FMT_NV21;
			 dst.nWidth = handle->dstTempImg[0].i32Width;
			 dst.nHeight = handle->dstTempImg[0].i32Height;
			 dst.pPlane[0] = handle->dstTempImg[0].ppu8Plane[0];
			 dst.pPlane[1] = handle->dstTempImg[0].ppu8Plane[1];
			 dst.nPitch[0] = handle->dstTempImg[0].pi32Pitch[0];
			 dst.nPitch[1] = handle->dstTempImg[0].pi32Pitch[1];
			 mask.nWidth = handle->FaceMask.lWidth;
			 mask.nHeight = handle->FaceMask.lHeight;
			 mask.nPitch = handle->FaceMask.lMaskLine;
			 mask.pData = handle->FaceMask.pData;
			 
			 TxMask featMask;
			 featMask.nWidth = handle->featMask.i32Width;
			 featMask.nHeight = handle->featMask.i32Height;
			 featMask.nPitch = handle->featMask.i32Width;
			 featMask.pData = handle->featMask.ppu8Plane[0];

			 if(res = TxSkinSmooth(&src, &dst, &mask, &featMask, level) != TOK) 
			 {
				 LOGE("[Ln%d] Makeup TxSkinSmooth = failed\n", __LINE__);
				 return res;
			 }
		 }else
		 {

#ifdef DEBUG
			 tcost1 = gettime();
#endif
			 //mopi
			 if(res = ts_makeup_skin_clean(&pIn, &pOut, &rcFace, level) != TOK)
			 {
				 
				 LOGE("[Ln%d] Makeup ts_makeup_skin_clean = failed,res = %d\n", __LINE__, res);
				 return res;
			 }
			 
#ifdef DEBUG
			 tcost2 = gettime();
			 LOGE("TShzFaceBeautify cost %d \n", tcost2-tcost1);
#endif
		 }
	 
		LOGE("[Ln%d] Makeup TsEffectSkinDenoise = OK \n", __LINE__ );
	}
// 	
// 
// 	// 2 锟斤拷锟斤拷meibai
	if(pParam->SkinWhiten_Enable == TTrue && handle->bFaceSkin != TFalse){
		TRECT lefteye, righteye;
		lefteye.left = handle->pEyePoint[0].x-handle->pEyeRadius[0];
		lefteye.right = handle->pEyePoint[0].x+handle->pEyeRadius[0];
		lefteye.top = handle->pEyePoint[0].y-handle->pEyeRadius[0];
		lefteye.bottom = handle->pEyePoint[0].y+handle->pEyeRadius[0];

		righteye.left = handle->pEyePoint[1].x-handle->pEyeRadius[1];
		righteye.right = handle->pEyePoint[1].x+handle->pEyeRadius[1];
		righteye.top = handle->pEyePoint[1].y-handle->pEyeRadius[1];
		righteye.bottom = handle->pEyePoint[1].y+handle->pEyeRadius[1];
	
 	     LOGE("[Ln%d] Makeup Before TFaceWhitening, level: %d\n", __LINE__, pParam->SkinWhiteningLevel);  
		if(res = ts_makeup_Bskin_Beauty(&pOut,
				pParam->SkinWhiteningLevel) != TOK)
		{
			 LOGE("[Ln%d] Makeup TFaceWhitening = failed\n", __LINE__);
    	     return res;
		}
        	LOGE("[Ln%d] Makeup TFaceWhitening = OK\n", __LINE__);  
// 		savefile(handle->dstTempImg[0].ppu8Plane[0], handle->srcImg.pi32Pitch[0]*handle->srcImg.i32Height+
// 		handle->srcImg.pi32Pitch[1]*handle->srcImg.i32Height/2, "d://result_720_960.NV21");
	}

	//锟斤拷锟桔达拄1�7
	if(pParam->EyeBag_Enable == TTrue){
	    TFaceOutline outline;
	    GetOutlinePoint(handle->pMarks, 66, &outline);
	    for(int i=0; i<66; i++) {
	    	LOGI("%d, %d",handle->pMarks[2*i],handle->pMarks[2*i+1]);
	    }
	    LOGI("left_eye_left.x======================%d\n", outline.tplefteyeleft.x);
	    LOGI("left_eye_left.y======================%d\n", outline.tplefteyeleft.y);

	    LOGI("left_eye_top.x======================%d\n",  outline.tplefteyetop.x);
	    LOGI("left_eye_top.y======================%d\n", outline.tplefteyetop.y);

	    LOGI("left_eye_bottom.x======================%d\n",  outline.tplefteyebottom.x);
	    LOGI("left_eye_bottom.y======================%d\n", outline.tplefteyebottom.y);

	    LOGI("left_eye_right.x======================%d\n",  outline.tplefteyeright.x);
	    LOGI("left_eye_right.y======================%d\n", outline.tplefteyeright.y);


	   	LOGI("right_eye_left.x======================%d\n", outline.tprighteyeleft.x);
	   	LOGI("right_eye_left.y======================%d\n", outline.tprighteyeleft.y);

	    LOGI("right_eye_top.x======================%d\n",  outline.tprighteyetop.x);
	   	LOGI("right_eye_top.y======================%d\n", outline.tprighteyetop.y);

	    LOGI("right_eye_bottom.x======================%d\n",  outline.tprighteyebottom.x);
	   	LOGI("right_eye_bottom.y======================%d\n", outline.tprighteyebottom.y);

	   	LOGI("right_eye_right.x======================%d\n",  outline.tprighteyeright.x);
	   	LOGI("right_eye_right.y======================%d\n", outline.tprighteyeright.y);

		LOGI("[Ln%d] Makeup TFaceEyeBag hMem=%d img=%d level=%d", __LINE__, handle->hMem, &(handle->dstTempImg[0]),pParam->EyeBagLevel);
		if(res=DoRemoveCircle(handle->hMem,&(handle->dstTempImg[0]),
				&(handle->dstTempImg[0]),&outline,pParam->EyeBagLevel, NULL)){
			 LOGE("[Ln%d] Makeup TFaceEyeBag failed: %d\n", __LINE__, res);
			    	     return res;
		}
		LOGI("[Ln%d] Makeup TFaceEyeBag = OK\n", __LINE__);
	}

	if(pParam->DarkCircle_Enable==TTrue) {
	    TFaceOutline outline;
	    GetOutlinePoint(handle->pMarks, 66, &outline);

		if(res=DoRemoveBlack(handle->hMem,&(handle->dstTempImg[0]),
				&(handle->dstTempImg[0]),&outline,pParam->DarkCircleLevel, NULL)){
			 LOGE("[Ln%d] Makeup TFaceDarkCircle failed: %d\n", __LINE__, res);
			    	     return res;
		}
		LOGE("[Ln%d] Makeup TFaceDarkCircle = OK\n", __LINE__);
	}

	if(pParam->BrightEye_Enable==TTrue) {
	    TFaceOutline outline;
	    GetOutlinePoint(handle->pMarks, 66, &outline);

		if(res=DoBrightEye(handle->hMem,&(handle->dstTempImg[0]),
				&(handle->dstTempImg[0]),&outline,pParam->BrightEyeLevel, NULL)){
			 LOGE("[Ln%d] Makeup DoBrightEye failed: %d\n", __LINE__, res);
			    	     return res;
		}
		LOGE("[Ln%d] Makeup DoBrightEye = OK\n", __LINE__);
	}
	//锟斤拷色  fengji
	if(pParam->SkinColor_Enable == TTrue && handle->bFaceSkin != TFalse){
		TSMakeupBlackSkinModel skinmodel;
		switch(pParam->SkinColorType){
		case 0:skinmodel = TS_BLACK_SKINA_MODEL_1;		break;
		case 1:skinmodel = TS_BLACK_SKINA_MODEL_2;		break;
		case 2:skinmodel = TS_BLACK_SKINA_MODEL_3;		break;
		case 3:skinmodel = TS_BLACK_SKINA_MODEL_4;		break;
		case 4:skinmodel = TS_BLACK_SKINA_MODEL_5;		break;
		case 5:skinmodel = TS_BLACK_SKINA_MODEL_6;		break;
		case 6:skinmodel = TS_BLACK_SKINA_MODEL_7;		break;
		case 7:skinmodel = TS_BLACK_SKINA_MODEL_8;		break;
		case 8:skinmodel = TS_BLACK_SKINA_MODEL_9;		break;
		case 9:skinmodel = TS_BLACK_SKINA_MODEL_10;		break;
		case 10:skinmodel = TS_BLACK_SKINA_MODEL_11;		break;
		case 11:skinmodel = TS_BLACK_SKINA_MODEL_12;		break;
		case 12:skinmodel = TS_BLACK_SKINA_MODEL_13;		break;
		case 13:skinmodel = TS_BLACK_SKINA_MODEL_14;		break;
		case 14:skinmodel = TS_BLACK_SKINA_MODEL_15;		break;
		case 15:skinmodel = TS_BLACK_SKINA_MODEL_16;		break;
		}
 	     LOGE("[Ln%d] Makeup Before TFaceSkinColor, level:%d maskCb=%d maskCr=%d\n", __LINE__, pParam->SkinColorLevel,pParam->SkinColorMask.Cb, pParam->SkinColorMask.Cr);  
		if(res = ts_makeup_do_foundation(&pOut, &pOut,1, (handle->pFaceRect),
			pParam->SkinColorLevel,skinmodel) != TOK)
		{
			 LOGE("[Ln%d] Makeup TFaceSkinColor = failed\n", __LINE__);
    	     return res;
		}
        	LOGE("[Ln%d] Makeup TFaceSkinColor = OK\n", __LINE__);  
	}

	//锟斤拷锟��7
	if(pParam->DeBlemish_Enable == TTrue && handle->bFaceSkin != TFalse)
	{
		int i=0;
	 
	    LOGE("[Ln%d] makeup_effect\n", __LINE__);   
		DeblemishPara InputParameter;
		InputParameter.lScale = handle->FaceMaskScale;
		TMemCpy(&(InputParameter.rcFace), handle->pFaceRect, sizeof(TRECT));
 
		for(i=0; i<pParam->pDeblemishParaNum[0]; i++){

		//	InputParameter.deblemishRadius = pParam->pDeblemishPara[i].deblemishRadius;  //TBD
  		
			if(pParam->bNewDeblemish == TTrue && i == pParam->pDeblemishParaNum[0]-1)
			{
				TPOINT oldedge, newedg;
				TInt32 xRadius, yRadius;
                oldedge.x = pParam->pDeblemishPara[i].selectpoint.x+pParam->pDeblemishPara[i].deblemishRadius;
				oldedge.y = pParam->pDeblemishPara[i].selectpoint.y+pParam->pDeblemishPara[i].deblemishRadius;

				LOGE("DoDeblemish Before track selectpoint[%d]: [%d, %d] \n", i, pParam->pDeblemishPara[i].selectpoint.x, pParam->pDeblemishPara[i].selectpoint.y); 
				pParam->pDeblemishPara[i].selectpoint = InputParameter.selectpoint = TS_WarpFace_TrackPoint(handle->hWarp, pParam->pDeblemishPara[i].selectpoint);
				
				newedg = TS_WarpFace_TrackPoint(handle->hWarp, oldedge);
				xRadius = newedg.x - InputParameter.selectpoint.x;
				if(xRadius < 0)  xRadius = -xRadius;
				yRadius = newedg.y - InputParameter.selectpoint.y;
				if(yRadius < 0)  yRadius = -yRadius;
				if(xRadius < yRadius)
					xRadius = yRadius;

                pParam->pDeblemishPara[i].deblemishRadius = InputParameter.deblemishRadius = xRadius;

			    LOGE("DoDeblemish After track selectpoint[%d]: [%d, %d] Radius[%d] \n", i, InputParameter.selectpoint.x, InputParameter.selectpoint.y, InputParameter.deblemishRadius); 

			}else
			{

				InputParameter.selectpoint.x = pParam->pDeblemishPara[i].selectpoint.x;
				InputParameter.selectpoint.y = pParam->pDeblemishPara[i].selectpoint.y;
                InputParameter.deblemishRadius = pParam->pDeblemishPara[i].deblemishRadius;
			}

		
		    

#ifdef DEBUG
	        tcost1 = gettime();
#endif
			LOGE("DoDeblemish hDeblemish=%d,dstTempImg[0]=%d, FaceMask=%d, dstTempImg[0]=%d \n", handle->hDeblemish,&(handle->dstTempImg[0]), &(handle->FaceMask),&(handle->dstTempImg[0])); 
			if(res = DoDeblemish(handle->hDeblemish,&(handle->dstTempImg[0]), &(handle->FaceMask),&(handle->dstTempImg[0]), 
				&InputParameter, TNull) != TOK)
			{
				LOGE("[Ln%d] DoDeblemish = failed\n", __LINE__);
				return res;
			}
#ifdef DEBUG
			tcost2 = gettime();
			LOGE("DoDeblemish cost %d \n", tcost2-tcost1); 
#endif
		}

			LOGE("DoDeblemish OK \n");  

	}
#ifdef DEBUG
	{

		char name[64];
		static int num = 0;
		// num ++;
#ifdef WIN32
		sprintf(name, "d:\\res_/%d_%d_%d.NV21", num, handle->dstTempImg[0].pi32Pitch[0], handle->dstTempImg[0].i32Height);
#else
		sprintf(name, "/sdcard/res_%d_%d_%d.NV21", num, handle->srcImg.pi32Pitch[0], handle->srcImg.i32Height);

#endif
//		LOGE(name);
	//	savefile( handle->dstTempImg[0].ppu8Plane[0], handle->dstTempImg[0].pi32Pitch[0]*handle->dstTempImg[0].i32Height*3/2, name);
		 

	}
#endif 

	  //Warp
	 if(pParam->BigEye_Enable || pParam->TrimFace_Enable)
	 {
        
         if(handle->hWarp==NULL) {
         	LOGE("[Ln%d] makeup_effect handle->hWarp==NULL\n", __LINE__);
         	return TERR_INVALID_PARAM;
         }
         TS_WarpFace_SetImage(handle->hWarp,&(handle->dstTempImg[0]));
		 TS_WarpFace_Reset(handle->hWarp);
		 LOGE("Before TS_WarpFace_Process, BigEyegLevel [%d], TrimFaceLevel[%d] \n", pParam->BigEyegLevel,pParam->TrimFaceLevel);

		 handle->dstTempImg[1] = TS_WarpFace_Process(handle->hWarp,pParam->BigEyegLevel/10,pParam->TrimFaceLevel/10,TS_FACEWARP_CHEEK|TS_FACEWARP_EYE);

		 LOGE("After TS_WarpFace_Process\n"); 
		 LOGE("handle->hWarp = %x", handle->hWarp);
		 LOGE("handle->dstTempImg[1].i32Width = %d", handle->dstTempImg[1].i32Width);
		 LOGE("handle->dstTempImg[1].i32Height = %d", handle->dstTempImg[1].i32Height);
		 LOGE("handle->dstTempImg[1].ppu8Plane[0] = %d", handle->dstTempImg[1].ppu8Plane[0]);
	 
		 LOGE("handle->dstTempImg[0].i32Height = %d", handle->dstTempImg[0].i32Height);
		 if(pImage->u32PixelArrayFormat == TS_PAF_RGB24_R8G8B8)
			 NV21_to_RGB888(handle->dstTempImg[1].ppu8Plane[0], handle->dstTempImg[1].ppu8Plane[1], handle->dstTempImg[0].i32Width, 
			 handle->dstTempImg[0].i32Height, handle->dstTempImg[0].pi32Pitch[0], pImage->ppu8Plane[0], pImage->i32Width, pImage->i32Height,
			 pImage->pi32Pitch[0]);
		 if(pImage->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8)
			 NV21_to_RGBA8888(handle->dstTempImg[1].ppu8Plane[0], handle->dstTempImg[1].ppu8Plane[1], handle->dstTempImg[0].i32Width, 
			 handle->dstTempImg[0].i32Height, handle->dstTempImg[0].pi32Pitch[0], pImage->ppu8Plane[0], pImage->i32Width, pImage->i32Height,
			 pImage->pi32Pitch[0]);
		 
         LOGE("[Ln%d] makeup_effect\n", __LINE__);  
		 

	 }else
	 {
		 //转锟截碉拷RGB锟斤拷式
		 
		 LOGE("[Ln%d] makeup_effect\n", __LINE__);  
		 if(pImage->u32PixelArrayFormat == TS_PAF_RGB24_R8G8B8)
			 NV21_to_RGB888(handle->dstTempImg[0].ppu8Plane[0], handle->dstTempImg[0].ppu8Plane[1], handle->dstTempImg[0].i32Width, 
			 handle->dstTempImg[0].i32Height, handle->dstTempImg[0].pi32Pitch[0], pImage->ppu8Plane[0], pImage->i32Width, pImage->i32Height,
			 pImage->pi32Pitch[0]);
		 if(pImage->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8)
			 NV21_to_RGBA8888(handle->dstTempImg[0].ppu8Plane[0], handle->dstTempImg[0].ppu8Plane[1], handle->dstTempImg[0].i32Width, 
			 handle->dstTempImg[0].i32Height, handle->dstTempImg[0].pi32Pitch[0], pImage->ppu8Plane[0], pImage->i32Width, pImage->i32Height,
			 pImage->pi32Pitch[0]);
		 
         LOGE("[Ln%d] makeup_effect\n", __LINE__);  
	 }
 
	 LOGI("makeup_effect ----->");

	return TOK;
	 

}


TInt32 makeup_done( /* return TOK if success, otherwise fail */
                THandle Handle)	/* [in/out] the address of engine handle */

{
	LOGI("makeup_done <-----");
	MakeupHandle* handle = (MakeupHandle*)Handle;
    THandle hMem;
	if (TNull == Handle) 
	{
		LOGE("[Ln%d] invalid parameter", __LINE__);
		return TERR_INVALID_PARAM;
	}

	hMem = handle->hMem;

//	ReleaseFDData(hMem,  handle->hFaceDection );
	sakDetectObject_destroy(&(handle->hFaceDection));

/* 	UninitSkinDetector(&(handle->hSkinDetector));*/
    UninitDeblemish(handle->hDeblemish);

//    if(handle->hFacialMarks) {
//    	sakFacialTrack_destroy(&(handle->hFacialMarks));
//    	handle->hFacialMarks = NULL;
//    }

	 SAFE_FREE(handle->srcImg.ppu8Plane[0]);
	 SAFE_FREE(handle->dstTempImg[0].ppu8Plane[0]);
	 SAFE_FREE(handle->dstTempImg[1].ppu8Plane[0]);
	 SAFE_FREE(handle->FaceMask.pData);
	 SAFE_FREE(handle->pEyeMarks);
	 SAFE_FREE(handle->pMarks);
	 SAFE_FREE(handle->featMask.ppu8Plane[0]);

	 LOGI("makeup_done ----->");
	 return TOK;

}

TInt32 makeup_load_image( /* return TOK if success, otherwise fail */
						 THandle hHandle,          /* [in] engine handle */
						 LPTSOFFSCREEN pImage,		/* [in] input image, only TS_PAF_RGB24_R8G8B8/TS_PAF_RGB32_R8G8B8A8 supported */
						 TInt32 *pFaceNum,			/* [out] face number */
                         TRECT  *pFaceRect,        /* [out] face rectangle array */
                         TPOINT  *pEyePoint,         /* [out] eye point array, double size of face rectangle; left/right;left/right */
					     TPOINT  *pMouthPoint)       /* [out] mouth point array */
{
	MakeupHandle* handle = (MakeupHandle*)hHandle;
	int width =  pImage->i32Width;
	int height = pImage->i32Height;
	int facenum = 0;
	TLong lScale = 0, lMaxV, lMaskW, lMaskH;
	int i;
    TRESULT res;

	if (TNull == hHandle || TNull == pImage || TNull == pFaceNum || TNull == pFaceRect ||TNull == pFaceRect || TNull == pFaceRect)
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
        LOGE("hHandle = %d",hHandle);
		LOGE("pImage = %d",pImage);
		LOGE("pFaceNum = %d",pFaceNum);
		LOGE("pFaceRect = %d",pFaceRect);
		LOGE("pFaceRect = %d",pFaceRect);
		LOGE("pFaceRect = %d",pFaceRect);
		return TERR_INVALID_PARAM;
	}

	if(pImage->u32PixelArrayFormat != TS_PAF_RGB24_R8G8B8 && pImage->u32PixelArrayFormat != TS_PAF_RGB32_R8G8B8A8)
	{
		LOGE("[Ln%d] invalid parameter\n", __LINE__);
		return TERR_INVALID_PARAM;
	}

     LOGE("[Ln%d]Before create img\n", __LINE__);

	//Convert Image
	CreateNV21ImageSpace(handle->hMem, pImage, &(handle->srcImg));
	if(handle->srcImg.ppu8Plane[0] == TNull)
	{
		LOGE("[Ln%d] memory failed\n", __LINE__);
    	return TERR_NO_MEMORY;
	}
	LOGE("[Ln%d]Do create img\n", __LINE__);

	CreateNV21ImageSpace(handle->hMem, pImage, &(handle->dstTempImg[0]));
	if(handle->dstTempImg[0].ppu8Plane[0] == TNull)
	{
		LOGE("[Ln%d] memory failed\n", __LINE__);
			return TERR_NO_MEMORY;
	}

    LOGE("[Ln%d]Before color convert\n", __LINE__);

    if(pImage->u32PixelArrayFormat == TS_PAF_RGB24_R8G8B8)
	   RGB888_to_NV21(pImage->ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.ppu8Plane[1],
                      pImage->i32Width, pImage->i32Height, pImage->pi32Pitch[0], handle->srcImg.i32Width,  handle->srcImg.i32Height);
    if(pImage->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8)
	   RGBA8888_to_NV21(pImage->ppu8Plane[0], handle->srcImg.ppu8Plane[0], handle->srcImg.ppu8Plane[1],
                      pImage->i32Width, pImage->i32Height, pImage->pi32Pitch[0], handle->srcImg.i32Width,  handle->srcImg.i32Height);

   LOGE("[Ln%d] After color convert\n", __LINE__);

#ifdef DEBUG
   tcost1 = gettime();
#endif
   sakDetectObject_setImage(handle->hFaceDection, &handle->srcImg);
   facenum = sakDetectObject_detect(handle->hFaceDection, (char*) "face", 0);

#ifdef DEBUG
   tcost2 = gettime();
   LOGE("sakFaceDetect_detect cost %d \n", tcost2-tcost1);
#endif


	if(facenum > MAEUP_MAX_FACE)
		facenum = MAEUP_MAX_FACE;

	LOGE("[Ln%d] facenum = %d\n", __LINE__, facenum);


	*pFaceNum = handle->FaceNum = facenum;
	if(facenum == 0)
	{
    	 return TOK;

	}

    for(i=0; i<facenum; i++)
	{
    	TRECT rect,facerect;
		sakDetectObject_object(handle->hFaceDection, i, &pFaceRect[i]);

		jint eyeCount = sakDetectObject_detect(handle->hFaceDection, (char*) "eye", &pFaceRect[i]);
		if (eyeCount > 0) {
			sakDetectObject_object(handle->hFaceDection, 0, &rect);
			pEyePoint[2*i].x = handle->pEyePoint[2*i].x = (rect.left+rect.right)/2;
			pEyePoint[2*i].y = handle->pEyePoint[2*i].y = (rect.top+rect.bottom)/2;;
			handle->pEyeRadius[2*i] = (rect.right - rect.left)/2;
		}
		if (eyeCount > 1) {
			sakDetectObject_object(handle->hFaceDection, 1, &rect);
			pEyePoint[2*i+1].x  = handle->pEyePoint[2*i+1].x = (rect.left+rect.right)/2;
			pEyePoint[2*i+1].y =  handle->pEyePoint[2*i+1].y = (rect.top+rect.bottom)/2;
			handle->pEyeRadius[2*i+1] = (rect.right - rect.left)/2;
		}

		jint mouthCount = sakDetectObject_detect(handle->hFaceDection, (char*) "mouth", &pFaceRect[i]);
		if (mouthCount > 0) {
			sakDetectObject_object(handle->hFaceDection, 0, &rect);
			pMouthPoint[i].x = handle->pMouthPoint[i].x = (rect.left+rect.right)/2;
			pMouthPoint[i].y = handle->pMouthPoint[i].y = (rect.top+rect.bottom)/2;
		}
	}

	if(handle->FaceMask.pData != TNull)
		TMemFree(handle->hMem, handle->FaceMask.pData);

	handle->FaceMask.pData = TNull;

	lMaxV = (handle->srcImg.i32Width > handle->srcImg.i32Height) ? handle->srcImg.i32Width : handle->srcImg.i32Height;
	while(lMaxV > 80)
	{
		lMaxV >>= 1;
		lScale++;
	}
	lMaskW = handle->srcImg.i32Width >> lScale;
	lMaskH = handle->srcImg.i32Height >> lScale;
    handle->FaceMaskScale = lScale;

	LOGE("[Ln%d] lMaskW =%d, lMaskH=%d\n", __LINE__, lMaskW, lMaskH);


	handle->FaceMask.lWidth    = lMaskW;
	handle->FaceMask.lHeight   = lMaskH;
	handle->FaceMask.lMaskLine = (lMaskW + 3) & 0xfffffffc;
	handle->FaceMask.pData     = (TByte*)TMemAlloc(handle->hMem, handle->FaceMask.lMaskLine*handle->FaceMask.lHeight);
	if(handle->FaceMask.pData == TNull)
	{

        LOGE("[Ln%d] handle->FaceMask.pData = TNUll\n", __LINE__);
    	 return TERR_NO_MEMORY;
	}



	{
		TByte* pTmpData = (TByte*)TMemAlloc(handle->hMem, 1024*1024*4);
		THandle hMem = TMemMgrCreate(pTmpData, 1024*1024*4);
		LOGE("[Ln%d] Before InitSkinDetector\n", __LINE__);
		if(res = InitSkinDetector(hMem, &(handle->hSkinDetector)) != TOK)
		{
			LOGE("[Ln%d] InitSkinDetector falied \n", __LINE__);

		//	return res;
		}else{
			LOGE("[Ln%d] Before DoSkinDetect\n", __LINE__);

			if(res = DoSkinDetect(handle->hSkinDetector, &(handle->srcImg), handle->pFaceRect,
				handle->FaceNum , &(handle->FaceMask)) != TOK)
			{
				LOGE("[Ln%d] DoSkinDetect = failed, res =%d\n", __LINE__, res);
				TMemSet(handle->FaceMask.pData, 0, handle->FaceMask.lMaskLine*handle->FaceMask.lHeight);
				handle->bFaceSkin = TFalse;
			//	return res;
			}else{
				LOGE("[Ln%d] DoSkinDetect OK\n", __LINE__);
				handle->bFaceSkin = TTrue;
				UninitSkinDetector(&(handle->hSkinDetector));

				 TxMask mask;
				 mask.nWidth = handle->FaceMask.lWidth;
				 mask.nHeight = handle->FaceMask.lHeight;
				 mask.nPitch = handle->FaceMask.lMaskLine;
				 mask.pData = handle->FaceMask.pData;
				TxMaskSmooth(&mask, 2);
			}

			LOGE("[Ln%d] Before UninitSkinDetector\n", __LINE__);

			//	UninitSkinDetector(&(handle->hSkinDetector));
            TMemFree(handle->hMem, pTmpData);
		}


	}


	{
		TPOINT facekp[3];
		facekp[0].x = handle->pEyePoint[0].x;
		facekp[0].y = handle->pEyePoint[0].y;
		facekp[1].x = handle->pEyePoint[1].x;
		facekp[1].y = handle->pEyePoint[1].y;
		facekp[2].x = handle->pMouthPoint[0].x;
		facekp[2].y = handle->pMouthPoint[0].y;
		if(handle->hWarp != NULL)
			TS_FaceWarp_Uninit(handle->hWarp);

		LOGE("facekp[0].x=%d\n", facekp[0].x);
		LOGE("facekp[0].y=%d\n", facekp[0].y);
		LOGE("facekp[1].x=%d\n", facekp[1].x);
		LOGE("facekp[1].y=%d\n", facekp[1].y);
		LOGE("facekp[2].x=%d\n", facekp[2].x);
		LOGE("facekp[2].y=%d\n", facekp[2].y);


		res = TS_FaceWarp_Init(&(handle->hWarp),handle->hMem,&(handle->dstTempImg[0]),facekp);
		if (TERR_NONE != res)
		{
			LOGE("TS_FaceWarp_Init failed %d\n", res);
			return res;
		 }
	}

//	if(!(handle->hFacialMarks)) {
//		handle->hFacialMarks = sakFacialTrack_create(0);
//	}
//	res = sakFacialTrack_figure(handle->hFacialMarks, &(handle->srcImg), pFaceRect[i], 0);
//	if(res!=TOK) {
//		LOGE("sakFacialTrack_figure failed: %d", res);
//	}
//	sakFacialTrack_setProperty(handle->hFacialMarks, (const TPChar)"landmark-type", "default");
//	sakFacialTrack_getProperty(handle->hFacialMarks, (const TPChar)"landmarks", (void*)(handle->pMarks));
//	sakFacialTrack_setProperty(hdl, (const TPChar)"landmark-type", "eyes4");
//	sakFacialTrack_getProperty(hdl, (const TPChar)"landmarks", (void*)(handle->pEyeMarks));

//    if(handle->featMask.ppu8Plane[0]!=NULL) {
// 	   TMemFree(handle->hMem, handle->featMask.ppu8Plane[0]);
// 	   handle->featMask.ppu8Plane[0]=NULL;
//    }
//    handle->featMask.u32PixelArrayFormat = TS_PAF_GRAY;
//    handle->featMask.i32Width = handle->srcImg.i32Width;
//    handle->featMask.i32Height = handle->srcImg.i32Height;
//    handle->featMask.pi32Pitch[0] = handle->srcImg.i32Width;
//    handle->featMask.ppu8Plane[0] = (TUInt8*)TMemAlloc(handle->hMem, handle->featMask.pi32Pitch[0]*handle->featMask.i32Height);
//    sakFacialTrack_get_benw_mask_from_landmarks(&(handle->featMask), handle->pMarks);
	return TOK;

}

TInt32 makeup_init_params( /* return TOK if success, otherwise fail */
                THandle Handle,
                MakeupFeatureParam* pParam,
                MakeupLevel makeupLevel) {
	LOGI("makeup_init_params level=%d<-----", makeupLevel);
	if(pParam==NULL) {
		return TERR_INVALID_PARAM;
	}

	pParam->SkinWhiten_Enable = TTrue;
	pParam->SkinClean_Enable = TTrue;
	pParam->TrimFace_Enable = TTrue;
	pParam->BigEye_Enable = TTrue;
	pParam->SkinColor_Enable = TTrue;
	pParam->EyeBag_Enable = TTrue;
	pParam->DarkCircle_Enable = TTrue;
	pParam->BrightEye_Enable = TTrue;
	pParam->SkinColorMask.Cb = 108;
	pParam->SkinColorMask.Cr = 182;

	switch(makeupLevel) {
	case MakeupLevel_1:
		pParam->SkinWhiteningLevel = 20;
		pParam->SkinCleanLevel = 40;
		pParam->TrimFaceLevel = 20;
		pParam->BigEyegLevel = 20;
		pParam->SkinColorLevel = 20;
		pParam->EyeBagLevel = 20;
		pParam->DarkCircleLevel = 20;
		pParam->BrightEyeLevel = 20;
		break;
	case MakeupLevel_2:
		pParam->SkinWhiteningLevel = 40;
		pParam->SkinCleanLevel = 60;
		pParam->TrimFaceLevel = 20;
		pParam->BigEyegLevel = 20;
		pParam->SkinColorLevel = 30;
		pParam->EyeBagLevel = 40;
		pParam->DarkCircleLevel = 40;
		pParam->BrightEyeLevel = 40;
		break;
	case MakeupLevel_3:
		pParam->SkinWhiteningLevel = 60;
		pParam->SkinCleanLevel = 70;
		pParam->TrimFaceLevel = 20;
		pParam->BigEyegLevel = 20;
		pParam->SkinColorLevel = 40;
		pParam->EyeBagLevel = 60;
		pParam->DarkCircleLevel = 60;
		pParam->BrightEyeLevel = 60;
		break;
	case MakeupLevel_4:
		pParam->SkinWhiteningLevel = 70;
		pParam->SkinCleanLevel = 85;
		pParam->TrimFaceLevel = 20;
		pParam->BigEyegLevel = 20;
		pParam->SkinColorLevel = 60;
		pParam->EyeBagLevel = 80;
		pParam->DarkCircleLevel = 80;
		pParam->BrightEyeLevel = 80;
		break;
	case MakeupLevel_5:
		pParam->SkinWhiteningLevel = 90;
		pParam->SkinCleanLevel = 100;
		pParam->TrimFaceLevel = 20;
		pParam->BigEyegLevel = 20;
		pParam->SkinColorLevel = 80;
		pParam->EyeBagLevel = 100;
		pParam->DarkCircleLevel = 100;
		pParam->BrightEyeLevel = 100;
		break;
	default:
		memset(pParam, 0, sizeof(MakeupFeatureParam));
		return TERR_INVALID_PARAM;
	}
	LOGI("makeup_init_params ----->");
	return TOK;
}
