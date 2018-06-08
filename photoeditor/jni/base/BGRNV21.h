#ifndef BGRNV21_H
#define BGRNV21_H

#include "tcomdef.h"

#ifdef __cplusplus
extern "C" {
#endif

void TS_RGBA8888_to_NV21_Sub(TUInt8 *pRGBA, TUInt8 *pSrcY, TUInt8 *pSrcUV,
					TInt32 width, TInt32 height, TInt32 pitch);

void TS_RGBA8888_to_NV21_MT(TUInt8 *pRGBA, TUInt8 *pSrcY, TUInt8 *pSrcUV,
					   TInt32 width, TInt32 height, TInt32 BgrPitch);
void RGB888_to_NV21(TUInt8 *pRGB, TUInt8 *pDstY,  TUInt8 *pDstUV,
                      TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, TInt32 dstwidth, TInt32 dstheight);
void RGBA8888_to_RGB888(TUInt8 *pRGBA, TUInt8 *pRGB, int width, int height);
#ifdef __cplusplus
};
#endif

#endif
