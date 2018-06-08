/*
** TxSkinDetect.h
*/

#ifndef _TX_SKIN_DETECT_H_
#define _TX_SKIN_DETECT_H_

#include "TxKitBase.h"

#ifdef __cplusplus
extern "C" {;
#endif

TXK_API TxResult TxSkinDetect(const TxImage* src, const TxImage* dst, TxRect rect, Sint32 level);

#ifdef __cplusplus
};
#endif

#endif /*_TX_SKIN_DETECT_H_*/
