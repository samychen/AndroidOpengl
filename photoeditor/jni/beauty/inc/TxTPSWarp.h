/*
** TxTPSWarp.h
*/

#ifndef _TX_TPS_WARP_H_
#define _TX_TPS_WARP_H_

#include "TxKitBase.h"
#include "TxMatrix.h"
#include "TxVector.h"

#ifdef __cplusplus
extern "C" {;
#endif

typedef struct __tag_TxTWInfo
{
    Sint32      num;
    Sint32      dim;
    TxMFloat64  src;
    TxMFloat64  dst;
    TxMFloat64  txm;
} TxTWInfo;

TXK_API TxResult TxTPSWarpInit(const Sint32 num, const Sint32 dim, TxTWInfo* twi);

TXK_API TxResult TxTPSWarpComputeCoef(TxTWInfo* twi);

TXK_API TxResult TxTPSWarpInterpolate(const TxTWInfo* twi, const TxVFloat64* src, TxVFloat64* dst);

TXK_API TxResult TxTPSWarpExit(TxTWInfo* twi);

#ifdef __cplusplus
};
#endif

#endif /* _TX_TPS_WARP_H_ */
