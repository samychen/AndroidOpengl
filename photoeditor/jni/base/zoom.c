#include "terror.h"
#include "tsoffscreen.h"

TRESULT FitZoomImg(LPTSOFFSCREEN pSrcImage, LPTSOFFSCREEN pDstImage)
{
	TInt32 i, j, nddx, nddy, ixxadd, iyyadd;
	TInt32 nRealWidth;
	TInt32 nRealHeight;
	TInt32 nRealPitch;
	TInt32 nHeight;
	TInt32 nWidth;
	TInt32 nPitch;
	TInt32 *pix, *piy;
	unsigned char *pSrc;
	unsigned char *pDst;

	TInt32 nZoomNumX = 1, nZoomNumY = 1;

	nRealWidth = pSrcImage->i32Width;
	nRealHeight = pSrcImage->i32Height;
	nRealPitch = pSrcImage->pi32Pitch[0];

	nHeight = pDstImage->i32Height;
	nWidth  = pDstImage->i32Width;
	nPitch  = pDstImage->pi32Pitch[0]; 

	while (nRealWidth*nZoomNumX < nWidth)
	{
		nZoomNumX++;
	}

	while (nRealHeight*nZoomNumY < nHeight)
	{
		nZoomNumY++;
	}

	pSrc = pSrcImage->ppu8Plane[0];
	pDst = pDstImage->ppu8Plane[0];

	pix = (TInt32*)malloc((nWidth+nHeight)*sizeof(TInt32));
	piy = pix + nWidth;

	nddx = 10 * nZoomNumX * nRealWidth  / nWidth;
	nddy = 10 * nZoomNumY * nRealHeight / nHeight;
	
	for (i = 0; i < nWidth; i++)
		pix[i] = i * nddx / 10;
	
	ixxadd = nZoomNumX * nRealWidth - pix[nWidth - 1];
	if (ixxadd > 1)
	{
		for (i = nWidth - ixxadd; i < nWidth; i++)
			pix[i] += i - nWidth + ixxadd;
	}
	
	for (j = 0; j < nHeight; j++)
		piy[j] = j * nddy / 10;

	iyyadd = nZoomNumY * nRealHeight - piy[nHeight - 1];
	if (iyyadd > 1)
	{
		for (i = nHeight - iyyadd; i < nHeight; i++)
			piy[i] += i - nHeight + iyyadd;
	}

	for (j = 0; j < nHeight; j++, pDst += nPitch)
	{
		for (i = 0; i < nWidth; i++)
		{
			int nStartX, nStartY, nEndx, nEndy, m, n, nSum;
			unsigned char* pY, *pTemp;
			
			nStartX =  pix[i];
			nStartY = piy[j];
			
			if (i < nWidth - 1)
				nEndx = pix[i+1];
			else
				nEndx = nZoomNumX*nRealWidth;

			if (j < nHeight - 1)	
				nEndy = piy[j+1];
			else
				nEndy = nZoomNumY*nRealHeight;
			
			/*
			nSum = 0;
			pY = pSrc + (nStartY / nZoomNumY) * nRealPitch + nStartX / nZoomNumX;
			pTemp = pY;
			for (m = nStartY; m < nEndy; m++)
			{
				for (n = 0; n < (nEndx - nStartX); n++)
				{
					nSum += pTemp[n/nZoomNumX];
				}

				if ((m % nZoomNumY) == 0)
				{
					pTemp += nRealPitch;
				}
			}
			nSum /= (nEndy - nStartY)*(nEndx - nStartX);
			pDst[i] = (unsigned char)nSum;
			*/

			nSum = 0;
			pY = pSrc + (nStartY / nZoomNumY) * nRealPitch + 4*(nStartX / nZoomNumX);
			pTemp = pY;
			for (m = nStartY; m < nEndy; m++)
			{
				for (n = 0; n < (nEndx - nStartX); n++)
				{
					nSum += pTemp[4*(n/nZoomNumX)];
				}
				
				if ((m % nZoomNumY) == 0)
				{
					pTemp += nRealPitch;
				}
			}
			nSum /= (nEndy - nStartY)*(nEndx - nStartX);
			pDst[4*i] = (unsigned char)nSum;

			nSum = 0;
			pY = pSrc + (nStartY / nZoomNumY) * nRealPitch + 4*(nStartX / nZoomNumX)+1;
			pTemp = pY;
			for (m = nStartY; m < nEndy; m++)
			{
				for (n = 0; n < (nEndx - nStartX); n++)
				{
					nSum += pTemp[4*(n/nZoomNumX)];
				}
				
				if ((m % nZoomNumY) == 0)
				{
					pTemp += nRealPitch;
				}
			}
			nSum /= (nEndy - nStartY)*(nEndx - nStartX);
			pDst[4*i+1] = (unsigned char)nSum;

			nSum = 0;
			pY = pSrc + (nStartY / nZoomNumY) * nRealPitch + 4*(nStartX / nZoomNumX)+2;
			pTemp = pY;
			for (m = nStartY; m < nEndy; m++)
			{
				for (n = 0; n < (nEndx - nStartX); n++)
				{
					nSum += pTemp[4*(n/nZoomNumX)];
				}
				
				if ((m % nZoomNumY) == 0)
				{
					pTemp += nRealPitch;
				}
			}
			nSum /= (nEndy - nStartY)*(nEndx - nStartX);
			pDst[4*i+2] = (unsigned char)nSum;

			nSum = 0;
			pY = pSrc + (nStartY / nZoomNumY) * nRealPitch + 4*(nStartX / nZoomNumX)+3;
			pTemp = pY;
			for (m = nStartY; m < nEndy; m++)
			{
				for (n = 0; n < (nEndx - nStartX); n++)
				{
					nSum += pTemp[4*(n/nZoomNumX)];
				}
				
				if ((m % nZoomNumY) == 0)
				{
					pTemp += nRealPitch;
				}
			}
			nSum /= (nEndy - nStartY)*(nEndx - nStartX);
			pDst[4*i+3] = (unsigned char)nSum;
		}
	}

	free(pix);
	return TERR_NONE;
}

void zoomRGBA(char* src, int srcW, int srcH, int stride, char* dst, int dstW, int dstH) {
	TSOFFSCREEN osSrc;
	osSrc.u32PixelArrayFormat = TS_PAF_RGB32_R8G8B8A8;
	osSrc.i32Width = srcW;
	osSrc.i32Height = srcH;
	osSrc.ppu8Plane[0] = (TUInt8*)src;
	osSrc.pi32Pitch[0] = stride;
	TSOFFSCREEN osDst;
	osDst.u32PixelArrayFormat = TS_PAF_RGB32_R8G8B8A8;
	osDst.i32Width = dstW;
	osDst.i32Height = dstH;
	osDst.ppu8Plane[0] = (TUInt8*)dst;
	osDst.pi32Pitch[0] = dstW*4;
	FitZoomImg(&osSrc, &osDst);
}