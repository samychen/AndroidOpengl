#ifndef __J_RECT_H__
#define __J_RECT_H__

#include <jni.h>

class jRect {
public:
	jRect(JNIEnv* env, jobject rect);
	int getLeft();
	int getTop();
	int getRight();
	int getBottom();
	void setLeft(int left);
	void setTop(int top);
	void setRight(int right);
	void setBottom(int buttom);
private:
	jobject m_rect;
	JNIEnv* m_jni_env;
	jfieldID m_field_left, m_field_top, m_field_right, m_field_bottom;
};

#endif
