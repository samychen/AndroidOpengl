package com.cam001.service;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class Config 
	implements  LocationListener{

    private static final String PREFS_FILE = "config.xml";
    private static final String KEY_DEVID = "device_id";
    private static final String KEY_LATI = "gps_latitude";
    private static final String KEY_LONG = "gps_longitude";
    
    private Context mContext = null;
    private SharedPreferences mPrefs = null;
    private DeviceUuidFactory mIdFac = null;
	private String mDevId = null;
    private String mChannel = null;

    public Config(Context context, String channel) {
        mContext = context;
        mChannel = channel;
        mPrefs = mContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mIdFac = new DeviceUuidFactory(mContext);
        
        boolean bLoc = false;
//        if(!bLoc) bLoc = retrieveLocation(LocationManager.PASSIVE_PROVIDER);
        if(!bLoc) bLoc = retrieveLocation(LocationManager.NETWORK_PROVIDER);
    }

    private boolean retrieveLocation(String provider) {
    	LocationManager locManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
    	Location loc = locManager.getLastKnownLocation(provider);
    	if(loc!=null) {
    		setPreferenceString(KEY_LATI, String.valueOf(loc.getLatitude()));
    		setPreferenceString(KEY_LONG, String.valueOf(loc.getLongitude()));
    		return true;
    	}
    	locManager.requestLocationUpdates(provider, 10000, 1, this);
    	return false;
    }
    
    public String getDeviceId() {
        if(mDevId==null) {
            mDevId = mPrefs.getString(KEY_DEVID, null);
            if(mDevId==null) {
                mDevId = mIdFac.getMD5Id();
                mPrefs.edit().putString(KEY_DEVID, mDevId).commit();
            }
        }
        return mDevId;
    }

    public String getIMEI() {
        return mIdFac.getIMEI();
    }

    public String getIMSI() {
        return mIdFac.getIMSI();
    }

    public Point getResolution() {
        DisplayMetrics disp = mContext.getResources().getDisplayMetrics();
        return new Point(disp.widthPixels, disp.heightPixels);
    }

    public String getChannel() {
        return mChannel;
    }

    public String getModelName() {
        return Build.MODEL;
    }

    public String getCompany() {
        return Build.MANUFACTURER;
    }

    public long getMemorySize() {
        long memTotal;
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeSilently(br);
        }
        // beginIndex
        int begin = content.indexOf(':');
        // endIndex
        int end = content.indexOf('k');

        content = content.substring(begin + 1, end).trim();
        memTotal = Integer.parseInt(content);
        return memTotal;
    }

    public String getCpu() {
        return Build.HARDWARE;
    }

    public String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public String getCountry() {
        return Locale.getDefault().getCountry();
    }

    public String getTimeZone() {
        int min =  TimeZone.getDefault().getRawOffset()/60000;
        StringBuilder sb = new StringBuilder();
        if(min<0) {
            sb.append('-');
        } else {
            sb.append('+');
        }
        min = Math.abs(min);
        String hour = String.format("%02d", min/60);
        sb.append(hour);
        sb.append(':');
        String m = String.format("%02d", min%60);
        sb.append(m);
        return sb.toString();
    }

    public int getVersionCode() {
        return ManifestUtil.getVersionCode(mContext);
    }

    public long getTimeStamp() {
        return System.currentTimeMillis()/1000;
    }

    public int getNonce() {
        int min = 123400;
        int max = 9999999;
        double random = new Random().nextDouble();
        return (int)(min + random*(max-min));
    }

    public String getCertInfo() {
		PackageInfo pis;
		try {
			String packageName = mContext.getPackageName();
			pis = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
			Signature[] sigs = pis.signatures;    //????
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");  

			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(
			new ByteArrayInputStream(sigs[0].toByteArray()));
			return cert.getIssuerDN().toString();
		} catch (CertificateException e) {
		// TODO Auto-generated catch block
			Log.e("","CertificateException" + e.getMessage());
		} catch (Exception e) {
		// TODO Auto-generated catch block
			Log.e("","Exception: " + e.getMessage());
		}
		return "";
    }
    
    public String getPackageName() {
    	return mContext.getPackageName();
    }
    
    public String getLatitude() {
    	return getPreferenceString(KEY_LATI);
    }
    
    public String getLongitude() {
    	return getPreferenceString(KEY_LONG);
    }
    
    private String getInstalledApps() {
        StringBuilder sb = new StringBuilder();
        final PackageManager packageManager = mContext.getPackageManager();
        List< PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> appList = new ArrayList<String>();
        if(pinfo != null && pinfo.size()>0){
            for(int i = 1; i < pinfo.size(); i++){
                String packName = pinfo.get(i).packageName;
                if(packName.startsWith("android") || packName.startsWith("com.android")) {
                    continue;
                } else if(packName.startsWith("com.google") || packName.startsWith("com.qualcomm")) {
                    continue;
                }
                appList.add(packName);
            }
        }
        if(appList.size()>0) {
            sb.append(appList.get(0));
            for(int i=1; i<appList.size(); i++) {
                sb.append(",");
                sb.append(appList.get(i));
            }
            return sb.toString();
        }
        return null;
    }

    public int getPreferenceInt(String key) {
        boolean bFirstLoad = false;
        if (mPrefs == null) {
            mPrefs = mContext.getSharedPreferences(PREFS_FILE, 0);
        }
        int value = mPrefs.getInt(key, 0);

        return value;
    }

    public void setPreferenceInt(String key, int value) {
        mPrefs.edit().putInt(key, value).commit();
    }

    public String getPreferenceString(String key) {
        boolean bFirstLoad = false;
        if (mPrefs == null) {
            mPrefs = mContext.getSharedPreferences(PREFS_FILE, 0);
        }
        return mPrefs.getString(key, null);
    }

    public void setPreferenceString(String key, String value) {
        mPrefs.edit().putString(key, value).commit();
    }

    public boolean getPreferenceBool(String key) {
        boolean bFirstLoad = false;
        if (mPrefs == null) {
            mPrefs = mContext.getSharedPreferences(PREFS_FILE, 0);
        }
        return mPrefs.getBoolean(key, false);
    }

    public void setPreferenceBool(String key, boolean value) {
        mPrefs.edit().putBoolean(key, value).commit();
    }

    public boolean isFirstLoad(String key) {
        boolean bFirstLoad = false;

        int currenVersion = getPreferenceInt(key);

        if (currenVersion == 0) {
            setPreferenceInt(key, getVersionCode());
            bFirstLoad = true;
        }

        return bFirstLoad;
    }

    public boolean isFirstLoadAfterUpdate(String key) {
        boolean bFirstLoad = false;

        int currenVersion =getPreferenceInt(key);

        if (getVersionCode() != currenVersion) {
            setPreferenceInt(key, getVersionCode());
            bFirstLoad = true;
        }

        return bFirstLoad;
    }

	@Override
	public void onLocationChanged(Location loc) {
    	if(loc!=null) {
    		setPreferenceString(KEY_LATI, String.valueOf(loc.getLatitude()));
    		setPreferenceString(KEY_LONG, String.valueOf(loc.getLongitude()));
    	}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
}
