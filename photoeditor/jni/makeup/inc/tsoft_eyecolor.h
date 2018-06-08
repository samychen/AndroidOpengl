#ifndef _THDSOFT_EYE_COLOR_H_
#define _THDSOFT_EYE_COLOR_H_

#include "tsoft_makeup_common.h"

typedef TInt32 TSM_LEN_TYPE;
#define	TSM_LEN_TRIAL		0
#define TSM_LEN_ACTUAL		1

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*   Iris Color                                                                   */
/************************************************************************/
TSM_API TRESULT TSM_IrisColor(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

TSM_API TRESULT TSM_SetIrisColor(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TCOLORREF		crBGR				// [in] The color. Default as TSM_COLOR(255, 170, 90)
);
TSM_API TRESULT TSM_SetIrisModel(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*pModel
);
TSM_API TRESULT TSM_SetIrisIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The saturation. Default as 50, Between[0, 100]
);	

/************************************************************************/
/*  Color Contact Lens                                                                    */
/************************************************************************/
TSM_API TRESULT TSM_ContactLen(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

TSM_API TRESULT TSM_SetLenModel(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	const TSM_MODEL*		pModel, 
	TSM_LEN_TYPE			eType
);	
TSM_API TRESULT TSM_SetContactLenIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lIntensity			// [in]  The saturation. Default as 50, Between[0, 100]
);	

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API const TSM_Version* TSM_GetEyeColorVersion();

#ifdef __cplusplus
}
#endif

#endif // _AIB_BASE_H_
