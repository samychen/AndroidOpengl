package com.cam001.photoeditor.resource;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cam001.photoeditor.AppConfig;
import com.cam001.util.DebugUtil;
import com.cam001.util.Util;

public abstract class Template {

	protected static final String THUMB_FILE = "thumb.jpg";
	
	public String mRoot = null;
	
	protected WeakReference<Bitmap> mRefThumb = null; 
	
	public Template(String path) {
		mRoot = path;
	}
	
	public Bitmap getThumbnail() {
		DebugUtil.startLogTime("getThumbnail");
		if(mRefThumb==null || mRefThumb.get()==null) {
			Bitmap bmpThumb = createBitmapScaledByDPI(THUMB_FILE);
			if(bmpThumb!=null){
				mRefThumb = new WeakReference<Bitmap>(bmpThumb);
			}
		}
		DebugUtil.stopLogTime("getThumbnail");
		if(mRefThumb!=null && mRefThumb.get()!=null){
			return mRefThumb.get();
		}
		else {
			return null;
		}
	}
	
	public int getThumbColor() {
		Bitmap bmp = getThumbnail();
		DebugUtil.startLogTime("getThumbColor");
		int color = bmp.getPixel(0, bmp.getHeight()-1);
		color &= 0xCDFFFFFF;
		DebugUtil.stopLogTime("getThumbColor");
		return color;
	}
	
	public int getSampleSize() {
		return 1;
	}
	
	protected InputStream openFileInputStream(String path)  {
//		return new FileInputStream(path);
		InputStream stream=null;
		try {
			stream=AppConfig.getInstance().appContext.getAssets().open(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream ;
	}
	
	public Bitmap createBitmapScaledByDPI(String filename) {
		String path = mRoot+"/"+filename;
		InputStream is = null;
		Bitmap bmp = null;
		try {
			is = openFileInputStream(path);
			if (is!=null) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inScaled = true;
				opts.inDensity = 320;
				opts.inTargetDensity = AppConfig.getInstance().appContext.getResources().getDisplayMetrics().densityDpi;
				bmp = BitmapFactory.decodeStream(is, null, opts);
			}else {
				bmp=null;
			}
		} catch (Exception e) {
		} finally {
			Util.closeSilently(is);
		}
		return bmp;
	}
	
	public Bitmap createBitmap(String filename) {
		String path = mRoot+"/"+filename;
		InputStream is = null;
		Bitmap bmp = null;
		try {
			is = openFileInputStream(path);
			if (is!=null) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = getSampleSize();
				bmp = BitmapFactory.decodeStream(is, null, opts);
			}else {
				bmp=null;
			}
		} catch (Exception e) {
		} finally {
			Util.closeSilently(is);
		}
		return bmp;
	}
	
	protected String loadStringFile(String filename) {
		String path = mRoot + "/" + filename;
		InputStream is = null;
		InputStreamReader reader = null;
		try {
			is = openFileInputStream(path);
			reader = new InputStreamReader(is);
			StringBuilder sb = new StringBuilder();
			char[] buffer = new char[512];
			int len = 0;
			while ((len = reader.read(buffer)) > 0) {
				sb.append(buffer, 0, len);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Util.closeSilently(reader);
			Util.closeSilently(is);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if(this==o) return true;
		if(o==null) return false;
		if(!(o instanceof Template)) return false;
		return mRoot.equals(((Template)o).mRoot);
	}
	
	@Override 
	public String toString() {
		return mRoot.substring(mRoot.lastIndexOf('/'));
	}
	
	protected void createSubFolderList() {
		try {
			FileWriter fw = new FileWriter("/sdcard/zhl/"+mRoot.replace('/', '_')+".json");
			fw.write("[\n");
			String[] mFilterPath = AppConfig.getInstance().appContext.getAssets().list(mRoot);
			for(String f: mFilterPath) {
				if(f.contains(".")) continue;
				fw.write("	\""+f+"\",\n");
			}
			fw.write("]");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
