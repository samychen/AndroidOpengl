#
# Copyright (C) 2012 Thundersoft Corporation
# All rights Reserved
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional


# This is the target being built.
LOCAL_MODULE:= makeupengine

# All of the source files that we will compile.
LOCAL_SRC_FILES:= \
./ts_makeup_engine.cpp\
./MakeupEngine.cpp\
./jPoint.cpp\
./jRect.cpp\
./tool.cpp \
./makeup.cpp \
./utils/debug.cpp \
./utils/BGRNV21.c \
./edit_bitmap_jni.cpp \
./face_detect_jni.cpp \
./facial_marks_track_jni.cpp \
./NV21Sampler_jni.cpp

# No static libraries.
LOCAL_LDLIBS += -llog -ljnigraphics

# CAUTION:
#   Please pay more attention to the ORDER of the static library
#-lmakeup -lfacewhiten -ltseffect -ldeblemish -lwarpface -lfaceskin -lfd -lplatformbase
    LOCAL_LDFLAGS := \
    	$(LOCAL_PATH)/libs/libFacialOutline.a \
    	$(LOCAL_PATH)/libs/libblackbeautify.a \
        $(LOCAL_PATH)/libs/libfacebeautify.a \
        $(LOCAL_PATH)/libs/libfacewhiten.a \
        $(LOCAL_PATH)/libs/libdeblemish.a \
        $(LOCAL_PATH)/libs/libwarpface.a \
        $(LOCAL_PATH)/libs/libfaceskin.a \
        $(LOCAL_PATH)/libs/libfacetracking.a \
        $(LOCAL_PATH)/libs/libplatformbase.a \
        $(LOCAL_PATH)/libs/libgnustl_static.a \
        $(LOCAL_PATH)/libs/libfacedenoise.a

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \
     $(LOCAL_PATH)/inc \
     $(LOCAL_PATH)/gpuprocess/include \

     #$(LOCAL_PATH)/../tesseract/liblept \

# Don't prelink this library.  For more efficient code, you may want
# to add this library to the prelink map and set this to true. However,
# it's difficult to do this for applications that are not supplied as
# part of a system image.

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

