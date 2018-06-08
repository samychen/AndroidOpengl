package com.cam001.photoeditor.beauty.makeup.widget;


import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;

import com.cam001.service.LogUtil;

public class TouchImageView extends GLImageView implements OnTouchListener{
	
	//public static final String TAG = "Tom";
	
	public static final int ENTER_LONGPRESS = 1;		//���볤��״̬
	public static final int IN_LONGPRESS = 2;			//���ڳ���״̬��
	public static final int LEAVE_LONGPRESS = 3;		//�뿪����״̬
	
	private GestureDetector mGestureDetector;
	private ScaleGestureDetector mScaleGestureDetector;
	private PointF			mCenterPoint = new PointF();
	private float			mPointLastDistance = 0f;
	
	private boolean			mbLongPress = false;
	private int				mPointerCount = 0;
	 
	public TouchImageView(Context context) {
		super(context);

		mGestureDetector = new GestureDetector(context, mGestureListener);
		mScaleGestureDetector = new ScaleGestureDetector(context, mScaleListener);
	}

	public TouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mGestureDetector = new GestureDetector(context, mGestureListener);
		mScaleGestureDetector = new ScaleGestureDetector(context, mScaleListener);
	}
	
	private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener(){
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
		//	Log.v(TAG, "onScale");
			if (Math.abs(detector.getCurrentSpan() - mPointLastDistance) < 5f){
				return true;
			}
			
			mPointLastDistance = detector.getCurrentSpan();
			if (detector.getScaleFactor() > 1){
				onImageScale(mCenterPoint, 1.08f);
			}else{
				onImageScale(mCenterPoint, 0.92f);
			}
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mCenterPoint.x = detector.getFocusX();
			mCenterPoint.y = detector.getFocusY();
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
		//	Log.v(TAG, "onScaleEnd");
			onLeaveAnimation();
			super.onScaleEnd(detector);
		}
	};
	
	private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){
		@Override
		public boolean onDoubleTap(MotionEvent e) {
	//		Log.v(TAG, "onDoubleTap");
			if(null != mDoubleTapUpListener)
			{
				mDoubleTapUpListener.onDoubleTapUp(e);
			}
			onRevert();
			return super.onDoubleTap(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (mPointerCount >= 2)
				return ;
			LogUtil.logV("Touch", "onLongPress");
			mbLongPress = true;
			if (null != mLongPressStatusListener){
				mLongPressStatusListener.onLongPressProcess(ENTER_LONGPRESS, e);
			}
			super.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
	//		Log.v(TAG, "onScroll");
			
			if (!mbLongPress){
				onTranslate(-distanceX, -distanceY);
			}else{
				if (null != mLongPressStatusListener){
//					Log.v(TAG, "mbLongPress+onScroll");
					mLongPressStatusListener.onLongPressProcess(IN_LONGPRESS,e2);
				}
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
//			Log.v(TAG, "onSingleTapUp");
			if (null != mSingleTapUpListener){
				mSingleTapUpListener.onSingleTapUp(e);
			}
			return super.onSingleTapUp(e);
		}};

    long lastdoublepointtime = 0;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		 
		mPointerCount = event.getPointerCount();
		LogUtil.logV("scale", "mPointerCount ="+ mPointerCount + "; Action="+event.getAction());
		if (!mbLongPress && mPointerCount >= 2){
			 			 
			lastdoublepointtime = System.currentTimeMillis();
			return mScaleGestureDetector.onTouchEvent(event);
		}else{
//			if( System.currentTimeMillis() - lastdoublepointtime <= 200)
//			{				 
//				return true; //ͣ��Ӧ
//			} 
			
			if (mbLongPress){
				if (event.getAction() == MotionEvent.ACTION_MOVE){
//					Log.v(TAG, "mbLongPress+onScroll");
					if (null != mLongPressStatusListener)
					     mLongPressStatusListener.onLongPressProcess(IN_LONGPRESS, event);
				}
			}
			
			if (event.getAction() == MotionEvent.ACTION_UP){
				if (mbLongPress){
					mbLongPress = false;
					if (null != mLongPressStatusListener){
						mLongPressStatusListener.onLongPressProcess(LEAVE_LONGPRESS, event);
					}
				}else
					onLeaveAnimation();
			}
			return mGestureDetector.onTouchEvent(event);
		}
	}
	
	/**
	 * ��ȡ�Ƿ񳤰���״̬
	 * @return
	 */
	public boolean getLongPress(){
		return mbLongPress;
	}
	

	/**
	 * ���ڴ��?��״̬�µĸ�����Ϣ��Ӧ
	 * @param	status	enter long press: 1, in long press: 2, leave long press:3
	 */
	private LongPressStatusListener mLongPressStatusListener = null;
	public void setLongPressListener(LongPressStatusListener l){
		mLongPressStatusListener = l;
	}
	public interface LongPressStatusListener{
		public void onLongPressProcess(int status, MotionEvent event);
	}
	
	/**
	 * ����view����Ӧ
	 */
	private SingleTapUpListener	mSingleTapUpListener = null;
	public void setSingleTapUpListener(SingleTapUpListener l){
		mSingleTapUpListener = l;
	}
	public interface SingleTapUpListener{
		public void onSingleTapUp(MotionEvent e);
	}
	
	
	/**
	 * ˫��view����Ӧ
	 */
	private DoubleTapUpListener	mDoubleTapUpListener = null;
	public void setDoubleTapUpListener(DoubleTapUpListener l){
		mDoubleTapUpListener = l;
	}
	public interface DoubleTapUpListener{
		public void onDoubleTapUp(MotionEvent e);
	}

}
