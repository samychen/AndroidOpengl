package com.cam001.photoeditor.makeup.editor;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cam001.photoeditor.R;
import com.cam001.widget.VerticalSeekBar;


public class EditorViewCateBase extends FrameLayout implements
		OnSeekBarChangeListener {
	protected HorizontalScrollView mHScrollLayout = null;
	protected GridView mGridView = null;
	protected VerticalSeekBar mSeekBar = null;
	protected TextView mTextView = null;
	protected Context mContext = null;
	protected boolean mBlShowText = true;
	public final static int DefaultSelect = -1;
	protected int mSel = DefaultSelect;

	public EditorViewCateBase(Context context) {
		super(context);
		initControls(context);
	}

	protected void initControls(Context context) {
		mContext = context;
		inflate(getContext(), R.layout.layout_edit_cate_viewbase, this);
		mHScrollLayout = (HorizontalScrollView) findViewById(R.id.bottom_layout);
		mGridView = (GridView) findViewById(R.id.gridview);
		mSeekBar = (VerticalSeekBar) findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mTextView = (TextView) findViewById(R.id.textview);
		mTextView.setShadowLayer(3, 0, 1, Color.parseColor("#4D000000"));
	}

	protected void SmoothScroolTo(final int disX){
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHScrollLayout.smoothScrollTo(disX, 0);
			}
		}, 400);
	}
	
	protected void startAnimation() {
		mTextView.clearAnimation();
		Animation anim = new AlphaAnimation(1, 0);
		anim.setDuration(500);
		mTextView.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mTextView.setVisibility(View.GONE);
			}
		});
	}

	public void setHighlight(int pos) {
	}

	protected Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			startAnimation();
		}

	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (mBlShowText) {
			mHandler.removeMessages(0);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(String.valueOf(progress) + "%");
			mHandler.sendEmptyMessageDelayed(0, 200);
		}
	}

	protected void showTextView() {
		if (mBlShowText) {
			mHandler.removeMessages(0);
			mTextView.setVisibility(View.VISIBLE);
			int progress = mSeekBar.getProgress();
			mTextView.setText(String.valueOf(progress) + "%");
			mHandler.sendEmptyMessageDelayed(0, 200);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}
}
