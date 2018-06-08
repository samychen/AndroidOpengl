#ifndef SAK_FACE_DETECTION_H__
#define SAK_FACE_DETECTION_H__

#include "object-detection.h"

#define IN
#define OUT

typedef struct SakFaceInfo {
	TRECT face;
	TRECT eyes[2];
	TRECT mouth;
	int   eyeCount;
	int   mouthCount;
} SakFaceInfo;

typedef enum SakFDProfile {
	FD_PROFILE_INVALID,			/**< DONT USE IT. The invalid profile. */
	FD_PROFILE_PORTRAIT,		/**< The default. */
	FD_PROFILE_COUPLE,
	FD_PROFILE_FAMILY,
	FD_PROFILE_MASS,
	FD_PROFILE_NUM				/**< DONT USE IT. The number of the profiles. */
} SakFDProfile;

#ifdef __cplusplus
extern "C" {
#endif

////////////////////////////////////////////////////////////////////////////////
///////////////////////      Constructor/Destructor      ///////////////////////
////////////////////////////////////////////////////////////////////////////////

THandle      sakFaceDetect_create     (IN THandle memHandle);
void         sakFaceDetect_destroy    (IN OUT THandle *pHandle);

////////////////////////////////////////////////////////////////////////////////
//////////////////////////      Detector method       //////////////////////////
////////////////////////////////////////////////////////////////////////////////

/** 
 * sakFaceDetect_detect Detect the faces, including eye and mouth, in the input
 * __image__.
 * 
 * @param handle IN/OUT The detector handle.
 * @param image  IN The input image.
 * 
 * @return The number of the detected faces.
 */
int          sakFaceDetect_detect     (THandle handle, LPTSOFFSCREEN image);

////////////////////////////////////////////////////////////////////////////////
/////////////////////////////      Properties      /////////////////////////////
////////////////////////////////////////////////////////////////////////////////

/** 
 * sakFaceDetect_count Get the number of the detected faces.
 * 
 * @param handle  IN The detector handle.
 * 
 * @return The number of the detected faces. If the __
 */
int          sakFaceDetect_count      (THandle handle);

/** 
 * sakFaceDetect_objectInfo Get the specified face infomation.
 * 
 * @param handle  IN/OUT The detector handle.
 * @param index   The index of the face, whose range is [0, __count__) where
 *                __count__ can be gotten by sakFaceDetect_count.
 * @param face    OUT Fill the specified face information.
 * 
 * @return If the __index__ is invalid or __face__ is NULL, or __handle__ is NULL
 * or invalid, TFalse is returned. Otherwise, TTrue.
 */
TBool         sakFaceDetect_objectInfo (THandle handle, int index, SakFaceInfo* face);

/** 
 * sakFaceDetect_profile Get the detection profile.
 * 
 * @param handle IN The detector handle.
 * 
 * @return The detection profile. If __handle__ is invalid, FD_PROFILE_INVALID is
 * returned.
 */
SakFDProfile sakFaceDetect_profile    (THandle handle);

/** 
 * sakFaceDetect_setProfile Specify the detection profile.
 * 
 * @param handle  IN/OUT The detector handle.
 * @param profile The profile, please refer to the definition of the SakFDProfile.
 */
void         sakFaceDetect_setProfile (THandle handle, SakFDProfile profile);

#ifdef __cplusplus
}
#endif

#endif//SAK_FACE_DETECTION_H__
