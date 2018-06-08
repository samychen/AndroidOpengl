/*
 * Copyright (C) 2010,2012 Thundersoft Corporation
 * All rights Reserved
 */

package com.cam001.photoeditor.resource;

import java.lang.reflect.Field;
import com.cam001.photoeditor.R;

public class RLoader {

    public static int getDrawableId(String name){
        int value = -1;
        try {
            Field fiedl = R.drawable.class.getField(name);
            value = fiedl.getInt(R.drawable.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            value = -1;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            value = -1;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            value = -1;
        }
        return value;
    }

    public static int getStringId(String name){
        int value = -1;
        try {
            Field field = R.string.class.getDeclaredField(name);
            value = field.getInt(R.string.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            value = -1;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            value = -1;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            value = -1;
        }
        return value;
    }
}
