package com.cam001.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class DownloadThread{
	
	private Intent mIntent = null;
	private String path = "tsDownload/";
	
    private NotificationManager updateNotificationManager = null;  
    private Notification updateNotification = null;  
    
    private String mURL = null;
    private Context mContext = null;
    
	public DownloadThread(Context context) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void StartDownload(Intent intent) {
		// TODO Auto-generated method stub
    	
    	mIntent = intent;
        new Thread(new Runnable() {
        	 
            @Override
            public void run() {  	
            	HttpDownloader httpDownloader = new HttpDownloader();
            	String uri = mIntent.getStringExtra("downloadURI");
            	
            	mURL = uri;
            	
            	 String[ ] uriAfterSplit= new String[8];
            	 uriAfterSplit=uri.split("/");
            	 String name = "tsdownload";
            	 if(uriAfterSplit.length > 0){
            		 name = uriAfterSplit[uriAfterSplit.length-1];
            	 }
            	 name = Uri.decode(name);
            	 int num = name.indexOf("=");
            	 if(num > -1){
            		 name = name.substring(num+1, name.length()); 
            	 }
            	            	 
            	 Log.v("DownloadService", " zcc name = "+"  "+ name+"  "+num);
            	 /*
                 for(int i=0;i<uriAfterSplit.length;i++)
                	 Log.v("DownloadService", " zcc "+i+"  "+ uriAfterSplit[i]);
            	*/ 
                 Intent nullIntent = new Intent();  
                 PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, nullIntent, 0); 
            	 
                 updateNotificationManager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);  
                 updateNotification = new Notification();  
                 updateNotification.icon = android.R.drawable.stat_sys_download;  
                 updateNotification.tickerText = "正在下载";  
                 updateNotification.setLatestEventInfo(mContext, "正在下载","0%", null);  
                 updateNotification.defaults = 0;  
                 updateNotification.flags = Notification.FLAG_AUTO_CANCEL;  
                 updateNotification.contentIntent = pendingIntent;  
                 updateNotificationManager.notify(101, updateNotification);  
                 
            	int result = httpDownloader.downFile(uri, path, name);
            	
            	Intent i = null;
            	PendingIntent contentIntent = null;
            	
            	String strResult,strText = null;
            	if(result == 0){
            		strResult = "下载成功";
            		strText = "";
            		
            		String str = "/"+path+name; 
            		String fileName = Environment.getExternalStorageDirectory() + str; 
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction(Intent.ACTION_VIEW);
        		    i.setDataAndType(Uri.fromFile(new File(fileName)),"application/vnd.android.package-archive");
        		    mContext.startActivity(i);
        		    
        		    i = new Intent(); 
                	contentIntent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            		  
            	}else if(result == 1){
            		strResult = "已下载，无需重复下载";
            		strText = "";
            		
            		String str = "/"+path+name; 
            		String fileName = Environment.getExternalStorageDirectory() + str; 
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setAction(Intent.ACTION_VIEW);
        		    i.setDataAndType(Uri.fromFile(new File(fileName)),"application/vnd.android.package-archive");
        		    mContext.startActivity(i);
        		    
        		    i = new Intent(); 
                	contentIntent = PendingIntent.getActivity(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            	}else{
            		strResult = "下载失败";
            		strText = "重新下载";
            		
//                	i = new Intent(mContext, MainService.class);
//                	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
//                	i.putExtra("downloadURI", uri);
//                	contentIntent = PendingIntent.getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            	}
            	          
            	updateNotificationManager.cancel(101);
            	Notification n = new Notification(android.R.drawable.stat_sys_download_done, strResult, System.currentTimeMillis());             
            	n.flags = Notification.FLAG_AUTO_CANCEL;                

            	n.setLatestEventInfo(
            			mContext,
            			strResult, 
            			strText, 
            	        contentIntent);
            	updateNotificationManager.notify(100, n);
            }
        }).start();
        
	}
    
    public class HttpDownloader {
    	private URL url = null;

    	public String download(String urlStr) {
    		StringBuffer sb = new StringBuffer();
    		String line = null;
    		BufferedReader buffer = null;
    		try {
    			url = new URL(urlStr);
    			HttpURLConnection urlConn = (HttpURLConnection) url
    					.openConnection();
    			buffer = new BufferedReader(new InputStreamReader(urlConn
    					.getInputStream()));
    			while ((line = buffer.readLine()) != null) {
    				sb.append(line);
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally {
    			try {
    				buffer.close();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    		return sb.toString();
    	}

    	public int downFile(String urlStr, String path, String fileName) {
    		InputStream inputStream = null;
    		try {
    			FileUtils fileUtils = new FileUtils();
    			
    			if (fileUtils.isFileExist(path + fileName)) {
    				return 1;
    			} else {
    				inputStream = getInputStreamFromUrl(urlStr);
    				File resultFile = fileUtils.write2SDFromInput(path,fileName, inputStream);
    				if (resultFile == null) {
    					return -1;
    				}
    			}
    		} catch (Exception e) {
    			e.printStackTrace();
    			return -1;
    		} finally {
    			try {
    				inputStream.close();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    		return 0;
    	}

    	public InputStream getInputStreamFromUrl(String urlStr)
    			throws MalformedURLException, IOException {
    		url = new URL(urlStr);
    		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    		InputStream inputStream = urlConn.getInputStream();
    		return inputStream;
    	}
    }
    
    public class FileUtils {
    	private String SDPATH;

    	public String getSDPATH() {
    		return SDPATH;
    	}
    	public FileUtils() {
    		SDPATH = Environment.getExternalStorageDirectory() + "/";
    	}

    	public File creatSDFile(String fileName) throws IOException {
    		File file = new File(SDPATH + fileName);
    		file.createNewFile();
    		return file;
    	}
    	
    	public File creatSDDir(String dirName) {
    		File dir = new File(SDPATH + dirName);
    		dir.mkdirs();
    		return dir;
    	}

    	public boolean isFileExist(String fileName){
    		File file = new File(SDPATH + fileName);
    		return file.exists();
    	}
    	
    	public File write2SDFromInput(String path,String fileName,InputStream input){
    		//
            int downloadCount = 0;  
            int currentSize = 0;  
            long totalSize = 0;  
            int updateTotalSize = 0;           
            HttpURLConnection httpConnection = null;  
            InputStream is = null;  
            FileOutputStream fos = null;  
            
    		File file = null;
    		OutputStream output = null;
    		try{
    			
                URL url = new URL(mURL);  
                httpConnection = (HttpURLConnection)url.openConnection();  
                if(currentSize > 0) {  
                    httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");  
                }  
                httpConnection.setConnectTimeout(10000);  
                httpConnection.setReadTimeout(20000);  
                updateTotalSize = httpConnection.getContentLength();  
                if (httpConnection.getResponseCode() == 404) {  
                    throw new Exception("fail!");  
                }
                
    			creatSDDir(path);
    			file = creatSDFile(path + fileName);
    			output = new FileOutputStream(file);
    			// 
    		       byte[] buffer = new byte[1024];
    		        int len = -1;
    		        while ((len = input.read(buffer)) != -1) {
    		            output.write(buffer, 0, len);
    		            
    		            //
    		            totalSize += len;  
    	                //
    	                if((downloadCount == 0)||(int) (totalSize*100/updateTotalSize)-5>downloadCount){   
    	                    downloadCount += 5;  
    	                    Intent nullIntent = new Intent();  
    	                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, nullIntent, 0);  
    	                    updateNotification.defaults = 0;  
    	                    updateNotification.contentIntent = pendingIntent;  
    	                    updateNotification.setLatestEventInfo(mContext, "正在下载", (int)totalSize*100/updateTotalSize+"%", null);  
    	                    updateNotificationManager.notify(101, updateNotification);  
    	                }  
    		        }
    		        
    			output.flush();
    		}
    		catch(Exception e){
    			e.printStackTrace();
    		}
    		finally{
    			try{
    				output.close();
    			}
    			catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    		return file;
    	}

    } 
}