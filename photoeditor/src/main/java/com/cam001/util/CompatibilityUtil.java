package com.cam001.util;

import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CompatibilityUtil {

	public static boolean needOrientationCorrect() {
		if(Build.MODEL.equalsIgnoreCase("Galaxy Nexus")) {
			return false;
		} else if(Build.MODEL.equalsIgnoreCase("GT-I9300")) {
			return false;
		} else if(Build.MODEL.equalsIgnoreCase("GT-I9100G")) {
			return false;
		} else if(Build.MODEL.equalsIgnoreCase("GT-I9500")) {
			return false;
		}  else if(Build.MODEL.startsWith("MI ")) {
			return false;
		} 
		return true;
	}
	
	public static boolean useFilterCamera() {
//		if(Build.MODEL.startsWith("MI 2")) {
//			return true;
//		} 
		return false;
	}
	
	public static String getUMengChannel(Context c) {
		String chl = "TEST";
		if(sLoad) {
			try {
				chl = getChannelName();
			} catch(UnsatisfiedLinkError e) {};
		}
		return chl;
	}
	
	private static native String getChannelName();
	
	private static boolean sLoad = false;
	static {	
		try {
			System.loadLibrary("tsfacebeautify");
			sLoad = true;
		} catch (UnsatisfiedLinkError e) {
			try {
				System.load("/vendor/lib/libts_face_beautify_hal.so");
				sLoad = true;
			} catch (UnsatisfiedLinkError e1) {	}
		}
	}

	private static int sTotalMem = -1;
	public static boolean low512MMemory(){
		return CompatibilityUtil.getTotalMem() > 1024*512;
	}
	public static int getTotalMem() {
		if(sTotalMem>0) {
			return sTotalMem;
		}
		String path = "/proc/meminfo";
		String content = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path), 8);
			String line;
			if ((line = br.readLine()) != null) {
				content = line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		int begin = content.indexOf(':');
		int end = content.indexOf('k');

		content = content.substring(begin + 1, end).trim();
		sTotalMem = Integer.parseInt(content);
		return sTotalMem;
	}
}
