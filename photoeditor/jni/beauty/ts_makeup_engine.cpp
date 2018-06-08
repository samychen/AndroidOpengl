/**
 * Copyright (C) 2012 Thundersoft Corporation
 * All rights Reserved
 */
#include <stdlib.h>
#include <time.h>
#include "ts_makeup_engine.h"
#include "tcomdef.h"
#include "tmem.h"
#include "terror.h"
#include "tsFaceBeauty.h"
#include "FaceBeautify.h"
#include "facewhiten.h"
#include "tdeblemish.h"
#include "facewarp.h"
#include "TxSkinSmooth.h"
#include "jnilogger.h"
#include "GLMakeup.h"
#include "EglGlContext.h"
#include "FilterMatrix.h"
#include "utils.h"

#define MAEUP_MAX_FACE          1

GLMakeup *pGlMakeup=NULL;
EglGlContext *pEglContext=NULL;
char* pSkinWhitenBuf=NULL;

inline void fullOffScreen(TSMakeupData *pdata, TSOFFSCREEN *pOffScrn, TUInt32 format){
    pOffScrn->u32PixelArrayFormat = format;
    pOffScrn->i32Width = pdata->frameWidth;
    pOffScrn->i32Height = pdata->frameHeight;
    pOffScrn->pi32Pitch[0] = pdata->frameWidth;
    pOffScrn->pi32Pitch[1] = pdata->frameWidth;
    pOffScrn->ppu8Plane[0] = pdata->yBuf;
    pOffScrn->ppu8Plane[1] = pdata->uvBuf;
}

inline void fullTRECT(const TRECT *pSrcRect, TRECT *pDstRect){
    pDstRect->left = pSrcRect->left;
    pDstRect->right = pSrcRect->right;
    pDstRect->bottom = pSrcRect->bottom;
    pDstRect->top = pSrcRect->top;
}

void copyNV21Image(TSOFFSCREEN* pSrcImg, TSOFFSCREEN* pDstImg){
    TUInt8 *ysrc = pSrcImg->ppu8Plane[0];
    TUInt8 *vusrc = pSrcImg->ppu8Plane[1];
    TUInt8 *ydst = pDstImg->ppu8Plane[0];
    TUInt8 *vudst = pDstImg->ppu8Plane[1];

    int minHeight = pSrcImg->i32Height < pDstImg->i32Height ? pSrcImg->i32Height : pDstImg->i32Height;
    int minYPitch = pSrcImg->pi32Pitch[0] < pDstImg->pi32Pitch[0] ? pSrcImg->pi32Pitch[0] : pDstImg->pi32Pitch[0];
    int minVUPitch = pSrcImg->pi32Pitch[1] < pDstImg->pi32Pitch[1] ? pSrcImg->pi32Pitch[1] : pDstImg->pi32Pitch[1];

    for(int i=0;i<minHeight;i++)
    {
        memcpy(ydst, ysrc, minYPitch);
        ysrc += pSrcImg->pi32Pitch[0];
        ydst += pDstImg->pi32Pitch[0];
        if(i%2==0){
            memcpy(vudst, vusrc, minVUPitch);
            vusrc += pSrcImg->pi32Pitch[1];
            vudst += pDstImg->pi32Pitch[1];
        }
    }
}

int ts_makeup_get_supported_face_num(){
    return MAEUP_MAX_FACE;
}

int ts_makeup_skin_clean(TSMakeupData *pInData, TSMakeupData *pOutData, const TRECT *pFaceRect, int level){
    if(NULL==pInData||NULL==pInData->yBuf||NULL==pInData->uvBuf||pInData->frameWidth<=0||pInData->frameHeight<=0 ||
            NULL==pOutData||NULL==pOutData->yBuf||NULL==pOutData->uvBuf||pOutData->frameWidth<=0||pOutData->frameHeight<=0 ||
            NULL==pFaceRect || level < 0){
        LOGE("function: %s,,param Error",__FUNCTION__);
        return TS_ERROR_PARAM;
    }

    TSOFFSCREEN input, output;
    fullOffScreen(pInData, &input, TSFB_FMT_YUV420LP_VUVU);
    fullOffScreen(pOutData, &output, TSFB_FMT_YUV420LP_VUVU);
    TRECT lFaceRect;
    fullTRECT(pFaceRect, &lFaceRect);

//    TRESULT result = TShzFaceBeautify(&input, &output, NULL, lFaceRect, level, NULL);
    CFaceBeautify fb;
    fb.Init(CFaceBeautify::DEAL_MODE_SKIN);
    TRESULT result = fb.Work(&input, &output, TNull, lFaceRect, level, 0);
    fb.Exit();
    if(result!= TOK){
        LOGE("function: %s,,TShzFaceBeautify Error: %d",__FUNCTION__,result);
        return TS_ERROR_INTERNAL;
    }
    return TS_OK;
}


