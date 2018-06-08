package com.cam001.share;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.cam001.photoeditor.R;
import com.cam001.util.ToastUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

public class WeChatTool {
	private static final String TAG = "WeChatTool";
	private Context mContext;
	public String mFilePath = null;
	private IWXAPI api = null;// wechat
	private boolean isReg = false;

	public interface WXConfig {
		static final String APP_ID = "wxdab31c9295153d06";
		static final String APP_SECRET = "799105f3b0051309b112f05dda719ad8";
	}
	
	public void Upload(final String filePath) {
		if(!isWxInstalled()){
			ToastUtil.showShortToast(mContext, R.string.wechat_notinstall_alert);
			return;
		}
		mFilePath = filePath;
		WXImageObject imgObj = new WXImageObject();
		imgObj.imagePath = filePath;
		
		WXMediaMessage msg = new WXMediaMessage(imgObj);
		msg.title = "Message Title";
		msg.description = "Message Description";
		int orientation = getExifOrientation(filePath);
		Bitmap thumbBmp = scaleBitmap(null, orientation);
		if (thumbBmp == null) {
			return;
		}
		msg.thumbData = bmpToByteArray(thumbBmp, true);

		
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = "SendMessageToWX.Req" + System.currentTimeMillis();
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
		boolean isSendOk = api.sendReq(req);
		Log.d(TAG, "SendMessageToWX.Req " + isSendOk);
	}
	
	public void UploadFG(final String filePath) {
		if (!isWxInstalled()) {
			ToastUtil.showShortToast(mContext, R.string.wechat_notinstall_alert);
			return;
		}
		mFilePath = filePath;
		WXImageObject imgObj = new WXImageObject();
		imgObj.imagePath = filePath;

		WXMediaMessage msg = new WXMediaMessage(imgObj);
		msg.title = "Message Title";
		msg.description = "Message Description";
		int orientation = getExifOrientation(filePath);
		Bitmap thumbBmp = scaleBitmap(null, orientation);
		if (thumbBmp == null) {
			return;
		}
		msg.thumbData = bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = "SendMessageToWX.Req" + System.currentTimeMillis();
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		boolean isSendOk = api.sendReq(req);
		Log.d(TAG, "SendMessageToWX.Req " + isSendOk);
	}

	public void UploadFGWebpage(int thumb, String title, String myappurl){
		if (!isWxInstalled()) {
			ToastUtil.showShortToast(mContext, R.string.wechat_notinstall_alert);
			return;
		}
		WXWebpageObject imgObj = new WXWebpageObject();
		imgObj.webpageUrl = myappurl;
//		imgObj.webpageUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.cam001.filtercollage";
		WXMediaMessage msg = new WXMediaMessage(imgObj);
		msg.title = title;
		msg.description = "Message Description";
		
		Bitmap thumbBmp = scaleBitmap(thumb);
		if (thumbBmp == null) {
			return;
		}
		msg.thumbData = bmpToByteArray(thumbBmp, true);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = "SendMessageToWX.Req" + System.currentTimeMillis();
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		boolean isSendOk = api.sendReq(req);
		Log.d(TAG, "SendMessageToWX.Req " + isSendOk);
	}
	
	private Bitmap scaleBitmap(int thumbId) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(mContext.getResources(), thumbId, options);
		options.inSampleSize = computeSampleSize(options, 200, 10 * 1024);
		options.inJustDecodeBounds = false;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), thumbId, options);
		return bmp;
	}

	public boolean isEmpty() {
		return false;
	}

	public WeChatTool(Context mContext) {
		super();
		this.mContext = mContext;
		if (api == null) {
			api = WXAPIFactory.createWXAPI(mContext, WXConfig.APP_ID, false);
			isReg = api.registerApp(WXConfig.APP_ID);
		}
	}
	
	private static WeChatTool mWeChatTool = null;
	public static WeChatTool getInstance(Context mContext){
		if(mWeChatTool == null){
			mWeChatTool = new WeChatTool(mContext);
		}
		return mWeChatTool;
	}
	
	private boolean isWxInstalled() {
		if (api == null) {
			api = WXAPIFactory.createWXAPI(mContext, WXConfig.APP_ID, false);
		}
		int i = 0;
		while (!isReg) {
			isReg = api.registerApp(WXConfig.APP_ID);
			i++;
			if (i == 10) {
				Log.d(TAG, "WechatCan not regist");
				break;
			}
		}
		return api.isWXAppInstalled();
	}
	
	 private int getExifOrientation(String filepath) {
	        int degree = 0;
	        if(filepath == null) {
	            return 0;
	        }
	        ExifInterface exif = null;
	        try {
	            exif = new ExifInterface(filepath);
	        } catch (IOException ex) {
	            Log.e(TAG, "cannot read exif", ex);
	        }
	        if (exif != null) {
	            int orientation = exif.getAttributeInt(
	                ExifInterface.TAG_ORIENTATION, -1);
	            if (orientation != -1) {
	                // We only recognize a subset of orientation tag values.
	                switch(orientation) {
	                    case ExifInterface.ORIENTATION_ROTATE_90:
	                        degree = 90;
	                        break;
	                    case ExifInterface.ORIENTATION_ROTATE_180:
	                        degree = 180;
	                        break;
	                    case ExifInterface.ORIENTATION_ROTATE_270:
	                        degree = 270;
	                        break;
	                }

	            }
	        }
	        return degree;
	    }
	 
	 private Bitmap scaleBitmap(Bitmap bitmap, int orientation) {
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        if(mFilePath != null)
	        	BitmapFactory.decodeFile(mFilePath, options);
	        else
	        	return null;
	        options.inSampleSize = computeSampleSize(options, 200, 10 * 1024);
	        options.inJustDecodeBounds = false;
	        options.inDither = false;
	        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	        if(mFilePath != null){
	        	Bitmap bmp = BitmapFactory.decodeFile(mFilePath, options);
	        	if(orientation != 0)
	        		return WeChatTool.rotate(bmp, orientation);
	        	return bmp;
	        }
	        else 
	        	return null;
	    }
	 
	 private int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	        int initialSize = computeInitialSampleSize(options, minSideLength,
	                maxNumOfPixels);
	        int roundedSize;
	        if (initialSize <= 8) {
	            roundedSize = 1;
	            while (roundedSize < initialSize) {
	                roundedSize <<= 1;
	            }
	        } else {
	            roundedSize = (initialSize + 7) / 8 * 8;
	        }
	        return roundedSize;
	    }
	 
	 private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	        double w = options.outWidth;
	        double h = options.outHeight;
	        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
	        if (upperBound < lowerBound) {
	            return lowerBound;
	        }
	        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
	            return 1;
	        } else if (minSideLength == -1) {
	            return lowerBound;
	        } else {
	            return upperBound;
	        }
	    }
	 
	 public static Bitmap rotate(Bitmap b, int degrees) {
	        if (degrees != 0 && b != null) {
	            Matrix m = new Matrix();
	            m.setRotate(degrees,
	                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
	            try {
	                Bitmap b2 = Bitmap.createBitmap(
	                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
	                if (b != b2) {
	                    b.recycle();
	                    b = b2;
	                }
	            } catch (OutOfMemoryError ex) {
	                // We have no memory to rotate. Return the original bitmap.
	            }
	        }
	        return b;
	    }
	 
	 public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	        bmp.compress(CompressFormat.PNG, 100, output);
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
}
