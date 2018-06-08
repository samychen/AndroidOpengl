package com.cam001.photoeditor.makeup.point;


import android.graphics.Rect;

import com.cam001.photoeditor.beauty.detect.FaceInfo;

public class FacePointUtil {
	
	public static FaceInfo createDefaultFace(int bmpWidth, int bmpHeight) {
		FaceInfo res = new FaceInfo();
		
		int faceWidth = Math.min(bmpWidth, bmpHeight)/2;
		int faceHeight = faceWidth;
		int faceLeft = (bmpWidth - faceWidth) / 2;
		int faceTop = (bmpHeight - faceHeight) / 2;
		res.face = new Rect(faceLeft, faceTop, faceLeft+faceWidth, faceTop+faceHeight);
		
		int eyeWidth = faceWidth*6/20;
		int eyeHeight = faceHeight*5/20;
		int lEyeLeft = faceLeft + faceWidth*2/20;
		int lEyeTop = faceTop + faceHeight*2/20;
		res.eye1 = new Rect(lEyeLeft,lEyeTop,lEyeLeft+eyeWidth,lEyeTop+eyeHeight);

		int rEyeLeft = faceLeft + faceWidth*12/20;
		int rEyeTop = faceTop + faceHeight*2/20;
		res.eye2 = new Rect(rEyeLeft,rEyeTop,rEyeLeft+eyeWidth,rEyeTop+eyeHeight);

		int mouthWidth = faceWidth*1/2;
		int mouthHeight = faceHeight*1/4;
		int mouthLeft = faceLeft + faceWidth*1/4;
		int mouthTop = faceTop + faceHeight*13/20;
		res.mouth = new Rect(mouthLeft, mouthTop, mouthLeft+mouthWidth, mouthTop+mouthHeight);

		return res;
	}
	
	public static FaceInfo createFace(int eye1_x, int eye1_y,
			int eye2_x, int eye2_y, int mouth_x, int mouth_y, int max_x, int max_y) {
		if(eye1_x > eye2_x) {
			int x = eye1_x;
			int y = eye1_y;
			eye1_x = eye2_x;
			eye1_y = eye2_y;
			eye2_x = x;
			eye2_y = y;
		}
		if(mouth_x==0 && mouth_y==0) {
			mouth_x = (eye1_x+eye2_x)/2;
			mouth_y = (eye2_x-eye1_x)+ (eye1_y+eye2_y)/2;
			mouth_x = clamp(0, max_x, mouth_x);
			mouth_y = clamp(0, max_x, mouth_y);
		}
		FaceInfo face = new FaceInfo();
		int faceWidth = (eye2_x - eye1_x) * 2;
		int faceHeight = (mouth_y - eye1_y)*2;
		int faceLeft = eye1_x - faceWidth/4;
		int faceTop = eye1_y - faceHeight*9/40;
		int faceRight = faceLeft+faceWidth;
		int faceBottom = faceTop+faceHeight;
		faceLeft = clamp(0, max_x, faceLeft);
		faceRight = clamp(0, max_x, faceRight);
		faceTop = clamp(0, max_y, faceTop);
		faceBottom = clamp(0, max_y, faceBottom);
		face.face = new Rect(faceLeft, faceTop, faceRight, faceBottom);

		int eyeWidth = faceWidth*6/20;
		int eyeHeight = faceHeight*5/20;
		int mouthWidth = faceWidth*1/2;
		int mouthHeight = faceHeight*1/4;
		face.eye1 = new Rect(eye1_x-eyeWidth/2, eye1_y-eyeHeight/2, 
				eye1_x+eyeWidth/2, eye1_y+eyeHeight/2);
		face.eye2 = new Rect(eye2_x-eyeWidth/2, eye2_y-eyeHeight/2, 
				eye2_x+eyeWidth/2, eye2_y+eyeHeight/2);
		face.mouth = new Rect(mouth_x-mouthWidth/2, mouth_y-mouthHeight/2, 
				mouth_x+mouthWidth/2, mouth_y+mouthHeight/2);
		return face;
	}

	private static int clamp(int min, int max, int v) {
		int res = Math.max(min,  v);
		res = Math.min(max, res);
		return res;
	}
}
