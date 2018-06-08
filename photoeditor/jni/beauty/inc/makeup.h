/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#ifndef __MAKEUP_H__
#define __MAKEUP_H__

#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tfacemaskdef.h"

#ifdef __cplusplus
extern "C" {
#endif


/* parameters for features */
 

typedef struct _tag_MakeupDeblemish {
	TPOINT		selectpoint;
	TLong		deblemishRadius;   // Radius of the deblemish rect //
} MakeupDeblemishPara, *LPMakeupDeblemishPara;


typedef struct _MakeupFeatureParam
{
	
	TBool  DeBlemish_Enable;
	MakeupDeblemishPara * pDeblemishPara; //in/out
	TInt32* pDeblemishParaNum;
    TBool bNewDeblemish; // This time new deblemish point is added

	TBool  SkinClean_Enable;
	TInt32 SkinCleanLevel;
		
	TBool SkinWhiten_Enable;
	TInt32 SkinWhiteningLevel;

	TBool  BigEye_Enable;
	TInt32 BigEyegLevel;
	
	TBool  TrimFace_Enable;
	TInt32 TrimFaceLevel;

	TBool  SkinColor_Enable;
	TCOLORMask SkinColorMask;
	TInt32 SkinColorLevel;
	TInt32 SkinColorType;
	
	TBool  EyeBag_Enable;
	TInt32 EyeBagLevel;

	TBool DarkCircle_Enable;
	TInt32 DarkCircleLevel;

	TBool BrightEye_Enable;
	TInt32 BrightEyeLevel;

} MakeupFeatureParam;


enum MakeupLevel {
	MakeupLevel_NONE,
	MakeupLevel_1,
	MakeupLevel_2,
	MakeupLevel_3,
	MakeupLevel_4,
	MakeupLevel_5
};


#define MAEUP_MAX_FACE          1

/************************************************************************
 * The function used to Initialize the Makeup engine. 
 ************************************************************************/
TInt32 makeup_init( /* return TOK if success, otherwise fail */
                 THandle hMem,	      /* [in] memory handle */
                 THandle *pHandle);  /* [out] the address of engine handle */



/************************************************************************
 * The function used to load a image to the engine. 
 ************************************************************************/
TInt32 makeup_load_image( /* return TOK if success, otherwise fail */
                      THandle hHandle,          /* [in] engine handle */ 
                      LPTSOFFSCREEN pImage,		/* [in] input image, only support TS_PAF_RGB24_R8G8B8/TS_PAF_RGB32_R8G8B8A8  */
					  TInt32 *pFaceNum,			/* [out] face number */   
                      TRECT  *pFaceRect,        /* [out] face rectangle array */
					  TPOINT  *pEyePoint,         /* [out] eye point array, double size of face rectangle; left/right;left/right */
					  TPOINT  *pMouthPoint);      /* [out] mouth point array */  


/************************************************************************
 * The function used to replace a image to the engine, the face info can be set by user 
 ************************************************************************/
TInt32 makeup_replace_image( /* return TOK if success, otherwise fail */
                      THandle hHandle,          /* [in] engine handle */ 
                      LPTSOFFSCREEN pImage,		/* [in] input image, only support TS_PAF_RGB24_R8G8B8/TS_PAF_RGB32_R8G8B8A8  */
					  TInt32 *pFaceNum,
					  TRECT  *pFaceRect,        /* [in] face rectangle array*/ 
					  TPOINT  *pEyePoint,         /* [in] eye rectangle array*/ 
					  TPOINT  *pMouthPoint,		/* [in] mouth rectangle array*/
					  TInt32  *pMarks,
					  TInt32  *pEyeMarks);



/************************************************************************
 * The function used to replace a image to the engine, the face info can be set by user. It will NOT detect face skin again
 ************************************************************************/
TInt32 makeup_replace_image_withoutsd( /* return TOK if success, otherwise fail */
									  THandle hHandle,          /* [in] engine handle */ 
									  LPTSOFFSCREEN pImage,		/* [in] input image, only support TS_PAF_RGB24_R8G8B8/TS_PAF_RGB32_R8G8B8A8  */
									  TInt32 *pFaceNum,
									  TRECT  *pFaceRect,        /* [in] face rectangle array*/ 
									  TPOINT  *pEyePoint,         /* [in] eye rectangle array*/ 
									  TPOINT  *pMouthPoint,		/* [in] mouth rectangle array*/
									  TInt32  *pMarks,
									  TInt32  *pEyeMarks);


/************************************************************************
 * The function used to apply all the current effects to image.
   Important: this function just manage the image data loaded by makeup_load_image or makeup_replace_image
 ************************************************************************/
TInt32 makeup_effect( /* return TOK if success, otherwise fail */
                        THandle hHandle,         /* [in] engine handle */  
						MakeupFeatureParam* pParam,  /* [in/out] parameters, deblemish point will be updated */  
													 /* All old deblemish points need be remembered.*/  
                        LPTSOFFSCREEN pImage); /* [out] output image, same content but different size as loaded image */ 


/************************************************************************
 * The function used to Uninitialize the Makeup engine. 
 ************************************************************************/
TInt32 makeup_done( /* return TOK if success, otherwise fail */
                THandle Handle);	/* [in/out] the engine handle */

/************************************************************************
 * The function used to initialize the Makeup Parameters.
 ************************************************************************/
TInt32 makeup_init_params( /* return TOK if success, otherwise fail */
                THandle Handle,
                MakeupFeatureParam* pParam,
                MakeupLevel makeupLevel);

#ifdef __cplusplus
}
#endif

#endif
