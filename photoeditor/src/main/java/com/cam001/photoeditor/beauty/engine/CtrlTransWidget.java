package com.cam001.photoeditor.beauty.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.MotionEvent;

public class CtrlTransWidget extends TransformWidget{

	
	public interface OnButtonClickListener {
		void onDeleteClick(CtrlTransWidget w);
		void onCopyClick(CtrlTransWidget w);
	}
	
	private float mLastX0,mLastY0,mLastX1,mLastY1;
	private boolean mbTouchDisp = false;
	private boolean mbTouchCtrl = false;
	private boolean mbTouchDel = false;
	private boolean mbTouchCpy = false;
	private OnButtonClickListener mlClick = null;
	
	private Bitmap mBmpCtrl = null;
	private float mRadius = 0.0f;
	private float mCtrlX = 0.0f;
	private float mCtrlY = 0.0f;
	private Bitmap mBmpDel = null;
	private float mDelX = 0.0f;
	private float mDelY = 0.0f;
	private Bitmap mBmpCpy = null;
	private float mCpyX = 0.0f;
	private float mCpyY = 0.0f;
	private Paint mBorderPaint = null;
	
	//Border lines around the image.
	private boolean mbShowBorder = false;
	//Include delete button and transform control button.
	private boolean mbShowThmb = false;
	
	private boolean mbFirstLoad = true;

	private RectF mRectCanvas = null;
	private float mMaxWidgetRadius;
	private float mMinWidgetRadius;
	
	
	private float[] mPtsSrc = new float[8];
	private float[] mPtsDst = new float[8];
	private float[] mLinSrc = new float[16];
	private float[] mLinDst = new float[16];
	
	public CtrlTransWidget(Bitmap display, Bitmap controller, Bitmap delete, Bitmap copy) {
		super(display);
		if(controller==null) {
			throw new NullPointerException();
		}
		mBmpCtrl = controller;
		mBmpDel = delete;
		mBmpCpy = copy;
		initCtrl();
	}
	
	public void setOnDeleteClickListener(OnButtonClickListener l) {
		mlClick = l;
	}
	
	private void initCtrl() {
		float bmpWidth = mBmpDisp.getWidth();
		float bmpHeight = mBmpDisp.getHeight();
		mRadius = (float)Math.sqrt(bmpWidth*bmpWidth+bmpHeight*bmpHeight)/2.0f;
		mCtrlX = bmpWidth - mBmpCtrl.getWidth()/2.0f;
		mCtrlY = bmpHeight - mBmpCtrl.getHeight()/2.0f;
		mDelX = -mBmpDel.getWidth()/2.0f;
		mDelY = -mBmpDel.getHeight()/2.0f;
		mCpyX = -mBmpCpy.getWidth()/2.0f;
		mCpyY = -mBmpCpy.getHeight()/2.0f;
		mBorderPaint = new Paint();
		mBorderPaint.setColor(0xFFFFFFFF);
		mBorderPaint.setStyle(Style.STROKE);
		mBorderPaint.setStrokeWidth(3);
		mPtsSrc[0] = bmpWidth;
		mPtsSrc[1] = bmpHeight;
		mPtsSrc[2] = bmpWidth;
		mPtsSrc[3] = 0.0f;
		mPtsSrc[4] = 0.0f;
		mPtsSrc[5] = 0.0f;
		mPtsSrc[6] = 0.0f;
		mPtsSrc[7] = bmpHeight;
		
		mLinSrc[0] = mLinSrc[8] = mPtsSrc[0];
		mLinSrc[1] = mLinSrc[9] = mPtsSrc[1];
		mLinSrc[2] = mLinSrc[14] = mPtsSrc[2];
		mLinSrc[3] = mLinSrc[15] = mPtsSrc[3];
		mLinSrc[4] = mLinSrc[12] = mPtsSrc[4];
		mLinSrc[5] = mLinSrc[13] = mPtsSrc[5];
		mLinSrc[6] = mLinSrc[10] = mPtsSrc[6];
		mLinSrc[7] = mLinSrc[11] = mPtsSrc[7];
	}
	
