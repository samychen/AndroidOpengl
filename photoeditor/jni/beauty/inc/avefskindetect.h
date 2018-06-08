#ifndef _TSF_SKIN_DETECT_H_
#define _TSF_SKIN_DETECT_H_

#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tfacemaskdef.h"

#ifdef __cplusplus
extern "C" {
#endif

TRESULT InitSkinDetector(THandle hMemMgr, THandle *phDetector);

TVoid UninitSkinDetector(THandle *phDetector);

TRESULT DoSkinDetect(THandle hDetector, LPTSOFFSCREEN pImg, const TRECT* pFaceRect, 
					 TLong lFacesNum, TMASK *pFaceMask);

#ifdef __cplusplus
}
#endif

#endif 