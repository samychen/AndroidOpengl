#ifndef __TMMEM_H__
#define __TMMEM_H__

#include "tcomdef.h"
#define SAK_MEMORY_SWITCH_PLATFORM

typedef struct	__tag_mem_info
{
	TDWord	dwTotalMemUsed;
	TDWord	dwTotalMemFree;
}TMEMINFO, *LPTMEMINFO;


#ifdef __cplusplus
extern "C" {
#  define TINLINE inline
#else
#  define TINLINE static
#endif

#ifdef SAK_MEMORY_SWITCH_PLATFORM

TVoid*	TMemAlloc(THandle hContext, TLong lSize);
TVoid*	TMemRealloc(THandle hContext, TVoid* pMem, TLong lSize);
TVoid	TMemFree(THandle hContext, TVoid* pMem);
TVoid	TMemSet(TVoid* pMem, TByte byVal, TLong lSize);
TVoid	TMemCpy(TVoid* pDst, const TVoid* pSrc, TLong lSize);
TVoid	TMemMove(TVoid* pDst, TVoid* pSrc, TLong lSize);
TLong	TMemCmp(TVoid* pBuf1, TVoid* pBuf2, TLong lSize);

THandle TMemMgrCreate(TVoid* pMem, TLong lMemSize);
TVoid	TMemMgrDestroy(THandle hMemMgr);	

TBool	TGetMemInfo(LPTMEMINFO pMemInfo);

TINLINE TVoid* TMemCalloc(THandle hContext, TLong size, TLong esize) 
{
    TVoid* p = TMemAlloc(hContext, size * esize);
    if (p) 
        TMemSet(p, 0, size * esize);
    return p;
}

#else

#include <stdlib.h>
#include <string.h>

#define TMemAlloc(hContext, lSize)            malloc(lSize)
#define TMemCalloc(hContext, size, esize)     calloc(size, esize)
#define TMemRealloc(hContext, pMem, lSize)    realloc(pMem, lSize)
#define TMemFree(hContext, pMem)              free(pMem)
#define TMemSet(pMem, byVal, lSize)           memset((pMem), (byVal), (lSize))
#define TMemCpy(pDst, pSrc, lSize)            memcpy((pDst), (pSrc), (lSize))
#define TMemMove(pDst, pSrc, lSize)           memmove((pDst), (pSrc), (lSize))
#define TMemCmp(pBuf1, pBuf2, lSize)          memcmp((pBuf1), (pBuf2), (lSize))

#define TMemMgrCreate(pMem, lMemSize)         ((THandle)(pMem))
#define TMemMgrDestroy(hMemMgr)

#define TGetMemInfo(pMemInfo)                 (0)

#endif



#ifdef __cplusplus
}
#endif

#undef TINLINE

#endif

