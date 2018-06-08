/**
 * Copyright (C) 2012 Thundersoft Corporation
 * All rights Reserved
 */

#include "imageprocess.h"
#include "jnilogger.h"
#include "TSAlgorithm.h"

#include <stdio.h>
#include <math.h>

extern "C"{
#include "jconfig.h"
#include "jpeglib.h"
#include "jmorecfg.h"
#include "jerror.h"
}

#include "TSDl.h"
#include "CpuABI.h"

using namespace tslib;

namespace imageprocess
{
Ip_Matrix ipMatrix( int rows, int cols, int type, void* data)
{
    Ip_Matrix m;
    if(type != IP_8UC1 && type != IP_32FC1) {
        LOGE("not supported type in function %s",__FUNCTION__);
        return m;
    }
    m.type = type;
    m.cols = cols;
    m.rows = rows;
    m.step = cols * type/8;
    m.data.ptr = (unsigned char*)data;

    return m;
}
Ip_Matrix* ipCreateMatrix(int rows, int cols, int type)
{
    unsigned char* data = new unsigned char[rows * cols * type];
    Ip_Matrix* mat = new Ip_Matrix();
    mat->type = type;
    mat->cols = cols;
    mat->rows = rows;
    mat->data.ptr = data;
    mat->step = cols * type/8;
    return mat;
}

void ipReleaseMatrix(Ip_Matrix** mat)
{
    if(!mat) {
        LOGE( "mat null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return;
    }
    if( *mat ) {
        Ip_Matrix* m = *mat;
        *mat = NULL;
        if(m->data.ptr) {
            delete[] m->data.ptr;
        }
        delete m;
    }
}

IpSize  ipSize( int width, int height )
{
    IpSize s;

    s.width = width;
    s.height = height;
    return s;
}

IpPoint ipPoint( int x, int y )
{
    IpPoint p;

    p.x = x;
    p.y = y;

    return p;
}

IpVal  ipVal( double val0, double val1, double val2, double val3)
{
    IpVal scalar;
    scalar.val[0] = val0; scalar.val[1] = val1;
    scalar.val[2] = val2; scalar.val[3] = val3;
    return scalar;
};

IpRect  ipRect( int x, int y, int width, int height )
{
    IpRect r;

    r.x = x;
    r.y = y;
    r.width = width;
    r.height = height;

    return r;
}

Ip_Image * ipCreateImageHeader( IpSize size, int depth, int channels )
{
    Ip_Image *img = new Ip_Image();
    img->width = size.width;
    img->height = size.height;
    img->depth = depth;
    img->nChannels = channels;
    img->widthStep = img->width * img->depth/8 * img->nChannels;
    img->imageSize = img->widthStep * img->height;
    img->roi = NULL;
    img->imageData = NULL;
    return img;
}
Ip_Image * ipCreateImage( IpSize size, int depth, int channels )
{
    Ip_Image* img = ipCreateImageHeader(size,depth,channels);
	if(img->imageSize > 0) {
	   img->imageData = new char[img->imageSize];
	}else {
	   LOGE( "image null pointer in function %s ,in file %s, line %d, size %d",__FUNCTION__,__FILE__,__LINE__, img->imageSize);
	}
    return img;
}

void ipReleaseImageHeader( Ip_Image** image )
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return;
    }
    if( *image )
    {
        Ip_Image* img = *image;
        *image = 0;

        if(img->roi) {
            delete img->roi;
        }
        delete img;
    }
}

void ipReleaseImage( Ip_Image ** image )
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return ;
    }
    if( *image )
    {
        Ip_Image* img = *image;
        *image = 0;

        if(img->imageData) {
            delete[] img->imageData;
        }
        ipReleaseImageHeader( &img );
    }
}

IpSize ipGetSize(Ip_Image* image)
{
    return ipSize(image->width,image->height);
}
IpROI* ipCreateROI( int coi, int xOffset, int yOffset, int width, int height )
{
    IpROI* roi = new IpROI();
    roi->coi = coi;
    roi->xOffset = xOffset;
    roi->yOffset = yOffset;
    roi->width = width;
    roi->height = height;
    return roi;
}

