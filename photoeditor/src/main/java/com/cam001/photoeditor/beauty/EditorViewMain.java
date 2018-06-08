package com.cam001.photoeditor.beauty;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cam001.photoeditor.BeautyActivity;
import com.cam001.photoeditor.MSG;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.engine.EditorHistory;
import com.cam001.service.AdView;
import com.cam001.stat.StatApi;

public class EditorViewMain extends EditorViewBase 
		implements OnClickListener {

	private static final int REQUEST_CODE_LOAD = 0x00002001;
	private ImageView lastView;
	private ImageView nextView;
	private ImageView backView;
	private ImageView saveView;
    private boolean mIsAdView;
	private boolean mIsModified = true;
	
	public EditorViewMain(BeautyActivity context,boolean isAdView) {
		super(context);
		mIsAdView=isAdView;
		initControls();
	}
	
	public EditorViewMain(Context context, AttributeSet attrs) {
		super(context, attrs);
		initControls();
	}
	
	private void initControls() {
		mPanelTop.removeAllViews();
		inflate(getContext(), R.layout.editor_panel_main_bottom, mPanelBottom);
		inflate(getContext(), R.layout.editor_panel_main_top, mPanelTop);
		
		
		LayoutParams params=new LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);

	    mLogoImage.setVisibility(View.VISIBLE);
	    if (mIsAdView){
			AdView ad = new AdView(mContect);
			params=new LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,-2);
			params.addRule(BELOW, R.id.editor_panel_top);
			addView(ad, params);
		}

		findViewById(R.id.title_txt).setVisibility(View.GONE);
		findViewById(R.id.opter_rl).setVisibility(View.VISIBLE);
		
		findViewById(R.id.editor_button_load).setOnClickListener(this);
		findViewById(R.id.editor_button_crop).setOnClickListener(this); 
		findViewById(R.id.editor_button_facewhiten).setOnClickListener(this); 
		findViewById(R.id.editor_button_facesoften).setOnClickListener(this); 
		findViewById(R.id.editor_button_facecolor).setOnClickListener(this); 
		findViewById(R.id.editor_button_facetrim).setOnClickListener(this);
//		findViewById(R.id.editor_button_eyebag).setOnClickListener(this);
//		findViewById(R.id.editor_button_eyecircle).setOnClickListener(this);
//		findViewById(R.id.editor_button_brighteyes).setOnClickListener(this);
		findViewById(R.id.editor_button_enlargeeyes).setOnClickListener(this);
		findViewById(R.id.editor_button_deblemish).setOnClickListener(this);
		findViewById(R.id.editor_button_more).setOnClickListener(this);
//		findViewById(R.id.editor_button_filter).setOnClickListener(this); 
//		findViewById(R.id.editor_button_magazine).setOnClickListener(this);
//		findViewById(R.id.editor_button_mosaic).setOnClickListener(this);
//		findViewById(R.id.editor_button_stamp).setOnClickListener(this);
//		findViewById(R.id.editor_button_frame).setOnClickListener(this);
		
		saveView = (ImageView) findViewById(R.id.editor_button_save);
		saveView.setOnClickListener(this);
		
		backView = (ImageView) findViewById(R.id.editor_button_back);
		backView.setOnClickListener(this);
		
//		findViewById(R.id.editor_button_face).setVisibility(View.VISIBLE);
		
		findViewById(R.id.last_opter_rl).setOnClickListener(this);
		lastView=(ImageView) mPanelTop.findViewById(R.id.last_opter);
		
		findViewById(R.id.next_opter_rl).setOnClickListener(this);
		nextView=(ImageView) mPanelTop.findViewById(R.id.next_opter);
		
		ensureBtnPrevNext();
		
		if (mConfig.isFirstLoadAfterUpdate(mConfig.SP_KEY_PINCH_DRAG_HELP)) {
			final View helpView = inflate(getContext(),
					R.layout.edit_main_help_view, null);
			params = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.FILL_PARENT);
			mPanelOverlay.addView(helpView, params);
			mPanelOverlay.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					helpView.setVisibility(View.GONE);
					mPanelOverlay.removeAllViews();
					mPanelOverlay.setVisibility(View.GONE);
					return true;
				}
			});
		}

