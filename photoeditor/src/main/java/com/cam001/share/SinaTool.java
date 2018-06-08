package com.cam001.share;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.cam001.photoeditor.R;
import com.cam001.photoeditor.ShareActivity;
import com.cam001.util.ToastUtil;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.api.StatusesAPI;
import com.weibo.sdk.android.api.keep.AccessTokenKeeper;
import com.weibo.sdk.android.net.HttpManager;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SinaTool {
	private static final String TAG = "SinaTool";
	private Context mContext;
	public String mFilePath = null, mContent = null;
	private SsoHandler mSsoHandler = null;
	private Weibo mWeibo = null;
	private Handler mHandler = new Handler();
	
	public interface SinaConfig {
		static final String APP_Key = "178681662";
		static final String APP_SECRET = "9962db9d8b4bb85065d7b70302f5bf1d";
		static final String REDIRECT_URL = "http://www.u-camera.com/";
		static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
				+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,";
	}
	public void Upload(final String filePath, final String content) {
		mFilePath = filePath;
		mContent = content;
		if (AccessTokenKeeper.readAccessToken(mContext)
				.isSessionValid()) {
			StatusesAPI statApi = new StatusesAPI(
					AccessTokenKeeper.readAccessToken(mContext));
			Location location = SinaTool.getLocation(mContext);
			String lat = "", lon = "";
			if (location != null) {
				lat = location.getLatitude() + "";
				lon = location.getLongitude() + "";
			}
			statApi.upload(mContent, filePath, lat, lon,new UploadRequestListener());
		}else{//jump to login
			Log.d(TAG, "into login");
			mSsoHandler.authorize(new AuthDialogListener());
		}
	}

	public boolean isEmpty() {
		return false;
	}

	public SinaTool(Context mContext) {
		super();
		this.mContext = mContext;
		mWeibo = Weibo.getInstance(SinaConfig.APP_Key, SinaConfig.REDIRECT_URL, SinaConfig.SCOPE);
		mSsoHandler = new SsoHandler( (ShareActivity)mContext, mWeibo);
	}

	private static SinaTool mSinaTool = null;

	public static SinaTool getInstance(Context mContext) {
		if (mSinaTool == null) {
			mSinaTool = new SinaTool(mContext);
		}
		return mSinaTool;
	}
	private class UploadRequestListener implements RequestListener{
		@Override
		public void onComplete(String arg0) {
			Log.d(TAG, "onComplete");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					ToastUtil.showShortToast(mContext, R.string.share_status_success);
				}
			});
		}

		@Override
		public void onComplete4binary(ByteArrayOutputStream arg0) {
			
		}

		@Override
		public void onError(WeiboException arg0) {
			Log.d(TAG, "onError"+arg0.toString());
		}

		@Override
		public void onIOException(IOException arg0) {
			Log.d(TAG, "onIOException"+arg0.toString());
			
		}
		
	}
	
	private class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onCancel() {
			
		}

		@SuppressLint("NewApi")
		@Override
		public void onComplete(Bundle values) {
			final String mTokenCode = values.getString("code");
			if (mTokenCode != null) {
				Log.d(TAG, "SinaAuth complete " + mTokenCode);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							WeiboParameters params = new WeiboParameters();
							params.add("client_id",
									SinaConfig.APP_Key);
							params.add("client_secret",
									SinaConfig.APP_SECRET);
							params.add("grant_type", "authorization_code");
							params.add("redirect_uri", SinaConfig.REDIRECT_URL);
							params.add("code", mTokenCode);
							String result = "";
							result = HttpManager
									.openUrl(
											"https://api.weibo.com/oauth2/access_token",
											"POST", params, null);
							JSONObject json = new JSONObject(result);
							String token = json.getString("access_token");
							String expires_in = json.getString("expires_in");
							Log.d(TAG, token + " " + expires_in);
							Oauth2AccessToken accessToken = new Oauth2AccessToken(
									token, expires_in);
							if (accessToken.isSessionValid()) {
								AccessTokenKeeper.keepAccessToken(
										mContext, accessToken);
								Log.d(TAG, "SinaAuth complete");
								Upload(mFilePath, mContent);
							}
						} catch (WeiboException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}).start();
				return;
			}
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			Log.d("token", token + " " + expires_in);
			Oauth2AccessToken accessToken = new Oauth2AccessToken(token,
					expires_in);
			if (accessToken.isSessionValid()) {
				AccessTokenKeeper.keepAccessToken(mContext,
						accessToken);
				Log.d(TAG, "SinaAuth complete");
				Upload(mFilePath, mContent);
			}
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			Log.d(TAG, "onWeiboException complete"+arg0);
		}

		@Override
		public void onError(WeiboDialogError arg0) {
			Log.d(TAG, "onError complete"+arg0);
		}
		
	}
	public static Location getLocation(Context context) {
		Location location = null;
		LocationManager loctionManager = null;
		loctionManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		location = loctionManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		return location;
	}

	public void authorizeCallBack(int requestCode, int resultCode, Intent data) {
		if(mSsoHandler != null)
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
	}
}
