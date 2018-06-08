/*
** TxVector.h
*/

#ifndef _TX_VECTOR_H_
#define _TX_VECTOR_H_

#include "TxKitBase.h"
#include "TxMemory.h"

#ifdef __cplusplus
extern "C" {;
#endif

typedef struct __tag_TxVector
{
    Void*   pData;
    Sint32  nElen;
    Sint32  nSize;
} TxVector;

TXK_API Void TxVectorMake(TxVector* pVector, const Sint32 nElen, const Sint32 nSize);

TXK_API Void TxVectorFree(TxVector* pVector);

TXK_API Void* TxVectorElem(TxVector* pVector, const Sint32 nIdx);

#define TXM_VECTOR_DEFINE(_type) \
struct\
{\
    union\
    {\
        _type* pType;\
        Void*  pVoid;\
    } uData;\
    Sint32 nSize;\
}

typedef TXM_VECTOR_DEFINE(Uint08)   TxVUint08;
typedef TXM_VECTOR_DEFINE(Uint32)   TxVUint32;
typedef TXM_VECTOR_DEFINE(Sint08)   TxVSint08;
typedef TXM_VECTOR_DEFINE(Sint32)   TxVSint32;
typedef TXM_VECTOR_DEFINE(Float32)  TxVFloat32;
typedef TXM_VECTOR_DEFINE(Float64)  TxVFloat64;

#define TXM_VECTOR_INIT(_vector)    \
{\
    (_vector)->uData.pVoid = TXC_NULL;\
    (_vector)->nSize = 0;\
}

#define TXM_VECTOR_ZERO(_vector)    TXM_MEM_ZERO((_vector)->uData.pVoid, (_vector)->nSize * sizeof(*(_vector)->uData.pType))

#define TXM_VECTOR_MAKE(_vector, _size)     \
{\
    if ((_size) > 0)\
    {\
        (_vector)->uData.pVoid = TXM_MEM_ALLOC((_size) * sizeof(*(_vector)->uData.pType));\
        if ((_vector)->uData.pVoid)\
        {\
            (_vector)->nSize = (_size);\
            TXM_MEM_ZERO((_vector)->uData.pVoid, (_size) * sizeof(*(_vector)->uData.pType));\
        }\
        else\
        {\
            (_vector)->nSize = 0;\
        }\
    }\
}\

//#define TXM_VECTOR_CONV_TYPE(_dst, _type, _src) \
//{\
//    if ((_dst)->nSize == (_src)->nSize)\
//    {\
//        int i;\
//        for (i = 0; i < (_dst)->nSize; ++i)\
//            *((_dst)->data + i) = (_type)(*((_src)->data + i));\
//    }\
//}

#define TXM_VECTOR_FREE(_vector) \
{\
    if ((_vector)->uData.pVoid)\
    {\
        TXM_MEM_FREE((_vector)->uData.pVoid);\
        (_vector)->uData.pVoid = TXC_NULL;\
        (_vector)->nSize = 0;\
    }\
}

#define TXM_VECTOR_RESIZE(_vector, _size)   \
{\
    TXM_VECTOR_FREE(_vector);\
    TXM_VECTOR_MAKE(_vector, _size);\
}

#define TXM_VECTOR_COPY(_dst, _src) \
{\
    memcpy((_dst)->uData.pVoid, (_src)->uData.pVoid, (_src)->nSize * sizeof(*(_src)->uData.pType));\
}

#define TXM_VECTOR_ELEM(_vector, _index)  \
    ((_vector)->uData.pType + (_index))

#define TXM_VECTOR_ELEM_S(_vector, _index)  \
    ((_vector)->uData.pType + ((_index) < 0) ? 0 : (((_index) < (_vector)->nSize) ? (_index) : ((_vector)->nSize - 1))

#ifdef __cplusplus
};
#endif

#endif /* _TX_VECTOR_H_ */
