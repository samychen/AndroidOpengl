package com.cam001.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AdAgent {

	private static final String TAG = "AdAgent";

    private static final String PREF_FIRSTLOAD = TAG+"_fist_load";
    private static final String PREF_ADON = TAG+"_ad_on";
    private static final String PREF_NEEDUPDATE = TAG+"_need_update";
    private static final String PREF_FOREUPDATE = TAG+"_force_update";

    private static final String PREF_AD_VERSION = "_ad_version";
    private static final String CACHE_IMAGE = "_image";
    private static final String CACHE_TEXT = "_text";
    private static final String CACHE_ACTION = "_action";
    private static final String CACHE_DATA = "_data";

    private static final String PREF_PUSH_ID = "_push_id";

    public static final int ACTION_NONE = 0;
    public static final int ACTION_VIEW = 1;
    public static final int ACTION_ACTION = 2;
    public static final int ACTION_CLASS = 3;
    public static final int ACTION_INSTALL = 4;

    public static AdAgent sInst = null;
    public static AdAgent instance() {
    	if(sInst==null) {
    		sInst = new AdAgent();
    	}
    	return sInst;
    }
    
    private Context mContext = null;
    private Config mConfig = null;
    private BusinessServer mServer = null;
    private Handler mHandler = new Handler();
    private boolean mbInit = false;
    
	private AdAgent() {
	}
	
	public void init(Context c, String channel) {
		if(mbInit) return;
        mContext = c;
        mConfig = new Config(c, channel);
        mServer = new BusinessServer(mConfig);
        mbInit = true;
	}
	
	public void register() {
		new RegThread().start();
        new PushThread().start();
	}
	
    public boolean isAdOn() {
        return mConfig.getPreferenceBool(PREF_ADON);
//        return false;
    }

    public boolean needUpdate() {
        return mConfig.getPreferenceBool(PREF_NEEDUPDATE);
//    	return false;
    }

    public boolean forceUpdate() {
        return mConfig.getPreferenceBool(PREF_FOREUPDATE);
    }

    public void fillAd(String adId,View view, ImageView imgView, TextView txtView, View clickView) {
        fillAdWidthCache(adId, imgView, txtView, clickView);
        new AdThread(adId, view,imgView, txtView).start();
    }

    private void fillAdWidthCache(String adId, ImageView imgView, TextView txtView, View clickView) {
//        if(!isAdOn()) return;
        Bitmap img = CacheUtil.getCachedBitmap(mContext, adId + CACHE_IMAGE);
        String text = CacheUtil.getCachedString(mContext, adId + CACHE_TEXT);
        final int action = CacheUtil.getCachedInt(mContext, adId + CACHE_ACTION);
        final String data = CacheUtil.getCachedString(mContext, adId + CACHE_DATA);
        if(imgView!=null) imgView.setImageBitmap(img);
        if(txtView!=null) txtView.setText(text);
        if(clickView!=null) clickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               switch (action) {
                   case ACTION_NONE:
                       processActionNone(data);
                       break;
                   case ACTION_VIEW:
                       processActionView(data);
                       break;
                   case ACTION_ACTION:
                       processActionAction(data);
                       break;
                   case ACTION_CLASS:
                       processActionClass(data);
                       break;
                   case ACTION_INSTALL:
                       processActionInstall(data);
                       break;
                   default:
                       break;
               }
            }
        });
    }

    private void processActionNone(String data) {};

    private void processActionView(String data) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(data);
        intent.setData(content_url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void processActionAction(String data) {
        Intent intent = new Intent();
        intent.setAction(data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void processActionClass(String data) {
        try {
            Intent intent = new Intent(mContext, Class.forName(data));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processActionInstall(String data) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(data);
        intent.setData(content_url);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private class RegThread extends Thread {
    	
    	public RegThread() {
    		setName("RegThread");
    	}
    	
    	@Override
    	public void run() {
            //One user only register once.
            if(mConfig.getPreferenceInt(PREF_FIRSTLOAD)==0) {
                boolean bSuc = mServer.register();
                if(bSuc) mConfig.setPreferenceInt(PREF_FIRSTLOAD, 1);
            }
            //Only signin once each day.
            long day = System.currentTimeMillis()/(1000*60*60*24);
            if(mConfig.getPreferenceInt(PREF_FIRSTLOAD)<day) {
                BusinessServer.Response resp = mServer.signin();
                if (resp.isSuccess()) {
                    mConfig.setPreferenceInt(PREF_FIRSTLOAD, (int) day);
                    mConfig.setPreferenceBool(PREF_ADON, resp.isAdOn());
                    mConfig.setPreferenceBool(PREF_NEEDUPDATE, resp.needUpdate());
                    mConfig.setPreferenceBool(PREF_FOREUPDATE, resp.forceUpdate());
                }
            }
    	}
    	
    }
    
    private class AdThread extends Thread {

        private String mAdId = null;
        View mView = null;
        ImageView mImgView = null;
        TextView mTxtView = null;

        public AdThread(String adId,View view, ImageView imgView, TextView txtView) {
            mAdId = adId;
            mView = view;
            mImgView = imgView;
            mTxtView = txtView;
            setName("AdThread");
        }

        @Override
        public void run() {
            int adVersion = mConfig.getPreferenceInt(mAdId + PREF_AD_VERSION);
            BusinessServer.Response resp = mServer.getAd(mAdId, adVersion);
            if(!resp.isSuccess()) {
                mView.setVisibility(View.GONE);
                return;
            }

            BusinessServer.Ad ad = resp.getAd();
            if(ad.needUpdate) {
                CacheUtil.cachepHttpFile(mContext, mAdId + CACHE_IMAGE, ad.imguri);
                CacheUtil.cacheString(mContext, mAdId + CACHE_TEXT, ad.text);
                CacheUtil.cacheInt(mContext, mAdId + CACHE_ACTION, ad.action);
                CacheUtil.cacheString(mContext, mAdId + CACHE_DATA, ad.data);
                mConfig.setPreferenceInt(mAdId+PREF_AD_VERSION, ad.version);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fillAdWidthCache(mAdId, mImgView, mTxtView, null);
                    }
                });
            }
        }

    }

    private class PushThread extends Thread {

        private static final int INTERVAL = 1000*60*60;

        public PushThread() {
            setName("PushThread");
        }

        @Override
        public void run() {
            int localId = mConfig.getPreferenceInt(PREF_PUSH_ID);
            int interval = 10000;
            while(true) {
                try {
                    Thread.sleep(interval);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                interval = INTERVAL;
                BusinessServer.Response resp = mServer.push();
                if(!resp.isSuccess()) continue;
                BusinessServer.Push push = resp.getPush();
            }
        }
    }
 }