//		MobclickAgent.onEventBegin(mConfig.appContext, "Begin to MainMakeUp");
	}
	
	@Override
	protected void setOriginal(boolean bOrig) {
		if(bOrig) {
			mEngine.showStampCtrl(false);
			mBtnBeforAfter.setBackgroundResource(R.drawable.but_original_pressed);
			mTxtBeforAfter.setVisibility(View.VISIBLE);
			mPanelOverlay.setVisibility(View.INVISIBLE);
		} else {
			mBtnBeforAfter.setBackgroundResource(R.drawable.but_original_normal);
			mTxtBeforAfter.setVisibility(View.GONE);
			mPanelOverlay.setVisibility(View.VISIBLE);
		}
		Bitmap bmp = EditorHistory.getInstance().original(bOrig);
		if(bmp!=null) {
			mEngine.loadImage(bmp);
			mDispView.invalidate();
		}
	}
	
	private void onBtnLoadClick() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		Message msg = Message.obtain(mHandler, MSG.ACTIVITY_START, REQUEST_CODE_LOAD, 0, intent);
		mHandler.sendMessage(msg);
	}
	
	private void onBtnStampClick() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_STAMP, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_Stamp);
//		MobclickAgent.onEvent(mConfig.appContext, "Stamp");
	}
	
	private void onBtnMosaicClick() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_MOSAIC, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_mosaic);
//		MobclickAgent.onEvent(mConfig.appContext, "mosaic");
	}
	
	private void onBtnFrameClick() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_FRAME, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_Frame);
//		MobclickAgent.onEvent(mConfig.appContext, "Frame");
	}
	
	private void onBtnFaceWhiten() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_FACEWHITEN, 0);
		mHandler.sendMessage(msg);
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnLighten);
//		MobclickAgent.onEvent(mConfig.appContext, "FaceWhiten");
	}
	
	private void onBtnFaceSoften() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_FACESOFTEN, 0);
		mHandler.sendMessage(msg);
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnSmooth);
//		MobclickAgent.onEvent(mConfig.appContext, "FaceSoften");
	}
	
	private void onBtnFaceColor() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_FACECOLOR, 0);
		mHandler.sendMessage(msg);
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnFoundation);
//		MobclickAgent.onEvent(mConfig.appContext, "FaceColor");
	}
	
	private void onBtnFaceTrim() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_FACETRIM, 0);
		mHandler.sendMessage(msg);
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnSlim);
//		MobclickAgent.onEvent(mConfig.appContext, "FaceTrim");
	}
	
	private void onBtnEyeBag() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_EYEBAG, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_EyeBag);
//		MobclickAgent.onEvent(mConfig.appContext, "EyeBag");
	}
	
	private void onBtnEnlargeEyes() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_ENLAGEEYES, 0);
		mHandler.sendMessage(msg);
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnBigEye);
//		MobclickAgent.onEvent(mConfig.appContext, "EnlargeEyes");
	}
	
	private void onBtnBrightEyes() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_BRIGHTEYES, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_BrightEyes);
//		MobclickAgent.onEvent(mConfig.appContext, "BrightEyes");
	}
	
	private void onBtnEyeCircle() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_EYECIRCLE, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_EyeCircle);
//		MobclickAgent.onEvent(mConfig.appContext, "EyeCircle");
	}
	
	private void onBtnDeblemish(){
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_DEBLEMISH, 0);
		mHandler.sendMessage(msg);
		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnDotsFix);
