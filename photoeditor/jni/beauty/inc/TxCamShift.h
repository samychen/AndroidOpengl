/*
** TxCamShift.h
*/

#ifndef _TX_CAMSHIFT_H_
#define _TX_CAMSHIFT_H_

#include "TxKitBase.h"
#include "TxMatrix.h"

#ifdef __cplusplus
extern "C" {;
#endif

typedef enum __tag_TxCSFlow
{
    TXE_CS_FLOW_NONE = 0,
    TXE_CS_FLOW_PREP,
    TXE_CS_FLOW_INIT,
    TXE_CS_FLOW_WORK,
    //TXE_CS_FLOW_STOP,
    TXE_CS_FLOW_EXIT,
} TxCSFlow;

typedef enum __tag_TxCSMode
{
    TXE_CS_MODE_NONE = 0,
    TXE_CS_MODE_AUTO,
    TXE_CS_MODE_CALC,
    TXE_CS_MODE_WORK
} TxCSMode;

typedef struct __tag_TxCSInfo
{
    Bool        flag;   // 是否有正确结果
    TxCSFlow    flow;
    TxCSMode    mode;

    TxRect      rcin;
    //TxRect      rcex;

    TxMSint32   matP;
    TxOriFmt    eOri;
    Sint32      mean;   //matP的平均点概率密度，初始化为-1，为后续计算打标记

    TxHType     type;
    TxHist      hist;   // hist of ROI
    TxHist      htfg;   // hist of foreground
    TxHist      htbg;   // hist of background

    Sint32      mscx;
    Sint32      mscy;

    Sint32      iter;
    Sint32      dist;

    // flow control
    Bool        doing;
    Bool        donne;
} TxCSInfo;

TXK_API TxResult TxCamShiftPrep(TxCSInfo* csinfo);

TXK_API TxResult TxCamShiftInit(const TxImage* image, const TxRect rect, TxCSInfo* csinfo);

TXK_API TxResult TxCamShiftWork(const TxImage* image, TxCSInfo* csinfo);

TXK_API TxResult TxCamShiftStop(TxCSInfo* csinfo);

TXK_API TxResult TxCamShiftExit(TxCSInfo* csinfo);

#ifdef __cplusplus
};
#endif

#endif /* _TX_CAMSHIFT_H_ */
