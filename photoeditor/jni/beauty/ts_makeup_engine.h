/**
 * Copyright (C) 2012 Thundersoft Corporation
 * All rights Reserved
 */
#ifndef __TS_MAKEUP_ENGINE_H__
#define __TS_MAKEUP_ENGINE_H__
#include "ts_makeup_data.h"
#include "tcomdef.h"

    #define TS_OK                (0x00000000)    //Successful
    #define TS_ERROR_PARAM       (0x00000001)    //Parameters error
    #define TS_ERROR_IO          (0x00000002)    //Input or output error
    #define TS_ERROR_INTERNAL    (0x00000003)    //Internal error
    #define TS_NO_MEMORY         (0x00000004)    //No memory error

/*
	typedef enum{
		TS_BLACK_SKINA_MODEL_1,     //Black Skin Foundation Model one
		TS_BLACK_SKINA_MODEL_2,		//Black Skin Foundation Model two
		TS_BLACK_SKINA_MODEL_3,		//Black Skin Foundation Model three 
		TS_BLACK_SKINA_MODEL_4,		//Black Skin Foundation Model four
		TS_BLACK_SKINA_MODEL_5,		//Black Skin Foundation Model five
		TS_BLACK_SKINA_MODEL_6		//Black Skin Foundation Model six
	} TSMakeupBlackSkinModel;
*/

	typedef enum{
		TS_BLACK_SKINA_MODEL_1,     //Black Skin Foundation Model one
		TS_BLACK_SKINA_MODEL_2,		//Black Skin Foundation Model two
		TS_BLACK_SKINA_MODEL_3,		//Black Skin Foundation Model three 
		TS_BLACK_SKINA_MODEL_4,		//Black Skin Foundation Model four
		TS_BLACK_SKINA_MODEL_5,		//Black Skin Foundation Model five
		TS_BLACK_SKINA_MODEL_6,		//Black Skin Foundation Model six
		TS_BLACK_SKINA_MODEL_7,     
		TS_BLACK_SKINA_MODEL_8,		
		TS_BLACK_SKINA_MODEL_9,		
		TS_BLACK_SKINA_MODEL_10,		
		TS_BLACK_SKINA_MODEL_11,		
		TS_BLACK_SKINA_MODEL_12,
		TS_BLACK_SKINA_MODEL_13,		
		TS_BLACK_SKINA_MODEL_14,		
		TS_BLACK_SKINA_MODEL_15,		
		TS_BLACK_SKINA_MODEL_16
	} TSMakeupBlackSkinModel;

    /*===========================================================================
     * Data struct : TSMakeupDeblemish
     *==========================================================================*/
    typedef struct _tag_tsmakeupdeblemish {
        TPOINT        selectpoint;
        long        deblemishRadius;   // Radius of the deblemish rect
    } TSMakeupDeblemish;

    /*===========================================================================
     * Data struct : TSMakeupEye
     *==========================================================================*/
    typedef struct _tag_tsmakeeye {
        TPOINT leftEyePoint;
        signed long leftEyeRadius;
        TPOINT rightEyePoint;
        signed long rightEyeRadius;
    } TSMakeupEye;

    /*===========================================================================
     * Data struct : TSMakeupData
     *==========================================================================*/
    typedef struct  __tag_tsmakeupdata
    {
        int frameWidth;                 //YUV Frame width.MUST > 0.
        int frameHeight;                //YUV Frame height. MUST > 0.
        unsigned char *yBuf;            //Y buffer pointer.MUST not null.
        unsigned char *uvBuf;           //UV buffer pointer.MUST not null.
    }TSMakeupData;


    /*===========================================================================
     * FUNCTION   : ts_makeup_get_supported_face_num
     *
     * DESCRIPTION: get supported face number
     *
     * RETURN    : The supported face number
     *
     *==========================================================================*/
    int ts_makeup_get_supported_face_num();


    /*===========================================================================
     * FUNCTION   : ts_makeup_detect_face
     *
     * DESCRIPTION: detect face method.
     *
     * PARAMETERS :
     *   @param[in] pInData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] pFaceNum : The number of detected face.MUST not NULL.
     *   @param[in] pFaceRect : The detected face rect.MUST not NULL.
     *   @param[in] pEye : The detected eye information.MUST not NULL.
     *   @param[in] pMouth : The detected mount point.MUST not NULL.
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
    int ts_makeup_detect_face(TSMakeupData *pInData,
            int *pFaceNum, TRECT *pFaceRect, TSMakeupEye *pEye, TPOINT *pMouth);


    /*===========================================================================
     * FUNCTION   : ts_makeup_skin_clean
     *
     * DESCRIPTION: skin clean method.
     *
     * PARAMETERS :
     *   @param[in] pInData : The TSMakeupData pointer.MUST not NULL.
     *   @param[out] pOutData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] pFaceRect : The face rect.MUST not NULL.
     *   @param[in] level : Skin clean level.
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
    int ts_makeup_skin_clean(TSMakeupData *pInData, TSMakeupData *pOutData, const TRECT *pFaceRect, int level);


    /*===========================================================================
     * FUNCTION   : ts_makeup_skin_whiten
     *
     * DESCRIPTION: skin whiten method.
     *
     * PARAMETERS :
     *   @param[in/out] pData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] level : Skin whiten level.
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
    int ts_makeup_skin_whiten(TSMakeupData *pData, int level);


    /*===========================================================================
     * FUNCTION   : ts_makeup_do_deblemish
     *
     * DESCRIPTION: do deblemish method.
     *
     * PARAMETERS :
     *   @param[in] pInData : The TSMakeupData pointer.MUST not NULL.
     *   @param[out] pOutData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] faceNum : The face number.MUST > 0.
     *   @param[in] pFaceRect : The face rects.MUST not NULL.
     *   @param[in] deblemishParaNum : The deblemish paramters number.MUST > 0.
     *   @param[in] pDeblemishPara : The deblemish paramters.MUST not NULL.
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
    int ts_makeup_do_deblemish(TSMakeupData *pInData, TSMakeupData *pOutData, int faceNum, const TRECT *pFaceRect,
            int deblemishParaNum, const TSMakeupDeblemish* pDeblemishPara);


    /*===========================================================================
     * FUNCTION   : ts_makeup_warp_face
     *
     * DESCRIPTION: do warp face method.
     *
     * PARAMETERS :
     *   @param[in] pInData : The TSMakeupData pointer.MUST not NULL.
     *   @param[out] pOutData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] pEye : The position info of eyes.
     *   @param[in] pMouth : The mouth info.
     *   @param[in] bigEyegLevel : The big eye level.
     *   @param[in] trimFaceLevel : The trim face level.
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
    int ts_makeup_warp_face(TSMakeupData *pInData, TSMakeupData *pOutData,
            const TSMakeupEye *pEye, const TPOINT *pMouth, int bigEyegLevel, int trimFaceLevel);

	 /*===========================================================================
     * FUNCTION   : ts_makeup_do_foundation
     *
     * DESCRIPTION: do foundation method.(black skin)
     *
     * PARAMETERS :
     *   @param[in] pInData : The TSMakeupData pointer.MUST not NULL.
     *   @param[out] pOutData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] faceNum : The face number.MUST > 0.
     *   @param[in] pFaceRect : The face rects.MUST not NULL.
     *   @param[in] level : Skin foundation level. [0 - 100]
	 *   @param[in] foundationlevel : Skin foundation Model.
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
	int ts_makeup_do_foundation(TSMakeupData *pInData, TSMakeupData *pOutData, int faceNum, const TRECT *pFaceRect,
		 int level, TSMakeupBlackSkinModel foundationlevel);

	 /*===========================================================================
     * FUNCTION   : ts_makeup_skin_whiten
     *
     * DESCRIPTION: black skin beauty method.
     *
     * PARAMETERS :
     *   @param[in/out] pData : The TSMakeupData pointer.MUST not NULL.
     *   @param[in] level : Skin beauty level. [0 - 100]
     *
     * RETURN    : int as the return result
     *
     *==========================================================================*/
    int ts_makeup_Bskin_Beauty(TSMakeupData *pData, int level);

#endif // __TS_MAKEUP_ENGINE_H__
