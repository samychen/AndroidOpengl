
#include "beauty_jni.h"

textureeffect *textureObj;

extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeSurfaceCreate(JNIEnv *env, jclass type,
                                                                  jint picwidth_,
                                                                  jint picheight_,
                                                                  jint texID,jint programID) {
    if (textureObj) {
        delete textureObj;
        textureObj = NULL;
    }
    textureObj = new textureeffect();
    textureObj->picwidth = picwidth_;
    textureObj->picheight = picheight_;
    textureObj->srcTexure = texID;
    textureObj->mPointProgramHandle = programID;
    if (!programID) {
        LOGE("Could not create program");
    }
    textureObj->createFrameBuffer();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeSurfaceChange(JNIEnv *env, jclass type,
                                                                     jint left_, jint top_,
                                                                     jint right_, jint bottom_,
                                                                     jint width_, jint height_) {
    if (textureObj) {
        textureObj->change(left_, top_, right_, bottom_, width_, height_);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeRender(JNIEnv *env,
                                                              jclass type,
                                                              jfloat x, jfloat y,jfloat radius) {
    if (textureObj) {
        int ret = textureObj->renderCenter({x, y}, radius);
        textureObj->mCompareFlag = 0;
        LOGE("ret=%d", ret);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeDrawPath(JNIEnv *env,
                                                                jclass type,
                                                                jfloat x, jfloat y,jfloat radius) {
    if (textureObj) {
        textureObj->initEffect = 1;
        int ret = textureObj->renderCenter({x, y}, radius);
        textureObj->mCompareFlag = 0;
        LOGE("ret=%d", ret);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeDrawFrame(JNIEnv *env,
                                                                 jclass type) {

    if (textureObj) {
        textureObj->draw();
    }
}

void unitAndinitEffect() {//之前是否选择其他按钮
    if (textureObj->hasEffect == 1) {
        textureObj->releaseEffect();
    }
    textureObj->initGLEffect();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativereleaseEffect(JNIEnv *env,
                                                                     jclass type,
                                                                     jint effecttype_) {
    if (textureObj) {
        LOGE("release");
        textureObj->initEffect = 1;
        if (effecttype_ == 1) {
            unitAndinitEffect();
            if (textureObj->hasEffect != 1) {
                textureObj->copyBuffer();
            }
            textureObj->ProType = TeethWhite;
            textureObj->bsWork = Paint;
            textureObj->hasEffect = 1;
        } else if (effecttype_ == 2) {
            unitAndinitEffect();
            if (textureObj->hasEffect != 1) {
                textureObj->copyBuffer();
            }
            textureObj->ProType = Smooth;
            textureObj->bsWork = Paint;
            textureObj->hasEffect = 1;
            textureObj->isMoreSmooth = 0;
        } else if (effecttype_ == 3) {
            unitAndinitEffect();
            if (textureObj->hasEffect != 1) {
                textureObj->copyBuffer();
            }
            textureObj->ProType = Smooth;
            textureObj->bsWork = Paint;
            textureObj->hasEffect = 1;
            textureObj->isMoreSmooth = 1;
        } else if (effecttype_ == 4) {
            unitAndinitEffect();
            if (textureObj->hasEffect != 1) {
                textureObj->copyBuffer();
            }
            textureObj->ProType = Detail;
            textureObj->bsWork = Paint;
            textureObj->hasEffect = 1;
        } else if (effecttype_ == 5) {
            //之前是否选择其他按钮
            if (textureObj->hasEffect == 0) {
                textureObj->initEffect = 0;
            } else {
                textureObj->bsWork = Erase;
            }
        } else if (effecttype_ == 6) {
            unitAndinitEffect();
            textureObj->copySrcBuffer();
        } else if (effecttype_ == 7) {
            textureObj->copyBuffer();
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeCompare(JNIEnv *env, jclass type,
                                                               jint actionup_) {
    if (textureObj) {
        textureObj->mCompareFlag = actionup_;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeRemoveLastEffect(JNIEnv *env, jclass type) {
    if (textureObj) {
        textureObj->initEffect = 1;
        textureObj->bsWork = Erase;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_nativeSetPaint(JNIEnv *env, jclass type) {
    if (textureObj) {
        textureObj->initEffect = 1;
        textureObj->bsWork = Paint;
    }
}
jobject globalObj = 0;
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_init(JNIEnv *env, jobject instance) {
    JavaVM *g_jvm = NULL;
    env->GetJavaVM(&g_jvm);
    globalObj = env->NewGlobalRef(instance);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ufotosoft_facetune_gles_EffectRender_destroy(JNIEnv *env, jobject instance) {
    if (textureObj) {
        textureObj->destroyTexture();
    }
    delete textureObj;
    textureObj = NULL;
    env->DeleteGlobalRef(globalObj);
    globalObj = 0;
}