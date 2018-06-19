
#include "facetune_jni.h"

textureeffect *textureeffectobj;

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeSurfaceCreate(JNIEnv *env, jclass type,
                                                                     jobject assetManager,
                                                                     jint picwidth_,
                                                                     jint picheight_,
                                                                     jstring picpath_) {
    const char *picpath = env->GetStringUTFChars(picpath_, 0);
    GLUtils::setEnvAndAssetManager(env, assetManager);
    if (textureeffectobj) {
        delete textureeffectobj;
        textureeffectobj = NULL;
    }
    textureeffectobj = new textureeffect();
    textureeffectobj->picwidth = picwidth_;
    textureeffectobj->picheight = picheight_;
    textureeffectobj->picpath = (char *) picpath;
    textureeffectobj->create();
    env->ReleaseStringUTFChars(picpath_, picpath);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeSurfaceChange(JNIEnv *env,
                                                                     jclass type,
                                                                     jint left_, jint top_,
                                                                     jint right_, jint bottom_,
                                                                     jint width_, jint height_) {
    if (textureeffectobj) {
        textureeffectobj->change(left_, top_, right_, bottom_, width_, height_);
        textureeffectobj->createFrameBuffer();
    }

}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeRender(JNIEnv *env,
                                                              jclass type,
                                                              jfloat x, jfloat y) {
    if (textureeffectobj) {
        int ret = textureeffectobj->renderCenter({x, y}, 15.0f);
        textureeffectobj->mCompareFlag = 0;
        LOGE("ret=%d", ret);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeDrawPath(JNIEnv *env,
                                                                jclass type,
                                                                jfloat x, jfloat y) {
    if (textureeffectobj) {
        textureeffectobj->initEffect = 1;
        int ret = textureeffectobj->renderCenter({x, y}, 15.0f);
        textureeffectobj->mCompareFlag = 0;
        LOGE("ret=%d", ret);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeDrawFrame(JNIEnv *env,
                                                                 jclass type) {

    if (textureeffectobj) {
        textureeffectobj->draw();
    }
}

void unitEffect();

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativereleaseEffect(JNIEnv *env,
                                                                     jclass type,
                                                                     jint effecttype_) {
    if (textureeffectobj) {
        LOGE("release");
        textureeffectobj->initEffect = 1;

        if (effecttype_ == 1) {
            unitEffect();
            textureeffectobj->ProType = TeethWhite;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;

            textureeffectobj->copyBuffer();
        } else if (effecttype_ == 2) {
            unitEffect();
            textureeffectobj->ProType = Smooth;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_ == 3) {
            unitEffect();
            textureeffectobj->ProType = Smooth;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->isMoreSmooth = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_ == 4) {
            unitEffect();
            textureeffectobj->ProType = Detail;
            textureeffectobj->bsWork = Paint;
            textureeffectobj->hasEffect = 1;
            textureeffectobj->copyBuffer();
        } else if (effecttype_ == 5) {
            //之前是否选择其他按钮
            if (textureeffectobj->hasEffect == 0) {
                textureeffectobj->initEffect = 0;
            } else {
                textureeffectobj->bsWork = Erase;
            }
        } else if (effecttype_ == 6) {
            unitEffect();
            textureeffectobj->copySrcBuffer();
        } else if (effecttype_ == 7) {
            textureeffectobj->copyBuffer();
        }
    }
}

void unitEffect() {//之前是否选择其他按钮
    if (textureeffectobj->hasEffect == 1) {
        textureeffectobj->releaseEffect();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeCompare(JNIEnv *env, jclass type,
                                                               jint actionup_) {
    if (textureeffectobj) {
        textureeffectobj->mCompareFlag = actionup_;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeRemoveLastEffect(JNIEnv *env, jclass type) {
    if (textureeffectobj) {
        textureeffectobj->initEffect = 1;
        textureeffectobj->bsWork = Erase;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_nativeSetPaint(JNIEnv *env, jclass type) {
    if (textureeffectobj) {
        textureeffectobj->initEffect = 1;
        textureeffectobj->bsWork = Paint;
    }
}
jobject globalObj = 0;
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_init(JNIEnv *env, jobject instance) {
    JavaVM *g_jvm = NULL;
    env->GetJavaVM(&g_jvm);
    globalObj = env->NewGlobalRef(instance);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_learnopengles_android_gles_EffectRender_destroy(JNIEnv *env, jobject instance) {
    delete textureeffectobj;
    textureeffectobj = NULL;
    env->DeleteGlobalRef(globalObj);
    globalObj = 0;
}