#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <stdint.h>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <string>
#include "ts-detect-object.h"

#define CALL(fun)                               \
    if (!(fun)) goto EXIT;

#define DATAi(im, i) (im)->ppu8Plane[(i)]
#define PITCHi(im, i) (im)->pi32Pitch[(i)]
#define DATA(im)   (im)->ppu8Plane[0]
#define PITCH(im)  (im)->pi32Pitch[0]
#define WIDTH(im)  (im)->i32Width
#define HEIGHT(im) (im)->i32Height
#define FORMAT(im) (im)->u32PixelArrayFormat

#define sakTrunc(x,minv,maxv) ((x)<(minv)?(minv):((x)>(maxv)?(maxv):(x)))
#define sakYUV2RGB(R,G,B,Y,U,V)                             \
    ((R)=(256*Y+358*V)>>8, (R)=sakTrunc(R,0,255),           \
     (G)=(256*Y-183*V-87*U)>>8, (G)=sakTrunc(G,0,255),      \
     (B)=(256*Y+454*U)>>8, (B)=sakTrunc(B,0,255))

#define sakRGB2YCbCr(Y, Cb, Cr, R, G, B)                                \
    (((Y) = (((R) * 306  + (G) * 601  + (B) * 117  + 512 ) >> 10)),     \
     ((Cr) = (((R) * 2048 - (G) * 1715 - (B) * 333  + 2048) >> 12) + 128), \
     ((Cb) = (((-R) * 691  - (G) * 1357 + (B) * 2048 + 2048) >> 12) + 128))  

#define EVEN(x) x/2*2

using namespace std;

// Image IO
//================================================================================

typedef unsigned char MByte;

#  ifndef _WIN32
typedef uint16_t WORD;
typedef uint32_t DWORD;
typedef uint32_t LONG;

typedef struct tagBITMAPFILEHEADER {
  WORD  bfType;
  DWORD bfSize;
  WORD  bfReserved1;
  WORD  bfReserved2;
  DWORD bfOffBits;
} __attribute__ ((packed)) BITMAPFILEHEADER, *PBITMAPFILEHEADER;

typedef struct tagBITMAPINFOHEADER {
  DWORD biSize;
  LONG  biWidth;
  LONG  biHeight;
  WORD  biPlanes;
  WORD  biBitCount;
  DWORD biCompression;
  DWORD biSizeImage;
  LONG  biXPelsPerMeter;
  LONG  biYPelsPerMeter;
  DWORD biClrUsed;
  DWORD biClrImportant;
} __attribute__ ((packed)) BITMAPINFOHEADER, *PBITMAPINFOHEADER;
#  else
#    include <Windows.h>
#  endif

/// Read BMP file as NV21 format.
static bool readBMPFile(const char *pFileName, TSOFFSCREEN* pImg)
{
    bool success = false;
    BITMAPFILEHEADER bmpfh;
    BITMAPINFOHEADER bmpih;
    TUInt8* buf = 0;
    int nLineBytes;
    FILE* fp = fopen(pFileName, "rb");
	if (fp == NULL) {
		cout << "Can't open " << pFileName << endl;
        return false;
	}

    CALL(fread(&bmpfh, sizeof(BITMAPFILEHEADER), 1, fp) == 1);
    CALL(bmpfh.bfType == 0x4D42);
    CALL(fread(&bmpih, sizeof(BITMAPINFOHEADER), 1, fp) == 1);
    nLineBytes = bmpih.biSizeImage / bmpih.biHeight;

    FORMAT(pImg) = TS_PAF_NV21;
    WIDTH(pImg) = bmpih.biWidth/2*2;
    HEIGHT(pImg) = bmpih.biHeight/2*2;
    PITCHi(pImg, 0) = WIDTH(pImg);
    PITCHi(pImg, 1) = WIDTH(pImg);
    CALL(DATA(pImg) = (TUInt8*) malloc(3 * HEIGHT(pImg)*PITCH(pImg)*sizeof(TUInt8) / 2));
    DATAi(pImg,1) = DATA(pImg) + HEIGHT(pImg)*PITCH(pImg);
    CALL(buf = (TUInt8*) malloc(2*nLineBytes*sizeof(*buf)));
    // bmpfh.bfOffBits may not be equal to sizeof(BITMAPINFOHEADER) + sizeof(BITMAPFILEHEADER)
    fseek(fp, bmpfh.bfOffBits, SEEK_SET);

    switch (bmpih.biBitCount) {
      case 24: {
          TUInt8* pY1 = DATA(pImg) + (HEIGHT(pImg) - 2) * PITCH(pImg);
          TUInt8* pY2 = DATA(pImg) + (HEIGHT(pImg) - 1) * PITCH(pImg);
          TUInt8* pUV = DATAi(pImg,1) + (HEIGHT(pImg)/2 - 1) * PITCHi(pImg,1);
          TUInt8* buf2 = buf;
          TUInt8* buf1 = buf + nLineBytes;

          for (int y = HEIGHT(pImg) - 1; y >= 0; y -= 2) {
              CALL(fread(buf, nLineBytes, 2, fp) == 2);
              for (int x = 0; x < WIDTH(pImg); x += 2) {
                  int R,G,B,Y,Cb,Cr;
                  int CB = 0, CR = 0;
                  R = buf1[3*x+2], G = buf1[3*x+1], B = buf1[3*x];
                  sakRGB2YCbCr(Y, Cb, Cr, R, G, B);
                  CB += Cb, CR += Cr;
                  pY1[x] = Y;

                  R = buf1[3*x+5], G = buf1[3*x+4], B = buf1[3*x+3];
                  sakRGB2YCbCr(Y, Cb, Cr, R, G, B);

                  CB += Cb, CR += Cr;
                  pY1[x+1] = Y;

                  R = buf2[3*x+2], G = buf2[3*x+1], B = buf2[3*x];
                  sakRGB2YCbCr(Y, Cb, Cr, R, G, B);
                  CB += Cb, CR += Cr;
                  pY2[x] = Y;

                  R = buf2[3*x+5], G = buf2[3*x+4], B = buf2[3*x+3];
                  sakRGB2YCbCr(Y, Cb, Cr, R, G, B);
                  CB += Cb, CR += Cr;
                  pY2[x+1] = Y;

                  pUV[x] = CR/4;
                  pUV[x+1] = CB/4;
              }

              pY1 -= 2 * PITCH(pImg);
              pY2 -= 2 * PITCH(pImg);
              pUV -= PITCHi(pImg,1);
          }
      }
          break;
      default:
		  cout << "Format error." << endl;
          goto EXIT;
    }

    success = true;

  EXIT:
    fclose(fp);
    if (buf)
        free(buf);

    return success;
}

