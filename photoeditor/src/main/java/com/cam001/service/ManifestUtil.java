package com.cam001.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by hzhao on 15/5/8.
 */


public class ManifestUtil {

    public static boolean isDebuggable(Context c) {
        return (0 != (c.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }
    public static int getVersionCode(Context c) {
        PackageManager packageManager =c.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(c.getPackageName(),0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getVersionName(Context c) {
        PackageManager packageManager =c.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(c.getPackageName(),0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMetaDataValue(Context c, String name, String def) {
        String value = getMetaDataValue(c, name);
        return (value == null) ? def : value;
    }

    private static String getMetaDataValue(Context c, String name) {
        Object value = null;
        PackageManager packageManager = c.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(c
                    .getPackageName(), 128);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                value = applicationInfo.metaData.get(name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            //            throw new RuntimeException(
            //                    "Could not read the name in the manifest file.", e);
            return null;
        }
        if (value == null) {
            //            throw new RuntimeException("The name '" + name
            //                    + "' is not defined in the manifest file's meta data.");
            return null;
        }
        return value.toString();
    }
}
