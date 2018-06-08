
#ifndef _TSF_SKIN_DETECT_H_
#define _TSF_SKIN_DETECT_H_

#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tfacemaskdef.h"

#if defined(TS_DLL)
	#if defined(_WIN32) && !defined(__GNUC__) 
		#ifdef MYDLL_DLL
			#define MYDLL_DECL __declspec(dllexport)
		#else
			#define MYDLL_DECL __declspec(dllimport)
		#endif
	#else     /* UNIX */
		#if defined(MYDLL_DLL) && defined(HAS_GCC_VISIBILITY)
			#define MYDLL_DECL __attribute__((visibility("default")))
		#else
			#define MYDLL_DECL
		#endif
	#endif
#else
	#define MYDLL_DECL
#endif

#ifdef __cplusplus
extern "C" {
#endif

MYDLL_DECL TRESULT InitSkinDetector(THandle hMemMgr, THandle *phDetector);

MYDLL_DECL TVoid UninitSkinDetector(THandle *phDetector);

MYDLL_DECL TRESULT DoSkinDetect(THandle hDetector, LPTSOFFSCREEN pImg, const TRECT* pFaceRect, 
					 TLong lFacesNum, TMASK *pFaceMask);

#ifdef __cplusplus
}
#endif

#endif 
