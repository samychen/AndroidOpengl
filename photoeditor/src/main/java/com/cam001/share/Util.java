package com.cam001.share;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Assert;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

public class Util {
	private static final String TAG = "SDK_Sample.Util";

	public static final String TOKEN_STORAGE = "qq_qzone_session";
	public static final String KEY_TOKEN = "qq_access_token";
	public static final String KEY_OPID = "qq_openid";
	public static final String KEY_EXPIRES_IN = "qq_expires_in";
	public static final String SAVE_ATTENTSINA = "attent_our_sinaweibo";
	public static final String SAVE_DIAMOND_COUNT = "save_account_diamond";
	public static final String DIAMOND_COUNT = "diamond_count";
	public static final String HAVE_ATTENTED = "have attented SinaWeibo";
	public static final String IS_FIRST_SHARE = "firstshare";
	public static final String IS_FIRST_SHARE_SINA = "firstsharetosina";
	public static final String IS_FIRST_SHARE_QZONE = "firstsharetoqzone";
	public static final String IS_FIRST_SHARE_WECHATFG = "firstsharetowechatfriendg";
	public static final String IS_FIRST_SHARE_RENREN = "firstsharetorenren";
	public static final String IS_FIRST_DOWN = "firstdown";
	public static final String IS_FIRST_ATT = "firstatt";
	public static final String IS_FIRST_ATT_SINA = "firstattsina";
	public static final String SAVE_SHARECOUNT = "sharecountsaved";
	public static final String KEY_SHARECOUNT = "sharecount", TOKEN = "token";
	public static boolean ATTRIGHT = true, FSHARE = false;
	public static String mReqUrl = "http://www.thundersoft.com:9978/resource/api/cam001-userdata/get_storage_token.json";
	public static String mDomain = "cam001-userdata.qiniudn.com";

	public static boolean isChinese() {
		return Locale.CHINA.equals(Locale.getDefault())
				|| Locale.CHINESE.equals(Locale.getDefault())
				|| Locale.PRC.equals(Locale.getDefault())
				|| Locale.SIMPLIFIED_CHINESE.equals(Locale.getDefault());
	}

