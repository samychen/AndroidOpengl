package com.cam001.photoeditor.beauty.detect;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

public class FacialMarksTrack {
	
    private static final int kSAK_FACIAL_TRACK_CANTHUS_LL = 34;
    private static final int kSAK_FACIAL_TRACK_CANTHUS_LR = 30;
    private static final int kSAK_FACIAL_TRACK_CANTHUS_RL = 40;
    private static final int kSAK_FACIAL_TRACK_CANTHUS_RR = 44;
    
    private static final int kSAK_FACIAL_TRACK_LIP_CORNER_LEFT = 59;
    private static final int kSAK_FACIAL_TRACK_LIP_CORNER_RIGHT = 65;
	
	private int mHandle = 0;

	public void initialize() {
		mHandle = native_create();
	}

	public void uninitialize() {
		native_destroy(mHandle);
	}

	public boolean dectectFeatures(Bitmap bmp, FaceInfo[] faces) {
		if (faces == null) {
			return false;
		}
		int count = faces.length;
		if (count == 0) {
			return false;
		}
		native_reset(mHandle);
		for (int i = 0; i < count; i++) {
			FaceInfo face = faces[i];
			face.marks = new int[256];
			for (int j = 0; j < face.marks.length; j++) {
				face.marks[j] = 0;
			}
			face.eyeMarks = new int[16];
			native_figure(mHandle, bmp, face.face, face.marks, face.eyeMarks);
			getFeaturesInOutline(face);
		}
		return true;
	}
	
	public void updateFeatures(Bitmap bmp, FaceInfo face) {
		if(face.marks==null) {
			face.marks = new int[256];
		}
		native_update(mHandle, bmp, face.face,
				new Point(face.eye1.centerX(), face.eye1.centerY()),
				new Point(face.eye2.centerX(), face.eye2.centerY()),
				new Point(face.mouth.centerX(), face.mouth.centerY()),
				face.marks);
	}

	private void getFeaturesInOutline(FaceInfo face) {
		int[] faceOutline = face.marks;
		face.eye1.left = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_LL*2];
		face.eye1.top = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_LL*2+1];
		face.eye1.right = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_LR*2];
		face.eye1.bottom = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_LR*2+1];
		
		face.eye2.left = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_RL*2];
		face.eye2.top = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_RL*2+1];
		face.eye2.right = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_RR*2];
		face.eye2.bottom = faceOutline[kSAK_FACIAL_TRACK_CANTHUS_RR*2+1];

		face.mouth.left = faceOutline[kSAK_FACIAL_TRACK_LIP_CORNER_LEFT*2];
		face.mouth.top = faceOutline[kSAK_FACIAL_TRACK_LIP_CORNER_LEFT*2+1];
		face.mouth.right = faceOutline[kSAK_FACIAL_TRACK_LIP_CORNER_RIGHT*2];
		face.mouth.bottom = faceOutline[kSAK_FACIAL_TRACK_LIP_CORNER_RIGHT*2+1];
		
	}
	
	private static native int native_create();

	private static native void native_destroy(int handle);

	private static native void native_figure(int handle, Bitmap bmp, Rect face,
			int[] marks, int[] eyeMarks);
	
	private static native void native_update(int handle, Bitmap bmp, Rect face,
			Point eye1, Point eye2, Point mouth, int[] outMarks);

	private static native int native_reset(int handle);
}
