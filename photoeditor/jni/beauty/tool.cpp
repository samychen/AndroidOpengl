/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#include <stdio.h>
#include "tool.h"
#include <time.h>

#define TRIMBYTE(x)	(TUInt8)((x)&(~255)?((-(x))>>31):(x))

long gettime(){
#ifdef WIN32
    return 0;
#else
    struct timeval time;
	
    gettimeofday(&time, NULL);
	
    return (time.tv_sec*1000+time.tv_usec/1000);
#endif	
}


int savefile(unsigned char* pData, int size, char* name)
{
    FILE *pf =fopen(name, "wb");
	if(pf != 0){
	   fwrite(pData, size, 1, pf);
	   fclose(pf);
		return 1;

	   }
     return 0;
}

int loadfile(unsigned char* pData, int size, char* name)
{

	FILE *pf =fopen(name, "rb");
	if(pf != 0){
		fread(pData, size, 1, pf);
		fclose(pf);
		return 1;
		
	   }
     return 0;
}

void RGB888_to_NV21(TUInt8 *pRGB, TUInt8 *pDstY,  TUInt8 *pDstUV, 
                      TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, TInt32 dstwidth, TInt32 dstheight)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4;
	
	TInt32 offset_src;
	TInt32 width2 = dstwidth;
	TInt32 height2 = dstheight;
	TUInt8 *pRGBNext = pRGB + srcpitch;
	TUInt8 * pY = pDstY;
	TUInt8 * pYNext = pY+width2;
    TUInt8 * pUV = pDstUV;
	
	offset_src = srcpitch - width2*3;
	
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
		
			
			Y1=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr=(32768*r-27439*g-5329*b+32768)>>16; 
			
			r = *pRGB++;
			g = *pRGB++;
			b = *pRGB++;
		
			
			Y2=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb+=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr+=(32768*r-27439*g-5329*b+32768)>>16; 
			
			// 			Cb = ((Cb1 + Cb2)>>1)+128;
			// 			Cr = ((Cr1 + Cr2)>>1)+128;
			
			
			r = *pRGBNext++;
			g = *pRGBNext++;
			b = *pRGBNext++;
		
			
			Y3=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb+=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr+=(32768*r-27439*g-5329*b+32768)>>16; 
			
			r = *pRGBNext++;
			g = *pRGBNext++;
			b = *pRGBNext++;
	
			
			Y4=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb+=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr+=(32768*r-27439*g-5329*b+32768)>>16; 
			
			
			Cb = (Cb>>2)+128;
			Cr = (Cr>>2)+128;
			
			
			
			*pY++ = TRIMBYTE(Y1);
			*pY++ = TRIMBYTE(Y2);
			*pYNext++ = TRIMBYTE(Y3);
			*pYNext++ = TRIMBYTE(Y4);
			*pUV++ = TRIMBYTE(Cr);
			*pUV++ = TRIMBYTE(Cb);
			
		}
		pRGB += offset_src+srcpitch;
		pRGBNext = pRGB + srcpitch;
		pY += width2;
		pYNext = pY+width2;
		
	}

}

void RGBA8888_to_NV21(TUInt8 *pRGBA, TUInt8 *pDstY,  TUInt8 *pDstUV, 
                      TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, TInt32 dstwidth, TInt32 dstheight)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4;
 
	TInt32 offset_src;
	TInt32 width2 = dstwidth;
	TInt32 height2 = dstheight;
	TUInt8 *pRGBANext = pRGBA + srcpitch;
	TUInt8 * pY = pDstY;
	TUInt8 * pYNext = pY+width2;
	TUInt8 * pUV = pDstUV;
	
	offset_src = srcpitch - width2*4;
	
	y = height2;
	while (y > 0)
	{
		y -= 2;
		x = width2>>1;
		while (x-- > 0)
		{
			r = *pRGBA++;
			g = *pRGBA++;
			b = *pRGBA++;
			pRGBA++;
			
			Y1=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr=(32768*r-27439*g-5329*b+32768)>>16; 
			
			r = *pRGBA++;
			g = *pRGBA++;
			b = *pRGBA++;
			pRGBA++;
			
			Y2=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb+=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr+=(32768*r-27439*g-5329*b+32768)>>16; 
			
// 			Cb = ((Cb1 + Cb2)>>1)+128;
// 			Cr = ((Cr1 + Cr2)>>1)+128;


			r = *pRGBANext++;
			g = *pRGBANext++;
			b = *pRGBANext++;
			pRGBANext++;
			
			Y3=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb+=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr+=(32768*r-27439*g-5329*b+32768)>>16; 
			
			r = *pRGBANext++;
			g = *pRGBANext++;
			b = *pRGBANext++;
			pRGBANext++;
			
			Y4=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb+=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr+=(32768*r-27439*g-5329*b+32768)>>16; 


			Cb = (Cb>>2)+128;
			Cr = (Cr>>2)+128;


			
			*pY++ = TRIMBYTE(Y1);
			*pY++ = TRIMBYTE(Y2);
			*pYNext++ = TRIMBYTE(Y3);
			*pYNext++ = TRIMBYTE(Y4);
   			*pUV++ = TRIMBYTE(Cr);
   			*pUV++ = TRIMBYTE(Cb);
			
		}
		pRGBA += offset_src+srcpitch;
		pRGBANext = pRGBA + srcpitch;
		pY += width2;
		pYNext = pY+width2;
 
	}
}

 

