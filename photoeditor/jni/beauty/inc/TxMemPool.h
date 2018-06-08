/*
** TxMemPool.h
*/

#ifndef _TX_MEM_POOL_H_
#define _TX_MEM_POOL_H_

#include "TxKitBase.h"

#ifdef __cplusplus
extern "C" {;
#endif

//内存块信息节点
typedef struct _TxMemPool
{
    Void*  pBase;
    Uint32 nSize;//字节数,总长度
    Uint32 nHead;//字节数,头指针
    Uint32 nUsed;//字节数,实际使用值
    Uint32 nMaxu;//字节数,最大使用值
    Uint32 nMaxa;//字节数,最大地址值
} TxMemPool;

TxMemPool* TxCreateMemPool(const Uint32 size);

Void TxDestroyMemPool(TxMemPool* pMemPool);

Void* TxSmallAlloc(TxMemPool* const pMemPool, const Uint32 size);

Bool TxSmallFree(TxMemPool* pMemPool, const Void* ptr);

//Bool SmallCheck(const TxMemPool* pMemPool, const Void* ptr, const Uint32 size);

Bool TxSmallCheck(const TxMemPool* pMemPool, const Void* ptr);

Bool TxSmallVerify(const TxMemPool * pMemPool);

Bool TxMemPoolGlobalCreate(Uint32 nSize);

Bool TxMemPoolGlobalDestroy();

Void* TxGlobalSmallAllocImpl(const Uint32 size);

Bool TxGlobalSmallFreeImpl(const Void* ptr);

TXK_API Void* TxGlobalSmallAlloc(const Uint32 size);

TXK_API Bool TxGlobalSmallFree(const Void* ptr);

#ifdef __cplusplus
};
#endif

#endif /* _TX_MEM_POOL_H_ */
