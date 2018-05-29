package com.zero.fragmentanimation.openGL.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 资源加载类
 * @author linzewu
 * @date 2017/7/11
 */

public class RawTools {

    /**
     * 从raw资源文件加载glsl程序代码
     * @param context
     * @param resId
     * @return
     */
    @Nullable
    public static String readTextFileFromRawResource(@NonNull final Context context,
                                                     @RawRes final int resId) {

        final BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(context.getResources().openRawResource(resId))
        );
        String line;
        final StringBuilder body = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                body.append(line).append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }
    
    
    
}
