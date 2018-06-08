/*
** TxFaceWarp.h
*/

#ifndef _TX_FACE_WARP_H_
#define _TX_FACE_WARP_H_

#include "TxKitBase.h"
#include "TxMatrix.h"
#include "TxVector.h"

#include "TxTPSWarp.h"

#ifdef __cplusplus
extern "C" {;
#endif

typedef struct __tag_TxFWInfo
{
    Sint32   num;
    TxImage  src;
    TxImage  dst;
    TxRect   roi;
    TxTWInfo twif;      // foreward
    TxTWInfo twib;      // backward
    TxMFloat64 mapf;    // foreward
    TxMFloat64 mapb;    // backward
} TxFWInfo;

TXK_API TxResult TxFaceWarpInit(const Sint32 frnum, const Sint32 ptnum, TxFWInfo* fwi);

TXK_API TxResult TxFaceWarpSetS(const TxImage* src, const TxMFloat64* pts, TxFWInfo* fwi);

TXK_API TxResult TxFaceWarpSetD(const TxImage* dst, const TxMFloat64* pts, TxFWInfo* fwi);

TXK_API TxResult TxFaceWarpWork(TxFWInfo* fwi, const TxRect* roi);

TXK_API TxResult TxFaceWarpAskI(const TxFWInfo* fwi, const Sint32 idx, TxImage* image);

TXK_API TxResult TxFaceWarpExit(TxFWInfo* fwi);

#ifdef __cplusplus
};
#endif

#endif /* _TX_FACE_WARP_H_ */
