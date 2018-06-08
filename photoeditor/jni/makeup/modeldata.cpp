#include "cosmetic.h"
#include "stdlib.h"
#include "memory.h"
#include "string.h"
#include "image-util.h"
#include "utils/debug.h"

int _ReadLine(AAsset* hStream, char *szLine, AAssetManager* asset)
{
	LOGI("AAsset _ReadLine");
	long i=0;
	int readBytes = 0;
	char a = 0;
	while((readBytes=AAsset_read(hStream, &a, 1))>0) {
		if(a=='\n')
			break;
		szLine[i++] = a;
	}
	szLine[i] = 0;
	LOGI("AAsset szLine=%s",szLine);
	return readBytes;
}

TRESULT _LoadKeyPts(TUInt8 **ppMem, const TPChar szLine, TSM_POINTS *psKeyPoints)
{
	TRESULT res = 0;
	TInt32 lPtsNum = 0;
	TInt32 j;
	//Get the point number
	for(j=0; j<strlen(szLine); j++)
	{
		if(szLine[j]=='(')
			lPtsNum++;
	}
	//  LOGD("[%s, Ln%d] lPtsNum=%d\n", __FILE__, __LINE__, lPtsNum);
	if(lPtsNum<=0)
	{
		res = TSM_ERR_NO_KEYPTS;
		goto EXT;
	}
	//Alloc memory
	psKeyPoints->pPoints=(TPOINT*)(*ppMem);
	*ppMem += lPtsNum*sizeof(TPOINT);
	LOGD("[Ln%d] lPtsNum*sizeof(TPOINT) = %d\n", __LINE__, lPtsNum*sizeof(TPOINT));
	if(psKeyPoints->pPoints==TNull)
	{
		res = TSM_ERR_ALLOC_MEM_FAIL;
		goto EXT;
	}
	//Read each point
	psKeyPoints->lPtsNum = lPtsNum;
	lPtsNum = 0;
	for(j=0; j<strlen(szLine); j++)
	{
		if(szLine[j]!='(')
			continue;
		sscanf(szLine+j, "(%d,%d)", &psKeyPoints->pPoints[lPtsNum].x,
			&psKeyPoints->pPoints[lPtsNum].y);
		lPtsNum++;
	}
EXT:
	return res;
}

#ifndef MIN
#define MIN(a,b) (((a) < (b)) ? (a) : (b))
#endif
TInt32 TMU_decrypt_data(TUInt8 *pData, TInt32 nLen)
{
	char *pTemp;
	char pMark[] = "Tmu";
	TInt32 nMarkLen = strlen(pMark);
	unsigned char u8Mark = (unsigned char)0x23;
	TBool bMarked = TTrue;
	TInt32 i;
	char tmp;

	pTemp = (char *)pData;	
	for(i=0; i<nLen; i++)
	{
		pTemp[i] = pTemp[i] ^ u8Mark;
	}

	//swap
	for(i=0; i<MIN(nLen/2, 125); i++)
	{
		//tmp=pTemp[2*i]; pTemp[2*i]=pTemp[2*i+1]; pTemp[2*i+1]=tmp;
		tmp = pTemp[i]; pTemp[i] = pTemp[nLen - 1 -i];
		pTemp[nLen -1 -i] = tmp;
	}
	return 0;
}

