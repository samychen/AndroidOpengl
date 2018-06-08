#include "cosmetic.h"
#include "stdlib.h"
#include "stdio.h"
#include "memory.h"
#include "string.h"
#include "utils/debug.h"

#define MAX_COLOR 4 
#define TEMPLATE_POINT_NUM_MAX 6

#define TEMPLATE_BLUSH_BUF_SIZE         (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 2*400*400 + 1024)
#define TEMPLATE_LIP_BUF_SIZE           (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 128*98)
#define TEMPLATE_IRIS_BUF_SIZE          (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 256*256)
#define TEMPLATE_EYESHADOW_BUF_SIZE     (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 4*600*600)
#define TEMPLATE_UPPEREYELASH_BUF_SIZE  (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 512*220)
#define TEMPLATE_LOWEREYELASH_BUF_SIZE  (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 512*210)
#define TEMPLATE_UPPEREYELINE_BUF_SIZE  (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 250*112)
#define TEMPLATE_LOWEREYELINE_BUF_SIZE  (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 208*70)
#define TEMPLATE_CONTACTLENS_BUF_SIZE   (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 256*256*3)
#define TEMPLATE_EYEBROW_BUF_SIZE       (TEMPLATE_POINT_NUM_MAX*sizeof(TPOINT) + 88*144)


#define TEMPLATE_BUF_SIZE \
	(TEMPLATE_BLUSH_BUF_SIZE \
	+TEMPLATE_LIP_BUF_SIZE \
	+TEMPLATE_IRIS_BUF_SIZE \
	+TEMPLATE_EYESHADOW_BUF_SIZE \
	+TEMPLATE_UPPEREYELASH_BUF_SIZE \
	+TEMPLATE_LOWEREYELASH_BUF_SIZE \
	+TEMPLATE_UPPEREYELINE_BUF_SIZE \
	+TEMPLATE_LOWEREYELINE_BUF_SIZE \
	+TEMPLATE_CONTACTLENS_BUF_SIZE \
	+TEMPLATE_EYEBROW_BUF_SIZE)

TRESULT TMU_Init(THandle *ppMkEng)
{
	TRESULT nRes;	
	if (!ppMkEng)
	{
		return TERR_INVALID_PARAM;
	}

	pMkUpEng pMakeUp = (pMkUpEng)malloc(sizeof(MkUpEng));
	if (!pMakeUp)
	{
		nRes = TERR_NO_MEMORY;
		goto EXT;
	}
	memset(pMakeUp,0,sizeof(MkUpEng));

	pMakeUp->pMemMode = (TUInt8*)malloc(TEMPLATE_BUF_SIZE);
	if (!pMakeUp->pMemMode)
	{
		nRes = TERR_NO_MEMORY;
		goto EXT;
	}

	pMakeUp->pMemModeBlush        = pMakeUp->pMemMode;
	pMakeUp->pMemModeLipStick     = pMakeUp->pMemModeBlush        + TEMPLATE_BLUSH_BUF_SIZE;
	pMakeUp->pMemModeIrisColor    = pMakeUp->pMemModeLipStick     + TEMPLATE_LIP_BUF_SIZE;
	pMakeUp->pMemModeEyeShadow    = pMakeUp->pMemModeIrisColor    + TEMPLATE_IRIS_BUF_SIZE;
	pMakeUp->pMemModeUpperEyeLash = pMakeUp->pMemModeEyeShadow    + TEMPLATE_EYESHADOW_BUF_SIZE;
	pMakeUp->pMemModeLowerEyeLash = pMakeUp->pMemModeUpperEyeLash + TEMPLATE_UPPEREYELASH_BUF_SIZE;
	pMakeUp->pMemModeUpperEyeLine = pMakeUp->pMemModeLowerEyeLash + TEMPLATE_LOWEREYELASH_BUF_SIZE;
	pMakeUp->pMemModeLowerEyeLine = pMakeUp->pMemModeUpperEyeLine + TEMPLATE_UPPEREYELINE_BUF_SIZE;
	pMakeUp->pMemModeContactLens  = pMakeUp->pMemModeLowerEyeLine + TEMPLATE_LOWEREYELINE_BUF_SIZE;
	pMakeUp->pMemModeEyeBrow      = pMakeUp->pMemModeContactLens  + TEMPLATE_CONTACTLENS_BUF_SIZE;
	nRes = TSM_InitialEngine(TNull, &(pMakeUp->hEngine));

	if(TOK != nRes)
	{ 
		LOGI("[Ln%d] TSM_InitialEngine Error\n", __LINE__);
		goto EXT;
	}

	*ppMkEng = (THandle)pMakeUp; 
	return nRes;
EXT:
	TMU_UnInit(pMakeUp);
	return nRes;
}

void TMU_UnInit(THandle pMkEng)
{
	pMkUpEng pMakeUp = (pMkUpEng)pMkEng;
	if (pMakeUp)
	{
		TSM_UnInitialEngine(pMakeUp->hEngine);
		if (pMakeUp->pMemMode)
		{
			free(pMakeUp->pMemMode);
		}

		free(pMakeUp);
	}	
}

#if 0

