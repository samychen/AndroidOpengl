package com.cam001.widget;

import com.cam001.util.Util;

import android.graphics.Matrix;

public class MatrixAnimation implements Runnable{

	private static final int TRANS_ANIM_FRAME_INTER = 15;
	private static final int TRANS_ANIM_FRAME_COUNT = 20;
	private Thread mAnimThread = null;
	private boolean mIsAnimating = false;
	private Matrix mMat = null;
	private OnRefreshListener mListener = null;
	
	private float[] mTransDelta = new float[9];
	private float[] mTransSrc = new float[9];
	private float[] mTransDst = new float[9];
	private float[] mTransTmp = new float[9];
	
	public interface OnRefreshListener {
		void onRefresh(Matrix mat);
	}
	
	public MatrixAnimation() {
		
	}
	
	public boolean isAnimating() {
		return mIsAnimating;
	}
	
	public void startAnimation(Matrix matSrc, Matrix matDst, OnRefreshListener l) {
		if(mIsAnimating) stopAnimation();
		mListener = l;
		mIsAnimating = true;
		mMat = matSrc;
		matSrc.getValues(mTransSrc);
		matDst.getValues(mTransDst);
		for(int i=0; i<9; i++) {
			mTransDelta[i] = (mTransDst[i]-mTransSrc[i]);
		}
		mAnimThread = new Thread(this);
		mAnimThread.start();
	}
	
	public void stopAnimation() {
		mIsAnimating = false;
		mAnimThread.interrupt();
		Util.joinThreadSilently(mAnimThread);
		mAnimThread = null;
	}

	@Override
	public void run() {
		int frameIndex = 0;
		while(frameIndex<TRANS_ANIM_FRAME_COUNT) {
			if(!mIsAnimating) {
				return;
			}
			frameIndex ++;
			double a1 = (1.0/TRANS_ANIM_FRAME_COUNT)*(TRANS_ANIM_FRAME_COUNT-frameIndex);
			double a = Math.pow(a1, 3);
			a = 1.0 - a;
			for(int i=0; i<9; i++) {
				mTransTmp[i] = mTransSrc[i] + (float)(mTransDelta[i]*a);
			}
			mMat.setValues(mTransTmp);
			if(mListener!=null) mListener.onRefresh(mMat);
			try {
				Thread.sleep(TRANS_ANIM_FRAME_INTER);
			} catch (InterruptedException e) {
				return;
			}
		}
		mMat.setValues(mTransDst);
		if(mListener!=null) mListener.onRefresh(mMat);
		mIsAnimating = false;
	}
}
