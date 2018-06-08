
#ifndef _FACE_BEAUTIFIER_H_
#define _FACE_BEAUTIFIER_H_

#include "tcomdef.h"
#include "tsoffscreen.h"
#include "tfacemaskdef.h"

#ifdef __cplusplus
extern "C" {
#endif

/* Defines for image color format*/
#define TSFB_FMT_YUV420LP			0x3		//yyyy...uvuv...
#define TSFB_FMT_YUV420LP_VUVU		0x4		//yyyy...vuvu...
#define TSFB_FMT_YUV420_PLANAR		0x5		//yyyy...uu(W/2*H/2)¡­vv(W/2*H/2)

/* Defines for image data*/
typedef struct {
	TLong		lWidth;				// Off-screen width
	TLong		lHeight;			// Off-screen height
	TLong		lPixelArrayFormat;	// Format of pixel array
	union
	{
		struct
		{
			TLong lLineBytes; 
			TVoid *pPixel;
		} chunky;
		struct
		{
			TLong lLinebytesArray[4];
			TVoid *pPixelArray[4];
		} planar;
	} pixelArray;
} tsfbSCREEN, *LPtsfbSCREEN;

/* Defines for face rectangles*/
typedef struct  {
	PTRECT	prtFaces;
	TLong	lFacesNum;
} tsFbFace, *LPtsFbFace;

/* Defines for type of face beautifier*/
typedef TLong tsFbOPTION;
#define ALL_SKIN_DEAL					0x0011
#define ONLY_FACE_DEAL					0x0021

/************************************************************************
* This function is implemented by the caller, registered with 
* any time-consuming processing functions, and will be called 
* periodically during processing so the caller application can 
* obtain the operation status (i.e., to draw a progress bar), 
* as well as determine whether the operation should be canceled or not
************************************************************************/
typedef TRESULT (*TshzFNPROGRESS) (
	TLong		lProgress,				// The percentage of the current operation
	TLong		lStatus,				// The current status at the moment
	TVoid		*pParam					// Caller-defined data
);

/************************************************************************/
/* The handle init/uninit function.                                                                     */
/************************************************************************/
TRESULT TshzFbInit(
	THandle                 hMemMgr,		// The handle for memory manager
	tsFbOPTION              eOption,		// The option for beautifier
	THandle                 *phBeautifier,	// The handle for beautifier
	TInt32					lCleanLevel
);
TVoid	TshzFbUnInit(THandle  hBeautifier);
/************************************************************************/
/* The main function for beautifier.                                                                     */
/************************************************************************/
TRESULT	TshzBeautify(
	THandle hBeautifier,					// [in]  The handle for beautifier
	LPtsfbSCREEN			pImgSrc, 		// [in]	 The source image
	LPtsFbFace				prtFaces,		// [in]  Faces Rectangle.
	LPtsfbSCREEN			pImgRlt,		// [out] The result image
	TshzFNPROGRESS			fnCallback,		// [in]  The callback function 
	TVoid*					pParam			// [in]  Caller-specific data that will be passed into the callback function
);

TRESULT TShzFaceBeautify(LPTSOFFSCREEN pFaceImg, 
						   LPTSOFFSCREEN pDstImage, 
						   TMASK* pFaceMask, 
						   TRECT rcFace, 
						   TLong lBlurLevel, 
						   THandle hMem);

#ifdef __cplusplus
}
#endif

#endif	// _FACE_BEAUTIFIER_H_
