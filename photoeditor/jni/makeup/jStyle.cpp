#include "jStyle.h"
#include<jni.h>

jStyle::jStyle(JNIEnv * env, jobject point) {
	m_jni_env = env;
	m_style = point;
	jclass class_point = env->GetObjectClass(point);
	m_field_bratio = env->GetFieldID(class_point, "bratio", "I");
	m_field_bcolor = env->GetFieldID(class_point, "bcolor",
			"Ljava/lang/String;");
	m_field_btemp = env->GetFieldID(class_point, "btemp", "Ljava/lang/String;");

	m_field_lratio = env->GetFieldID(class_point, "lratio", "I");
	m_field_lgloss = env->GetFieldID(class_point, "lgloss", "I");
	m_field_lcolor = env->GetFieldID(class_point, "lcolor",
			"Ljava/lang/String;");
	m_field_ltemp = env->GetFieldID(class_point, "ltemp", "Ljava/lang/String;");

	m_field_elashratio = env->GetFieldID(class_point, "elashratio", "I");
	m_field_elashcolor = env->GetFieldID(class_point, "elashcolor",
			"Ljava/lang/String;");
	m_field_elashupper = env->GetFieldID(class_point, "elashupper",
			"Ljava/lang/String;");
	m_field_elashlower = env->GetFieldID(class_point, "elashlower",
			"Ljava/lang/String;");

	m_field_elineratio = env->GetFieldID(class_point, "elineratio", "I");
	m_field_elinercolor = env->GetFieldID(class_point, "elinercolor",
			"Ljava/lang/String;");
	m_field_elinerupper = env->GetFieldID(class_point, "elinerupper",
			"Ljava/lang/String;");
	m_field_elinerlower = env->GetFieldID(class_point, "elinerlower",
			"Ljava/lang/String;");

	m_field_eshaderatio = env->GetFieldID(class_point, "eshaderatio", "I");
	m_field_eshaderfratio = env->GetFieldID(class_point, "eshaderfratio", "I");
	m_field_eshadercolor = env->GetFieldID(class_point, "eshadercolor",
			"Ljava/lang/String;");
	m_field_eshadertemp = env->GetFieldID(class_point, "eshadertemp",
			"Ljava/lang/String;");

	m_field_cratio = env->GetFieldID(class_point, "cratio", "I");
	m_field_ctemp = env->GetFieldID(class_point, "ctemp", "Ljava/lang/String;");
}

int jStyle::getcratio() {
	return m_jni_env->GetIntField(m_style, m_field_cratio);
}

char* jStyle::getctemp() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style, m_field_ctemp);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}

int jStyle::getbratio() {
	return m_jni_env->GetIntField(m_style, m_field_bratio);
}

int jStyle::getlratio() {
	return m_jni_env->GetIntField(m_style, m_field_lratio);
}

int jStyle::getelashratio() {
	return m_jni_env->GetIntField(m_style, m_field_elashratio);
}
int jStyle::getelineratio() {
	return m_jni_env->GetIntField(m_style, m_field_elineratio);
}
int jStyle::geteshaderatio() {
	return m_jni_env->GetIntField(m_style, m_field_eshaderatio);
}
int jStyle::getlgloss() {
	return m_jni_env->GetIntField(m_style, m_field_lgloss);
}
int jStyle::geteshaderfratio() {
	return m_jni_env->GetIntField(m_style, m_field_eshaderfratio);
}
char* jStyle::getbcolor() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style, m_field_bcolor);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getlcolor() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style, m_field_lcolor);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getelashcolor() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_elashcolor);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getelinercolor() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_elinercolor);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::geteshadercolor() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_eshadercolor);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getbtemp() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style, m_field_btemp);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getltemp() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style, m_field_ltemp);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getelashupper() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_elashupper);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getelashlower() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_elashlower);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getelinerupper() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_elinerupper);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::getelinerlower() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_elinerlower);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
char* jStyle::geteshadertemp() {
	jstring jstr = (jstring) m_jni_env->GetObjectField(m_style,
			m_field_eshadertemp);
	char* pszStr = (char*) m_jni_env->GetStringUTFChars(jstr, 0);
	return pszStr;
}
//void jStyle::setX(int value) {
//	m_jni_env->SetIntField(m_style, m_field_x, value);
//}
//
//void jStyle::setY(int value) {
//	m_jni_env->SetIntField(m_style, m_field_y, value);
//}