	public static String getJson() {
		String line = "";
		String JsonStr = "";
		try {
			URL url = new URL(mReqUrl);

			HttpURLConnection httpconn = (HttpURLConnection) url
					.openConnection();
			InputStreamReader inputReader = new InputStreamReader(
					httpconn.getInputStream());
			BufferedReader buffReader = new BufferedReader(inputReader);
			while ((line = buffReader.readLine()) != null) {
				JsonStr += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JsonStr;
	}

	public static String resolveJson(String json, String key) {
		String ret = null;
		try {
			JSONObject obj = new JSONObject(json);
			String aa = "";
			String filed = "";
			ret = obj.getString(key);
			aa += obj.getString("token") + "\n";
			Log.d("json", aa);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	public static boolean isAppInstalled(String packagename, Context context) {
		PackageManager pm = context.getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(packagename, 0);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}
	public static int getShareCount(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(SAVE_SHARECOUNT,
				Context.MODE_PRIVATE);
		return prefs.getInt(KEY_SHARECOUNT, 0);
	}

	public static void saveShareCount(Context context, int count) {
		SharedPreferences prefs = context.getSharedPreferences(SAVE_SHARECOUNT,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(KEY_SHARECOUNT, count);
		editor.commit();
	}

	public static void clearShareCount(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(SAVE_SHARECOUNT,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove(KEY_SHARECOUNT);
		editor.commit();
	}

	public static boolean getFirstAttStatus(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(IS_FIRST_ATT,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(IS_FIRST_ATT_SINA, true);
	}

	public static void saveFirstAttStatus(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(IS_FIRST_ATT,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(IS_FIRST_ATT_SINA, false);
		editor.commit();
	}

	public static boolean getFirstShareStatus(Context context, String TypeKey) {
		SharedPreferences prefs = context.getSharedPreferences(IS_FIRST_SHARE,
				Context.MODE_PRIVATE);
		return prefs.getBoolean(TypeKey, true);
	}

	public static void saveFirstShareStatus(Context context, String TypeKey) {
		SharedPreferences prefs = context.getSharedPreferences(IS_FIRST_SHARE,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(TypeKey, false);
		editor.commit();
	}

	public static void clearFirstShareStatus(Context context, String TypeKey) {
		SharedPreferences prefs = context.getSharedPreferences(IS_FIRST_SHARE,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove(TypeKey);
		editor.commit();
	}

	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, 80, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static byte[] getHtmlByteArray(final String url) {
		URL htmlUrl = null;
		InputStream inStream = null;
		try {
			htmlUrl = new URL(url);
			URLConnection connection = htmlUrl.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inStream = httpConnection.getInputStream();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data = inputStreamToByte(inStream);

		return data;
	}

	public static byte[] inputStreamToByte(InputStream is) {
		try {
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] readFromFile(String fileName, int offset, int len) {
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (!file.exists()) {
			Log.i(TAG, "readFromFile: file not found");
			return null;
		}

		if (len == -1) {
			len = (int) file.length();
		}

		Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len
				+ " offset + len = " + (offset + len));

		if (offset < 0) {
			Log.e(TAG, "readFromFile invalid offset:" + offset);
			return null;
		}
		if (len <= 0) {
			Log.e(TAG, "readFromFile invalid len:" + len);
			return null;
		}
		if (offset + len > (int) file.length()) {
			Log.e(TAG, "readFromFile invalid file len:" + file.length());
			return null;
		}

		byte[] b = null;
		try {
			RandomAccessFile in = new RandomAccessFile(fileName, "r");
			b = new byte[len]; // 閸掓稑缂撻崥鍫ワ拷閺傚洣娆㈡径褍鐨惃鍕殶缂侊拷
			in.seek(offset);
			in.readFully(b);
			in.close();

		} catch (Exception e) {
			Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
			e.printStackTrace();
		}
		return b;
	}

	private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

	public static Bitmap extractThumbNail(final String path, final int height,
			final int width, final boolean crop) {
		Assert.assertTrue(path != null && !path.equals("") && height > 0
				&& width > 0);

		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(path, options);
			if (tmp != null) {
				tmp.recycle();
				tmp = null;
			}

			Log.d(TAG, "extractThumbNail: round=" + width + "x" + height
					+ ", crop=" + crop);
			final double beY = options.outHeight * 1.0 / height;
			final double beX = options.outWidth * 1.0 / width;
			Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = "
					+ beY);
			options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY)
					: (beY < beX ? beX : beY));
			if (options.inSampleSize <= 1) {
				options.inSampleSize = 1;
			}

			// NOTE: out of memory error
			while (options.outHeight * options.outWidth / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
				options.inSampleSize++;
			}

			int newHeight = height;
			int newWidth = width;
			if (crop) {
				if (beY > beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			} else {
				if (beY < beX) {
					newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
				} else {
					newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
				}
			}

			options.inJustDecodeBounds = false;

			Log.i(TAG, "bitmap required size=" + newWidth + "x" + newHeight
					+ ", orig=" + options.outWidth + "x" + options.outHeight
					+ ", sample=" + options.inSampleSize);
			Bitmap bm = BitmapFactory.decodeFile(path, options);
			if (bm == null) {
				Log.e(TAG, "bitmap decode failed");
				return null;
			}

			Log.i(TAG,
					"bitmap decoded size=" + bm.getWidth() + "x"
							+ bm.getHeight());
			final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth,
					newHeight, true);
			if (scale != null) {
				bm.recycle();
				bm = scale;
			}

			if (crop) {
				final Bitmap cropped = Bitmap.createBitmap(bm,
						(bm.getWidth() - width) >> 1,
						(bm.getHeight() - height) >> 1, width, height);
				if (cropped == null) {
					return bm;
				}

				bm.recycle();
				bm = cropped;
				Log.i(TAG,
						"bitmap croped size=" + bm.getWidth() + "x"
								+ bm.getHeight());
			}
			return bm;

		} catch (final OutOfMemoryError e) {
			Log.e(TAG, "decode bitmap failed: " + e.getMessage());
			options = null;
		}
		return null;
	}

	/**
	 * @des 瀵版鍩宑onfig.properties闁板秶鐤嗛弬鍥︽娑擃厾娈戦幍锟芥箒闁板秶鐤��
	 * @return Properties鐎电钖��
	 */
	public static Properties getConfig() {
		Properties props = new Properties();
		InputStream in = Util.class
				.getResourceAsStream("/com/sns/config.properties");
		try {
			props.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return props;
	}

	/**
	 * @des 閸掋倖鏌囪ぐ鎾冲缂冩垹绮堕弰顖氭儊娑撶皯ifi
	 * @return true if wifi exist
	 */
	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	public static boolean isNetWork(Context mContext) {
		boolean wifiConnected = false;
		boolean mobileConnected = false;
		ConnectivityManager connMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
			mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
		} else {
			wifiConnected = false;
			mobileConnected = false;
		}
		return wifiConnected || mobileConnected;
	}

	public static boolean isMobileQQInstalled(Context mContext) {
		PackageManager localPackageManager = mContext.getPackageManager();
		PackageInfo localPackageInfo = null;
		try {
			localPackageInfo = localPackageManager.getPackageInfo(
					"com.tencent.mobileqq", 0);
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
			Log.d("checkMobileQQ", "error");
			localNameNotFoundException.printStackTrace();
		}
		if (localPackageInfo != null) {
			String str = localPackageInfo.versionName;
			try {
				Log.d("MobileQQ verson", str);
				String[] arrayOfString = str.split("\\.");
				int i = Integer.parseInt(arrayOfString[0]);
				int j = Integer.parseInt(arrayOfString[1]);
				if ((i > 4) || ((i == 4) && (j >= 0))) {
					return true;
				}
				return false;
			} catch (Exception localException) {
				localException.printStackTrace();

				return false;
			}
		}
		return false;
	}

	/**
	 * 閸掋倖鏌囪ぐ鎾冲缂冩垹绮堕弰顖氭儊閸欘垳鏁��
	 * 
	 *            瑜版挸澧燗citivity鐎电钖��
	 * @return 閸欘垳鏁ゆ潻鏂挎礀true 閸氾箑鍨潻鏂挎礀false
	 * 
	 * */
	public static boolean isNetworkAvailable(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		} else {
			NetworkInfo info[] = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Location getLocation(Activity activity) {
		Location location = null;
		LocationManager loctionManager = null;
		// 闁俺绻冪化鑽ょ埠閺堝秴濮熼敍灞藉絿瀵版〕ocationManager鐎电钖��
		loctionManager = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);
		location = loctionManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		return location;
	}

	public static void saveSessionQQ(Context context, String token,
			String openid, String expires) {
		SharedPreferences prefs = context.getSharedPreferences(TOKEN_STORAGE,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		if (!TextUtils.isEmpty(token)) {
			editor.putString(KEY_TOKEN, token);
		}
		if (!TextUtils.isEmpty(openid)) {
			editor.putString(KEY_OPID, openid);
		}
		if (!TextUtils.isEmpty(expires)) {
			editor.putString(KEY_EXPIRES_IN, expires);
		}
		editor.commit();
	}

	public static String readQQ_AccessToken(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(TOKEN_STORAGE,
				Context.MODE_PRIVATE);
		String token = prefs.getString(KEY_TOKEN, null);
		if (TextUtils.isEmpty(token)) {
			return null;
		}
		return token;
	}

	public static String readQQ_OpenId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(TOKEN_STORAGE,
				Context.MODE_PRIVATE);
		String openid = prefs.getString(KEY_OPID, null);
		if (TextUtils.isEmpty(openid)) {
			return null;
		}
		return openid;
	}

	public static String readQQ_Expires(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(TOKEN_STORAGE,
				Context.MODE_PRIVATE);
		String expires = prefs.getString(KEY_EXPIRES_IN, null);
		if (TextUtils.isEmpty(expires)) {
			return null;
		}
		return expires;
	}

	public static void clearSessionQQ(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(TOKEN_STORAGE,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove(KEY_TOKEN);
		editor.remove(KEY_OPID);
		editor.remove(KEY_EXPIRES_IN);
		editor.commit();
	}

	public static void saveStateForAttentSinaWeibo(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(SAVE_ATTENTSINA,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(HAVE_ATTENTED, ATTRIGHT);
		editor.commit();
	}

	public static boolean readStateForAttentSinaWeibo(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(SAVE_ATTENTSINA,
				Context.MODE_PRIVATE);
		boolean haveAtt = prefs.getBoolean(HAVE_ATTENTED, false);
		return haveAtt;
	}

	public static void clearStateForAttentSinaWeibo(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(SAVE_ATTENTSINA,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.remove(HAVE_ATTENTED);
		editor.commit();
	}

	// public static void saveDiamondCount(Context context, int mCount){
	// SharedPreferences prefs =
	// context.getSharedPreferences(SAVE_DIAMOND_COUNT, Context.MODE_PRIVATE);
	// Editor editor = prefs.edit();
	// editor.putInt(DIAMOND_COUNT, mCount);
	// editor.commit();
	// }
	// public static int readDiamondCount(Context context){
	// SharedPreferences prefs =
	// context.getSharedPreferences(SAVE_DIAMOND_COUNT, Context.MODE_PRIVATE);
	// int mCount =prefs.getInt(DIAMOND_COUNT, 0);
	// return mCount;
	// }
	// public static void clearDiamondCount(Context context){
	// SharedPreferences prefs =
	// context.getSharedPreferences(SAVE_DIAMOND_COUNT, Context.MODE_PRIVATE);
	// Editor editor = prefs.edit();
	// editor.remove(DIAMOND_COUNT);
	// editor.commit();
	// }
	public static void saveMyBitmap(String bitName, Bitmap mBitmap) {
		File f = new File("/sdcard/" + bitName + ".png");
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
		mBitmap.compress(CompressFormat.PNG, 100, fOut);
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

	public static void saveMyBitmap(String bitName, byte[] data) {
		File f = new File("/sdcard/" + bitName + ".png");
		try {
			f.createNewFile();
		} catch (IOException e) {
		}
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(f);
			os.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean DateInPeriod(int beginYear, int beginMonth,
			int beginDate, int beginHour, int endYear, int endMonth,
			int endDate, int endHour) {
		String bgDateStr = beginYear + "-" + beginMonth + "-" + beginDate + " "
				+ beginHour;
		String endDateStr = endYear + "-" + endMonth + "-" + endDate + " "
				+ endHour;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
		Date curDate = new Date(System.currentTimeMillis());// 锟斤拷取锟斤拷前时锟斤拷
		String curDateStr = df.format(curDate);
		try {
			Date dtBegine = df.parse(bgDateStr);
			Date dtEnd = df.parse(endDateStr);
			Date currDate = df.parse(curDateStr);
			if (dtBegine.getTime() <= currDate.getTime()
					&& currDate.getTime() <= dtEnd.getTime()) {
				Log.d(TAG, "true");
				return true;
			} else {
				Log.d(TAG, "false");
				return false;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
}
