/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */

#include "TBilateraDenoise.h"
#include "terror.h"
#include "tBilatera.h"
#include "tmem.h"

#define NODOUBLE

#define rectlength 5
#define rectMaxlength 9

void TDo5guass(TUInt8* pMaskSrc, TLong lMaskW, TLong lMaskH)
{
	TUInt8 *ptemp, *ptemp1;
	TInt32 j, i;

	ptemp = pMaskSrc;
	ptemp1 = pMaskSrc + (lMaskH-1)*lMaskW;
	ptemp[0] = (ptemp[0]+ptemp[1]+ptemp[lMaskW]+ptemp[lMaskW+1]) >> 2;	
	ptemp1[0] = (ptemp1[0]+ptemp1[1]+ptemp1[-lMaskW]+ptemp1[-lMaskW+1]) >> 2;
	
	for (i = 1; i < lMaskW-1; i++)
	{
		ptemp[i] = (ptemp[i-1] + 8*ptemp[i] + ptemp[i+1] +
				ptemp[i+lMaskW-1] + ptemp[lMaskW+i] + ptemp[lMaskW+1+i]) >> 4;

		ptemp1[i] = (ptemp1[i-1] + 8*ptemp1[i] + ptemp1[i+1] +
				ptemp1[i-lMaskW-1] + ptemp1[-lMaskW+i] + ptemp1[-lMaskW+1+i]) >> 4;
	}
	ptemp[i] = (ptemp[i-1]+ptemp[i]+ptemp[i+lMaskW-1]+ptemp[i+lMaskW]) >> 2;
	ptemp1[i] = (ptemp1[i-1]+ptemp1[i]+ptemp1[i-lMaskW-1]+ptemp1[i-lMaskW]) >> 2;


	ptemp = pMaskSrc + lMaskW + 1;
	for (j = 1; j < lMaskH - 1; j++)
	{
		for (i = 1; i < lMaskW - 1; i++)
		{
			ptemp[i] = (ptemp[-lMaskW-1+i] + ptemp[-lMaskW+i] + ptemp[-lMaskW+1+i] +
				ptemp[i-1] + 8*ptemp[i] + ptemp[i+1] +
				ptemp[i+lMaskW-1] + ptemp[lMaskW+i] + ptemp[lMaskW+1+i]) >> 4;
		}
		ptemp += lMaskW;
	}

	ptemp = pMaskSrc+lMaskW;
	ptemp1 = pMaskSrc+2*lMaskW-1;
	for (i = 1; i < lMaskH-1; i++)
	{
		ptemp[0] = (ptemp[-lMaskW] + ptemp[-lMaskW+1] + 8*ptemp[0] + ptemp[1] +
				ptemp[lMaskW] + ptemp[lMaskW+1]) >> 4;

		ptemp1[0] = (ptemp1[-lMaskW-1] + ptemp1[-lMaskW] + ptemp1[-1] + 8*ptemp1[0] +
				ptemp1[lMaskW-1] + ptemp1[lMaskW]) >> 4;

		ptemp += lMaskW;
		ptemp1 += lMaskW;
	}

	return ;
}

