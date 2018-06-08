package com.cam001.photoeditor.makeup.point;

import com.cam001.photoeditor.R;
import com.cam001.photoeditor.makeup.engine.TsMakeuprtEngine;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.util.DensityUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class EyePointActivity extends Activity 
	implements View.OnClickListener {

	public static final String INTENT_EXTRA_FACERECT = "face_rect";
	public static final String INTENT_EXTRA_LEYEPOINT = "leye_point";
	public static final String INTENT_EXTRA_REYEPOINT = "reye_point";
	public static final String INTENT_EXTRA_MOUTHPOINT = "mouth_point";

	private EyePointView mView = null;
	private ImageView mHelpView = null;
	private AnimationDrawable mHelpAnim = null;
	private TsMakeuprtEngine mTsEngine = TsMakeuprtEngine.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_eyepoint);
		mView = (EyePointView)findViewById(R.id.eyepoint_view);
		mHelpView = (ImageView) findViewById(R.id.eyepoint_help);
		mHelpAnim = (AnimationDrawable)mHelpView.getDrawable();
		findViewById(R.id.eyepoint_ok_button).setOnClickListener(this);
		findViewById(R.id.eyepoint_cancel_button).setOnClickListener(this);
		mView.setOnEyePointChangeListener(new EyePointView.OnEyePointChangeListenter(){
			private float mMargin = DensityUtil.dip2px(EyePointActivity.this, 12f);
			private boolean mIsHelpLeft = true;
			@Override
			public void onEyePointMove(float x, float y) {
				float middle = mView.getWidth()/2.0f;
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mHelpView.getLayoutParams();
				boolean isHelpLeft;
				if(x<middle) {
					isHelpLeft = false;
					params.leftMargin = (int)(middle*2 - mHelpView.getWidth() - mMargin);
				} else {
					isHelpLeft = true;
					params.leftMargin = (int)mMargin;
				}
				if(isHelpLeft!=mIsHelpLeft) {
					mHelpView.getParent().requestLayout();
					mIsHelpLeft = isHelpLeft;
				}
			}
			
		});
		init();
	}
	
	private void init() {
		Bitmap img = null;
		if(Util.LowMemory(this)){
			ToastUtil.showShortToast(this, R.string.low_mem_toast);
			setResult(RESULT_CANCELED);
			finish();
		}else{
			img = mTsEngine.getOriginalBitmap();
			if (img != null)
				mView.setImageBitmap(img);
			mView.enableFacePoint(true, true);
		}
	}
	
	@Override
	protected void onPause() {
		mHelpAnim.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mHelpAnim.start();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.eyepoint_ok_button:
			FaceInfo face = mView.getFace();
			Intent i = getIntent();
			i.putExtra(INTENT_EXTRA_FACERECT, face.face);
			i.putExtra(INTENT_EXTRA_LEYEPOINT, rectToPoint(face.eye1));
			i.putExtra(INTENT_EXTRA_REYEPOINT, rectToPoint(face.eye2));
			i.putExtra(INTENT_EXTRA_MOUTHPOINT, rectToPoint(face.mouth));
			setResult(RESULT_OK, i);
			finish();
			break;
		case R.id.eyepoint_cancel_button:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}

	public Point rectToPoint(Rect rc) {
		return new Point(rc.centerX(), rc.centerY());
	}
}
