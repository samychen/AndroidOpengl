package com.cam001.photoeditor.resource;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cam001.photoeditor.AppConfig;
import com.cam001.util.DebugUtil;
import com.cam001.util.HanziToPinyin;

public class FilterCategory extends Template{

	private static final String LIST_FILE = "config.json";
	
	private ArrayList<Filter> mFilterList = null;
	private String mName = null;
	
	public FilterCategory(String path) {
		super(path);
	}

	private synchronized void loadConfig() {
		if(mFilterList!=null) return;
		DebugUtil.startLogTime("loadConfig");
		String str = loadStringFile(LIST_FILE);
		mFilterList = new ArrayList<Filter>();
		try {
			JSONObject obj = new JSONObject(str);
			mName = decodeName(obj.getString("name"));
			JSONArray array = obj.getJSONArray("list");
			int len = array.length();
			String preName = decodeFirstChar(mName);
			for(int i=0; i<len; i++) {
				String filter = array.getString(i);
				Filter f = new Filter(mRoot+"/"+filter);
				f.setName(preName+(i+1), mName);
				mFilterList.add(f);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		DebugUtil.stopLogTime("loadConfig");
	}

    private String decodeName(String n) {
        int resId = RLoader.getStringId(n);
        String res = n;
        if(resId>0) {
            res = AppConfig.getInstance().appContext.getString(resId);
        }
        return res;
    }

    private String decodeFirstChar(String n) {
        return HanziToPinyin.getInstance().getFirst(n);
    }

	public String getName() {
		loadConfig();
		return mName;
	}
	
	public ArrayList<Filter> getFilters() {
		loadConfig();
		return mFilterList;
	}
	
	private String[] mFilterPath;
	public String[] getFilterPath() {
		if (mFilterPath == null) {
			try {
				mFilterPath = AppConfig.getInstance().appContext.getAssets()
						.list(mRoot);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mFilterPath;
	}
}
