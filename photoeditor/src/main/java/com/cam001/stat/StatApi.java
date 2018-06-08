/*
 * Copyright (C) 2011,2013 Thundersoft Corporation
 * All rights Reserved
 */
package com.cam001.stat;

import android.app.Activity;
import android.content.Context;

import java.util.Map;

import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.onlineconfig.UmengOnlineConfigureListener;

public class StatApi {
	public static final String UMENG_EVENT_btnShareFacebook = "btnShareFacebook";
	public static final String UMENG_EVENT_btnShareTwitter = "btnShareTwitter";
	public static final String UMENG_EVENT_btnShareInstagram = "btnShareInstagram";
	public static final String UMENG_EVENT_btnSharePinterest = "btnSharePinterest";
	public static final String UMENG_EVENT_btnShareMore = "btnShareMore";
	public static final String UMENG_EVENT_btnShareCancel = "btnShareCancel";

	public static final String UMENG_EVENT_btnLighten = "btnLighten";
	public static final String UMENG_EVENT_btnSmooth = "btnSmooth";
	public static final String UMENG_EVENT_btnFoundation = "btnFoundation";
	public static final String UMENG_EVENT_btnSlim = "btnSlim";
	public static final String UMENG_EVENT_btnBigEye = "btnBigEye";
	public static final String UMENG_EVENT_btnDotsFix = "btnDotsFix";

	public static final String UMENG_EVENT_btnHome = "btnHome";
	public static final String UMENG_EVENT_btnSave = "btnSave";

	public static final String UMENG_EVENT_btnLightenSave = "btnLightenSave";
	public static final String UMENG_EVENT_btnLightenCancel = "btnLightenSaveCancel";
	public static final String UMENG_EVENT_btnSmoothSave = "btnSmoothSave";
	public static final String UMENG_EVENT_btnSmoothCancel = "btnSmoothCancel";
	public static final String UMENG_EVENT_btnFoundationSave = "btnFoundationSave";
	public static final String UMENG_EVENT_btnFoundationCancel = "btnFoundationCancel";
	public static final String UMENG_EVENT_btnSlimSave = "btnSlimSave";
	public static final String UMENG_EVENT_btnSlimCancel = "btnSlimCancel";
	public static final String UMENG_EVENT_btnBigEyeSave = "btnBigEyeSave";
	public static final String UMENG_EVENT_btnBigEyeCancel = "btnBigEyeCancel";
	public static final String UMENG_EVENT_btnDotsFixSave = "btnDotsFixSave";
	public static final String UMENG_EVENT_btnDotsFixCancel = "btnDotsFixCancel";


	private static final StatApiImpl sStatApi = new UmengStatApiImpl();
	public static String APPACKAGE_NAME = "appackage_name";

	private static StatApiImpl getStatApi() {
		return sStatApi;
	}

	public static void init(Context context) {
		StatApiImpl api = getStatApi();
		if (api != null && context != null) {
			api.init(context);
		}
	}
	
	public static void setDebugMode(Boolean arg0) {
		StatApiImpl api = getStatApi();
		if (api != null) {
			api.setDebugMode(arg0);
		}
	}

	public static void onPause(Activity context) {
		StatApiImpl api = getStatApi();
		if (api != null && context != null) {
			api.onPause(context);
		}
	}

	public static void onResume(Activity context) {
		StatApiImpl api = getStatApi();
		if (api != null && context != null) {
			api.onResume(context);
		}
	}

	public static void onEvent(Context context, String event) {
		StatApiImpl api = getStatApi();
		if (api != null) {
			/*
			 * BUG COMMENT: stat is not correct Date: 2014-03-10
			 */
			api.onEvent(context, event);
			// api.onEventBegin(context, event, param);
		}
	}

	public static void onEvent(Context context, String event,
			Map<String, String> params) {
		StatApiImpl api = getStatApi();
		if (api != null) {
			api.onEvent(context, event, params);
		}
	}

	public static void onEventBegin(Context context, String event, String param) {
		StatApiImpl api = getStatApi();
		if (api != null) {
			api.onEventBegin(context, event, param);
		}
	}

	public static void onEventEnd(Context context, String event, String param) {
		StatApiImpl api = getStatApi();
		if (api != null) {
			api.onEventEnd(context, event, param);
		}
	}

	public static void updateOnlineConfig(Context context) {
		StatApiImpl api = getStatApi();
		if (api != null) {
			api.updateOnlineConfig(context);
		}
	}

	public static void downloadResource(Context context, String type,
			String resid) {
		StatApiImpl api = getStatApi();
		if (api != null && type != null && resid != null) {
		}
	}

	public static String joinValue(String p, Object... remains) {
		StringBuilder builder = new StringBuilder();
		builder.append(p);
		for (Object o : remains) {
			builder.append(":").append(o);
		}
		return builder.toString();
	}

	public static String getResourceName(String s) {
		int x = s.lastIndexOf("/");
		if (x != -1) {
			s = s.substring(x + 1);
		}
		return s;
	}

	public static String getParam(Context context, String key) {
		StatApiImpl api = getStatApi();
		if (api != null && key != null) {
			return api.getConfigParams(context, key);
		}
		return null;// default values;
	}

	public static void reportError(Context c, String err) {
		StatApiImpl api = getStatApi();
		api.reportError(c, err);
	}

	public static void setOnlineConfigureListener(
			UmengOnlineConfigureListener umengOnlineConfigureListener) {
		MobclickAgent.setOnlineConfigureListener(umengOnlineConfigureListener);
	}
}