TRESULT _LoadBGRSeries(const char* szLine, COLOR_ARRAY *psColors, AAssetManager* asset)
{
	TInt32 i, j;
	char szTemp[MAX_PATH];
	strcpy(szTemp, szLine);
	psColors->lNum=0;
	//Read each BGR
	for(j=0; j<strlen(szLine); j++)
	{
		TInt32 lB, lG, lR;
		if(szLine[j]!='#')
			continue;
		j += 1;
		for (i=0; i<6; i++)
		{
			if (szTemp[j+i]>='a')
			{
				szTemp[j+i] -= 'a';
				szTemp[j+i] += 10;
			}
			else if (szTemp[j+i]>='A')
			{
				szTemp[j+i] -= 'A';
				szTemp[j+i] += 10;
			}
			else
				szTemp[j+i] -= '0';
		}
		lR = szTemp[j]*16 + szTemp[j+1];
		lG = szTemp[j+2]*16 + szTemp[j+3];
		lB = szTemp[j+4]*16 + szTemp[j+5];

		psColors->pcrBGRs[psColors->lNum] = TSM_COLOR(lB, lG, lR);
		psColors->lNum++;
	}
	return 0;
}
void RGB2GRAY(unsigned char * BGR,int nWidth,int nHeight,int nPitch,unsigned char * gray,int gPitch)
{
	int i,j;
	for (i=0;i<nHeight;i++)
	{
		unsigned char * pBgr = BGR + i*nPitch;
		unsigned char * pGry = gray+ i*gPitch;
		for (j=0;j<nWidth;j++)
		{
			pGry[j] = (pBgr[j*3]+2*pBgr[j*3+1] +pBgr[j*3+2])>>2;
		}
	}
	return;
}
int gIndex = 0;
int fread_jpg(TUInt8 **ppMem, char *filepath, TUInt8 **ppGray, TInt32 *pWidth, TInt32 *pHeight, TInt32 *pPitch, TInt32 *pBitCount, AAssetManager* asset)
{
	AAsset * pf;
	TUInt8 * pData;
	LOGI("filepath=%s", filepath);
	pf = AAssetManager_open(asset, filepath, AASSET_MODE_BUFFER);
	int lDatal = AAsset_getLength(pf);
	pData = (TUInt8*) malloc(lDatal);
	AAsset_read(pf, pData, lDatal);
	AAsset_close(pf);

	TMU_decrypt_data((TUInt8*) pData, lDatal);
//
//	if(gIndex == 0){
//		dumpToFile("/sdcard/out52x32.jpg",(unsigned char*)pData, lDatal);
//		gIndex++;
//	}else if(gIndex == 1){
//		dumpToFile("/sdcard/out128x112.jpg",(unsigned char*)pData, lDatal);
//		gIndex++;
//	}else if(gIndex == 2){
//		dumpToFile("/sdcard/out120x95.jpg",(unsigned char*)pData, lDatal);
//		gIndex++;
//	}

//	Mat img = imdecode(Mat(vData),IMREAD_UNCHANGED);//decode jpg

	Ip_Image* img = imageUtil::cvLoadJpegBuffer((char*) pData, lDatal);
//	Ip_Image ipgrey ={0};
//	ipgrey.widthStep = img->width;
//	ipgrey.width     = img->width;
//	ipgrey.height    = img->height;
//	ipgrey.nChannels = 1;
//	ipgrey.imageSize = ipgrey.widthStep*ipgrey.height;
//	ipgrey.imageData  = (char*)malloc(ipgrey.imageSize);
//	if(img->nChannels == 3)
//		RGB2GRAY((unsigned char*)(img->imageData),img->width,img->height,img->widthStep,(unsigned char*)(ipgrey.imageData),ipgrey.widthStep);
//	else
//		memcpy(ipgrey.imageData,img->imageData, img->imageSize );
//	if(gIndex++ == 0)
//	if(strcmp("/sdcard/.featuremodel/contactlens/contactlen_4.ldm", filepath) == 0){
////	if(strcmp("/sdcard/.featuremodel/blush/jiace.ldm", filepath) == 0){
//		LOGI("img size=%d,width=%d,height=%d,channels=%d, widthsetp=%d",img->imageSize, img->width,img->height,img->nChannels, img->widthStep);
//		dumpToFile("/sdcard/out.rgb", (unsigned char*)(img->imageData), (img->widthStep)*(img->height));
//	}
//	if(img->width == 52)
//		dumpToFile("/sdcard/out52x32.grey", (unsigned char*)(ipgrey.imageData), (ipgrey.widthStep)*(ipgrey.height));
//	if(img->width == 128)
//		dumpToFile("/sdcard/out128x112.grey", (unsigned char*)(ipgrey.imageData), (ipgrey.widthStep)*(ipgrey.height));
//	if(img->width == 120)
//		dumpToFile("/sdcard/out120x95.grey", (unsigned char*)(ipgrey.imageData), (ipgrey.widthStep)*(ipgrey.height));
//	LOGI("size=%d,width=%d,height=%d,channels=%d, widthsetp=%d",ipgrey.imageSize, ipgrey.width,ipgrey.height,ipgrey.nChannels, ipgrey.widthStep);
	//imwrite("dst.jpg",img);
	TUInt8 *pGray = (TUInt8 *) (*ppMem);
	*ppMem += img->imageSize;
//	*ppMem += ipgrey.imageSize;

//	memcpy(pGray, (ipgrey.imageData), ipgrey.imageSize);
	if(img->nChannels==3)
	{
		for(int i=0;i<img->height;i++)
		{
			unsigned char * pSrc = (unsigned char *)img->imageData + i*img->widthStep;
			for(int j=0;j<img->width;j++)
			{
				unsigned char Tmp = pSrc[j*3];
				pSrc[j*3]   = pSrc[j*3+2];
				pSrc[j*3+2] = Tmp;
			}
		}
	}
	memcpy(pGray, (img->imageData), img->imageSize);

	/*if (img.channels() == 3)
	{
		cvtColor(img,img,CV_BGR2GRAY);
	}

	if (img.channels() == 1)
	{
		memcpy(pGray,img.data,img.step * img.cols);
	}*/

	//free(pData);

	*ppGray = pGray;
	*pWidth = img->width;
	*pHeight = img->height;
	*pBitCount = (img->nChannels) * 8;
	*pPitch    = img->widthStep;
//	*pWidth = ipgrey.width;
//	*pHeight = ipgrey.height;
//	*pBitCount = (ipgrey.nChannels) * 8;
	if(pData){
			free(pData);
		}
//	if(ipgrey.imageData)
//	{
//	  free(ipgrey.imageData);
//	}
	imageprocess::ipReleaseImage(&img);
	return 0;
}

