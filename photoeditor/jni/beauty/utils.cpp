/**
 * Copyright (C) 2012 Thundersoft Corporation
 * All rights Reserved
 */
#include"utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

namespace ts{

    void Utils::Yuv4202Rgba(char* y, char* uv, char* rgb, int width, int height)
    {
        int imageSize= width*height;

        // table
        static const signed kClipMin = -278;
        static const signed kClipMax = 535;

        unsigned char* mClip = new unsigned char[kClipMax - kClipMin + 1];
        for (signed i = kClipMin; i <= kClipMax; ++i) {
            mClip[i - kClipMin] = (i < 0) ? 0 : (i > 255) ? 255 : (unsigned char)i;
        }
        unsigned char *kAdjustedClip = &mClip[-kClipMin];

        unsigned char* pimg = (unsigned char*)rgb;

        const unsigned char *src_y = (const unsigned char *)y;
        const unsigned char *src_u =(const unsigned char *)uv;

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

                *pimg++ = kAdjustedClip[b1];
                *pimg++ = kAdjustedClip[g1];
                *pimg++ = kAdjustedClip[r1];
                *pimg++ = 255;
                *pimg++ = kAdjustedClip[b2];
                *pimg++ = kAdjustedClip[g2];
                *pimg++ = kAdjustedClip[r2];
                *pimg++ = 255;
            }

            src_y += width;

            if (y & 1) {
                src_u += width;
            }
        }

        delete[] mClip;
    }

    void Utils::Rgba2Yuv420(char* ybuffer, char* uvbuffer, char* rgba, int width, int height)
    {
        int len = width * height;
        unsigned char* y = (unsigned char*)ybuffer;
        unsigned char* uv = (unsigned char*)uvbuffer;

        unsigned char* prgb = (unsigned char*)rgba;
        unsigned char R,G,B;
        for(int h =0 ;h < height; h ++){
            if(h%2){
                for(int w=0; w < width; w+=2){
                    B = *prgb++;
                    G = *prgb++;
                    R = *prgb++;
                    prgb ++;
                    *y++ = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16 ;
                    *uv++ = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                    *uv++ = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;


                    B = *prgb++;
                    G = *prgb++;
                    R = *prgb++;
                    prgb ++;
                    *y++ = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16 ;
                }
            }else {
                for(int w=0; w < width; w++){
                    B = *prgb++;
                    G = *prgb++;
                    R = *prgb++;
                    prgb ++;
                    *y++ = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16 ;
                }
            }
        }
    }

}

