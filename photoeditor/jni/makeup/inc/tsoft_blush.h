#ifndef _THDSOFT_BLUSH_H_
#define _THDSOFT_BLUSH_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*    Make the effect to image                                                                  */
/************************************************************************/
TSM_API TRESULT TSM_Blush(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* The intensity setting function. Call if needed                       */
/************************************************************************/
TSM_API TRESULT TSM_SetBlushIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

TSM_API TRESULT TSM_SetBlushMultiIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			*plIntensity,		// [in]  The intensity array. Default as 50, Between[0, 100]
	TInt32			lColorNumber		// [in] The blush color number
);	

/************************************************************************/
/* The color setting function. Call if needed                           */
/************************************************************************/
TSM_API TRESULT TSM_SetBlushMultiColor(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF		*pcrBGR,			// [in] The blush color. Default as TSM_COLOR(109, 98, 230)
	TInt32			lColorNumber		// [in] The blush color number
);	

TSM_API TRESULT TSM_SetBlushModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);	

/************************************************************************/
/* Get the valid area (rectangle)                                       */
/************************************************************************/
TSM_API TRESULT TSM_GetBlushArea(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TRECT			*prtLeft, 
	TRECT			*prtRight
);

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API	const TSM_Version* TSM_GetBlushVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_BLUSH_H_