Ip_Image* ipDuplicateImage(Ip_Image* src )
{
    if( !src ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return NULL;
    }
    Ip_Image* dst = NULL;
    dst = ipCreateImageHeader(ipGetSize(src),src->depth,src->nChannels);
    if(src->roi){
        IpROI* roi = src->roi;
        dst->roi = ipCreateROI(roi->coi,roi->xOffset,roi->yOffset, roi->width, roi->height);
        // memcpy
    }

    if(src->imageData){
        dst->imageData = new char[src->imageSize];
        memcpy(dst->imageData,src->imageData,src->imageSize);
    }
    return dst;
}

void ipSet(Ip_Image* image, IpVal color)
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return ;
    }
    IpRect srcRect = ipGetImageROI(image);
    unsigned char* psrc = (unsigned char*)image->imageData + srcRect.y* image->widthStep + srcRect.x * image->nChannels;
    for (int h = 0; h < srcRect.height; h ++) {
        unsigned char* ps = psrc;
        for (int w = 0; w < srcRect.width; w ++) {
            for(int ch =0; ch < image->nChannels; ch ++) {
                ps[ch] = color.val[ch];
            }
            ps += image->nChannels;
        }
        psrc += image->widthStep;
    }
}

void ipSetImageROI( Ip_Image* image, IpRect rect )
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return ;
    }
    // allow zero ROI width or height
    if( rect.width >= 0 && rect.height >= 0 &&
               rect.x < image->width && rect.y < image->height &&
               rect.x + rect.width >= (int)(rect.width > 0) &&
               rect.y + rect.height >= (int)(rect.height > 0) ) {
    }else {
        LOGE("roi size not proper");
        return;
    };

    rect.width += rect.x;
    rect.height += rect.y;

    rect.x = max(rect.x, 0);
    rect.y = max(rect.y, 0);
    rect.width = min(rect.width, image->width);
    rect.height = min(rect.height, image->height);

    rect.width -= rect.x;
    rect.height -= rect.y;

    if( image->roi ) {
        image->roi->xOffset = rect.x;
        image->roi->yOffset = rect.y;
        image->roi->width = rect.width;
        image->roi->height = rect.height;
    } else {
        image->roi = ipCreateROI( 0, rect.x, rect.y, rect.width, rect.height );
    }
}
void ipResetImageROI( Ip_Image* image )
{
    if(image->roi){
        delete image->roi;
        image->roi = NULL;
    }
}

IpRect ipGetImageROI( Ip_Image* image)
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return ipRect(0,0,0,0);
    }
    if(image->roi) {
        IpROI* roi= image->roi;
        return ipRect(roi->xOffset,roi->yOffset,roi->width,roi->height);
    }
    return ipRect(0,0,image->width,image->height);
}

void ipSetImageCOI( Ip_Image* image, int coi )
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return;
    }
    if( (unsigned)coi > (unsigned)(image->nChannels) ){
        LOGE( "bad input image coi in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return;
    }

    if( image->roi || coi != 0 )
    {
        if( image->roi )
        {
            image->roi->coi = coi;
        }
        else
        {
            image->roi = ipCreateROI( coi, 0, 0, image->width, image->height );
        }
    }
}

int ipGetImageCOI(Ip_Image* image)
{
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return 0;
    }
    if(image->roi) {
        return image->roi->coi;
    }
    return 0;
}