void TInterlineMask(TUInt8* pMaskSrc, TLong lMaskW, TLong lMaskH, TLong lMaskLine, TUInt8* pImgMask, TLong lScale)
{
	TInt32 i, j, n;
	TInt32 lsrcwidth = lMaskW*lScale;
	TInt32 lsrcheight = lMaskH*lScale;
	TUInt8 *pMask1, *pMask2, *pTemp;
	
	pMask1 = pMaskSrc;
	pMask2 = pImgMask;
	for (j = 0; j < lsrcheight; j += lScale)
	{
		for (i = 0; i < lsrcwidth; i+= lScale)
		{
			pMask2[i] = pMask1[i/lScale];
		}
		pMask1 += lMaskLine;
		pMask2 += lsrcwidth*lScale;
	}
	
	for (i = 0; i < lsrcwidth; i += lScale)
	{
		for (j = 0; j < lsrcheight - lScale; j += lScale)
		{
			int t1, t2, t3, t4;
			pMask1 = pImgMask + j*lsrcwidth + i;
			pMask2 = pImgMask + (j+lScale)*lsrcwidth + i;
            t1 = pMask1[0];
			t2 = pMask2[0];
			t3 = t1*lScale;
			t4 = 0;
			pTemp = pMask1;
			for (n = 0; n < lScale; n++)
			{				
				//pTemp = pMask1 + n*lsrcwidth;
				pTemp = pMask1 + n*lsrcwidth;   
				//pTemp[0] = (t1*(lScale-n) + t2*n) / lScale;
				pTemp[0] = (t3 + t4) / lScale;
				t3 -= t1;
				t4 += t2;
				pTemp += lsrcwidth;
			}
		}
		{
			int t1, t3;
			pMask1 = pImgMask + j*lsrcwidth + i;
			t1 = pMask1[0];
			t3 = t1*lScale;
			for (n = 0; n < lScale; n++)
			{				
				pTemp = pMask1 + n*lsrcwidth;   
				pTemp[0] = t3  / lScale;
				t3 -= t1;
				pTemp += lsrcwidth;
			}
		}
	}

	pTemp = pImgMask;
	for (j = 0; j < lsrcheight; j++)
	{
		for (i = 0; i < lsrcwidth - lScale; i += lScale)
		{
			int t1, t2, t3, t4;
			pMask1 = pTemp + i;
			pMask2 = pTemp + i + lScale;
			t1 = pMask1[0];
			t2 = pMask2[0];
			t3 = t1*lScale;
			t4 = 0;
			for (n = 0; n < lScale; n++)
			{
				//pTemp[i+n] = (pMask1[0]*(lScale-n)+pMask2[0]*n) / lScale;
				//pTemp[i+n] = (t1*(lScale-n)+t2*n) / lScale;
				pTemp[i+n] = (t3+t4) / lScale;
				t3 -= t1;
				t4 += t2;
			}
		}
		{
			int t1, t3;
			pMask1 = pTemp + i;
			t1 = pMask1[0];
			t3 = t1*lScale;
			for (n = 0; n < lScale; n++)
			{
				pTemp[i+n] = t3 / lScale;
				t3 -= t1;
			}
		}
		pTemp += lsrcwidth;
	}

}

void TDomaskupdate(TUInt8* pMaskSrc, TLong lMaskW, TLong lMaskH)
{

	TInt32 i, j, lsize, lcurseed, temp;
	TUInt8 *ptemp;

	lsize = lMaskH*lMaskW;
	ptemp = pMaskSrc;
	lcurseed = 0;
	for (i = 0; i < lsize; i++)
	{
		temp = *ptemp++;		
		if (temp > lcurseed)
			lcurseed = temp;
	}

	if (lcurseed > 0)
	{
		ptemp = pMaskSrc;
		for (i = 0; i < lsize; i++)
		{
			temp = *ptemp;
			*ptemp = temp*255 / lcurseed;
		}
	}

	return ;	
}

void TDoRectInter002(TUInt8* pMaskSrc, TLong lMaskW, TLong lMaskH, TRECT rcFace)
{
		
	TDo5guass(pMaskSrc, lMaskW, lMaskH);
	TDo5guass(pMaskSrc, lMaskW, lMaskH);
	TDo5guass(pMaskSrc, lMaskW, lMaskH);
	TDo5guass(pMaskSrc, lMaskW, lMaskH);
	
	TDomaskupdate(pMaskSrc, lMaskW, lMaskH);
		
	return ;
}

