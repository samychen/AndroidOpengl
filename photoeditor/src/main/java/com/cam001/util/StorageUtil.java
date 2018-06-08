/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cam001.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageUtil {
	private static final String TAG = "CameraStorage";

	private static final String DCIM = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
			.toString();
	public static final String DIRECTORY = DCIM + "/FasionMakeup";

	// Match the code in MediaProvider.computeBucketValues().
	public static final String BUCKET_ID = String.valueOf(DIRECTORY
			.toLowerCase().hashCode());

	public static final long UNAVAILABLE = -1L;
	public static final long PREPARING = -2L;
	public static final long UNKNOWN_SIZE = -3L;
	public static final long LOW_STORAGE_THRESHOLD = 3000000; // In bytes
	public static final long PICTURE_SIZE = 1500000;

	private static final int BUFSIZE = 4096;

	public static Uri addImage(ContentResolver resolver, String path,
			long date, Location location, int orientation, byte[] jpeg,
			int width, int height) {
		// Save the image.
		if (!saveImageToStorage(path, jpeg)) {
			return null;
		}
		return insertImageToMediaStore(path, date, orientation, jpeg.length,
				location, resolver);
	}

	public static Uri addImage(ContentResolver resolver, String path,
			long date, Location location, int orientation, Bitmap bmp) {
		// Save the image.
		if (!saveImageToStorage(path, bmp)) {
			return null;
		}
		if (orientation != 0) {
			try {
				ExifInterface exif = new ExifInterface(path);
				exif.setAttribute(ExifInterface.TAG_ORIENTATION,
						String.valueOf(orientation));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return insertImageToMediaStore(path, date, orientation, 0, location,
				resolver);
	}

	public static boolean saveImageToStorage(String path, Bitmap bmp) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			bmp.compress(CompressFormat.JPEG, 95, out);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		return true;
	}

	private static boolean saveImageToStorage(String path, byte[] jpeg) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(path);
			out.write(jpeg);
		} catch (Exception e) {
			LogUtil.logE(TAG, "Failed to write image", e);
			return false;
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
		return true;
	}

	public static Uri insertImageToMediaStore(String path, long date,
			int orientation, long size, Location location,
			ContentResolver resolver) {
		int tileBegin = path.lastIndexOf("/") + 1;
		int tileend = path.lastIndexOf(".");
		String title = path.substring(tileBegin, tileend);
		String ext = path.substring(tileend + 1);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				ext);
		// Insert into MediaStore.
		ContentValues values = new ContentValues(9);
		values.put(MediaColumns.TITLE, title);
		values.put(MediaColumns.DISPLAY_NAME, title + "." + ext);
		values.put(ImageColumns.DATE_TAKEN, date);
		values.put(MediaColumns.MIME_TYPE, mimeType);
		values.put(ImageColumns.ORIENTATION, orientation);
		values.put(MediaColumns.DATA, path);
		values.put(MediaColumns.SIZE, size);
		// values.put(ImageColumns.WIDTH, width);
		// values.put(ImageColumns.HEIGHT, height);

		if (location != null) {
			values.put(ImageColumns.LATITUDE, location.getLatitude());
			values.put(ImageColumns.LONGITUDE, location.getLongitude());
		}

		Uri uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		if (uri == null) {
			LogUtil.logE(TAG, "Failed to write MediaStore");
			return null;
		}
		return uri;
	}

	static ContentValues getContentValues(Context context, Uri sourceUri,
										  File file, long time) {
		final ContentValues values = new ContentValues();

		time /= 1000;
		values.put(Images.Media.TITLE, file.getName());
		values.put(Images.Media.DISPLAY_NAME, file.getName());
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(Images.Media.DATE_TAKEN, time);
		values.put(Images.Media.DATE_MODIFIED, time);
		values.put(Images.Media.DATE_ADDED, time);
		values.put(Images.Media.ORIENTATION, 0);
		values.put(Images.Media.DATA, file.getAbsolutePath());
		values.put(Images.Media.SIZE, file.length());
		values.put(Images.Media.SIZE,1);
		// This is a workaround to trigger the MediaProvider to re-generate the
		// thumbnail.
		values.put(Images.Media.MINI_THUMB_MAGIC, 0);

		final String[] projection = new String[] {
				ImageColumns.DATE_TAKEN,
				ImageColumns.LATITUDE, ImageColumns.LONGITUDE,
		};

		SaveImage.querySource(context, sourceUri, projection,
				new SaveImage.ContentResolverQueryCallback() {

					@Override
					public void onCursorResult(Cursor cursor) {
						values.put(Images.Media.DATE_TAKEN, cursor.getLong(0));

						double latitude = cursor.getDouble(1);
						double longitude = cursor.getDouble(2);
						// TODO: Change || to && after the default location
						// issue is fixed.
						if ((latitude != 0f) || (longitude != 0f)) {
							values.put(Images.Media.LATITUDE, latitude);
							values.put(Images.Media.LONGITUDE, longitude);
						}
					}
				});
		return values;
	}

	public static long getAvailableSpace() {
		String state = Environment.getExternalStorageState();
		LogUtil.logV(TAG, "External storage state=" + state);
		if (Environment.MEDIA_CHECKING.equals(state)) {
			return PREPARING;
		}
		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			return UNAVAILABLE;
		}

		File dir = new File(DIRECTORY);
		dir.mkdirs();
		if (!dir.isDirectory() || !dir.canWrite()) {
			return UNAVAILABLE;
		}

		try {
			StatFs stat = new StatFs(DIRECTORY);
			return stat.getAvailableBlocks() * (long) stat.getBlockSize();
		} catch (Exception e) {
			LogUtil.logE(TAG, "Fail to access external storage", e);
		}
		return UNKNOWN_SIZE;
	}

	/**
	 * OSX requires plugged-in USB storage to have path /DCIM/NNNAAAAA to be
	 * imported. This is a temporary fix for bug#1655552.
	 */
	public static void ensureOSXCompatible() {
		File nnnAAAAA = new File(DIRECTORY);
		if (!(nnnAAAAA.exists() || nnnAAAAA.mkdirs())) {
			LogUtil.logE(TAG, "Failed to create " + nnnAAAAA.getPath());
		}
	}

	public static boolean checkStorage() {
		return getAvailableSpace() > LOW_STORAGE_THRESHOLD;
	}

	public static long getSystemAvailableSize() {
		// context.getFilesDir().getAbsolutePath();
		return getAvailableSize("/data");
	}

	private static long getAvailableSize(String path) {
		StatFs fileStats = new StatFs(path);
		fileStats.restat(path);
		return (long) fileStats.getAvailableBlocks() * fileStats.getBlockSize();
	}

	public static Uri insertImageToMediaStore(String path, long date,
			int orientation, long size, Location location,
			ContentResolver resolver, String emojiTitle) {
		int tileBegin = path.lastIndexOf("/") + 1;
		int tileend = path.lastIndexOf(".");
		String title = emojiTitle;
		if (emojiTitle == null || emojiTitle == "") {
			title = path.substring(tileBegin, tileend);
		}
		String ext = path.substring(tileend + 1);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				ext);
		// Insert into MediaStore.
		ContentValues values = new ContentValues(9);
		values.put(MediaColumns.TITLE, title);
		values.put(MediaColumns.DISPLAY_NAME, title + "." + ext);
		values.put(ImageColumns.DATE_TAKEN, date);
		values.put(MediaColumns.MIME_TYPE, mimeType);
		values.put(ImageColumns.ORIENTATION, orientation);
		values.put(MediaColumns.DATA, path);
		values.put(MediaColumns.SIZE, size);
		// values.put(ImageColumns.WIDTH, width);
		// values.put(ImageColumns.HEIGHT, height);

		if (location != null) {
			values.put(ImageColumns.LATITUDE, location.getLatitude());
			values.put(ImageColumns.LONGITUDE, location.getLongitude());
		}

		Uri uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		if (uri == null) {
			LogUtil.logE(TAG, "Failed to write MediaStore");
			return null;
		}
		return uri;
	}

	public static String getRealPath(ContentResolver cr, Uri uri) {
		if (uri.getScheme().equalsIgnoreCase("file")) {
			return uri.getPath();
		} else if (uri.getScheme().equalsIgnoreCase("content")) {
			return getRealPathFromURI(cr, uri);
		} else
			return null;
	}

	public static String getRealPathFromURI(ContentResolver cr, Uri contentUri) {
		// can post image
		String path = null;
		String[] proj = { Images.Media.DATA };
		Cursor cursor = cr.query(contentUri, proj, null, null, null);
		if (cursor != null) {
			try {
				int column_index = cursor
						.getColumnIndexOrThrow(Images.Media.DATA);
				if (cursor.moveToFirst()) {
					path = cursor.getString(column_index);
				}
			} finally {
				cursor.close();
			}
		}
		return path;
	}

}