TRESULT _LoadGraySeries(TUInt8 **ppMem, const char* szLine, const char* szDir, TSM_OFFSCREEN *psMultiChannel, AAssetManager* asset)
{
	TRESULT res = 0;
	char szGrayName[MAX_PATH];
	TInt32 lChannel=0, lWidth=0, lHeight=0;
	TInt32 lSize=0, j;

	//Analysis current line
	for(j=0; j<strlen(szLine); j++)
	{
		TInt32 k = strlen(szDir);
		if(szLine[j]!='"')
			continue;					
		//Get the gray full name
		strcpy(szGrayName, szDir);
		j++;
		while (j<strlen(szLine) && szLine[j]!='"')
			szGrayName[k++] = szLine[j++];
		szGrayName[k] = 0;
		//Load image
		//		res = LoadBmp(hMemMgr, szGrayName, (TPOINT**)(psMultiChannel->ppPlane+lChannel), 
		//			&lWidth,&lHeight, &k);
		res = fread_jpg(ppMem, szGrayName, (TUInt8**)(psMultiChannel->ppPlane+lChannel),
			&lWidth, &lHeight, psMultiChannel->plPitch+lChannel, &k, asset);

		if(res != 0)
			goto EXT;
		if(k==24)
		{
			psMultiChannel->lWidth = lWidth;
			psMultiChannel->lHeight = lHeight;
//			psMultiChannel->plPitch[lChannel] = LINE_BYTES(psMultiChannel->lWidth*3);
			psMultiChannel->lPixelArrayFormat = TSM_PAF_BGR888;
			goto EXT;
		}
		if(k!=8)
		{
			res = TSM_ERR_GRAY_IMG;
			goto EXT;
		}
		if(lChannel == 0)
		{
			psMultiChannel->lWidth = lWidth;
			psMultiChannel->lHeight = lHeight;
		}
		else if(psMultiChannel->lWidth!=lWidth || psMultiChannel->lHeight!=lHeight)
		{
			res = TSM_ERR_GRAY_IMG;
			goto EXT;
		}
//		psMultiChannel->plPitch[lChannel] = LINE_BYTES(psMultiChannel->lWidth);
		lSize += psMultiChannel->plPitch[lChannel]*psMultiChannel->lHeight;
		lChannel++;
	}
	if(lChannel<=0)
	{
		res = TSM_ERR_NO_GRAY_FILE;
		goto EXT;
	}

	//Set the template format
	if(lChannel==1)
		psMultiChannel->lPixelArrayFormat = TSM_PAF_SINGLE_CHANNEL;
	else if(lChannel==2)
		psMultiChannel->lPixelArrayFormat = TSM_PAF_TWO_CHANNEL;
	else if(lChannel==3)
		psMultiChannel->lPixelArrayFormat = TSM_PAF_THREE_CHANNEL;
	else if(lChannel==4)
		psMultiChannel->lPixelArrayFormat = TSM_PAF_FOUR_CHANNEL;

EXT:
	return res;
}

TRESULT _LoadColorData(TUInt8 **ppMem, const char* szLine, const char* szDir,TRGBA **pColor,TSM_OFFSCREEN *psMultiChannel)
{
	TRESULT nRes = TOK;
	if (!ppMem ||!pColor||!szLine||!szDir)
	{
		return TERR_INVALID_PARAM;
	}

	char szColorName[MAX_PATH] = {0};
	for (int i=0;i<strlen(szLine);i++)
	{
		TInt32 k = strlen(szDir);
		if (szLine[i]!='"')
		{
			continue;
		}

		i++;
		//Get the gray full name
		strcpy(szColorName, szDir);

		while (i<strlen(szLine) && szLine[i]!='"')
			szColorName[k++] = szLine[i++];
		szColorName[k] = 0;

		k = strlen(szColorName);
		if (szColorName[k-1]!='t'&&szColorName[k-2]!='a')
		{
			nRes = TERR_UNSUPPORTED;
		}
		//load color data
		FILE * fColor = fopen(szColorName,"rb");
		TInt32 nLength;
		TInt32 width,height,pitch;
		if (!fColor)
		{
			nRes = TERR_INVALID_PARAM;
			goto EXT;
		}
		fseek(fColor,0,SEEK_END);
		nLength = ftell(fColor);
		fseek(fColor,0,SEEK_SET);

		fread(&width,sizeof(width),1,fColor);  //?ик
		fread(&height,sizeof(height),1,fColor);//??
		fread(&pitch,sizeof(pitch),1,fColor);  //DD?ик

		if (nLength != height*pitch+1024+3*sizeof(TInt32))
		{
			nRes = TERR_UNSUPPORTED;
			fclose(fColor);
			goto EXT;
		}
		
		fread(*ppMem,1024,1,fColor);           //?ивии?ж╠б┬иж?бу?
		*pColor = (TRGBA*)*ppMem;				
		*ppMem += 1024;

		fread(*ppMem,height*pitch,1,fColor);	//?ивии??б┬и░y?ж╠

		psMultiChannel->lHeight    = height;
		psMultiChannel->lPixelArrayFormat = TSM_PAF_SINGLE_CHANNEL;
		psMultiChannel->lWidth     = width;
		psMultiChannel->plPitch[0] = pitch;
		psMultiChannel->ppPlane[0] = *ppMem;
		fclose(fColor);	
	}
EXT:
	return nRes;
}

TRESULT _LoadColorMaskData(TUInt8 **ppMem, const char* szLine, const char* szDir,TSM_OFFSCREEN *psMultiChannel, AAssetManager* asset)
{
	TRESULT nRes = TOK;
	if (!ppMem||!szLine||!szDir)
	{
		return TERR_INVALID_PARAM;
	}

	char szColorName[MAX_PATH] = {0};
	for (int i=0;i<strlen(szLine);i++)
	{
		TInt32 k = strlen(szDir);
		if (szLine[i]!='"')
		{
			continue;
		}

		i++;
		//Get the gray full name
		strcpy(szColorName, szDir);

		while (i<strlen(szLine) && szLine[i]!='"')
			szColorName[k++] = szLine[i++];
		szColorName[k] = 0;

		k = strlen(szColorName);
		if (szColorName[k-1]!='g'&&szColorName[k-2]!='n')
		{
			nRes = TERR_UNSUPPORTED;
		}
		//load color data
		psMultiChannel->ppPlane[0] = *ppMem;
		nRes = load_png_data_palette(szColorName,psMultiChannel, asset);
		if (!nRes)
		{
			*ppMem += psMultiChannel->lHeight*psMultiChannel->plPitch[0];
		}
		else
		{
			printf("load png error %d\n",nRes);
		}
	}
EXT:
	return nRes;
}

