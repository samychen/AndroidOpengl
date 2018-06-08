#include "jPoint.h"

jPoint::jPoint(JNIEnv * env, jobject point) {
	m_jni_env = env;
	m_point = point;
    jclass class_point = env->GetObjectClass(point);
    m_field_x = env->GetFieldID(class_point, "x", "I");
    m_field_y = env->GetFieldID(class_point, "y", "I");
}

int jPoint::getX() {
	return m_jni_env->GetIntField(m_point, m_field_x);
}

int jPoint::getY() {
	return m_jni_env->GetIntField(m_point, m_field_y);
}

void jPoint::setX(int value) {
	m_jni_env->SetIntField(m_point, m_field_x, value);
}

void jPoint::setY(int value) {
	m_jni_env->SetIntField(m_point, m_field_y, value);
}

