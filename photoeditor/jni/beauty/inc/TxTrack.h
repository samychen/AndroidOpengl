/*
** TxTrack.h
*/

#ifndef _TX_TRACK_H_
#define _TX_TRACK_H_

#include "TxKitBase.h"
#include "TxCamShift.h"

#ifdef __cplusplus
extern "C" {;
#endif

typedef struct __tag_TxTrackInfo
{
    // preporcess
    //Sint32      radf;   // filter radius
    Sint32      stat;
    TxRect      init;

    // camshift
    TxCSInfo    csinfo;


    // flex plsf
    TxMUint08   matY;
    //TMPlsF      fpre;
    //TMPlsF      fcur;
    Sint32      radv;   // PLSF's radius
    Sint32      step;   // PLSF's step
    Sint32      radc;   // comparison's radius
    TxRect      rpre;
    TxRect      rcur;

    Float64     rate;

    // flow control
    Sint32      doing;
    Sint32      donne;
} TxTrackInfo;

TXK_API TxResult TxTrackConf();

/*
** create track info, and calc the first step
*/
TXK_API TxResult TxTrackPrep(/*const TxImage* image, */TxTrackInfo* tinfo);

TXK_API TxResult TxTrackInit(const TxImage* image, const TxRect rect, TxTrackInfo* tinfo);

TXK_API TxResult TxTrackWork(const TxImage* image, TxTrackInfo* tinfo);

TXK_API TxResult TxTrackStop(TxTrackInfo* tinfo);

TXK_API TxResult TxTrackExit(TxTrackInfo* tinfo);

#ifdef __cplusplus
};
#endif

#endif /* _TX_TRACK_H_ */
