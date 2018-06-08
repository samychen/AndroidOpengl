package com.cam001.util;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.cam001.exif.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

/**
 * Created by hzhao on 15/6/5.
 */
public class ImageUtil {

    private static final int FILE_BUF_SIZE = 128*1024;
    private static final String TIME_STAMP_NAME = "_yyyyMMdd_HHmmss_SSS";

    public static byte[] readBuffer(Context context, Uri uri) {
        String type = context.getContentResolver().getType(uri);
        if(type!=null && !type.equalsIgnoreCase("image/jpeg")) {
            Bitmap bmp =  BitmapUtil.getBitmap( uri, context, 1024, 1024);
            DebugUtil.logE("ImageUtil", "Image Type: "+type);
            return BitmapUtil.SaveBitmapToMemory(bmp, Bitmap.CompressFormat.JPEG);
        }
        byte[] data = null;
        byte[] buf = new byte[1024];
        try {
            InputStream stream = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int readBytes = 0;
            do {
                readBytes = stream.read(buf);
                if(readBytes>0) baos.write(buf, 0, readBytes);
            } while (readBytes>0);
            data = baos.toByteArray();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Uri saveToUri(Context context,Bitmap bmp,Uri sourceUri,String srcPath) {
        com.cam001.service.LogUtil.startLogTime("save");
        long dateTaken= System.currentTimeMillis();
        String ext="";
        if (srcPath.contains("jpg")) {
            ext="jpg";
        }else if (srcPath.contains("png")) {
            ext="png";
        }
        String dstPath= srcPath.substring(0, srcPath.lastIndexOf("/"));
        String path = dstPath + "/" + dateTaken+"."+ext;
        ContentValues values = StorageUtil.getContentValues(context, sourceUri, new File(path), dateTaken);
        if (!StorageUtil.saveImageToStorage(path, bmp)) {
            return null;
        }
//        ExifInterface exif = getExifData(context, sourceUri, sourceUri);
//        updateExifData(exif, dateTaken);
        context.getContentResolver().update(sourceUri,values,null,null);
        updataImageDimensionInDB(context,new File(path),bmp.getWidth(),bmp.getHeight(),dateTaken);
        com.cam001.service.LogUtil.stopLogTime("save");

        return sourceUri;
    }

    private static void updateExifData(ExifInterface exif, long time) {
        // Set tags
        exif.addDateTimeStampTag(ExifInterface.TAG_DATE_TIME, time,
                TimeZone.getDefault());
        exif.setTag(exif.buildTag(ExifInterface.TAG_ORIENTATION,
                ExifInterface.Orientation.TOP_LEFT));
        // Remove old thumbnail
        exif.removeCompressedThumbnail();
    }

    public static ExifInterface getExifData(Context mContext,Uri source,Uri dstUri) {
        ExifInterface exif = new ExifInterface();
        String mimeType = mContext.getContentResolver().getType(dstUri);
        if (mimeType == null) {
            mimeType = getMimeType(dstUri);
            /// M: [BUG.ADD] @{
            //mimeType may be null in some cases
            if (mimeType == null) return exif;
            /// @}
        }
        if (mimeType.equals(JPEG_MIME_TYPE)) {
            InputStream inStream = null;
            try {
                inStream = mContext.getContentResolver().openInputStream(source);
                assert exif != null;
                exif.readExif(inStream);
            } catch (FileNotFoundException e) {
                Log.w("xu", "Cannot find file: " + source, e);
            } catch (IOException e) {
                Log.w("xu", "Cannot read exif for: " + source, e);
            } finally {
                Util.closeSilently(inStream);
            }
        }
        return exif;
    }

    public static final String JPEG_MIME_TYPE = "image/jpeg";
    public static String getMimeType(Uri src) {
        String postfix = MimeTypeMap.getFileExtensionFromUrl(src.toString());
        String ret = null;
        if (postfix != null) {
            ret = MimeTypeMap.getSingleton().getMimeTypeFromExtension(postfix);
        }
        return ret;
    }

    public static boolean updataImageDimensionInDB(Context context, File file, int width, int height,long time) {
        if (file == null) return false;
        final ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.WIDTH, width);
        values.put(MediaStore.Images.Media.HEIGHT, height);
        values.put(MediaStore.Images.Media.SIZE, file.length());
        values.put(MediaStore.Images.Media.DATE_TAKEN, time);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, time);
        values.put(MediaStore.Images.Media.DATE_ADDED, time);

        int r = context.getContentResolver().update(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values,
                MediaStore.Images.Media.DATA + "=?",
                new String[]{file.getAbsolutePath()});
        return (r > 0);
    }

    private static void deleteImage(Context context, String imgPath) {
            File file = new File(imgPath);
             file.delete();
    }

    public static Uri rotate(Context context, String path, Uri uri, int angle, boolean mirrorX, boolean mirrorY) {
        Uri dstUri=null;
        try {
            byte[] in = readBuffer(context, uri);
            byte[] out = native_rotate(in, angle, mirrorX, mirrorY);
            Bitmap bmp= BitmapFactory.decodeByteArray(out, 0, out.length);
             dstUri =saveToUri(context, bmp, uri,path);

            deleteImage(context,path);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return  dstUri;
    }

    public static Uri enhance(Context context, Uri uri, float[] colorMatrix,String path) {
        Uri dstUri = null;
        try {
            byte[] in = readBuffer(context, uri);
            byte[] out = native_enhance(in, colorMatrix);
            Bitmap bmp= BitmapFactory.decodeByteArray(out, 0, out.length);
            dstUri =saveToUri(context, bmp, uri,path);

            deleteImage(context, path);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dstUri;
    }

    public static Uri crop(Context context, Uri uri, Rect rect,String path) {
        return crop(context, uri, rect.left, rect.top, rect.right, rect.bottom,path);
    }

    public static Uri crop(Context context, Uri uri, RectF rect,String path) {
        return crop(context, uri, (int)rect.left, (int)rect.top, (int)rect.right, (int)rect.bottom,path);
    }

    public static Uri crop(Context context, Uri uri, int left, int top, int right, int bottom,String path) {
        Uri dstUri=null;
        try {
            byte[] in = readBuffer(context, uri);
            byte[] out = native_crop(in, left, top, right, bottom);
            Bitmap bmp= BitmapFactory.decodeByteArray(out, 0, out.length);
            dstUri =saveToUri(context, bmp, uri,path);
            deleteImage(context, path);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dstUri;
    }

    public static  Uri save(Context context, Uri uri,Bitmap bmp,String path){
        Uri dstUri=null;
        try {
            dstUri =saveToUri(context, bmp, uri,path);
            deleteImage(context, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  dstUri;
    }

    private static native byte[] native_rotate(byte[] in, int angle, boolean mirrorX, boolean mirrorY);
    private static native byte[] native_enhance(byte[] in, float[] colorMatrix);
    private static native byte[] native_crop(byte[] in, int left, int top, int right, int bottom);

}