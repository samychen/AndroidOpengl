package com.cam001.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtil {
	public static JSONArray getJSonArrayFromFile(String path) {
		JSONArray jsonarray = null;
		InputStream is = null;
		InputStreamReader isr = null;
		try {
			is = new FileInputStream(path);
			isr = new InputStreamReader(is);
			char[] buf = new char[512];
			int len = 0;
			StringBuilder sbuilder = new StringBuilder();
			while ((len = isr.read(buf)) > 0) {
				sbuilder.append(buf, 0, len);
			}
			jsonarray = new JSONArray(sbuilder.toString());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				isr.close();
				is.close();
			} catch (IOException e) {
			}
		}
		return jsonarray;
	}

	public static JSONArray getJSonArrayFromFile(InputStream is) {
		JSONArray jsonarray = null;
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is);
			char[] buf = new char[512];
			int len = 0;
			StringBuilder sbuilder = new StringBuilder();
			while ((len = isr.read(buf)) > 0) {
				sbuilder.append(buf, 0, len);
			}
			jsonarray = new JSONArray(sbuilder.toString());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				isr.close();
			} catch (IOException e) {
			}
		}
		return jsonarray;
	}

	public static JSONObject getJSonObjectFromFile(String path) {
		JSONObject jsonobject = null;
		InputStream is = null;
		InputStreamReader isr = null;
		try {
			is = new FileInputStream(path);
			isr = new InputStreamReader(is);
			char[] buf = new char[512];
			int len = 0;
			StringBuilder sbuilder = new StringBuilder();
			while ((len = isr.read(buf)) > 0) {
				sbuilder.append(buf, 0, len);
			}
			jsonobject = new JSONObject(sbuilder.toString());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				isr.close();
				is.close();
			} catch (IOException e) {
			}
		}
		return jsonobject;
	}

	public static JSONObject getJSonObjectFromFile(InputStream is) {
		JSONObject jsonobject = null;
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(is);
			char[] buf = new char[512];
			int len = 0;
			StringBuilder sbuilder = new StringBuilder();
			while ((len = isr.read(buf)) > 0) {
				sbuilder.append(buf, 0, len);
			}
			jsonobject = new JSONObject(sbuilder.toString());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				isr.close();
			} catch (IOException e) {
			}
		}
		return jsonobject;
	}
}
