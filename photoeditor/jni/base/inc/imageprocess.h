/**
 * Copyright (C) 2012 Thundersoft Corporation
 * All rights Reserved
 * code here referenced : example.c in jpeg_6b
 */

#ifndef _OPENIP_LITE_H_
#define _OPENIP_LITE_H_

#include <stddef.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

namespace imageprocess
{
/* Constants for color conversion */
enum
{
    IP_RGB2GRAY = 0,
    IP_BGR2GRAY = IP_RGB2GRAY,
    IP_GRAY2BGR,
    IP_GRAY2RGB = IP_GRAY2BGR,
    IP_YUV2RGB,
    IP_YCrCb2BGR = IP_YUV2RGB,
    IP_YCrCb2RGB = IP_YUV2RGB,
    IP_RGB2YUV,
    IP_BGR2YCrCb = IP_RGB2YUV,
    IP_RGB2YCrCb = IP_RGB2YUV,
    IP_BGR2HLS,
    IP_RGB2HSL = IP_BGR2HLS,
    IP_HLS2BGR,
    IP_HSL2RGB = IP_HLS2BGR,
    IP_RGB2HSV,
    IP_HSV2RGB
};

#define IP_PI 3.1415926
#define IP_RGB(r,g,b) ipVal(r,g,b)
#define RANGE(v,down,up) (v < down ? down : (v > up ? up : v))
#define RANGE0255(v) RANGE(v,0,255)
enum {
    IP_8UC1 = 8,
    IP_8UC3 = 8 * 3,
    IP_32FC1 = 32,
    IP_32FC3 = 32 * 3,
};

typedef struct Ip_Matrix
{
    int type;
    int step;
    union
    {
        unsigned char* ptr;
    } data;

    union
    {
        int rows;
        int height;
    };

    union
    {
        int cols;
        int width;
    };
}Ip_Matrix;
Ip_Matrix ipMatrix( int rows, int cols, int type, void* data = NULL);
Ip_Matrix* ipCreateMatrix(int rows, int cols, int type);
void ipReleaseMatrix(Ip_Matrix** mat);

typedef struct
{
    int width;
    int height;
}IpSize;

IpSize  ipSize( int width, int height );

typedef struct IpPoint
{
    int x;
    int y;
}IpPoint;
IpPoint  ipPoint( int x, int y );

typedef struct IpVal
{
    double val[4];
}IpVal;
IpVal  ipVal( double val0, double val1 = 0, double val2 = 0, double val3 = 0);

typedef struct IpRect
{
    int x;
    int y;
    int width;
    int height;
}IpRect;
IpRect  ipRect( int x, int y, int width, int height );

typedef struct _IpROI
{
    int  coi; /* 0 - no COI (all channels are selected), 1 - 0th channel is selected ...*/
    int  xOffset;
    int  yOffset;
    int  width;
    int  height;
}IpROI;
IpROI* ipCreateROI( int coi, int xOffset, int yOffset, int width, int height );

typedef struct _Ip_Image
{
    int  nChannels;
    int  depth;             /* Pixel depth in bits: IPL_DEPTH_8U, IPL_DEPTH_8S, IPL_DEPTH_16S,
                               IPL_DEPTH_32S, IPL_DEPTH_32F and IPL_DEPTH_64F are supported.  */
    int  width;             /* Image width in pixels.                           */
    int  height;            /* Image height in pixels.                          */
    struct _IpROI *roi;    /* Image ROI. If NULL, the whole image is selected. */
    int  imageSize;         /* Image data size in bytes
                               (==image->height*image->widthStep
                               in case of interleaved data)*/
    char *imageData;        /* Pointer to aligned image data.         */
    int  widthStep;         /* Size of aligned image row in bytes.    */
}Ip_Image;

Ip_Image * ipCreateImageHeader( IpSize size, int depth, int channels );
Ip_Image * ipCreateImage( IpSize size, int depth, int channels );
void ipReleaseImageHeader( Ip_Image** image );
void ipReleaseImage( Ip_Image** image );
IpSize ipGetSize(Ip_Image* image);
Ip_Image* ipDuplicateImage( Ip_Image* src );
void ipSetImageCOI( Ip_Image* image, int coi );
void ipResetImageCOI( Ip_Image* image );
int ipGetImageCOI(Ip_Image* image);
void ipSetImageROI( Ip_Image* image, IpRect rect );
void ipResetImageROI( Ip_Image* image );
IpRect ipGetImageROI(Ip_Image* image);

void ipSet(Ip_Image* image, IpVal color);
void ipCopyImage(Ip_Image* src, Ip_Image* dst);
void ipResizeImage(Ip_Image* src, Ip_Image* dst);
void ipConvertColor(Ip_Image* src, Ip_Image* dst,int mode);
void ipSetZero(Ip_Image* image);

enum {
    IP_GAUSSIAN = 0,
    IP_BLUR
};
void ipBlurImage(Ip_Image* src, Ip_Image* dst, int method, int param1 = 3, int param2 = 0);

void ipMerge(Ip_Image* src0, Ip_Image* src1, Ip_Image* src2, Ip_Image* src3, Ip_Image* dst);
void ipSplit(Ip_Image* src, Ip_Image* dst0, Ip_Image* dst1, Ip_Image* dst2, Ip_Image* dst3);

void ipSaveImage(const char* filename, Ip_Image* image, int globalJpegQuality = 90);
}
#endif//_OPENIP_LITE_H_
