#ifndef _THDSOFT_EYEBROW_H_
#define _THDSOFT_EYEBROW_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_Eyebrow(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_GetInputEyebrowPts(
	THandle					hMakeupEngine, 
	TSM_POINTS				*psBrowLeft, 
	TSM_POINTS				*psBrowRight 
);
TSM_API TRESULT TSM_SetInputEyebrowPts(
	THandle					hMakeupEngine, 
	const TSM_POINTS		*psBrowLeft, 
	const TSM_POINTS		*psBrowRight
);

/************************************************************************/
/* The intensity setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetEyebrowIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in] The intensity. Default as 50, Between[0, 100]
);
TSM_API TRESULT TSM_SetEyebrowThickness(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lThickness			// [in] The thickness. Default as 50, Between[0, 100]
);
/************************************************************************/
/* The color/model setting function. Call if needed                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_SetEyeBrowColor(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF				crBGR				// [in] The color. Default as TSM_COLOR(109, 98, 230)
);	
TSM_API TRESULT TSM_SetEyeBrowModel(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL			*pModel             // [in] The model of Eyebrow
);
/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API	const TSM_Version* TSM_GetEyebrowVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_EYEBROW_H_