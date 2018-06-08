/**
 * Name        : ImageUtilEngine.java
 * Copyright   : Copyright (c) Tencent Inc. All rights reserved.
 * Description : TODO
 */

package com.spore.meitu.jni;

import android.graphics.Bitmap;

/**
 * @author ianmao
 */
public class ImageUtilEngine {

    static {
        System.loadLibrary("JNITest");
    }

    public native String getResultFromJni();
    
    public native int[] toGray(int[] buffer, int width, int heigth);

    public native int[] toFudiao(int[] buffer, int width, int heigth);

    public native int[] toHeibai(int[] buffer, int width, int heigth);

    public native int[] toMohu(int[] buffer, int width, int heigth, int blur);

    public native int[] toDipian(int[] buffer, int width, int heigth);

    public native int[] toSunshine(int[] buffer, int width, int heigth, int centerX, int intcenterY, int radius,
            int strength);
    
    public native int[] toFangdajing(int[] buffer,int width,int heigth, int centerX,int centerY,int radius,float multiple);
    
    public native int[] toHahajing(int[] buffer,int width,int heigth, int centerX,int centerY,int radius,float multiple);
}
