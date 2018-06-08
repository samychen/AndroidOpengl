/*
 * Copyright (C) 2011,2012 Thundersoft Corporation
 * All rights Reserved
 */
package com.cam001.stat;

import java.util.Map;

import android.content.Context;

import com.cam001.util.CompatibilityUtil;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

class UmengStatApiImpl implements StatApiImpl {

	@Override
	public void init(Context context) {
		AnalyticsConfig.setChannel(CompatibilityUtil.getUMengChannel(context));
	}

	@Override
	public void updateOnlineConfig(Context context) {
		MobclickAgent.updateOnlineConfig(context);
		// MobclickAgent.onError(context);
	}

	@Override
	public void onEventBegin(Context context, String event, String param) {
		MobclickAgent.onEventBegin(context, event, param);
	}

	@Override
	public void onEventEnd(Context context, String event, String param) {
		MobclickAgent.onEventEnd(context, event, param);
	}

	@Override
	public void setDebugMode(Boolean arg0) {
		// TODO Auto-generated method stub
		MobclickAgent.setDebugMode(arg0);
	}

	@Override
	public void onEvent(Context context, String event,
			Map<String, String> params) {
		MobclickAgent.onEvent(context, event, params);
	}

	@Override
	public void onEvent(Context context, String event) {
		MobclickAgent.onEvent(context, event);
	}

	@Override
	public void onResume(Context context) {
		MobclickAgent.onResume(context);
	}

	@Override
	public void onPause(Context context) {
		MobclickAgent.onPause(context);
	}

	@Override
	public String getConfigParams(Context context, String key) {
		return MobclickAgent.getConfigParams(context, key);
	}

	@Override
	public void reportError(Context c, String err) {
		MobclickAgent.reportError(c, err);
	}

}
