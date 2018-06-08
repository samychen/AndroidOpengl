package com.cam001.photoeditor.filter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;

import com.cam001.gles.FBO;
import com.cam001.gles.ShaderUtil;
import com.cam001.photoeditor.resource.Filter;
import com.cam001.gles.Program;
import com.cam001.gles.Texture;

public class FilterProgram extends Program {

	private static class TexParam {
		public TexParam(String n, Texture t) {
			name = n;
			tex = t;
		}
		public String name;
		public Texture tex;
	}
	private static class FloatParam {
		public FloatParam(String n, float[] f) {
			name = n;
			array = f;
		}
		public String name;
		public float[] array;
	}
	
	private Filter mFilter = null;
	private boolean mIsLoaded = false;
	private ArrayList<TexParam> mTexParams = null;
	private ArrayList<FloatParam> mFloatParams = null;
	private int mImgWidth = 0;
	private int mImgHeight = 0;
	
	public FilterProgram(Filter filter) {
		super(filter.getVertextShader(), filter.getFragmentShader());
		mFilter = filter;
	}
	
	public Filter getFilter() {
		return mFilter;
	}

	public void setImageTexture(Texture texImage) {
		setUniformTexture("texture", texImage);
		mImgWidth = texImage.getWidth();
		mImgHeight = texImage.getHeight();
	}
	
	public void setMaskTexture(Texture texMask) {
		setUniformTexture("masktexture", texMask);
	}
	
	public void setStrength(float level) {
		setUniform1f("fpercent", level);
	}
	
	public void setMaskColor(int maskColor) {
		float red = Color.red(maskColor)/255.0f;
		float green = Color.green(maskColor)/255.0f;
		float blue = Color.blue(maskColor)/255.0f;
		setUniform3f("maskrgb", red, green, blue);
	}
	
	public void setMaskIndex(int index) {
		float red = (index>>2)&1;
		float green = (index>>1)&1;
		float blue = (index)&1;
		setUniform3f("maskrgb", red, green, blue);
	}
	
	private void loadParam() {
		if(mIsLoaded) return;
		mIsLoaded = true;
		mTexParams = new ArrayList<TexParam>();
		mFloatParams = new ArrayList<FloatParam>();
		int count = mFilter.getParamsCount();
		for(int i=0; i<count; i++) {
			JSONObject object = mFilter.getParam(i);
			try {
				String type = object.getString("type");
				if(type.equals("sampler2D")) {
					String name = object.getString("name");
					String img = object.getString("value");
					Bitmap bmp = mFilter.createBitmap(img);
					Texture tex = new Texture();
					tex.load(bmp);
					mTexParams.add(new TexParam(name, tex));
				} else if(type.equals("float")) {
					String name = object.getString("name");
					float f = (float)object.getDouble("value");
					mFloatParams.add(new FloatParam(name, new float[]{f}));
					setUniform1f(name, f);
				} else if(type.equals("vec2")) {
					String name = object.getString("name");
					JSONArray a = object.getJSONArray("value");
					float f1 = (float)a.getDouble(0);
					float f2 = (float)a.getDouble(1);
					mFloatParams.add(new FloatParam(name, new float[]{f1, f2}));
				} else if(type.equals("vec3")) {
					String name = object.getString("name");
					JSONArray a = object.getJSONArray("value");
					float f1 = (float)a.getDouble(0);
					float f2 = (float)a.getDouble(1);
					float f3 = (float)a.getDouble(2);
					mFloatParams.add(new FloatParam(name, new float[]{f1, f2, f3}));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setFilterParams() {
		loadParam();
		for(TexParam param: mTexParams) {
			setUniformTexture(param.name, param.tex);
		}
		for(FloatParam param: mFloatParams) {
			setUniformNf(param.name, param.array);
		}
	}

	public void readPixelsToFile(String path) {
		int width = mImgWidth;
		int height = mImgHeight;

		FBO fbo = new FBO();
		fbo.initFBO();
		fbo.setTexSize(width, height);
		fbo.bindFrameBuffer();

		GLES20.glViewport(0, 0, width, height);
		draw();
		ShaderUtil.glReadPixelsToFile(null, width, height, path);

		fbo.unbindFrameBuffer();
		fbo.uninitFBO();
	}


	@Override
	public void recycle() {
		super.recycle();
		if(mTexParams!=null) {
			for(TexParam param: mTexParams) {
				param.tex.recycle();
			}
			mTexParams = null;
		}
		mFloatParams = null;
		mIsLoaded = false;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof FilterProgram)) return false;
        return mFilter.equals(((FilterProgram)o).mFilter);
    }
}
