/*
** TxMemPool.h
*/

#ifndef _TX_MEM_POOL_H_
#define _TX_MEM_POOL_H_

#include "TxKitBase.h"

#ifdef __cplusplus
extern "C" {;
#endif

//�ڴ����Ϣ�ڵ�
typedef struct _TxMemPool
{
    Void*  pBase;
    Uint32 nSize;//�ֽ���,�ܳ���
    Uint32 nHead;//�ֽ���,ͷָ��
    Uint32 nUsed;//�ֽ���,ʵ��ʹ��ֵ
    Uint32 nMaxu;//�ֽ���,���ʹ��ֵ
    Uint32 nMaxa;//�ֽ���,����ֵַ
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
