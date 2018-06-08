package com.cam001.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONObject;


import android.R;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;


public class ShowNotification {
	
	private static String TAG = "ShowNotification";
	
	private static String TYPE_OPENACTION = "1";
	private static String TYPE_OPENWEB = "2";
	private static String TYPE_DOWNLOAD = "3";
	private static String TYPE_OPENPACKAGE = "4";
	
	private static String ALERT_NULL = "0";
	private static String ALERT_VIBRATE_SOUND = "1";
	private static String ALERT_VIBRATE = "2";
	private static String ALERT_SOUND = "3";
	
	//push message
	private String tickerText = null;
    private String message = null;
    private String description = null;
	private String icon = null;
    private String type = null;
    private String action = null;
    private String alert = null;
    private String userdata = null;
    
    private Context serviceContext = null;
    
    private Notification messageNotification = null;
    private NotificationManager messageNotificationManager = null;
	private Intent messageIntent = null;
	private PendingIntent messagePendingIntent = null;
	private Builder notificationBuilder = null;

	public ShowNotification(Context serviceContext, String msg) {
		super();
		
		try {
			String json = msg;
			JSONObject obj = new JSONObject(json);
			this.tickerText = obj.getString("title");
			this.message = obj.getString("title");
			this.description = obj.getString("message");
			this.icon = obj.getString("icon");
			this.type = obj.getString("type");
			this.action = obj.getString("action");
			this.alert = obj.getString("alert");	
			if(obj.has("userdata")) {
				this.userdata = obj.getString("userdata");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		this.serviceContext = serviceContext;
		
		notificationBuilder = new Builder(serviceContext);
		messageNotification = new Notification();
	}


	public void showNotification_default()
	{
		if(message == null ||
				description == null ||
				icon == null ||
				type == null ||
				action == null ||
				alert == null)
		{return ;}
		
		//alert
		int alert_defaults = 0;
		if(alert.equals(ALERT_NULL))
		{
			alert_defaults = 0;
		}else if(alert.equals(ALERT_VIBRATE_SOUND))
		{
			alert_defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
		}else if(alert.equals(ALERT_VIBRATE))
		{
			alert_defaults = Notification.DEFAULT_VIBRATE;
		}else if(alert.equals(ALERT_SOUND))
		{
			alert_defaults = Notification.DEFAULT_SOUND;
		}else
		{
			alert_defaults = 0;
		}
		
		//type
		if(type.equals(TYPE_OPENACTION))
		{
			//open app
		    messageIntent=new Intent();   
		    //messageIntent.setComponent(componentName);   
		    messageIntent.setAction(action);     
		    messageIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		    if(userdata!=null) {
		    	messageIntent.putExtra("userdata", userdata);
		    }
		    messagePendingIntent = PendingIntent.getActivity(serviceContext, 0, messageIntent, 0);
			
		}else if(type.equals(TYPE_OPENWEB))
		{
			//open web
	        messageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action));
	        messageIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        messagePendingIntent = PendingIntent.getActivity(serviceContext, 0, messageIntent, 0);
		
		}else if(type.equals(TYPE_DOWNLOAD))
		{
			//download
//			messageIntent = new Intent(serviceContext,MainService.class);
//			messageIntent.putExtra("downloadURI", action);
//		    messagePendingIntent  = PendingIntent.getService(serviceContext, 0, messageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        //messageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(action));
	       // messageIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	       //messagePendingIntent = PendingIntent.getActivity(serviceContext, 0, messageIntent, 0);
		} else if(type.equals(TYPE_OPENPACKAGE)) {
			 PackageManager packageManager = serviceContext.getPackageManager();
			 messageIntent=new Intent();
			 if(userdata!=null) {
			    messageIntent.putExtra("userdata", userdata);
			 }
			 messageIntent = packageManager.getLaunchIntentForPackage(action);
		}
		
		notificationBuilder.setAutoCancel(true);
		notificationBuilder.setTicker(tickerText);
		notificationBuilder.setContentTitle(message);
		notificationBuilder.setContentText(description);
		notificationBuilder.setDefaults(alert_defaults);
		
		notificationBuilder.setContentIntent(messagePendingIntent);
		
		//get bitmap from network
		Bitmap bitmap = decodeBitmapHttp(icon);
		notificationBuilder.setSmallIcon(R.drawable.stat_notify_chat);
		if(null != bitmap)
		{
			notificationBuilder.setLargeIcon(bitmap);
		}else
		{
			//notificationBuilder.setSmallIcon(R.drawable.ic_camera);
		}
		messageNotification = notificationBuilder.build();
		
		messageNotificationManager = (NotificationManager) serviceContext.getSystemService(Service.NOTIFICATION_SERVICE);
		messageNotificationManager.notify(1, messageNotification);
		
		
	/*
	 //1
		//ComponentName componentName=new   ComponentName("com.android.music", "com.android.music.ArtistAlbumBrowserActivity");
		//ComponentName componentName=new   ComponentName("com.qihoo.browser", "com.qihoo.browser.BrowserActivity");
		//ComponentName componentName=new   ComponentName("com.android.gallery3d", "com.android.camera.CameraLauncher");
	   // ComponentName componentName = new ComponentName("com.android.settings","com.android.settings.settings");
	    messageIntent=new Intent();   
	    //messageIntent.setComponent(componentName);   
	    messageIntent.setAction(Intent.ACTION_VIEW);     
	    messageIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	   
	//2
	   // PackageManager packageManager = serviceContext.getPackageManager(); 
	   // messageIntent=new Intent(); 
       //messageIntent = packageManager.getLaunchIntentForPackage("com.qihoo.browser");
	    
		
	*/	
//		Statistics.onEvent(serviceContext, "receive", message);
	}
	
private static Bitmap decodeBitmapHttp(String urlStr) {
	HttpURLConnection con = null;
	InputStream is = null;
	Bitmap res = null;
	try {
		URL url = new URL(urlStr);
		con = (HttpURLConnection) url.openConnection();
		con.setInstanceFollowRedirects(true);
		con.connect();
		int httpCode = con.getResponseCode();
		//LogUtil.logV(TAG, "ResponseCode = %d", httpCode);
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
		closeSilently(is);
		if (con != null)
			con.disconnect();
	}
	return res;
}

private static void closeSilently(Closeable c) {
    if (c == null) return;
    try {
        c.close();
    } catch (Throwable t) {
        // do nothing
    }
}

}

