package com.cam001.photoeditor.beauty.makeup.engine;

import java.util.ArrayList;

public class StyleInfo {
	
	public static final int STYLEMODE_LEVE1 = 0x01;
	public static final int STYLEMODE_LEVE2 = 0x02;
	public static final int STYLEMODE_LEVE3 = 0x03;
	public static final int STYLEMODE_LEVE4 = 0x04;
	public static final int STYLEMODE_LEVE5 = 0x05;
	
	private int mStyle = STYLEMODE_LEVE1;
	
	ArrayList<FeatureInfo> mFeaturelist;
	
	public StyleInfo(int nStyle, boolean bEntireBmp) {
		super();
		mStyle = nStyle;
		
		FeatureInfo whiteFace = new FeatureInfo(FeatureInfo.FEATUREMODE_WHITENFACE);
		FeatureInfo softFace = new FeatureInfo(FeatureInfo.FEATUREMODE_SOFTENFACE);
		FeatureInfo thinFace = new FeatureInfo(FeatureInfo.FEATUREMODE_TRIMFACE);
		FeatureInfo bigEye = new FeatureInfo(FeatureInfo.FEATUREMODE_BIGEYE);
		FeatureInfo skinColor = new FeatureInfo(FeatureInfo.FEATUREMODE_SKINCOLOR);
		FeatureInfo eyeBag = new FeatureInfo(FeatureInfo.FEATUREMODE_EYEBAG);
		FeatureInfo darkCircle = new FeatureInfo(FeatureInfo.FEATUREMODE_DARKCIRCLE);
		FeatureInfo brightEye = new FeatureInfo(FeatureInfo.FEATUREMODE_BRIGHTEYE);
		skinColor.mSkinColorCb = 108;
		skinColor.mSkinColorCr = 182;
		
		switch(mStyle){
		case STYLEMODE_LEVE1:
			whiteFace.intensity = 20; //TBD,�����ô���������
			softFace.intensity = 40;
			thinFace.intensity = 20;
			bigEye.intensity = 20;
			skinColor.intensity = 20;
			eyeBag.intensity = 20;
			darkCircle.intensity = 20;
			brightEye.intensity = 20;
			break;
		case STYLEMODE_LEVE2:
			whiteFace.intensity = 40;  
			softFace.intensity = 60;
			thinFace.intensity = 20;
			bigEye.intensity = 20;
			skinColor.intensity = 30;
			eyeBag.intensity = 40;
			darkCircle.intensity = 40;
			brightEye.intensity = 20;
			break;
		case STYLEMODE_LEVE3:
			whiteFace.intensity = 60;  
			softFace.intensity = 70;
			thinFace.intensity = 20;
			bigEye.intensity = 20;
			skinColor.intensity = 40;
			eyeBag.intensity = 60;
			darkCircle.intensity = 60;
			brightEye.intensity = 20;
			break;
		case STYLEMODE_LEVE4:
			whiteFace.intensity = 70;  
			softFace.intensity = 85;
			thinFace.intensity = 20;
			bigEye.intensity = 20;
			skinColor.intensity = 60;
			eyeBag.intensity = 80;
			darkCircle.intensity = 80;
			brightEye.intensity = 60;
			break;
		case STYLEMODE_LEVE5:
			whiteFace.intensity = 90;  
			softFace.intensity = 100;
			thinFace.intensity = 20;
			bigEye.intensity = 20;
			skinColor.intensity = 80;
			eyeBag.intensity = 100;
			darkCircle.intensity = 100;
			brightEye.intensity = 100;
			break;
		default:
			break;
		}
		
		mFeaturelist = new ArrayList<FeatureInfo>(5); 
		mFeaturelist.add(whiteFace);
		mFeaturelist.add(softFace);
		mFeaturelist.add(skinColor);
		if(!bEntireBmp) {
			mFeaturelist.add(thinFace);
			mFeaturelist.add(bigEye);
			mFeaturelist.add(eyeBag);
			mFeaturelist.add(darkCircle);
			mFeaturelist.add(brightEye);
		}
	}
	
	public int GetFeatureIntensity(int nIndex)
	{
		return mFeaturelist.get(nIndex).GetIntensity();
	}
	

	
	
	
}
