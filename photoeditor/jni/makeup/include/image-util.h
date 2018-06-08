/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 */

#ifndef _IMAGE_UTIL_H_
#define _IMAGE_UTIL_H_

#include "imageprocess.h"
using namespace imageprocess;

namespace imageUtil {
//yuv to rgb8888
int* YUV2BGR888(char* yuv, int width, int height );

// yuv to Ip_Image
void YUV2Ip_Image(char* yuvData, Ip_Image* img);

// convert YUV to Ip_Image grey scale
void YUV2grey(char* yuvData, Ip_Image* grey);

// rotate image clockwise, only 0, 90 ,180 , 270 supported
Ip_Image* RotateImage(Ip_Image* src,int angle);

// load jpeg file to Ip_Image
Ip_Image* loadJpegFile(char* filename);

// bilinear interpolation
void BilinearInterpolation(Ip_Image* src, Ip_Image* mask);

// load png to bgra
Ip_Image* cvLoadImagePng(char* file_name,bool isRGBA = false);

// decode jpeg buffer to rgb
Ip_Image* cvLoadJpegBuffer(char* jpegBuffer, int bufferLength);
// encode Ip_Image to jpeg buffer
char* cvEncodeJpegBuffer(Ip_Image* image, int& bufferLength,int jpegQuality = 85,int srcBufferLength = (1<<22));

char* rotateYuv(char* yuvData,int width,int height);
char* rotateYuvFront(char* yuvData,int width,int height);
char* rotateYuvRevert(char* yuvData,int width,int height);

}
#endif//_IMAGE_UTIL_H_
