#ifndef TS_FACIAL_OUTLINE_H__
#define TS_FACIAL_OUTLINE_H__

#include "tcomdef.h"
#include "terror.h"
#include "tsoffscreen.h"

#if defined(__GNUC__) || defined(__llvm__) || defined(__clang__)
#  ifdef SAK_FacialOutline_EXPORTS
#    define FacialOutline_EXPORTS __attribute__((visibility("default")))
#  else
#    define FacialOutline_EXPORTS
#  endif
#elif defined(_WINDOWS) || defined(WIN32)
#  ifdef SAK_FacialOutline_EXPORTS
#    define FacialOutline_EXPORTS __declspec(dllexport)
#  elif defined(FacialOutline_STATIC)
#    define FacialOutline_EXPORTS
#  else
#    define FacialOutline_EXPORTS __declspec(dllimport)
#  endif
#else
#  error "The compiler is not supported."
#endif

#ifndef TSMemory
#  define TSMemory THandle
#endif

typedef struct {
    unsigned int w;
    unsigned int h;
} TSFO_Size;

enum {
    TS_FO_TYPE_DUMB = 0,
    TS_FO_TYPE_REALTIME = 1,
    TS_FO_TYPE_FINE = 2
};

// DEPRECATED!
enum {
    TS_FO_FLAG_TK = 1
};

typedef THandle TSFacialOutline;

/**
 * To compatiable with the old version.
 */
#define sakFacialTrack_create      tsFacialOutline_create
#define sakFacialTrack_destroy     tsFacialOutline_destroy
#define sakFacialTrack_figure      tsFacialOutline_figure
#define sakFacialTrack_figure_ex   tsFacialOutline_figure_ex
#define sakFacialTrack_reset       tsFacialOutline_reset
#define sakFacialTrack_getProperty tsFacialOutline_getProperty
#define sakFacialTrack_setProperty tsFacialOutline_setProperty

#ifndef __cplusplus
#   define SAK_DEFAULT(val)
#else
#   define SAK_DEFAULT(val) = val
extern "C" {
#endif

/** 
 * Create the facial track handle.
 * 
 * @param data The pointer pointed to the model data.
 * 
 * @return 0 failed, otherwise the handle of the facial track.
 */
FacialOutline_EXPORTS TSFacialOutline tsFacialOutline_create(TUInt32 engine SAK_DEFAULT(TS_FO_TYPE_FINE), void* data SAK_DEFAULT(0));

/** 
 * Destory the handle and it's content.
 * 
 * @param ft The handle of the facial track.
 */
FacialOutline_EXPORTS void    tsFacialOutline_destroy(TSFacialOutline ft);

/** 
 * Get the facial marks' positions.
 * 
 * @param ft        The handle.
 * @param image 
 * @param faceRect  The rectangle of the face in the image. But, the zero
 *                  rectangle will trigger the internal face detection/tracking
 *                  function.
 * @param landmarks The marks's positions. Allocated by the caller. If NULL, the
 *                  result landmarks will not be copied out.
 * 
 * @return TOK if successed, otherwise failed.
 */
FacialOutline_EXPORTS TRESULT tsFacialOutline_figure(TSFacialOutline ft, TSOFFSCREEN *image, TRECT faceRect, TDouble *landmarks, int angle SAK_DEFAULT(0));

/** 
 * @brief Get the facial landmarks.
 * 
 * @param ft        The handle.
 * @param image 
 * @param faceRect  The face rectangle.
 * @param leye      The center point of the left eye.
 * @param reye      The center point of the right eye.
 * @param mouth     The center point of the mouth.
 * @param landmarks The marks's positions. Allocated by the caller. If NULL, the
 *                  result landmarks will not be copied out.
 * 
 * @return TOK is successed, otherwise failed.
 */
FacialOutline_EXPORTS TRESULT tsFacialOutline_figure_ex(TSFacialOutline ft, TSOFFSCREEN *image, TRECT faceRect, TPOINT leye, TPOINT reye, TPOINT mouth, TDouble *landmarks);

/** 
 * If using the internal detection/tracking function, need not call this API. Otherwise,
 * call this API after each calling of tsFacialOutline_figure.
 * @param ft 
 */
FacialOutline_EXPORTS void    tsFacialOutline_reset(TSFacialOutline ft);

/** 
 * Get value of the specified key.
 * 
 * | Key Name            | Value Type   | Description                       |
 * |-------------------- | ------------ | ----------------------------------|
 * | landmarks           | void*        | Landmark points: (x₁,y₁,x₂,y₂,⋯). |
 * | landmarks-number    | int*         | The number of the above points.   |
 * | landmark-value-type | const char** | The value type of (x,y).          |
 * | landmark-type       | const char** | The type of the landmarks set.    |
 * | BENM-mask           | uchar*       | Mask of brow, eye, nose and mouth.|
 * | face-rect           | TRECT        | The face rectangle.               |
 * 
 * @param hft 
 * @param key 
 * @param value OUT
 * 
 * @return TOK is successfully, otherwise failed.
 */
FacialOutline_EXPORTS TRESULT tsFacialOutline_getProperty(TSFacialOutline hft, TPCChar key, TPVoid value);

/** 
 * Set a key-value.
 *
 * | Key Name            | Value Type   | Description                       |
 * |-------------------- | ------------ | ----------------------------------|
 * | landmark-type       | const char*  | The type of the landmarks set.    |
 * | max-size            | TSFO_Size*   | The maximum internal image's size.|
 * 
 * @param hft 
 * @param key 
 * @param value IN
 * 
 * @return 
 */
FacialOutline_EXPORTS TRESULT  tsFacialOutline_setProperty(TSFacialOutline hft, TPCChar key, const void* value);

/** 
 * Get the landmarks point number of a organ, e.g, lefteye, nose and mouth etc..
 * 
 * @param fo      
 * @param organ  The organ name, includes {"lefteye", "righteye", "mouth", "nose",
 *               "mouth_cavity", "leftbrow", "rightbrow", "temple", "chin",
 *               "leftjaw", "rightjaw"}.
 * 
 * @return The number of the landmarks number.
 */
FacialOutline_EXPORTS int     tsFacialOutline_getLanmarksNumberOf(TSFacialOutline fo, TPCChar organ);

/** 
 * Get the landmarks of a organ.
 * 
 * @param fo 
 * @param organ  The organ name, such as 'lefteye' etc..
 * @param marks  The landmarks of the organ, x0,y0,x1,y1,...
 * 
 * @return TOK if successfully.
 */
FacialOutline_EXPORTS TRESULT tsFacialOutline_getLandmarksOf(TSFacialOutline fo, TPCChar organ, TDouble* marks);


#ifdef __cplusplus
}
#endif

#endif//TS_FACIAL_OUTLINE_H__
