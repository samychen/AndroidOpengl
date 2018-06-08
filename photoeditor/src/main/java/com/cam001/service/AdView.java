package com.cam001.service;

import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.cam001.photoeditor.R;

public class AdView extends RelativeLayout implements OnClickListener {

	private AdAgent mAd = null;
	private ImageView mImageView = null;
	
	public AdView(Context context) {
		super(context);
		initControls();
	}
	
	public AdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initControls();
	}
	
	private void initControls() {
		mImageView = new ImageView(getContext());
		LayoutParams params = new LayoutParams(-1, -2);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		addView(mImageView, params);
		
		ImageView btnClose = new ImageView(getContext());
		btnClose.setImageResource(R.drawable.advertisting_close_select);
		params = new LayoutParams(-2, -2);
		params.addRule(ALIGN_PARENT_RIGHT);
		addView(btnClose, params);
		btnClose.setOnClickListener(this);
		setVisibility(View.VISIBLE);
		mAd = AdAgent.instance();
		load("MakeupMainBanner");
	}

	public void load(String id) {
//		if(mAd.isAdOn()) {
			setVisibility(View.VISIBLE);
			mAd.fillAd(id, this,mImageView, null, mImageView);
//		}
	}
//
	@Override
	public void onClick(View arg0) {
		setVisibility(View.GONE);
	}

}