void ipCopyImage(Ip_Image* src, Ip_Image* dst)
{
    if( !src || !dst) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return ;
    }
    IpRect srcRect = ipGetImageROI(src);
    IpRect dstRect = ipGetImageROI(dst);
    if(srcRect.width != dstRect.width || srcRect.height != dstRect.height) {
        LOGE( "roi not same size in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return ;
    }
    // selected channels number should be same
    int srcCOI = ipGetImageCOI(src);
    int dstCOI = ipGetImageCOI(dst);

    int srcChannel = srcCOI ? 1 : src->nChannels;
    int dstChannel = dstCOI ? 1 : dst->nChannels;
    if(srcChannel != dstChannel) {
        LOGE("valid channel not same");
        return;
    }

    char* psrc = src->imageData + srcRect.y * src->widthStep + srcRect.x * src->nChannels;
    char* pdst = dst->imageData + dstRect.y * dst->widthStep + dstRect.x * dst->nChannels;

    int roiwidth = srcRect.width * src->nChannels;
    int offsetPixelSrc = srcCOI;
    int offsetPixelDst = dstCOI;
    if(srcCOI || dstCOI || src->nChannels!=dst->nChannels) {
        roiwidth = 1;
    }
    pdst += offsetPixelDst;
    psrc += offsetPixelSrc;
    if(roiwidth == 1) {
        for(int h =0; h < srcRect.height; h ++){
            for(int w=0; w< srcRect.width; w ++){
                memcpy(pdst,psrc,roiwidth);
                psrc += src->nChannels;
                pdst += dst->nChannels;
            }
        }
    }else {
        for(int h =0; h < srcRect.height; h ++){
            memcpy(pdst,psrc,roiwidth);
            psrc += src->widthStep;
            pdst += dst->widthStep;
        }
    }
}

void ipResizeImage(Ip_Image* src, Ip_Image* dst)
{
    float xscale = (float)src->width / dst->width;
    float yscale = (float)src->height / dst->height;

    unsigned char* psrc = (unsigned char*)src->imageData;
    unsigned char* pdst = (unsigned char*)dst->imageData;
    for (int y = 0; y < dst->height; y++)
    {
       for (int x = 0; x < dst->width; x++)
       {
          float sx = x * xscale;
          float sy = y * yscale;
          int x0 = (int)sx;
          int y0 = (int)sy;

          // Calculate coordinates of the 4 interpolation points
          float fracx = sx - x0;
          float fracy = sy - y0;
          float ifracx = 1 - fracx;
          float ifracy = 1 - fracy;
          int x1 = x0 + 1;
          if (x1 >= src->width)
          {
             x1 = x0;
          }
          int y1 = y0 + 1;
          if (y1 >= src->height)
          {
             y1 = y0;
          }

          int yw0 = y0 * src->widthStep;
          int yw1 = y1 * src->widthStep;
          int step1 = x0 * src->nChannels;
          int step2 = x1 * src->nChannels;
          for(int ch =0 ; ch < src->nChannels; ch++){
              // Read source color
              float c1r = psrc[ yw0 + step1 + ch];
              float c2r = psrc[ yw0 + step2 + ch];

              float c3r = psrc[ yw1 + step1 + ch];
              float c4r = psrc[ yw1 + step2 + ch];

              // Calculate colors
              // Red
              float l0 = ifracx * c1r + fracx * c2r;
              float l1 = ifracx * c3r + fracx * c4r;
              float rf = ifracy * l0 + fracy * l1;

              // Write destination
              *pdst++ = rf;
          }
       }
    }
}


static float min3f(float v1, float v2, float v3)
{
    return ((v1)>(v2)? ((v2)>(v3)?(v3):(v2)):((v1)>(v3)?(v3):(v1)));
}

static float max3f(float v1, float v2, float v3)
{
    return ((v1)<(v2)? ((v2)<(v3)?(v3):(v2)):((v1)<(v3)?(v3):(v1)));
}
static void cvRgb2Gray(unsigned char* psrc, unsigned char* pdst)
{
    *pdst = (psrc[0] >> 2) + (psrc[1] >> 1) + (psrc[2] >> 2);
}
static void cvGray2Rgb(unsigned char* psrc, unsigned char* pdst)
{
    pdst[0] = *psrc;
    pdst[1] = *psrc;
    pdst[2] = *psrc;
}
static void cvRgb2Yuv(unsigned char* psrc, unsigned char* pdst)
{
    unsigned char c0 = psrc[0];
    unsigned char c1 = psrc[1];
    unsigned char c2 = psrc[2];
    pdst[0] = RANGE0255((c0 * 0.257 ) + (c1 * 0.504) + (c2 * 0.098) + 16);
    pdst[2] = RANGE0255((c0 * 0.439) - (c1 * 0.368) - (c2 * 0.071) + 128);
    pdst[1] = RANGE0255(- (c0 * 0.148) - (c1 * 0.291) + (c2 * 0.439) + 128);
}

