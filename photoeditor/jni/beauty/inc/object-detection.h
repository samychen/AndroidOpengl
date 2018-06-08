#ifndef SAK_OBJECT_DETECTION_H__
#define SAK_OBJECT_DETECTION_H__

#include "tsoffscreen.h"
#include "tmem.h"

typedef struct SakObjectInfo {
	int   x;
	int   y;
	int   width;
	int   height;
	float value;
} SakObjectInfo;

#ifdef __cplusplus
extern "C" {
#endif

THandle     sakObjectDetect_create     (THandle memHandle, const char* className);
void        sakObjectDetect_destroy    (THandle *pHandle);
int         sakObjectDetect_detect     (THandle handle, LPTSOFFSCREEN image);
int         sakObjectDetect_count      (THandle handle);
bool        sakObjectDetect_objectInfo (THandle handle, int index, SakObjectInfo* obj);

#ifdef __cplusplus
}
#endif

#endif//SAK_OBJECT_DETECTION_H__