TRESULT TMU_DoMakeUp(THandle pMkEng,TSOFFSCREEN * src,TSOFFSCREEN * dst,TPOINT * featPt,int nNum,TInt32 MakeUpType)
{
	pMkUpEng pMakeUp = (pMkUpEng)pMkEng;
	TRESULT nRes = TOK;
	TSM_OFFSCREEN ImgSrc = {0},ImgDst = {0};
	TRECT rtLeft={0}, rtRight={0};
	TSM_IRIS *pIrisLeft,*pIrisRight;
	if (!pMakeUp || !src)
	{
		return TERR_INVALID_PARAM;
	}

	nRes = TSM_SetInputFaceOutline(pMakeUp->hEngine, featPt, nNum, 0);
	if (TOK != nRes)
	{
		LOGI("[Ln%d] TSM_SetInputFaceOutline Error\n", __LINE__);
		return nRes;
	}

	ImgSrc.lHeight           = src->i32Height;
	ImgSrc.lWidth            = src->i32Width&(~1);
	ImgSrc.lPixelArrayFormat = TSM_PAF_YUV422_YUYV;
	ImgSrc.plPitch[0]        = ImgSrc.lWidth*2;	
	ImgSrc.ppPlane[0]        = malloc(ImgSrc.lHeight*ImgSrc.plPitch[0]);

	ImgDst = ImgSrc;
	ImgDst.ppPlane[0] = dst->ppu8Plane[0];
	
	nRes = TMU_load_image(src,&ImgSrc);//RGBA2YUYV
	if (TOK!=nRes)
	{
		LOGI("[Ln%d] TMU_load_image Error\n", __LINE__);
		goto EXT;
	}

	memcpy(ImgDst.ppPlane[0],ImgSrc.ppPlane[0],ImgSrc.lHeight*ImgSrc.plPitch[0]);

	nRes = TSM_SetInputImage(pMakeUp->hEngine, &ImgSrc);
	if (TOK!=nRes)
	{
		LOGI("[Ln%d] TSM_SetInputImage Error\n", __LINE__);
		goto EXT;;
	}

	
	pIrisLeft  = &pMakeUp->irisLeft;
	pIrisRight = &pMakeUp->irisRight;

	if (TFalse == pMakeUp->iris_valid) 
	{		
		TSM_IrisDetect(pMakeUp->hEngine, pIrisLeft, pIrisRight);
		nRes = TSM_SetInputIrisCircle(pMakeUp->hEngine, pIrisLeft, pIrisRight);
		if (TOK!=nRes)
		{
			LOGI("[Ln%d] TSM_SetInputIrisCircle Error\n", __LINE__);
			goto EXT;;
		}

		pMakeUp->iris_valid = TTrue;
	}
		
	switch(MakeUpType) 
	{   
	case Cmd_Color_EyeLash://½ÞÃ«
		{
			TInt32 EnableLowerEyeLash = 1;
			TInt32 ratio_thickness = 100;
			TInt32  ratio          = 100;
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeLowerEyeLash, "/sdcard/featuremodel/eyelash_lower/eyelash_lower2.ini", &pMakeUp->sLowerEyeLashModel, &sColors);
			LoadModelFromFile(pMakeUp->pMemModeUpperEyeLash, "/sdcard/featuremodel/eyelash_upper/eyelash_upper2.ini", &pMakeUp->sUpperEyeLashModel, &sColors);
			/*if (pParam) 
			{
				r = pParam->r;
				g = pParam->g;
				b = pParam->b;
				EnableLowerEyeLash = pParam->x;
				crBGR = AFM_COLOR(b,g,r);
				CHECK(AFM_SetEyeLashColor(pContext->makeup.hEngine, crBGR));
			}*/
			TSM_SetLashIntensity(pMakeUp->hEngine, ratio);
			TSM_EnableLowerEyeLash(pMakeUp->hEngine, (EnableLowerEyeLash==0)?TFalse:TTrue);


			TSM_SetLowerEyeLashModel(pMakeUp->hEngine, &pMakeUp->sLowerEyeLashModel);
			TSM_SetUpperEyeLashModel(pMakeUp->hEngine, &pMakeUp->sUpperEyeLashModel);

			TSM_EyeLash(pMakeUp->hEngine, &ImgDst, TNull, TNull);		

			if(pMakeUp->eyeline_fix)
			{
				TSM_AutoEyeLineFix(pMakeUp->hEngine, &ImgDst);
				pMakeUp->eyeline_fix = 0;
			}
			TSM_GetEyeLashArea(pMakeUp->hEngine, &rtLeft, &rtRight);
		}
		break;
	case Cmd_Color_IrisColor :
		{
			TInt32 ratio_thickness = 100;
			TInt32  ratio          = 100;
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeIrisColor, "/sdcard/featuremodel/iriscolor/iris_gray_4.ini", &pMakeUp->sIrisColorModel, &sColors);
			/*if (pParam) 
			{
				r = pParam->r;
				g = pParam->g;
				b = pParam->b;
				crBGR = AFM_COLOR(b,g,r);
				CHECK(AFM_SetIrisColor(pContext->makeup.hEngine, crBGR));
			}*/
			TSM_SetIrisIntensity(pMakeUp->hEngine, ratio);	
			TSM_SetIrisModel(pMakeUp->hEngine, &pMakeUp->sIrisColorModel);
			TSM_IrisColor(pMakeUp->hEngine, &ImgDst, TNull, TNull);
			
			rtLeft.left = pIrisLeft->ptCenter.x - pIrisLeft->lRadius;
			rtLeft.top = pIrisLeft->ptCenter.y - pIrisLeft->lRadius;
			rtLeft.right = pIrisLeft->ptCenter.x + pIrisLeft->lRadius;
			rtLeft.bottom = pIrisLeft->ptCenter.y + pIrisLeft->lRadius;
			if(rtLeft.left<0) rtLeft.left = 0;
			if(rtLeft.top<0) rtLeft.top = 0;
			if(rtLeft.right>ImgSrc.lWidth-1) rtLeft.right = ImgSrc.lWidth-1;
			if(rtLeft.bottom>ImgSrc.lHeight-1) rtLeft.bottom = ImgSrc.lHeight-1;

			rtRight.left = pIrisRight->ptCenter.x - pIrisRight->lRadius;
			rtRight.top = pIrisRight->ptCenter.y - pIrisRight->lRadius;
			rtRight.right = pIrisRight->ptCenter.x + pIrisRight->lRadius;
			rtRight.bottom = pIrisRight->ptCenter.y + pIrisRight->lRadius;
			if(rtRight.left<0) rtRight.left = 0;
			if(rtRight.top<0) rtRight.top = 0;
			if(rtRight.right>ImgSrc.lWidth-1) rtRight.right    = ImgSrc.lWidth-1;
			if(rtRight.bottom>ImgSrc.lHeight-1) rtRight.bottom = ImgSrc.lHeight-1;
			break;
		}
	case Cmd_Color_EyeBrow://Ã¼Ã«
      {
        TInt32 ratio_thickness = 100;
        TInt32  ratio          = 100;
		COLOR_ARRAY sColors;
		LoadModelFromFile(pMakeUp->pMemModeEyeBrow, "/sdcard/featuremodel/eyebrow/eyebrow.ini", &pMakeUp->sEyeBrowModel, &sColors);
       /* if (pParam) 
        {
          r = pParam->r;
          g = pParam->g;
          b = pParam->b;
          ratio_thickness = pParam->x;
          crBGR = AFM_COLOR(b,g,r);
          res = AFM_SetEyeBrowColor(pContext->makeup.hEngine, crBGR);
        }*/
        TSM_SetEyebrowIntensity(pMakeUp->hEngine, ratio);
        TSM_SetEyebrowThickness(pMakeUp->hEngine, ratio_thickness);
       
        TSM_SetEyeBrowModel(pMakeUp->hEngine, &pMakeUp->sEyeBrowModel);

        TSM_Eyebrow(pMakeUp->hEngine, &ImgDst, TNull, TNull);
      }

      break;
	case Cmd_Color_EyeLine: //ÑÛÏß
		{
			TInt32 EnableLowerEyeLine = 1;
			COLOR_ARRAY sColorsL,sColorsU;
			TInt32  ratio        = 50;
			LoadModelFromFile(pMakeUp->pMemModeLowerEyeLine, "/sdcard/featuremodel/eyeline_lower/eyeline_lower.ini",
				&pMakeUp->sLowerEyeLineModel, &sColorsL);
			LoadModelFromFile(pMakeUp->pMemModeUpperEyeLine, "/sdcard/featuremodel/eyeline_upper/eyeline_upper.ini",
				&pMakeUp->sUpperEyeLineModel, &sColorsU);
			//TSM_SetEyeLineColor(pMakeUp->hEngine, crBGR);
			/*if (pParam) 
			{
				r = pParam->r;
				g = pParam->g;
				b = pParam->b;
				EnableLowerEyeLine = pParam->x;
				crBGR = TSM_COLOR(b,g,r);
				res = TSM_SetEyeLineColor(pContext->makeup.hEngine, crBGR);
			}*/

			TSM_SetEyeLineIntensity(pMakeUp->hEngine, ratio);
			TSM_EnableLowerEyeLine(pMakeUp->hEngine, (EnableLowerEyeLine==0)?TFalse:TTrue);
			
			TSM_SetLowerEyeLineModel(pMakeUp->hEngine, &pMakeUp->sLowerEyeLineModel);
			TSM_SetUpperEyeLineModel(pMakeUp->hEngine, &pMakeUp->sUpperEyeLineModel);

			TSM_EyeLine(pMakeUp->hEngine, &ImgDst, TNull, TNull);

			//if(pContext->makeup.eyeline_fix)
			{
				TSM_AutoEyeLineFix(pMakeUp->hEngine, &ImgDst);
				//pContext->makeup.eyeline_fix = 0;
			}

			TSM_GetEyeLineArea(pMakeUp->hEngine, &rtLeft, &rtRight);
		}
		break;
	case Cmd_Color_ContactLens://ÃÀÍ«
		{ 
			TInt32  ratio        = 50;
			TSM_LEN_TYPE eType=0;	

			COLOR_ARRAY sColors;

			//eType = TSM_LEN_ACTUAL;
			
			eType = TSM_LEN_TRIAL;

			LoadModelFromFile(pMakeUp->pMemModeContactLens, "/sdcard/featuremodel/contactlens/contactlen_1.ini", &pMakeUp->sContactLensModel, &sColors);

			TSM_SetContactLenIntensity(pMakeUp->hEngine, ratio);
			
			TSM_SetLenModel(pMakeUp->hEngine, &pMakeUp->sContactLensModel, eType);

			TSM_ContactLen(pMakeUp->hEngine, &ImgDst, TNull, TNull);

			rtLeft.left = pIrisLeft->ptCenter.x - pIrisLeft->lRadius;
			rtLeft.top = pIrisLeft->ptCenter.y - pIrisLeft->lRadius;
			rtLeft.right = pIrisLeft->ptCenter.x + pIrisLeft->lRadius;
			rtLeft.bottom = pIrisLeft->ptCenter.y + pIrisLeft->lRadius;
			if(rtLeft.left<0) rtLeft.left = 0;
			if(rtLeft.top<0) rtLeft.top = 0;
			if(rtLeft.right>ImgSrc.lWidth-1)   rtLeft.right  = ImgSrc.lWidth -1;
			if(rtLeft.bottom>ImgSrc.lHeight-1) rtLeft.bottom = ImgSrc.lHeight-1;

			rtRight.left = pIrisRight->ptCenter.x - pIrisRight->lRadius;
			rtRight.top = pIrisRight->ptCenter.y - pIrisRight->lRadius;
			rtRight.right = pIrisRight->ptCenter.x + pIrisRight->lRadius;
			rtRight.bottom = pIrisRight->ptCenter.y + pIrisRight->lRadius;
			if(rtRight.left<0) rtRight.left = 0;
			if(rtRight.top<0) rtRight.top = 0;
			if(rtRight.right>ImgSrc.lWidth-1)   rtRight.right  = ImgSrc.lWidth -1;
			if(rtRight.bottom>ImgSrc.lHeight-1) rtRight.bottom = ImgSrc.lHeight-1;
		}
		break;
	case Cmd_Color_Blush://Èùºì
		{
			TInt32  ratio_gloss  = 50;
			TInt32  ratio        = 100;
			TInt32 ratio_flash  = 50;
			TInt32 i = 0;
			TCOLORREF crBGR2[MAX_COLOR] = {0};
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeBlush, "/sdcard/featuremodel/blush/blush_default.ini", &pMakeUp->sBlushModel, &sColors);

			/*if (pParam) 
			{
				for(i=0; i<pModelParam->skin_param.Blush_ColorNum; i++)
				{
					r = pParam[i].r;
					g = pParam[i].g;
					b = pParam[i].b;
					crBGR2[i] = TSM_COLOR(b,g,r);
				}
			}*/

			TSM_SetBlushIntensity(pMakeUp->hEngine, ratio);

			TSM_SetBlushModel(pMakeUp->hEngine, &pMakeUp->sBlushModel);

			TSM_SetBlushMultiColor(pMakeUp->hEngine, sColors.pcrBGRs, sColors.lNum);

			TSM_Blush(pMakeUp->hEngine, &ImgDst, TNull, TNull);

			TSM_GetBlushArea(pMakeUp->hEngine, &rtLeft, &rtRight);
		}
		break;
	case Cmd_Color_EyeShadow://ÑÛÓ°
		{
			TInt32 ratio_gloss  = 50;
			TInt32 ratio        = 100;
			TInt32 ratio_flash = 50;
			TInt32 i = 0;
			TCOLORREF crBGR2[MAX_COLOR] = {0};
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeEyeShadow, "/sdcard/featuremodel/eyeshadow/gaga.ini", &pMakeUp->sEyeShadowModel, &sColors);
			/*if (pParam) 
			{
				for(i=0; i<pModelParam->eye_param.EyeShadow_ColorNum; i++)
				{
					r = pParam[i].r;
					g = pParam[i].g;
					b = pParam[i].b;
					crBGR2[i] = TSM_COLOR(b,g,r);
				}
				ratio_flash = pParam[0].x;				
			}*/
			TSM_SetEyeShadowIntensity(pMakeUp->hEngine, ratio);
			TSM_SetEyeGlitterIntensity(pMakeUp->hEngine, ratio_flash);

			TSM_SetEyeShadowModel(pMakeUp->hEngine, &pMakeUp->sEyeShadowModel);

			TSM_SetEyeShadowMultiColor(pMakeUp->hEngine, sColors.pcrBGRs, sColors.lNum);
			//TSM_SetEyeShadowMultiColor(pMakeUp->hEngine, crBGR2, pModelParam->eye_param.EyeShadow_ColorNum);

			TSM_EyeShadow(pMakeUp->hEngine, &ImgDst, TNull, TNull);

			(TSM_GetEyeShadowArea(pMakeUp->hEngine, &rtLeft, &rtRight));
		} 
		break;
    case Cmd_Color_LipStick://¿Úºì
      {
        TInt32 ratio_gloss = 20;
		TInt32 ratio = 20;
		COLOR_ARRAY sColors;
		LoadModelFromFile(pMakeUp->pMemModeLipStick, "/sdcard/featuremodel/lipstick/lip_default.ini", &pMakeUp->sLipStickModel, &sColors);
        /*if (pParam) 
        {
          r = pParam->r;
          g = pParam->g;
          b = pParam->b;
          ratio_gloss = pParam->x;
          crBGR = TSM_COLOR(b,g,r);
          CHECK(TSM_SetLipStickColor(pContext->makeup.hEngine, crBGR));
        }*/

        TSM_SetLipStickIntensity(pMakeUp->hEngine, ratio);
        TSM_SetLipGlossIntensity(pMakeUp->hEngine, ratio_gloss);
		TSM_SetLipLineIntensity(pMakeUp->hEngine, 50);
        TSM_SetLipModel(pMakeUp->hEngine, &pMakeUp->sLipStickModel); 

		//TSM_SetModelData(pMakeUp->hEngine,pModelData,lDataBytes);
        TSM_LipStick(pMakeUp->hEngine, &ImgDst, TNull, TNull);
        TSM_GetLipArea(pMakeUp->hEngine, &rtLeft);
        memcpy(&rtRight, &rtLeft, sizeof(TRECT));
      }
      break;
    default:
      break;
    }

	dump_image(dst,&ImgDst);
