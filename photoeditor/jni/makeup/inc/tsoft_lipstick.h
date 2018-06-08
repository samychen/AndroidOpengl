#ifndef _THDSOFT_EYE_LIPSTICK_H_
#define _THDSOFT_EYE_LIPSTICK_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_LipStick(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

TSM_API TRESULT TSM_LipTattoos(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* The intensity setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetLipStickIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

TSM_API TRESULT TSM_SetLipGlossIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

TSM_API TRESULT TSM_SetLipLineIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

TSM_API TRESULT TSM_SetLipSmoothIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

TSM_API TRESULT TSM_SetLipTattoosIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The intensity. Default as 50, Between[0, 100]
);	

/************************************************************************/
/* The color setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetLipStickColor(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF		crBGR				// [in] The color. Default as TSM_COLOR(109, 98, 230)
);	
TSM_API TRESULT TSM_SetLipLineColor(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF		crBGR				// [in] The color. Default as TSM_COLOR(109, 98, 230)
);	

TSM_API TRESULT TSM_SetLipModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel				// [in] The lip model
);	

TSM_API TRESULT TSM_SetLipTattoosModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel				// [in] The lip tattoos model
);  

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_GetLipArea(
	THandle			hMakeupEngine,		
	TRECT			*prtLip
);
/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetLipStickVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_EYE_LIPSTICK_H_