package com.cam001.photoeditor.beauty;

import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cam001.photoeditor.MSG;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.beauty.detect.FaceInfo;
import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.photoeditor.beauty.makeup.engine.FeatureInfo;
import com.cam001.photoeditor.beauty.makeup.engine.MakeupEngine;
import com.cam001.util.DensityUtil;
import com.cam001.widget.CommonHelpView;
import com.cam001.widget.VerticalSeekBar;
import com.cam001.widget.slider.cheelview;

public class EditorViewFaceColor extends EditorViewBase 
	implements SeekBar.OnSeekBarChangeListener,cheelview.singletapListener {

	private VerticalSeekBar mSeekBar = null;
	private Bitmap mCurrentBitmap = null;
	private FeatureInfo mFeatureColor = null;
	private View colorGridLayout;
	private cheelview mSliderView = null;
	
	private ImageButton mDeepColour = null;
	private ImageButton mLightColour = null;
	
//	private GridView colorGridView;
//	private ColorItemAdapter colorItemAdapter;
	private CommonHelpView mHelpView = null;
	private ImageView vertGuide = null;
	private SharedPreferences mSharedPreferences = null;

	private int currentskincolorid = 0;
//	private int[] colorNormalRes = { R.drawable.color_08_normal,
//			R.drawable.color_06_normal,
//			R.drawable.color_07_normal, R.drawable.color_01_normal,
//			 R.drawable.color_11_normal
//			
//			 };

	private int[] mDeepColorRes={
			R.drawable.color_deep_0001,R.drawable.color_deep_0002,
			R.drawable.color_deep_0003,R.drawable.color_deep_0004,
			R.drawable.color_deep_0005,R.drawable.color_deep_0006,
			R.drawable.color_deep_0007,R.drawable.color_deep_0008
		};
	private int[] mLigthColorRes={
			R.drawable.color_light_001,R.drawable.color_light_002,
			R.drawable.color_light_003,R.drawable.color_light_004,
			R.drawable.color_light_005,R.drawable.color_light_006,
			R.drawable.color_light_007,R.drawable.color_light_008
		};
	 private int[] colorvalue = {
			 113,  146,
			 105 , 156,
			 97 , 166,
			 90  ,175,
			 99 ,160,
			 98, 160,
			 106 ,155,
			 118, 149,
			 102, 144,
			 109, 150,
			 125, 145,
			 117, 143,
			 84,  181,
			 106,  155,
			 70,  200,
			 95,  169
	 };
	
	public EditorViewFaceColor(Context context) {
		super(context);
		initControls();
	}
	
	public EditorViewFaceColor(Context context, AttributeSet attrs) {
		super(context, attrs);
		initControls();
	}

	private void initControls() {
		setTitle(R.string.edt_lbl_color);
		MakeupEngine.Init_Lib();
		LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		FrameLayout dummyView = new FrameLayout(mConfig.appContext);
		dummyView.setId(dummyView.hashCode());
		mPanelOverlay.addView(dummyView, params);
		
		colorGridLayout=inflate(getContext(), R.layout.edit_color_wheel, null);
		mSliderView = (cheelview) colorGridLayout.findViewById(R.id.camera_cheelview);
		final int sliderlength = 16;
		Bitmap[] sliderbitmap = new Bitmap[sliderlength * 2];
		for (int i = 0; i < sliderlength; i++) {
			int res = 0;
			if (i < 4) {
				res = mDeepColorRes[i];
			} else if (i < 8) {
				res = mLigthColorRes[i - 4];
			} else if (i < 12) {
				res = mLigthColorRes[i - 4];
			} else if (i < 16) {
				res = mDeepColorRes[i - 8];
			}
			
			sliderbitmap[2 * i] = BitmapFactory.decodeResource(getResources(),
					res);
			sliderbitmap[2 * i + 1] = BitmapFactory.decodeResource(
					getResources(), res);
		}
		
		mSliderView.installcheelunit(sliderbitmap);
		mSliderView.setVisibility(View.VISIBLE);
		mSliderView.setSingletapListener(this);
		
		mDeepColour = (ImageButton)colorGridLayout.findViewById(R.id.deep_image);
		mLightColour = (ImageButton)colorGridLayout.findViewById(R.id.light_image);
		final TextView mDeepTxt=(TextView) colorGridLayout.findViewById(R.id.deep_txt);
		final TextView mLightTxt=(TextView) colorGridLayout.findViewById(R.id.light_txt);
		
		colorGridLayout.findViewById(R.id.deep_txt).setOnClickListener(new OnClickListener(){  
            public void  onClick(View v)     
            {  
        		if(mSliderView.getActive()!=0){
        			mDeepColour.setBackgroundResource(R.drawable.deep_select);
        			mDeepTxt.setTextColor(Color.WHITE);
        			mLightTxt.setTextColor(Color.parseColor("#a5a5a5"));
        			mLightColour.setBackgroundResource(R.drawable.btn_light_selector);
        			mSliderView.rotate180();
        		}
            }  
        });
		colorGridLayout.findViewById(R.id.light_txt).setOnClickListener(new OnClickListener(){  
			public void  onClick(View v)     
			{  
        		if(mSliderView.getActive()!=8){
        			mDeepColour.setBackgroundResource(R.drawable.btn_deep_selector);
        			mDeepTxt.setTextColor(Color.parseColor("#a5a5a5"));
        			mLightTxt.setTextColor(Color.parseColor("#351402"));
        			mLightColour.setBackgroundResource(R.drawable.light_select);
        			
        			mSliderView.rotate180();
        		}
			}  
		});
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		LayoutParams colorParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, width / 2);
		colorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mPanelBottom.addView(colorGridLayout, colorParams);
		
//		mHelpView = new CommonHelpView(mConfig.appContext);
//		mHelpView.setText(R.string.edt_help_seekbar);
//		params = new RelativeLayout.LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		params.addRule(RelativeLayout.ABOVE, mSeekBar.getId());
//		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//		params.bottomMargin = DensityUtil.dip2px(getContext(), 10);
//		mPanelOverlay.addView(mHelpView, params);
		mSharedPreferences = mContect.getSharedPreferences(EditorViewFaceWhiten.GUIDE_HELP_SEEKBAR, Context.MODE_PRIVATE);
		boolean shouldShow = mSharedPreferences.getBoolean(EditorViewFaceWhiten.GUIDE_HELP_SEEKBAR, true);
		if (shouldShow){
			 params = new LayoutParams(
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
		
		mFeatureColor = new FeatureInfo(FeatureInfo.FEATUREMODE_SKINCOLOR);
		//mFeatureColor.setSkinColor(146, 155);
//		int currentCb = colorvalue[0];
//		int currentCr = colorvalue[1];
//		mFeatureColor.setSkinColor(currentCb, currentCr);
		
		mFeatureColor.SetSkinFoundationType(0);
		
		mFeatureColor.intensity = 50;
		mCurrentBitmap = mEngine.getEditBitmap().getBitmap();
		View seekView=inflate(getContext(), R.layout.editor_panel_trim_bottom, null);
		LayoutParams seekPam = new LayoutParams(
				LayoutParams.FILL_PARENT,
				DensityUtil.dip2px(getContext(), 300));
		seekPam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		seekPam.addRule(RelativeLayout.CENTER_VERTICAL);
		mPanelOverlay.addView(seekView, seekPam);
		
		mSeekBar = (VerticalSeekBar)seekView.findViewById(R.id.editor_trim_seek);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setMax(100);
		mSeekBar.setMinimumHeight(10);
		mSeekBar.setProgress(50);
		FaceInfo[] faces = EditEngine.getInstance().getEditBitmap().getFaces();
		MakeupEngine.ReloadFaceInfo(mCurrentBitmap, faces[0]);
		MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureColor);
		mDispView.invalidate();
		
//		MobclickAgent
//		.onEventBegin(getContext(), "Begin to faceWhiteMakeUp");
	}
	
	private void closeGuideHelpView(){
		if (vertGuide!=null && vertGuide.getVisibility()==View.VISIBLE) {
			vertGuide.clearAnimation();
			vertGuide.setVisibility(View.GONE);
			Editor editor = mSharedPreferences.edit();
			editor.putBoolean(EditorViewFaceWhiten.GUIDE_HELP_SEEKBAR, false);
			editor.commit();
		}
	}
	
	@Override
	public boolean isModified() {
		return mFeatureColor.GetIntensity()>0;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mFeatureColor.setIntensity(progress);
		mPercentTxt.setVisibility(View.VISIBLE);
		mPercentTxt.setText(progress+"%");
		mPercentTxt.clearAnimation();
		closeGuideHelpView();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
//		if(mHelpView != null)
//		mHelpView.setVisibility(View.GONE);
		if(vertGuide != null){
			vertGuide.clearAnimation();
			vertGuide.setVisibility(View.GONE);
			Editor editor = mSharedPreferences.edit();
			editor.putBoolean(EditorViewFaceWhiten.GUIDE_HELP_SEEKBAR, false);
			editor.commit();
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
//		new Thread(){
//
//			@Override
//			public void run() {
				// TODO Auto-generated method stub
				MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureColor);
//				
//				handle.sendEmptyMessage(HANDLER_MSG_DISPLAY_IMG);
//				super.run();
//			}
//			 
//	}.start();
				mDispView.invalidate();
				mHandler.sendEmptyMessageDelayed(MSG.HIDE_PERCENT_TXT, DEPLAY_TIME);
	}
	
	private class ColorItemAdapter extends BaseAdapter
	{
		private LayoutInflater inflater;
		private int[] resIndex;
		
		public ColorItemAdapter(Context context,int[] resIndex)
		{
			inflater = LayoutInflater.from(context);
			this.resIndex = resIndex;
		}
		
		@Override
		public int getCount() 
		{
			return resIndex != null && resIndex.length > 0 ? resIndex.length
					: 0;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ViewHold holder = null;
	        
	        if (null == convertView)
	        {
	            holder = new ViewHold();
	            convertView = inflater.inflate(R.layout.face_edit_color_item,
	                    null);
	            holder.image=(ImageView) convertView.findViewById(R.id.color_item_image);
	            holder.focusimage=(ImageView) convertView.findViewById(R.id.color_item_image_focus);
	            
	            convertView.setTag(holder);
	        }
	        else 
	        {
				holder=(ViewHold) convertView.getTag();
			}
	        holder.image.setImageResource(resIndex[position]);
	        
	        if(position !=  currentskincolorid)
            	holder.focusimage.setVisibility(View.GONE);
            else{
            	holder.focusimage.setVisibility(View.VISIBLE);
            }
	        
			return convertView;
		}
	}
	
	public class ViewHold
	{
		ImageView image;
		ImageView focusimage;
	}
	
	
	@Override
	public void onSave() {
		HashMap<String, String> args = new HashMap<String, String>();
		args.put("SkinColorId", String.valueOf(currentskincolorid));
		args.put("SkinColorStength", String.valueOf(mFeatureColor.GetIntensity()));
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnFoundationSave, args);
//		MobclickAgent.onEvent(mConfig.appContext, "UseSkinBeautify", args);
//		MobclickAgent.onEventEnd(getContext(), "End to faceWhiteMakeUp");
	}
	
	@Override
	public boolean onBackPressed() {
//		MobclickAgent.onEventEnd(getContext(), "End to faceWhiteMakeUp");
//		StatApi.onEvent(mConfig.appContext, StatApi.UMENG_EVENT_btnFoundationCancel);
		return super.onBackPressed();
	}

	@Override
	public void onsingleclick(int iIndex) {
		currentskincolorid = iIndex;
//		int currentCb = colorvalue[2*currentskincolorid];
//		int currentCr = colorvalue[2*currentskincolorid+1];
//		
//		mFeatureColor.setSkinColor(currentCb, currentCr);
		mFeatureColor.SetSkinFoundationType(iIndex);
		MakeupEngine.ManageImgae(mCurrentBitmap, mFeatureColor);

		mDispView.invalidate();
		
	}
	
	@Override
	public void startEnterAnimation(AnimationListener l) {
		super.startEnterAnimation(l);
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		int offset = wm.getDefaultDisplay().getWidth()/2;
		Animation animation = new TranslateAnimation(0, 0, offset, 0);
		animation.setDuration(300);
		mPanelBottom.clearAnimation();
		mPanelBottom.startAnimation(animation);
	}
	
	@Override
	public void startLeaveAnimation(AnimationListener l) {
		super.startLeaveAnimation(l);
		WindowManager wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);
		int offset = wm.getDefaultDisplay().getWidth()/2;
		Animation animation = new TranslateAnimation(0, 0, 0, offset);
		animation.setDuration(200);
		mPanelBottom.clearAnimation();
		mPanelBottom.startAnimation(animation);
	}
	
}
