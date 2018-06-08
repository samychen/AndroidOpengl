#ifndef __J_POINT_H__
#define __J_POINT_H__

#include <jni.h>

class jPoint {
public:
	jPoint(JNIEnv* env, jobject point);
	int getX();
	int getY();
	void setX(int x);
	void setY(int y);
private:
	jobject m_point;
	JNIEnv* m_jni_env;
	jfieldID m_field_x, m_field_y;
};

#endif
