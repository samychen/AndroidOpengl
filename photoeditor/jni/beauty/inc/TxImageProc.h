/*
** TxImageProc.h
*/

#ifndef _TX_IMAGE_PROC_H_
#define _TX_IMAGE_PROC_H_

#include "TxKitBase.h"
#include "TxMatrix.h"
#include "TxVector.h"

#ifdef __cplusplus
extern "C" {;
#endif

#define TXM_NV21_GET_Y_PTR(image, x, y)     ((image)->pPlane[0] + (y) * (image)->nPitch[0] + (x))
#define TXM_NV21_GET_U_PTR(image, x, y)     ((image)->pPlane[1] + ((y) >> 1) * (image)->nPitch[1] + (((x) >> 1) << 1) + 1)
#define TXM_NV21_GET_V_PTR(image, x, y)     ((image)->pPlane[1] + ((y) >> 1) * (image)->nPitch[1] + (((x) >> 1) << 1))

#define TXM_NV21_NEXT_Y_PTR(ptr, x)         (++(ptr))
#define TXM_NV21_NEXT_U_PTR(ptr, x)         ((ptr) += (((x) & 0x01) << 1))
#define TXM_NV21_NEXT_V_PTR(ptr, x)         ((ptr) += (((x) & 0x01) << 1))

#define TXC_HIST_BINS   16

#if TXC_HIST_BINS == 11
    #define TXC_Y_BINS  11
    #define TXC_U_BINS  11
    #define TXC_V_BINS  11
    #define TXC_Y_STEP  (255 / TXC_Y_BINS + 1)
    #define TXC_U_STEP  (255 / TXC_U_BINS + 1)
    #define TXC_V_STEP  (255 / TXC_V_BINS + 1)
    #define TXC_CAMSHIFT_YUV_BINS       (TXC_Y_BINS * TXC_U_BINS * TXC_V_BINS)
    #define TXM_HIST_OFFSET(y, u, v)    (((y) / TXC_Y_STEP) * TXC_U_BINS * TXC_V_BINS + ((u) / TXC_U_STEP) * TXC_V_BINS + (v) / TXC_V_STEP)
#elif TXC_HIST_BINS == 16
    #define TXC_Y_BINS  16
    #define TXC_U_BINS  16
    #define TXC_V_BINS  16
    #define TXC_Y_STEP  (255 / TXC_Y_BINS + 1)
    #define TXC_U_STEP  (255 / TXC_U_BINS + 1)
    #define TXC_V_STEP  (255 / TXC_V_BINS + 1)
    #define TXC_CAMSHIFT_YUV_BINS       (TXC_Y_BINS * TXC_U_BINS * TXC_V_BINS)
    #define TXM_HIST_OFFSET(y, u, v)    ((((y) & 0xFFFFFFF0) << 4) + ((u) & 0xFFFFFFF0) + ((v) >> 4))
#elif TXC_HIST_BINS == 8
    #define TXC_Y_BINS  8
    #define TXC_U_BINS  8
    #define TXC_V_BINS  8
    #define TXC_Y_STEP  (255 / TXC_Y_BINS + 1)
    #define TXC_U_STEP  (255 / TXC_U_BINS + 1)
    #define TXC_V_STEP  (255 / TXC_V_BINS + 1)
    #define TXC_CAMSHIFT_YUV_BINS       (TXC_Y_BINS * TXC_U_BINS * TXC_V_BINS)
    #define TXM_HIST_OFFSET(y, u, v)    ((((y) & 0xFFFFFFC0) << 1) + ((u) >> 2) + ((v) >> 5))
#endif // TXC_HIST_BINS

#define TXC_H_Y_BINS                64
#define TXC_H_U_BINS                64
#define TXC_H_V_BINS                64
#define TXC_H_Y_STEP                (255 / TXC_H_Y_BINS)
#define TXC_H_U_STEP                (255 / TXC_H_U_BINS)
#define TXC_H_V_STEP                (255 / TXC_H_V_BINS)
#define TXC_CAMSHIFT_HUE_BINS       48

typedef struct __tag_TxColor
{
    Uint08 v0;  // y; r
    Uint08 v1;  // u; g
    Uint08 v2;  // v; b
    Uint08 v3;  // .; a
} TxColor;

TXK_API TxColor TxColorYUV2RGB(TxColor yuv);

TXK_API TxColor TxColorRGB2YUV(TxColor rgb);

TXK_API TxResult TxImageMake(TxImage* image, const TxPixFmt pixfmt, const Sint32 width, const Sint32 height);

TXK_API TxResult TxImageFree(TxImage* image);

TXK_API TxResult TxImageCopy(const TxImage* dst, const TxImage* src);

TXK_API TxResult TxImageLoadRaw(const TxImage* image, const Schar* path);

TXK_API TxResult TxImageSaveRaw(const TxImage* image, const Schar* path);

TXK_API TxResult TxImageLoadBmp(const TxImage* image, const Schar* path);

TXK_API TxResult TxImageSaveBmp(const TxImage* image, const Schar* path);

TXK_API TxResult TxImageRotate090(const TxImage* dst, const TxImage* src);

TXK_API TxResult TxImageRotate180(const TxImage* dst, const TxImage* src);

TXK_API TxResult TxImageRotate270(const TxImage* dst, const TxImage* src);

TXK_API TxResult TxImageMakeYuvToHueTable(TxVUint08* table);

TXK_API TxResult TxImageFreeYuvToHueTable(TxVUint08* table);

TXK_API TxResult TxImageConvRgb24ToYuv(const TxImage* dst, const TxImage* src, const Sint32 flip);

TXK_API TxResult TxImageConvYuvToRgb24(const TxImage* dst, const TxImage* src, const Sint32 flip);

TXK_API TxResult TxImageConvRgb24ToNv21(const TxImage* dst, const TxImage* src, const Sint32 flip);

TXK_API TxResult TxImageConvNv21ToRgb24(const TxImage* dst, const TxImage* src, const Sint32 flip);

TXK_API TxResult TxImageConvYuvToNv21(const TxImage* dst, const TxImage* src, const Sint32 flip);

TXK_API TxResult TxImageMakeHist(const TxHType type, TxHist* hist);

TXK_API TxResult TxImageFreeHist(TxHist* hist);

TXK_API TxResult TxImageCalcHist(const TxImage* image, TxHist* hist);

TXK_API TxResult TxImageCalcHistRect(const TxImage* image, const TxRect rect, TxHist* hist);

TXK_API TxResult TxImageCalcMatP(const TxImage* image, const TxHist* hist, const TxMSint32* matP);

TXK_API TxResult TxImageCalcMatPRect(const TxImage* image, const TxRect rect, const TxHist* hist, const TxMSint32* matP);

TXK_API TxResult TxImageBackMatP(const TxMSint32* matP, const TxImage* image);

TXK_API TxResult TxImageBackMatPRect(const TxMSint32* matP, const TxRect rect, const TxImage* image);

TXK_API TxResult TxImageCalcMatPMoments(const TxMSint32* matP, const Sint32 order, TxMoments* moments);

TXK_API TxResult TxImageCalcMatPMomentsRect(const TxMSint32* matP, const Sint32 order, const TxRect rect, TxMoments* moments);

#ifdef __cplusplus
};
#endif

#endif /* _TX_IMAGE_PROC_H_ */
