package com.cam001.photoeditor.makeup.point;

import java.io.IOException;
import java.io.InputStream;

import com.cam001.photoeditor.R;
import com.cam001.widget.ScaledImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GlanceView extends ScaledImageView{

	private static final float[] FACE_POINT = new float[]{
		74,348,457,361,140,216,181,199,221,223,181,237,336,225,380,198,423,223,380,241,228,334,333,336,
		206,413,//59
		234,393,//60
		265,389,//61
		282,394,//62
		298,389,//63
		327,394,//64
		353,408,//65
		315,408,//66
		283,409,//67
		247,407,//68
		246,416,//69
		282,425,//70
		315,416,312,441,
		282,450,//74
		250,441
	};
	private int mSelectedIndex = -1;
	private Bitmap mBmpPoint = null; 
	private Bitmap mBmpSelect = null;
	private Matrix mMatPoint = null;
	
	private float[] mMappedFacePoint = new float[FACE_POINT.length];
	
	public GlanceView(Context context) {
		super(context);
		init();
	}
	
	public GlanceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		Bitmap image = null;
		try {
			InputStream is = getContext().getAssets().open("model.jpg");
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setImageBitmap(image);
		mBmpPoint = BitmapFactory.decodeResource(getResources(), R.drawable.sample_point);
		mBmpSelect = BitmapFactory.decodeResource(getResources(), R.drawable.sample_select);
	}
	
	public void select(int index) {
		mSelectedIndex = index;
		if(index<1) {
			transformToIdentify();
		} else {
			transformTo(FACE_POINT[2*index], FACE_POINT[2*index+1]);
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		canvas.save();
		super.onDraw(canvas);
		canvas.restore();
		drawFacePoint(canvas);
	}
	
	private void drawFacePoint(Canvas c) {
		mapFaceToPoints();
		float dx = mBmpPoint.getWidth()/2.0f;
		float dy = mBmpPoint.getHeight()/2.0f;
		int pointCount = mMappedFacePoint.length/2;
		for(int i=0; i<pointCount; i++) {
			if(i==mSelectedIndex) {
				c.drawBitmap(mBmpSelect, mMappedFacePoint[2*i]-dx, mMappedFacePoint[2*i+1]-dy, null);
			} else {
				c.drawBitmap(mBmpPoint, mMappedFacePoint[2*i]-dx, mMappedFacePoint[2*i+1]-dy, null);
			}
		}
	}

	private void mapFaceToPoints() {
		mMatPoint = new Matrix(getImageMatrix());
		mMatPoint.postConcat(mMatCanvas);
		mMatPoint.mapPoints(mMappedFacePoint, FACE_POINT);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
}
