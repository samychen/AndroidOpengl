//
// FaceBeautify.h
//

#ifndef __FACE_BEAUTIFY_H__
#define __FACE_BEAUTIFY_H__

#include "tsoffscreen.h"
#include "tfacemaskdef.h"

#define TSFB_FMT_YUV420LP			0x3
#define TSFB_FMT_YUV420LP_VUVU		0x4		//yyyy...vuvu...
#define TSFB_FMT_YUV420_PLANAR		0x5		//yyyy...uu(W/2*H/2)¡­vv(W/2*H/2)

class CFaceBeautify
{
public:
    CFaceBeautify();
    ~CFaceBeautify();

public:
    typedef enum __tag_TDealMode
    {
        DEAL_MODE_FACE,
        DEAL_MODE_SKIN
    } TDealMode;

public:
    int Init(int mode);
    int Work(LPTSOFFSCREEN pFaceImg, LPTSOFFSCREEN pDstImage, TMASK* pFaceMask, TRECT rcFace, 
		TInt32 lBlurLevel, TInt32 lWhitenLevel, TBool bCaptureProcess);
    int Exit();

	int WorkBeautifulColor(LPTSOFFSCREEN pFaceImg, LPTSOFFSCREEN pDstImage, TMASK* pFaceMask, TRECT rcFace, 
		TInt32 lBlurLevel, TInt32 lWhitenLevel, TBool bCaptureProcess, TInt32 ColorId);

private:
    void* _impl;
};

#endif // __FACE_BEAUTIFY_H__
