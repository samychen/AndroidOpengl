package com.cam001.photoeditor.beauty.makeup.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.cam001.photoeditor.AppConfig;
import com.cam001.util.DensityUtil;
import com.cam001.util.LogUtil;

public class MagnifierView extends View
		  {

	Context mContext;
	 
	private Path mPath = new Path();
	private Matrix matrix = new Matrix();
	private Bitmap zoombitmap = null;
	private Bitmap screenbitmap = null;
	 
 
	// �Ŵ󾵵İ뾶
	private static  int XRADIUS = 100;
	private static  int YRADIUS = 100;
	
	// �Ŵ���
	private static final float FACTOR = (float) 2;

	 

	Paint paint = new Paint();
 
	Paint mRedCirclePaint, mGreenCirclePaint, mFramePaint;
	boolean bDisplayZoom;
	MotionEvent currentevent;
	int mStartX = 0;
	int mStartY = 0;
	int mBottomMargin = 0;
	
	int mLeftViewX = 0;
	int mTopViewY = 0;
	
	
	int mViewWidth, mViewHeight;
	Rect leftviewrect, rightviewrect;
	int mTopViewIndex = -1;
	
	
	 

	float[] boundPoints;

	int pointradious = 0;
	int mCurrentpoint = -1;
	 
	
//	private int[] radiusgroup = {10, 12, 14, 16, 18}; 
//	private int radiusIndex = 4;
	
	private Bitmap circleresource;
 
	
	private View dispView;
	
	public MagnifierView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Intial(context);
	}

	public MagnifierView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		Intial(context);
	}

	public MagnifierView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		Intial(context);
	}

	private void Intial(Context context) {
		mContext = context;

	//	mHolder = getHolder();
	//	mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
	 //	setZOrderOnTop(true);
	//	mHolder.setFormat(PixelFormat.TRANSPARENT);
	//	mHolder.addCallback(this);

		//mPath.addCircle(RADIUS, RADIUS, RADIUS, Direction.CW);
		//mPath.addRect(0, 0, 2*XRADIUS, 2*YRADIUS, Direction.CW);
		 

		bDisplayZoom = false;
		paint.setAntiAlias(true);
		// this.setVisibility(View.INVISIBLE);

		mCurrentpoint = -1;

		mLeftViewX = DensityUtil.dip2px(mContext, 10);
		mTopViewY = DensityUtil.dip2px(mContext, 75);

		mRedCirclePaint = new Paint();
		mRedCirclePaint.setAntiAlias(true);
		mRedCirclePaint.setStrokeWidth(4);
		mRedCirclePaint.setColor(Color.RED);
		mRedCirclePaint.setStyle(Style.FILL);

		mGreenCirclePaint = new Paint();
		mGreenCirclePaint.setAntiAlias(true);
		mGreenCirclePaint.setStrokeWidth(2);
		mGreenCirclePaint.setColor(Color.GREEN);
		mGreenCirclePaint.setStyle(Style.STROKE);

		mFramePaint = new Paint();

		mFramePaint.setStrokeWidth(10);
		mFramePaint.setColor(Color.parseColor("#edebeb"));
		mFramePaint.setStyle(Style.STROKE);
		
	 	 
		leftviewrect = new Rect();
		rightviewrect = new Rect();
		mTopViewIndex = 0;
		
		disableHWAccelerated();
		bringToFront();
	}

	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void disableHWAccelerated() {
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}
	 
	 

	public void setDisplayView(View view)
	{
		dispView = view;
	}

	 
	
	
	
	public void intialbackgroudbmp(Bitmap bitmap) {

		 screenbitmap = bitmap;
		
	}
	
   

	// @Override
	// public void surfaceDestroyed(SurfaceHolder holder) {
	// // TODO Auto-generated method stub
	// if (zoombitmap != null) {
	// zoombitmap.recycle();
	// zoombitmap = null;
	// }
	//
	//
	//
	// if (screenbitmap != null) {
	// screenbitmap.recycle();
	// screenbitmap = null;
	// }
	//
	// }
	
	
	
	 
	 
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		if (changed) {
			mStartX = left;
			mStartY = top;
			mBottomMargin = AppConfig.getInstance().screenHeight- bottom;
			 
		

		}
	}

	
	
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		if(w != oldw || h != oldh)
		{
			bDisplayZoom = false;
			mViewWidth = w;
			mViewHeight = h;
			
			XRADIUS = (w-mLeftViewX*2)/4;
			YRADIUS = XRADIUS*2/3;
			mPath.addRect(0, 0, 2*XRADIUS, 2*YRADIUS, Direction.CW);
			
			
			leftviewrect.left = mLeftViewX;
			leftviewrect.right = mLeftViewX+XRADIUS*2 ;
			rightviewrect.left = mViewWidth-1-mLeftViewX-XRADIUS*2;
			rightviewrect.right = mViewWidth-1-mLeftViewX;
			
			leftviewrect.top = rightviewrect.top = mTopViewY;
			leftviewrect.bottom = rightviewrect.bottom = mTopViewY+YRADIUS*2;
			mTopViewIndex = 0;	
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	
	int mCurrentX  = 0;
	int mCurrentY = 0 ; 
	
	@Override
	
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		//DoDraw(canvas);
		if (bDisplayZoom ) {
		
			DoDraw(canvas); 
	 
		}
		
		super.onDraw(canvas);
	}

	public void DoDraw(Canvas canvas) {
		// TODO Auto-generated method stub

		long t = System.currentTimeMillis();
		try {
			
		 	int viewleft = leftviewrect.left ;

		 //	canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			
		 

			if (bDisplayZoom ) {

			 
				
				LogUtil.logV("magnifer", "mStartY=" + mStartY);
				LogUtil.logV("magnifer", "mCurrentY="+mCurrentY);

				canvas.save();
				
				if(mTopViewIndex == 0 && leftviewrect.contains(mCurrentX, mCurrentY))
				{
					mTopViewIndex = 1;
					
				}
				
				if(mTopViewIndex == 1 && rightviewrect.contains(mCurrentX, mCurrentY))
				{
					mTopViewIndex = 0;
					
				}
				
				if(mTopViewIndex == 1)
					viewleft = rightviewrect.left;

				 

				// ���û���λ��

			//	canvas.translate(mCurrentX - RADIUS, mCurrentY - 2 * RADIUS);
				canvas.translate(viewleft, mTopViewY);
				// ����
				canvas.clipPath(mPath);
				canvas.scale(FACTOR, FACTOR);
				canvas.translate(XRADIUS / FACTOR - mCurrentX, YRADIUS / FACTOR
						- mCurrentY);
				// canvas.drawBitmap(pixels, 0, pointview.mWidth, 0, 0,
				// pointview.mWidth,
				// pointview.mHeight, true, paint);

				canvas.drawColor(Color.BLACK);
				canvas.drawBitmap(screenbitmap, 0, 0, paint);

				// canvas.translate(RADIUS-mCurrentX*FACTOR,
				// RADIUS-mCurrentY*FACTOR);
				// canvas.drawBitmap(screenbitmap, matrix, paint);

				canvas.restore();
				canvas.save();
				// ���Ŵ�
//   				canvas.translate(viewleft, mTopViewY);
//				canvas.translate(viewleft, mTopViewY);
// 	 			canvas.drawBitmap(zoombitmap, 0, 0, paint);
  				
   				canvas.drawRect(viewleft, mTopViewY, viewleft+XRADIUS * 2, mTopViewY+YRADIUS * 2, mFramePaint);
				 
			 	

   				if(circleresource != null)
   					canvas.drawBitmap(circleresource, viewleft+XRADIUS-circleresource.getWidth()/2,
   							mTopViewY+YRADIUS-circleresource.getHeight()/2, null);
   				
   			//	canvas.drawCircle(viewleft+XRADIUS, mTopViewY+YRADIUS, 40, mGreenCirclePaint);
				canvas.restore();

				 

			 

			}

		} catch (NullPointerException e) {
			LogUtil.logE("SCALE", "canvas null");
		}

		long t2 = System.currentTimeMillis();
		// Log.e("SCALE", "Cost "+(t2-t));

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public static interface OnSelectPointListenter{
		public void onSelectBegin(float x, float y, int radius);
		public void onSelectEnd(float x, float y, int radius);
	}
	
	private OnSelectPointListenter mselectpointListener = null;
	public void setSelectPointEndListener(OnSelectPointListenter l){
		mselectpointListener = l;
	}
	
	 

 
	 
	public boolean DispachTouchEvent(MotionEvent e1) {
		if(e1.getPointerCount()>1) {
			if(bDisplayZoom) {
				bDisplayZoom = false;
				invalidate();
			}
			return false;
		}
		currentevent = e1;
		
		LogUtil.logV("facepoint", "DispachTouchEvent mCurrentY:"+currentevent.getY());
		
        int status = e1.getAction();
		switch (status) {
		case MotionEvent.ACTION_DOWN:
			if (true) {
		 
				mTopViewIndex = 0;
				if(screenbitmap != null && screenbitmap.isRecycled() == false)
				{
					screenbitmap.recycle();
					screenbitmap = null;
				}
				
			 	screenbitmap = convertViewToBitmap(dispView);
			 	if(screenbitmap==null) break;

				bDisplayZoom = true;
			 	  mCurrentX = (int) (currentevent.getX() );
				  mCurrentY = (int) (currentevent.getY()); 
				  
				if(mselectpointListener != null)
					mselectpointListener.onSelectBegin(e1.getX()- mStartX, e1.getY()- mStartY,(int) ((circleresource.getWidth()/2)/FACTOR));
				 invalidate();
				 return true;
			}
		case MotionEvent.ACTION_MOVE:
			 mCurrentX = (int) (currentevent.getX() );
			  mCurrentY = (int) (currentevent.getY()); 
			  
			    invalidate();
				return true;
		case MotionEvent.ACTION_UP:
			if(!bDisplayZoom) break;
			LogUtil.logE("scale", "LEAVE_LONGPRESS");
			bDisplayZoom = false;
			 mCurrentX = (int) (currentevent.getX() );
			  mCurrentY = (int) (currentevent.getY()); 
			  
			 invalidate();
			
			if(mselectpointListener != null)
				mselectpointListener.onSelectEnd(e1.getX()- mStartX, e1.getY()- mStartY,(int) ((circleresource.getWidth()/2)/FACTOR));
			
			 return true;
		}
		
		return false;

	}

	
	public void FinishGetImg() {
		// TODO Auto-generated method stub
		Canvas canvas;
	
	 
		bDisplayZoom = true;
		 invalidate();
	

	}
	
//	public int SetRadiusIndex(int index)
//	{
//        this.radiusIndex = index;
//        return radiusgroup[index];
//	}
	
	public void setCircleResource(Bitmap circle) {
		circleresource = circle;
	}
	
	
	public static Bitmap convertViewToBitmap(View view){
		int w = view.getWidth();
		int h = view.getHeight();
		if(w<1||h<1) return null;
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        view.draw(new Canvas(bitmap));

	   return bitmap;
	}

	public boolean isViewDisplay() {
		return bDisplayZoom;
	}
	
}
