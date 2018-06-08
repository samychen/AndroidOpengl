/*
** TxAlgo.h
*/

#ifndef _TX_ALGO_H_
#define _TX_ALGO_H_

#include "TxKitBase.h"

#ifdef __cplusplus
extern "C" {;
#endif

TXK_API Void TxSwap(const Void* pa, const Void* pb, Sint32 size);

TXK_API TxResult TxQsort(Void* base, const Sint32 count, const Sint32 elen, Sint32 (*compare)(const Void*, const Void*));

TXK_API Void* TxQselect(Void* base, const Sint32 count, const Sint32 elen, const Sint32 idx, Sint32 (*compare)(const Void*, const Void*));

#ifdef __cplusplus
};
#endif

#endif /*_TX_ALGO_H_*/
