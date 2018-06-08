/*
** TxImage.h
*/

#ifndef _TX_IMAGE_H_
#define _TX_IMAGE_H_

#include "TxConf.h"
#include "TxType.h"

#ifdef __cplusplus
extern "C" {;
#endif

//#define TX_IMAGE_COORDINATE_SYSTEM_0
/*
** 图像的原点为左上角
** ------------------------------->
   |             top
   |      _________________
   | left |               | right
   |      |               |
   |      |_______________|
   |            bottom
   |
   v
*/

typedef enum __tag_TxPixFmt
{
    TXE_PIX_FMT_RGB24_R8G8B8 = 0,
    TXE_PIX_FMT_RGB24_B8G8R8,
    TXE_PIX_FMT_NV21,
    TXE_PIX_FMT_YUV,
    TXE_PIX_FMT_GRAY,
} TxPixFmt;

typedef struct __tag_TxRGBA
{
    Uint08 r;
    Uint08 g;
    Uint08 b;
    Uint08 a;
} TxRGBA;

typedef enum __tag_TxHType
{
    TXE_HTYPE_CAMSHIFT_NONE = 0,
    TXE_HTYPE_CAMSHIFT_YUV,
    TXE_HTYPE_CAMSHIFT_HSV,
    TXE_HTYPE_NORMAL_Y
} TxHType;

typedef struct __tag_TxHist
{
    TxHType eType;
    Sint32* pData;
    Sint32  nSize;
} TxHist;

typedef enum __tag_TxOriFmt
{
    TXE_ORI_FMT_000 = 0,
    TXE_ORI_FMT_090,
    TXE_ORI_FMT_180,
    TXE_ORI_FMT_270
} TxOriFmt;

typedef struct __tag_TxImage
{
    TxPixFmt    ePixfmt;
    TxOriFmt    eOrifmt;
    Sint32      nWidth;
    Sint32      nHeight;
    Uint08*     pPlane[4];
    Sint32      nPitch[4];
} TxImage;

typedef struct __tag_TxMoments
{
    Sint64 m00;
    Sint64 m10, m01;
    Sint64 m20, m02, m11;
} TxMoments;

typedef struct __tag_TxHSV
{
    Uint08 h;
    Uint08 s;
    Uint08 v;
    Uint08 r;   // reserved
} TxHSV;

#define TXM_RGB2GRAY(r,g,b)     (((b)*117 + (g)*601 + (r)*306) >> 10)

typedef struct __tag_TxThld
{
    Uint08 min0, max0;
    Uint08 min1, max1;
    Uint08 min2, max2;
    Uint08 min3, max3;
} TxThld;

typedef struct __tag_TxRegion
{
    Float64 cx;     // center : x
    Float64 cy;     // center : y

    Sint32  nm;     // number of the pixels
    Sint32  id;     //
} TxRegion;

#ifdef __cplusplus
};
#endif

#endif /* _TX_IMAGE_H_ */