static bool saveBMPFile(const char *pFileName, TSOFFSCREEN* pImg)
{
    bool success = false;
    BITMAPFILEHEADER bmpfh;
    BITMAPINFOHEADER bmpih;
    FILE *fp;
    int width, height,nBytesLine;
    //int nPitch;
    //int bitCount = 24;
    TUInt8* buf = 0;

    assert((pImg && FORMAT(pImg) == TS_PAF_NV21) || FORMAT(pImg) == TS_PAF_GRAY);

    //MByte *pSrc = DATA(pImg);
    //nPitch = PITCH(pImg);
    width = WIDTH(pImg);
    height = HEIGHT(pImg);
    nBytesLine = (width*24+31)/32*4;
    
    if ((fp = fopen(pFileName, "wb")) == NULL) {
        return false;
    }
    /* init .bmp file header */

    bmpfh.bfType = 0x4D42;        /* 'BM' */
    bmpfh.bfSize = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER) + nBytesLine * height;
    bmpfh.bfReserved1 = 0;
    bmpfh.bfReserved2 = 0;
    bmpfh.bfOffBits = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);

    /* Write .bmp file header */
    CALL(fwrite(&bmpfh, sizeof(BITMAPFILEHEADER), 1, fp) == 1);

    /* init .bmp info header */
    bmpih.biSize = sizeof(BITMAPINFOHEADER);
    bmpih.biWidth = width;
    bmpih.biHeight = height;
    bmpih.biPlanes = 1;
    bmpih.biBitCount = 24;
    bmpih.biCompression = 0;//BI_RGB;
    bmpih.biSizeImage = nBytesLine * height;
    bmpih.biXPelsPerMeter = 0;
    bmpih.biYPelsPerMeter = 0;
    bmpih.biClrUsed = 0;
    bmpih.biClrImportant = 0;

    /* Write the BITMAPINFOHEADER */
    CALL(fwrite(&bmpih, sizeof(BITMAPINFOHEADER), 1, fp) == 1);

    CALL(buf = (TUInt8*) malloc(2*nBytesLine*sizeof(*buf)));

    if (FORMAT(pImg) == TS_PAF_NV21) {
        TUInt8* pY1 = DATA(pImg) + (HEIGHT(pImg)/2*2-2) * PITCH(pImg);
        TUInt8* pY2 = DATA(pImg) + (HEIGHT(pImg)/2*2-1) * PITCH(pImg);
        TUInt8* pUV = DATAi(pImg,1) + (HEIGHT(pImg)/2 - 1) * PITCH(pImg);
        TUInt8* buf2 = buf;
        TUInt8* buf1 = buf + nBytesLine;

        for (int y = HEIGHT(pImg)/2*2 - 1; y >= 0; y -= 2) {
            for (int x = 0; x < WIDTH(pImg)/2*2; x += 2) {
                int R,G,B,Y,U,V;
                Y = pY1[x], U = pUV[x+1] - 128, V = pUV[x] - 128;
                sakYUV2RGB(R, G, B, Y, U, V);
                buf1[3*x] = B, buf1[3*x+1] = G, buf1[3*x+2] = R;

                Y = pY1[x+1];
                sakYUV2RGB(R, G, B, Y, U, V);
                buf1[3*x+3] = B, buf1[3*x+4] = G, buf1[3*x+5] = R;

                Y = pY2[x];
                sakYUV2RGB(R, G, B, Y, U, V);
                buf2[3*x] = B, buf2[3*x+1] = G, buf2[3*x+2] = R;

                Y = pY2[x+1];
                sakYUV2RGB(R, G, B, Y, U, V);
                buf2[3*x+3] = B, buf2[3*x+4] = G, buf2[3*x+5] = R;
            }
            CALL(fwrite(buf, nBytesLine, 2*sizeof(*buf), fp) == 2);

            pY1 -= 2 * PITCH(pImg);
            pY2 -= 2 * PITCH(pImg);
            pUV -= PITCHi(pImg,1);
        }
    }
    else if (FORMAT(pImg) == TS_PAF_GRAY) {
        TUInt8* pY = DATA(pImg) + (HEIGHT(pImg)-1) * PITCH(pImg);

        for (int y = HEIGHT(pImg) - 1; y >= 0; y--) {
            for (int x = 0; x < WIDTH(pImg); x++) {
                buf[3*x] = buf[3*x+1] = buf[3*x+2] = pY[x];
            }
            CALL(fwrite(buf, nBytesLine, sizeof(*buf), fp) == 1);

            pY -= PITCH(pImg);
        }
    }

    success = true;

  EXIT:
    fclose(fp);
    if (buf)
        free(buf);
    return success;
}

