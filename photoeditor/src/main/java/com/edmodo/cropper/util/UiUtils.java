package com.edmodo.cropper.util;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cam001.photoeditor.R;


public abstract class UiUtils {

	 public static boolean checkNetworkShowAlert(Context context) {
	        if (isNetworkAvailable(context))
	            return true;

	        showAlert(context, context.getString(android.R.string.dialog_alert_title),
	                    context.getString(R.string.network_unavailable));
	        return false;
	    }
	 
	 public static void showAlert(Context context, String title, String message) {
	        new AlertDialog.Builder(context)
	                .setIcon(android.R.drawable.ic_dialog_alert)
	                .setTitle(title)
	                .setMessage(message)
	                .show();
	    }
	 
	 public static boolean isNetworkAvailable(Context context) {
	        ConnectivityManager connManager =
	                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
	        if (networkInfo != null) {
	            return networkInfo.isAvailable();
	        } else {
	            return false;
	        }
	    }
}
