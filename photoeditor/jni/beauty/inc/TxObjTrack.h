/*
** TxObjTrack.h
*/

#ifndef _TX_OBJ_TRACK_H_
#define _TX_OBJ_TRACK_H_

#include "TxKitBase.h"
#include "TxCamShift.h"
#include "TxObjDetect.h"

#ifdef __cplusplus
extern "C" {;
#endif

// Thread Mode
typedef enum __tag_TxOTMode
{
    TXE_OT_MODE_ST,
    TXE_OT_MODE_MT,
} TxOTMode;

// Single or Multiple Object Tracking
typedef enum __tag_TxOTFlag
{
    TXE_OT_FLAG_SO,
    TXE_OT_FLAG_MO,
} TxOTFlag;

typedef struct __tag_TxOTInfo
{
    TxOTFlag    otflag;
    TxOTMode    otmode;
    TxODInfo    odinfo[2];
    TxCSInfo    csinfo[__MAXO];
    
    Sint32      _obsize;
    TxObInfo    _lastob[__MAXO];
    TxImage     _lastim;
    Bool        _isused;

    Sint32      _doing;
    Sint32      _donne;

    Sint32      obsize;
    TxObInfo    lastob[__MAXO];

    TxObInfo    stabob[__MAXO];

    Void*       rwlock;
    Void*       rwattr;
    Void*       cdwait;
    Void*       cdattr;
    Void*       thread;

    // flow control
    Bool        doing;
    Bool        donne;
} TxOTInfo;

TXK_API TxResult TxObjTrackInit(const Schar* type, TxOTInfo* otinfo);

TXK_API TxResult TxObjTrackWork(const TxImage* image, TxOTInfo* otinfo);

TXK_API TxResult TxObjTrackExit(TxOTInfo* otinfo);

#ifdef __cplusplus
};
#endif

#endif /* _TX_OBJ_TRACK_H_ */
