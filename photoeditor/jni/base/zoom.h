#ifndef ZOOM_H
#define ZOOM_H

#ifdef __cplusplus
extern "C" {
#endif

void zoomRGBA(char* src, int srcW, int srcH, int stride, char* dst, int dstW, int dstH);

#ifdef __cplusplus
};
#endif

#endif
