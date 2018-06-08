
#ifndef __TMMEM_H__
#define __TMMEM_H__

#include "tcomdef.h"

typedef struct	__tag_mem_info
{
	TDWord	dwTotalMemUsed;
	TDWord	dwTotalMemFree;
}TMEMINFO, *LPTMEMINFO;


#ifdef __cplusplus
extern "C" {
#endif



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





#ifdef __cplusplus
}
#endif

#endif

