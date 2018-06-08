#include "BGRNV21.h"
#include <stdlib.h>

#define TRIMBYTE(x)	(TUInt8)((x)&(~255)?((-(x))>>31):(x))

void BGR888_to_NV21(TUInt8 *pRGB, TUInt8 *pYUV, 
					TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4;
	
	TInt32 offset_src;
	TInt32 width2 = width&(~1);
	TInt32 height2 = height&(~1);
	TUInt8 *pRGBNext = pRGB + pitch;
	TUInt8 * pY = pYUV;
	TUInt8 * pYNext = pYUV+width2;
	TUInt8 * pUV = pYUV + width2*height2;
	
	offset_src = pitch - width2*3;
	
	y = height2;
	while (y > 0)
	{
		y -= 2;
		x = width2>>1;
		while (x-- > 0)
		{
			b = *pRGB++;
			g = *pRGB++;
			r = *pRGB++;
			
			
			Y1=(19595*r+38470*g+7471*b)>>16;         
			Cb=(32768*b-11059*r-21709*g)>>16; 
			Cr=(32768*r-27439*g-5329*b)>>16; 
			
			b = *pRGB++;
			g = *pRGB++;
			r = *pRGB++;
			
			
			Y2=(19595*r+38470*g+7471*b)>>16;         
			Cb+=(32768*b-11059*r-21709*g)>>16; 
			Cr+=(32768*r-27439*g-5329*b)>>16; 			
			
			b = *pRGBNext++;
			g = *pRGBNext++;
			r = *pRGBNext++;
			
			
			Y3=(19595*r+38470*g+7471*b)>>16;         
			Cb+=(32768*b-11059*r-21709*g)>>16; 
			Cr+=(32768*r-27439*g-5329*b)>>16; 
			
			b = *pRGBNext++;
			g = *pRGBNext++;
			r = *pRGBNext++;
			
			
			Y4=(19595*r+38470*g+7471*b)>>16;         
			Cb+=(32768*b-11059*r-21709*g)>>16; 
			Cr+=(32768*r-27439*g-5329*b)>>16; 
			
			
			Cb = (Cb>>2)+128;
			Cr = (Cr>>2)+128;
						
			*pY++ = TRIMBYTE(Y1);
			*pY++ = TRIMBYTE(Y2);
			*pYNext++ = TRIMBYTE(Y3);
			*pYNext++ = TRIMBYTE(Y4);
			*pUV++ = TRIMBYTE(Cr);
			*pUV++ = TRIMBYTE(Cb);
			
		}
		pRGB += offset_src+pitch;
		pRGBNext = pRGB + pitch;
		pY += width2;
		pYNext = pY+width2;		
	}	
}

void RGBA8888_to_NV21(TUInt8 *pRGB, TUInt8 *pYUV,
					TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x,y;
	TInt32 r,g,b,a;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4;

	TInt32 offset_src;
	TInt32 width2 = width&(~1);
	TInt32 height2 = height&(~1);
	TUInt8 *pRGBNext = pRGB + pitch;
	TUInt8 * pY = pYUV;
	TUInt8 * pYNext = pYUV+width2;
	TUInt8 * pUV = pYUV + width2*height2;

	offset_src = pitch - width2*4;

	y = height2;
	while (y > 0)
	{
		y -= 2;
		x = width2>>1;
		while (x-- > 0)
		{
			r = *pRGB++;
			g = *pRGB++;
			b = *pRGB++;
			a = *pRGB++;


			Y1=(19595*r+38470*g+7471*b)>>16;
			Cb=(32768*b-11059*r-21709*g)>>16;
			Cr=(32768*r-27439*g-5329*b)>>16;

			r = *pRGB++;
			g = *pRGB++;
			b = *pRGB++;
			a = *pRGB++;


			Y2=(19595*r+38470*g+7471*b)>>16;
			Cb+=(32768*b-11059*r-21709*g)>>16;
			Cr+=(32768*r-27439*g-5329*b)>>16;

			r = *pRGBNext++;
			g = *pRGBNext++;
			b = *pRGBNext++;
			a = *pRGBNext++;


			Y3=(19595*r+38470*g+7471*b)>>16;
			Cb+=(32768*b-11059*r-21709*g)>>16;
			Cr+=(32768*r-27439*g-5329*b)>>16;

			r = *pRGBNext++;
			g = *pRGBNext++;
			b = *pRGBNext++;
			a = *pRGBNext++;


			Y4=(19595*r+38470*g+7471*b)>>16;
			Cb+=(32768*b-11059*r-21709*g)>>16;
			Cr+=(32768*r-27439*g-5329*b)>>16;


			Cb = (Cb>>2)+128;
			Cr = (Cr>>2)+128;

			*pY++ = TRIMBYTE(Y1);
			*pY++ = TRIMBYTE(Y2);
			*pYNext++ = TRIMBYTE(Y3);
			*pYNext++ = TRIMBYTE(Y4);
			*pUV++ = TRIMBYTE(Cr);
			*pUV++ = TRIMBYTE(Cb);

		}
		pRGB += offset_src+pitch;
		pRGBNext = pRGB + pitch;
		pY += width2;
		pYNext = pY+width2;
	}
}