TRESULT TBilateraDenoise(LPTSOFFSCREEN pInputImg, LPTSOFFSCREEN pOutputImage, TMASK* pFaceMask, TRECT rcFace, TLong lBlurLevel, THandle hMem)
{
	TRESULT nRes = TOK;

	TLong x, y, lReduceH, lReduceW, lMaskW, lMaskH, i, j;	
	TUInt8 *pImgMask, *ptempmask, *ptemp1, *ptemp2, *ptemp3, *pMaskdata;
	TByte *pMaskCur;
	TLong lMaskV;
	TInt32 iBlur;
	TRECT rctemp;

	TUInt32 *pTable;
#ifdef NODOUBLE
	TUInt32 tu32wp, tu32k, tu32euklidDiff, tu32factor;
#endif

	#ifdef LOGONDEVICE
	LOGE("TBilateraDenoiseOutput start! \n");
	#endif

	if (pInputImg == TNull || pOutputImage == TNull || pFaceMask == TNull)
	{
#ifdef LOGONDEVICE
LOGE("TBilateraDenoiseOutput pInputImg = %d, pOutputImage = %d, pFaceMask = %d \n", pInputImg, pOutputImage, pFaceMask); 
#endif
		nRes = TERR_INVALID_PARAM;
		goto EXIT;
	}

	pImgMask = ptempmask = pMaskdata = TNull;

	lReduceW = pInputImg->i32Width / pFaceMask->lWidth;
	lReduceH = pInputImg->i32Height / pFaceMask->lHeight;	
	lMaskW = pFaceMask->rcMask.right-pFaceMask->rcMask.left;
	lMaskH = pFaceMask->rcMask.bottom-pFaceMask->rcMask.top;

	pImgMask = (TUInt8*)TMemAlloc(hMem, lMaskH*lReduceH*lMaskW*lReduceW*sizeof(TUInt8));
	if (TNull == pImgMask)
	{
		nRes = TERR_NO_MEMORY;
		goto EXIT;
	}
	TMemSet(pImgMask, 0, lMaskH*lReduceH*lMaskW*lReduceW*sizeof(TUInt8));

	ptempmask = (TUInt8*)TMemAlloc(hMem, lMaskW*lMaskH);
	if (TNull == ptempmask)
	{
		nRes = TERR_NO_MEMORY;
		goto EXIT;
	}

	ptemp1 = pFaceMask->pData;
	ptemp2 = ptempmask;
	for (y = 0; y < lMaskH; y++)
	{
		TMemCpy(ptemp2, ptemp1, lMaskW);
		ptemp1 += pFaceMask->lMaskLine;
		ptemp2 += lMaskW;
	}
	rctemp.left = rcFace.left / lReduceW;
	rctemp.right = rcFace.right / lReduceW;
	rctemp.top = rcFace.top / lReduceH;
	rctemp.bottom = rcFace.bottom / lReduceH;

	TDoRectInter002(ptempmask, lMaskW, lMaskH, rctemp);
	TInterlineMask(ptempmask, lMaskW, lMaskH, lMaskW, pImgMask, lReduceW);

	pMaskdata = (TUInt8*)TMemAlloc(hMem, pInputImg->i32Width*pInputImg->i32Height*sizeof(TUInt8));
	if (pMaskdata == TNull)
	{
		nRes = TERR_NO_MEMORY;
		goto EXIT;
	}
	TMemSet(pMaskdata, 0, pInputImg->i32Width*pInputImg->i32Height*sizeof(TUInt8));

	ptemp1 = pImgMask;
	ptemp2 = pMaskdata;
	for (y = 0; y < lMaskH*lReduceH; y++)
	{
		TMemCpy(ptemp2, ptemp1, lMaskW*lReduceW);
		ptemp1 += lMaskW*lReduceW;
		ptemp2 += pInputImg->i32Width;
	}

	TMemFree(hMem, pImgMask);
	pImgMask = TNull;

	if (lBlurLevel < 0 || lBlurLevel > 100)
	{
#ifdef LOGONDEVICE
 LOGE("TBilateraDenoiseOutput lBlurLevel = %d !\n", lBlurLevel); 
#endif
		nRes = TERR_INVALID_PARAM;
		goto EXIT;
	}

	TMemCpy(pOutputImage->ppu8Plane[0], pInputImg->ppu8Plane[0], pInputImg->pi32Pitch[0]*pInputImg->i32Height);
	TMemCpy(pOutputImage->ppu8Plane[1], pInputImg->ppu8Plane[1], pInputImg->pi32Pitch[1]*pInputImg->i32Height/2);

	iBlur = 128 * lBlurLevel / 100.0;

// 	ptemp1 = pInputImg->ppu8Plane[0];
// 	ptemp2 = pOutputImage->ppu8Plane[0];
// 	ptemp3 = pMaskdata;

	pTable = pBliateraTable3 + 256;

	ptemp1 = pInputImg->ppu8Plane[0] + rectlength*pInputImg->pi32Pitch[0];
	ptemp2 = pOutputImage->ppu8Plane[0] + rectlength*pOutputImage->pi32Pitch[0];
	ptemp3 = pMaskdata + rectlength*pInputImg->i32Width;
	for (y = rectlength; y < pInputImg->i32Height-rectlength; y++)
	{
		for (x = rectlength; x < pInputImg->i32Width-rectlength; x++)
		{
			if (ptemp3[x] >= 5)
			{
				int centerY, currY;
				double wp, k;

				centerY = ptemp1[x];
				wp = k = 0.0;

#ifdef NODOUBLE
				tu32wp = tu32k = 0;
#endif
				
/*				
				for (j=-4;j <=4;j++)
				{
					for (i=-4;i<=4;i++)
					{
						//float delta, euklidDiff, intens, factor;
						int dx, dy, intens;
						
						dy = y + j;
						dx = x + i;
						
// 						if (dy < 0)
// 							dy = 0;
// 						else if (dy > pInputImg->i32Height-1)
// 							dy = pInputImg->i32Height-1;
// 						
// 						if (dx < 0)
// 							dx = 0;
// 						else if (dx > pInputImg->i32Width-1)
// 							dx = pInputImg->i32Width-1;
						
						currY = pInputImg->ppu8Plane[0][dy*pInputImg->pi32Pitch[0]+dx];
#ifdef NODOUBLE
						tu32euklidDiff = pBliateraTable1[5*TABS(j)+TABS(i)];
						tu32factor = pBliateraTable2[TABS(centerY - currY)];
// 						tu32wp += tu32euklidDiff*tu32factor*currY;
// 						tu32k += tu32euklidDiff*tu32factor;

						intens = tu32euklidDiff*tu32factor;
						tu32wp += intens*currY;
						tu32k += intens;

#else
						delta = sqrt((i*i+j*j));
						euklidDiff=exp(-0.5 * pow(delta/10.0,2));
						
						intens = centerY - currY;
						factor = exp(-0.5 * pow(intens/10.0,2)) * euklidDiff;
						wp += factor * currY;
						k += factor;
#endif						
					}
				}*/	

				for (j=-rectlength;j <=0;j++)
				{
					for (i=-rectlength;i<=0;i++)
					{						
						int dx, dy, intens;
						
						dy = y + j;
						dx = x + i;
						
												
						currY = pInputImg->ppu8Plane[0][dy*pInputImg->pi32Pitch[0]+dx];

						tu32euklidDiff = pBliateraTable1[rectMaxlength*(-j)-i];
						//tu32factor = pBliateraTable2[TABS(centerY - currY)];
						tu32factor = pTable[centerY - currY];
										
						intens = tu32euklidDiff*tu32factor;
						tu32wp += intens*currY;
						tu32k += intens;										
					}

					for (; i <=rectlength; i++)
					{
						int dx, dy, intens;
						
						dy = y + j;
						dx = x + i;
						
						
						currY = pInputImg->ppu8Plane[0][dy*pInputImg->pi32Pitch[0]+dx];
						
						tu32euklidDiff = pBliateraTable1[rectMaxlength*(-j)+i];
						//tu32factor = pBliateraTable2[TABS(centerY - currY)];
						tu32factor = pTable[centerY - currY];
						
						intens = tu32euklidDiff*tu32factor;
						tu32wp += intens*currY;
						tu32k += intens;
					}
				}
				
				for (;j <=rectlength;j++)
				{
					for (i=-rectlength;i<=0;i++)
					{						
						int dx, dy, intens;
						
						dy = y + j;
						dx = x + i;
						
						
						currY = pInputImg->ppu8Plane[0][dy*pInputImg->pi32Pitch[0]+dx];
						
						tu32euklidDiff = pBliateraTable1[rectMaxlength*j-i];
						//tu32factor = pBliateraTable2[TABS(centerY - currY)];
						tu32factor = pTable[centerY - currY];
						
						intens = tu32euklidDiff*tu32factor;
						tu32wp += intens*currY;
						tu32k += intens;										
					}
					
					for (; i <=rectlength; i++)
					{
						int dx, dy, intens;
						
						dy = y + j;
						dx = x + i;
						
						
						currY = pInputImg->ppu8Plane[0][dy*pInputImg->pi32Pitch[0]+dx];
						
						tu32euklidDiff = pBliateraTable1[rectMaxlength*j+i];
						//tu32factor = pBliateraTable2[TABS(centerY - currY)];
						tu32factor = pTable[centerY - currY];
						
						intens = tu32euklidDiff*tu32factor;
						tu32wp += intens*currY;
						tu32k += intens;
					}
				}	

#ifdef NODOUBLE
				currY = tu32wp / tu32k;
#else
				currY = wp / k;
#endif
				
				ptemp2[x] = (((currY * ptemp3[x] + (255 - ptemp3[x])*centerY) >> 8) * iBlur + ptemp2[x] * (128 - iBlur)) >> 7;
			}
		}
		ptemp1 += pInputImg->pi32Pitch[0];
		ptemp2 += pOutputImage->pi32Pitch[0];
		ptemp3 += pInputImg->i32Width;
	}

EXIT:

	if (pMaskdata)
		TMemFree(hMem, pMaskdata);
	if (ptempmask)
		TMemFree(hMem, ptempmask);
	if (pImgMask)
		TMemFree(hMem, pImgMask);
	
	return nRes;
}

