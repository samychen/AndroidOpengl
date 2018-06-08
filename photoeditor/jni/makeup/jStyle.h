#ifndef __J_STYLE_H__
#define __J_STYLE_H__

#include <jni.h>

class jStyle {
public:
	jStyle(JNIEnv* env, jobject point);
	int getbratio();
	int getlratio();
	int getelashratio();
	int getelineratio();
	int geteshaderatio();
	int getlgloss();
	int geteshaderfratio();
	char* getbcolor();
	char* getlcolor();
	char* getelashcolor();
	char* getelinercolor();
	char* geteshadercolor();
	char* getbtemp();
	char* getltemp();
	char* getelashupper();
	char* getelashlower();
	char* getelinerupper();
	char* getelinerlower();
	char* geteshadertemp();

	int getcratio();
	char* getctemp();
//	void setX(int x);
//	void setY(int y);
private:
	jobject m_style;
	JNIEnv* m_jni_env;
	jfieldID m_field_bratio, m_field_bcolor, m_field_btemp,
				m_field_lratio, m_field_lgloss, m_field_lcolor, m_field_ltemp,
				m_field_elashratio, m_field_elashcolor, m_field_elashupper, m_field_elashlower,
				m_field_elineratio, m_field_elinercolor, m_field_elinerupper, m_field_elinerlower,
				m_field_eshaderatio, m_field_eshaderfratio, m_field_eshadercolor, m_field_eshadertemp,
				m_field_cratio, m_field_ctemp;
};

#endif
