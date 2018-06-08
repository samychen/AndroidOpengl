#ifndef TS_COS_METIC_H
#define TS_COS_METIC_H
#include "tcomdef.h"
#include "terror.h"
#include "tmem.h"
#include "tsoffscreen.h"
#include "tsoft_blush.h"
#include "tsoft_eyecolor.h"
#include "tsoft_eyelash.h"
#include "tsoft_eyeline.h"
#include "tsoft_eyeoutline.h"
#include "tsoft_eyeshadow.h"
#include "tsoft_lipstick.h"
//#include "tsoft_eyebrow.h"
#include "tsoft_makeup_common.h"
#include <android/asset_manager.h>

#include "jStyle.h"

#ifdef __cplusplus
extern "C" {
#endif

#ifndef MAX_PATH
#define MAX_PATH 256
#endif

#ifndef LINE_BYTES
#define LINE_BYTES(lWidth) (((lWidth)+3)/4*4)
#endif

#define TSM_ERR_NO_KEYPTS			3001
#define TSM_ERR_NO_GRAY_FILE		3002
#define TSM_ERR_GRAY_IMG			3003
#define TSM_ERR_TEMPLATE_IMG		3004

#define TSM_REAL_TIME_PREVIEW		3010
#define TSM_STATIC_PIC				3011

	typedef struct  
	{
		TCOLORREF	pcrBGRs[TSM_MAX_CHANNELS];
		TInt32		lNum;
	}COLOR_ARRAY;

	/* commands for manual mode */
	typedef enum _FaceCommand {
		/* bottom makeup stage */
		Cmd_Bottom_TeethWhiten  = 0x01,
		Cmd_Bottom_EyeBrighten  = 0x02,
		Cmd_Bottom_DePouch      = 0x03,
		Cmd_Bottom_DeBlemish    = 0x04,
		Cmd_Bottom_SkinSoften   = 0x05,
		Cmd_Bottom_SkinWhite    = 0x06,
		Cmd_Bottom_AntiShine    = 0x07,
		Cmd_Bottom_NoseHigh     = 0x08,

		/* colorful makeup stage */
		Cmd_Color_IrisColor     = 0x11,
		Cmd_Color_ContactLens   = 0x12,
		Cmd_Color_EyeShadow     = 0x13,
		Cmd_Color_EyeLine       = 0x14,
		Cmd_Color_EyeLash       = 0x15,
		Cmd_Color_EyeBrow       = 0x16,
		Cmd_Color_Blush         = 0x17,
		Cmd_Color_LipStick      = 0x18,

		/* plastic stage */
		Cmd_Plastic_EyeBig      = 0x21,
		Cmd_Plastic_FaceSmile   = 0x22,
		Cmd_Plastic_CheekLift   = 0x23,
		Cmd_Plastic_FaceThin    = 0x24
	}FaceCommand;

	typedef struct _tag_MakeUp_
	{
		TSM_IRIS irisLeft,irisRight;
		TInt32 iris_valid;
		TUInt32     eyeline_fix;
		THandle hEngine;
		/* model */
		TUInt8 *pMemMode;
		TUInt8 *pMemModeBlush;
		TUInt8 *pMemModeLipStick;
		TUInt8 *pMemModeIrisColor;
		TUInt8 *pMemModeEyeShadow;
		TUInt8 *pMemModeUpperEyeLash;
		TUInt8 *pMemModeLowerEyeLash;
		TUInt8 *pMemModeUpperEyeLine;
		TUInt8 *pMemModeLowerEyeLine;
		TUInt8 *pMemModeContactLens;
		TUInt8 *pMemModeEyeBrow;

		//FaceModelParam model_param;
		TSM_MODEL sIrisColorModel;
		TSM_MODEL sContactLensModel;
		TSM_MODEL sEyeShadowModel;
		TSM_MODEL sLowerEyeLineModel;
		TSM_MODEL sUpperEyeLineModel;
		TSM_MODEL sLowerEyeLashModel;
		TSM_MODEL sUpperEyeLashModel;
		TSM_MODEL sEyeBrowModel;
		TSM_MODEL sBlushModel;
		TSM_MODEL sLipStickModel;
	} *pMkUpEng,MkUpEng;

TRESULT TMU_Init(THandle *ppMkEng);
void    TMU_UnInit(THandle pMkEng);

TRESULT TMU_DoMakeUp(THandle pMkEng,TSOFFSCREEN * src,TSOFFSCREEN * dst,TPOINT * featPt,int nNum,TInt32 MakeUpType);

TRESULT LoadModelFromFile(TUInt8 *pMem, const char *szName, TSM_MODEL *psModel, COLOR_ARRAY *psColors, AAssetManager* asset);

TInt32 TMU_decrypt_data(TUInt8 *pData, TInt32 nLen);

void ARGB8888_to_Gray(TUInt8 *pARGB, TUInt8 *pGray, 
					  TInt32 width, TInt32 height, TInt32 pitch);
void RGBA8888_to_Gray(TUInt8 *pRGBA, TUInt8 *pGray, 
					  TInt32 width, TInt32 height, TInt32 pitch);
void YUYV_to_ARGB8888(TUInt8 *pYUYV, TUInt8 *pARGB, 
					  TInt32 width, TInt32 height, TInt32 pitch1, TInt32 pitch2);
void YUYV_to_BGRA8888(TUInt8 *pYUYV, TUInt8 *pBGRA, 
					  TInt32 width, TInt32 height, TInt32 pitch1, TInt32 pitch2);
void YUYV_to_RGBA8888(TUInt8 *pYUYV, TUInt8 *pRGBA, 
					  TInt32 width, TInt32 height, TInt32 pitch1, TInt32 pitch2);
void ARGB8888_to_YUYV(TUInt8 *pARGB, TUInt8 *pYUYV, 
					  TInt32 width, TInt32 height, TInt32 pitch);
void BGRA8888_to_YUYV(TUInt8 *pBGRA, TUInt8 *pYUYV, 
					  TInt32 width, TInt32 height, TInt32 pitch);
void RGBA8888_to_YUYV(TUInt8 *pRGBA, TUInt8 *pYUYV, 
					  TInt32 width, TInt32 height, TInt32 pitch);

TInt32 TMU_load_image(LPTSOFFSCREEN pImage,TSM_OFFSCREEN * pInterPix);

TInt32 dump_image(LPTSOFFSCREEN pImage,TSM_OFFSCREEN * pInterPix);



//one key make up --- real time 
TRESULT TMU_InitMakeUpStyle(THandle *ppMkEng);
TRESULT TMU_MakeUpStyleLoad(THandle pMkEng, TInt32 part, jStyle jstyle, int makemode, AAssetManager* asset);
TRESULT TMU_MakeUpStyle(THandle pMkEng,TSOFFSCREEN * src,TSOFFSCREEN * dst,TPOINT * featPt,int nNum, int makemode, int rotate);
TVoid   TMU_UnInitMakeUpStyle(THandle pMkEng);

TVoid BGR2YUV420VU(TUInt8 *pu8BGR, TInt32 dwBGRLine, 
				   TUInt8 *pu8YUV[], TInt32 dwYUVLine[],
				   TInt32 lWidth, TInt32 lHeight);

TVoid YUV420VU2BGR(TUInt8 *pu8YUV[], TInt32 dwYUVLine[],
				   TUInt8 *pu8BGR, TInt32 dwBGRLine,						
				   TInt32 lWidth, TInt32 lHeight);

int load_png_data_palette( const char *filepath, TSM_OFFSCREEN *psMultiChannel, AAssetManager* asset);
#ifdef __cplusplus
}

#ifdef _DEBUG
#define  LOGD(...)  __android_log_print(ANDROID_LOG_INFO,"JNI",__VA_ARGS__)
//#define  LOGD(...)  printf(__VA_ARGS__)
#else
#define  LOGD(...) 
#endif

#endif
#endif