void NV21_to_RGB888(TUInt8 *pSrcY, TUInt8 *pSrcUV, TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, 
					TUInt8 *pRGB, TInt32 dstwidth, TInt32 dstheight, TInt32 dstpitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4, Ydiff;
	
	TInt32 offsetbgr, offsetyuv;
	TUInt8 *pRGBNext = pRGB + dstpitch;
	TUInt8 * pY = pSrcY;
	TUInt8 * pYNext = pSrcY+srcpitch;
	TUInt8 * pUV = pSrcUV;
	
	offsetbgr = dstpitch - srcwidth*3;
	offsetyuv = srcpitch - srcwidth;
	
	y = srcheight;
	while (y > 0)
	{
		y -= 2;
		x = srcwidth >> 1;
		while ( x-- > 0)
		{
			Y1 = *pY++;
			Cr = *pUV++ - 128;
			Cb = *pUV++ - 128;
			
			r = Y1+((91881*Cr+32768)>>16);
			g = Y1-((22554*Cb+46802*Cr+32768)>>16);
			b = Y1+((116130*Cb+32768)>>16);
			
			*pRGB++ = TRIMBYTE(r);
			*pRGB++ = TRIMBYTE(g);			
			*pRGB++ = TRIMBYTE(b);
			
			Y2 = *pY++;
			Ydiff = Y2 - Y1;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;
			
			*pRGB++ = TRIMBYTE(r);
			*pRGB++ = TRIMBYTE(g);
			*pRGB++ = TRIMBYTE(b);
			
			
			Y3 = *pYNext++;
			Ydiff = Y3 - Y2;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;
			
			*pRGBNext++ = TRIMBYTE(r);
			*pRGBNext++ = TRIMBYTE(g);
			*pRGBNext++ = TRIMBYTE(b);
			
			Y4 = *pYNext++;
			Ydiff = Y4 - Y3;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;
			
			*pRGBNext++ = TRIMBYTE(r);
			*pRGBNext++ = TRIMBYTE(g);
			*pRGBNext++ = TRIMBYTE(b);
		}
		
		pY += offsetyuv + srcpitch;
		pYNext = pY + srcpitch;
		pUV += offsetyuv;
		pRGB += offsetbgr + dstpitch;
		pRGBNext = pRGB + dstpitch;
	}
}



void NV21_to_RGBA8888(TUInt8 *pSrcY, TUInt8 *pSrcUV, TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, 
					TUInt8 *pRGBA, TInt32 dstwidth, TInt32 dstheight, TInt32 dstpitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Cb,Cr;
	TInt32 Y1, Y2, Y3, Y4, Ydiff;
	
	TInt32 offsetbgr, offsetyuv;
	TUInt8 *pRGBANext = pRGBA + dstpitch;
	TUInt8 * pY = pSrcY;
	TUInt8 * pYNext = pSrcY+srcpitch;
	TUInt8 * pUV = pSrcUV;
	
	offsetbgr = dstpitch - srcwidth*4;
	offsetyuv = srcpitch - srcwidth;
	
	y = srcheight;

//	memset(pRGBA, 0xFF, dstpitch*dstheight);
	while (y > 0)
	{
		y -= 2;
		x = srcwidth >> 1;
		while ( x-- > 0)
		{
			Y1 = *pY++;
			Cr = *pUV++ - 128;
			Cb = *pUV++ - 128;
			
			r = Y1+((91881*Cr+32768)>>16);
			g = Y1-((22554*Cb+46802*Cr+32768)>>16);
			b = Y1+((116130*Cb+32768)>>16);
			
			*pRGBA++ = TRIMBYTE(r);
			*pRGBA++ = TRIMBYTE(g);			
			*pRGBA++ = TRIMBYTE(b);
			pRGBA++;
			
			Y2 = *pY++;
			Ydiff = Y2 - Y1;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;
			
			*pRGBA++ = TRIMBYTE(r);
			*pRGBA++ = TRIMBYTE(g);			
			*pRGBA++ = TRIMBYTE(b);
			pRGBA++;
			
			
			Y3 = *pYNext++;
			Ydiff = Y3 - Y2;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;
			
			*pRGBANext++ = TRIMBYTE(r);
			*pRGBANext++ = TRIMBYTE(g);			
			*pRGBANext++ = TRIMBYTE(b);
			pRGBANext++;
			
			Y4 = *pYNext++;
			Ydiff = Y4 - Y3;
			r += Ydiff;
			g += Ydiff;
			b += Ydiff;
			
			*pRGBANext++ = TRIMBYTE(r);
			*pRGBANext++ = TRIMBYTE(g);			
			*pRGBANext++ = TRIMBYTE(b);
			pRGBANext++;
		}
		
		pY += offsetyuv + srcpitch;
		pYNext = pY + srcpitch;
		pUV += offsetyuv;
		pRGBA += offsetbgr + dstpitch;
		pRGBANext = pRGBA + dstpitch;
	}
}