TRESULT LoadModelFromFile(TUInt8 *pMem, const char *szName, TSM_MODEL *psModel, COLOR_ARRAY *psColors, AAssetManager* asset)
{
	char szIni[MAX_PATH];
	AAsset* hStream = TNull;
	TRESULT res = 0;
	TInt32 len=0;

	memset(psModel, 0, sizeof(TSM_MODEL));

	if(psColors!=TNull)
		memset(psColors, 0, sizeof(COLOR_ARRAY));

	strcpy(szIni, szName);

	/* remove .bmp */
	len = strlen(szIni);
	szIni[len-1] = 0;
	szIni[len-2] = 0;
	szIni[len-3] = 0;
	szIni[len-4] = 0;

	strcat(szIni, ".ini");
	LOGI("AAssetManager_open hdl=0x%X, path=%s", (int)asset, szIni);
	hStream = AAssetManager_open(asset,szIni,AASSET_MODE_UNKNOWN);
	LOGD("[%s, Ln%d] szIni=%s, hStream=0x%x\n", __FILE__, __LINE__, szIni, hStream);

	int read = 1;
	while (hStream!=TNull && read)
	{
		char szLine[MAX_PATH*2];
		TInt32 i=0;
		read =_ReadLine(hStream, szLine, asset);
		LOGD("[%s, Ln%d] szLine=%s\n", __FILE__, __LINE__, szLine);    
		for(i=0; i<strlen(szLine); i++)
		{
			if((szLine[i]>='a'&&szLine[i]<='z') || (szLine[i]>='A'||szLine[i]<='Z'))
				break;
		}

		if(!strncmp(szLine+i, "KeyPoint", strlen("KeyPoint")))
		{
			res = _LoadKeyPts(&pMem, szLine+i+strlen("KeyPoint"), &psModel->sKeyPoints);
			if(res != 0)
			{
				LOGD("[%s, Ln%d] res=%d\n", __FILE__, __LINE__, res);
				goto EXT;
			}
		}
		else if(!strncmp(szLine+i, "GrayList", strlen("GrayList")))
		{	
			char szDir[MAX_PATH];
			TInt32 j;
			//Get the fold from szName
			strcpy(szDir, szName);

			/* remove .bmp */
			len = strlen(szDir);
			szDir[len-1] = 0;
			szDir[len-2] = 0;
			szDir[len-3] = 0;
			szDir[len-4] = 0;

			for(j=strlen(szDir)-1; j>=0&&szDir[j]!='/'&&szDir[j]!='\\'; j--);
			szDir[j+1] = 0;
			res = _LoadGraySeries(&pMem, szLine+i+strlen("GrayList"), szDir, &psModel->sData, asset);
			if(res != 0)
			{
				LOGD("[%s, Ln%d] res=%d\n", __FILE__, __LINE__, res);
				goto EXT;
			}
		}
		else if(!strncmp(szLine+i, "ColorList", strlen("ColorList")))
		{
			res = TOK;
			if(psColors!=TNull)
			{
				res = _LoadBGRSeries(szLine+i+strlen("ColorList"), psColors, asset);
				if(res != 0)
				{
					LOGD("[%s, Ln%d] res=%d\n", __FILE__, __LINE__, res);
					goto EXT;
				}
			}
		}
		else if (!strncmp(szLine+i,"ColorSeq",strlen("ColorSeq")))
		{
			char szDir[MAX_PATH]={0};
			int j;
			strcpy(szDir, szName);
			/* remove .ini */
			len = strlen(szDir);
			szDir[len-1] = 0;
			szDir[len-2] = 0;
			szDir[len-3] = 0;
			szDir[len-4] = 0;
			for(j=strlen(szDir)-1; j>=0&&szDir[j]!='/'&&szDir[j]!='\\'; j--);
			szDir[j+1] = 0;
			res = _LoadColorData(&pMem,szLine+i+strlen("ColorSeq"), szDir,&psModel->sColorp,&psModel->sData);
			if (res != 0)
			{
				LOGD("[%s, Ln%d] res=%d\n", __FILE__, __LINE__, res);
				goto EXT;
			}
		}
		else if (!strncmp(szLine+i,"Model",strlen("Model")))
		{
			char szDir[MAX_PATH]={0};
			int j;
			strcpy(szDir, szName);
			/* remove .ini */
			len = strlen(szDir);
			szDir[len-1] = 0;
			szDir[len-2] = 0;
			szDir[len-3] = 0;
			szDir[len-4] = 0;
			for(j=strlen(szDir)-1; j>=0&&szDir[j]!='/'&&szDir[j]!='\\'; j--);
			szDir[j+1] = 0;
			res = _LoadColorMaskData(&pMem,szLine+i+strlen("Model"), szDir,&psModel->sData, asset);
			if (res != 0)
			{
				LOGD("[%s, Ln%d] res=%d\n", __FILE__, __LINE__, res);
				goto EXT;
			}
		}
	}
#if 1
	if(psModel->sData.lPixelArrayFormat==0) // if no "GrayList" in x.ini file, then use x.gry
	{
		char szBmp[MAX_PATH];
		TInt32 lBitCounts=0;
		strcpy(szBmp, szName);
		//MStrCat(szBmp, ".bmp");


		/* .bmp to .gry */
		szBmp[len-1] = 'm';
		szBmp[len-2] = 'd';
		szBmp[len-3] = 'l';

		//		res = LoadBmp(hMemMgr, szBmp, (TPOINT**)psModel->sData.ppPlane, 
		//			&psModel->sData.lWidth, &psModel->sData.lHeight, &lBitCounts);
		res = fread_jpg(&pMem, szBmp, (TUInt8**)psModel->sData.ppPlane, 
			&psModel->sData.lWidth, &psModel->sData.lHeight, psModel->sData.plPitch, &lBitCounts, asset);

		if(res != 0)
		{
			LOGD("[%s, Ln%d] res=%d\n", __FILE__, __LINE__, res);
			goto EXT;
		}
//		psModel->sData.plPitch[0] = LINE_BYTES(psModel->sData.lWidth*lBitCounts/8);
		if(lBitCounts == 8)
			psModel->sData.lPixelArrayFormat = TSM_PAF_GRAY;
		else if(lBitCounts == 24)
			psModel->sData.lPixelArrayFormat = TSM_PAF_BGR888;
		else
			res = TSM_ERR_TEMPLATE_IMG;
	}
#endif
EXT:
	if(hStream!=TNull)	
		AAsset_close(hStream);
	return res;
}

