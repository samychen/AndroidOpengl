package com.cam001.photoeditor.beauty;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cam001.photoeditor.AppConfig;
import com.cam001.photoeditor.BeautyActivity;
import com.cam001.photoeditor.MSG;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.engine.EditorHistory;
import com.cam001.photoeditor.beauty.makeup.FacePointActivity;
import com.cam001.util.DensityUtil;

public class EditorViewBase extends RelativeLayout{
	public static final int DEPLAY_TIME = 0;
	public FacePointDisplayView mDispView = null;
	protected FrameLayout mPanelTop = null;
	protected FrameLayout mPanelBottom = null;
	protected RelativeLayout mPanelDisplay = null;
	protected RelativeLayout mPanelOverlay = null;
	public RelativeLayout mPanelShare=null;
	protected EditEngine mEngine = null;
	protected AppConfig mConfig = null;
	protected Handler mHandler = null;
	protected View mBtnBeforAfter = null;
	protected View mTxtBeforAfter = null;
	protected boolean isMosaic=false;
	public TextView mPercentTxt;
	protected Context mContect;
	protected ImageView mLogoImage;
	
	private OnClickListener mEmptyListener = new OnClickListener(){
		@Override
		public void onClick(View arg0) {
		}
	};
	
	public EditorViewBase(Context context) {
		super(context);
		initControls();
		mContect = context;
	}
	
	public EditorViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		initControls();
		mContect = context;
	}
	
	public void setHandler(Handler handler) {
		mHandler = handler;
	}
	
	public void startEnterAnimation(AnimationListener l) {
		int offset = DensityUtil.dip2px(mContect, 75);
		Animation animation = new TranslateAnimation(0, 0, -offset, 0);
		animation.setDuration(300);
		mPanelTop.startAnimation(animation);
		animation = new TranslateAnimation(0, 0, offset, 0);
		animation.setDuration(300);
		mPanelBottom.startAnimation(animation);
		animation = new AlphaAnimation(0, 1);
		animation.setDuration(300);
		mPanelOverlay.startAnimation(animation);
		if(l!=null) animation.setAnimationListener(l);
	}
	Animation mPercentAnim;
	public void startPercentPushOnAnim(){
		if (mPercentAnim == null) {
			mPercentAnim = AnimationUtils.loadAnimation(mContect,
					R.anim.push_out);
			mPercentAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation arg0) {
					mPercentTxt.setVisibility(View.GONE);
				}
			});
		}
		mPercentTxt.startAnimation(mPercentAnim);
	}
	
	public void startLeaveAnimation(AnimationListener l) {
		int offset = DensityUtil.dip2px(mContect, 75);
		Animation animation = new TranslateAnimation(0, 0, 0, -offset);
		animation.setDuration(200);
		mPanelTop.startAnimation(animation);
		animation = new TranslateAnimation(0, 0, 0, offset);
		animation.setDuration(200);
		mPanelBottom.startAnimation(animation);
		animation = new AlphaAnimation(1, 0);
		animation.setDuration(200);
		mPanelOverlay.startAnimation(animation);
		if(l!=null) animation.setAnimationListener(l);
	}
	
	public void setTitle(int resId) {
		((TextView)findViewById(R.id.editor_label_title)).setText(resId);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void enableHWAccelerated(boolean bEnable) {
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			if(bEnable) {
				setLayerType(View.LAYER_TYPE_HARDWARE, null);
			} else {
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
	}
	
	private void initControls() {
		inflate(getContext(), R.layout.editor_view_base, this);
		setBackgroundColor(Color.parseColor("#313131"));
		
		mDispView = (FacePointDisplayView)findViewById(R.id.editor_display_view);
		
		mPanelTop = (FrameLayout)findViewById(R.id.editor_panel_top);
		mPanelTop.setOnClickListener(mEmptyListener);
		mPanelBottom = (FrameLayout)findViewById(R.id.editor_panel_bottom);
		mPanelBottom.setOnClickListener(mEmptyListener);
		mPanelDisplay = (RelativeLayout) findViewById(R.id.editor_display_layout);
		mPanelOverlay = (RelativeLayout) findViewById(R.id.editor_panel_overlay);
		
		mLogoImage=(ImageView) findViewById(R.id.logo_icon_image);
		mLogoImage.setVisibility(View.GONE);
		
		mPanelShare=(RelativeLayout) findViewById(R.id.editor_share_layout);
		mPercentTxt=(TextView) findViewById(R.id.editor_bar_txt);
		mPercentTxt.setShadowLayer(3, 0, 1, Color.parseColor("#4D000000"));
		mEngine = EditEngine.getInstance();
		mConfig = AppConfig.getInstance();
		mBtnBeforAfter=findViewById(R.id.editor_button_ba);
		mTxtBeforAfter = findViewById(R.id.editor_label_ba);
		mBtnBeforAfter.setOnTouchListener(new OnTouchListener() 
		{
			private long downTime = 0;
			private boolean lastState = false;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					setOriginal(true);
					downTime = System.currentTimeMillis();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
//					if(System.currentTimeMillis()-downTime<500
//							&& lastState==false) {
//						lastState = true;
//						break;
//					}
					setOriginal(false);
					lastState = false;
					break;
				}
				return true;
			}
		});
		
		inflate(getContext(), R.layout.editor_panel_base_top, mPanelTop);
		findViewById(R.id.editor_button_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(MSG.EDITOR_CANCEL);
			}
		});
		findViewById(R.id.editor_button_confirm).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSave();
				boolean isModified = isModified();
				mEngine.loadImage(mEngine.saveToBitmap());
				if(isModified) {
					EditorHistory.getInstance().addHistory(mEngine.getEditBitmap().getBitmap());
				}
				Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_MAIN, Activity.RESULT_OK);
				mHandler.sendMessage(msg);
			}
		});
		findViewById(R.id.editor_button_face).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mConfig.appContext, FacePointActivity.class);
				Message msg = Message.obtain(mHandler, MSG.ACTIVITY_START, BeautyActivity.REQUEST_CODE_FACE, 0, intent);
				mHandler.sendMessage(msg);
			}
		});
	}

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
		mEngine.showOriginal(bOrig);
		mDispView.invalidate();
	}
	
	public boolean isModified() {
		return mEngine.isModified();
	}
	
	public void onPause() {
		mDispView.onPause();
	}
	
	public void onResume() {
		mDispView.onResume();
	}
	
	public void onSave() {
		
	}
	
	private boolean isVisible = true;

	public void setBackVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	public boolean onBackPressed() {
		if(mDispView.onBackPressed()) {
			return true;
		}
		mEngine.reset();
		Message msg = Message.obtain(mHandler, MSG.EDITOR_SWITCH_MODE, EditEngine.EDIT_MODE_MAIN, Activity.RESULT_CANCELED);
		mHandler.sendMessage(msg);
		return isVisible;
	}
}