//		MobclickAgent.onEvent(mConfig.appContext, "Deblemish");
	}
	
	public void onBtnMoreClick() {
		if(mEngine==null) {
			return;
		}
		mHandler.sendEmptyMessage(MSG.EDIT_MORE);
	}
	
	public void onBtnCropClick() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_CROP, 0);
		mHandler.sendMessage(msg);
	}
	
	private void onBtnFilterClick() {
		if(mEngine==null) {
			return;
		}
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_FILTER, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_Filter);
//		MobclickAgent.onEvent(mConfig.appContext, "Filter");
	}
	
	private void onBtnMagazineClick() {
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_COVER, 0);
		mHandler.sendMessage(msg);
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_Magazine);
//		MobclickAgent.onEvent(mConfig.appContext, "Magazine");
	}
	
	private void onBtnSaveClick() {
		if(mEngine==null) {
			return;
		}
		mHandler.sendEmptyMessage(MSG.EDITOR_SAVE);
//		mPanelShare.setVisibility(View.VISIBLE);
//	    mShareView.showShareView();
	}
	
	private void onBtnResetClick() {
		if(mEngine==null) {
			return;
		}
		mEngine.reset();
		mDispView.invalidate();
	}
	
	private void onBtnPrevClick() {
		Bitmap bmp = EditorHistory.getInstance().previous();
		if(bmp!=null) {
			mEngine.loadImage(bmp);
			mDispView.invalidate();
		}
		ensureBtnPrevNext();
	}
	
	private void onBtnNextClick() {
		Bitmap bmp = EditorHistory.getInstance().next();
		if(bmp!=null) {
			mEngine.loadImage(bmp);
			mDispView.invalidate();
		}
		ensureBtnPrevNext();
	}
	
	private void ensureBtnPrevNext() {
		EditorHistory hist = EditorHistory.getInstance();
		if (hist.hasPrevious()) 
		{
			lastView.setImageResource(R.drawable.btn_last_do_select);
			mBtnBeforAfter.setBackgroundResource(R.drawable.but_original_normal);
			mBtnBeforAfter.setEnabled(true);
		}
		else 
		{
			lastView.setImageResource(R.drawable.btn_last_undo);
			mBtnBeforAfter.setBackgroundResource(R.drawable.but_original_disable);
			mBtnBeforAfter.setEnabled(false);
		}
		
		if (hist.hasNext()) 
		{
			nextView.setImageResource(R.drawable.btn_next_do_select);
		}
		else 
		{
			nextView.setImageResource(R.drawable.btn_next_undo);
		}
	}
	
	@Override
	public void onClick(View v) {
//		if(v.getId()!=R.id.save_rl) {
//			mIsModified = true;
//		}
		switch(v.getId()) {
		case R.id.editor_button_load:
			onBtnLoadClick();
			break;
		case R.id.editor_button_crop:
			onBtnCropClick();
			break;
		case R.id.editor_button_facewhiten:
			onBtnFaceWhiten();
			break;
		case R.id.editor_button_facesoften:
			onBtnFaceSoften();
			break;
		case R.id.editor_button_facecolor:
			onBtnFaceColor();
			break;
		case R.id.editor_button_facetrim:
			onBtnFaceTrim();
			break;
//		case R.id.editor_button_eyebag:
//			onBtnEyeBag();
//			break;
//		case R.id.editor_button_eyecircle:
//			onBtnEyeCircle();
//			break;
//		case R.id.editor_button_brighteyes:
//			onBtnBrightEyes();
//			break;
		case R.id.editor_button_enlargeeyes:
			onBtnEnlargeEyes();
			break;
		case R.id.editor_button_deblemish:
			onBtnDeblemish();
			break;
		case R.id.editor_button_more:
			onBtnMoreClick();
			break;
//		case R.id.editor_button_filter:
//			onBtnFilterClick();
//			break;
//		case R.id.editor_button_magazine:
//			onBtnMagazineClick();
//			break;
//		case R.id.editor_button_stamp:
//			onBtnStampClick();
//			break;
//		case R.id.editor_button_frame:
//			onBtnFrameClick();
//			break;
//		case R.id.editor_button_mosaic:
//			onBtnMosaicClick();
//			break;
		case R.id.editor_button_back:
			StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnHome);
			mHandler.sendEmptyMessage(MSG.EDITOR_CANCEL);
			break;
		case R.id.editor_button_save:
			StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnSave);
			onBtnSaveClick();
			return;
		case R.id.last_opter_rl:
			onBtnPrevClick();
			break;
		case R.id.next_opter_rl:
			onBtnNextClick();
			break;
		default:
			throw new RuntimeException("onClick Not find View: "+v);
		}
	}
	
	@Override
	public void onSave() {
		mIsModified = false;
	}
	
	public boolean isModified() {
		return mIsModified;
	}
	
	@Override
	public boolean onBackPressed() {
		if(mDispView.onBackPressed()) {
			return true;
		}
		setBackVisible(false);
		return false;
	}
}