void RGBA8888_to_YUYV(TUInt8 *pRGBA, TUInt8 *pYUYV, 
					  TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Y1,Cb1,Cr1;
	TInt32 Y2,Cb2,Cr2;
	TInt32 Cb,Cr;
	TInt32 offset_src;
	TInt32 width2 = width&(~1);

	offset_src = pitch - width2*4;

	y = height;
	while (y-- > 0)
	{
		x = width2>>1;
		while (x-- > 0)
		{
			r = *pRGBA++;
			g = *pRGBA++;
			b = *pRGBA++;
			pRGBA++;

			Y1=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb1=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr1=(32768*r-27439*g-5329*b+32768)>>16; 

			r = *pRGBA++;
			g = *pRGBA++;
			b = *pRGBA++;
			pRGBA++;

			Y2=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb2=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr2=(32768*r-27439*g-5329*b+32768)>>16; 

			Cb = ((Cb1 + Cb2)>>1)+128;
			Cr = ((Cr1 + Cr2)>>1)+128;

			*pYUYV++ = TRIMBYTE(Y1);
			*pYUYV++ = TRIMBYTE(Cb);
			*pYUYV++ = TRIMBYTE(Y2);
			*pYUYV++ = TRIMBYTE(Cr);
		}
		pRGBA += offset_src;
	}
}

void BGRA8888_to_YUYV(TUInt8 *pBGRA, TUInt8 *pYUYV, 
					  TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Y1,Cb1,Cr1;
	TInt32 Y2,Cb2,Cr2;
	TInt32 Cb,Cr;
	TInt32 offset_src;
	TInt32 width2 = width&(~1);

	offset_src = pitch - width2*4;

	y = height;
	while (y-- > 0)
	{
		x = width2>>1;
		while (x-- > 0)
		{
			b = *pBGRA++;
			g = *pBGRA++;
			r = *pBGRA++;
			pBGRA++;

			Y1=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb1=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr1=(32768*r-27439*g-5329*b+32768)>>16; 

			b = *pBGRA++;
			g = *pBGRA++;
			r = *pBGRA++;
			pBGRA++;

			Y2=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb2=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr2=(32768*r-27439*g-5329*b+32768)>>16; 

			Cb = ((Cb1 + Cb2)>>1)+128;
			Cr = ((Cr1 + Cr2)>>1)+128;

			*pYUYV++ = TRIMBYTE(Y1);
			*pYUYV++ = TRIMBYTE(Cb);
			*pYUYV++ = TRIMBYTE(Y2);
			*pYUYV++ = TRIMBYTE(Cr);
		}
		pBGRA += offset_src;
	}
}

void ARGB8888_to_YUYV(TUInt8 *pARGB, TUInt8 *pYUYV, 
					  TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x,y;
	TInt32 r,g,b;
	TInt32 Y1,Cb1,Cr1;
	TInt32 Y2,Cb2,Cr2;
	TInt32 Cb,Cr;
	TInt32 offset_src;
	TInt32 width2 = width&(~1);

	offset_src = pitch - width2*4;

	y = height;
	while (y-- > 0)
	{
		x = width2>>1;
		while (x-- > 0)
		{
			pARGB++;
			r = *pARGB++;
			g = *pARGB++;
			b = *pARGB++;

			Y1=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb1=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr1=(32768*r-27439*g-5329*b+32768)>>16; 

			pARGB++;
			r = *pARGB++;
			g = *pARGB++;
			b = *pARGB++;

			Y2=(19595*r+38470*g+7471*b+32768)>>16;         
			Cb2=(32768*b-11059*r-21709*g+32768)>>16; 
			Cr2=(32768*r-27439*g-5329*b+32768)>>16; 

			Cb = ((Cb1 + Cb2)>>1)+128;
			Cr = ((Cr1 + Cr2)>>1)+128;

			*pYUYV++ = TRIMBYTE(Y1);
			*pYUYV++ = TRIMBYTE(Cb);
			*pYUYV++ = TRIMBYTE(Y2);
			*pYUYV++ = TRIMBYTE(Cr);
		}
		pARGB += offset_src;
	}
}

