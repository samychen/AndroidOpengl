#include <stdlib.h>
#include "com_spore_meitu_jni_ImageUtilEngine.h"

#include <android/log.h>
#include <android/bitmap.h>
#include <math.h>
#define LOG_TAG "Spore.meitu"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT jstring JNICALL Java_com_spore_meitu_jni_ImageUtilEngine_getResultFromJni(
		JNIEnv* env, jobject thiz) {
	return (*env)->NewStringUTF(env, "ArtFilter from JNI !!!");
}

int min(int x, int y) {
    return (x <= y) ? x : y;
}
int max(int x,int y){
	return (x >= y) ? x : y;
}
int alpha(int color) {
    return (color >> 24) & 0xFF;
}
int red(int color) {
    return (color >> 16) & 0xFF;
}
int green(int color) {
    return (color >> 8) & 0xFF;
}
int blue(int color) {
    return color & 0xFF;
}
int ARGB(int alpha, int red, int green, int blue) {
    return (alpha << 24) | (red << 16) | (green << 8) | blue;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toGray(JNIEnv* env,
		jobject thiz, jintArray buf, jint width, jint height)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	int count = 0;
	int preColor = 0;
	int prepreColor = 0;
	int color = 0;
	preColor = cbuf[0];

	int i = 0;
	int j = 0;
	for (i = 0; i < width; i++)
	{
		for (j = 0; j < height; j++)
		{
			int curr_color = cbuf[j * width + i];
			int r = red(curr_color);
			int g = green(curr_color);
			int b = blue(curr_color);
			int modif_color = (int)(r * 0.3 + g * 0.59 + b * 0.11);
			rbuf[j * width + i] = ARGB(alpha(curr_color),modif_color,modif_color,modif_color);
		}
	}
	jintArray result = (*env)->NewIntArray(env, newSize); // 新建一个jintArray
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf); // 将rbuf转存入result
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0); // 释放int数组元素
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toFudiao(JNIEnv* env,
		jobject thiz, jintArray buf, jint width, jint height)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);
	//jintArray result = (*env)->NewIntArray(env, width * height);
	LOGE("Bitmap Buffer %d %d",cbuf[0],cbuf[1]);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	int count = 0;
	int preColor = 0;
	int prepreColor = 0;
	int color = 0;
	preColor = cbuf[0];

	int i = 0;
	int j = 0;
	for (i = 0; i < width; i++)
	{
		for (j = 0; j < height; j++)
		{
			int curr_color = cbuf[j * width + i];
			int r = red(curr_color) - red(prepreColor) + 128;
			int g = green(curr_color) - red(prepreColor) + 128;
			int b = green(curr_color) - blue(prepreColor) + 128;
			int a = alpha(curr_color);

			int newcolor = (int)(r * 0.3 + g * 0.59 + b * 0.11);

			int modif_color = ARGB(a, newcolor, newcolor, newcolor);
			rbuf[j * width + i] = modif_color;
			prepreColor = preColor;
			preColor = curr_color;
		}
	}
	jintArray result = (*env)->NewIntArray(env, newSize); // 新建一个jintArray
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf); // 将rbuf转存入result
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0); // 释放int数组元素
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toHeibai
  (JNIEnv* env,jobject thiz, jintArray buf, jint width, jint height)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	int count = 0;
	int preColor = 0;
	int prepreColor = 0;
	int color = 0;
	preColor = cbuf[0];

	int i = 0;
	int j = 0;
	int iPixel = 0;
	for (i = 0; i < width; i++) {
		for (j = 0; j < height; j++) {
			int curr_color = cbuf[j * width + i];

			int avg = (red(curr_color) + green(curr_color) + blue(curr_color))
					/ 3;
			if (avg >= 100) {
				iPixel = 255;
			} else {
				iPixel = 0;
			}
			int modif_color = ARGB(255, iPixel, iPixel, iPixel);
			rbuf[j * width + i] = modif_color;
		}
	}
	jintArray result = (*env)->NewIntArray(env, newSize); // 新建一个jintArray
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf); // 将rbuf转存入result
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0); // 释放int数组元素
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toMohu
  (JNIEnv* env,jobject thiz, jintArray buf, jint width, jint height,jint blur)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	int pixColor = 0;
	int newR = 0;
	int newG = 0;
	int newB = 0;

	int newColor = 0;

	int i = 1;
	int j = 1;
	int times = 0;
	for(;times<blur;times++)
	for (i = 1; i < width - 1; i++)
	{
		for (j = 1; j < height - 1; j++)
		{
			newR = 0;
			newG = 0;
			newB = 0;
			int m = 0;
			for (; m < 9; m++)
			{
				int s = 0;
				int p = 0;
				switch (m)
				{
				case 0:
					s = i - 1;
					p = j - 1;
					break;
				case 1:
					s = i;
					p = j - 1;
					break;
				case 2:
					s = i + 1;
					p = j - 1;
					break;
				case 3:
					s = i + 1;
					p = j;
					break;
				case 4:
					s = i + 1;
					p = j + 1;
					break;
				case 5:
					s = i;
					p = j + 1;
					break;
				case 6:
					s = i - 1;
					p = j + 1;
					break;
				case 7:
					s = i - 1;
					p = j;
					break;
				case 8:
					s = i;
					p = j;
				}
				pixColor = cbuf[p * width + s];
				newR += red(pixColor);
				newG += green(pixColor);
				newB += blue(pixColor);
			}

			newR = (int) (newR / 9.0);
			newG = (int) (newG / 9.0);
			newB = (int) (newB / 9.0);

			newR = min(255, max(0, newR));
			newG = min(255, max(0, newG));
			newB = min(255, max(0, newB));

			newColor = ARGB(255, newR, newG, newB);
			rbuf[j * width + i] = newColor;
		}
	}

	jintArray result = (*env)->NewIntArray(env, newSize); // 新建一个jintArray
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf); // 将rbuf转存入result
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0); // 释放int数组元素
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toDipian
  (JNIEnv* env,jobject thiz, jintArray buf, jint width, jint height)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);
	LOGE("Bitmap Buffer %d %d",cbuf[0],cbuf[1]);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	int count = 0;
	int preColor = 0;
	int prepreColor = 0;
	int color = 0;
	preColor = cbuf[0];

	int i = 0;
	int j = 0;
	int iPixel = 0;
	for (i = 0; i < width; i++) {
		for (j = 0; j < height; j++) {
			int curr_color = cbuf[j * width + i];

			int r = 255 - red(curr_color);
			int g = 255 - green(curr_color);
			int b = 255 - blue(curr_color);
			int a = alpha(curr_color);
			int modif_color = ARGB(a, r, g, b);
			rbuf[j * width + i] = modif_color;
		}
	}
	jintArray result = (*env)->NewIntArray(env, newSize); // 新建一个jintArray
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf); // 将rbuf转存入result
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0); // 释放int数组元素
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toSunshine
  (JNIEnv* env,jobject thiz, jintArray buf, jint width, jint height,jint centerX, jint centerY, jint radius, jint strength)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	radius = min(centerX, centerY);

	int i = 0;
	int j = 0;
	for (i = 0; i < width; i++) {
		for (j = 0; j < height; j++) {
			int curr_color = cbuf[j * width + i];

			int pixR = red(curr_color);
			int pixG = green(curr_color);
			int pixB = blue(curr_color);

			int newR = pixR;
			int newG = pixG;
			int newB = pixB;
			int distance = (int) ((centerY - i) * (centerY - i) + (centerX - j) * (centerX - j));
			if (distance < radius * radius)
			{
				int result = (int) (strength * (1.0 - sqrt(distance) / radius));
				newR = pixR + result;
				newG = pixG + result;
				newB = pixB + result;
			}
			newR = min(255, max(0, newR));
			newG = min(255, max(0, newG));
			newB = min(255, max(0, newB));

			int a = alpha(curr_color);
			int modif_color = ARGB(a, newR, newG, newB);
			rbuf[j * width + i] = modif_color;
		}
	}
	jintArray result = (*env)->NewIntArray(env, newSize);
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf);
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0);
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toFangdajing
  (JNIEnv* env,jobject thiz, jintArray buf, jint width, jint height,jint centerX, jint centerY, jint radius, jfloat multiple)
{
	jint * cbuf;
	cbuf = (*env)->GetIntArrayElements(env, buf, 0);

	int newSize = width * height;
	jint rbuf[newSize]; // 新图像像素值

	float xishu = multiple;
	int real_radius = (int)(radius / xishu);

	int i = 0, j = 0;
	for (i = 0; i < width; i++)
	{
		for (j = 0; j < height; j++)
		{
			int curr_color = cbuf[j * width + i];

			int pixR = red(curr_color);
			int pixG = green(curr_color);
			int pixB = blue(curr_color);
			int pixA = alpha(curr_color);

			int newR = pixR;
			int newG = pixG;
			int newB = pixB;
			int newA = pixA;

			int distance = (int) ((centerX - i) * (centerX - i) + (centerY - j) * (centerY - j));
			if (distance < radius * radius)
			{
				// 图像放大效果
				int src_x = (int)((float)(i - centerX) / xishu + centerX);
				int src_y = (int)((float)(j - centerY) / xishu + centerY);

				int src_color = cbuf[src_y * width + src_x];
				newR = red(src_color);
				newG = green(src_color);
				newB = blue(src_color);
				newA = alpha(src_color);
			}

			newR = min(255, max(0, newR));
			newG = min(255, max(0, newG));
			newB = min(255, max(0, newB));
			newA = min(255, max(0, newA));

			int modif_color = ARGB(newA, newR, newG, newB);
			rbuf[j * width + i] = modif_color;
		}
	}

	jintArray result = (*env)->NewIntArray(env, newSize);
	(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf);
	(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0);
	return result;
}