static void cvRgb2Hsv(unsigned char* psrc, unsigned char* pdst)
{
    double R = psrc[0]/255.0;
    double G = psrc[1] / 255.0;
    double B = psrc[2] / 255.0;
    double value = max3f(R, G, B);
    double saturation = value ==0 ? 0 :( 1.0 - min3f(R,G,B)/value);
    double hue = 0;
    if (value == R)
    {
        hue = 60 * (G - B)/saturation;
    }
    else if (value == G)
    {
        hue = 120 + 60 * (B - R) / saturation;
    }
    else
    {
        hue = 240 + 60 * (R - G ) / saturation;
    }
    if (hue < 0)
    {
        hue += 360;
    }
    pdst[0] = hue/2;
    pdst[1] = saturation * 255;
    pdst[2] = value * 255;
}

static void cvHsv2Rgb(unsigned char* psrc, unsigned char* pdst)
{
    int hue = psrc[0] * 2;
    int saturation = psrc[1];
    double value = psrc[2];

    double tmp = hue / 60.0;
    int hi = (int)floor(tmp)%6;
    double f = tmp - floor(tmp);
    double p = value * (255 - saturation)/255;
    double q = value * (255 - f * saturation)/255;
    double t = value * (255 - (1 - f) * saturation)/255;
    double rgb[6][3] = {
        {value,t,p},
        {q,value,p},
        {p,value,t},
        {p,q,value},
        {t,p,value},
        {value,p,q}
    };
    pdst[0] = rgb[hi][0];
    pdst[1] = rgb[hi][1];
    pdst[2] = rgb[hi][2];
}

static void cvYuv2Rgb(unsigned char* src, unsigned char* dst)
{
    unsigned char c0 = src[0];
    unsigned char c1 = src[1];
    unsigned char c2 = src[2];
    double y1 = 1.164 * (c0 - 16);
    dst[2] = RANGE0255(y1 + 2.018 * (c1 - 128));
    dst[1] = RANGE0255(y1 - 0.813 * (c2 - 128) - 0.391 *(c1 - 128));
    dst[0] = RANGE0255(y1 + 1.596 * (c2 - 128));
}

static void cvRgb2Hsl(unsigned char* src, unsigned char* dst)
{
    float h = 0, s = 0, l = 0;
    // normalizes red-green-blue values
    float r = 1.0f * src[0] / 255;
    float g = 1.0f * src[1] / 255;
    float b = 1.0f * src[2] / 255;

    float maxVal = max3f(r, g, b);
    float minVal = min3f(r, g, b);

    if (maxVal == minVal)
    {
        h = 0;
    }
    else if (maxVal == r)
    {
        h = 60.0f * (g - b) / (maxVal - minVal);
    }
    else if (maxVal == g)
    {
        h = 60.0f * (b - r) / (maxVal - minVal) + 120.0f;
    }
    else if (maxVal == b)
    {
        h = 60.0f * (r - g) / (maxVal - minVal) + 240.0f;
    }

    if (h < 0) h += 360;

    l = (maxVal + minVal) / 2.0f;

    // saturation
    if (maxVal == minVal)
    {
        s = 0;
    }
    else if (l < 0.5f)
    {
        s = (maxVal - minVal) / (maxVal + minVal);
    }
    else if (l >= 0.5f)
    {
        s = (maxVal - minVal) / (2 - maxVal - minVal);
    }

    dst[0] = RANGE0255(((h > 360) ? 360 : ((h < 0) ? 0 : h)) / 2);
    dst[1] = RANGE0255(((s > 1) ? 1 : ((s < 0) ? 0 : s)) * 255);
    dst[2] = RANGE0255(((l > 1) ? 1 : ((l < 0) ? 0 : l)) * 255);
}