static bool loadImage(const char* fn, TSOFFSCREEN* im)
{
    return readBMPFile(fn, im);
}

static bool saveImage(const char* fn, TSOFFSCREEN* im)
{
    return saveBMPFile(fn, im);
}

// GDI
//================================================================================
static void
DrawPoint(TSOFFSCREEN* im, int x, int y, int color[3])
{
    int w = im->i32Width;
    int h = im->i32Height;

    if (x < 0 || x >= w || y < 0 || y >= h)
        return;

    if (FORMAT(im) == TS_PAF_GRAY) {
        unsigned char* p = im->ppu8Plane[0];
        int step = im->pi32Pitch[0];
        int c = color[0];
        if (c < color[1]) c = color[1];
        if (c < color[2]) c = color[2];

        p[y * step + x] = c;
        if (x > 0)
            p[y*step + x - 1] = c;
        if (x < w - 1)
            p[y*step + x + 1] = c;
        if (y > 0)
            p[(y-1)*step + x] = c;
        if (y < h - 1)
            p[(y+1)*step + x] = c;
    }
    else if (FORMAT(im) == TS_PAF_NV21) {
        int Y, Cb, Cr;
        unsigned char* py = DATA(im);
        unsigned char* puv = DATAi(im, 1);
        int stepy = PITCH(im);
        int stepuv = PITCHi(im,1);

        sakRGB2YCbCr(Y, Cb, Cr, color[2], color[1], color[0]);
        py[EVEN(y)*stepy + EVEN(x)] = Y;
        py[(EVEN(y)+1)*stepy + EVEN(x)] = Y;
        py[EVEN(y)*stepy + EVEN(x) + 1] = Y;
        py[(EVEN(y)+1)*stepy + EVEN(x) + 1] = Y;
        puv[y/2*stepuv + EVEN(x)] = Cr;
        puv[y/2*stepuv + EVEN(x) + 1] = Cb;
    }
    else assert(0);
}

#pragma GCC diagnostic ignored "-Wunused-function"

static void
DrawPoints(TSOFFSCREEN* im, double* landmarks, int n)
{
    for (int i = 0; i < n; i++) {
        int color[3] = {0, 255, 0};
        DrawPoint(im, (int)(landmarks[2*i] + 0.5), (int)(landmarks[2*i+1] + 0.5), color);
    }
}

static void
DrawRect(TSOFFSCREEN* im, TRECT rect)
{
    int color[3] = {255, 128, 0};
    int x, y;
    for (x = rect.left, y = rect.top; x < rect.right; x++) {
        DrawPoint(im, x, y, color);
    }
    for (x = rect.right-1, y = rect.top; y < rect.bottom; y++) {
        DrawPoint(im, x, y, color);
    }
    for (x = rect.left, y = rect.bottom-1; x < rect.right; x++) {
        DrawPoint(im, x, y, color);
    }
    for (x = rect.left, y = rect.top; y < rect.bottom; y++) {
        DrawPoint(im, x, y, color);
    }
}

