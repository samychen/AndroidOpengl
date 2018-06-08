/*
** TxMemory.h
*/

#ifndef _TX_MEMORY_H_
#define _TX_MEMORY_H_

#include "TxKitBase.h"

//#define __TXC_USE_MEM_POOL__
#ifdef __TXC_USE_MEM_POOL__
#include "TxMemPool.h"
#else
#if defined(_WIN32) || defined (__ANDROID__)
#include <malloc.h>
#include <memory.h>
#else
#include <stdlib.h>
#endif
#endif // __TXC_USE_MEM_POOL__

#include <string.h>

#ifdef __cplusplus
extern "C" {;
#endif

///* memory operations */
#ifdef __TXC_USE_MEM_POOL__
#define TXM_MEM_ALLOC(_size)                    TxGlobalSmallAlloc((_size))
#define TXM_MEM_FREE(_p)                        TxGlobalSmallFree((_p))
#else
#define TXM_MEM_ALLOC(_size)                    malloc((_size))
#define TXM_MEM_FREE(_p)                        free((_p))
#endif // __TXC_USE_MEM_POOL__

#define TXM_MEM_COPY(_dst, _src, _size)         memcpy((_dst), (_src), (_size))
#define TXM_MEM_ZERO(_p, _size)                 memset((_p), 0, (_size))

#define TXM_MEM_SET(_p, _v, _size)              memset((_p), (_v), (_size))

#define TXM_MEM_OFFSET(_base, _count, _elen)    ((Uint08*)(_base) + (_count) * (_elen))

#define TXM_MEM_SWAP(_pa, _pb, _size)   \
{\
    if ((_pa) != (_pb))\
    {\
        Sint32 size = (_size);\
        while (size > 0)\
        {\
            size--;\
            *((Uchar*)(_pa) + size) ^= *((Uchar*)(_pb) + size);\
            *((Uchar*)(_pb) + size) ^= *((Uchar*)(_pa) + size);\
            *((Uchar*)(_pa) + size) ^= *((Uchar*)(_pb) + size);\
        }\
    }\
}

#ifdef __cplusplus
};
#endif

#endif /* _TX_MEMORY_H_ */
