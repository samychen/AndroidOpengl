#ifndef _THDSOFT_EYE_SHADOW_H_
#define _THDSOFT_EYE_SHADOW_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_EyeShadow(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* The intensity setting function. Call if needed                       */
/************************************************************************/
TSM_API TRESULT TSM_SetEyeShadowIntensity(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32					lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

TSM_API TRESULT TSM_SetEyeShadowMultiIntensity(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32					*plIntensity,		// [in] The intensity array. Default as 50, Between[0, 100]
	TInt32					lColorNumber		// [in] The number of eyeshadow color
);	

TSM_API TRESULT TSM_SetEyeGlitterIntensity(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32					lIntensity			// [in] The intensity. Default as 50, Between[0, 100]
);	

/************************************************************************/
/* The color/model setting function. Call if needed                     */
/************************************************************************/
TSM_API TRESULT TSM_SetEyeShadowMultiColor(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF				*pcrBGR,			// [in] The color
	TInt32					lColorNumber		// [in] The number of eyeshadow color
);	

TSM_API TRESULT TSM_SetEyeShadowModel(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL			*pModel
);	

TSM_API TRESULT TSM_SetEyeGlitterModel(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL			*pModel
);	

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_GetEyeShadowArea(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TRECT			*prtLeft, 
	TRECT			*prtRight
);

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetEyeShadowVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_EYE_SHADOW_H_