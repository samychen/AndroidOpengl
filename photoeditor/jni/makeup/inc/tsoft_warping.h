#ifndef _THDSOFT_WARPING_H_
#define _THDSOFT_WARPING_H_

#include "tsoft_makeup_common.h"

#ifdef __cplusplus
extern "C" {
#endif

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API TRESULT TSM_WARPING(
	THandle					hMakeupEngine,		// [in] The handle for face makeup engine
	TSM_OFFSCREEN*			pImgRlt,			// [out]
	TPOINT                  ptDownMousePoint,    // [in]  The point when clicked down
	TPOINT                  ptUpMousePoint,     // [in]  The point when clicked up
	TSM_FNPROGRESS			fnCallback,			// [in]  The callback function 
	TVoid*					pParam				// [in]  Caller-specific data that will be passed into the callback function
);

/************************************************************************/
/* The brush size setting function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetBrushSize(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32			lBrushiSize	    	// [in]  The brush size. Default as 50, Between[1, 100]
);	

/************************************************************************/
/* The brush intensity function. Call if needed                                 */
/************************************************************************/
TSM_API TRESULT TSM_SetBrushIntensity(
	THandle			hMakeupEngine,		// [in] The handle for face makeup engine
	TInt32		    lBlushIntensity 	// [in] The brush intensity. Default as 50, Between[1, 100]
);	

/************************************************************************/
/*                                                                      */
/************************************************************************/
TSM_API	const TSM_Version* TSM_GetWarpingVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_WARPING_H_