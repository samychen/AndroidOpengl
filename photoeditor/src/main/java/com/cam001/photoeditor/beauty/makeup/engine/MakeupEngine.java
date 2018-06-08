package com.cam001.photoeditor.beauty.makeup.engine;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import com.cam001.photoeditor.beauty.detect.FaceInfo;

public class MakeupEngine {
	public final static int Max_FaceNum = 1;
	public final static int MAX_DEBLEMISH_AREA = 100;
	
	static boolean bInitial = false;
	static int mHandle = 0;
	public MakeupEngine() {
		// TODO Auto-generated constructor stub
	}
	
	
	  public static void Init_Lib(){
		  
		  if(  bInitial == false){
			  mHandle= Init();
		       bInitial = true;
		  }
		 
		  
		  
	  }
	  
      public static void UnInit_Lib(){
		  
    	  if( bInitial == true){
    		  UnInit(mHandle);
        	  bInitial = false;
			  mHandle = 0;
    	  }
    	  
	  }
 
	
	  private synchronized static native int Init();
	  private synchronized static native void UnInit(int handle);
	  
	  public synchronized static native void ResetParameter(int mHandle);

	  
	  // input BGRA8888 bitmap, output face info
	  public synchronized static native void ReplaceImage(int mHandle,Bitmap bitmap, int[] face_num,
	      Rect[] face_rect, Point[] eye_rect, Point[] mouth_rect, int[] marks, int[] eyeMarks, boolean skinDetect);
	  
	  public static void ReloadFaceInfo(Bitmap bmp, FaceInfo face) {
		  ReloadFaceInfo(bmp, face, false);
	  }
	  
	  public static void ReloadFaceInfo(Bitmap bmp, FaceInfo face, boolean bSkinDetect) {
			int[] face_num = new int[1];
			Rect[] face_rect = new Rect[1];
			Point[] eye_point = new Point[2];
			Point[] mouth_point = new Point[1];
			int[] marks = null;
			int[] eyeMarks = null;
			face_num[0] = 1;
			face_rect[0] = face.face;
			Rect eye1 = face.eye1;
			eye_point[0] = new Point(eye1.centerX(), eye1.centerY());
			Rect eye2 = face.eye2;
			eye_point[1] = new Point(eye2.centerX(), eye2.centerY());
			Rect mouth = face.mouth;
			mouth_point[0] = new Point(mouth.centerX(), mouth.centerY());
			marks = face.marks;
			eyeMarks = face.eyeMarks;
			MakeupEngine.ReplaceImage(mHandle,bmp, face_num, face_rect,
					eye_point, mouth_point, marks, eyeMarks, bSkinDetect);
	  }

	public static int ManageImgae(Bitmap bitmap, FeatureInfo info){
		return ManageImgae(mHandle, bitmap, info);
	}

	public synchronized static native int ManageImgae(int mHandle,Bitmap bitmap, FeatureInfo info);
	  
	  public synchronized static native int SetParameter(int mHandle,FeatureInfo info);
	  
//	  public synchronized static native int TakeEffect(Bitmap outBmp);
	  
      public static int ManageImgae(Bitmap bitmap, StyleInfo info){
		  
    	  int res = 0;
    	  
    	  ResetParameter(mHandle);
    	  if(info.mFeaturelist.size() < 1)
    		  return 0;
    	  else
    	  {
    		  for(int i=0; i<info.mFeaturelist.size()-1; i++)
    			   res= SetParameter(mHandle,info.mFeaturelist.get(i));
    		  
    		  res= ManageImgae(mHandle,bitmap, info.mFeaturelist.get(info.mFeaturelist.size()-1));
    	  }
    	 
		  
		  return res;
	  }

}
