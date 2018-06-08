#include "jPoint.h"

//JNIEnv* jPoint::m_jni_env = 0;
//jfieldID jPoint::m_field_x = 0;
//jfieldID jPoint::m_field_y = 0;

jPoint::jPoint(JNIEnv * env, jobject point) {
//	if (!m_jni_env) {
		jclass class_point = env->GetObjectClass(point);
		m_field_x = env->GetFieldID(class_point, "x", "I");
		m_field_y = env->GetFieldID(class_point, "y", "I");
		m_jni_env = env;
//	}
	m_point = point;
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

