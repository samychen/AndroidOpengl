/*
** TxSkinSmooth.h
*/

#ifndef _TX_SKIN_SMOOTH_H_
#define _TX_SKIN_SMOOTH_H_

#include "TxKitBase.h"
#include "TxMatrix.h"

#ifdef __cplusplus
extern "C" {;
#endif

typedef struct __tag_TxMask
{
    Uint08* pData;
    Sint32  nWidth;
    Sint32  nHeight;
    Sint32  nPitch;
} TxMask;

//TXK_API TxResult TxSkinSmooth(const TxImage* src, const TxImage* dst, const TxMask* mask, const TxRect rect, const Sint32 level);
TXK_API TxResult TxSkinSmooth(const TxImage* src, const TxImage* dst, const TxMask* skin, const TxMask* feat, const Sint32 level);

TXK_API TxResult TxMaskSmooth(const TxMask* mask, const Sint32 radius);

TXK_API TxResult TxMaskSmoothRect(const TxMask* mask, const TxRect rect, const Sint32 radius);

#ifdef __cplusplus
};
#endif

#endif /*_TX_SKIN_SMOOTH_H_*/
