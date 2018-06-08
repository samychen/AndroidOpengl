package com.cam001.photoeditor.makeup.Util;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.cam001.photoeditor.makeup.struct.BlushStruct;
import com.cam001.photoeditor.makeup.struct.ContactStruct;
import com.cam001.photoeditor.makeup.struct.LashStruct;
import com.cam001.photoeditor.makeup.struct.LineStruct;
import com.cam001.photoeditor.makeup.struct.LipstickStruct;
import com.cam001.photoeditor.makeup.struct.ModuleStruct;
import com.cam001.photoeditor.makeup.struct.ShadowStruct;
import com.cam001.util.JSONUtil;

public class ResUtil {
	public static final String Root = "makeup";
	public static ModuleStruct[] decodeModuleFromAssets(Context context) {
		ModuleStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/config.dat");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			int len = jsonArray.length();
			res = new ModuleStruct[len];
			for(int i = 0; i < len; i++){
				ModuleStruct moduleStruct = new ModuleStruct(Root);
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				moduleStruct.blush = jsonObject.getInt("blush");
				moduleStruct.eyelash = jsonObject.getInt("eyelash");
				moduleStruct.eyeline = jsonObject.getInt("eyeline");
				moduleStruct.shadow = jsonObject.getInt("shadow");
				moduleStruct.contact = jsonObject.getInt("contact");
				moduleStruct.lipstick = jsonObject.getInt("lipstick");
				moduleStruct.thumb = jsonObject.getString("thumb");
				try {
					int ratio = jsonObject.getInt("blushratio");
					moduleStruct.bratio = ratio;
				} catch (JSONException e) {
				}
				try {
					int ratio = jsonObject.getInt("eyelashratio");
					moduleStruct.lashratio = ratio;
				} catch (JSONException e) {
				}
				try {
					int ratio = jsonObject.getInt("eyelineratio");
					moduleStruct.lineratio = ratio;
				} catch (JSONException e) {
				}
				try {
					int ratio = jsonObject.getInt("shadowratio");
					moduleStruct.shadowratio = ratio;
				} catch (JSONException e) {
				}
				try {
					int ratio = jsonObject.getInt("contactratio");
					moduleStruct.conratio = ratio;
				} catch (JSONException e) {
				}
				try {
					int ratio = jsonObject.getInt("lipstickratio");
					moduleStruct.lipratio = ratio;
				} catch (JSONException e) {
				}
				res[i] = moduleStruct;
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	
	public static LineStruct[] decodeLineFromAssets(Context context) {
		LineStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/eyeline/eyelines.txt");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			if (jsonArray != null) {
				int len = jsonArray.length();
				res = new LineStruct[len];
				for (int i = 0; i < len; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					LineStruct data = new LineStruct(Root+"/eyeline");

					data.elineratio = obj.getInt("ratio");
					data.elinercolor = obj.getString("color");
					data.elinerupper = obj.getString("upperini");
					data.elinerlower = obj.getString("lowerini");
					data.thumb = obj.getString("thumb");
					
					res[i] = data;
					// data.show();
				}
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	public static ShadowStruct[] decodeShadowFromAssets(Context context) {
		ShadowStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/eyeshadow/eyeshadow.txt");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			if (jsonArray != null) {
				int len = jsonArray.length();
				res = new ShadowStruct[len];
				for (int i = 0; i < len; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					ShadowStruct data = new ShadowStruct(Root+"/eyeshadow");

					data.eshaderatio = obj.getInt("ratio");
					data.eshaderfratio = obj.getInt("flashratio");
					data.eshadercolor = obj.getString("color");
					data.eshadertemp = obj.getString("ini");
					data.thumb = obj.getString("thumb");

					res[i] = data;
					// data.show();
				}
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	public static ContactStruct[] decodeContactFromAssets(Context context) {
		ContactStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/contactlen/contactlens.txt");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			if (jsonArray != null) {
				int len = jsonArray.length();
				res = new ContactStruct[len];
				for (int i = 0; i < len; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					ContactStruct data = new ContactStruct(Root+"/contactlen");
					
					data.cratio =  obj.getInt("ratio");
					data.ctemp = obj.getString("ini");
					data.thumb = obj.getString("thumb");

					res[i] = data;
					// data.show();
				}
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	public static LashStruct[] decodeLashFromAssets(Context context) {
		LashStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/eyelash/eyelash.txt");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			if (jsonArray != null) {
				int len = jsonArray.length();
				res = new LashStruct[len];
				for (int i = 0; i < len; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					LashStruct data = new LashStruct(Root+"/eyelash");

					data.elashratio = obj.getInt("ratio");
					data.elashcolor = obj.getString("color");
					data.elashupper = obj.getString("upperini");
					data.elashlower = obj.getString("lowerini");
					data.thumb = obj.getString("thumb");

					res[i] = data;
					// data.show();
				}
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	public static BlushStruct[] decodeBlushFromAssets(Context context) {
		BlushStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/blush/blush.txt");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			if (jsonArray != null) {
				int len = jsonArray.length();
				res = new BlushStruct[len];
				for (int i = 0; i < len; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					BlushStruct data = new BlushStruct(Root+"/blush");

					data.bratio = obj.getInt("ratio");
					data.bcolor = obj.getString("color");
					data.btemp = obj.getString("ini");
					data.thumb = obj.getString("thumb");

					res[i] = data;
					// data.show();
				}
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
	public static LipstickStruct[] decodeLipstickFromAssets(Context context) {
		LipstickStruct[] res = null;
		InputStream is = null;
		try {
			is = context.getAssets().open(Root+"/lipstick/lipstick.txt");
			JSONArray jsonArray = JSONUtil.getJSonArrayFromFile(is);
			if (jsonArray != null) {
				int len = jsonArray.length();
				res = new LipstickStruct[len];
				for (int i = 0; i < len; i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					LipstickStruct data = new LipstickStruct(Root+"/lipstick");

					data.lratio = obj.getInt("ratio");
					data.lcolor = obj.getString("color");
					data.ltemp = obj.getString("ini");
					data.lgloss = obj.getInt("glossratio");
					data.thumb = obj.getString("thumb");

					res[i] = data;
					// data.show();
				}
			}
		} catch (IOException e) {
		} catch (JSONException e) {
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return res;
	}
	
}
