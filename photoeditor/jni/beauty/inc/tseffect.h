/*
** tseffect.h
*/

#ifndef __TS_EFFECT_H__
#define __TS_EFFECT_H__

#include <tcomdef.h>
#include <tsoffscreen.h>

#ifdef __cplusplus
extern "C" {
#endif

TRESULT TsEffectRectDenoise(const TSOFFSCREEN src, const TSOFFSCREEN dst, TRECT rect, TInt32 param);

TRESULT TsEffectSkinDenoise(THandle hMemMgr, const TSOFFSCREEN src, const TSOFFSCREEN dst, TUInt8* pSkinMask, TInt32 lMaskW, TInt32 lMaskH, TInt32 lMaskLine, TRECT rcFace, TInt32 param);

#ifdef __cplusplus
}
#endif

#endif /* __TS_EFFECT_H__ */
