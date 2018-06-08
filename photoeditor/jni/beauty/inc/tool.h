/*
 * Copyright (C) 2012,2013 Thundersoft Corporation
 * All rights Reserved
 */


#ifndef __TOOL_H__
#define __TOOL_H__

#include "tcomdef.h"
#include "tmem.h"
#define  DEBUG

#ifdef WIN32
#include <stdio.h>
#include <stdlib.h> 
#define  LOGI  printf
#define  LOGE  printf

#else
//Android
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h> 
#ifdef DEBUG
#define LOG_TAG "makeup"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#define  LOGI(...)  
#define  LOGE(...)  
#endif

#endif


long gettime();
int savefile(unsigned char* pData, int size, char* name);
int loadfile(unsigned char* pData, int size, char* name);

void RGBA8888_to_NV21(TUInt8 *pRGBA, TUInt8 *pDstY,  TUInt8 *pDstUV, 
                      TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, TInt32 dstwidth, TInt32 dstheight);


void RGB888_to_NV21(TUInt8 *pRGB, TUInt8 *pDstY,  TUInt8 *pDstUV, 
                      TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, TInt32 dstwidth, TInt32 dstheight);

void NV21_to_RGB888(TUInt8 *pSrcY, TUInt8 *pSrcUV, TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, 
					TUInt8 *pRGB, TInt32 dstwidth, TInt32 dstheight, TInt32 dstpitch);


void NV21_to_RGBA8888(TUInt8 *pSrcY, TUInt8 *pSrcUV, TInt32 srcwidth, TInt32 srcheight, TInt32 srcpitch, 
					TUInt8 *pRGBA, TInt32 dstwidth, TInt32 dstheight, TInt32 dstpitch);


















#endif