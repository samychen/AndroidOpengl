package com.cam001.photoeditor.resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Filter extends Template{

	private static final String PARAM_FILE = "params.json";
	
	private static final String EMPTY_VERTEX = "attribute vec4 aPosition;attribute vec2 aTextureCoord;varying vec2 vTextureCoord;void main() {    gl_Position = aPosition;    vTextureCoord = aTextureCoord;}";
	private static final String EMPTY_FRAGNEBT = "precision mediump float;varying vec2 vTextureCoord;uniform sampler2D texture;	void main() {    vec4 color = texture2D(texture, vTextureCoord);  gl_FragColor = color;}";

	private static final String TRANSFORM_VERTEX = "uniform mat3 uMVPMatrix;attribute vec4 aPosition;attribute vec2 aTextureCoord;varying vec2 vTextureCoord;void main() {    gl_Position = vec4((vec3(aPosition.xy, 1.0)*uMVPMatrix).xy, aPosition.z, 1.0);    vTextureCoord = aTextureCoord;}";
	private static final String BRIGHTNESS_FRAG = "precision mediump float;varying highp vec2 vTextureCoord;uniform sampler2D texture;uniform sampler2D masktexture;uniform vec3 maskrgb;uniform float fpercent;void main() {vec3 orig = texture2D (texture,vTextureCoord).rgb;vec3 mask = texture2D (masktexture,vTextureCoord).rgb;vec3 rgb;float len = length(maskrgb-mask.rgb);if (len < 0.01){float bright = fpercent - 0.5;rgb = orig + vec3(bright);}else if (len < 0.5){float bright = fpercent - 0.5;rgb = orig + vec3(bright);rgb = rgb*0.7 + orig*0.3;}else{rgb = orig;}gl_FragColor = vec4(rgb, 1.0);}";
	private static final String CONTRAST_FRAG = "precision mediump float;varying highp vec2 vTextureCoord;uniform sampler2D texture;uniform sampler2D masktexture;uniform vec3 maskrgb;uniform float fpercent;void main() {vec3 orig = texture2D (texture,vTextureCoord).rgb;vec3 mask = texture2D (masktexture,vTextureCoord).rgb;   vec3 rgb;float len = length(maskrgb-mask.rgb);if (len < 0.01){float contrast;if (fpercent >= 0.5)contrast = 1.0 + (fpercent-0.5)*2.0;else contrast = fpercent + 0.5;rgb = (orig - vec3(0.5)) * contrast + vec3(0.5);}else if (len < 0.5){float contrast;if (fpercent >= 0.5)contrast = 1.0 + (fpercent-0.5)*2.0;else contrast = fpercent + 0.5;rgb = (orig - vec3(0.5)) * contrast + vec3(0.5);rgb = rgb*0.7 + orig*0.3;}else{rgb = orig;}gl_FragColor = vec4(rgb, 1.0);}";
	private static final String SATURATION_FRAG = "precision mediump float;varying highp vec2 vTextureCoord;uniform sampler2D texture;uniform sampler2D masktexture;uniform vec3 maskrgb;uniform float fpercent;void main() {vec3 orig = texture2D (texture,vTextureCoord).rgb;vec3 mask = texture2D (masktexture,vTextureCoord).rgb;   vec3 rgb;float len = length(maskrgb-mask.rgb);if (len < 0.01){float saturation = fpercent * 2.0;vec3 lumW = vec3(0.2125, 0.7154, 0.0721);float grey = dot(orig, lumW);rgb = mix(vec3(grey), orig, saturation);}else if (len < 0.5){float saturation = fpercent * 2.0;vec3 lumW = vec3(0.2125, 0.7154, 0.0721);float grey = dot(orig, lumW);rgb = mix(vec3(grey), orig, saturation);rgb = rgb*0.7 + orig*0.3;}else{rgb = orig;}gl_FragColor = vec4(rgb, 1.0);}";
	
	public static final Filter EMPTY = new Filter(EMPTY_VERTEX, EMPTY_FRAGNEBT);
	public static final Filter BRIGHTNESS = new Filter(EMPTY_VERTEX, BRIGHTNESS_FRAG);
	public static final Filter CONTRAST = new Filter(EMPTY_VERTEX, CONTRAST_FRAG);
	public static final Filter SATURATION = new Filter(EMPTY_VERTEX, SATURATION_FRAG);
	public static final Filter TRANSFORM = new Filter(TRANSFORM_VERTEX, EMPTY_FRAGNEBT);
	
	private String mVertexShader = null;
	private String mFragmentShader = null;
	
	private JSONArray mJsonArray = null;
	private String mName = null;
	private String mParentName = null;
	
	public Filter(String path) {
		super(path);
	}
	
	private Filter(String vertex, String fragment) {
		super(null);
		mVertexShader = vertex;
		mFragmentShader = fragment;
	}
	
	protected void setName(String name, String parent) {
		mName = name;
		mParentName = parent;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getVertextShader() {
		loadConfig();
		return mVertexShader;
	}
	
	public String getFragmentShader() {
		loadConfig();
		return mFragmentShader;
	}
	
	public int getParamsCount() {
		loadConfig();
		if(mJsonArray==null) return 0;
		return mJsonArray.length();
	}
	
	public JSONObject getParam(int index) {
		try {
			return mJsonArray.getJSONObject(index);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private synchronized void loadConfig() {
		if(mRoot==null) return;
		if(mJsonArray!=null) return;
		mFragmentShader = loadStringFile("fragment.fs");
		mVertexShader = EMPTY_VERTEX;
		String json = loadStringFile(PARAM_FILE);
		if (json==null) {
			return;
		}
		try {
			mJsonArray = new JSONArray(json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override 
	public String toString() {
		if(mParentName==null) return mRoot;
		return mParentName+"/"+mName;
	}

    @Override
    public boolean equals(Object o) {
        if(this==o) return true;
        if(o==null) return false;
        if(!(o instanceof Filter)) return false;
        Filter that = (Filter) o;
        if(mRoot!=null) {
            return mRoot.equals(that.mRoot);
        }
        return mVertexShader.equals(that.mVertexShader)
                && mFragmentShader.equals(that.mFragmentShader);
    }
}
