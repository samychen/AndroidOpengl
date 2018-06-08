package com.cam001.photoeditor.beauty;

import com.cam001.photoeditor.R;
import com.cam001.util.DensityUtil;
import com.cam001.util.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HelpedDisplayView extends FacePointDisplayView
	implements Runnable{

	private static final int ANIM_INTERVAL = 500;
	private static final int[] RES_ALL_POINTS_ANIM = new int[] {
		R.drawable.face_point_help01,
		R.drawable.face_point_help02,
		R.drawable.face_point_help03,
		R.drawable.face_point_help04,
		R.drawable.face_point_help05,
		R.drawable.face_point_help06
	};
	private static final int[] RES_EYE_POINTS_ANIM = new int[] {
		R.drawable.face_point_help11,
		R.drawable.face_point_help12,
		R.drawable.face_point_help13,
		R.drawable.face_point_help14
	};
	
	private static int MARGIN_TOP = 70;
	private static int MARGIN_LEFT = 10;
	private static boolean sNeedDipToPix = true;
	
	private int mAnimIndex = 0;
	private Thread mAnimThread = null;
	
	private boolean mbShowHelp = false;
	private Bitmap[] mAnimFram = null;
	
	public HelpedDisplayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(sNeedDipToPix) {
			MARGIN_TOP = DensityUtil.dip2px(context, MARGIN_TOP);
			MARGIN_LEFT = DensityUtil.dip2px(context, MARGIN_LEFT);
			sNeedDipToPix = false;
		}
	}

	public void showHelp(boolean bShow) {
		if(mbShowHelp==bShow) return;
		mbShowHelp = bShow;
		if(mbShowHelp) {
			mAnimIndex = 0;
			mAnimThread = new Thread(this);
			mAnimThread.start();
		} else {
			if(mAnimThread!=null) {
				mAnimThread.interrupt();
				Util.joinThreadSilently(mAnimThread);
				mAnimThread = null;
			}
		}
	}
	
	private void loadAnimRes() {
		int[] reslist = null;
		if(mbEnableEyes&&mbEnableMouth) {
			reslist = RES_ALL_POINTS_ANIM;
		} else {
			reslist = RES_EYE_POINTS_ANIM;
		}
		if(mAnimFram==null) {
			mAnimFram = new Bitmap[reslist.length];
			for(int i=0; i<reslist.length; i++) {
				mAnimFram[i] = BitmapFactory.decodeResource(getResources(), reslist[i]);
			}
		}
	}
	
	@Override
	public void showFacePoint(boolean bShow) {
		super.showFacePoint(bShow);
		showHelp(bShow);
	}
	
	@Override
	protected boolean handleSingleTouhEvent(MotionEvent event) {
		boolean res = super.handleSingleTouhEvent(event);
		if(mZoomer!=null && mZoomer.isViewDisplay()) {
			showHelp(false);
		}
		return res;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(mbShowHelp && mAnimFram!=null) {
			canvas.drawBitmap(mAnimFram[mAnimIndex], MARGIN_LEFT, MARGIN_TOP, null);
		}
	}

	@Override
	public void run() {
		while(mbShowHelp) {
			loadAnimRes();
			postInvalidate();
			try {
				Thread.sleep(ANIM_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
			mAnimIndex = (mAnimIndex+1)%mAnimFram.length;
		}
	}
}
