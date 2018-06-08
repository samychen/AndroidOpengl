/*
** TxObjDetect.h
*/

#ifndef _TX_OBJ_DETECT_H_
#define _TX_OBJ_DETECT_H_

#include "TxKitBase.h"

#ifdef __cplusplus
extern "C" {;
#endif

#define __MAXO    8

// Single or Multiple Object Detect
typedef enum __tag_TxODMode
{
    TXE_OD_MODE_SO, // Single OD
    TXE_OD_MODE_MO, // Multiple OD
} TxODMode;

typedef struct __tag_TxODConf
{
    TxODMode mode;
    //
    Sint32  smin;
    Sint32  smax;
    Float32 step;
    //
    Sint32  wsize[16];
    Sint32  wstep[16];
    Sint32  wlgth;
} TxODConf;

typedef struct __tag_TxObInfo
{
    Sint32  f;
    Sint32  x;
    Sint32  y;
    Sint32  w;
    Sint32  h;
    Float32 v;
} TxObInfo;

typedef struct __tag_TxODInfo
{
    Schar       odtype[256];
    TxODConf    odconf;

    Void*       hdetor;
    
    Sint32      obsize;
    TxObInfo    obinfo[__MAXO];
} TxODInfo;

TXK_API Sint32 TxObjDetectCalcStep(const Sint32 size);

TXK_API TxResult TxObjDetectFillConfSL(const TxODMode mode, const Sint32 size, const Sint32 levels, TxODConf* conf);

TXK_API TxResult TxObjDetectFillConfML(const TxODMode mode, const Sint32 smin, const Sint32 smax, const Sint32 levels, TxODConf* conf);

TXK_API TxResult TxObjDetectInit(const Schar* type, TxODInfo* odinfo);

TXK_API TxResult TxObjDetectWork(const TxImage* image, TxODInfo* odinfo);

TXK_API TxResult TxObjDetectExit(TxODInfo* odinfo);

#ifdef __cplusplus
};
#endif

#endif /* _TX_OBJ_DETECT_H_ */
