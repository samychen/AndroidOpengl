#ifndef TS_FACE_WARP_H
#define TS_FACE_WARP_H
#include "tcomdef.h"
#include "tmem.h"
#include "types.h"
#ifdef __cplusplus
extern "C" {
#endif

#define  TS_FACEWARP_STYLE_THIN     0x00000000
#define  TS_FACEWARP_STYLE_GROW		0x00000080

#define  TS_FACEWARP_CHEEK			1
#define  TS_FACEWARP_EYE			2

TSOFFSCREEN  TS_WarpFace_Process(THandle pFWHandle,TLong nEyeStrenth,TLong nFaceStrength,TLong nTyle);
void         TS_WarpFace_Reset(THandle pFWHandle);
TPOINT		 TS_WarpFace_TrackPoint(THandle pFWHandle,TPOINT originalPt);
void         TS_FaceWarp_Uninit(THandle pFWHandle);
TLong        TS_FaceWarp_Init(THandle * ppFWHandle,THandle hMemMgr,TSOFFSCREEN* pImg,TPOINT *facekp);
void         TS_WarpFace_SetImage(THandle pFWHandle,LPTSOFFSCREEN pImg);
#ifdef __cplusplus
			}
#endif
#endif