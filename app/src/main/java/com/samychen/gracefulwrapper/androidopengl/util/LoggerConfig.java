package com.samychen.gracefulwrapper.androidopengl.util;

import android.util.Log;

/**
 * Created by samychen on 2018/5/20 0020.
 * 我的github地址 https://github.com/samychen
 */

public class LoggerConfig {
    public static final boolean ON = true;
    public static void i(String tag,String str){
        if (ON){
            Log.i(tag, "i: ");
        }
    }

    public static void e(String tag,String str){
        if (ON){
            Log.e(tag, "i: ");
        }
    }
}