TRESULT TBilateralBlur(LPTSOFFSCREEN pFaceImg, LPTSOFFSCREEN pDstImage, TMASK* pFaceMask, TRECT rcFace, TLong lBlurLevel, THandle hMem)
{
	TUInt8 *pSrctemp, *pDsttemp, centerY, currY;
	long x, y, i, j, width, height;
	TUInt32 iwpy, iky;
	TInt32 diff, lScale, lMaxV, iBlur;

	TUInt32 *pTableInter;

	long lMaxReduce = 1;
	TUInt8 *pMasktemp;

	if (pFaceImg == TNull || pDstImage == TNull)
	{
		return TERR_INVALID_PARAM;
	}

	if (pFaceImg->u32PixelArrayFormat != TS_PAF_NV21)
		return TERR_INVALID_PARAM;

	iBlur = 128 * lBlurLevel / 100;
	pTableInter = pBliateraBlurUInt + 255;

	lScale = 0;
	lMaxV = (pFaceImg->i32Width > pFaceImg->i32Height) ? pFaceImg->i32Width : pFaceImg->i32Height;
	while(lMaxV > 80)
	{
		lMaxV >>= 1;
		lScale++;
	}
	lMaxReduce = 1 << lScale;

	width = pFaceImg->i32Width;
	height = pFaceImg->i32Height;

	pSrctemp = pFaceImg->ppu8Plane[0] + rectlength*pFaceImg->pi32Pitch[0];
	pDsttemp = pDstImage->ppu8Plane[0] + rectlength*pDstImage->pi32Pitch[0];

	for (y = rectlength; y < height-rectlength; ++y)
	{
		pMasktemp = pFaceMask->pData + (y/lMaxReduce)*pFaceMask->lMaskLine;

		for (x = rectlength; x < width-rectlength; x++)
		{
			centerY = pSrctemp[x];
			if (pMasktemp[x/lMaxReduce] > 3)
			{

				iwpy = iky = 0;

				for (j=-rectlength;j <=rectlength;j++)
				{
					for (i=-rectlength;i<=rectlength;i++)
					{

						TUInt32 ifactor;
						int dx, dy;

						dy = y + j;
						dx = x + i;

						currY = pFaceImg->ppu8Plane[0][dy*pFaceImg->pi32Pitch[0]+dx];
						
						ifactor = pTableInter[centerY - currY];
						iwpy += ifactor*currY;
						iky += ifactor;					
					}
				}	

				centerY = iwpy / iky;
				centerY = (centerY * iBlur + (128 - iBlur) * pSrctemp[x]) >> 7;
			}

			pDsttemp[x] = TMAX(TMIN(centerY , 255), 0);
		}

		pSrctemp += pFaceImg->pi32Pitch[0];
		pDsttemp += pFaceImg->pi32Pitch[0];

	}
	return TERR_NONE;
}
