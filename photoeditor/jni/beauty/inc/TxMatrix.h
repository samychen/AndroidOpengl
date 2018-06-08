/*
** TxMatrix.h
*/

#ifndef _TX_MATRIX_H_
#define _TX_MATRIX_H_

#include "TxKitBase.h"
#include "TxMemory.h"

#ifdef __cplusplus
extern "C" {;
#endif

//typedef struct __tag_TMatrix
//{
//    Void* data;
//    Sint32 elen;
//    Sint32 rows;
//    Sint32 cols;
//} TMatrix;
//
//TXK_API
//Void xm_matrix_make(TMatrix* matrix, const Sint32 elen, const Sint32 rows, const Sint32 cols);
//
//TXK_API
//Void xm_matrix_free(TMatrix* matrix);
//
//TXK_API
//Void* xm_matrix_elem(TMatrix* matrix, const Sint32 row, const Sint32 col);

typedef enum __tag_TxMajor
{
    TXE_MAJOR_ROW = 0,
    TXE_MAJOR_COL
} TxMajor;

#define TXM_MATRIX_DEFINE(_type)    \
struct\
{\
    union\
    {\
        _type* pType;\
        Void*  pVoid;\
    } uData;\
    Sint32 nRows;\
    Sint32 nCols;\
    TxMajor eMode;\
}

typedef TXM_MATRIX_DEFINE(Sint08)   TxMSint08;
typedef TXM_MATRIX_DEFINE(Uint08)   TxMUint08;

typedef TXM_MATRIX_DEFINE(Sint32)   TxMSint32;
typedef TXM_MATRIX_DEFINE(Uint32)   TxMUint32;

typedef TXM_MATRIX_DEFINE(Sint64)   TxMSint64;
typedef TXM_MATRIX_DEFINE(Uint64)   TxMUint64;

typedef TXM_MATRIX_DEFINE(Float32)  TxMFloat32;
typedef TXM_MATRIX_DEFINE(Float64)  TxMFloat64;

#define TXM_MATRIX_INIT(_matrix) \
{\
    (_matrix)->uData.pVoid = NULL;\
    (_matrix)->nRows = 0;\
    (_matrix)->nCols = 0;\
    (_matrix)->eMode = TXE_MAJOR_ROW;\
}

#define TXM_MATRIX_ZERO(_matrix)    TXM_MEM_ZERO((_matrix)->uData.pVoid, (_matrix)->nRows * (_matrix)->nCols * sizeof(*(_matrix)->uData.pType))

#define TXM_MATRIX_MAKE(_matrix, _rows, _cols)  \
{\
    (_matrix)->uData.pVoid = TXM_MEM_ALLOC((_rows) * (_cols) * sizeof(*(_matrix)->uData.pType));\
    if ((_matrix)->uData.pVoid)\
    {\
        TXM_MEM_ZERO((_matrix)->uData.pVoid, (_rows) * (_cols) * sizeof(*(_matrix)->uData.pType));\
        (_matrix)->nRows = (_rows);\
        (_matrix)->nCols = (_cols);\
        (_matrix)->eMode = TXE_MAJOR_ROW;\
    }\
    else\
    {\
        (_matrix)->nRows = 0;\
        (_matrix)->nCols = 0;\
        (_matrix)->eMode = TXE_MAJOR_ROW;\
    }\
}

//#define TXM_MATRIX_SIZE(_src)  ((_src)->nRows * (_src)->nCols)

//#define TXM_MATRIX_CONV_TYPE(_dst, _type, _src) \
//{\
//    Sint32 i;\
//    Sint32 size = (_src)->nRows * (_src)->nCols;\
//    for (i = 0; i < size; ++i)\
//    {\
//        *((_dst)->data + i) = (_type)(*((_src)->data + i));\
//    }\
//}

#define TXM_MATRIX_FREE(_matrix)  \
{\
    if ((_matrix)->uData.pVoid)\
    {\
        TXM_MEM_FREE((_matrix)->uData.pVoid);\
        (_matrix)->uData.pVoid = NULL;\
        (_matrix)->nRows = 0;\
        (_matrix)->nCols = 0;\
        (_matrix)->eMode = TXE_MAJOR_ROW;\
    }\
}

#define TXM_MATRIX_RESIZE(_matrix, _rows, _cols) \
{\
    TXM_MATRIX_FREE(_matrix);\
    TXM_MATRIX_MAKE(_matrix, _rows, _cols);\
}

#define TXM_MATRIX_COPY(_dst, _src) \
{\
    memcpy((_dst)->uData.pVoid, (_src)->uData.pVoid, (_src)->nRows * (_src)->nCols * sizeof(*(_src)->uData.pType));\
}

#define TXM_MATRIX_ELEM(_matrix, _row, _col)  \
    ((_matrix)->uData.pType + (_row) * (_matrix)->nCols + (_col))
//#define TXM_MATRIX_ELEM(_matrix, _row, _col)  \
//    ((TXE_MAJOR_ROW == (_matrix)->eMode) ? ((_matrix)->uData.pType + (_row) * (_matrix)->nCols + (_col)) : ((_matrix)->uData.pType + (_col) * (_matrix)->nRows + (_row)))

#define TXM_MATRIX_ELEM_RM(_matrix, _row, _col)  \
    ((_matrix)->uData.pType + (_row) * (_matrix)->nCols + (_col))

#define TXM_MATRIX_ELEM_CM(_matrix, _row, _col)  \
    ((_matrix)->uData.pType + (_col) * (_matrix)->nRows + (_row))

#define TXM_MATRIX_ELEM_S(_matrix, _row, _col) \
    ((_matrix)->uData.pType + \
    (((_row) < 0) ? 0 : (((_row) < (_matrix)->nRows) ? (_row) : ((_matrix)->nRows - 1))) * (_matrix)->nCols + \
    (((_col) < 0) ? 0 : (((_col) < (_matrix)->nCols) ? (_col) : ((_matrix)->nCols - 1))))

#define TXM_MATRIX_AS_VECTOR_ELEM(_matrix, _index) \
    ((_matrix)->uData.pType + (_index))

#define TXM_MATRIX_AND(_left, _right)    \
{\
    Sint32 i, j;\
    for (i = 0; i < (_left)->nRows; ++i)\
    {\
        for (j = 0; j < (_left)->nCols; ++j)\
        {\
            if (0 == *TXM_MATRIX_ELEM((_right), i, j)) *TXM_MATRIX_ELEM((_left), i, j) = 0;\
            /**TXM_MATRIX_ELEM((_left), i, j) &= *TXM_MATRIX_ELEM((_right), i, j);*/\
        }\
    }\
}

//#define TXM_MATRIX_EXPAND(_dst, _src, _eh, _ew) \
//{\
//    Sint32 i, j;\
//    for (i = 0; i < (_dst)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_dst)->nCols; ++j)\
//        {\
//            TXM_MATRIX_ELEM((_dst), i, j) = TXM_MATRIX_ELEM_S((_src), i - _eh, j - _ew);\
//        }\
//    }\
//}

//#define TXM_MATRIX_TRANSPOSE(_src, _type) \
//{\
//    Sint32 i, j, k;\
//    Sint32 size = (_src)->nRows * (_src)->nCols;\
//    _type* p = (_type*)calloc(size, sizeof(_type));\
//    memcpy(p, (_src)->data, size * sizeof(_type));\
//    k = (_src)->nRows;\
//    (_src)->nRows = (_src)->nCols;\
//    (_src)->nCols = k;\
//    for (k = 0, j = 0; j < (_src)->nCols; ++j)\
//    {\
//        for (i = 0; i < (_src)->nRows; ++i)\
//        {\
//            TXM_MATRIX_ELEM((_src), i, j) = p[k];\
//            k++;\
//        }\
//    }\
//    free(p);\
//}

//#define TXM_MATRIX_TRANSPOSE(_matrix) \
//{\
//    Sint32 i, j, k;\
//    Sint32 size = (_src)->nRows * (_src)->nCols;\
//    _type* p = (_type*)calloc(size, sizeof(_type));\
//    memcpy(p, (_src)->data, size * sizeof(_type));\
//    k = (_src)->nRows;\
//    (_src)->nRows = (_src)->nCols;\
//    (_src)->nCols = k;\
//    for (k = 0, j = 0; j < (_src)->nCols; ++j)\
//    {\
//        for (i = 0; i < (_src)->nRows; ++i)\
//        {\
//            TXM_MATRIX_ELEM((_src), i, j) = p[k];\
//            k++;\
//        }\
//    }\
//    free(p);\
//}

//#define TXM_MATRIX_SUBTRACT(_left, _right) \
//{\
//    Sint32 i, j;\
//    for (i = 0; i < (_left)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_left)->nCols; ++j)\
//        {\
//            TXM_MATRIX_ELEM((_left), i, j) -= TXM_MATRIX_ELEM((_right), i, j);\
//        }\
//    }\
//}

//#define TXM_MATRIX_ADD(_left, _right) \
//{\
//    Sint32 i, j;\
//    for (i = 0; i < (_left)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_left)->nCols; ++j)\
//        {\
//            TXM_MATRIX_ELEM((_left), i, j) += TXM_MATRIX_ELEM((_right), i, j);\
//        }\
//    }\
//}
//
//#define TXM_MATRIX_DOT_MULTIPLY(_left, _right) \
//{\
//    Sint32 i, j;\
//    for (i = 0; i < (_left)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_left)->nCols; ++j)\
//        {\
//            Float64 v = TXM_MATRIX_ELEM((_left), i, j) * TXM_MATRIX_ELEM((_right), i, j);\
//            TXM_MATRIX_ELEM((_left), i, j) = (v);\
//        }\
//    }\
//}
//
//#define TXM_MATRIX_STAR_MULTIPLY(_result, _left, _right) \
//{\
//    Sint32 i, j, k;\
//    Float64 v;\
//    for (i = 0; i < (_result)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_result)->nCols; ++j)\
//        {\
//            v = 0.0;\
//            for (k = 0; k < (_left)->nCols; k++)\
//                v += TXM_MATRIX_ELEM((_left), i, k) * TXM_MATRIX_ELEM((_right), k, j);\
//            TXM_MATRIX_ELEM((_result), i, j) = (v);\
//        }\
//    }\
//}
//
//#define TXM_MATRIX_ARITH_MEAN(_left, _right) \
//{\
//    Sint32 i, j;\
//    for (i = 0; i < (_left)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_left)->nCols; ++j)\
//        {\
//            Float64 v = TXM_MATRIX_ELEM((_left), i, j) + TXM_MATRIX_ELEM((_right), i, j);\
//            TXM_MATRIX_ELEM((_left), i, j) = (v / 2.0);\
//        }\
//    }\
//}
//
//#define TXM_MATRIX_GEOM_MEAN(_left, _right) \
//{\
//    Sint32 i, j;\
//    for (i = 0; i < (_left)->nRows; ++i)\
//    {\
//        for (j = 0; j < (_left)->nCols; ++j)\
//        {\
//            Float64 v = TXM_MATRIX_ELEM((_left), i, j) * TXM_MATRIX_ELEM((_right), i, j);\
//            TXM_MATRIX_ELEM((_left), i, j) = sqrt(v);\
//        }\
//    }\
//}


#ifdef __cplusplus
};
#endif

#endif /* _TX_MATRIX_H_ */
