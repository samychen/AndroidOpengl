package com.cam001.photoeditor.beauty;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.cam001.photoeditor.MSG;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.makeup.engine.FeatureInfo;
import com.cam001.photoeditor.beauty.makeup.engine.MakeupEngine;
import com.cam001.util.DensityUtil;
import com.cam001.widget.CommonHelpView;
import com.cam001.widget.VerticalSeekBar;

import java.util.HashMap;

public class EditorViewFaceWhiten extends EditorViewBase 
implements SeekBar.OnSeekBarChangeListener {

public static final String GUIDE_HELP_SEEKBAR = "vertGuide";
private VerticalSeekBar mSeekBar = null;
private Bitmap mCurrentBitmap = null;
private FeatureInfo mFeatureInfo = null;

private CommonHelpView mHelpView = null;
private ImageView vertGuide = null;
private SharedPreferences mSharedPreferences = null;

public EditorViewFaceWhiten(Context context) {
	super(context);
	initControls();
}

public EditorViewFaceWhiten(Context context, AttributeSet attrs) {
	super(context, attrs);
	initControls();
}

private void initControls() {
	setTitle(R.string.edt_lbl_white);
	
	inflate(getContext(), R.layout.editor_panel_trim_bottom, mPanelOverlay);
	mPanelBottom.setVisibility(View.GONE);
	MakeupEngine.Init_Lib();

	mFeatureInfo = new FeatureInfo(FeatureInfo.FEATUREMODE_WHITENFACE);
	mCurrentBitmap = mEngine.getEditBitmap().getBitmap();
	
	mSeekBar = (VerticalSeekBar)findViewById(R.id.editor_trim_seek);
	mSeekBar.setOnSeekBarChangeListener(this);
	mSeekBar.setMax(100);
	mSeekBar.setMinimumHeight(10);
	mSeekBar.setProgress(50);
	
//	mHelpView = new CommonHelpView(mConfig.appContext);
//	mHelpView.setText(R.string.edt_help_seekbar);
	mSharedPreferences = mContect.getSharedPreferences(GUIDE_HELP_SEEKBAR, Context.MODE_PRIVATE);
	boolean shouldShow = mSharedPreferences.getBoolean(GUIDE_HELP_SEEKBAR, true);
	if(shouldShow){
		LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.rightMargin = DensityUtil.dip2px(getContext(), 40);
		vertGuide = new ImageView(mConfig.appContext);
		vertGuide.setImageResource(R.drawable.guide_vertseekbar_icn);
		Animation anim = AnimationUtils.loadAnimation(mConfig.appContext, R.anim.anim_vert_seeekbar);
		vertGuide.startAnimation(anim);
		vertGuide.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeGuideHelpView();
			}
		});
		mPanelOverlay.addView(vertGuide, params);
	}
//	mPanelOverlay.addView(mHelpView, params);
	
	FaceInfo[] faces = EditEngine.getInstance().getEditBitmap().getFaces();
	MakeupEngine.ReloadFaceInfo(mCurrentBitmap, faces[0]);
	mFeatureInfo.setIntensity(mSeekBar.getProgress());
	MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureInfo);
	mDispView.invalidate();
	
//	MobclickAgent
//	.onEventBegin(getContext(), "Begin to EyeBagMakeUp");
}

private void closeGuideHelpView(){
	if (vertGuide!=null && vertGuide.getVisibility()==View.VISIBLE) {
		vertGuide.clearAnimation();
		vertGuide.setVisibility(View.GONE);
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(GUIDE_HELP_SEEKBAR, false);
		editor.commit();
	}
}
@Override
public boolean isModified() {
	return mFeatureInfo.GetIntensity()>0;
}

@Override
public void onProgressChanged(SeekBar seekBar, int progress,
		boolean fromUser) {
	mFeatureInfo.setIntensity(progress);
	mPercentTxt.setVisibility(View.VISIBLE);
	mPercentTxt.setText(progress+"%");
	mPercentTxt.clearAnimation();
	closeGuideHelpView();
}

@Override
public void onStartTrackingTouch(SeekBar seekBar) {
//	mHelpView.setVisibility(View.GONE);
	if(vertGuide != null){
		vertGuide.clearAnimation();
		vertGuide.setVisibility(View.GONE);
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean(GUIDE_HELP_SEEKBAR, false);
		editor.commit();
	}
}

@Override
public void onStopTrackingTouch(SeekBar seekBar) {
//	new Thread(){
//
//		@Override
//		public void run() {
			// TODO Auto-generated method stub
			MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureInfo);
//			
//			handle.sendEmptyMessage(HANDLER_MSG_DISPLAY_IMG);
//			super.run();
//		}
//		 
//}.start();
			mDispView.invalidate();
			mHandler.sendEmptyMessageDelayed(MSG.HIDE_PERCENT_TXT, DEPLAY_TIME);
}

@Override
public void onSave() {
	HashMap<String, String> args = new HashMap<String, String>();
	args.put("strength", String.valueOf(mSeekBar.getProgress()));
//	StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnLightenSave, args);
//	MobclickAgent.onEvent(mConfig.appContext, "UseEyeBag", args);
//	MobclickAgent.onEventEnd(getContext(), "End to EyeBagMakeUp");
}

@Override
	public boolean onBackPressed() {
//	  MobclickAgent.onEventEnd(getContext(), "End to EyeBagMakeUp");
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnLightenCancel);
		return super.onBackPressed();
	}

}