#ifndef _THDSOFT_EYE_LASH_H_
#define _THDSOFT_EYE_LASH_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_EyeLash(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* The intensity setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetLashIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	
TSM_API TRESULT TSM_SetEyeLashColor(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF		crBGR				// [in]  The color
);	

/************************************************************************/
/* Setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_EnableLowerEyeLash(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TBool			bEnable
);	

TSM_API TRESULT TSM_SetUpperEyeLashModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);	

TSM_API TRESULT TSM_SetLowerEyeLashModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);	

/************************************************************************/
/* Get the valid area(rectangle)                                                                     */
/************************************************************************/
TSM_API TRESULT TSM_GetEyeLashArea(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TRECT			*prtLeft, 
	TRECT			*prtRight
);

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetEyeLashVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_EYE_LASH_H_