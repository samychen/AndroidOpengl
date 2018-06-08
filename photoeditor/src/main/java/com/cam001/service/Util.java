package com.cam001.service;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

public class Util {

	private static final String TAG  = "Util";
	
    // The brightness setting used when it is set to automatic in the system.
    // The reason why it is set to 0.7 is just because 1.0 is too bright.
    // Use the same setting among the Camera, VideoCamera and Panorama modes.
    private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;
	
    public static void initializeScreenBrightness(Window win, ContentResolver resolver) {
        // Overright the brightness settings if it is automatic
        int mode = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
            win.setAttributes(winParams);
        }
    }
	
	public static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
    public static void Assert(boolean cond) {
    	if(!LogUtil.DEBUG) {
    		return;
    	}
        if (!cond) {
            throw new AssertionError();
        }
    }
	
    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }
    
    public static void joinThreadSilently(Thread t) {
    	if(t == null) return;
    	try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public static void closeSilently(ParcelFileDescriptor c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }
    
    public static void dumpToFile(byte[] data, String path) {
    	FileOutputStream os = null;
    	try {
			os = new FileOutputStream(path);
			os.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeSilently(os);
		}
    }
    
    public static void dumpNv21ToJpeg(byte[] nv21, int width, int height, String path) {
    	FileOutputStream os = null;
    	try {
			os = new FileOutputStream(path);
			Rect rect = new Rect(0,0,width,height);
			YuvImage img = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
			img.compressToJpeg(rect, 85, os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeSilently(os);
		}
    }
    
    public static Rect RectFtoRect(RectF r) {
    	return new Rect((int)r.left, (int)r.top, (int)r.right, (int)r.bottom);
    }
	
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f:files) {
                deleteDir(f);
            }
            dir.delete();
        } else {
            dir.delete();
        }
    } 
	
	public static String HTTPRequest(String urlStr, String content, Map<String, String> headers) {
		HttpURLConnection con = null;
		InputStream is = null;
		InputStreamReader reader = null;
		OutputStream os = null;
		OutputStreamWriter writer = null;
		try {
			URL url = new URL(urlStr);
			con = (HttpURLConnection)url.openConnection();
			con.setDoInput(true);
			con.setInstanceFollowRedirects(true);
			con.setConnectTimeout(15000);
			if(content!=null) {
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				if(headers!=null) {
					Iterator<Map.Entry<String, String>> i = headers.entrySet().iterator();
					if(i.hasNext()) {
						Map.Entry<String, String> entry = i.next();
						con.setRequestProperty(entry.getKey(),entry.getValue());
					}
				}
			}
			con.connect();
			if(content!=null) {
				os = con.getOutputStream();
				writer = new OutputStreamWriter(os);
				writer.write(content);
                writer.flush();
				writer.close();
				writer = null;
			}
			int httpCode = con.getResponseCode();
			LogUtil.logV(TAG, "ResponseCode = %d", httpCode);
			if(httpCode/100!=2) {
				return null;
			}
			is = con.getInputStream();
			reader = new InputStreamReader(is);
			char[] buffer = new char[512];
			int len = 0;
			StringBuilder sb = new StringBuilder();
			while((len=reader.read(buffer))>0) {
				sb.append(buffer, 0, len);
			}
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Util.closeSilently(reader);
			Util.closeSilently(is);
			Util.closeSilently(writer);
			Util.closeSilently(os);
			if(con!=null) con.disconnect();
		}
		return null;
	}
	
	public static Bitmap decodeBitmapHttp(String urlStr) {
		HttpURLConnection con = null;
		InputStream is = null;
		Bitmap res = null;
		try {
			URL url = new URL(urlStr);
			con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(true);
			con.connect();
			int httpCode = con.getResponseCode();
			LogUtil.logV(TAG, "ResponseCode = %d", httpCode);
			if (httpCode / 100 != 2) {
				return null;
			}
			is = con.getInputStream();
			res = BitmapFactory.decodeStream(is);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			Util.closeSilently(is);
			if (con != null)
				con.disconnect();
		}
		return res;
	}
	      
	/**
	 * ���ַ�ת��MD5ֵ
	 * 
	 * @param string
	 * @return
	 */
	public static String getMD5(String string) {
		byte[] hash;

		try {
			hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}

		return hex.toString();
	}
	
	public static boolean saveBmp(Bitmap bitmap, String name) {
		File pf = new File(name);
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(pf);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
			stream.flush();
			stream.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}
}
