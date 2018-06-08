package com.cam001.photoeditor.makeup.point;

import com.cam001.photoeditor.R;
import com.cam001.photoeditor.makeup.engine.TsMakeuprtEngine;
import com.cam001.util.DebugUtil;
import com.cam001.util.DensityUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class TsFacePointActivity extends Activity
	implements OnClickListener{

	public static final String INTENT_EXTRA_POINT = "points";
	
	private static final int[] DISPLAY_POINTS = new int[]
			{2,10,34,32,30,36,40,42,44,46,58,54,59,60,61,62,63,64,65,66,67,68,69,70,71,73,74,75}; 
	private static final int[] LEFTEYE_POINTS = new int[] {34,32,30,36};
	private static final int[] RIGHTEYE_POINTS = new int[] {40,42,44,46};
	private static final int[] NOSE_POINTS = new int[] {58,54};
	private static final int[] MOUTH_POINTS = new int[] {59,60,61,62,63,64,65,66,67,68,69,70,71,73,74,75};
	
	private FacePointView mView = null;
	private GlanceView mGlance = null;
	private View mGlanceFrame = null;
	private Point[] mPoints = null;
	private Point[] mTmpPts = null;

	private TsMakeuprtEngine mEngine = TsMakeuprtEngine.getInstance();
	private View mLastView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facepoint);
		mView = (FacePointView) findViewById(R.id.facepoint_view);
		mGlance = (GlanceView) findViewById(R.id.glance_view);
		mGlanceFrame = findViewById(R.id.face_point_glance_frame);
		findViewById(R.id.facepoint_ok_button).setOnClickListener(this);
		findViewById(R.id.facepoint_cancel_button).setOnClickListener(this);
		mLastView = findViewById(R.id.btn_origin);
		mLastView.setSelected(true);
		findViewById(R.id.btn_origin).setOnClickListener(this);
		findViewById(R.id.btn_leye).setOnClickListener(this);
		findViewById(R.id.btn_reye).setOnClickListener(this);
		findViewById(R.id.btn_nose).setOnClickListener(this);
		findViewById(R.id.btn_mouth).setOnClickListener(this);
		mView.setOnFacePointChangeListener(new FacePointView.OnFacePointChangeListenter(){

			private float mMargin = DensityUtil.dip2px(TsFacePointActivity.this, 12f);
			private boolean mIsGlanceLeft = true;
			@Override
			public void onFacePointChange(float[] facepoints) {
				getFacePoints(mTmpPts);
				mEngine.setOutline(mTmpPts);
				mEngine.makeup();
				mView.invalidate();
			}

			@Override
			public void onFacePointDown(int index) {
				mGlance.select(index);
			}

			@Override
			public void onFacePointUp(int index) {
				mGlance.select(-1);
			}

			@Override
			public void onFacePointMove(float x, float y) {
				float middle = mView.getWidth()/2.0f;
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mGlanceFrame.getLayoutParams();
				boolean isGlanceLeft;
				if(x<middle) {
					isGlanceLeft = false;
					params.leftMargin = (int)(middle*2 - mGlanceFrame.getWidth() - mMargin);
				} else {
					isGlanceLeft = true;
					params.leftMargin = (int)mMargin;
				}
				if(isGlanceLeft!=mIsGlanceLeft) {
					mGlanceFrame.getParent().requestLayout();
					mIsGlanceLeft = isGlanceLeft;
				}
			}
			
		});
		init();
	}
	
	private void init() {
		if(Util.LowMemory(this)){
			ToastUtil.showShortToast(this, R.string.low_mem_toast);
			setResult(RESULT_CANCELED);
			finish();
		}else{
			Intent intent = getIntent();
			mPoints = mEngine.getOutline();
			mTmpPts = new Point[mPoints.length];
			for (int i = 0; i < mPoints.length; i++) {
				mTmpPts[i] = new Point();
			}
			if (mPoints != null && mPoints.length > 0) {
				setFacePoints(mPoints);
			}

			Bitmap bmp = mEngine.getResultBitmap();
			mView.setImageBitmap(bmp);
		}
	}
	
	private void moveTo(int[] points) {
		float x = 0;
		float y = 0;
		for(int i: points) {
			Point p = mPoints[i];
			x += p.x;
			y += p.y;
		}
		x /= points.length;
		y /= points.length;
		mView.transformTo(x, y);
	}
	
	private void setFacePoints(Point[] points) {
		int len = DISPLAY_POINTS.length;
		float[] fp = new float[len*2];
		for(int i=0; i<len; i++) {
			Point p = points[DISPLAY_POINTS[i]];
			fp[i*2] = p.x;
			fp[i*2+1] = p.y;
		}
		mView.setFacePoints(fp);
	}
	
	private void getFacePoints(Point[] points) {
		float[] fp = mView.getFacePoints();
		int len = DISPLAY_POINTS.length;
		for(int i=0; i<len; i++) {
			Point p = points[DISPLAY_POINTS[i]];
			DebugUtil.logV("", "p[%d] before=%d,%d after=%d,%d",i, p.x,p.y,(int)fp[i*2],(int)fp[i*2+1]);
			p.x = (int)fp[i*2];
			p.y = (int)fp[i*2+1];
		}
	}
	
	public static void getFacePoints(float[] in, Point[] out) {
		int len = DISPLAY_POINTS.length;
		for(int i=0; i<len; i++) {
			Point p = out[DISPLAY_POINTS[i]];
			p.x = (int)in[DISPLAY_POINTS[i]*2];
			p.y = (int)in[DISPLAY_POINTS[i]*2+1];
		}
	}
	
	@Override
	public void onBackPressed() {
		if(mView.onBackPressed()) {
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.facepoint_ok_button:
			getFacePoints(mPoints);
		case R.id.facepoint_cancel_button:
			Intent i = getIntent();
			i.putExtra(INTENT_EXTRA_POINT, mPoints);
			mEngine.setOutline(mPoints);
			setResult(RESULT_OK, i);
			finish();
			break;
		case R.id.btn_origin:
			changeState(v);
			mView.transformToIdentify();
			break;
		case R.id.btn_leye:
			changeState(v);
			moveTo(LEFTEYE_POINTS);
			break;
		case R.id.btn_reye:
			changeState(v);
			moveTo(RIGHTEYE_POINTS);
			break;
		case R.id.btn_nose:
			changeState(v);
			moveTo(NOSE_POINTS);
			break;
		case R.id.btn_mouth:
			changeState(v);
			moveTo(MOUTH_POINTS);
			break;
		}
	}
	
	private void changeState(View v){
		if(mLastView.getId() == v.getId())
			return;
		mLastView.setSelected(false);
		mLastView = v;
		mLastView.setSelected(true);
	}
}