jintArray Java_com_spore_meitu_jni_ImageUtilEngine_toHahajing
  (JNIEnv* env,jobject thiz, jintArray buf, jint width, jint height,jint centerX, jint centerY, jint radius, jfloat multiple)
{
	jint * cbuf;
		cbuf = (*env)->GetIntArrayElements(env, buf, 0);

		int newSize = width * height;
		jint rbuf[newSize]; // 新图像像素值

		float xishu = multiple;
		int real_radius = (int)(radius / xishu);

		int i = 0, j = 0;
		for (i = 0; i < width; i++)
		{
			for (j = 0; j < height; j++)
			{
				int curr_color = cbuf[j * width + i];

				int pixR = red(curr_color);
				int pixG = green(curr_color);
				int pixB = blue(curr_color);
				int pixA = alpha(curr_color);

				int newR = pixR;
				int newG = pixG;
				int newB = pixB;
				int newA = pixA;

				int distance = (int) ((centerX - i) * (centerX - i) + (centerY - j) * (centerY - j));
				if (distance < radius * radius)
				{
					// 放大镜的凹凸效果
					int src_x = (int) ((float) (i - centerX) / xishu);
					int src_y = (int) ((float) (j - centerY) / xishu);
					src_x = (int)(src_x * (sqrt(distance) / real_radius));
					src_y = (int)(src_y * (sqrt(distance) / real_radius));
					src_x = src_x + centerX;
					src_y = src_y + centerY;

					int src_color = cbuf[src_y * width + src_x];
					newR = red(src_color);
					newG = green(src_color);
					newB = blue(src_color);
					newA = alpha(src_color);
				}

				newR = min(255, max(0, newR));
				newG = min(255, max(0, newG));
				newB = min(255, max(0, newB));
				newA = min(255, max(0, newA));

				int modif_color = ARGB(newA, newR, newG, newB);
				rbuf[j * width + i] = modif_color;
			}
		}

		jintArray result = (*env)->NewIntArray(env, newSize);
		(*env)->SetIntArrayRegion(env, result, 0, newSize, rbuf);
		(*env)->ReleaseIntArrayElements(env, buf, cbuf, 0);
		return result;
}
