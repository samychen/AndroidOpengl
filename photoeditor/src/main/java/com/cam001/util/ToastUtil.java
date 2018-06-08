package com.cam001.util;

import java.util.WeakHashMap;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

	private static WeakHashMap<String, Toast> sMapToast = new WeakHashMap<String, Toast>();
	
	public static void showShortToast(Context context, String msg) {
		showToast(context,Toast.LENGTH_SHORT, msg);
	}
	
	public static void showShortToast(Context context, int resId) {
		String str = context.getString(resId);
		showShortToast(context, str);
	}
	
	public static void showToast(Context context, int duration, int resId) {
		String str = context.getString(resId);
		showToast(context, duration, str);
	}
	
	public static void showToast(Context context,int duration,String msg, Object...args) {
		Toast t = sMapToast.get("");
		String label = String.format(msg, args);
		if(t==null) {
			t = Toast.makeText(context, msg, duration);
			sMapToast.put("", t);
		}
		t.setText(label);
		t.show();
	}
}
