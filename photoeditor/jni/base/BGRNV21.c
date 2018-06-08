#include "BGRNV21.h"
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#define TRIMBYTE(x)	(TUInt8)((x)&(~255)?((-(x))>>31):(x))

typedef struct _tag_ts_mdmt_ {
	TUInt8 *pRGBA;
	TUInt8 *pSrcY;
	TUInt8 *pSrcUV;
	TInt32 width;
	TInt32 height;
	TInt32 BgrPitch;
} TsMdmt;

void* fn_thd_TS_RGBA8888toNv21(void* arg)
{
	TsMdmt * pMd  = (TsMdmt*)arg;

	TS_RGBA8888_to_NV21_Sub(pMd->pRGBA, pMd->pSrcY, pMd->pSrcUV,
			pMd->width, pMd->height, pMd->BgrPitch);

	return NULL;
}

void TS_RGBA8888_to_NV21_MT(TUInt8 *pRGBA, TUInt8 *pSrcY, TUInt8 *pSrcUV,
						   TInt32 width, TInt32 height, TInt32 BgrPitch)
{
	TsMdmt nMdmt[2]={0};

	pthread_t tid1[2];
	int nHalfHUp = (height/2)&(~1);
	int nHalfHDn = height - nHalfHUp;
	int i;
	nMdmt[0].pRGBA     = pRGBA;
	nMdmt[0].pSrcY    = pSrcY;
	nMdmt[0].pSrcUV   = pSrcUV;
	nMdmt[0].width    = width;
	nMdmt[0].height   = nHalfHUp;
	nMdmt[0].BgrPitch = BgrPitch;

	nMdmt[1] = nMdmt[0];
	nMdmt[1].pSrcY = pSrcY + nHalfHUp*width;
	nMdmt[1].pSrcUV= pSrcUV+ (nHalfHUp/2)*width;
	nMdmt[1].pRGBA  = pRGBA  + nHalfHUp*BgrPitch;
	nMdmt[1].height= nHalfHDn;

	for (i = 0; i < 2; i++)
	{
		int err = pthread_create(tid1+i, 0, fn_thd_TS_RGBA8888toNv21, (void*)(&nMdmt[i]));
		if(err!=0)
		{
			printf("create thread 1 failed: err=%d", err);
		}
	}

	for ( i=0;i<2;i++)
	{
		pthread_join(tid1[i],0);
	}

	return;
}

void TS_RGBA8888_to_NV21_Sub(TUInt8 *pRGB, TUInt8 *py, TUInt8 *puv,
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
	TUInt8 * pY = py;
	TUInt8 * pYNext = pY+width2;
	TUInt8 * pUV = puv;

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
			pRGB++;


			Y1=(19595*r+38470*g+7471*b+32768)>>16;
			Cb=(32768*b-11059*r-21709*g+32768)>>16;
			Cr=(32768*r-27439*g-5329*b+32768)>>16;

			r = *pRGB++;
			g = *pRGB++;
			b = *pRGB++;
			pRGB++;


			Y2=(19595*r+38470*g+7471*b+32768)>>16;
			Cb+=(32768*b-11059*r-21709*g+32768)>>16;
			Cr+=(32768*r-27439*g-5329*b+32768)>>16;

			r = *pRGBNext++;
			g = *pRGBNext++;
			b = *pRGBNext++;
			pRGBNext++;


			Y3=(19595*r+38470*g+7471*b+32768)>>16;
			Cb+=(32768*b-11059*r-21709*g+32768)>>16;
			Cr+=(32768*r-27439*g-5329*b+32768)>>16;

			r = *pRGBNext++;
			g = *pRGBNext++;
			b = *pRGBNext++;
			pRGBNext++;


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
		pRGB += offset_src+pitch;
		pRGBNext = pRGB + pitch;
		pY += width2;
		pYNext = pY+width2;
		}
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

void RGBA8888_to_RGB888(TUInt8 *pRGBA, TUInt8 *pRGB, int width, int height) {
	int* pSrcPix = (int*)pRGBA;
	int size = width*height;
	while(size>0) {
		memcpy(pRGB, pSrcPix, 3);
		pRGB += 3;
		pSrcPix ++;
		size--;
	}
}

void RGB888_to_RGBA8888(TUInt8 *pRGB, TUInt8 *pRGBA, int width, int height) {
	int* pDstPix = (int*)pRGBA;
	int size = width*height;
	memset(pRGBA, 0xFF, size*4);
	while(size>0) {
		memcpy(pDstPix, pRGB, 3);
		pRGB += 3;
		pDstPix ++;
		size--;
	}
}
