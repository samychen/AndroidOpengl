LOCAL_PATH :=$(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := bspatch
LOCAL_SRC_FILES :=  lib/libbspatch.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/inc
LOCAL_MODULE :=tsutils

LOCAL_SRC_FILES := \
	$(LOCAL_PATH)/Shader_util_jni.cpp \
    $(LOCAL_PATH)/image_rotate_jni.cpp \
	$(LOCAL_PATH)/zoom.c \
	$(LOCAL_PATH)/JpegUtils.cpp \
	$(LOCAL_PATH)/jPoint.cpp\
	$(LOCAL_PATH)/BGRNV21.c\
	$(LOCAL_PATH)/debug.cpp\
	$(LOCAL_PATH)/image-util.cpp\
	$(LOCAL_PATH)/imageprocess.cpp\
	$(LOCAL_PATH)/TSJpeg.cpp\
	$(LOCAL_PATH)/TSAlgorithm.cpp\
	$(LOCAL_PATH)/TSDl.cpp\
	$(LOCAL_PATH)/CpuABI.cpp
	
LOCAL_CFLAGS := -fpermissive

ifeq ($(TARGET_ARCH_ABI), armeabi)
LOCAL_LDLIBS += -fuse-ld=bfd -llog -ljnigraphics -lGLESv2 -lGLESv1_CM \
	-L$(LOCAL_PATH)/lib/arm \
            $(LOCAL_PATH)/lib/arm/libjpeg.so \
            $(LOCAL_PATH)/lib/arm/libpng.a \
            $(LOCAL_PATH)/lib/arm/libz.so
endif
ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_LDLIBS += -fuse-ld=bfd -llog -ljnigraphics -lGLESv2 -lGLESv1_CM \
	-L$(LOCAL_PATH)/lib/armeabi-v7a \
            $(LOCAL_PATH)/lib/armeabi-v7a/libjpegts.a \
            $(LOCAL_PATH)/lib/armeabi-v7a/libpng.a \
            $(LOCAL_PATH)/lib/armeabi-v7a/libz.so
endif

LOCAL_STATIC_LIBRARIES := cpufeatures
include $(BUILD_SHARED_LIBRARY)
$(call import-module,android/cpufeatures)

#include $(LOCAL_PATH)/android_forjpeg.txt

