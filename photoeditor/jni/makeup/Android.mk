# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := Jnijpegdecode
LOCAL_SRC_FILES :=  lib/libJnijpegdecode.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := tsmakeuprt_jni
LOCAL_CFLAGS    := -Werror -fvisibility=hidden

LOCAL_C_INCLUDES := $(LOCAL_PATH)/inc\
		$(LOCAL_PATH)/include

LOCAL_SRC_FILES := \
		./utils/debug.cpp \
		./utils/BGRNV21.c \
		NV21Sampler_jni.cpp\
		tsmakeuprt_engine_jni.cpp\
		cosmetic.cpp\
		modeldata.cpp\
		loadpng.cpp \
		edit_bitmap_jni.cpp \
		jStyle.cpp \
		jRect.cpp
		
LOCAL_LIBPATH := -L$(LOCAL_PATH)/lib -L./lib 

LOCAL_LDLIBS    := -llog -landroid -ljnigraphics -ldl \
	-L$(LOCAL_PATH)/lib -lFacialOutline -lJnijpegdecode -ltsm_makeup -lfacebeautify -lpng -lgnustl_static -lplatformbase -lstdc++ 
ifeq ($(TARGET_ARCH_ABI), x86)
LOCAL_LIBPATH   := $(LOCAL_PATH)/libx86 
endif


include $(BUILD_SHARED_LIBRARY)
