/*
 * Copyright (C) 2011,2012 Thundersoft Corporation
 * All rights Reserved
 */
package com.cam001.stat;

import android.content.Context;

import java.util.Map;

interface StatApiImpl {
    public void init(Context context);
	public String getConfigParams(Context context, String key);
	
    public void updateOnlineConfig(Context context);

    public void onEventBegin(Context context, String event, String param);
    public void onEventEnd(Context context, String event, String param);

    public void onEvent(Context context, String event, Map<String, String> params);
    public void onEvent(Context context, String event);

    public void onResume(Context context);
    public void onPause(Context context);
    public void setDebugMode(Boolean arg0);
    
	public void reportError(Context c, String err);
}