static void cvHsl2Rgb(unsigned char* src, unsigned char* dst)
{
    float h = 1.0f * src[0] * 2;
    float s = 1.0f * src[1] / 255;
    float l = 1.0f * src[2] / 255;


    float r, g, b;
    if (src[1] == 0)
    {
        r = g = b = l * 255.0f;
    }
    else
    {
        float q = (l < 0.5f) ? (l * (1.0f + s)) : (l + s - (l * s));
        float p = (2.0f * l) - q;
        float Hk = h / 360.0f;
        float T[3];
        T[0] = Hk + 0.3333333f;
        T[1] = Hk;
        T[2] = Hk - 0.3333333f;

        for (int i = 0; i < 3; i++)
        {
            if (T[i] < 0) T[i] += 1.0f;
            if (T[i] > 1) T[i] -= 1.0f;
            if ((T[i] * 6) < 1)
            {
                T[i] = p + ((q - p) * 6.0f * T[i]);
            }
            else if ((T[i] * 2.0f) < 1)
            {
                T[i] = q;
            }
            else if ((T[i] * 3.0f) < 2)  // 0.5<=T[i] && T[i]<(2.0/3.0)
            {
                T[i] = p + (q - p) * ((2.0f / 3.0f) - T[i]) * 6.0f;
            }
            else
            {
                T[i] = p;
            }
        }

        r = T[0] * 255.0f;
        g = T[1] * 255.0f;
        b = T[2] * 255.0f;
    }

    dst[0] = RANGE0255(r);
    dst[1] = RANGE0255(g);
    dst[2] = RANGE0255(b);
}

void ipConvertColor(Ip_Image* src, Ip_Image* dst,int mode)
{
    void (*pconvertor)(unsigned char*, unsigned char*) = NULL;

    switch(mode) {
        case IP_GRAY2RGB:
            pconvertor = cvGray2Rgb;
            break;
        case IP_RGB2GRAY:
            pconvertor = cvRgb2Gray;
            break;
        case IP_YUV2RGB:
            pconvertor = cvYuv2Rgb;
            break;
        case IP_RGB2YUV:
            pconvertor = cvRgb2Yuv;
            break;
        case IP_RGB2HSL:
            pconvertor = cvRgb2Hsl;
            break;
        case IP_HSL2RGB:
            pconvertor = cvHsl2Rgb;
            break;
        case IP_RGB2HSV:
            pconvertor = cvRgb2Hsv;
            break;
        case IP_HSV2RGB:
            pconvertor = cvHsv2Rgb;
            break;
        default:
            break;
    }
    if( !pconvertor ) {
        return ;
    }
    IpRect srcRect = ipGetImageROI(src);
    IpRect dstRect = ipGetImageROI(dst);
    unsigned char* psrc = (unsigned char*)src->imageData + srcRect.y* src->widthStep + srcRect.x * src->nChannels;
    unsigned char* pdst = (unsigned char*)dst->imageData + dstRect.y* dst->widthStep + dstRect.x * dst->nChannels;;
    for (int h = 0; h < srcRect.height; h ++) {
        unsigned char* ps = psrc;
        unsigned char* pd = pdst;
        for (int w = 0; w < srcRect.width; w ++) {
            pconvertor(ps,pd);
            ps += src->nChannels;
            pd += dst->nChannels;
        }
        psrc += src->widthStep;
        pdst += dst->widthStep;
    }
}

void ipSetZero(Ip_Image* image)
{
    memset(image->imageData,0,image->imageSize);
}


static double* getGaussianKernel(int diameter)
{
    const int SMALL_GAUSSIAN_SIZE = 7;
    double small_gaussian_tab [4][SMALL_GAUSSIAN_SIZE] = {
        {1.0f,0,0,0,0,0,0},
        {0.25f, 0.5f, 0.25f,0,0,0,0},
        {0.0625f, 0.25f, 0.375f, 0.25f, 0.0625f,0,0},
        {0.03125f, 0.109375f, 0.21875f, 0.28125f, 0.21875f, 0.109375f, 0.03125f}
    };
    double sigma = 0;
    double* fixed_kernel = diameter % 2 == 1 && diameter <= SMALL_GAUSSIAN_SIZE
        && sigma <= 0 ? small_gaussian_tab[diameter>>1] : NULL;

    double* kernel = new double[diameter];
    double* cf = kernel;
    double sigmaX = sigma > 0 ? sigma : ((diameter-1)*0.5 - 1)*0.3 + 0.8;
    double scale2X = -0.5/(sigmaX*sigmaX);
    double sum = 0;

    int i;
    for( i = 0; i < diameter; i++ )
    {
        double x = i - (diameter-1)*0.5;
        double t = fixed_kernel!=NULL ? (double)fixed_kernel[i] : exp(scale2X*x*x);
        cf[i] = t;
        sum += cf[i];
    }

    sum = 1.0/sum;
    for( i = 0; i < diameter; i++ )
    {
        cf[i] = cf[i]*sum;
    }

    return kernel;
}

