//
// Created by Administrator on 2018/6/18 0018.
//

#ifndef ANDROIDOPENGL_MASTER_FACETUNE_JNI_H
#define ANDROIDOPENGL_MASTER_FACETUNE_JNI_H

#include "facetune/textureeffect.h"
#include <graphics/GLUtils.h>
#include <android/log.h>
#include <jni.h>
#include <graphics/Matrix.h>
#include "include/beautitune.h"
#define LOG_TAG "textureeffect"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)
#endif //ANDROIDOPENGL_MASTER_FACETUNE_JNI_H
