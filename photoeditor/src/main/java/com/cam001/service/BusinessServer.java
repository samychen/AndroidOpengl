package com.cam001.service;

import android.graphics.Point;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BusinessServer {

	private static final String TAG = "BusinessServer";
	
//	private static final String HOST = "api.camtoolbar.com";
    private static final String HOST = "120.26.205.60";

	private static final String APP_KEY = "52802FB350154AC38F20EF525C98801F";
	private static final String APP_SECRET = "D21978667CBA4CBABBF3989C081A6273";

	private static final String ACTION_REGISTER = "/device/regkey";
    private static final String ACTION_SIGNIN = "/device/signin";
    private static final String ACTION_GETAD = "/device/ad";
    private static final String ACTION_PUSH = "/device/push";
	
	//Auto generated params.
	private static final String KEY_APPKEY = "appkey";
	private static final String KEY_RANDOM = "nonce";
	private static final String KEY_SIGNATURE = "sig";
	private static final String KEY_TIME = "timestamp";
    private static final String KEY_DEVID = "mi";
	
	//User defined params.
    private static final String KEY_LANGUAGE = "lang";
    private static final String KEY_VERSION = "cver";
    private static final String KEY_DEVICE = "device";
    private static final String KEY_MEMORY = "mem";
    private static final String KEY_IMEI = "imei";
    private static final String KEY_IMSI = "imsi";
    private static final String KEY_CPU = "soc";
    private static final String KEY_COMPANY = "company";
    private static final String KEY_RESOLUTAON = "dpi";
    private static final String KEY_CHANNEL = "chl";
    private static final String KEY_TIMEZONE = "tz";
    private static final String KEY_COUNTRY = "ctry";
    private static final String KEY_CERT = "apk";
    private static final String KEY_PKGNAME = "appid";
    private static final String KEY_LATITUDE = "lat";
    private static final String KEY_LONGITUDE = "long";

    private static final String KEY_ADID = "code";
    private static final String KEY_ADVERSION = "cver";

    private Config mConfig = null;

	public BusinessServer(Config config) {
        mConfig = config;
	}
	
	public boolean register() {
        LogUtil.logV(TAG, "register <-----");
		LinkedList<Param> params = new LinkedList<Param>();
        params.add(new Param(KEY_LANGUAGE, mConfig.getLanguage()));
        params.add(new Param(KEY_DEVICE, mConfig.getModelName()));
		params.add(new Param(KEY_VERSION, mConfig.getVersionCode()));
        params.add(new Param(KEY_MEMORY, mConfig.getMemorySize()));
        params.add(new Param(KEY_IMEI, mConfig.getIMEI()));
        params.add(new Param(KEY_IMSI, mConfig.getIMSI()));
        params.add(new Param(KEY_CPU, mConfig.getCpu()));
        params.add(new Param(KEY_COMPANY, mConfig.getCompany()));
        params.add(new Param(KEY_TIMEZONE, mConfig.getTimeZone()));
        params.add(new Param(KEY_COUNTRY, mConfig.getCountry()));
        params.add(new Param(KEY_CHANNEL, mConfig.getChannel()));
        Point res = mConfig.getResolution();
        params.add(new Param(KEY_RESOLUTAON, res.x+"x"+res.y));
        params.add(new Param(KEY_CERT, mConfig.getCertInfo()));
        params.add(new Param(KEY_PKGNAME, mConfig.getPackageName()));

		String respJson = requestPost(ACTION_REGISTER, params);
        LogUtil.logV(TAG, "register -----> "+respJson);
		Response response = new Response(respJson);
		if(response.isSuccess()) {
			return true;
		}
		LogUtil.logE(TAG, "register failed: msg="+response.mMsg);
		return false;
	}

    public Response signin() {
        LogUtil.logV(TAG, "signin <-----");
        LinkedList<Param> params = new LinkedList<Param>();
        params.add(new Param(KEY_LANGUAGE, mConfig.getLanguage()));
        params.add(new Param(KEY_VERSION, mConfig.getVersionCode()));
        params.add(new Param(KEY_TIMEZONE, mConfig.getTimeZone()));
        params.add(new Param(KEY_COUNTRY, mConfig.getCountry()));
        params.add(new Param(KEY_PKGNAME, mConfig.getPackageName()));
        params.add(new Param(KEY_LATITUDE, mConfig.getLatitude()));
        params.add(new Param(KEY_LONGITUDE, mConfig.getLongitude()));

        String respJson = requestPost(ACTION_SIGNIN, params);
        LogUtil.logV(TAG, "signin -----> "+respJson);
        return new Response(respJson);
    }

    public Response getAd(String id, int curVersion) {
        LogUtil.logV(TAG, "getAd <----- id=%s ver=%d", id, curVersion);
        LinkedList<Param> params = new LinkedList<Param>();
        params.add(new Param(KEY_LANGUAGE, mConfig.getLanguage()));
        params.add(new Param(KEY_ADID, id));
        params.add(new Param(KEY_ADVERSION, curVersion));

        String respJson = requestGet(ACTION_GETAD, params);
        LogUtil.logV(TAG, "getAd -----> "+respJson);
        return new Response(respJson);
    }



    public Response push() {
        LogUtil.logV(TAG, "push <-----");
        LinkedList<Param> params = new LinkedList<Param>();
        params.add(new Param(KEY_LANGUAGE, mConfig.getLanguage()));
        params.add(new Param(KEY_VERSION, mConfig.getVersionCode()));
        params.add(new Param(KEY_TIMEZONE, mConfig.getTimeZone()));
        params.add(new Param(KEY_COUNTRY, mConfig.getCountry()));
        params.add(new Param(KEY_PKGNAME, mConfig.getPackageName()));
        params.add(new Param(KEY_LATITUDE, mConfig.getLatitude()));
        params.add(new Param(KEY_LONGITUDE, mConfig.getLongitude()));

        String respJson = requestPost(ACTION_PUSH, params);
        LogUtil.logV(TAG, "push -----> "+respJson);
        return new Response(respJson);
    }

    private String requestPost(String action, List<Param> params) {
        String url = buildRequestUrl(action);
        LogUtil.logV(TAG, "Request Post: " + url);
        params = buildRequestParams(action, params);
        HashMap<String, String> map = new HashMap<String, String>();
        String contents = buildRequestForm(params, map);
        LogUtil.logV(TAG, "Post Content: "+contents);
        String resp = Util.HTTPRequest(url, contents, map);
        return resp;
    }

    private String requestGet(String action, List<Param> params) {
        String url = buildRequestUrl(action);
        String p = buildRequestString(action, params);
        url += "?"+p;
        LogUtil.logV(TAG, "Request Get: "+url);
        String resp = Util.HTTPRequest(url, null, null);
        return resp;
    }

	private String buildRequestUrl(String action) {
		//Generate HTTP URL.
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(HOST);
		sb.append(action);
		return sb.toString();
	}

    private List<Param> buildRequestParams(String action, List<Param> params) {
        params.add(new Param(KEY_APPKEY, APP_KEY));
        params.add(new Param(KEY_DEVID, mConfig.getDeviceId()));
        //Added random number to defend redo.
        params.add(new Param(KEY_RANDOM, mConfig.getNonce()));
        //Added timestamp;
        params.add(new Param(KEY_TIME, mConfig.getTimeStamp()));

        String signature = genSignature(action, params);
        params.add(new Param(KEY_SIGNATURE, signature));
        return params;
    }

    private String buildRequestString(String action, List<Param> params) {
        params.add(new Param(KEY_APPKEY, APP_KEY));
        params.add(new Param(KEY_DEVID, mConfig.getDeviceId()));
        //Added random number to defend redo.
        params.add(new Param(KEY_RANDOM, mConfig.getNonce()));
        //Added timestamp;
        params.add(new Param(KEY_TIME, mConfig.getTimeStamp()));

        String signature = genSignature(action, params);
        //Generate HTTP URL.
        StringBuilder sb = new StringBuilder();
        for(Param p: params) {
            if(p.value==null) continue;
            sb.append(p.toString());
            sb.append('&');
        }
        sb.append(new Param(KEY_SIGNATURE, signature).toString());
        return sb.toString();
    }

    private String genSignature(String action, List<Param> params) {
        //Generate MD5 singnature.
        Collections.sort(params, new Param(null,null));
        StringBuilder sb = new StringBuilder();
        sb.append(APP_SECRET);
        sb.append('&');
        sb.append(action);
        for(Param p: params) {
            if(p.value==null) continue;
            sb.append('&');
            sb.append(p.toString());
        }
        String str = sb.toString();
        String signature = Util.getMD5(str);
        LogUtil.logV(TAG, "Gen Sig: %s %s", str, signature);
        return signature;
    }
	
	private String buildRequestForm(List<Param> params, Map<String, String> map) {
        int boundary = 123456789;//new Random().nextInt();
        String header = String.format("--%d\n", boundary);
        String footer = String.format("--%d--\n", boundary);
        
        map.clear();
        map.put("Content-Type", "multipart/form-data; boundary="+boundary);
        
		StringBuilder sb = new StringBuilder();
        for(Param p: params) {
            if(p.value==null) continue;
            sb.append(header);
            sb.append(String.format("Content-Disposition: form-data; name=\"%s\"\n", p.key));
            sb.append("Content-Type: text/plain; charset=US-ASCII\n");
            sb.append("Content-Transfer-Encoding: 8bit\n");
            sb.append("\n");
            sb.append(URLEncoder.encode(p.value));
            sb.append("\n");
        }
        sb.append(footer);
		return sb.toString();
	}
	
	private static class Param implements Comparator<Param> {
		private String key;
		private String value;
		public Param(String k, String v) {
			key = k;
			value = v;
		}
        public Param(String k, int v) {
            key = k;
            value = String.valueOf(v);
        }
        public Param(String k, long v) {
            key = k;
            value = String.valueOf(v);
        }
		@Override
		public String toString() {
			return key+"="+URLEncoder.encode(value);
		}
		@Override
		public int compare(Param arg0, Param arg1) {
			return arg0.key.compareTo(arg1.key);
		}
	}
	
	public static class Response {
		private String mJson = null;
		private boolean mIsLoaded = false;
		private int mRc = -1;
        private int mVerCfg = 0;

        private boolean mIsAdOn = false;
		private String mMsg = null;
        private Ad mAd = null;
        private Push mPush = null;

		public Response(String json) {
			mJson = json;
		}
		public boolean isSuccess() {
			load();
			return mRc==0;
		}

        public boolean needUpdate() {
            load();
            return mVerCfg!=0;
        }

        public boolean forceUpdate() {
            load();
            return mVerCfg==2;
        }

        public  boolean isAdOn() {
            load();;
            return mIsAdOn;
        }

        public Ad getAd() {
            load();;
            LogUtil.logV(TAG, "img="+mAd.imguri);
            return mAd;
        }

        public Push getPush() {
            load();
            return mPush;
        }

		private void load() {
			if(mIsLoaded) return;
			try {
				JSONObject root = new JSONObject(mJson);
				mRc = root.getInt("rc");
				if(mRc!=0) {
					mMsg = root.getString("message");
					LogUtil.logE(TAG, mMsg);
				}
                if(root.has("versionconfig")) {
                    mVerCfg = root.getInt("versionconfig");
                }
                if(root.has("adview")) {
                    mIsAdOn = root.getBoolean("adview");
                }
                mAd = new Ad();
                mPush = new Push();
                if(root.has("state")) {
                    mAd.needUpdate = root.getBoolean("state");
                }
                if(root.has("code")) {
                    mAd.id = root.getString("code");
                }
                if(root.has("text")) {
                    mAd.text= root.getString("text");
                }
                if(root.has("imgurl")) {
                    mAd.imguri= URLDecoder.decode(root.getString("imgurl"));
                }
                if(root.has("action")) {
                    mAd.action= root.getInt("action");
                }
                if(root.has("data")) {
                    mAd.data= URLDecoder.decode(root.getString("data"));
                    mPush.data= URLDecoder.decode(root.getString("data"));
                }
                if(root.has("id")) {
                    mPush.id = root.getInt("id");
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			mIsLoaded = true;
		}
	}

    public static class Ad {
        public String id;
        public boolean needUpdate;
        public String imguri;
        public String text;
        public int action;
        public String data;
        public int version;
    }

    public static class Push {
        public int id;
        public String action;
        public String data;
    }

}
