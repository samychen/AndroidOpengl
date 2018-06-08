#ifndef _THDSOFT_EYE_LINE_H_
#define _THDSOFT_EYE_LINE_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_EyeLine(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* The intensity/color setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetEyeLineIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in] The intensity. Default as 50, Between[0, 100]
);	
TSM_API TRESULT TSM_SetEyeLineColor(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF		crBGR				// [in] The color. 
);	

/************************************************************************/
/* Set eyeline model                                                                     */
/************************************************************************/
TSM_API TRESULT TSM_SetUpperEyeLineModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);	
TSM_API TRESULT TSM_SetLowerEyeLineModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);	
/************************************************************************/
/* Setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_EnableLowerEyeLine(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TBool			bEnable
);	

/************************************************************************/
/* Get the valid area(rectangle)                                                                     */
/************************************************************************/
TSM_API TRESULT TSM_GetEyeLineArea(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TRECT			*prtLeft, 
	TRECT			*prtRight
);

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetEyeLineVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_EYE_LINE_H_