int ts_makeup_skin_whiten(TSMakeupData *pData, int level){
//    if(NULL==pData||NULL==pData->yBuf||NULL==pData->uvBuf||pData->frameWidth<=0||pData->frameHeight<=0||
//            level < 0){
//        LOGE("function: %s,,param Error",__FUNCTION__);
//        return TS_ERROR_PARAM;
//    }

////    TSOFFSCREEN pOffscrn;
////    fullOffScreen(pData, &pOffscrn, TSFB_FMT_YUV420LP_VUVU);
////
////    TRESULT result = TWhiteningAllRegion(&pOffscrn, level, TNull);
////    if(result != TOK){
////        LOGE("function: %s,,TWhiteningAllRegion Error: %d",__FUNCTION__, result);
////        return TS_ERROR_INTERNAL;
////    }
//    if(!pGlMakeup){
//        pEglContext = new EglGlContext;
//        if(!pEglContext){
//            pEglContext=NULL;
//            return TS_NO_MEMORY;
//        }
//        pGlMakeup = new GLMakeup;
//        if(!pGlMakeup){
//            deleteC(pEglContext);
//            pGlMakeup=NULL;
//            return TS_NO_MEMORY;
//        }
//        pSkinWhitenBuf = new char[pData->frameWidth*pData->frameHeight*4];
//        if(!pSkinWhitenBuf){
//            deleteC(pEglContext);
//            deleteC(pGlMakeup);
//            return TS_NO_MEMORY;
//        }
//        pEglContext->setSurfaceSize(pData->frameWidth, pData->frameHeight);
//        pEglContext->createEGLPbufferContext();
//        glViewport(0, 0, pData->frameWidth, pData->frameHeight);
//        pGlMakeup->surfaceChanged(pData->frameWidth, pData->frameHeight);
//    }
//    int result=pGlMakeup->skinWhiten(pData->yBuf, pData->uvBuf, pData->frameWidth, pData->frameHeight,identityMatrix, MATRIX_LEN, level, pSkinWhitenBuf);
//    ts::Utils::Rgba2Yuv420((char*)pData->yBuf, (char*)pData->uvBuf, pSkinWhitenBuf, pData->frameWidth, pData->frameHeight);
//    if(result != 0){
//        LOGE("function: %s,,TWhiteningAllRegion Error: %d",__FUNCTION__, result);
//        return TS_ERROR_INTERNAL;
//    }
    return TS_OK;
}


void ts_makeup_finish(){
//    if(pGlMakeup){
//        deleteC(pGlMakeup);
//    }
//    if(pEglContext){
//        pEglContext->destroyEGLContext();
//        deleteC(pEglContext);
//    }
    if(pSkinWhitenBuf){
        delete []pSkinWhitenBuf;
        pSkinWhitenBuf=NULL;
    }
}