// Time
//================================================================================
double g_time = 0;

#if defined _MSC_VER || defined __BORLANDC__
   typedef __int64 int64;
#else
   typedef int64_t int64;
#endif

namespace sak{
int64 getTickCount(void)
{
#if defined WIN32 || defined _WIN32 || defined WINCE
    LARGE_INTEGER counter;
    QueryPerformanceCounter( &counter );
    return (int64)counter.QuadPart;
#elif defined __linux || defined __linux__
    struct timespec tp;
    clock_gettime(CLOCK_MONOTONIC, &tp);
    return (int64)tp.tv_sec*1000000000 + tp.tv_nsec;
#elif defined __MACH__ && defined __APPLE__
//    return (int64)mach_absolute_time();
    return 0;
#else
    struct timeval tv;
    struct timezone tz;
    gettimeofday( &tv, &tz );
    return (int64)tv.tv_sec*1000000 + tv.tv_usec;
#endif
}

double getTickFrequency(void)
{
#if defined WIN32 || defined _WIN32 || defined WINCE
    LARGE_INTEGER freq;
    QueryPerformanceFrequency(&freq);
    return (double)freq.QuadPart;
#elif defined __linux || defined __linux__
    return 1e9;
#elif defined __MACH__ && defined __APPLE__
    // static double freq = 0;
    // if (freq == 0){
    //     mach_timebase_info_data_t sTimebaseInfo;
    //     mach_timebase_info(&sTimebaseInfo);
    //     freq = sTimebaseInfo.denom*1e9/sTimebaseInfo.numer;
    // }
    // return freq;
    return 1.0;
#else
    return 1e6;
#endif
}

}


void* load_model(const char* fn)
{
    FILE* fp = fopen(fn, "rb");
    if (!fp) return NULL;

    // get the file length
    fseek(fp, 0L, SEEK_END);
    int sz = ftell(fp);
    fseek(fp, 0L, SEEK_SET);

    void* data = malloc(sz);
    if (fread(data, sz, 1, fp) != 1) {
        free(data);
        fclose(fp);
        return NULL;
    }

    fclose(fp);

    return data;
}

void unload_model(void* data)
{
    if (data)
        free(data);
}


/* Usage: ./detect-object_example <image list file> [obj-type]
 * The image list file's format is like the following,
 *   
 *   /user/images/im.bmp
 *   /home/abc/im3.bmp
 *
 * Each line is a image path name. NOTE, path name should not have blanks.
 */
int main(int argc, char* argv[])
{
    const char* imListFileName = argv[1];
    const char* objType = "face";
    const char* modelFN = "fmodel.data";

    // Load model data.
    // void* faceModelData = load_model(modelFN);
    // if (!faceModelData) {
    //     cerr << "Warning: Load model data " << modelFN << " failed." << endl;
    // }
    // ModelDataList data;
    // data.n = 1;
    // data.datalist = (ModelData*) malloc(sizeof(ModelData));
    // data.datalist[0].objType = "face";
    // data.datalist[0].data = faceModelData;

    TSObjectDetector od = tsDetectObject_create();
    if (!od) {
        cerr << "Create Object Detector failed." << endl;
        return 1;
    }

    // Pass the model data
    // tsDetectObject_setProperty(od, "face", "ModelData", faceModelData);

    /// Detecting
    ifstream imList(imListFileName);
    string fn;
    int num = 0;
    while (imList >> fn) {
        if (fn.empty())
            continue;
        num++;

        // Load the image file.
        TSOFFSCREEN im;
		if (!loadImage(fn.c_str(), &im)) {
			cerr << "Loading " << fn << " failed" << endl;
            abort();
		}
        if (TOK != tsDetectObject_setImage(od, &im)) {
            cerr << "tsDetectObject_setImage failed" << endl;
            abort();
        }

        double timeT = (double) sak::getTickCount();
        int n = tsDetectObject_detect(od, objType, NULL);
        g_time += (double) sak::getTickCount() - timeT;

        // Draw face rectangle and save result image.
        if (n > 0) {
            char name[64];
            sprintf(name, "%04d.bmp", num);
            for (int i = 0; i < n; i++) {
                TRECT faceRect;
                tsDetectObject_object(od, i, &faceRect);
                DrawRect(&im, faceRect);
            }
            saveImage(name, &im);
        }

        free(DATA(&im));
    }

    cout << g_time*1000/sak::getTickFrequency()/num << "ms" << endl;

    tsDetectObject_destroy(od);
    // free(data.datalist);
    // unload_model(faceModelData);

    return 0;
}