// guassin smooth with length
static void ipBlurImageGaussian(Ip_Image* src,Ip_Image* _dst, int diameter)
{
    double* kernel = getGaussianKernel(diameter);
    if (diameter <= 1 || (diameter % 2 != 0) || diameter >= src->height || diameter >= src->width)
    {
        return;
    }
    Ip_Image* dst = _dst;
    if(src == _dst) {
        dst = ipCreateImage(ipGetSize(src),8,src->nChannels);
    }
    int radius = diameter / 2;
    unsigned char* psrc = (unsigned char*)src->imageData;
    unsigned char* pdst = (unsigned char*)dst->imageData;

    // row filter
    for (int h = 0; h < src->height - diameter; h++)
    {
        unsigned char* psrcline = psrc;
        unsigned char* pdstline = pdst + radius * dst->nChannels;
        for (int w = 0; w < src->width - diameter; w++)
        {
            unsigned char* color = psrcline;
            double rgb[3] = { 0, 0, 0 };
            for (int i = 0; i < diameter; i++)
            {
                for(int ch = 0; ch < src->nChannels; ch ++){
                    rgb[ch] += kernel[i] * color[ch];
                }
                color += src->nChannels;
            }
            for(int ch = 0; ch < src->nChannels; ch ++){
                pdstline[ch] = rgb[ch];
            }

            psrcline += src->nChannels;
            pdstline += dst->nChannels;
        }
        psrc += src->widthStep;
        pdst += dst->widthStep;
    }
    Ip_Image* tmp = ipDuplicateImage(dst);
    psrc = (unsigned char*)tmp->imageData;
    pdst = (unsigned char*)dst->imageData;
    // col filter
    for (int w = 0; w < src->width - diameter; w++)
    {
        unsigned char* psrcline = psrc;
        unsigned char* pdstline = pdst + radius * dst->widthStep;
        for (int h = 0; h < src->height - diameter; h++)
        {
            unsigned char* color = psrcline;
            double rgb[3] = { 0, 0, 0 };
            for (int i = 0; i < diameter; i++)
            {
                for(int ch = 0; ch < src->nChannels; ch ++){
                    rgb[ch] += kernel[i] * color[ch];
                }
                color += src->widthStep;
            }
            for(int ch = 0; ch < src->nChannels; ch ++){
                pdstline[ch] = rgb[ch];
            }

            psrcline += src->widthStep;
            pdstline += dst->widthStep;
        }
        psrc += src->nChannels;
        pdst += dst->nChannels;
    }
    ipReleaseImage(&tmp);
    delete[] kernel;

    if(dst != _dst) {
        ipCopyImage(dst, _dst);
        ipReleaseImage(&dst);
    }
}
void ipBlurImage(Ip_Image* src, Ip_Image* dst, int method, int param1 , int param2 )
{
    if(method == IP_GAUSSIAN) {
        ipBlurImageGaussian(src,dst,param1);
    } else if(method == IP_BLUR) {
        ipBlurImageGaussian(src,dst,param1);
    }else {
        LOGE("method of ipBlurImage not support now");
    }
}

// only fisrt channel of src in used
void ipMerge(Ip_Image* src0, Ip_Image* src1, Ip_Image* src2, Ip_Image* src3, Ip_Image* dst)
{
    Ip_Image* srcs[4] = {src0, src1, src2, src3};
    char* psrc[4];
    int n =0;
    for(int i =0; i < 4; i++) {
        n = i;
        if(!srcs[i]) {
            break;
        }
        psrc[i] = srcs[i]->imageData;
    }
    n = min(n, dst->nChannels);
    int size = dst->width * dst->height;
    char* pd = dst->imageData;
    for(int i = 0 ; i < size; i++) {
        for(int ch =0; ch < n ; ch++){
            pd[ch] = psrc[ch][0];
            psrc[ch] += srcs[ch]->nChannels;
        }
        pd += dst->nChannels;
    }
}

