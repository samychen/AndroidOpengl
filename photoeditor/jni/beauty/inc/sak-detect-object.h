#ifndef __SAK_DETECT_OBJECT_H__
#define __SAK_DETECT_OBJECT_H__

#include "tcomdef.h"
#include "terror.h"
#include "tmem.h"
#include "tsoffscreen.h"


#if (defined(_WIN32) || defined(_WIN64)) && !defined(__GNUC__)
#   ifdef SAK_DLL
#       define SAK_EXPORTS __declspec(dllexport)
#   else
#       define SAK_EXPORTS __declspec(dllimport)
#   endif
#elif (defined(linux) || defined(__linux) || defined(__ANDROID__)) && defined(__GNUC__)
#   define SAK_EXPORTS __attribute__((visibility("default")))
#else  //for IOS
#   define SAK_EXPORTS 
#   endif


#ifndef __cplusplus
#   define SAK_DEFAULT(val)
#else
#   define SAK_DEFAULT(val) = val
extern "C" {
#endif


/** 
 * sakDetectObject_create Create a DetectObject object and return its handle.
 * 
 * @param memHandle 
 * 
 * @return 
 */
SAK_EXPORTS  THandle sakDetectObject_create(THandle memHandle);

/** 
 * sakDetectObject_destroy Destroy the DetectObject object and release its memory.
 * 
 * @param pMemHandle 
 * 
 * @return 
 */
SAK_EXPORTS  TVoid sakDetectObject_destroy(THandle* pHandle);

/** 
 * sakDetectObject_setImage Add the image prepared to be detected on.
 * NOTE: After calling this API, the last object information got by the calling of
 * sakDetectObject_detect will lost.
 * 
 * @param hDetector 
 * @param image 
 * @return 
 */
SAK_EXPORTS  TRESULT  sakDetectObject_setImage(THandle hDetector, 
                                               const TSOFFSCREEN* image);

/** 
 * sakDetectObject_detect Detect the object belongs to the class specified by `className', 
 * such as "face", "eye", "mouth", "gesture" and "smile" etc.
 * 
 * @param hDetector 
 * @param className  Case insensible.
 * @param faceRect   Pass NULL when detecting face.
 * 
 * @return If less than zero, failed, otherwise the number of the detected objects.
 */
SAK_EXPORTS  TInt32  sakDetectObject_detect(THandle hDetector, 
                                            const TPChar className,
                                            const TRECT* faceRect SAK_DEFAULT(0));

/** 
 * sakDetectObject_object Get the `index'th object's position and size, which were
 * detected by the lastest calling of sakDetectObject_detect.
 * NOTE: The position is always related to the left-top point in the image specified
 * by the sakDetectObject_setImage.
 * 
 * @param hDetector 
 * @param index 
 * @param object : left-close-right-open
 * 
 * @return TFalse if failed, otherwise TTrue.
 */
SAK_EXPORTS  TBool  sakDetectObject_object(THandle hDetector, 
                                           TInt32 index, 
                                           TRECT* object);

/** 
 * sakDetectObject_setProperty set the property of detector, Must be called after sakDetectObject_setImage
 * @param hDetector 
 * @param className.
 * @param key    : Pass property name
 * @param value  : Pass property pointer, If 0 use the default property
 * @return If less than zero, failed, otherwise the number of the detected objects.
 */
SAK_EXPORTS  TBool  sakDetectObject_setProperty(
                                           THandle      hDetector, 
                                           const TPChar className,
                                           const TPChar key,
                                           TPVoid       value);
/*
The property type
*/
typedef struct tagSearchWindows
{
    int *winSize;
    int *stepSize;
    int length;
}SearchWindows;
    
typedef struct tagSearchWindowRange
{
    int  minSize;
    int  maxSize;
    int  maxObjects;
    float stepRatio;
}SearchWindowRange;
    
#define profile_portrait 1

#ifdef __cplusplus
}
#endif

#endif//__SAK_DETECT_OBJECT_H__
