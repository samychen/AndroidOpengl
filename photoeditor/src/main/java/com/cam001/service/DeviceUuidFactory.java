package com.cam001.service;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceUuidFactory {
	
	private Context mContext = null;
	
	public DeviceUuidFactory(Context context) {
		mContext = context;
	}


	public String getMD5Id() {
		String id = getIMEI()+getSerialNumber()+getAndroidId();
		return Util.getMD5(id);
	}
	
	private String getAndroidId() {
		return Secure.getString(mContext.getContentResolver(),
				Secure.ANDROID_ID);
	}

	public String getIMEI() {
		return ((TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}
	
	private String getSerialNumber() {
		return Build.SERIAL;
	}

    public String getIMSI() {
        return ((TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
    }
}
