/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#ifndef _T_FACE_WHITEN_H_
#define _T_FACE_WHITEN_H_

#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tfacemaskdef.h"
#include "tskindetect.h"

#if !defined(TS_STATIC)
# if defined(_WIN32) && !defined(__GNUC__) 
#  ifdef MYDLL_DLL
#   define MYDLL_DECL __declspec(dllexport)
#  else
#   define MYDLL_DECL __declspec(dllimport)
#  endif
# else     /* UNIX */
#  if defined(MYDLL_DLL) && defined(HAS_GCC_VISIBILITY)
#   define MYDLL_DECL __attribute__((visibility("default")))
#  else
#   define MYDLL_DECL
#  endif
# endif
#else
# define MYDLL_DECL
#endif
#ifdef __cplusplus
extern "C" {
#endif

MYDLL_DECL TRESULT TFaceWhitening(LPTSOFFSCREEN pFaceImg, TMASK* pFaceMask, TRECT rcFace, TRECT rclefteye, TRECT rcrighteye, TLong lWhiteningLevel, THandle hMem);

MYDLL_DECL TRESULT TGETRGBAFaceMask(LPTSOFFSCREEN pFaceImg, TRECT rcFace, LPTSOFFSCREEN pFaceMaskImg, THandle hMem);

MYDLL_DECL TRESULT TFaceSkinColor(LPTSOFFSCREEN pFaceImg, TMASK* pFaceMask, TRECT rcFace, TLong lColorLevel, LPTCOLORMask pColor, THandle hMem);

MYDLL_DECL TRESULT TWhiteningAllRegion(LPTSOFFSCREEN pFaceImg, TLong lWhiteningLevel, THandle hMem);

#ifdef __cplusplus
}
#endif

#endif