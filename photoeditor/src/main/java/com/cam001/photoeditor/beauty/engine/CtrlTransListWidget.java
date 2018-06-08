package com.cam001.photoeditor.beauty.engine;

import java.util.ArrayList;
import java.util.List;

import com.cam001.photoeditor.AppConfig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;

public class CtrlTransListWidget implements Widget, CtrlTransWidget.OnButtonClickListener {
	
	private static final int MAX_STAMP_SIZE = 10;
	
	private List<CtrlTransWidget> mListWidget = null;
	private Bitmap mBmpCtrl = null;
	private Bitmap mBmpDel = null;
	private Bitmap mBmpCpy = null;
	
	private RectF mRectImagCanv = null;
	private RectF mRectCtrlCanv = null;
	
	public CtrlTransListWidget(Bitmap controller, Bitmap delete, Bitmap copy) {
		mBmpCtrl = controller;
		mBmpDel = delete;
		mBmpCpy = copy;
		mListWidget = new ArrayList<CtrlTransWidget>();
	}
	
	public void setImageDispRect(RectF rect) {
		mRectImagCanv = rect;
		for(CtrlTransWidget widget:mListWidget) {
			widget.setImagDispRect(mRectImagCanv);
		}
	}
	
	public void setCtrlDispRect(RectF rect) {
		mRectCtrlCanv = rect;
		for(CtrlTransWidget widget:mListWidget) {
			widget.setDisplayRect(mRectCtrlCanv);
		}
	}
	
	public void addWidget(Bitmap display, float scale) {
		if(getCount()>=MAX_STAMP_SIZE) {
			Context c = AppConfig.getInstance().appContext;
//			String msg = c.getString(R.string.edt_tst_stamp_exceed_limit);
//			msg = String.format(msg, MAX_STAMP_SIZE);
//			ToastUtil.showShortToast(c, msg);
			return;
		}
		CtrlTransWidget widget = new CtrlTransWidget(display, mBmpCtrl, mBmpDel,mBmpCpy);
		widget.scale(scale, scale);
		widget.setImagDispRect(mRectImagCanv);
		widget.setDisplayRect(mRectCtrlCanv);
		widget.setOnDeleteClickListener(this);
		mListWidget.add(widget);
	}
	
	public void showCtrl(boolean bShow) {
		int widgetSize = mListWidget.size();
		if(widgetSize<1) {
			return;
		}
		CtrlTransWidget topWidget = mListWidget.get(widgetSize-1);
		topWidget.showCtrl(bShow, bShow);
	}
	
	public void removeTopWidget() {
		int widgetSize = mListWidget.size();
		if(widgetSize<1) {
			return;
		}
		mListWidget.remove(widgetSize-1);
	}
	
	public int getCount() {
		if(mListWidget==null) {
			return 0;
		}
		return mListWidget.size();
	}
	
	public void reset() {
		mListWidget.clear();
	}
	
	@Override
	public void draw(Canvas canvas) {
		for(CtrlTransWidget widget: mListWidget) {
			widget.draw(canvas);
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		int widgetCount = mListWidget.size();
		if(widgetCount<1) {
			return false;
		}

		int pCount = event.getPointerCount();
		CtrlTransWidget focusedWidget = null;
		if(pCount==1) { //Single Touch
			for (int i = widgetCount - 1; i >= 0; i--) {
				focusedWidget = mListWidget.get(i);
				if (focusedWidget.dispatchTouchEvent(event)) {
					bHandled = true;
					switch(event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						showCtrl(focusedWidget, true);
						break;
					default:
						break;
					}
					break;
				}
			}
		} else { //Multi-touch. Do not show the control icon.
			focusedWidget = mListWidget.get(widgetCount-1);
			if (focusedWidget.dispatchTouchEvent(event)) {
				//Only show focus rect when multi-touch.
				focusedWidget.showCtrl(false, true);
				bHandled = true;
			}
		}
		return bHandled;
	}
	
	private void showCtrl(CtrlTransWidget widget, boolean bShow) {
		if(bShow) {
			//Bring the shown widget to the top of z order.
			mListWidget.remove(widget);
			for(CtrlTransWidget w: mListWidget) {
				w.showCtrl(false, false);
			}
			mListWidget.add(widget);
			widget.showCtrl(true, true);
		} else {
			widget.showCtrl(false, false);
		}
	}

	@Override
	public void onDeleteClick(CtrlTransWidget w) {
		mListWidget.remove(w);
	}

	@Override
	public void onCopyClick(CtrlTransWidget w) {
		if(getCount()>=MAX_STAMP_SIZE) {
			w.showCtrl(true, true);
			Context c = AppConfig.getInstance().appContext;
//			String msg = c.getString(R.string.edt_tst_stamp_exceed_limit);
//			msg = String.format(msg, MAX_STAMP_SIZE);
//			ToastUtil.showShortToast(c, msg);
			return;
		}
		CtrlTransWidget cpy = w.clone();
		mListWidget.add(cpy);
		cpy.showCtrl(true, true);
	}

}