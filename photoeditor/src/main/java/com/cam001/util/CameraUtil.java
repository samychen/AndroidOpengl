package com.cam001.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

public class CameraUtil {

	private static final String TAG = "Util";
	public static final String REVIEW_ACTION = "com.android.action.REVIEW";

	// Orientation hysteresis amount used in rounding, in degrees
	public static final int ORIENTATION_HYSTERESIS = 5;

	private static ImageFileNamer sImageFileNamer;

	private CameraUtil() {
	}

	public static int getDisplayOrientation(int degrees, int cameraId) {
		// See android.hardware.Camera.setDisplayOrientation for
		// documentation.
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int result;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		return result;
	}

	public static int getDisplayRotation(Activity activity) {
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}

	public static int roundOrientation(int orientation, int orientationHistory) {
		boolean changeOrientation = false;
		if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
			changeOrientation = true;
		} else {
			int dist = Math.abs(orientation - orientationHistory);
			dist = Math.min(dist, 360 - dist);
			changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
		}
		if (changeOrientation) {
			return ((orientation + 45) / 90 * 90) % 360;
		}
		return orientationHistory;
	}

	public static int getPictureRotation(int cameraId, int orientation) {
		// See android.hardware.Camera.Parameters.setRotation for
		// documentation.
		int rotation = 0;
		if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
			CameraInfo info = CameraHolder.instance().getCameraInfo()[cameraId];
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				rotation = (info.orientation - orientation + 360) % 360;
			} else { // back-facing camera
				rotation = (info.orientation + orientation) % 360;
			}
		}
		return rotation;
	}

	public static void setGpsParameters(Parameters parameters, Location loc) {
		// Clear previous GPS location from the parameters.
		parameters.removeGpsData();

		// We always encode GpsTimeStamp
		parameters.setGpsTimestamp(System.currentTimeMillis() / 1000);

		// Set GPS location.
		if (loc != null) {
			double lat = loc.getLatitude();
			double lon = loc.getLongitude();
			boolean hasLatLon = (lat != 0.0d) || (lon != 0.0d);

			if (hasLatLon) {
				LogUtil.logV(TAG, "Set gps location");
				parameters.setGpsLatitude(lat);
				parameters.setGpsLongitude(lon);
				parameters.setGpsProcessingMethod(loc.getProvider()
						.toUpperCase());
				if (loc.hasAltitude()) {
					parameters.setGpsAltitude(loc.getAltitude());
				} else {
					// for NETWORK_PROVIDER location provider, we may have
					// no altitude information, but the driver needs it, so
					// we fake one.
					parameters.setGpsAltitude(0);
				}
				if (loc.getTime() != 0) {
					// Location.getTime() is UTC in milliseconds.
					// gps-timestamp is UTC in seconds.
					long utcTimeSeconds = loc.getTime() / 1000;
					parameters.setGpsTimestamp(utcTimeSeconds);
				}
			} else {
				loc = null;
			}
		}
	}

	public static Camera openCamera(Activity activity,
			int cameraId) throws CameraHardwareException,
			CameraDisabledException {
		// Check if device policy has disabled the camera.
		// DevicePolicyManager dpm = (DevicePolicyManager)
		// activity.getSystemService(
		// Context.DEVICE_POLICY_SERVICE);
		// if (dpm.getCameraDisabled(null)) {
		// throw new CameraDisabledException();
		// }

		if (Camera.getNumberOfCameras() < 2) {
			cameraId = 0;
		}
		try {
			return CameraHolder.instance().open(cameraId);
		} catch (CameraHardwareException e) {
			// In eng build, we throw the exception so that test tool
			// can detect it and report it
			if ("eng".equals(Build.TYPE)) {
				throw new RuntimeException("openCamera failed", e);
			} else {
				throw e;
			}
		}
	}

	public static void Assert(boolean cond) {
		if (!cond) {
			throw new AssertionError();
		}
	}

	public static void dumpToFile(String path, byte[] buffer) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			fos.write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void dumpToFile(String path, byte[] buffer, boolean append) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path, append);
			fos.write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void dumpToFile(String path, Bitmap mBitmap) {
		File f = new File(path);
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// public static void broadcastNewPicture(Context context, Uri uri) {
	// context.sendBroadcast(new
	// Intent(android.hardware.Camera.ACTION_NEW_PICTURE, uri));
	// // Keep compatibility
	// context.sendBroadcast(new Intent("com.arcsoft.nativecamera.NEW_PICTURE",
	// uri));
	// }

	public static String createJpegPath(long dateTaken) {
		if (sImageFileNamer == null) {
			sImageFileNamer = new ImageFileNamer("'IMG'_yyyyMMdd_HHmmss");
		}
		synchronized (sImageFileNamer) {
			return StorageUtil.DIRECTORY + "/"
					+ sImageFileNamer.generateName(dateTaken) + ".jpg";
		}
	}

	public static boolean isUriValid(Uri uri, ContentResolver resolver) {
		if (uri == null)
			return false;

		try {
			ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
			if (pfd == null) {
				LogUtil.logE(TAG, "Fail to open URI. URI=" + uri);
				return false;
			}
			pfd.close();
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	public static void viewUri(Uri uri, Context context) {
		if (!isUriValid(uri, context.getContentResolver())) {
			LogUtil.logE(TAG, "Uri invalid. uri=" + uri);
			return;
		}

		Intent intent = new Intent();
		intent.setData(uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			intent.setAction(CameraUtil.REVIEW_ACTION);
			context.startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			try {
				intent.setAction(Intent.ACTION_VIEW);
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				LogUtil.logE(TAG, "review image fail. uri=" + uri, e);
			}
		}
	}

	public static Size getOptimalPreviewSize(Activity currentActivity,
			List<Size> sizes, double targetRatio) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.001;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		// Because of bugs of overlay and layout, we sometimes will try to
		// layout the viewfinder in the portrait orientation and thus get the
		// wrong size of mSurfaceView. When we change the preview size, the
		// new overlay will be created before the old one closed, which causes
		// an exception. For now, just get the screen size

		Display display = currentActivity.getWindowManager()
				.getDefaultDisplay();
		int targetHeight = Math.min(display.getHeight(), display.getWidth());

		if (targetHeight <= 0) {
			// We don't know the size of SurfaceView, use screen height
			targetHeight = display.getHeight();
		}

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
			if(size.width == 1280 && size.height == 720)
				optimalSize = size;
		}

		// Cannot find the one match the aspect ratio. This should not happen.
		// Ignore the requirement.
		if (optimalSize == null) {
			LogUtil.logE(TAG, "No preview size match the aspect ratio");
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	private static class ImageFileNamer {
		private SimpleDateFormat mFormat;

		// The date (in milliseconds) used to generate the last name.
		private long mLastDate;

		// Number of names generated for the same second.
		private int mSameSecondCount;

		public ImageFileNamer(String format) {
			mFormat = new SimpleDateFormat(format);
		}

		public String generateName(long dateTaken) {
			Date date = new Date(dateTaken);
			String result = mFormat.format(date);

			// If the last name was generated for the same second,
			// we append _1, _2, etc to the name.
			if (dateTaken / 1000 == mLastDate / 1000) {
				mSameSecondCount++;
				result += "_" + mSameSecondCount;
			} else {
				mLastDate = dateTaken;
				mSameSecondCount = 0;
			}

			return result;
		}
	}

	public static void broadcastNewPicture(Context context, Uri uri) {
		context.sendBroadcast(new Intent(android.hardware.Camera.ACTION_NEW_PICTURE, uri));
	}
}
