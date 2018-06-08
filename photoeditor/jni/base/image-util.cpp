/**
 * Copyright (C) 2011 Thundersoft Corporation
 * All rights Reserved
 */
// conversion from yuv to rgb is stolen from android-2.3 framework/media/libstagefright/colorconversion

#include "image-util.h"
#include "jnilogger.h"
#include "TSJpeg.h"
#include <png.h>

namespace imageUtil {

#ifndef uint32_t
#define uint32_t unsigned int
#endif
#ifndef uint8_t
#define uint8_t unsigned char
#endif
#ifndef size_t
#define size_t int
#endif

// yuv to argb_8888
// conversion part is stolen from android-2.3 framework/media/libstagefright/colorconversion
int* YUV2BGR888(char* yuv, int width, int height)
{
    int imageSize= width*height;
    int* dstBits = new int[imageSize];

    // table
    static const signed kClipMin = -278;
    static const signed kClipMax = 535;

    uint8_t* mClip = new uint8_t[kClipMax - kClipMin + 1];
    for (signed i = kClipMin; i <= kClipMax; ++i) {
        mClip[i - kClipMin] = (i < 0) ? 0 : (i > 255) ? 255 : (uint8_t)i;
    }
    uint8_t *kAdjustedClip = &mClip[-kClipMin];

    //uint32_t *dst_ptr = (uint32_t *)dstBits ;
    unsigned char* pimg = (unsigned char*)dstBits;

    const uint8_t *src_y = (const uint8_t *)yuv;
    const uint8_t *src_u =(const uint8_t *)src_y + imageSize;

    for (size_t y = 0; y < height; ++y) {
        for (size_t x = 0; x < width; x += 2) {
            signed y1 = (signed)src_y[x] - 16;
            signed y2 = (signed)src_y[x + 1] - 16;

            signed v = (signed)src_u[x & ~1] - 128;
            signed u = (signed)src_u[(x & ~1) + 1] - 128;

            signed u_b = u * 517;
            signed u_g = -u * 100;
            signed v_g = -v * 208;
            signed v_r = v * 409;

            signed tmp1 = y1 * 298;
            signed b1 = (tmp1 + u_b) / 256;
            signed g1 = (tmp1 + v_g + u_g) / 256;
            signed r1 = (tmp1 + v_r) / 256;

            signed tmp2 = y2 * 298;
            signed b2 = (tmp2 + u_b) / 256;
            signed g2 = (tmp2 + v_g + u_g) / 256;
            signed r2 = (tmp2 + v_r) / 256;

            *pimg++ = kAdjustedClip[b1] ;
            *pimg++ = kAdjustedClip[g1] ;
            *pimg++ = kAdjustedClip[r1] ;
            *pimg++ = 255 ;
            *pimg++ = kAdjustedClip[b2] ;
            *pimg++ = kAdjustedClip[g2] ;
            *pimg++ = kAdjustedClip[r2] ;
            *pimg++ = 255 ;
        }

        src_y += width;

        if (y & 1) {
            src_u += width;
        }
    }

    delete[] mClip;
    return dstBits;
}

// yuv to Ip_Image
// conversion part is stolen from android-2.3 framework/media/libstagefright/colorconversion
void YUV2Ip_Image(char* yuv, Ip_Image* img)
{
    int width = img->width;
    int height = img->height;

    // table
    static const signed kClipMin = -278;
    static const signed kClipMax = 535;

    uint8_t* mClip = new uint8_t[kClipMax - kClipMin + 1];
    for (signed i = kClipMin; i <= kClipMax; ++i) {
        mClip[i - kClipMin] = (i < 0) ? 0 : (i > 255) ? 255 : (uint8_t)i;
    }
    uint8_t *kAdjustedClip = &mClip[-kClipMin];

    const uint8_t *src_y = (const uint8_t *)yuv;
    const uint8_t *src_u =(const uint8_t *)src_y + width*height;

    if(img->nChannels != 4){
        for (size_t y = 0; y < height; ++y) {
            unsigned char* pimg = (unsigned char*)img->imageData + y * img->widthStep;
            for (size_t x = 0; x < width; x += 2) {
                signed y1 = (signed)src_y[x] - 16;
                signed y2 = (signed)src_y[x + 1] - 16;

                signed v = (signed)src_u[x & ~1] - 128;
                signed u = (signed)src_u[(x & ~1) + 1] - 128;

                signed u_b = u * 517;
                signed u_g = -u * 100;
                signed v_g = -v * 208;
                signed v_r = v * 409;

                signed tmp1 = y1 * 298;
                signed b1 = (tmp1 + u_b) / 256;
                signed g1 = (tmp1 + v_g + u_g) / 256;
                signed r1 = (tmp1 + v_r) / 256;

                signed tmp2 = y2 * 298;
                signed b2 = (tmp2 + u_b) / 256;
                signed g2 = (tmp2 + v_g + u_g) / 256;
                signed r2 = (tmp2 + v_r) / 256;

                *pimg++ = kAdjustedClip[r1] ;
                *pimg++ = kAdjustedClip[g1] ;
                *pimg++ = kAdjustedClip[b1] ;
                *pimg++ = kAdjustedClip[r2] ;
                *pimg++ = kAdjustedClip[g2] ;
                *pimg++ = kAdjustedClip[b2] ;
            }

            src_y += width;

            if (y & 1) {
                src_u += width;
            }

        }
    }else{
        for (size_t y = 0; y < height; ++y) {
            unsigned char* pimg = (unsigned char*)img->imageData + y * img->widthStep;
            for (size_t x = 0; x < width; x += 2) {
                signed y1 = (signed)src_y[x] - 16;
                signed y2 = (signed)src_y[x + 1] - 16;

                signed v = (signed)src_u[x & ~1] - 128;
                signed u = (signed)src_u[(x & ~1) + 1] - 128;

                signed u_b = u * 517;
                signed u_g = -u * 100;
                signed v_g = -v * 208;
                signed v_r = v * 409;

                signed tmp1 = y1 * 298;
                signed b1 = (tmp1 + u_b) / 256;
                signed g1 = (tmp1 + v_g + u_g) / 256;
                signed r1 = (tmp1 + v_r) / 256;

                signed tmp2 = y2 * 298;
                signed b2 = (tmp2 + u_b) / 256;
                signed g2 = (tmp2 + v_g + u_g) / 256;
                signed r2 = (tmp2 + v_r) / 256;

                *pimg++ = kAdjustedClip[r1] ;
                *pimg++ = kAdjustedClip[g1] ;
                *pimg++ = kAdjustedClip[b1] ;
                *pimg++ = 255 ;
                *pimg++ = kAdjustedClip[r2] ;
                *pimg++ = kAdjustedClip[g2] ;
                *pimg++ = kAdjustedClip[b2] ;
                *pimg++ = 255 ;
            }

            src_y += width;

            if (y & 1) {
                src_u += width;
            }

        }

    }
    delete[] mClip;
}

// convert YUV to Ip_Image grey scale
void YUV2grey(char* yuvData, Ip_Image* grey)
{
    if(grey->width % 4){
        int len = grey->width * grey->height;
        char* p = grey->imageData;
        for(int h = 0 ; h < grey->height ; h++){
            memcpy(p, yuvData, len);
            p += grey->widthStep;
            yuvData += len;
        }
    }else {
        memcpy(grey->imageData, yuvData, grey->width * grey->height); // width should be even
    }
}

// rotate image clockwise, only 0, 90 ,180 , 270 supported
Ip_Image* RotateImage(Ip_Image* src,int angle)
{
    LOGD("RotateImage enter");
    if(angle < 0) {
        angle += 360;
    }else if (angle >= 360){
        angle %= 360;
    }
    // rotate
    Ip_Image* rotatedImg = NULL ;

    int channel = src->nChannels;
    if(angle == 0){
        rotatedImg = ipDuplicateImage(src);
    }else if (angle == 90){// 90
        rotatedImg = ipCreateImage(ipSize(src->height, src->width), 8, channel);
        rotatedImg->height = src->width;
        rotatedImg->width = src->height;
        rotatedImg->widthStep = channel*rotatedImg->width;
        unsigned char* psrc = (unsigned char*)src->imageData;
        unsigned char* pdst = (unsigned char*)rotatedImg->imageData;
        for(int h = 0; h < rotatedImg->height; h++){
            unsigned char* pd = pdst;
            pdst += rotatedImg->widthStep;
            for(int w = 0; w < rotatedImg->width; w++){
                unsigned char * ps = psrc + (src->height - w - 1) * src->widthStep + h * channel;
                for(int ch = 0; ch < channel; ch ++){
                    *pd++ = *ps++;
                }
            }
        }
    }else if (angle == 180){// 180
        rotatedImg = ipCreateImage(ipGetSize(src), 8, channel);
        unsigned char* psrc = (unsigned char*)src->imageData + src->imageSize-(src->widthStep - src->width*src->nChannels);
        unsigned char* pdst = (unsigned char*)rotatedImg->imageData;
        for(int h = 0; h < rotatedImg->height; h++){
            unsigned char* pd = pdst;
            unsigned char* ps = psrc;
            psrc -= src->widthStep;
            pdst += rotatedImg->widthStep;
            for(int w = 0; w < rotatedImg->width; w++){
                ps -= channel;
                memcpy(pd, ps, channel);
                pd += channel;
            }
        }
    }else if (angle == 270){// 270
        rotatedImg = ipCreateImage(ipSize(src->height, src->width), 8, channel);
        rotatedImg->height = src->width;
        rotatedImg->width = src->height;
        rotatedImg->widthStep = channel*rotatedImg->width;
        unsigned char* psrc = (unsigned char*)src->imageData;
        unsigned char* pdst = (unsigned char*)rotatedImg->imageData;
        for(int h = 0; h < rotatedImg->height; h++){
            unsigned char* pd = pdst;
            pdst += rotatedImg->widthStep;
            for(int w = 0; w < rotatedImg->width; w++){
                unsigned char * ps = psrc + w * src->widthStep + (src->width - h - 1) * channel;
                for(int ch = 0; ch < channel; ch ++){
                    *pd++ = *ps++;
                }
            }
        }
    }else {
        LOGE("angle not supported for rotation:%d ",angle);
    }
    LOGD("RotateImage leave");
    return rotatedImg;
}

Ip_Image* loadJpegFile(char* filename)
{
    FILE* fp=fopen(filename,"rb");
    if(fp==NULL){
        LOGE("failed to open input file %s\n",filename);
        return NULL;
    }
    const int SIZE = 1<<18;
    char* buffer = new char[SIZE];
    int buffersize = fread(buffer,SIZE,1,fp);
    fclose(fp);

    Ip_Image* img = cvLoadJpegBuffer(buffer, buffersize);
    delete[] buffer;
    return img;
}

// bilinear interpolation
// src: source image
// mask: if the element in mask equals zero, then interpolate it
void BilinearInterpolation(Ip_Image* src, Ip_Image* mask)
{
    // bilinear interpolation
    int rightDist = 0,leftDist, upDist;
    int* downDist = new int [mask->width];
    memset(downDist,0,sizeof(int)*mask->width);

    const int BOARDER = 1;
    // right side interpolation first
    int startX = mask->width - BOARDER;
    for(int h = 0; h < mask->height; h++){
        unsigned char* pm = (unsigned char*)mask->imageData + h* mask->widthStep + startX;
        unsigned char* pd = (unsigned char*)src->imageData + h* src->widthStep + startX * 3 ;
        for(int w = startX; w < mask->width; w++){
            if(!*pm){
                if(downDist[w] == 0){
                    unsigned char* pt = pm;
                    while(!*pt && h + downDist[w] < mask->height - 1){
                        pt += mask->widthStep;
                        downDist[w]++;
                    }
                }
                *pm = 255;
                upDist = h == 0 ? 0 : 1;

                unsigned char* pup = pd - upDist * src->widthStep;
                unsigned char* pdown = pd + downDist[w] * src->widthStep;
                int verDist = upDist + downDist[w];
                float r1 = (float)downDist[w] / verDist ;
                for(int ch = 0 ; ch < 3 ; ch++){
                    *pd ++ = r1 * (*pup++) + (1 - r1 ) * (*pdown++);
                }
                if(downDist[w] > 0){
                    downDist[w] --;
                }
            } else{
                downDist[w] = 0;
                pd += 3;
            }
            pm++;
        }
    }
    // bottom interpolation
    int startY = mask->height - BOARDER;
    for(int h = startY; h < mask->height; h++){
        unsigned char* pm = (unsigned char*)mask->imageData + h* mask->widthStep;
        unsigned char* pd = (unsigned char*)src->imageData + h* src->widthStep;
        for(int w = 0; w < mask->width; w++){
            if(!*pm){
                unsigned char* pt = pm;
                if(rightDist == 0){
                    while(!*pt++ && w + rightDist < mask->width - 1){
                        rightDist++;
                    }
                }
                *pm = 255;
                leftDist = w == 0 ? 0 : 1;

                unsigned char* pleft = pd - leftDist*3;
                unsigned char* pright = pd + rightDist*3;
                int horDist = rightDist + leftDist;

                float r1 = (float)rightDist / horDist ;
                for(int ch = 0 ; ch < 3 ; ch++){
                    *pd ++ = r1 * (*pleft++) + (1 - r1) * (*pright++);
                }

                if(rightDist > 0){
                    rightDist --;
                }
            } else{
                rightDist = 0;
                pd += 3;
            }
            pm++;
        }
    }
    // main interpolation
    memset(downDist,0,sizeof(int)*mask->width);
    unsigned char* pmap = (unsigned char*)mask->imageData;
    unsigned char* pdst = (unsigned char*)src->imageData;
    for(int h = 0; h < mask->height; h++){
        unsigned char* pm = pmap;
        unsigned char* pd = pdst;
        for(int w = 0; w < mask->width; w++){
            if(!*pm){
                unsigned char* pt = pm;
                if(rightDist == 0){
                    while(!*pt++ && w + rightDist < mask->width - 1){
                        rightDist++;
                    }
                }
                if(downDist[w] == 0){
                    unsigned char* pt = pm;
                    while(!*pt && h + downDist[w] < mask->height - 1){
                        pt += mask->widthStep;
                        downDist[w]++;
                    }
                }
                leftDist = w == 0 ? 0 : 1;
                upDist = h == 0 ? 0 : 1;

                unsigned char* pleft = pd - leftDist*3;
                unsigned char* pright = pd + rightDist*3;
                unsigned char* pup = pd - upDist * src->widthStep;
                unsigned char* pdown = pd + downDist[w] * src->widthStep;
                int horDist = rightDist + leftDist;
                int verDist = upDist + downDist[w];
                int totalDist = horDist + verDist;
                float ratioLeft = (float)rightDist / horDist * verDist /totalDist;
                float ratioRight = (float)leftDist / horDist * verDist /totalDist;
                float ratioUp = (float)downDist[w] / verDist * horDist /totalDist;
                float ratioDown = (float)upDist / verDist * horDist /totalDist;

                if(h < BOARDER || h > mask->height - BOARDER){
                    for(int ch = 0 ; ch < 3 ; ch++){
                        *pd ++ = (float)rightDist / horDist * (*pleft++) + (float)leftDist / horDist * (*pright++);
                    }
                } else if(w < BOARDER || w > mask->width - BOARDER){
                    for(int ch = 0 ; ch < 3 ; ch++){
                        *pd ++ = (float)downDist[w] / verDist * (*pup++) + (float)upDist / verDist * (*pdown++);
                    }
                } else {
                    for(int ch = 0 ; ch < 3 ; ch++){
                        *pd ++ = ratioLeft * (*pleft++) + ratioRight * (*pright++)
                            + ratioUp * (*pup++) + ratioDown * (*pdown++);
                    }
                }

                if(rightDist > 0){
                    rightDist --;
                }
                if(downDist[w] > 0){
                    downDist[w] --;
                }
            } else{
                rightDist = 0;
                downDist[w] = 0;
                pd += 3;
            }
            pm++;
        }
        pmap += mask->widthStep;
        pdst += src->widthStep;
    }
    delete[] downDist;
}

// load png to bgra
Ip_Image* cvLoadImagePng(char* file_name,bool isRGBA)
{
    int width, height;
    png_byte color_type;
    png_byte bit_depth;

    png_structp png_ptr;
    png_infop info_ptr;
    int number_of_passes;
    png_bytep * row_pointers;

    char header[8];    // 8 is the maximum size that can be checked

    /* open file and test for it being a png */
    FILE *fp = fopen(file_name, "rb");
    if (!fp){
        LOGE("failed to read image file: %s\n",file_name);
        return NULL;
    }
    fread(header, 1, 8, fp);
    if (png_sig_cmp((png_byte*)header, 0, 8)){
        LOGE("File %s is not recognized as a PNG file\n", file_name);
        return NULL;
    }

    /* initialize stuff */
    png_ptr = png_create_read_struct(PNG_LIBPNG_VER_STRING, NULL, NULL, NULL);

    if (!png_ptr){
        LOGE(" png_create_read_struct failed\n");
        return NULL;
    }
    info_ptr = png_create_info_struct(png_ptr);
    if (!info_ptr){
        png_destroy_read_struct(&png_ptr,(png_infopp)NULL, (png_infopp)NULL);
        LOGE("png_create_info_struct failed\n");
        return NULL;
    }
    png_infop end_info = png_create_info_struct(png_ptr);
    if (!end_info){
        png_destroy_read_struct(&png_ptr, &info_ptr,(png_infopp)NULL);
        return NULL;
    }

    if (setjmp(png_jmpbuf(png_ptr))){
        LOGE("Error during init_io\n");
        png_destroy_read_struct(&png_ptr, &info_ptr,&end_info);
        fclose(fp);
        return NULL;
    }
    png_init_io(png_ptr, fp);
    png_set_sig_bytes(png_ptr, 8);

    png_read_info(png_ptr, info_ptr);

    width = png_get_image_width(png_ptr, info_ptr);
    height = png_get_image_height(png_ptr, info_ptr);
    color_type = png_get_color_type(png_ptr, info_ptr);
    bit_depth = png_get_bit_depth(png_ptr, info_ptr);

    number_of_passes = png_set_interlace_handling(png_ptr);
    png_read_update_info(png_ptr, info_ptr);

    /* read file */
    if (setjmp(png_jmpbuf(png_ptr))){
        LOGE("Error during read_image\n");
        return NULL;
    }
    Ip_Image* argb = ipCreateImage(ipSize(width, height),8,4);
    row_pointers = (png_bytep*) malloc(sizeof(png_bytep) * height);
    for (int y=0; y<height; y++){
        row_pointers[y] = (png_byte*) (argb->imageData + y* argb->widthStep);
    }
    png_read_image(png_ptr, row_pointers);
    if (png_get_color_type(png_ptr, info_ptr) == PNG_COLOR_TYPE_RGB){
        LOGE("input file is PNG_COLOR_TYPE_RGB but must be PNG_COLOR_TYPE_RGBA, lacks the alpha channel");
        return NULL;
    }
    if (png_get_color_type(png_ptr, info_ptr) != PNG_COLOR_TYPE_RGBA){
        LOGE("[process_file] color_type of input file must be PNG_COLOR_TYPE_RGBA (%d) (is %d)",
            PNG_COLOR_TYPE_RGBA, png_get_color_type(png_ptr, info_ptr));
        return NULL;
    }
    png_byte t;
/*
    if(!isRGBA){
        for (int h=0; h<height; h++){
            png_byte* p= row_pointers[h];
            for(int w=0; w< width; w++){
                t = *p;
                *p = *(p+2);
                *(p+2) = t;
                p += 4;
            }
        }
    }
*/
    fclose(fp);
    free(row_pointers);
    png_destroy_read_struct(&png_ptr, &info_ptr,&end_info);
    return argb;
}
// decode jpeg buffer to rgb
Ip_Image* cvLoadJpegBuffer(char* jpegBuffer, int bufferLength)
{
    TSJpegParam param;
    param.srcBuffer = jpegBuffer;
    param.srcBufferSize = bufferLength;
    param.color_space = TSJPEG_COLOR_RGB;

    CTSJpeg::DecodeMemToMem(param);
//    CTSJpeg::DeCompressMemToMem(param);

    Ip_Image* img = ipCreateImage(ipSize(param.img_width, param.img_height), 8, param.img_components);
//    param.img_widthStep = param.img_width*3;
/*    if(param.img_width % 4 ){
        //LOGD("PhotoGridcopy data to Ip_Image");
        char* pimg = img->imageData;
        char* pjpeg = param.dstBuffer;
        for (int i = 0; i < param.img_height; i++){
            memcpy(pimg, pjpeg, param.img_widthStep);
            pimg += img->widthStep;
            pjpeg += param.img_widthStep;
        }
    }else {
        memcpy(img->imageData, param.dstBuffer, img->imageSize);
        //img->imageData = param.dstBuffer;
    }
*/
//    memcpy(img->imageData, param.dstBuffer, img->imageSize);
    LOGI("img->widthStep=%d, param.img_widthStep=%d",img->widthStep , param.img_widthStep );
    int nPitch = img->widthStep > param.img_widthStep ? param.img_widthStep : img->widthStep;
    for(int i = 0;i<param.img_height;i++)
    {
    	unsigned char * pSrc = (unsigned char*)param.dstBuffer + i*param.img_widthStep;
    	unsigned char * pDst = (unsigned char*)img->imageData + i*img->widthStep;
    	memcpy(pDst,pSrc,nPitch);
    }

    delete [] param.dstBuffer;
//    cvConvertImage(img, img, IP_CVTIMG_SWAP_RB);
    return img;
}

// encode Ip_Image to jpeg buffer
char* cvEncodeJpegBuffer(Ip_Image* img, int& bufferLength,int jpegQuality,int srcBufferLength )
{
    TSJpegParam param;
    if(img->nChannels==1) {
        param.color_space = TSJPEG_COLOR_GRAYSCALE;
        param.img_components = 1;
    } else {
        param.color_space = TSJPEG_COLOR_RGB;
    }
    param.img_height = img->height;
    param.img_width = img->width;
    param.srcBuffer = img->imageData;
    param.srcBufferSize = img->imageSize;
    param.img_widthStep = img->widthStep;
    param.quality = jpegQuality;

    if(jpegQuality>95){
        srcBufferLength *= 2;
    }
//    cvConvertImage(img, img, IP_CVTIMG_SWAP_RB);

    CTSJpeg::CompressMemToMem(param,srcBufferLength);

    bufferLength = param.dstBufferSize;
    return param.dstBuffer;
}

// rotate yuv
char* rotateYuv(char* yuvData,int width,int height)
{
    int len = width * height;
    int len23 = len*3/2;
    char* newYuv = new char[len23];
    // y
    for(int h = 0; h < width; h++){
        char* dy = newYuv + h * height;
        char* sy = yuvData + len- width + h;
        for(int w =0; w < height; w ++){
            *dy ++ = *sy;
            sy -= width;
        }
    }
    // uv
    int width2 = width/2;
    int height2 = height/2;
    for(int h = 0; h < width2; h++){
        char* duv = newYuv + h * height + len;
        char* suv = yuvData + len23- width + h *2;
        for(int w =0; w < height2; w ++){
            *duv ++ = suv[0];
            *duv ++ = suv[1];
            suv -= width;
        }
    }
    return newYuv;
}

// rotate yuv
char* rotateYuvFront(char* yuvData,int width,int height)
{
    int len = width * height;
    int len23 = len*3/2;
    char* newYuv = new char[len23];
    // y
    for(int h = width - 1; h >=0; h--){
        char* dy = newYuv + (width - h - 1) * height;
        char* sy = yuvData + len- width + h;
        for(int w =0; w < height; w ++){
            *dy ++ = *sy;
            sy -= width;
        }
    }
    // uv
    int width2 = width/2;
    int height2 = height/2;
    for(int h = width2 - 1; h >= 0; h--){
        char* duv = newYuv + (width2 - 1 - h) * height + len;
        char* suv = yuvData + len23- width + h *2;
        for(int w =0; w < height2; w ++){
            *duv ++ = suv[0];
            *duv ++ = suv[1];
            suv -= width;
        }
    }
    return newYuv;
}

// rotate yuv
char* rotateYuvRevert(char* yuvData,int width,int height)
{
    int len = width * height;
    int len23 = len*3/2;
    char* newYuv = new char[len23];
    // y
    for(int h = 0; h < height; h++){
        char* dy = newYuv + h * width;
        char* sy = yuvData + width * (h + 1) - 1;
        for(int w =0; w < width; w++){
            *dy ++ = *sy--;
        }
    }
    // uv
    int width2 = width/2;
    int height2 = height/2;
    for(int h = 0; h < height2; h++){
        char* duv = newYuv + len + h * width;
        char* suv = yuvData + len + width * (h + 1) - 2;
        for(int w =0; w < width2; w++){
            *duv ++ = suv[0];
            *duv ++ = suv[1];
            suv -= 2;
        }
    }
    return newYuv;
}
}// end of namespace
