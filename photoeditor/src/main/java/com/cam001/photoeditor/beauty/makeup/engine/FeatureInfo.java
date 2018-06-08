package com.cam001.photoeditor.beauty.makeup.engine;

import android.graphics.Point;

public class FeatureInfo {
	
	
	public static final int FEATUREMODE_DEBLEMISh = 0x01;
	public static final int FEATUREMODE_WHITENFACE = 0x02;
	public static final int FEATUREMODE_SOFTENFACE = 0x03;
	public static final int FEATUREMODE_TRIMFACE = 0x04; 
	public static final int FEATUREMODE_BIGEYE = 0x05;
	public static final int FEATUREMODE_SKINCOLOR = 0x06;
	public static final int FEATUREMODE_EYEBAG = 0x07;
	public static final int FEATUREMODE_DARKCIRCLE = 0x08;
	public static final int FEATUREMODE_BRIGHTEYE = 0x09;
	
	public int mode;
	public int intensity; //special for deblemish
	
	//Only for color skin
	public int mSkinColorCb;
	public int mSkinColorCr;
	
	//only for deblemish
	Point[] touchpoint;
	int[] deblemishradiuse;
	
    int deblemish_num = 0;
    int deblemish_index = 0;
	
	

	public FeatureInfo(int nMode) {
		super();
		// TODO Auto-generated constructor stub
		
		mode = nMode;
		if(nMode == FEATUREMODE_DEBLEMISh){
			touchpoint = new Point[MakeupEngine.MAX_DEBLEMISH_AREA];
			for(int i=0; i<touchpoint.length; i++)
				touchpoint[i] = new Point();
			deblemishradiuse = new int[MakeupEngine.MAX_DEBLEMISH_AREA];
			deblemish_num = 0;
		}
		intensity = 0;	 
		
	}
	
	
	public int GetIntensity(){
		return intensity;
	}
	
	public int GetMod(){
		return mode;
	}
	
	public int getSkinColorCb() {
		return mSkinColorCb;
	}
	
	public int getSkinColorCr() {
		return mSkinColorCr;
	}
	
	public void setIntensity(int nInten){
		intensity = nInten;
	}
	
	public void setSkinColor(int Cb, int Cr) {
		mSkinColorCb = Cb;
		mSkinColorCr = Cr;
	}
	
	public boolean setArea(int x, int y){
		if(mode != FEATUREMODE_DEBLEMISh || deblemish_num >= MakeupEngine.MAX_DEBLEMISH_AREA)
			return false;
		
		touchpoint[deblemish_num].x = x;
		touchpoint[deblemish_num].y = y;
		deblemishradiuse[deblemish_num] = intensity;
		
		deblemish_num++;
		
		return true;
		
	}
	
	public int GetDeblemishNum()
	{
		return deblemish_num;
	}
	
	public void RemoveLastblemishArea(){
		if(deblemish_num != 0)
			deblemish_num--;
	}
	
	public Point GetDeblemishArea(int i)
	{
		if(i<deblemish_num)
			return touchpoint[i];
		else
		    return null;
	}
	
	public int GetDeblemishRadius(int i)
	{
		 
		if(i<deblemish_num)
			return deblemishradiuse[i];
		else
		    return 0;
	}
	
	public void SetDeblemishRadius(int i, int radius)
	{
		 
		if(i<deblemish_num)
		{
			deblemishradiuse[i] = radius;
		}
			 
	}
	
	
	
	public void ReSetDeblemishNum()
	{
		deblemish_num = 0;
	}
	
	public void SetDeblemish_index(int index)
	{
		deblemish_index = index;
	}
	
	public int GetDeblemish_index()
	{
		return deblemish_index;
	}
	
	private int mSkinFoundationType = 0;
	public void SetSkinFoundationType(int currentskincolorid) {
		this.mSkinFoundationType = currentskincolorid;
	}
	public int GetSkinFoundationType(){
		return this.mSkinFoundationType;
	}
}
