#if !defined(_fd_h)
#define _fd_h


#ifdef __cplusplus 
extern "C" { 
#endif 

#include "tcomdef.h"
#include "terror.h"
#include "tmem.h"

 
#define PFDInfo void*  

typedef struct{
    unsigned int CX,CY;  //center of face rectangle
    unsigned int Size;   //size of rectangle of face	
    float Value;
}FaceInfo;

typedef struct{
    int Num;
    FaceInfo *DetectedFace; //list hold 
}DetectedFaceInfo;

int InitFDData(THandle memHandle,  PFDInfo * fdinfo) ;
DetectedFaceInfo RAFaceDetection(THandle memHandle, PFDInfo info,  int scale, 
					 unsigned char *InputImage, int ImgWid, int ImgLine, int ImgHei ); 
void ReleaseFDData(THandle memHandle,  PFDInfo fdinfo );


#ifdef __cplusplus 
} 
#endif 


#endif


