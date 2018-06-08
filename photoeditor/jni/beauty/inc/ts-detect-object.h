#ifndef TS_DETECT_OBJECT_H__
#define TS_DETECT_OBJECT_H__

#include "tcomdef.h"
#include "terror.h"
#include "tsoffscreen.h"

#if defined(__GNUC__) || defined(__llvm__) || defined(__clang__)
#  ifdef SAK_DetectObject_EXPORTS
#    define DetectObject_EXPORTS __attribute__((visibility("default")))
#  else
#    define DetectObject_EXPORTS
#  endif
#elif defined(_WINDOWS) || defined(WIN32)
#  ifdef SAK_DetectObject_EXPORTS
#    define DetectObject_EXPORTS __declspec(dllexport)
#  elif defined(DetectObject_STATIC)
#    define DetectObject_EXPORTS
#  else
#    define DetectObject_EXPORTS __declspec(dllimport)
#  endif
#else
#  error "The compiler is not supported."
#endif

#define sakDetectObject_create      tsDetectObject_create
#define sakDetectObject_destroy     tsDetectObject_destroy
#define sakDetectObject_setImage    tsDetectObject_setImage
#define sakDetectObject_detect      tsDetectObject_detect
#define sakDetectObject_object      tsDetectObject_object
#define sakDetectObject_count       tsDetectObject_count
#define sakDetectObject_setProperty tsDetectObject_setProperty
#define sakDetectObject_getProperty tsDetectObject_getProperty

#ifndef TSMemory
#  define TSMemory THandle
#endif

typedef struct {
    const char* objType;        /**< Model Name, such as `face', `gesture', `eye' and `mouth' etc.. */
    void* data;                 /**< Pointer to the data buffer. */
} ModelData;

typedef struct {
    int n;                      /**< The length of the `datalist' */
    ModelData* datalist;
} ModelDataList;

typedef struct {
    unsigned int w;
    unsigned int h;
} TsSize;

enum {    
    TS_OT_FLAG_TK = 1,           //!< Tracking enable
    TS_OT_FLAG_MO = 2            //!< Tracking multiple objects
};
    
#ifndef __cplusplus
#   define SAK_DEFAULT(val)
#else
#   define SAK_DEFAULT(val) = val
extern "C" {
#endif

typedef THandle TSObjectDetector;

/** 
 * tsDetectObject_create Create a DetectObject object and return its handle.
 *
 * @param mode  0 detect, 1 single object track, 3  multi-object track
 *
 * @param ModelDataList  List of model data;
 * 
 * @return The SDK handle.
 */
DetectObject_EXPORTS  TSObjectDetector tsDetectObject_create(TUInt32 mode SAK_DEFAULT(0), const ModelDataList* data SAK_DEFAULT(0));

/** 
 * tsDetectObject_destroy Destroy the DetectObject object and release its memory.
 * 
 * @param pHandle 
 * 
 * @return 
 */
DetectObject_EXPORTS  TVoid tsDetectObject_destroy(TSObjectDetector pHandle);

/** 
 * tsDetectObject_setImage Add the image prepared to be detected on.
 * NOTE: After calling this API, the object information got by the calling of
 * tsDetectObject_detect will be lost.
 * 
 * @param hDetector  
 * @param image      Only NV21 be supported now.
 * @param angle      [0, 360). Positive for clockwise, negative for counter-clockwise.
 * 
 * @return TOK if successfully, otherwise please refer to terror.h.
 */
DetectObject_EXPORTS  TRESULT  tsDetectObject_setImage(TSObjectDetector hDetector, 
                                                       const TSOFFSCREEN* image,
                                                       TInt32 angle SAK_DEFAULT(0));

/** 
 * tsDetectObject_detect Detect the object belongs to the class specified by `className', 
 * such as "face", "eye", "mouth" and "smile" etc.
 * 
 * @param hDetector 
 * @param className  "face", "eye", "mouth", or "smile".
 * @param faceRect   The face rectangle. Pass NULL when detecting face.
 * 
 * @return If less than zero, failed, otherwise the number of the detected objects.
 */
DetectObject_EXPORTS  TInt32  tsDetectObject_detect(TSObjectDetector      hDetector, 
                                                    const char*  className,
                                                    const TRECT* faceRect SAK_DEFAULT(0));

/** 
 * tsDetectObject_reset Reset the detector's internal state to prepare for a new
 * tracking process.
 * 
 * @param h The detector.
 */
DetectObject_EXPORTS void tsDetectObject_reset(TSObjectDetector h);


/** 
 * tsDetectObject_object Get the `index'th object's position and size, which were
 * detected by the last calling of tsDetectObject_detect.
 * NOTE: The position is always related to the left-top point in the image specified
 * by the tsDetectObject_setImage.
 * 
 * @param hDetector 
 * @param index    [0, n), where `n' is the number of objects detected.
 * @param object : Position and size. NOTE the rectangle is left closed and right opened.
 * 
 * @return TFalse if failed, otherwise TTrue.
 */
DetectObject_EXPORTS  TBool  tsDetectObject_object(TSObjectDetector hDetector, 
                                                   TInt32 index, 
                                                   TRECT* object);

DetectObject_EXPORTS  TBool  tsDetectObject_objId(TSObjectDetector hDetector, 
                                                  TInt32 index, 
                                                  int* id);
/** 
 * tsDetectObject_count Get the count of the objects detected by the last calling
 * of tsDetectObject_detect.
 * 
 * @param hDetector
 * 
 * @return The number of detected objects. Failed, if less than zero.
 */
    
DetectObject_EXPORTS  TInt32  tsDetectObject_count(TSObjectDetector hDetector);

/// Spcify the searching windows' scale information.
/**
 * Given a TsWindowScale, `n' scales of the searching windows will be
 * generated as below by the detector,
 * 
 *   W0, W1, ..., Wn,
 *
 * where W0 = minSize, Wn = maxSize, n = levelNum.
 */
typedef struct {
    int levelNum;               //!< The number of searching windows scales.
    int minSize;                //!< Minimal searching window's size.
    int maxSize;                //!< Maximal searching window's size.
} TsWindowScale;

typedef TsWindowScale SakWindowScale;

/** 
 * tsDetectObject_setProperty Set the property of detector.
 * It should be called after the calling of tsDetectObject_setImage.
 *
 * Key         | Value Type     | Description
 * ----------- | -------------- | ---------------------------------------
 * MaxSize     | TsSize         | The maximum internal image's size.
 * WindowScale | TsWindowScale  | Specify the search window's scale info.
 * 
 * @param hDetector 
 * @param className : Such as "face".
 * @param key    : Property name, specified in the above table.
 * @param value  : A void pointer to the value. The value type refers to the
 *                 above table.
 * 
 * @return TFalse if failed, otherwise TTrue.
 */
DetectObject_EXPORTS  TBool  tsDetectObject_setProperty(TSObjectDetector      hDetector, 
                                                        const char*  className,
                                                        const char*  key,
                                                        TPVoid       value);

/** 
 * tsDetectObject_getProperty
 * 
 * @param hDetector 
 * @param className 
 * @param key 
 * @param value 
 * 
 * @return 
 */
DetectObject_EXPORTS  TRESULT tsDetectObject_getProperty(TSObjectDetector hDetector,
                                                         const TPChar  className,
                                                         const TPChar  key,
                                                         TPVoid        value);

#ifdef __cplusplus
}
#endif

#endif//TS_DETECT_OBJECT_H__
