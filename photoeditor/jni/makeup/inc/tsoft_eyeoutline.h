#ifndef _THDSOFT_EYE_OUTLINE_H_
#define _THDSOFT_EYE_OUTLINE_H_

#include "tsoft_makeup_common.h"

typedef struct  
{
	TPOINT ptCenter;
	TInt32 lRadius;
}TSM_IRIS;

typedef struct  
{
	TPOINT *pptLinePts;
	TInt32 lPtsNum;			//lPtsNum is a even number. 
							//Left corner: pptLinePts[0], Right corner: pptLinePts[lPtsNum/2]
}TSM_EYE_LINE;

typedef struct
{
	TBool	bIsColorIris;
	TUInt8*	pu8Gray;
	TRECT	rtIris;
}TSM_IRIS_MASK;

//////////////////////////////////////////////////////////////////////////
#ifdef __cplusplus
extern "C" {
#endif	

/************************************************************************/
/*Detect the iris                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_IrisDetect(
	THandle			hEngine, 
	TSM_IRIS*		pirisLeft, 
	TSM_IRIS*		pirisRight
);

/************************************************************************/
/* Set the intital eye rectangle                                                                     */
/************************************************************************/
TSM_API TRESULT TSM_SetInputEyeRect(
	THandle				hMakeupEngine, 
	const TRECT*		prtLeft, 
	const TRECT*		prtRight
);

/************************************************************************/
/* Set the iris directly                                                                     */
/************************************************************************/
TSM_API TRESULT TSM_SetInputIrisCircle(
	THandle				hMakeupEngine, 
	const TSM_IRIS*		psIrisLeft, 
	const TSM_IRIS*		psIrisRight
);

/************************************************************************/
/*   Detect the eye line                                                                   */
/************************************************************************/
TSM_API TRESULT TSM_LinePtsDetect(
	THandle			hEngine, 
	TSM_EYE_LINE*	plineLeft, 
	TSM_EYE_LINE*	plineRight, 
	TInt32			lLinePtsNum
);

/************************************************************************/
/*   Detect the eye mask                                                                   */
/************************************************************************/
TSM_API TRESULT TSM_IrisMaskDetect(
	THandle			hEngine, 
	TSM_IRIS_MASK*	psMaskLeft, 
	TSM_IRIS_MASK*	psMaskRight
);

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetEyeOutlineVersion();


#ifdef __cplusplus
}
#endif

#endif //_THDSOFT_EYE_OUTLINE_H_