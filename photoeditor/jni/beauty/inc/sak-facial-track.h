#ifndef SAK_FACIAL_TRACK_H__
#define SAK_FACIAL_TRACK_H__

#include "tcomdef.h"
#include "terror.h"
#include "tmem.h"
#include "tsoffscreen.h"

#ifndef SAK_STATIC  // Dynamic Library
#   if (defined(_WIN32) || defined(_WIN64)) && !defined(__GNUC__)
#       ifdef SAK_DLL
#           define SAK_EXPORTS __declspec(dllexport)
#       else
#           define SAK_EXPORTS __declspec(dllimport)
#       endif
#   elif (defined(linux) || defined(__linux) || defined(__ANDROID__)) && defined(__GNUC__)
#      define SAK_EXPORTS __attribute__((visibility("default")))
#   else
#       error "The OS is not supported."
#   endif
#else  // Static Library
#   define SAK_EXPORTS
#endif

#ifndef __cplusplus
#   define SAK_DEFAULT(val)
#else
#   define SAK_DEFAULT(val) = val
extern "C" {
#endif

/**
 * The index of the facial marks.
 */
enum {
    kSAK_FACIAL_TRACK_TEMPLE_L0 = 0,
    kSAK_FACIAL_TRACK_TEMPLE_L1 = 1,
    kSAK_FACIAL_TRACK_TEMPLE_L2 = 2,
    kSAK_FACIAL_TRACK_TEMPLE_R0 = 16,
    kSAK_FACIAL_TRACK_TEMPLE_R1 = 15,
    kSAK_FACIAL_TRACK_TEMPLE_R2 = 14,
    kSAK_FACIAL_TRACK_JAW_L0 = 3,
    kSAK_FACIAL_TRACK_JAW_L1 = 4,
    kSAK_FACIAL_TRACK_JAW_R0 = 13,
    kSAK_FACIAL_TRACK_JAW_R1 = 12,
    kSAK_FACIAL_TRACK_CHIN_L0 = 5,
    kSAK_FACIAL_TRACK_CHIN_L1 = 6,
    kSAK_FACIAL_TRACK_CHIN_L2 = 7,
    kSAK_FACIAL_TRACK_CHIN_CENTER = 8,
    kSAK_FACIAL_TRACK_CHIN_R0 = 11,
    kSAK_FACIAL_TRACK_CHIN_R1 = 10,
    kSAK_FACIAL_TRACK_CHIN_R2 = 9,
    kSAK_FACIAL_TRACK_EYEBROW_LL = 17,
    kSAK_FACIAL_TRACK_EYEBROW_LLC = 18,
    kSAK_FACIAL_TRACK_EYEBROW_LC = 19,
    kSAK_FACIAL_TRACK_EYEBROW_LRC = 20,
    kSAK_FACIAL_TRACK_EYEBROW_LR = 21,
    kSAK_FACIAL_TRACK_EYEBROW_RL = 22,
    kSAK_FACIAL_TRACK_EYEBROW_RLC = 23,
    kSAK_FACIAL_TRACK_EYEBROW_RC = 24,
    kSAK_FACIAL_TRACK_EYEBROW_RRC = 25,
    kSAK_FACIAL_TRACK_EYEBROW_RR = 26,
    kSAK_FACIAL_TRACK_CANTHUS_LL = 36,
    kSAK_FACIAL_TRACK_CANTHUS_LR = 39,
    kSAK_FACIAL_TRACK_CANTHUS_RL = 42,
    kSAK_FACIAL_TRACK_CANTHUS_RR = 45,
    kSAK_FACIAL_TRACK_EYE_LEFT_UP0 = 37,
    kSAK_FACIAL_TRACK_EYE_LEFT_UP1 = 38,
    kSAK_FACIAL_TRACK_EYE_LEFT_DOWN0 = 41,
    kSAK_FACIAL_TRACK_EYE_LEFT_DOWN1 = 40,
    kSAK_FACIAL_TRACK_EYE_RIGHT_UP0 = 43,
    kSAK_FACIAL_TRACK_EYE_RIGHT_UP1 = 44,
    kSAK_FACIAL_TRACK_EYE_RIGHT_DOWN0 = 47,
    kSAK_FACIAL_TRACK_EYE_RIGHT_DOWN1 = 46,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE0 = 27,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE1 = 28,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE2 = 29,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE3 = 30,
    kSAK_FACIAL_TRACK_NOSE_CENTER = 33,
    kSAK_FACIAL_TRACK_NOSTRIL_L0 = 31,
    kSAK_FACIAL_TRACK_NOSTRIL_L1 = 32,
    kSAK_FACIAL_TRACK_NOSTRIL_R0 = 35,
    kSAK_FACIAL_TRACK_NOSTRIL_R1 = 34,
    kSAK_FACIAL_TRACK_LIP_CORNER_LEFT = 48,
    kSAK_FACIAL_TRACK_LIP_CORNER_RIGHT = 54,
    kSAK_FACIAL_TRACK_LIP_UP0 = 49,
    kSAK_FACIAL_TRACK_LIP_UP1 = 50,
    kSAK_FACIAL_TRACK_LIP_UP = 51,
    kSAK_FACIAL_TRACK_LIP_UP2 = 52,
    kSAK_FACIAL_TRACK_LIP_UP3 = 53,
    kSAK_FACIAL_TRACK_LIP_DOWN0 = 59,
    kSAK_FACIAL_TRACK_LIP_DOWN1 = 58,
    kSAK_FACIAL_TRACK_LIP_DOWN = 57,
    kSAK_FACIAL_TRACK_LIP_DOWN2 = 56,
    kSAK_FACIAL_TRACK_LIP_DOWN3 = 55,
    kSAK_FACIAL_TRACK_LIP_INNER_UP = 61,
    kSAK_FACIAL_TRACK_LIP_INNER_DOWN = 64,
    kSAK_FACIAL_TRACK_LIP_INNER_UP0 = 60,
    kSAK_FACIAL_TRACK_LIP_INNER_UP1 = 62,
    kSAK_FACIAL_TRACK_LIP_INNER_DOWN0 = 65,
    kSAK_FACIAL_TRACK_LIP_INNER_DOWN1 = 63,
    kSAK_FACIAL_TRACK_NUM = 66                /**< The total number of facial marks. */
};

static int s_sak_ft_lefteye[] = {
    kSAK_FACIAL_TRACK_CANTHUS_LL,
    kSAK_FACIAL_TRACK_EYE_LEFT_UP0,
    kSAK_FACIAL_TRACK_EYE_LEFT_UP1,
    kSAK_FACIAL_TRACK_CANTHUS_LR,
    kSAK_FACIAL_TRACK_EYE_LEFT_DOWN1,
    kSAK_FACIAL_TRACK_EYE_LEFT_DOWN0
};
static int s_sak_ft_righteye[] = {
    kSAK_FACIAL_TRACK_CANTHUS_RL,
    kSAK_FACIAL_TRACK_EYE_RIGHT_UP0,
    kSAK_FACIAL_TRACK_EYE_RIGHT_UP1,
    kSAK_FACIAL_TRACK_CANTHUS_RR,
    kSAK_FACIAL_TRACK_EYE_RIGHT_DOWN1,
    kSAK_FACIAL_TRACK_EYE_RIGHT_DOWN0
};
static int s_sak_ft_nose[] = {
    kSAK_FACIAL_TRACK_NOSE_BRIDGE0,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE1,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE2,
    kSAK_FACIAL_TRACK_NOSE_BRIDGE3,
    kSAK_FACIAL_TRACK_NOSTRIL_L0,
    kSAK_FACIAL_TRACK_NOSTRIL_L1,
    kSAK_FACIAL_TRACK_NOSE_CENTER,
    kSAK_FACIAL_TRACK_NOSTRIL_R1,
    kSAK_FACIAL_TRACK_NOSTRIL_R0
};
static int s_sak_ft_mouth[] = {
    kSAK_FACIAL_TRACK_LIP_CORNER_LEFT,
    kSAK_FACIAL_TRACK_LIP_UP0,
    kSAK_FACIAL_TRACK_LIP_UP1,
    kSAK_FACIAL_TRACK_LIP_UP,
    kSAK_FACIAL_TRACK_LIP_UP2,
    kSAK_FACIAL_TRACK_LIP_UP3,
    kSAK_FACIAL_TRACK_LIP_CORNER_RIGHT,
    kSAK_FACIAL_TRACK_LIP_DOWN3,
    kSAK_FACIAL_TRACK_LIP_DOWN2,
    kSAK_FACIAL_TRACK_LIP_DOWN,
    kSAK_FACIAL_TRACK_LIP_DOWN1,
    kSAK_FACIAL_TRACK_LIP_DOWN0
};
static int s_sak_ft_leftbrow[] = {
    kSAK_FACIAL_TRACK_EYEBROW_LL,
    kSAK_FACIAL_TRACK_EYEBROW_LLC,
    kSAK_FACIAL_TRACK_EYEBROW_LC,
    kSAK_FACIAL_TRACK_EYEBROW_LRC,
    kSAK_FACIAL_TRACK_EYEBROW_LR
};
static int s_sak_ft_rightbrow[] = {
    kSAK_FACIAL_TRACK_EYEBROW_RL,
    kSAK_FACIAL_TRACK_EYEBROW_RLC,
    kSAK_FACIAL_TRACK_EYEBROW_RC,
    kSAK_FACIAL_TRACK_EYEBROW_RRC,
    kSAK_FACIAL_TRACK_EYEBROW_RR
};

/** 
 * Create the facial track handle.
 * 
 * @param memHandle It's ignored now. Just passing zero is OK.
 * 
 * @return 0 failed, otherwise the handle of the facial track.
 */
SAK_EXPORTS THandle sakFacialTrack_create(THandle memHandle);

/** 
 * Destory the handle and it's content.
 * 
 * @param ft The handle of the facial track.
 */
SAK_EXPORTS void    sakFacialTrack_destroy(THandle* ft);

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
SAK_EXPORTS TRESULT sakFacialTrack_figure(THandle ft, TSOFFSCREEN *image, TRECT faceRect, long *landmarks);

/** 
 * If using the internal detection/tracking function, need not call this API. Otherwise,
 * call this API after each calling of sakFacialTrack_figure.
 * @param ft 
 */
SAK_EXPORTS void    sakFacialTrack_reset(THandle ft);

/** 
 * Get value of the specified key.
 * 
 * |---------------------+--------------+-----------------------------------|
 * | Key Name            | Value Type   | Description                       |
 * |---------------------+--------------+-----------------------------------|
 * | landmarks           | void*        | Landmark points: (x₁,y₁,x₂,y₂,⋯). |
 * | landmarks-number    | int*         | The number of the above points.   |
 * | landmark-value-type | const char** | The value type of (x,y).          |
 * | landmark-type       | const char** | The type of the landmarks set.    |
 * | BENM-mask           | uchar*       | Mask of brow, eye, nose and mouth.|
 * | face-rect           | TRECT        | The face rectangle.               |
 * |---------------------+--------------+-----------------------------------|
 * 
 * @param hft 
 * @param key 
 * @param value OUT
 * 
 * @return TOK is successfully, otherwise failed.
 */
SAK_EXPORTS TRESULT sakFacialTrack_getProperty(THandle hft, const TPChar key, TPVoid value);

/** 
 * Set a key-value.
 *
 * |---------------------+--------------+-----------------------------------| 
 * | Key Name            | Value Type   | Description                       |
 * |---------------------+--------------+-----------------------------------|
 * | landmark-type       | const char*  | The type of the landmarks set.    |
 * |---------------------+--------------+-----------------------------------|
 * 
 * @param hft 
 * @param key 
 * @param value IN
 * 
 * @return 
 */
SAK_EXPORTS void    sakFacialTrack_setProperty(THandle hft, const TPChar key, const void* value);


#ifdef __cplusplus
}
#endif

#endif//SAK_FACIAL_TRACK_H__
