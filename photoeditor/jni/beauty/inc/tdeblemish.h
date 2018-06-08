/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#ifndef T_DEBLEMISH_H
#define T_DEBLEMISH_H


#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tfacemaskdef.h"

typedef struct _tag_DEBLEMISH {
	TPOINT		selectpoint;
	TLong		deblemishRadius;   // Radius of the deblemish rect //
	TRECT		rcFace;
	TUInt32     lScale;
} DeblemishPara, *LPDeblemishPara;

#ifdef __cplusplus
extern "C" {
#endif

TRESULT InitDeblemish(THandle hMemMgr, THandle *hDeblemish);

TVoid UninitDeblemish(THandle hDeblemish);

TRESULT DoDeblemish(THandle hDeblemish, LPTSOFFSCREEN pSrcImg, TMASK* pFaceMask, LPTSOFFSCREEN pDstImg, 
					LPDeblemishPara pInputParameter, TFNPROGRESS fnProgress);

TRESULT DoRemoveCircle(THandle hMemHandle, LPTSOFFSCREEN pSrcImg, LPTSOFFSCREEN pDstImg, 
					   LPTFaceOutline pInputParameter, TLong lRemoveLevel, TFNPROGRESS fnProgress);

TRESULT DoRemoveBlack(THandle hMemHandle, LPTSOFFSCREEN pSrcImg, LPTSOFFSCREEN pDstImg, 
					  LPTFaceOutline pInputParameter, TLong lRemoveLevel,TFNPROGRESS fnProgress);

TRESULT DoBrightEye(THandle hMemHandle, LPTSOFFSCREEN pSrcImg, LPTSOFFSCREEN pDstImg, 
					   LPTFaceOutline pInputParameter, TLong lRemoveLevel,TFNPROGRESS fnProgress);

TRESULT GetOutlinePoint(TLong * pOutlinepoint, TLong lPointSize, LPTFaceOutline pInputParameter);

#ifdef __cplusplus
}
#endif

#endif