void YUYV_to_RGBA8888(TUInt8 *pYUYV, TUInt8 *pRGBA, 
					  TInt32 width, TInt32 height, TInt32 pitch1, TInt32 pitch2)
{
	TInt32 x, y;
	TInt32 Y,Cb,Y2,Cr;
	TInt32 dY;
	TInt32 r,g,b;
	TInt32 r2,g2,b2;
	TInt32 offset_src,offset_dst;
	TInt32 width2 = width&(~1);

	if (pYUYV != pRGBA) 
	{
		offset_src = pitch1 - width2*2;
		offset_dst = pitch2 - width2*4;

		y = height;
		while (y-- > 0)
		{
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV++;
				Cb = *pYUYV++;
				Y2 = *pYUYV++;
				Cr = *pYUYV++;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pRGBA++ = TRIMBYTE(r);
				*pRGBA++ = TRIMBYTE(g);
				*pRGBA++ = TRIMBYTE(b);
				*pRGBA++ = 0xFF;

				*pRGBA++ = TRIMBYTE(r2);
				*pRGBA++ = TRIMBYTE(g2);
				*pRGBA++ = TRIMBYTE(b2);
				*pRGBA++ = 0xFF;
			}
			pYUYV += offset_src;
			pRGBA += offset_dst;
		}
	}
	else /* pYUYV == pRGBA */
	{
		TUInt8 *pYUYV_Line = TNull;
		TUInt8 *pRGBA_Line = TNull;

		y = height-1;
		while (y-- > 0)
		{
			pYUYV_Line = pYUYV+(y+1)*pitch1;
			pRGBA_Line = pRGBA+(y+1)*pitch2;
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV_Line++;
				Cb = *pYUYV_Line++;
				Y2 = *pYUYV_Line++;
				Cr = *pYUYV_Line++;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pRGBA_Line++ = TRIMBYTE(r);
				*pRGBA_Line++ = TRIMBYTE(g);
				*pRGBA_Line++ = TRIMBYTE(b);
				*pRGBA_Line++ = 0xFF;

				*pRGBA_Line++ = TRIMBYTE(r2);
				*pRGBA_Line++ = TRIMBYTE(g2);
				*pRGBA_Line++ = TRIMBYTE(b2);
				*pRGBA_Line++ = 0xFF;
			}
		}

		/* y = 0 */
		{
			pYUYV_Line = pYUYV+(width2-2)*2;
			pRGBA_Line = pRGBA+(width2-2)*4;
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV_Line++;
				Cb = *pYUYV_Line++;
				Y2 = *pYUYV_Line++;
				Cr = *pYUYV_Line++;
				pYUYV_Line -= 8;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pRGBA_Line++ = TRIMBYTE(r);
				*pRGBA_Line++ = TRIMBYTE(g);
				*pRGBA_Line++ = TRIMBYTE(b);
				*pRGBA_Line++ = 0xFF;

				*pRGBA_Line++ = TRIMBYTE(r2);
				*pRGBA_Line++ = TRIMBYTE(g2);
				*pRGBA_Line++ = TRIMBYTE(b2);
				*pRGBA_Line++ = 0xFF;
				pRGBA_Line -= 16;
			}
		}

	}
}

void YUYV_to_BGRA8888(TUInt8 *pYUYV, TUInt8 *pBGRA, 
					  TInt32 width, TInt32 height, TInt32 pitch1, TInt32 pitch2)
{
	TInt32 x, y;
	TInt32 Y,Cb,Y2,Cr;
	TInt32 dY;
	TInt32 r,g,b;
	TInt32 r2,g2,b2;
	TInt32 offset_src,offset_dst;
	TInt32 width2 = width&(~1);

	if (pYUYV != pBGRA) 
	{
		offset_src = pitch1 - width2*2;
		offset_dst = pitch2 - width2*4;

		y = height;
		while (y-- > 0)
		{
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV++;
				Cb = *pYUYV++;
				Y2 = *pYUYV++;
				Cr = *pYUYV++;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pBGRA++ = TRIMBYTE(b);
				*pBGRA++ = TRIMBYTE(g);
				*pBGRA++ = TRIMBYTE(r);
				*pBGRA++ = 0xFF;

				*pBGRA++ = TRIMBYTE(b2);
				*pBGRA++ = TRIMBYTE(g2);
				*pBGRA++ = TRIMBYTE(r2);
				*pBGRA++ = 0xFF;
			}
			pYUYV += offset_src;
			pBGRA += offset_dst;
		}
	}
	else /* pYUYV == pBGRA */
	{
		TUInt8 *pYUYV_Line = TNull;
		TUInt8 *pRGBA_Line = TNull;

		y = height-1;
		while (y-- > 0)
		{
			pYUYV_Line = pYUYV+(y+1)*pitch1;
			pRGBA_Line = pBGRA+(y+1)*pitch2;
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV_Line++;
				Cb = *pYUYV_Line++;
				Y2 = *pYUYV_Line++;
				Cr = *pYUYV_Line++;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pRGBA_Line++ = TRIMBYTE(b);
				*pRGBA_Line++ = TRIMBYTE(g);
				*pRGBA_Line++ = TRIMBYTE(r);
				*pRGBA_Line++ = 0xFF;

				*pRGBA_Line++ = TRIMBYTE(b2);
				*pRGBA_Line++ = TRIMBYTE(g2);
				*pRGBA_Line++ = TRIMBYTE(r2);
				*pRGBA_Line++ = 0xFF;
			}
		}

		/* y = 0 */
		{
			pYUYV_Line = pYUYV+(width2-2)*2;
			pRGBA_Line = pBGRA+(width2-2)*4;
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV_Line++;
				Cb = *pYUYV_Line++;
				Y2 = *pYUYV_Line++;
				Cr = *pYUYV_Line++;
				pYUYV_Line -= 8;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pRGBA_Line++ = TRIMBYTE(b);
				*pRGBA_Line++ = TRIMBYTE(g);
				*pRGBA_Line++ = TRIMBYTE(r);
				*pRGBA_Line++ = 0xFF;

				*pRGBA_Line++ = TRIMBYTE(b2);
				*pRGBA_Line++ = TRIMBYTE(g2);
				*pRGBA_Line++ = TRIMBYTE(r2);
				*pRGBA_Line++ = 0xFF;
				pRGBA_Line -= 16;
			}
		}

	}
}