void ipSplit(Ip_Image* src, Ip_Image* dst0, Ip_Image* dst1, Ip_Image* dst2, Ip_Image* dst3)
{
    Ip_Image* dsts[4] = {dst0, dst1, dst2, dst3};
    char* pdst[4];
    int n = src->nChannels;
    for(int i =0; i < 4; i++) {
        if(dsts[i]) {
            pdst[i] = dsts[i]->imageData;
        }else {
            pdst[i] = NULL;
        }
    }

    int size = src->width * src->height;
    char* ps = src->imageData;
    for(int i = 0 ; i < size; i++) {
        for(int ch =0; ch < n ; ch++){
            if(pdst[ch]){
                pdst[ch][0] = ps[ch];
                pdst[ch] += dsts[ch]->nChannels;
            }
        }
        ps += src->nChannels;
    }
}

// only rgb mode is supported now
void ipSaveImage(const char * filename, Ip_Image* _image, int quality)
{
	TSDl dl;
	bool loadSucc=false;
//	if(!CpuABI::FeatrueContainsNeon())
//	{
//		loadSucc=dl.LoadDl(SYS_LIBJPEG);
//	}

    Ip_Image* image = _image;
    if( !image ) {
        LOGE( "image null pointer in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
        return;
    }
   J_COLOR_SPACE colorspace;
    if(image -> nChannels == 3) {
        colorspace = JCS_RGB;
    }else if(image-> nChannels == 1){
        colorspace = JCS_GRAYSCALE;
    }else if(image-> nChannels == 4){
        colorspace = JCS_RGB;
        Ip_Image* tmp = ipCreateImage(ipGetSize(image),8,3);
        for(int h =0; h < tmp->height; h++) {
            char* pd = tmp->imageData + tmp->widthStep * h;
            char* ps = image->imageData + image->widthStep * h;
            for(int w=0; w < tmp->width; w++) {
                memcpy(pd,ps,3);
                pd += 3;
                ps += 4;
            }
        }
        image = tmp;
    }else {
        LOGE( "image color channel not support yet. in function %s ,in file %s, line %d",__FUNCTION__,__FILE__,__LINE__ );
    }
    struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;

    FILE * outfile;        /* target file */
    JSAMPROW row_pointer[1];    /* pointer to JSAMPLE row[s] */
    int row_stride;        /* physical row width in image buffer */
    JSAMPLE * image_buffer = (JSAMPLE *)image->imageData;

    struct jpeg_error_mgr * (*std_error_fun_ptr)(struct jpeg_error_mgr * err);
    if(loadSucc)
    {
    	std_error_fun_ptr=dl.GetFuncPtr("jpeg_std_error");
    	if(NULL!=std_error_fun_ptr)
    	{
    		cinfo.err = (*std_error_fun_ptr)(&jerr);
    	}
    	else
    	{
    		cinfo.err = jpeg_std_error(&jerr);
    	}
    }
    else
    {
    	cinfo.err = jpeg_std_error(&jerr);
    }

    void (*createCompress_fun_ptr)(j_compress_ptr,int, size_t);
	if(loadSucc)
	{
		createCompress_fun_ptr=dl.GetFuncPtr("jpeg_CreateCompress");
		if(NULL!=createCompress_fun_ptr)
		{
			(*createCompress_fun_ptr)(&cinfo,JPEG_LIB_VERSION,(size_t)sizeof(struct jpeg_compress_struct));
		}
		else
		{
			 jpeg_create_compress(&cinfo);
		}
	}
	else
	{
		 jpeg_create_compress(&cinfo);
	}

    if ((outfile = fopen(filename, "wb")) == NULL) {
        LOGE("can't open file : %s to rewrite image, in file %s, line %d", filename,__FILE__,__LINE__);
        return;
    }

    void (*stdio_dst_fun_ptr)(j_compress_ptr,FILE *);
    if(loadSucc)
    {
    	stdio_dst_fun_ptr=dl.GetFuncPtr("jpeg_stdio_dest");
    	if(NULL!=stdio_dst_fun_ptr)
    	{
    		(*stdio_dst_fun_ptr)(&cinfo, outfile);
    	}
    	else
    	{
    		jpeg_stdio_dest(&cinfo, outfile);
    	}
    }
    else
    {
    	jpeg_stdio_dest(&cinfo, outfile);
    }

    cinfo.image_width = image->width;     /* image width and height, in psrc */
    cinfo.image_height = image->height;
    cinfo.input_components = image -> nChannels;        /* # of color components per pixel */
    cinfo.in_color_space = colorspace;     /* colorspace of input image */

    void (*set_defaults_fun_ptr)(j_compress_ptr);
    if(loadSucc)
    {
		set_defaults_fun_ptr=dl.GetFuncPtr("jpeg_set_defaults");
		if(NULL!=set_defaults_fun_ptr)
		{
			(*set_defaults_fun_ptr)(&cinfo);
		}
		else
		{
			jpeg_set_defaults(&cinfo);
		}
    }
    else
    {
	   jpeg_set_defaults(&cinfo);
    }

    void (*set_quality_fun_ptr)(j_compress_ptr,int,boolean);
    if(loadSucc)
    {
    	set_quality_fun_ptr=dl.GetFuncPtr("jpeg_set_quality");
    	if(NULL!=set_quality_fun_ptr)
    	{
    		(*set_quality_fun_ptr)(&cinfo, quality, TRUE /* limit to baseline-JPEG values */);
    	}
    	else
    	{
    		jpeg_set_quality(&cinfo, quality, TRUE /* limit to baseline-JPEG values */);
    	}
    }
    else
    {
        jpeg_set_quality(&cinfo, quality, TRUE /* limit to baseline-JPEG values */);
    }

    void (*start_compress_fun_ptr)(j_compress_ptr,boolean);
    if(loadSucc)
    {
    	start_compress_fun_ptr=dl.GetFuncPtr("jpeg_start_compress");
		if(NULL!=start_compress_fun_ptr)
		{
			(*start_compress_fun_ptr)(&cinfo,TRUE);
		}
		else
		{
			jpeg_start_compress(&cinfo,TRUE);
		}
    }
    else
    {
	   jpeg_start_compress(&cinfo, TRUE);
    }

    JDIMENSION (*write_scanlines_fun_ptr)(j_compress_ptr,JSAMPARRAY,JDIMENSION);
	if(loadSucc)
	{
		write_scanlines_fun_ptr=dl.GetFuncPtr("jpeg_write_scanlines");
	}
	else
	{
		write_scanlines_fun_ptr=NULL;
	}

    row_stride = image->widthStep;    /* JSAMPLEs per row in image_buffer */

    while (cinfo.next_scanline < cinfo.image_height) {
        row_pointer[0] = & image_buffer[cinfo.next_scanline * row_stride];
        if(NULL!=write_scanlines_fun_ptr)
        {
        	(*write_scanlines_fun_ptr)(&cinfo, row_pointer, 1);
        }
        else
        {
        	(void) jpeg_write_scanlines(&cinfo, row_pointer, 1);
        }
    }

    void (*finish_compress_fun_ptr)(j_compress_ptr);
	if(loadSucc)
	{
		finish_compress_fun_ptr=dl.GetFuncPtr("jpeg_finish_compress");
		if(NULL!=finish_compress_fun_ptr){
			(*finish_compress_fun_ptr)(&cinfo);
		}
		else
		{
			jpeg_finish_compress(&cinfo);
		}
	}
	else
	{
		jpeg_finish_compress(&cinfo);
	}

    fclose(outfile);

    void (*destroy_compress_fun_ptr)(j_compress_ptr);
	if(loadSucc)
	{
		destroy_compress_fun_ptr=dl.GetFuncPtr("jpeg_destroy_compress");
		if(NULL!=destroy_compress_fun_ptr)
		{
			(*destroy_compress_fun_ptr)(&cinfo);
		}
		else
		{
			jpeg_destroy_compress(&cinfo);
		}
	}
	else
	{
		jpeg_destroy_compress(&cinfo);
	}

    if(image != _image) {
        ipReleaseImage(&image);
    }
}


}
