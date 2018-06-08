/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#ifndef _T_FACE_COMDEF_
#define _T_FACE_COMDEF_

typedef struct  
{
	TByte *pData;
	TLong lMaskLine;
	TLong lWidth, lHeight;
	TRECT rcMask;
} TMASK, *LPTMASK;

typedef struct  
{
	TUInt8 Cb;
	TUInt8 Cr;
} TCOLORMask, *LPTCOLORMask;

typedef struct _tag_COLORRANGE
{
	TUInt8 minY;
	TUInt8 maxY;
	TUInt8 minCb;
	TUInt8 maxCb;
	TUInt8 minCr;
	TUInt8 maxCr;
} TCOLORRANGE, *LPTCOLORRANGE;

typedef struct _tag_FACEOUTLINE {

	TPOINT		tplefteyeleft;
	TPOINT		tplefteyeright;
	TPOINT		tplefteyetop;
	TPOINT		tplefteyebottom;

	TPOINT		tprighteyeleft;
	TPOINT		tprighteyeright;
	TPOINT		tprighteyetop;
	TPOINT		tprighteyebottom;

	TPOINT		tpnoseleft;
	TPOINT		tpnoseright;

	TPOINT		tpmouseleft;
	TPOINT		tpmouseright;
	TPOINT		tpmouseintopone;
	TPOINT		tpmouseintoptwo;
	TPOINT		tpmouseintopthree;
	TPOINT		tpmouseinbottomone;
	TPOINT		tpmouseinbottomtwo;
	TPOINT		tpmouseinbottomthree;
	TPOINT		tpmouseouttopone;
	TPOINT		tpmouseouttoptwo;
	TPOINT		tpmouseouttopthree;
	TPOINT		tpmouseouttopfour;
	TPOINT		tpmouseouttopfive;
	TPOINT		tpmouseoutbottomone;
	TPOINT		tpmouseoutbottomtwo;
	TPOINT		tpmouseoutbottomthree;
	TPOINT		tpmouseoutbottomfour;
	TPOINT		tpmouseoutbottomfive;

	TPOINT      tplefteyetopleft;
	TPOINT		tplefteyetopright;
	TPOINT		tplefteyebottomleft;
	TPOINT		tplefteyebottomright;

	TPOINT		tprighteyetopleft;
	TPOINT		tprighteyetopright;
	TPOINT		tprighteyebottomleft;
	TPOINT		tprighteyebottomright;
	
	TRECT		rcFace;
	TUInt32		lScale;
} TFaceOutline, *LPTFaceOutline;

#endif