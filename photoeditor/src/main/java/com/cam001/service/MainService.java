package com.cam001.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cam001.util.CompatibilityUtil;

public class MainService extends Service {
	
	private static final String TAG = "MainService";
	
//	private PushCommunicationThread.OnSocketRecieveCallBack mCallback = new PushCommunicationThread.OnSocketRecieveCallBack() {
//
//		@Override
//		public void OnRecieveFromServerMsg(String msg) {
//			LogUtil.logV(TAG, "OnRecieveFromServerMsg: "+msg);
//			ShowNotification showNotification = new ShowNotification(MainService.this,msg);
//			showNotification.showNotification_default();
//		}
//		
//	};
	
	@Override
	public void onCreate() {
		LogUtil.logV(TAG, "onCreate");
		super.onCreate();
//		new PushCommunicationThread(mCallback).start();
		AdAgent.instance().init(getApplicationContext(), 
				CompatibilityUtil.getUMengChannel(this));
		AdAgent.instance().register();
	}

	@Override
	public void onDestroy() {
		LogUtil.logV(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		LogUtil.logV(TAG, "onBind");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String uri = null;
		if(intent!=null) {
			uri = intent.getStringExtra("downloadURI");
		}
		if(uri != null)
		{
			DownloadThread downloadThread = new DownloadThread(MainService.this);
			downloadThread.StartDownload(intent);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
}