void YUYV_to_ARGB8888(TUInt8 *pYUYV, TUInt8 *pARGB, 
					  TInt32 width, TInt32 height, TInt32 pitch1, TInt32 pitch2)
{
	TInt32 x, y;
	TInt32 Y,Cb,Y2,Cr;
	TInt32 dY;
	TInt32 r,g,b;
	TInt32 r2,g2,b2;
	TInt32 offset_src,offset_dst;
	TInt32 width2 = width&(~1);

	if (pYUYV != pARGB) 
	{
		offset_src = pitch1 - width2*2;
		offset_dst = pitch2 - width2*4;

		y = height;
		while (y-- > 0)
		{
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV++;
				Cb = *pYUYV++;
				Y2 = *pYUYV++;
				Cr = *pYUYV++;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pARGB++ = 0xFF;
				*pARGB++ = TRIMBYTE(r);
				*pARGB++ = TRIMBYTE(g);
				*pARGB++ = TRIMBYTE(b);

				*pARGB++ = 0xFF;
				*pARGB++ = TRIMBYTE(r2);
				*pARGB++ = TRIMBYTE(g2);
				*pARGB++ = TRIMBYTE(b2);
			}
			pYUYV += offset_src;
			pARGB += offset_dst;
		}
	}
	else /* pYUYV == pARGB */
	{
		TUInt8 *pYUYV_Line = TNull;
		TUInt8 *pARGB_Line = TNull;

		y = height-1;
		while (y-- > 0)
		{
			pYUYV_Line = pYUYV+(y+1)*pitch1;
			pARGB_Line = pARGB+(y+1)*pitch2;
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV_Line++;
				Cb = *pYUYV_Line++;
				Y2 = *pYUYV_Line++;
				Cr = *pYUYV_Line++;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pARGB_Line++ = 0xFF;
				*pARGB_Line++ = TRIMBYTE(r);
				*pARGB_Line++ = TRIMBYTE(g);
				*pARGB_Line++ = TRIMBYTE(b);

				*pARGB_Line++ = 0xFF;
				*pARGB_Line++ = TRIMBYTE(r2);
				*pARGB_Line++ = TRIMBYTE(g2);
				*pARGB_Line++ = TRIMBYTE(b2);
			}
		}


		/* y = 0 */
		{
			pYUYV_Line = pYUYV+(width2-2)*2;
			pARGB_Line = pARGB+(width2-2)*4;
			x = width2>>1;
			while (x-- > 0)
			{
				Y  = *pYUYV_Line++;
				Cb = *pYUYV_Line++;
				Y2 = *pYUYV_Line++;
				Cr = *pYUYV_Line++;
				pYUYV_Line -= 8;

				Cb-=128; Cr-=128;

				r = Y+((91881*Cr+32768)>>16);
				g = Y-((22554*Cb+46802*Cr+32768)>>16);
				b = Y+((116130*Cb+32768)>>16);

				dY = Y2-Y;

				r2 = r+dY;
				g2 = g+dY;
				b2 = b+dY;

				*pARGB_Line++ = 0xFF;
				*pARGB_Line++ = TRIMBYTE(r);
				*pARGB_Line++ = TRIMBYTE(g);
				*pARGB_Line++ = TRIMBYTE(b);

				*pARGB_Line++ = 0xFF;
				*pARGB_Line++ = TRIMBYTE(r2);
				*pARGB_Line++ = TRIMBYTE(g2);
				*pARGB_Line++ = TRIMBYTE(b2);
				pARGB_Line -= 16;
			}
		}

	}
}

void RGBA8888_to_Gray(TUInt8 *pRGBA, TUInt8 *pGray, 
					  TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x, y;
	TInt32 offset_src;

	offset_src = pitch - width*4;

	y = height;
	while (y-- > 0)
	{
		x = width;
		while (x-- > 0)
		{
			*pGray++ = *pRGBA;
			pRGBA += 4;
		}
		pRGBA += offset_src;
	}
}

void ARGB8888_to_Gray(TUInt8 *pARGB, TUInt8 *pGray, 
					  TInt32 width, TInt32 height, TInt32 pitch)
{
	TInt32 x, y;
	TInt32 offset_src;
	TUInt8 *ptr = pARGB+2;

	offset_src = pitch - width*4;

	y = height;
	while (y-- > 0)
	{
		x = width;
		while (x-- > 0)
		{
			*pGray++ = *ptr;
			ptr += 4;
		}
		ptr += offset_src;
	}
}