EXT:
	if (ImgSrc.ppPlane[0])
	{
		free(ImgSrc.ppPlane[0]);
	}
	return nRes;
}

#endif

TRESULT TMU_InitMakeUpStyle(THandle *ppMkEng)
{
	TRESULT nRes = TOK;
	nRes = TMU_Init(ppMkEng);
	return nRes;
}

TVoid TMU_UnInitMakeUpStyle(THandle pMkEng)
{
	TMU_UnInit(pMkEng);
	return;
}

TRESULT _ReLoadBGRSeries(const char* szLine, COLOR_ARRAY *psColors)
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

TRESULT TMU_MakeUpStyleLoad(THandle pMkEng, TInt32 part, jStyle jstyle, int makemode, AAssetManager* asset)
{//makeupmode:TSM_STATIC_PIC->ºó´¦Àí , TSM_REAL_TIME_PREVIEW->ÊµÊ±
	TRESULT nRes = TOK;
	pMkUpEng pMakeUp = (pMkUpEng)pMkEng;
	if (!pMkEng)
	{
		return TERR_INVALID_PARAM;
	}

	switch(part){
	case -1://Ä£°åfull
			{
			if(makemode == TSM_STATIC_PIC)
			//ÃÀÍ«contactlen
			{
				TInt32 ratio = jstyle.getcratio();//50;
				TSM_LEN_TYPE eType = 0;

				COLOR_ARRAY sColors;

				//eType = TSM_LEN_ACTUAL;

				eType = TSM_LEN_TRIAL;
//"/sdcard/.featuremodel/contactlens/contactlen_4.ini"
			LoadModelFromFile(pMakeUp->pMemModeContactLens, jstyle.getctemp(),
					&pMakeUp->sContactLensModel, &sColors, asset);

				TSM_SetContactLenIntensity(pMakeUp->hEngine, ratio);

				TSM_SetLenModel(pMakeUp->hEngine, &pMakeUp->sContactLensModel,
						eType);
			}

			if(makemode == TSM_STATIC_PIC)
			//ÑÛÏßeyeliner
			{

				TInt32 EnableLowerEyeLine = 1;
				COLOR_ARRAY sColorsL,sColorsU;
				TInt32  ratio        = jstyle.getelineratio();
				LOGI("jstyle.getelinerlower()=%s", jstyle.getelinerlower());
				LoadModelFromFile(pMakeUp->pMemModeLowerEyeLine, jstyle.getelinerlower(),
					&pMakeUp->sLowerEyeLineModel, &sColorsL, asset);
				LOGI("jstyle.getelinerupper()=%s", jstyle.getelinerupper());
				LoadModelFromFile(pMakeUp->pMemModeUpperEyeLine, jstyle.getelinerupper(),
					&pMakeUp->sUpperEyeLineModel, &sColorsU, asset);

				TSM_SetEyeLineIntensity(pMakeUp->hEngine, ratio);
				TSM_EnableLowerEyeLine(pMakeUp->hEngine, (EnableLowerEyeLine==0)?TFalse:TTrue);

				TSM_SetLowerEyeLineModel(pMakeUp->hEngine, &pMakeUp->sLowerEyeLineModel);
				TSM_SetUpperEyeLineModel(pMakeUp->hEngine, &pMakeUp->sUpperEyeLineModel);

			}

			//ÑÛÓ°eyeshadow
			{
	//			TInt32 ratio_gloss  = 50;
				TInt32 ratio        = jstyle.geteshaderatio();
				TInt32 ratio_flash = jstyle.geteshaderfratio();
				TInt32 i = 0;
				TCOLORREF crBGR2[MAX_COLOR] = {0};
				COLOR_ARRAY sColors;
				LoadModelFromFile(pMakeUp->pMemModeEyeShadow, jstyle.geteshadertemp(), &pMakeUp->sEyeShadowModel, &sColors, asset);
				_ReLoadBGRSeries(jstyle.geteshadercolor(), &sColors);
				LOGI("sColors.lNum=%d",sColors.lNum);
				TSM_SetEyeShadowIntensity(pMakeUp->hEngine, ratio);
				TSM_SetEyeGlitterIntensity(pMakeUp->hEngine, ratio_flash);
				TSM_SetEyeShadowModel(pMakeUp->hEngine, &pMakeUp->sEyeShadowModel);
				if(sColors.lNum) TSM_SetEyeShadowMultiColor(pMakeUp->hEngine, sColors.pcrBGRs, sColors.lNum);
			}
			if(makemode == TSM_STATIC_PIC)
			//½ÞÃ«eyelash
			{
				TInt32 EnableLowerEyeLash = 1;
				TInt32  ratio          = jstyle.getelashratio();
				COLOR_ARRAY sColors;
				LoadModelFromFile(pMakeUp->pMemModeLowerEyeLash, jstyle.getelashlower(), &pMakeUp->sLowerEyeLashModel, &sColors, asset);
				LoadModelFromFile(pMakeUp->pMemModeUpperEyeLash, jstyle.getelashupper(), &pMakeUp->sUpperEyeLashModel, &sColors, asset);
				_ReLoadBGRSeries(jstyle.getelashcolor(), &sColors);

				TSM_SetLashIntensity(pMakeUp->hEngine, ratio);
				TSM_EnableLowerEyeLash(pMakeUp->hEngine, (EnableLowerEyeLash==0)?TFalse:TTrue);

				TSM_SetLowerEyeLashModel(pMakeUp->hEngine, &pMakeUp->sLowerEyeLashModel);
				TSM_SetUpperEyeLashModel(pMakeUp->hEngine, &pMakeUp->sUpperEyeLashModel);
				LOGI("lash WxH=%d,%d",pMakeUp->sUpperEyeLashModel.sData.lWidth,pMakeUp->sUpperEyeLashModel.sData.lHeight);
			}

			//Èùºìblush
			{
	//			TInt32  ratio_gloss  = 50;
				TInt32  ratio        = jstyle.getbratio();
	//			TInt32 ratio_flash  = 50;
				TInt32 i = 0;
				TCOLORREF crBGR2[MAX_COLOR] = {0};
				COLOR_ARRAY sColors;
				LoadModelFromFile(pMakeUp->pMemModeBlush, jstyle.getbtemp(), &pMakeUp->sBlushModel, &sColors, asset);
				_ReLoadBGRSeries(jstyle.getbcolor(), &sColors);


				TSM_SetBlushIntensity(pMakeUp->hEngine, ratio);
				TSM_SetBlushModel(pMakeUp->hEngine, &pMakeUp->sBlushModel);
				TSM_SetBlushMultiColor(pMakeUp->hEngine, sColors.pcrBGRs, sColors.lNum);
			}
			//¿Úºìlipstick
			{
				TInt32 ratio_gloss = jstyle.getlgloss();
				TInt32 ratio = jstyle.getlratio();
				COLOR_ARRAY sColors;
				LoadModelFromFile(pMakeUp->pMemModeLipStick, jstyle.getltemp(), &pMakeUp->sLipStickModel, &sColors, asset);
				_ReLoadBGRSeries(jstyle.getlcolor(), &sColors);

				TSM_SetLipStickColor(pMakeUp->hEngine, sColors.pcrBGRs[0]);
				TSM_SetLipStickIntensity(pMakeUp->hEngine, ratio);
				TSM_SetLipGlossIntensity(pMakeUp->hEngine, 0);
				TSM_SetLipLineIntensity(pMakeUp->hEngine, 50);
				TSM_SetLipModel(pMakeUp->hEngine, &pMakeUp->sLipStickModel);
			}

		}
			break;
	case 0://BLUSHÈùºì
		{
			//			TInt32  ratio_gloss  = 50;
			TInt32 ratio = jstyle.getbratio();
			//			TInt32 ratio_flash  = 50;
			TInt32 i = 0;
			TCOLORREF crBGR2[MAX_COLOR] = { 0 };
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeBlush, jstyle.getbtemp(),
					&pMakeUp->sBlushModel, &sColors, asset);
			_ReLoadBGRSeries(jstyle.getbcolor(), &sColors);

			TSM_SetBlushIntensity(pMakeUp->hEngine, ratio);
			TSM_SetBlushModel(pMakeUp->hEngine, &pMakeUp->sBlushModel);
			TSM_SetBlushMultiColor(pMakeUp->hEngine, sColors.pcrBGRs, sColors.lNum);
		}
		break;
	case 1://LIPSTICK¿Úºì
		{
			TInt32 ratio_gloss = jstyle.getlgloss();
			TInt32 ratio = jstyle.getlratio();
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeLipStick, jstyle.getltemp(), &pMakeUp->sLipStickModel, &sColors, asset);
			_ReLoadBGRSeries(jstyle.getlcolor(), &sColors);

			TSM_SetLipStickColor(pMakeUp->hEngine, sColors.pcrBGRs[0]);
			TSM_SetLipStickIntensity(pMakeUp->hEngine, ratio);
			TSM_SetLipGlossIntensity(pMakeUp->hEngine, 0);
			TSM_SetLipLineIntensity(pMakeUp->hEngine, 50);
			TSM_SetLipModel(pMakeUp->hEngine, &pMakeUp->sLipStickModel);
		}
		break;
	case 2://EYELASHÑÛ½ÞÃ«
		if (makemode == TSM_STATIC_PIC)
		//½ÞÃ«eyelash
		{
			TInt32 EnableLowerEyeLash = 1;
			TInt32 ratio = jstyle.getelashratio();
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeLowerEyeLash,
					jstyle.getelashlower(), &pMakeUp->sLowerEyeLashModel,
					&sColors, asset);
			LoadModelFromFile(pMakeUp->pMemModeUpperEyeLash,
					jstyle.getelashupper(), &pMakeUp->sUpperEyeLashModel,
					&sColors, asset);
			_ReLoadBGRSeries(jstyle.getelashcolor(), &sColors);

			TSM_SetLashIntensity(pMakeUp->hEngine, ratio);
			TSM_EnableLowerEyeLash(pMakeUp->hEngine,
					(EnableLowerEyeLash == 0) ? TFalse : TTrue);

			TSM_SetLowerEyeLashModel(pMakeUp->hEngine,
					&pMakeUp->sLowerEyeLashModel);
			TSM_SetUpperEyeLashModel(pMakeUp->hEngine,
					&pMakeUp->sUpperEyeLashModel);
			LOGI("lash WxH=%d,%d", pMakeUp->sUpperEyeLashModel.sData.lWidth,
					pMakeUp->sUpperEyeLashModel.sData.lHeight);
		}
		break;
	case 3://EYELINEÑÛÏß
		if (makemode == TSM_STATIC_PIC)
		//ÑÛÏßeyeliner
		{

			TInt32 EnableLowerEyeLine = 1;
			COLOR_ARRAY sColorsL, sColorsU;
			TInt32 ratio = jstyle.getelineratio();
			LOGI("jstyle.getelinerlower()=%s", jstyle.getelinerlower());
			LoadModelFromFile(pMakeUp->pMemModeLowerEyeLine,
					jstyle.getelinerlower(), &pMakeUp->sLowerEyeLineModel,
					&sColorsL, asset);
			LOGI("jstyle.getelinerupper()=%s", jstyle.getelinerupper());
			LoadModelFromFile(pMakeUp->pMemModeUpperEyeLine,
					jstyle.getelinerupper(), &pMakeUp->sUpperEyeLineModel,
					&sColorsU, asset);

			TSM_SetEyeLineIntensity(pMakeUp->hEngine, ratio);
			TSM_EnableLowerEyeLine(pMakeUp->hEngine,
					(EnableLowerEyeLine == 0) ? TFalse : TTrue);

			TSM_SetLowerEyeLineModel(pMakeUp->hEngine,
					&pMakeUp->sLowerEyeLineModel);
			TSM_SetUpperEyeLineModel(pMakeUp->hEngine,
					&pMakeUp->sUpperEyeLineModel);

		}
		break;
	case 4://EYESHADOWÑÛÓ°
		{
			//			TInt32 ratio_gloss  = 50;
			TInt32 ratio = jstyle.geteshaderatio();
			TInt32 ratio_flash = jstyle.geteshaderfratio();
			TInt32 i = 0;
			TCOLORREF crBGR2[MAX_COLOR] = { 0 };
			COLOR_ARRAY sColors;
			LoadModelFromFile(pMakeUp->pMemModeEyeShadow, jstyle.geteshadertemp(),
					&pMakeUp->sEyeShadowModel, &sColors, asset);
			_ReLoadBGRSeries(jstyle.geteshadercolor(), &sColors);
			LOGI("sColors.lNum=%d", sColors.lNum);
			TSM_SetEyeShadowIntensity(pMakeUp->hEngine, ratio);
			TSM_SetEyeGlitterIntensity(pMakeUp->hEngine, ratio_flash);
			TSM_SetEyeShadowModel(pMakeUp->hEngine, &pMakeUp->sEyeShadowModel);
			TSM_SetEyeShadowMultiColor(pMakeUp->hEngine, sColors.pcrBGRs,
					sColors.lNum);
		}
		break;
	case 5://contactlenÃÀÍ«
		{
			if (makemode == TSM_STATIC_PIC)
			//ÃÀÍ«contactlen
			{
				TInt32 ratio = jstyle.getcratio(); //50;
				TSM_LEN_TYPE eType = 0;

				COLOR_ARRAY sColors;

				//eType = TSM_LEN_ACTUAL;

				eType = TSM_LEN_TRIAL;
				//"/sdcard/.featuremodel/contactlens/contactlen_4.ini"
				LoadModelFromFile(pMakeUp->pMemModeContactLens, jstyle.getctemp(),
						&pMakeUp->sContactLensModel, &sColors, asset);

				TSM_SetContactLenIntensity(pMakeUp->hEngine, ratio);

				TSM_SetLenModel(pMakeUp->hEngine, &pMakeUp->sContactLensModel,
						eType);
			}
		}
		break;
	}
	return nRes;
}

