#include "jRect.h"

jRect::jRect(JNIEnv * env, jobject rect) {
	m_jni_env = env;
	m_rect = rect;
    jclass class_rect = env->GetObjectClass(rect);
    m_field_left = env->GetFieldID(class_rect, "left", "I");
    m_field_top = env->GetFieldID(class_rect, "top", "I");
    m_field_right = env->GetFieldID(class_rect, "right", "I");
    m_field_bottom = env->GetFieldID(class_rect, "bottom", "I");
}

int jRect::getLeft() {
	return m_jni_env->GetIntField(m_rect, m_field_left);
}

int jRect::getTop() {
	return m_jni_env->GetIntField(m_rect, m_field_top);
}

int jRect::getRight() {
	return m_jni_env->GetIntField(m_rect, m_field_right);
}

int jRect::getBottom() {
	return m_jni_env->GetIntField(m_rect, m_field_bottom);
}

void jRect::setLeft(int value) {
	m_jni_env->SetIntField(m_rect, m_field_left, value);
}

void jRect::setTop(int value) {
	m_jni_env->SetIntField(m_rect, m_field_top, value);
}

void jRect::setRight(int value) {
	m_jni_env->SetIntField(m_rect, m_field_right, value);
}

void jRect::setBottom(int value) {
	m_jni_env->SetIntField(m_rect, m_field_bottom, value);
}
