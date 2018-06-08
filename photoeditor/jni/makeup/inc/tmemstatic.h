
#ifndef _TMMEMSTATIC_H_
#define _TMMEMSTATIC_H_
#include "tcomdef.h"

#ifdef __cplusplus
extern "C" {
#endif

TVoid*	TMemAllocStatic(THandle hContext, TDWord dwSize);
TVoid	TMemFreeStatic(THandle hContext, TVoid* pMem);

#ifdef __cplusplus
}
#endif

#endif//