int ts_makeup_do_deblemish(TSMakeupData *pInData, TSMakeupData *pOutData, const TRECT *pFaceRect,
        int deblemishParaNum, const TSMakeupDeblemish* pDeblemishPara){
    if(NULL==pInData||NULL==pInData->yBuf||NULL==pInData->uvBuf||pInData->frameWidth<=0||pInData->frameHeight<=0 ||
            NULL==pOutData||NULL==pOutData->yBuf||NULL==pOutData->uvBuf||pOutData->frameWidth<=0||pOutData->frameHeight<=0 ||
            NULL==pFaceRect){
        LOGE("function: %s,,param Error",__FUNCTION__);
        return TS_ERROR_PARAM;
    }
    TLong lScale = 0, lMaxV, lMaskW, lMaskH;
    TInt32 faceMaskScale;
    TBool bFaceSkin;
    TMASK faceMask;
    TSOFFSCREEN input, output;
    TRECT faceRect;

    faceMask.pData = NULL;
    fullOffScreen(pInData, &input, TS_PAF_NV21);
    fullOffScreen(pOutData, &output, TS_PAF_NV21);
    fullTRECT(pFaceRect, &faceRect);

    lMaxV = (input.i32Width > input.i32Height) ? input.i32Width : input.i32Height;
    while(lMaxV > 80){
        lMaxV >>= 1;
        lScale++;
    }
    lMaskW = input.i32Width >> lScale;
    lMaskH = input.i32Height >> lScale;
    faceMaskScale = lScale;
    LOGE("[Ln%d] lMaskW =%d, lMaskH=%d\n", __LINE__, lMaskW, lMaskH);
    faceMask.lWidth    = lMaskW;
    faceMask.lHeight   = lMaskH;
    faceMask.lMaskLine = (lMaskW + 3) & 0xfffffffc;
//    faceMask.pData     = (TByte*)TMemAlloc(handle->hMem, handle->faceMask.lMaskLine*handle->faceMask.lHeight);
    faceMask.pData = (TByte*)malloc(faceMask.lMaskLine * faceMask.lHeight);
    if(faceMask.pData == NULL){
        LOGE("[Ln%d] faceMask.pData = NULL\n", __LINE__);
         return TS_NO_MEMORY;
    }

//        TByte* pTmpData = (TByte*)TMemAlloc(handle->hMem, 1024*1024*4);
//        THandle hMem = TMemMgrCreate(pTmpData, 1024*1024*4);
    TLong faceNum = 1;

    LOGE("[Ln%d] Before InitSkinDetector\n", __LINE__);
    THandle hSkinDetector;
    if(InitSkinDetector(NULL, &hSkinDetector) != TOK){
        LOGE("[Ln%d] InitSkinDetector falied \n", __LINE__);
    }else{
        LOGE("[Ln%d] Before DoSkinDetect\n", __LINE__);
        if(DoSkinDetect(hSkinDetector, &input, &faceRect, faceNum , &faceMask) != TOK){
            LOGE("[Ln%d] DoSkinDetect = failed\n", __LINE__);
            memset(faceMask.pData, 0, faceMask.lMaskLine * faceMask.lHeight);
            bFaceSkin = TFalse;
        }else{
            LOGE("[Ln%d] DoSkinDetect OK\n", __LINE__);
            bFaceSkin = TTrue;
            UninitSkinDetector(&hSkinDetector);

             TxMask mask;
             mask.nWidth = faceMask.lWidth;
             mask.nHeight = faceMask.lHeight;
             mask.nPitch = faceMask.lMaskLine;
             mask.pData = faceMask.pData;
            TxMaskSmooth(&mask, 2);
        }
    }

    THandle hDeblemish;
    DeblemishPara InputParameter;
    TRESULT result = InitDeblemish(NULL, &hDeblemish);
    if(result != TOK){
        LOGE("function: %s,,InitDeblemish Error: %d",__FUNCTION__, result);
        free(faceMask.pData);
        return TS_ERROR_INTERNAL;
    }

    InputParameter.lScale = faceMaskScale;
    TMemCpy(&(InputParameter.rcFace), &faceRect, sizeof(TRECT));

    for(int i=0; i<deblemishParaNum; i++){
    //  InputParameter.deblemishRadius = pParam->pDeblemishPara[i].deblemishRadius;  //TBD
/*        if(pParam->bNewDeblemish == TTrue && i == deblemishParaNum-1){
            TPOINT oldedge, newedg;
            TInt32 xRadius, yRadius;
            oldedge.x = pDeblemishPara[i].selectpoint.x + pDeblemishPara[i].deblemishRadius;
            oldedge.y = pDeblemishPara[i].selectpoint.y + pDeblemishPara[i].deblemishRadius;
LOGE("DoDeblemish Before track selectpoint[%d]: [%d, %d] \n", i, pDeblemishPara[i].selectpoint.x, pDeblemishPara[i].selectpoint.y);
            pParam->pDeblemishPara[i].selectpoint = InputParameter.selectpoint = TS_WarpFace_TrackPoint(handle->hWarp, pParam->pDeblemishPara[i].selectpoint);
            newedg = TS_WarpFace_TrackPoint(handle->hWarp, oldedge);
            xRadius = newedg.x - InputParameter.selectpoint.x;
            if(xRadius < 0)  xRadius = -xRadius;
            yRadius = newedg.y - InputParameter.selectpoint.y;
            if(yRadius < 0)  yRadius = -yRadius;
            if(xRadius < yRadius)
                xRadius = yRadius;
            pParam->pDeblemishPara[i].deblemishRadius = InputParameter.deblemishRadius = xRadius;

            LOGE("DoDeblemish After track selectpoint[%d]: [%d, %d] Radius[%d] \n", i, InputParameter.selectpoint.x, InputParameter.selectpoint.y, InputParameter.deblemishRadius);

        }else*/
        {
            InputParameter.selectpoint.x = pDeblemishPara[i].selectpoint.x;
            InputParameter.selectpoint.y = pDeblemishPara[i].selectpoint.y;
            InputParameter.deblemishRadius = pDeblemishPara[i].deblemishRadius;
        }

       LOGE("DoDeblemish hDeblemish=%d\n", hDeblemish);
        if(DoDeblemish(hDeblemish, &input, &faceMask, &output, &InputParameter, NULL) != TOK){
            LOGE("[Ln%d] DoDeblemish = failed\n", __LINE__);
            UninitDeblemish(hDeblemish);
            free(faceMask.pData);
            return TS_ERROR_INTERNAL;
        }
    }
   UninitDeblemish(hDeblemish);
   free(faceMask.pData);
   return TS_OK;
}


