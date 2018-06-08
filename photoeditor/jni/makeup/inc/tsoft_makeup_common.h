#ifndef _THDSOFT_MAKEUP_COMMON_H_
#define _THDSOFT_MAKEUP_COMMON_H_

#ifdef ENABLE_DLL_EXPORT
#define TSM_API __declspec(dllexport)
#else
#define TSM_API
#endif

#include "tcomdef.h"

/* Defines for image color format */
typedef TInt32 TSM_PIXEL_ARRAY_FORMAT;
#define TSM_PAF_GRAY				0x0011		//y0, y1, y2, y3...

//Input image data
#define TSM_PAF_YUV422_YUYV			0x0023		//YUY2. y0, u0, y1, v0, y2, u1, y3, v1 ...
#define TSM_PAF_YUV422_Y1VY0U		0x0123		//YVYU2.y1, v0, y0, u0, y3, v1, y2, u1 ...
#define TSM_PAF_YUV420LP			0x0223		//NV12. y0, y1, y2, y3, ...u0, v0, u1, v1.
#define TSM_PAF_YUV420LP_VUVU		0x0323		//NV21. y0, y1, y2, y3, ...v0, u0, v1, u1.....
#define TSM_PAF_YUV420_PLANAR		0x0423		//I420. y0, y1, y2, y3, ...  u0, u1... v0, v1...
#define TSM_PAF_YUV422_PLANAR		0x0523		//I422H.y0, y1, y2, y3, ...  u0, u1... v0, v1...
#define TSM_PAF_YUV422LP			0x0A23		//LPI422H.	y0, y1, y2, y3, ... u0, v0, u1, v1...
#define TSM_PAF_YUV444_PLANAR		0x0B23		//I444.	y0, y1, y2, y3... u1, u2, u3, u4... v1, v2, v3, v4...

//Input template data
#define TSM_PAF_BGR888              0x0033	    //b, g, r, b, g, r...
#define TSM_PAF_SINGLE_CHANNEL		TSM_PAF_GRAY
#define TSM_PAF_TWO_CHANNEL			0x0012
#define TSM_PAF_THREE_CHANNEL		0x0013
#define TSM_PAF_FOUR_CHANNEL		0x0014
#define TSM_CHANNEL_NUM(fmt)		((fmt)&0xF)
#define TSM_PAF_RGBA8888			0x0034      //r g b a r g b a ...
/* Defines for image data */
#define TSM_MAX_CHANNELS			4
typedef struct {
	TSM_PIXEL_ARRAY_FORMAT	lPixelArrayFormat;
	TInt32					lWidth;								// Image width
	TInt32					lHeight;							// Image height
	TVoid*					ppPlane[TSM_MAX_CHANNELS];			// Image Data
	TInt32					plPitch[TSM_MAX_CHANNELS];			// Image Pitch
} TSM_OFFSCREEN;

/* Defines for error */
#define TSM_ERR_NONE					0
#define TSM_ERR_UNKNOWN					-1
#define TSM_ERR_INVALID_PARAM			-2
#define TSM_ERR_USER_ABORT				-3
#define TSM_ERR_UNSUPPORT_PARAM			-4
#define TSM_ERR_IMAGE_FORMAT			-101
#define TSM_ERR_IMAGE_SIZE_UNMATCH		-102
#define TSM_ERR_ALLOC_MEM_FAIL			-201
#define TSM_ERR_NOFACE_FOUND			-901
#define TSM_ERR_PIXEL_ARRAY				-1001
#define TSM_ERR_FACE_RECT				-1001
#define TSM_ERR_TOO_LARGE_SKIN			-1002
#define TSM_ERR_FEATURESPTS_NUM			-1202
#define TSM_ERR_FEATURESPTS_NOSET		-1203
#define TSM_ERR_EYE_BLUR				-1410
#define TSM_ERR_MODEL_ONLY_GRAY			-1502
#define TSM_ERR_MODEL_NOSET				-1506
#define TSM_ERR_MODEL_OR_COLOR_NUMBER	-1507

#define TSM_ERR_NOT_CHANNEL_IMG			1001

/* Defines for color */
#define TSM_COLOR(cr1, cr2, cr3)		(((cr1) << 16) | ((cr2) << 8) | (cr3))

/* Defines for different makeup type*/
typedef TInt32	TSM_KEYPTS_TYPE;
#define TSM_KEYPTS_NONE			0x00
#define TSM_KEYPTS_SIMPLE		0x02
#define TSM_KEYPTS_FULL			0x03

/* Defines for points array, usually used for curve or feature points presentation*/
typedef struct  
{
	TPOINT			*pPoints;
	TInt32			lPtsNum;
}TSM_POINTS;