	/**
	 * Show or hide the controls of the widget. 
	 * Include delete button, rotate/scale button, and the rect around the widget.
	 * @param bShowThmb whether to show the delete button and rotate/scale button.
	 * @param bShowRect whether to show the rect around the widget.
	 */
	public void showCtrl(boolean bShowThmb, boolean bShowRect) {
		mbShowThmb = bShowThmb;
		mbShowBorder = bShowRect;
	}
	
	private boolean isPointInCtrl(float x, float y) {
		if(!mbShowThmb) return false;
		return x>mCtrlX && x<(mCtrlX+mBmpCtrl.getWidth())
				&& y>mCtrlY && y<(mCtrlY+mBmpCtrl.getHeight());
	}

	private boolean isPointInDelete(float x, float y) {
		return x>mDelX && x<(mDelX+mBmpDel.getWidth())
				&& y>mDelY && y<(mDelY+mBmpDel.getHeight());
	}
	
	private boolean isPointInCopy(float x, float y) {
		return x>mCpyX && x<(mCpyX+mBmpCpy.getWidth())
				&& y>mCpyY && y<(mCpyY+mBmpCpy.getHeight());
	}
	
	/**
	 * Keep the widget size not to small or to large.
	 */
	private void ensureWidgetSize() {
		mMatDisp.mapPoints(mPtsDst, mPtsSrc);
		float x0 = mPtsDst[0];
		float y0 = mPtsDst[1];
		float x1 = mPtsDst[4];
		float y1 = mPtsDst[5];
		double radius = Math.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0))/2.0;
		//Radius of the widget must between mMinWidgetRadius and mMaxWidgetRadius;
		if(radius<mMinWidgetRadius) {
			float scale = (float) (mMinWidgetRadius/radius);
			scale(scale, scale);
			mMatDisp.mapPoints(mLinDst, mLinSrc);
		} else if(radius>mMaxWidgetRadius) {
			float scale = (float) (mMaxWidgetRadius/radius);
			scale(scale, scale);
			mMatDisp.mapPoints(mLinDst, mLinSrc);
		}
	}
	
	/**
	 * Keep the widget in the image.
	 */
	private void ensureWidgetPosition() {
		float x = 0.0f;
		float y = 0.0f;
		boolean needTrans = false;
		float left = mRecDisp.left - mCenterX;
		if(left > 0) {
			x = left;
			needTrans = true;
		}
		float right = mRecDisp.right - mCenterX;
		if(right < 0) {
			x = right;
			needTrans = true;
		}
		float top = mRecDisp.top - mCenterY;
		if(top > 0) {
			y = top;
			needTrans = true;
		}
		float bottom = mRecDisp.bottom - mCenterY;
		if(bottom < 0) {
			y = bottom;
			needTrans = true;
		}
		if(needTrans) {
			move(x, y);
		}
	}
	
	/**
	 * Calculate the delete button / control button position.
	 */
	private void ensureCtrlPosition() {
		mMatDisp.mapPoints(mPtsDst, mPtsSrc);
		mMatDisp.mapPoints(mLinDst, mLinSrc);
		mCtrlX = mPtsDst[0];
		mCtrlY = mPtsDst[1];
		mCtrlX -= mBmpCtrl.getWidth()/2.0f;
		mCtrlY -= mBmpCtrl.getHeight()/2.0f;
		mDelX = mPtsDst[4];
		mDelY = mPtsDst[5];
		mDelX -= mBmpDel.getWidth()/2.0f;
		mDelY -= mBmpDel.getHeight()/2.0f;
		mCpyX = mPtsDst[2];
		mCpyY = mPtsDst[3];
		mCpyX -= mBmpCpy.getWidth()/2.0f;
		mCpyY -= mBmpCpy.getHeight()/2.0f;
	}
	
	/**
	 * Keep the controller button for rotate/scale in the view.
	 */
	private void ensureCtrlInDisplay() {
		mMatDisp.mapPoints(mPtsDst, mPtsSrc);
		int i=0;
		for(i=0; i<4; i++) {
			mCtrlX = mPtsDst[i*2];
			mCtrlY = mPtsDst[i*2+1];
			mCtrlX -= mBmpCtrl.getWidth()/2.0f;
			mCtrlY -= mBmpCtrl.getHeight()/2.0f;
			if(mRectCanvas==null) {
				return;
			}
			if(mRectCanvas.contains(mCtrlX, mCtrlY,
					mCtrlX + mBmpCtrl.getWidth(),
					mCtrlY + mBmpCtrl.getHeight())) {
				break;
			}
		}
		
		float[] pts = new float[8];
		int j = 0;
		for(int k=i; k<i+4; k++) {
			pts[j] = mPtsSrc[(2*k)%8];
			j++;
			pts[j] = mPtsSrc[(2*k)%8+1];
			j++;
		}
		mPtsSrc = pts;
	}
	
	private float getScale(float x, float y) {
		double o = Math.sqrt((mLastX0-mCenterX)*(mLastX0-mCenterX) +(mLastY0-mCenterY)*(mLastY0-mCenterY));
		double n = Math.sqrt((x-mCenterX)*(x-mCenterX) +(y-mCenterY)*(y-mCenterY));
		return (float)(n/o);
	}
	
	private float getScale(float x0, float y0, float x1, float y1) {
		double o = Math.sqrt((mLastX0-mLastX1)*(mLastX0-mLastX1) +(mLastY0-mLastY1)*(mLastY0-mLastY1));
		double n = Math.sqrt((x0-x1)*(x0-x1) +(y0-y1)*(y0-y1));
		return (float)(n/o);
	}
	
	private float getRotate(float x, float y) {
		double a1 = mLastY0-mCenterY;
		double b1 = mLastX0-mCenterX;
		double angle1 = Math.atan(a1/b1);
		if(b1<0) {
			angle1 += Math.PI;
		}
		
		double a2 = y-mCenterY;
		double b2 = x-mCenterX;
		double angle2 = Math.atan(a2/b2);
		if(b2<0) {
			angle2 += Math.PI;
		}
		
		double degree = (angle2-angle1)*180/Math.PI;

		return (float)degree;
	}
	
	private float getRotate(float x0, float y0, float x1, float y1) {
		double a1 = mLastY0-mLastY1;
		double b1 = mLastX0-mLastX1;
		double angle1 = Math.atan(a1/b1);
		if(b1<0) {
			angle1 += Math.PI;
		}
		
		double a2 = y0-y1;
		double b2 = x0-x1;
		double angle2 = Math.atan(a2/b2);
		if(b2<0) {
			angle2 += Math.PI;
		}
		
		double degree = (angle2-angle1)*180/Math.PI;

		return (float)degree;
	}
	
	/**
	 * Set the rect to be display. (View rect, include the black side)
	 */
	@Override
	public void setDisplayRect(RectF rect) {
		mRectCanvas = rect;
		//Make the stamp in the center of display.
		if(mbFirstLoad) {
			float x = (mRectCanvas.width() - mBmpDisp.getWidth()) / 2.0f;
			float y = (mRectCanvas.height() - mBmpDisp.getHeight()) / 2.0f;
			move(x, y);
			mbFirstLoad = false;
		}
		mMatDisp.mapPoints(mLinDst, mLinSrc);
		double dispRadius = Math.sqrt(rect.width()*rect.width()+rect.height()*rect.height())/2.0;
		mMinWidgetRadius = (float)(dispRadius*0.05);
		mMaxWidgetRadius = (float)(dispRadius*1.0);
		ensureCtrlPosition();
	}
	
	/**
	 * Set the rect of the image to drawn. (exclude the black side)
	 * @param rect
	 */
	public void setImagDispRect(RectF rect) {
		super.setDisplayRect(rect);
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if(mbShowBorder) {
			/* Draw border as a rectangle.*/
			canvas.drawLines(mLinDst, mBorderPaint);
			/* Draw border as a circle.*/
//			canvas.drawCircle(mCenterX, mCenterY, mMatDisp.mapRadius(mRadius), mBorderPaint);
		}
		if(mbShowThmb) {
			canvas.drawBitmap(mBmpCtrl, mCtrlX, mCtrlY, null);
			canvas.drawBitmap(mBmpDel, mDelX, mDelY, null);
			canvas.drawBitmap(mBmpCpy, mCpyX, mCpyY, null);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		int pCount = event.getPointerCount();
		switch(pCount) {
		case 1:
			bHandled = handleSingleTouchEvent(event);
			break;
		case 2:
			bHandled = handleMultiTouchEvent(event);
			break;
		default:
			break;
		}
		return bHandled;
	}

	private boolean handleSingleTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastX0 = event.getX();
			mLastY0 = event.getY();
			if(isPointInCtrl(mLastX0, mLastY0)) {
				mbTouchCtrl = true;
				bHandled = true;
				break;
			}
			if(isPointInDelete(mLastX0, mLastY0)) {
				mbTouchDel = true;
				bHandled = true;
				break;
			}
			if(isPointInDisp(mLastX0, mLastY0)) {
				mbTouchDisp = true;
				bHandled = true;
				break;
			}
			if(isPointInCopy(mLastX0, mLastY0)) {
				mbTouchCpy = true;
				bHandled = true;
				break;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float offsetX = event.getX() - mLastX0;
			float offsetY = event.getY() - mLastY0;
			if(mbTouchDisp) {
				move(offsetX, offsetY);
				mCtrlX += offsetX;
				mCtrlY += offsetY;
				bHandled = true;
			} else if(mbTouchCtrl){
				mCtrlX += offsetX;
				mCtrlY += offsetY;
				float scale = getScale(event.getX(),event.getY());
				scale(scale, scale);
				rotate(getRotate(event.getX(),event.getY()));
				bHandled = true;
			}
			mMatDisp.mapPoints(mLinDst, mLinSrc);
			mLastX0 = event.getX();
			mLastY0 = event.getY();
			ensureCtrlPosition();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(mbTouchDisp || mbTouchCtrl) {
				mbTouchDisp = false;
				mbTouchCtrl = false;
				ensureWidgetSize();
				ensureWidgetPosition();
				ensureCtrlInDisplay();
				ensureCtrlPosition();
				bHandled = true;
			}
			if(mbTouchDel) {
				mbTouchDel = false;
				if(isPointInDelete(event.getX(), event.getY())) {
					if(mlClick!=null) mlClick.onDeleteClick(this);
					mlClick = null;
				}
				bHandled = true;
			}
			if(mbTouchCpy) {
				mbTouchCpy = false;
				if(isPointInCopy(event.getX(), event.getY())) {
					if(mlClick!=null) mlClick.onCopyClick(this);
				}
				bHandled = true;
			}
			break;
		default:
			break;
		}
		return bHandled;
	}
	
	private boolean handleMultiTouchEvent(MotionEvent event) {
		mbTouchDisp = false;
		switch(event.getAction()& MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_POINTER_DOWN:
			mLastX0 = event.getX(0);
			mLastY0 = event.getY(0);
			mLastX1 = event.getX(1);
			mLastY1 = event.getY(1);
			break;
		case MotionEvent.ACTION_MOVE:
			float x0 = event.getX(0);
			float y0 = event.getY(0);
			float x1 = event.getX(1);
			float y1 = event.getY(1);
			
			float offsetX = (x0-mLastX0 + x1-mLastX1)/2.0f;
			float offsetY = (y0-mLastY0 + y1-mLastY1)/2.0f;
			move(offsetX, offsetY);
			float scale = getScale(x0, y0, x1, y1);
			scale(scale, scale);
			float rotate = getRotate(x0, y0, x1, y1);
			rotate(rotate);
			mMatDisp.mapPoints(mLinDst, mLinSrc);
			
			mLastX0 = x0;
			mLastY0 = y0;
			mLastX1 = x1;
			mLastY1 = y1;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			ensureWidgetSize();
			ensureWidgetPosition();
			ensureCtrlInDisplay();
			ensureCtrlPosition();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected CtrlTransWidget clone() {
		CtrlTransWidget c = new CtrlTransWidget(mBmpDisp, mBmpCtrl, mBmpDel, mBmpCpy);
		c.setDisplayRect(mRectCanvas);
		c.setImagDispRect(mRecDisp);
		c.setOnDeleteClickListener(mlClick);
		c.mMatDisp.set(mMatDisp);
		c.mCenterX = mCenterX;
		c.mCenterY = mCenterY;
		c.move(50,50);
		c.ensureWidgetPosition();
		c.ensureCtrlInDisplay();
		c.ensureCtrlPosition();
		return c;
	}
	
}