TRESULT TMU_MakeUpStyle(THandle pMkEng,TSOFFSCREEN * src,TSOFFSCREEN * dst,TPOINT * featPt,int nNum, int makemode, int rotate)
{
	LOGI("into TMU_MakeUpStyle ");
	TRESULT nRes = TOK;
	pMkUpEng pMakeUp = (pMkUpEng)pMkEng;
	TSM_OFFSCREEN ImgSrc = {0},ImgDst = {0};
	TRECT rtLeft={0}, rtRight={0};
	//TSM_IRIS *pIrisLeft,*pIrisRight;
	if (!pMakeUp || !src)
	{
		LOGI("TERR_INVALID_PARAM err");
		return TERR_INVALID_PARAM;
	}
	


	if (src->u32PixelArrayFormat == TS_PAF_RGB32_B8G8R8A8||
		src->u32PixelArrayFormat == TS_PAF_RGB32_A8R8G8B8||
		src->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8)
	{
		ImgSrc.lHeight           = src->i32Height;
		ImgSrc.lWidth            = src->i32Width&(~1);
		ImgSrc.lPixelArrayFormat = TSM_PAF_YUV422_YUYV;
		ImgSrc.plPitch[0]        = ImgSrc.lWidth*2;	
		ImgSrc.ppPlane[0]        = malloc(ImgSrc.lHeight*ImgSrc.plPitch[0]);
		LOGI("into TMU_load_image ");
		nRes = TMU_load_image(src,&ImgSrc);//RGBA2YUYV
		if (TOK!=nRes)
		{
			LOGI("[Ln%d] TMU_load_image Error\n", __LINE__);
			goto EXT;
		}
	}
	else if (src->u32PixelArrayFormat == TS_PAF_NV21)
	{
		ImgSrc.lHeight = src->i32Height;
		ImgSrc.lWidth  = src->i32Width;
		ImgSrc.plPitch[0] = src->pi32Pitch[0];
		ImgSrc.plPitch[1] = src->pi32Pitch[1];
		ImgSrc.ppPlane[0] = src->ppu8Plane[0];
		ImgSrc.ppPlane[1] = src->ppu8Plane[1];
		ImgSrc.lPixelArrayFormat = TSM_PAF_YUV420LP_VUVU;//NV21
	}	
	else
		{
		LOGI("[Ln%d] u32PixelArrayFormat Error\n", __LINE__);
			nRes = TERR_INVALID_PARAM;
			goto EXT;
	} 

	ImgDst = ImgSrc;

	//use the src as output
	//ImgDst.ppPlane[0] = dst->ppu8Plane[0];
	LOGI("into TSM_SetInputImage ");
	nRes = TSM_SetInputImage(pMakeUp->hEngine, &ImgSrc);
	if (TOK!=nRes)
	{
		LOGI("[Ln%d] TSM_SetInputImage Error\n", __LINE__);
		goto EXT;
	}
	nRes = TSM_SetInputFaceOutline(pMakeUp->hEngine, featPt, nNum, rotate);
		if (TOK != nRes)
		{
			LOGI("[Ln%d] TSM_SetInputFaceOutline Error\n", __LINE__);
			return nRes;
		}
	{
#if 1
		if(makemode == TSM_STATIC_PIC){
		//ÃÀÍ«
			LOGI("into TSM_ContactLen ");
			nRes = TSM_ContactLen(pMakeUp->hEngine, &ImgDst, TNull, TNull);
			if (nRes!=TOK)
			{
				LOGI("[Ln%d] TSM_ContactLen error\n", __LINE__);
//				goto EXT;
			}
		}
#endif

#if 1
		if(makemode==TSM_STATIC_PIC){
			//ÑÛÏß
			LOGI("into TSM_EyeLine ");
			nRes = TSM_EyeLine(pMakeUp->hEngine, &ImgDst, TNull, TNull);
			if (nRes!=TOK)
			{
				LOGI("TSM_EyeLine error\n");
//				goto EXT;
			}
		}
#endif

#if 1
		//ÑÛÓ°
		LOGI("into TSM_EyeShadow ");
		nRes = TSM_EyeShadow(pMakeUp->hEngine, &ImgDst, TNull, TNull);
		if (nRes!=TOK)
		{
			LOGI("[Ln%d] TSM_EyeShadow Error: %d\n", __LINE__, nRes);
//			goto EXT;
		}
#endif

#if 1
		//Èùºì
		LOGI("into TSM_Blush ");
		nRes =TSM_Blush(pMakeUp->hEngine, &ImgDst, TNull, TNull);
		if (nRes!=TOK)
		{
			LOGI("[Ln%d] TSM_Blush Error\n", __LINE__);
//			goto EXT;
		}
#endif

#if 1
		////¿Úºì
		LOGI("into TSM_LipStick ");
		nRes =TSM_LipStick(pMakeUp->hEngine, &ImgDst, TNull, TNull);
		if (nRes!=TOK)
		{
			LOGI("[Ln%d] TSM_LipStick Error\n", __LINE__);
//			goto EXT;
		}
#endif

#if 1
		if(makemode==TSM_STATIC_PIC){
			//ÑÛ½ÞÃ«
			LOGI("into TSM_EyeLash ");
			nRes = TSM_EyeLash(pMakeUp->hEngine, &ImgDst, TNull, TNull);
			if (nRes!=TOK)
			{
				LOGI("TSM_EyeLash error\n");
//				goto EXT;
			}
			if(pMakeUp->eyeline_fix)
			{
				TSM_AutoEyeLineFix(pMakeUp->hEngine, &ImgDst);
				pMakeUp->eyeline_fix = 0;
			}
		}
#endif

	}

	dump_image(dst,&ImgDst);
EXT:
	LOGI("into EXT ");
	if ((src->u32PixelArrayFormat == TS_PAF_RGB32_B8G8R8A8||
	 	 src->u32PixelArrayFormat == TS_PAF_RGB32_A8R8G8B8||
		 src->u32PixelArrayFormat == TS_PAF_RGB32_R8G8B8A8 ) && ImgSrc.ppPlane[0])
	{
		free(ImgSrc.ppPlane[0]);
	}
	LOGI("out EXT ");
	
	return nRes;
}