void NV21_to_BGR888(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch, 
					TUInt8 *pBGR, TInt32 bgrpitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4, Ydiff;
	
	TInt32 offsetbgr, offsetyuv;
	TUInt8 *pBGRNext = pBGR + bgrpitch;
	TUInt8 * pY = pYUV;
	TUInt8 * pYNext = pYUV+pitch;
	TUInt8 * pUV = pYUV + pitch*height;

	offsetbgr = bgrpitch - width*3;
	offsetyuv = pitch - width;

	y = height;
	while (y > 0)
	{
		y -= 2;
		x = width >> 1;
		while ( x-- > 0)
		{
			Y1 = *pY++;
			Cr = *pUV++ - 128;
			Cb = *pUV++ - 128;

			r = Y1+(91881*Cr>>16);
			g = Y1-((22554*Cb+46802*Cr)>>16);
			b = Y1+(116130*Cb>>16);

			*pBGR++ = TRIMBYTE(b);
			*pBGR++ = TRIMBYTE(g);
			*pBGR++ = TRIMBYTE(r);

			Y2 = *pY++;
			Ydiff = Y2 - Y1;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;

			*pBGR++ = TRIMBYTE(b);
			*pBGR++ = TRIMBYTE(g);
			*pBGR++ = TRIMBYTE(r);

			Y3 = *pYNext++;
			Ydiff = Y3 - Y2;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;

			*pBGRNext++ = TRIMBYTE(b);
			*pBGRNext++ = TRIMBYTE(g);
			*pBGRNext++ = TRIMBYTE(r);

			Y4 = *pYNext++;
			Ydiff = Y4 - Y3;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;

			*pBGRNext++ = TRIMBYTE(b);
			*pBGRNext++ = TRIMBYTE(g);
			*pBGRNext++ = TRIMBYTE(r);
		}

		pY += offsetyuv + pitch;
		pYNext = pY + pitch;
		pUV += offsetyuv;
		pBGR += offsetbgr + bgrpitch;
		pBGRNext = pBGR + bgrpitch;
	}
}


