/*
** TxType.h
*/

#ifndef _TX_TYPE_H_
#define _TX_TYPE_H_

#if defined (WIN32) || defined(_WINDOWS)
#include <tchar.h>
#else
//#include
#endif /* WIN32 || _WINDOWS */

#ifdef __cplusplus
extern "C" {;
#endif

#ifdef UNICODE
//typedef wchar_t             tchar;
#define TXM_STRING(x)       L ## x
#else
//typedef char                tchar;
#define TXM_STRING(x)       x
#endif

#ifndef TXC_FALSE
#define TXC_FALSE           0
#endif

#ifndef TXC_TRUE
#define TXC_TRUE            1
#endif

#ifndef TXP_IN
#define TXP_IN
#endif // TXP_IN

#ifndef TXP_OUT
#define TXP_OUT
#endif // TXP_OUT

#ifndef TXP_REF
#define TXP_REF
#endif // TXP_REF

#ifndef TXC_NULL
#define TXC_NULL    0
#endif // TXC_NULL

typedef void*               TxHandle;

typedef char                Schar;
typedef unsigned char       Uchar;

#if defined (WIN32) || defined(_WINDOWS)
//typedef wchar_t             Wchar;
//typedef unsigned char       Byte;
//#ifdef UNICODE
//typedef Wchar               Tchar;
//#else
//typedef Schar               Tchar;
//#endif
#endif /* WIN32 || _WINDOWS */

typedef float               Float32;
typedef double              Float64;

#if defined (WIN32) || defined(_WINDOWS)
typedef __int8              Sint08;
#else
typedef char                Sint08;
#endif /* WIN32 || _WINDOWS */
typedef short               Sint16;
typedef int                 Sint32;
#if defined (WIN32) || defined(_WINDOWS)
typedef __int64             Sint64;
#else
typedef signed long long    Sint64;
#endif /* WIN32 || _WINDOWS */

typedef unsigned char       Uint08;
typedef unsigned short      Uint16;
typedef unsigned long       Uint32;
#if defined (WIN32) || defined(_WINDOWS)
typedef unsigned __int64    Uint64;
#else
typedef unsigned long long  Uint64;
#endif /* WIN32 || _WINDOWS */

typedef Sint32              Bool;
typedef void                Void;

//typedef uint32              size_t;
//typedef uint32              index_t;
//typedef sint32              offset_t;
//typedef sint64              diff_t;
//typedef sint64              dist_t;
//typedef sint64              ptrdiff_t;

//typedef sint32              bool;

typedef Sint32              TxResult;

typedef struct __tag_TxPoint
{
    Sint32 x;
    Sint32 y;
} TxPoint;

typedef struct __tag_TxPointf
{
    Float64 x;
    Float64 y;
} TxPointf;

typedef struct __tag_TxRect
{
    //sint32 lft;     // left
    //sint32 top;     // top
    //sint32 rht;     // right
    //sint32 bot;     // bottom
    Sint32 x0;     // left
    Sint32 y0;     // top
    Sint32 x1;     // right
    Sint32 y1;     // bottom
} TxRect;

typedef struct __tag_TxRange
{
    Sint32 nBeg;
    Sint32 nEnd;
} TxRange;

// complex type
typedef struct __tag_TxCplx
{
    Float64 fReal;
    Float64 fImag;
} TxCplx;

typedef struct __tag_TxPoint2D
{
    Float64 x;
    Float64 y;
} TxPoint2D;

#ifdef __cplusplus
};
#endif

#endif /* _TX_TYPE_H_ */
