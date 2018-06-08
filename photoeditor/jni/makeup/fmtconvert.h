#ifndef FORMAT_CONVERT_H
#define FORMAT_CONVERT_H
#define FLOOR_2(x)			( (x) & (~0x1) )
#define TRIM_UINT8(x)	(TUInt8)((x)&(~255) ? ((-(x))>>31) : (x))

#define SHIFT_HALF(shift)		( (1<<(shift))>>1 )

#define DOWN_ROUND(x, shift)	( ( (x) + SHIFT_HALF(shift) ) >> (shift) )
#define DOWN_CEIL(x, shift)		( ( (x) + (1<<(shift)) - 1	) >> (shift) )
#define DOWN_FLOOR(x, shift)	( (x) >> (shift) )

#define ZOOM				32768
#define	ZOOM_SHIFT			15
#define	ZOOM_HALF	   		16384

#define COLOR_OFFSET		(1<<7)
#define COLOR_OFFSET_14		(1<<13)

#define	RGB_YUV_R 			9798	//0.299
#define	RGB_YUV_G 			19235	//0.587
#define	RGB_YUV_B 			3736	//0.114

#define	RGB_YUV_U 			18492	//(0.5/(1-0.114)) = 0.564
#define	RGB_YUV_V 			23372	//(0.5/(1-0.299)) = 0.713

#define	YUV_RGB_R 			45941	//	(1-0.299)/0.5	= 1.402
#define	YUV_RGB_U			-11277	// -0.114/((0.5/(1-0.114))*0.587) = -0.344
#define	YUV_RGB_V			-23401	// -0.299/((0.5/(1-0.299))*0.587) = -0.714
#define	YUV_RGB_B 			58065	//	(1-0.114)/0.5	= 1.772

#define _RGB2YUV_L(R, G, B, lY, lCb, lCr)									\
{																			\
	TInt32 _b = B, _g = G, _r = R;											\
	lY = RGB_YUV_R*_r + RGB_YUV_G*_g + RGB_YUV_B*_b;						\
	lCb = ((_b<<7) - (lY>>(ZOOM_SHIFT-7)))*RGB_YUV_U >> 7;					\
	lCr = ((_r<<7) - (lY>>(ZOOM_SHIFT-7)))*RGB_YUV_V >> 7;					\
}

#define TRIM_TYPE	TRIM_UINT8
#define _RGB2YUV(R, G, B, Y, U, V)											\
{																			\
	TInt32 _lY, _lU, _lV;													\
	_RGB2YUV_L(R, G, B, _lY, _lU, _lV);										\
	Y  = (TUInt8)DOWN_ROUND(_lY, ZOOM_SHIFT);								\
	_lU = DOWN_ROUND(_lU, ZOOM_SHIFT) + COLOR_OFFSET;						\
	U = TRIM_TYPE(_lU);														\
	_lV = DOWN_ROUND(_lV, ZOOM_SHIFT) + COLOR_OFFSET;						\
	V = TRIM_TYPE(_lV);														\
}

#define _YUV2RGB_L(Y, U, V, lHalf, lR, lG, lB)								\
{																			\
	TInt32 _lY = Y, _lCb = (U)-(lHalf), _lCr = (V)-(lHalf);					\
	lB = (_lY<<ZOOM_SHIFT) + YUV_RGB_B*_lCb;								\
	lG = (_lY<<ZOOM_SHIFT) + YUV_RGB_U*_lCb + YUV_RGB_V*_lCr;				\
	lR = (_lY<<ZOOM_SHIFT) + YUV_RGB_R*_lCr;								\
}

#define _YUV2RGB(Y, U, V, R, G, B)											\
{																			\
	TInt32 _b, _g, _r;														\
	_YUV2RGB_L(Y, U, V, COLOR_OFFSET,_r, _g, _b);							\
	_r = DOWN_ROUND(_r, ZOOM_SHIFT);										\
	R = TRIM_UINT8(_r);														\
	_g = DOWN_ROUND(_g, ZOOM_SHIFT);										\
	G = TRIM_UINT8(_g);														\
	_b = DOWN_ROUND(_b, ZOOM_SHIFT);										\
	B = TRIM_UINT8(_b);														\
}
#endif