void NV21_to_RGB888(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch,
					TUInt8 *pBGR, TInt32 bgrpitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4, Ydiff;

	TInt32 offsetbgr, offsetyuv;
	TUInt8 *pBGRNext = pBGR + bgrpitch;
	TUInt8 * pY = pYUV;
	TUInt8 * pYNext = pYUV+pitch;
	TUInt8 * pUV = pYUV + pitch*height;

	offsetbgr = bgrpitch - width*3;
	offsetyuv = pitch - width;

	y = height;
	while (y > 0)
	{
		y -= 2;
		x = width >> 1;
		while ( x-- > 0)
		{
			Y1 = *pY++;
			Cr = *pUV++ - 128;
			Cb = *pUV++ - 128;

			r = Y1+(91881*Cr>>16);
			g = Y1-((22554*Cb+46802*Cr)>>16);
			b = Y1+(116130*Cb>>16);

			*pBGR++ = TRIMBYTE(r);
			*pBGR++ = TRIMBYTE(g);
			*pBGR++ = TRIMBYTE(b);

			Y2 = *pY++;
			Ydiff = Y2 - Y1;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;

			*pBGR++ = TRIMBYTE(r);
			*pBGR++ = TRIMBYTE(g);
			*pBGR++ = TRIMBYTE(b);

			Y3 = *pYNext++;
			Ydiff = Y3 - Y2;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;

			*pBGRNext++ = TRIMBYTE(r);
			*pBGRNext++ = TRIMBYTE(g);
			*pBGRNext++ = TRIMBYTE(b);

			Y4 = *pYNext++;
			Ydiff = Y4 - Y3;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;

			*pBGRNext++ = TRIMBYTE(r);
			*pBGRNext++ = TRIMBYTE(g);
			*pBGRNext++ = TRIMBYTE(b);
		}

		pY += offsetyuv + pitch;
		pYNext = pY + pitch;
		pUV += offsetyuv;
		pBGR += offsetbgr + bgrpitch;
		pBGRNext = pBGR + bgrpitch;
	}
}

	void NV21_to_RGBA8888(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch,
						TUInt8 *pBGR, TInt32 bgrpitch)
	{
		TInt32 x,y;
		TInt32 r,g,b,a;
		TInt32 Cb,Cr;
		TInt32 Y1, Y2, Y3, Y4, Ydiff;

		TInt32 offsetbgr, offsetyuv;
		TUInt8 *pBGRNext = pBGR + bgrpitch;
		TUInt8 * pY = pYUV;
		TUInt8 * pYNext = pYUV+pitch;
		TUInt8 * pUV = pYUV + pitch*height;

		offsetbgr = bgrpitch - width*4;
		offsetyuv = pitch - width;

		y = height;
		while (y > 0)
		{
			y -= 2;
			x = width >> 1;
			while ( x-- > 0)
			{
				Y1 = *pY++;
				Cr = *pUV++ - 128;
				Cb = *pUV++ - 128;

				r = Y1+(91881*Cr>>16);
				g = Y1-((22554*Cb+46802*Cr)>>16);
				b = Y1+(116130*Cb>>16);

				*pBGR++ = TRIMBYTE(r);
				*pBGR++ = TRIMBYTE(g);
				*pBGR++ = TRIMBYTE(b);
				*pBGR++ = 255;
//				pBGR++;

				Y2 = *pY++;
				Ydiff = Y2 - Y1;
				r += Ydiff;
				g += Ydiff;
				b += Ydiff;

				*pBGR++ = TRIMBYTE(r);
				*pBGR++ = TRIMBYTE(g);
				*pBGR++ = TRIMBYTE(b);
				*pBGR++ = 255;
//				pBGR++;

				Y3 = *pYNext++;
				Ydiff = Y3 - Y2;
				r += Ydiff;
				g += Ydiff;
				b += Ydiff;

				*pBGRNext++ = TRIMBYTE(r);
				*pBGRNext++ = TRIMBYTE(g);
				*pBGRNext++ = TRIMBYTE(b);
				*pBGRNext++ = 255;
//				pBGRNext++;

				Y4 = *pYNext++;
				Ydiff = Y4 - Y3;
				r += Ydiff;
				g += Ydiff;
				b += Ydiff;

				*pBGRNext++ = TRIMBYTE(r);
				*pBGRNext++ = TRIMBYTE(g);
				*pBGRNext++ = TRIMBYTE(b);
				*pBGRNext++ = 255;
//				pBGRNext++;
			}

			pY += offsetyuv + pitch;
			pYNext = pY + pitch;
			pUV += offsetyuv;
			pBGR += offsetbgr + bgrpitch;
			pBGRNext = pBGR + bgrpitch;
		}
}

void NV21_to_RGBA8888_Mono(TUInt8 *pYUV, TInt32 width, TInt32 height, TInt32 pitch,
					TUInt8 *pBGR, TInt32 bgrpitch) {
	int pixCount = width*height;
	int pixIndex = 0;
	do {
		memset(pBGR, pYUV[pixIndex],4);
		pBGR += 4;
		pixIndex ++;
	} while (pixIndex<pixCount);
}

void ARGB2RGB(TUInt8* argb, TUInt8* rgb, int width, int height) {
	int size = width*height;
	int index = 0;
	for(index=0; index<size; index++) {
		rgb[0] = argb[2];
		rgb[1] = argb[1];
		rgb[2] = argb[0];
		rgb += 3;
		argb += 4;
	}
}

void RGB2ARGB(TUInt8* rgb, TUInt8* argb, int width, int height) {
	int size = width*height;
	int index = 0;
	for(index=0; index<size; index++) {
		argb[0] = rgb[2];
		argb[1] = rgb[1];
		argb[2] = rgb[0];
		argb += 4;
		rgb += 3;
	}
}

void ARGB2GRAY(TUInt8* argb, TUInt8* gray, int width, int height) {
	int size = width*height;
	int index = 0;
	int* pix = (int*)argb;
	memset(gray, 0, size);
	for(index=0; index<size; index++) {
		if(*pix) {
			*gray = ((TUInt8*)pix)[2];
		}
		pix ++;
		gray ++;
	}
}

void GRAY2ARGB(TUInt8* gray, TUInt8* argb, int width, int height) {
	int size = width*height;
	int index = 0;
	int* pix = (int*)argb;
	memset(gray, 0, size);
	for(index=0; index<size; index++) {
		if(*gray) {
			*pix = *gray;
			*pix |= *gray<<8;
			*pix |= *gray<<16;
			*pix |= 0xFF<<24;
		}
		pix ++;
		gray ++;
	}
}
