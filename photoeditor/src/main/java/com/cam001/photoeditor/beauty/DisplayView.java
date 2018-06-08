package com.cam001.photoeditor.beauty;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cam001.photoeditor.beauty.engine.EditEngine;
import com.cam001.service.LogUtil;

public class DisplayView extends View{

	private static final String TAG = "DisplayView";
	
	private static final int MSG_HIDE_CTRL = 0;
	private static final int DLY_HIDE_CTRL = 2000;
	
	private static class DispHandler extends Handler{
		
		@Override
		public void handleMessage(Message msg) {
			DisplayView ths = (DisplayView)msg.obj;
			switch(msg.what) {
			case MSG_HIDE_CTRL:
				ths.mEngine.showStampCtrl(false);
				ths.invalidate();
				return;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
	protected EditEngine mEngine = null;
	protected Handler mHandler = new DispHandler();
	
	public DisplayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mEngine = EditEngine.getInstance();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		LogUtil.logV(TAG, "onSizeChanged w=%d h=%d", w, h);
		super.onSizeChanged(w, h, oldw, oldh);
		mEngine.setDispViewSize(w, h);
		invalidate();
	}

	/**
	 * ��������true��false������ֻҪ���ô˷������ᵼ��Stamp�����½���
	 * ע������trueʱdrawʱ���ӡ�������ܸܺߣ���ʵ�ʲ����Ͽ���
	 * ��Ҫ��Բ�Ǳ߿�֪��Ӳ�����٣���Crash��Ч���쳣
	 * @param bEnable
	 */
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
	
	@Override
	protected void onDraw(Canvas canvas) {
		mEngine.draw(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		if(mEngine==null) {
			return bHandled;
		}
		long time1 = System.currentTimeMillis();
		bHandled = mEngine.dispatchTouchEvent(event);
		long time2 = System.currentTimeMillis();
		System.out.println("dispatchTouchEvent cost: "+(time2-time1));
		if(bHandled) {
			invalidate();
			mHandler.removeMessages(MSG_HIDE_CTRL);
			Message msg = Message.obtain(mHandler, MSG_HIDE_CTRL, this);
			mHandler.sendMessageDelayed(msg, DLY_HIDE_CTRL);
			return true;
		}
		mEngine.showStampCtrl(false);
		return bHandled;
	}

	public void onPause() {
		
	}
	
	public void onResume() {
//		enableHWAccelerated(false);
		invalidate();
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
}