int ts_makeup_warp_face(TSMakeupData *pInData, TSMakeupData *pOutData,
        const TRECT *pLeftEye, const TRECT *pRightEye, const TRECT *pMouth, int bigEyeLevel, int trimFaceLevel){
    if(NULL==pInData||NULL==pInData->yBuf||NULL==pInData->uvBuf||pInData->frameWidth<=0||pInData->frameHeight<=0 ||
            NULL==pOutData||NULL==pOutData->yBuf||NULL==pOutData->uvBuf||pOutData->frameWidth<=0||pOutData->frameHeight<=0 ||
            NULL==pLeftEye || NULL==pRightEye || NULL==pMouth){
        LOGE("function: %s,,param Error",__FUNCTION__);
        return TS_ERROR_PARAM;
    }
    if(bigEyeLevel < 0 && trimFaceLevel < 0){
        LOGE("function: %s,,param Error,,bigEyeLevel: %d,,trimFaceLevel: %d",__FUNCTION__,bigEyeLevel, trimFaceLevel);
        return TS_ERROR_PARAM;
    }

    TSOFFSCREEN input, output;
    fullOffScreen(pInData, &input, TS_PAF_NV21);
    fullOffScreen(pOutData, &output, TS_PAF_NV21);

    TPOINT facekp[3];
    facekp[0].x = (pLeftEye->left+pLeftEye->right)/2;
    facekp[0].y = (pLeftEye->top+pLeftEye->bottom)/2;
    facekp[1].x = (pRightEye->left+pRightEye->right)/2;
    facekp[1].y = (pRightEye->top+pRightEye->bottom)/2;
    facekp[2].x = (pMouth->left+pMouth->right)/2;
    facekp[2].y = (pMouth->top+pMouth->bottom)/2;

    THandle hWarp;
    TRESULT result = TS_FaceWarp_Init(&hWarp, NULL, &input, facekp);
    if (TERR_NONE != result){
        LOGE("function: %s,,InitDeblemish Error: %d",__FUNCTION__, result);
        return TS_ERROR_INTERNAL;
    }

    TS_WarpFace_SetImage(hWarp, &input);
    TS_WarpFace_Reset(hWarp);

    TSOFFSCREEN retImg = TS_WarpFace_Process(hWarp, bigEyeLevel / 10, trimFaceLevel / 10, TS_FACEWARP_CHEEK | TS_FACEWARP_EYE);

    copyNV21Image(&retImg, &output);

    TS_FaceWarp_Uninit(hWarp);

    return TS_OK;
}