/*define color BGRA*/
typedef struct _ts_tagRGBQUAD {
	unsigned char   rgbBlue;
	unsigned char   rgbGreen;
	unsigned char   rgbRed;
	unsigned char   rgbReserved;
} TRGBA;

/* Defines for model */
typedef struct {
	TSM_OFFSCREEN	sData;						//Template data
	TSM_POINTS		sKeyPoints;					//Key points
	TRGBA          *sColorp;					//color palette
}TSM_MODEL;

/************************************************************************
* This function is implemented by the caller, registered with 
* any time-consuming processing functions, and will be called 
* periodically during processing so the caller application can 
* obtain the operation status (i.e., to draw a progress bar), 
* as well as determine whether the operation should be canceled or not
************************************************************************/
typedef TRESULT (*TSM_FNPROGRESS) (
	TInt32		lProgress,				// The percentage of the current operation
	TInt32		lStatus,				// The current status at the moment
	TVoid		*pParam					// Caller-defined data
);

#ifdef __cplusplus
extern "C" {
#endif
	
/************************************************************************/
/* The handle init/uninit function.                                             */
/************************************************************************/
TSM_API TRESULT TSM_InitialEngine(
	 THandle            hMemMgr,		// [in] The handle for memory manager
	 THandle*			phMakeupEngine	// [out]The handle for face makeup engine
);


TSM_API TVoid	TSM_UnInitialEngine(
	THandle				hMakeupEngine	// [in] The handle for face makeup engine
);

/************************************************************************/
/*  Set the necessary info for makeup                                            */
/************************************************************************/
TSM_API TRESULT TSM_SetInputImage(
	THandle				hMakeupEngine, 
	const TSM_OFFSCREEN* pImgSrc
);

TSM_API TRESULT TSM_SetInputFaceOutline(
	THandle				hMakeupEngine, 
	const TPOINT*		pFeaturePts, 
	TInt32				lFeaturesNum,
	TInt32				lAngle
);

TSM_API TRESULT TSM_SetModelData(
	THandle				hMakeupEngine, 
	const TVoid*		pModelData, 
	TInt32				lDataBytes
);

/************************************************************************/
/*   Get the curve of each features                                              */
/************************************************************************/
TSM_API TRESULT TSM_GetLipCurve(
	THandle				hMakeupEngine,
	TSM_POINTS			*psOuterCurve,
	TSM_POINTS			*psInnerCurve
);

TSM_API TRESULT TSM_GetEyeCurve(
	THandle				hMakeupEngine,
	TSM_POINTS			*psLeftCurve,
	TSM_POINTS			*psRightCurve
);

TSM_API TRESULT TSM_GetEyebrowCurve(
	THandle				hMakeupEngine,
	TSM_POINTS			*psLeftCurve,
	TSM_POINTS			*psRightCurve
);

/************************************************************************/
/*  Other function for better quality                                            */
/************************************************************************/
TSM_API TRESULT TSM_AutoEyeLineFix(
	THandle				hMakeupEngine, 
	TSM_OFFSCREEN		*pImgRlt
);

/************************************************************************/
/* External API for detailed lip shape generate                        
TSM_KEYPTS_SIMPLE for 6 key points (Default)
TSM_KEYPTS_FULL for full 14 key points                                            */
/************************************************************************/
TSM_API TRESULT TSM_GetLipKeyPts(
	THandle				hMakeupEngine, 
	TSM_POINTS			*pptOuterLip, 
	TSM_POINTS			*pptInnerLip, 
	TSM_KEYPTS_TYPE		eType);		

TSM_API TRESULT TSM_SetLipKeyPts(
	THandle				hMakeupEngine, 
	const TSM_POINTS	*pptOuterLip, 
	const TSM_POINTS	*pptInnerLip, 
	TSM_KEYPTS_TYPE		eType);

/************************************************************************/
/* Adjust lip points  
ptRltLip : after adjusting the lip points
ptOldLip : before adjusting the lip points
*/
/************************************************************************/
TRESULT TSM_AdjustLipPoint(THandle hMakeupEngine,TPOINT* ptRltLip,TPOINT* ptOldLip);


/************************************************************************/
/*   Get the version                                                             */
/************************************************************************/
typedef struct
{ 
	TInt32 lMajor;				// major version number 
	TInt32 lMinor;				// minor version number
	TInt32 lBuild;				// Build version number, increasable only
	const TChar *szVersion;		// version in string form
	const TChar *szBuildDate;	// latest build Date
	const TChar *szCopyRight;	// copyright 
} TSM_Version;
TSM_API const TSM_Version* TSM_GetCommonVersion();

#ifdef __cplusplus
}
#endif

#endif	// _THDSOFT_MAKEUP_COMMON_H_