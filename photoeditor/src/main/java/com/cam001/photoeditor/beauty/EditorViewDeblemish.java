package com.cam001.photoeditor.beauty;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.cam001.photoeditor.MSG;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.makeup.engine.FeatureInfo;
import com.cam001.photoeditor.beauty.makeup.engine.MakeupEngine;
import com.cam001.photoeditor.beauty.makeup.widget.MagnifierView;
import com.cam001.stat.StatApi;
import com.cam001.widget.CommonHelpView;

import java.util.HashMap;

public class EditorViewDeblemish extends EditorViewBase implements
		MagnifierView.OnSelectPointListenter, OnSeekBarChangeListener {

	private Bitmap mCurrentBitmap = null;
	private FeatureInfo mFeatureInfo = null;

	private MagnifierView magniferview;

	private float[] point = new float[4];

	private ImageView undobutton;
	private SeekBar sizebar;
	
	private Object mLock = new Object();

	public EditorViewDeblemish(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initControls(context);

	}

	public EditorViewDeblemish(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initControls(context);

	}

	void initControls(Context context) {
		setTitle(R.string.edt_lbl_deblemish);
		inflate(getContext(), R.layout.editor_panel_deblemish_bottom,
				mPanelBottom);
		mPanelBottom.setVisibility(View.VISIBLE);
		undobutton = (ImageView) findViewById(R.id.editor_deblemish_undobn);
		undobutton.setEnabled(false);

		undobutton.setOnClickListener(undodeblemish);

		MakeupEngine.Init_Lib();

		magniferview = new MagnifierView(context);
		magniferview.setDisplayView(mDispView);
		magniferview.setCircleResource(BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ring_3));
		mDispView.setDrawingCacheEnabled(true);

		mEngine.setMagnifier(magniferview);

		mFeatureInfo = new FeatureInfo(FeatureInfo.FEATUREMODE_DEBLEMISh);
		mCurrentBitmap = mEngine.getEditBitmap().getBitmap();
		FaceInfo[] faces = EditEngine.getInstance().getEditBitmap().getFaces();
		MakeupEngine.ReloadFaceInfo(mCurrentBitmap, faces[0]);

		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);

		RelativeLayout layout = (RelativeLayout) this
				.findViewById(R.id.editor_display_layout);
		// layout.addView(magniferview, 0);
		layout.addView(magniferview, new LayoutParams(-1, -1));
		magniferview.setSelectPointEndListener(this);

		sizebar = (SeekBar) this.findViewById(R.id.editor_deblemish_seekbar);
		sizebar.setOnSeekBarChangeListener(this);
		sizebar.setProgress(50);

		if(mConfig.isFirstLoadAfterUpdate(mConfig.SP_KEY_DEBLEMISH_HELP)){
			CommonHelpView mHelpView = new CommonHelpView(mConfig.appContext);
			params = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);
			mPanelOverlay.addView(mHelpView, params);
			mPanelOverlay.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					mPanelOverlay.removeAllViews();
					mPanelOverlay.setVisibility(View.GONE);
					return true;
				}
			});
		}
		
//		MobclickAgent
//		.onEventBegin(getContext(), "Begin to deblemishMakeUp");
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void disableHWAccelerated() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	@Override
	public boolean isModified() {
		return mFeatureInfo.GetDeblemishNum() != 0;
	}

	@Override
	public void onSelectBegin(float x, float y, int radius) {
	}

	@Override
	public void onSelectEnd(final float x, final float y, final int radius) {
		Message.obtain(mHandler, MSG.ACTIVITY_ASYNC_JOB, new Runnable() {

			@Override
			public void run() {
				synchronized(mLock) {
				point[0] = x - radius;
				point[1] = y - radius;
				point[2] = x + radius;
				point[3] = y + radius;
				
				Matrix mat = new Matrix(mEngine.getImageMatrix());
				mat.postConcat(mDispView.getScaleMatrix());
				Matrix inverse = new Matrix();
				mat.invert(inverse);
				inverse.mapPoints(point);

				int imgX = (int) ((point[0] + point[2]) / 2);
				int imgY = (int) ((point[1] + point[3]) / 2);
				int imgRadius = (int) Math.abs((point[2] - point[0]) / 2);
				mFeatureInfo.setIntensity(imgRadius);
				if (point[0] < 0 || point[1] < 0
						|| point[2] > mCurrentBitmap.getWidth() - 1
						|| point[3] > mCurrentBitmap.getHeight() - 1) {
					Message msg = Message.obtain(mHandler,
							MSG.ACTIVITY_SHOW_TOAST, R.string.deblemish_info1,
							Toast.LENGTH_SHORT);
					msg.sendToTarget();
					return;
				}
				if (mFeatureInfo.setArea(imgX, imgY) == false) {
					Message msg = Message.obtain(mHandler,
							MSG.ACTIVITY_SHOW_TOAST, R.string.deblemish_info2,
							Toast.LENGTH_SHORT);
					msg.sendToTarget();
					return;
				} else {
					MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureInfo);
					mDispView.postInvalidate();
				}
				}
			}

		}).sendToTarget();
		undobutton.setEnabled(true);

	}

	OnClickListener undodeblemish = new OnClickListener() {

		@Override
		public void onClick(View v) {
			synchronized(mLock) {
			if (mFeatureInfo.GetDeblemishNum() != 0) {
				mFeatureInfo.RemoveLastblemishArea();
				MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureInfo);
				mDispView.invalidate();
			}

			if (mFeatureInfo.GetDeblemishNum() == 0)
				undobutton.setEnabled(false);
			}
		}

	};

	int mSeekbarValue = (int) (50 * 4 / 100.f + 0.5f);

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		mSeekbarValue = (int) (progress * 4 / 100.f + 0.5f);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

		seekBar.setProgress(mSeekbarValue * 25);
		magniferview.setCircleResource(BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ring_1+mSeekbarValue));
	}

	@Override
	public void onSave() {
		super.onSave();
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("strength", String.valueOf(sizebar.getProgress()));
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnDotsFixSave, args);
//		MobclickAgent.onEventEnd(getContext(), "End to deblemishMakeUp");
	}
	
	@Override
	public boolean onBackPressed() {
//		MobclickAgent.onEventEnd(getContext(), "End to deblemishMakeUp");
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnDotsFixCancel);
		return super.onBackPressed();
	}
}
