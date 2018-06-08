#ifndef _THDSOFT_CATCH_LIGHT_H_
#define _THDSOFT_CATCH_LIGHT_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_CatchLight(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* Get/Set the position of CatchLight                                                                     */
/************************************************************************/
TSM_API TRESULT TSM_GetCatchLightPos(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TPOINT					*pptLeft, 
	TPOINT					*pptRight
);
TSM_API TRESULT TSM_SetCatchLightPos(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	const TPOINT			*pptLeft, 
	const TPOINT			*pptRight
);
/************************************************************************/
/* Setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetLightShape(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);	

TSM_API TRESULT TSM_SetLightIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			Intensity			// [in]  The saturation. Default as 50, Between[0, 100]
);	

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetCatchLightVersion();

#ifdef __cplusplus
}
#endif

#endif // _AIB_BASE_H_