TInt32 dump_image(LPTSOFFSCREEN pImage,TSM_OFFSCREEN * pInterPix)
{	
	if (TNull == pImage || TNull == pInterPix)
	{
		LOGD("[Ln%d] hHandle = TNull\n", __LINE__);
		return -1;
	}

	int pixel_format = pImage->u32PixelArrayFormat;
	if(TS_PAF_RGB32_R8G8B8A8 == pixel_format)
	{
		YUYV_to_RGBA8888((TUInt8*)pInterPix->ppPlane[0], (TUInt8*)pImage->ppu8Plane[0], pInterPix->lWidth, pInterPix->lHeight, pInterPix->plPitch[0], pImage->pi32Pitch[0]);	
	}
	else if (TS_PAF_RGB32_A8R8G8B8 == pixel_format)
	{
		YUYV_to_ARGB8888((TUInt8*)pInterPix->ppPlane[0], (TUInt8*)pImage->ppu8Plane[0], pInterPix->lWidth, pInterPix->lHeight, pInterPix->plPitch[0], pImage->pi32Pitch[0]);
		
	}
	else if (TS_PAF_RGB32_B8G8R8A8 == pixel_format)
	{
		YUYV_to_BGRA8888((TUInt8*)pInterPix->ppPlane[0], (TUInt8*)pImage->ppu8Plane[0], pInterPix->lWidth, pInterPix->lHeight, pInterPix->plPitch[0], pImage->pi32Pitch[0]);
	}

	return 0;
}

TInt32 TMU_load_image(LPTSOFFSCREEN pImage,TSM_OFFSCREEN * pInterPix)
{
  TInt32 ret = 0;
  TUInt8 *pPixel = TNull;
  TInt32 width  = 0, height = 0, pitch = 0;
  //TInt32 width2 = 0;
 
  if (TNull == pImage || TNull == pInterPix)
  {
    LOGD("[Ln%d] pImage = TNull\n", __LINE__);
    return -1;
  }

  if ( (TS_PAF_RGB32_R8G8B8A8 != pImage->u32PixelArrayFormat)
    && (TS_PAF_RGB32_A8R8G8B8 != pImage->u32PixelArrayFormat)
    && (TS_PAF_RGB32_B8G8R8A8 != pImage->u32PixelArrayFormat) )
  {
    LOGD("[Ln%d] invalid color format\n", __LINE__);
    return TERR_UNSUPPORTED;
  }

  pPixel =  pImage->ppu8Plane[0];
  width  =  pImage->i32Width;
  height =  pImage->i32Height;
  pitch  =  pImage->pi32Pitch[0];

  //width2 = width&(~1);

  //pContext->pixel_format = pImage->u32PixelArrayFormat;

  ///* clean status */
  //pContext->face_id = -1;
  //MMemSet(pContext->face_dirty, 0, MAX_FACE_NUM*sizeof(TInt32));
  //MMemSet(pContext->model_param_array, 0, MAX_FACE_NUM*sizeof(FaceModelParam));
  //pContext->makeup.iris_valid = MFalse;
  //MMemSet(&pContext->makeup.irisLeft, 0, sizeof(TSM_IRIS));
  //MMemSet(&pContext->makeup.irisRight, 0, sizeof(TSM_IRIS));

  //pContext->width = width;
  //pContext->height = height;
  //pContext->pitch = pitch;
  //pContext->width2 = width2;
  //LOGD("[Ln%d] width=%d", __LINE__, width);
  //LOGD("[Ln%d] width2=%d", __LINE__, width2);
  //LOGD("[Ln%d] height=%d", __LINE__, height);
  //LOGD("[Ln%d] pitch=%d", __LINE__, pitch);

  //if (pContext->pixel)
  //  MMemFree(pContext->hMem, pContext->pixel);
  //pContext->pixel = (TUInt8 *)MMemAlloc(pContext->hMem, height*width2*2);
  //LOGE("[Ln%d] YUYV = %d", __LINE__, height*width2*2);
  //LOGD("[Ln%d] pixel=0x%x", __LINE__, pContext->pixel);
  //if (TNull == pContext->pixel)
  //{
  //  LOGD("[Ln%d] out of memory", __LINE__);
  //  return -1;
  //}

  if(TS_PAF_RGB32_R8G8B8A8 == pImage->u32PixelArrayFormat)
  {    
    RGBA8888_to_YUYV(pPixel, (TUInt8*)pInterPix->ppPlane[0], width, height, pitch);    
  }
  else if (TS_PAF_RGB32_A8R8G8B8 == pImage->u32PixelArrayFormat)
  {
    ARGB8888_to_YUYV(pPixel, (TUInt8*)pInterPix->ppPlane[0], width, height, pitch);    
  }
  else if(TS_PAF_RGB32_B8G8R8A8 == pImage->u32PixelArrayFormat)
  {
    BGRA8888_to_YUYV(pPixel, (TUInt8*)pInterPix->ppPlane[0], width, height, pitch);
  }

  LOGD("[Ln%d] load_image OK\n", __LINE__);
/*
  {
    FILE *pf = fopen("/sdcard/debug.yuv", "wb");
    if (pf) {
      fwrite(pContext->pixel, 1, height*width2*2, pf);
      fclose(pf);
    }
  }
*/

  return ret;
}

