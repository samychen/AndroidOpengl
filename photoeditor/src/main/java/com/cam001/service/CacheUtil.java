package com.cam001.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hzhao on 15/5/8.
 */
public class CacheUtil {

    private static final String TAG = "CacheUtil";

    public static void cacheInt(Context c, String name, int value) {
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            fw.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeSilently(fw);
        }
    }

    public static int getCachedInt(Context c, String name) {
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
        int res = 0;
        FileReader fr = null;
        try {
            fr = new FileReader(path);
            res = fr.read();
        } catch (IOException e) {
            LogUtil.logV(TAG, "getCachedInt failed. "+path);
        } finally {
            Util.closeSilently(fr);
        }
        return res;
    }

    public static void cacheString(Context c, String name, String value) {
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
//        System.out.println("zhl path="+path);
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            fw.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeSilently(fw);
        }
    }

    public static String getCachedString(Context c, String name) {
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
//        System.out.println("zhl path="+path);
        String res = null;
        FileReader fr = null;
        try {
            fr = new FileReader(path);
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[512];
            int readBytes;
            while((readBytes = fr.read(buf))>0){
                sb.append(buf, 0, readBytes);
            }
            res = sb.toString();
        } catch (IOException e) {
            LogUtil.logV(TAG, "getCachedString failed. "+path);
        } finally {
            Util.closeSilently(fr);
        }
        return res;
    }

    public static void cacheBitmap(Context c, String name, Bitmap bmp) {
        cacheBitmap(c, name, bmp, Bitmap.CompressFormat.PNG);
    }

    public static void cacheBitmap(Context c, String name, Bitmap bmp, Bitmap.CompressFormat format) {
        if(bmp==null) return;
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(path);
            bmp.compress(format, 90, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            Util.closeSilently(stream);
        }
    }

    public static Bitmap getCachedBitmap(Context c, String name) {
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, opts);
    }

    public static boolean cachepHttpFile(Context c, String name, String urlStr) {
        String path = c.getCacheDir().getAbsolutePath()+"/"+name;
        HttpURLConnection con = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection)url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.connect();
            int httpCode = con.getResponseCode();
            LogUtil.logV(TAG, "ResponseCode = %d", httpCode);
            if(httpCode/100!=2) {
                return false;
            }
            is = con.getInputStream();
            os = new FileOutputStream(path);
            byte[] buf = new byte[512];
            int readBytes = 0;
            while ((readBytes=is.read(buf))>0) {
                os.write(buf, 0, readBytes);
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeSilently(is);
            Util.closeSilently(os);
            if(con!=null) con.disconnect();
        }
        return false;
    }
}
