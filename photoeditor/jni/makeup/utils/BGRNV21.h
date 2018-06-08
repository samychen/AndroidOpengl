#ifndef BGRNV21_H
#define BGRNV21_H

#include "tcomdef.h"

#ifdef __cplusplus
extern "C" {
#endif

void BGR888_to_NV21(TUInt8 *pRGB, TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch);

void RGBA8888_to_NV21(TUInt8 *pRGB, TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch);

void NV21_to_BGR888(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch, TUInt8 *pBGR, TInt32 bgrpitch);

void NV21_to_RGB888(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch, TUInt8 *pRGB, TInt32 rgbpitch);

void NV21_to_RGBA8888(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch, TUInt8 *pBGRA, TInt32 bgrpaitch);

void NV21_to_RGBA8888_Mono(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch, TUInt8 *pBGR, TInt32 bgrpitch);

void ARGB2RGB(TUInt8* argb, TUInt8* rgb, int width, int height);

void RGB2ARGB(TUInt8* rgb, TUInt8* argb, int width, int height);

void ARGB2GRAY(TUInt8* argb, TUInt8* gray, int width, int height);

void GRAY2ARGB(TUInt8* gray, TUInt8* argb, int width, int height);
					
#ifdef __cplusplus
};
#endif

#endif
