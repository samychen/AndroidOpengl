//
// FaceBeautify.h
//

#ifndef __FACE_BEAUTIFY_H__
#define __FACE_BEAUTIFY_H__

#include "tsoffscreen.h"
#include "tfacemaskdef.h"

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
    int Work(LPTSOFFSCREEN pFaceImg, LPTSOFFSCREEN pDstImage, TMASK* pFaceMask, TRECT rcFace, TLong lBlurLevel, TLong lWhitenLevel);
    int Exit();

private:
    void* _impl;
};

#endif // __FACE_BEAUTIFY_H__
