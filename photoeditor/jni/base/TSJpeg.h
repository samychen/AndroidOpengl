/**
 * Copyright (C) 2012 Thundersoft Corporation
 * All rights Reserved
 */

#ifndef __TSJPEG_H__
#define __TSJPEG_H__

/*jconfig.h
jpeglib.h
jmorecfg.h
jerror.h*/

// parameters needed for jpeg compression :
// jpeg.image_width = width;
// jpeg.image_height = height;
// jpeg.input_components  = 3;
// jpeg.in_color_space = JCS_RGB;
// jpeg_set_defaults(&jpeg);
// jpeg_set_quality (&jpeg, 70, true);
// return pointer to compressed jpeg buffer and length of the buffer

#define TSJPEG_COLOR_UNKONW 0
#define TSJPEG_COLOR_GRAYSCALE 1
#define TSJPEG_COLOR_RGB 2
#define TSJPEG_COLOR_YCbCr 3
#define TSJPEG_COLOR_CMYK 4
#define TSJPEG_COLOR_YCCK 5
#ifdef ANDROID_RGB
    #define TSJPEG_COLOR_RGBA8888 6
    #define TSJPEG_COLOR_RGB565 7
#endif

typedef enum enumScaleType
{
    ORIGIN_SCALE = 0, // origin size
    HALF_SCALE,       // 1/2 size
    QUARTER_SCALE,    // 1/4 size
    EIGHTH_SCALE      // 1/8 size
}ScaleType;

struct TSJpegParam
{
    int img_width;
    int img_height;
    int img_widthStep;
    int img_components;
    int color_space;
    int quality;
    ScaleType scale;

    char* srcBuffer;
    int   srcBufferSize;
    char* dstBuffer;
    int   dstBufferSize;

    int   nErrno;
    TSJpegParam()
    {
        img_width = img_height = img_widthStep = color_space = quality = 0;
        img_components = 3;
        srcBuffer = NULL;
        srcBufferSize = 0;
        dstBuffer = NULL;
        dstBufferSize = 0;
        scale = ORIGIN_SCALE;

        nErrno = 0;
    }


};
class CTSJpeg
{
public:
    static int CompressMemToMem(TSJpegParam & compressParam, const int &nDefaultSize);

    static int DeCompressMemToMem(TSJpegParam &deCompressParam);
	static int DecodeMemToMem(TSJpegParam &deCompressParam);
    static int m_nCompressDefaultSize;
private:
    CTSJpeg(){};
    ~CTSJpeg(){};
};

#endif

