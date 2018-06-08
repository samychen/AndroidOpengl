package com.cam001.widget.slider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

import com.cam001.photoeditor.R;
import com.cam001.widget.timer.ITimeListener;
import com.cam001.widget.timer.TimeSchedlue;

public class cheelview extends View implements OnTouchListener, ITimeListener {
	
	final int _unitnum = 16;
	final int _refreshtime = 20;
	
	cheelunit[] unitlist;
	
 
	Context mContext;
	
	Slider slider;
	Sprite[] spritelist;
	
	TimeSchedlue timer;
	
	 
	 
	
	Paint mPaint;
	boolean bDoing;
	Bitmap centerfocusebitmap;
	
	Bitmap mSanjiaoBitmap;
	float angleSanjiao = 0.0f;
	int positionx = 0;
	int positiony = 0;
	int yuanRadius = 0;
	boolean mIsShowSamjiao = false;
	
	Rect centerrect= new Rect();
	Rect mSanjiaoRect = new Rect();
	Handler mHandler;
	 
	//Gesture
	private int mTouchSlopSquare, touchSlop, doubleTapSlop, mMinimumFlingVelocity, mMaximumFlingVelocity;


	public cheelview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	//	crossbitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cross);
		
		unitlist = new cheelunit[_unitnum];
		spritelist = new Sprite[_unitnum];
		
		
		
		
		timer = new TimeSchedlue();
		timer.addListener(this);
	 
		this.setLongClickable(true);
		this.setOnTouchListener(this);
		mContext = context;
		
		final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        doubleTapSlop = configuration.getScaledDoubleTapSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        
        mTouchSlopSquare = touchSlop * touchSlop;
        
      
      
        mPaint = new Paint();
    	mPaint.setAntiAlias(true);
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeWidth(10);
    	mPaint.setColor(0xFF888888);
    	
    	centerfocusebitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shutglory);
    	mSanjiaoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sanjiao);
    	
    	bDoing = false;
        
	}
	
	
	public cheelview(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	//	crossbitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cross);
		
		unitlist = new cheelunit[_unitnum];
		spritelist = new Sprite[_unitnum];
		
		
		
		
		timer = new TimeSchedlue();
		timer.addListener(this);
	 
		this.setLongClickable(true);
		this.setOnTouchListener(this);
		mContext = context;
		
		final ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        doubleTapSlop = configuration.getScaledDoubleTapSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        
        mTouchSlopSquare = touchSlop * touchSlop;
        
      
       
        mPaint = new Paint();
    	mPaint.setAntiAlias(true);
    	mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeWidth(10);
    	mPaint.setColor(0xFF888888);
    	
    	centerfocusebitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shutglory);
    	mSanjiaoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sanjiao);
    	
    	bDoing = false;          
	}
	
 
	public void setHandler(Handler h) {
		mHandler = h;
	}
	
	public void installcheelunit(Bitmap[] sliderbitmap){
		if(sliderbitmap[0]!=null){
			yuanRadius = sliderbitmap[0].getWidth()/2;
		}
		for(int i=0; i<unitlist.length; i++)
		{
			unitlist[i] = new cheelunit();
	//		unitlist[i].setBitmap(crossbitmap,""+i);
			Bitmap normalbitmap = null;
			Bitmap focusbitmap = null;
			
			normalbitmap = sliderbitmap[2*i];
			focusbitmap = sliderbitmap[2*i+1];
			
			unitlist[i].setBitmap(normalbitmap,focusbitmap,""+i);
			unitlist[i].setDeminsion(normalbitmap.getWidth(),  normalbitmap.getHeight(), focusbitmap.getWidth(),
					focusbitmap.getHeight());
			
	//		unit.setCenter(100, 500);
			spritelist[i] = unitlist[i].getSprite();
		}
		
		 
		
		slider = new Slider();
		slider.setSpeed(14);
	 	
		slider.init(spritelist, 0);
//		slider.init(spritelist, AppConfig.getInstance().cameraMode);
		
		mIsShowSamjiao = true;
	}
	
	
	
	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		
		slider.setChord(w*83/100);
		
		slider.setField(3.1415926);
		slider.setSpace(3.1415926/8);
		slider.setHeight(h);
		
		slider.setOffsetX(w/2);
	
//		slider.setHeight(600);
//		Point pt = slider.getCenter();
//		pt.x=pt.x;
			
		for(int i=0; i<unitlist.length; i++)
		{			 
			unitlist[i].setCenter(unitlist[i].getSprite().getPosition());
//			if(i == AppConfig.getInstance().cameraMode){
			if(i == -1){
				unitlist[i].setFocuse(true);
				//��������Ȧ��λ��
				
				centerrect.left = unitlist[i].getSprite().getPosition().x- centerfocusebitmap.getWidth()/2;
				centerrect.right = unitlist[i].getSprite().getPosition().x+centerfocusebitmap.getWidth()/2;
				centerrect.top = unitlist[i].getSprite().getPosition().y - centerfocusebitmap.getHeight()/2;
				centerrect.bottom = unitlist[i].getSprite().getPosition().y + centerfocusebitmap.getHeight()/2;
			}
		}
		
		
		super.onSizeChanged(w, h, oldw, oldh);  
	}





	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
	//	canvas.drawArc(arcrect, 0, 360, false, mPaint);
		
		for(int i=0; i<unitlist.length; i++)
		{			 
			unitlist[i].Draw(canvas);
		}
	 
//		canvas.drawBitmap(centerfocusebitmap, null, centerrect, null);
		if(mIsShowSamjiao){
			double angle = slider.getSprite(0).getAngle();	
			angleSanjiao = (float)(angle*180/3.1415926);
			positionx = unitlist[0].getSprite().getPosition().x;
			positiony = unitlist[0].getSprite().getPosition().y;
			mIsShowSamjiao = false;
		}
		
		if(positionx != 0){
			Matrix matrix = new Matrix();
			matrix.postScale(1f, 1f);	
			matrix.postRotate(angleSanjiao,mSanjiaoBitmap.getWidth()/2,mSanjiaoBitmap.getHeight()/2); 
			float radiusx = (float)((yuanRadius+mSanjiaoBitmap.getWidth()/2)*Math.cos(angleSanjiao*3.1415926/180));
			float radiusy = (float)((yuanRadius+mSanjiaoBitmap.getWidth()/2)*Math.sin(angleSanjiao*3.1415926/180));
			matrix.postTranslate((positionx-mSanjiaoBitmap.getWidth()/2)-radiusy, 
					(positiony-mSanjiaoBitmap.getHeight()/2)+radiusx);
			canvas.drawBitmap(mSanjiaoBitmap, matrix, null);
		}	
		super.onDraw(canvas);
	}

	 
	private clicksliderListener mClickListener = null;
	public void setClicksliderListener(clicksliderListener l){
		mClickListener = l;
	}
	public interface clicksliderListener{
		public void onclickslider(int iIndex, boolean bSingleTap);
	}
	
	
	private singletapListener mtapListener = null;
	public void setSingletapListener(singletapListener l){
		mtapListener = l;
	}
	public interface singletapListener{
		public void onsingleclick(int iIndex);
	}
	
	
	 

	float oldPointX;
	float oldPointY;
	
	int selectedIdx = 0;
	
	private boolean JudgePointEqual(float srcX, float srcY, float dstX, float dstY)
	{
		final int deltaX = (int) (dstX - srcX);
        final int deltaY = (int) (dstY - srcY);
        int distance = (deltaX * deltaX) + (deltaY * deltaY);
	
         if (distance > mTouchSlopSquare) 
                return false;
         else
            	return true;

 	
		 
	}
	
	boolean bScroll;
	boolean bSingleTap = false;
  
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		//return mGestureDetector.onTouchEvent(event);
		
//		Log.d("cheelview", "touch event.getPointerCount():"+event.getPointerCount());
		
		if(bDoing)
			return false;
		
		if(event.getPointerCount() > 1)
			return false;
				
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
//			LogUtil.logV("cheelview", "touch ACTION_DOWN");		
			oldPointX = event.getX();
			oldPointY = event.getY();
			bScroll = false;
			bSingleTap = false;
			bDiable  = false;
			bFirstDisplay = false;
			for(int i=0; i<unitlist.length; i++){
				if(unitlist[i].JudgePoint((int)event.getX(), (int)event.getY()) == true){
					unitlist[i].setFocuse(true);
					 
					for(int j=0; j<unitlist.length; j++)
					{
						if(i != j)
							unitlist[j].setFocuse(false);
					}
				}
			 
			}
			invalidate();
		 
			
		} else if(event.getAction() == MotionEvent.ACTION_UP && JudgePointEqual(oldPointX, oldPointY, event.getX(), event.getY()) )
		{
			 
//			LogUtil.logV("cheelview", "touch single tap");			
			//single tap
			int i= 0;
			for(i=0; i<unitlist.length; i++)
			{
				if(unitlist[i].JudgeSelect() ){
					 
					//if(i != slider.getActive() )
					 
//						slider.onSTap(unitlist[i].getSprite(), new Point((int)event.getX(), (int)event.getY()));
//						slider.refresh();
//						bDoing = true;
						bSingleTap = true;
						if(mtapListener!=null)
						{
							mtapListener.onsingleclick(i);
							double angle = slider.getSprite(i).getAngle();
							
							angleSanjiao = (float)(angle*180/3.1415926);
							positionx = unitlist[i].getSprite().getPosition().x;
							positiony = unitlist[i].getSprite().getPosition().y;
							this.invalidate();				
							
							selectedIdx = i;
						}
//						timer.startTimer(_refreshtime);
						break;				 
				}
			}
			
			if(i == unitlist.length)
			{
				if(mClickListener != null)
				{
					mClickListener.onclickslider(slider.getActive(), true);
				}
			}
			
		}else if(event.getAction() == MotionEvent.ACTION_MOVE && !bScroll && !JudgePointEqual(oldPointX, oldPointY, event.getX(), event.getY()) )
		{
//		    //��ʼ���� 
//			bScroll = true;	
//		 	
////			for(int i=0; i<unitlist.length; i++)
////			{
////				if(unitlist[i].JudgeSelect())
////				{
////					 
////					slider.onDown(unitlist[i].getSprite(), new Point((int)event.getX(), (int)event.getY()));
////					 
////				}
////			}
//			
////			
////			for(int i=0; i<unitlist.length; i++)
////			{
////				unitlist[i].JudgeSelect();				
////			}
//			
//			 
//			slider.onDown(unitlist[slider.getActive()].getSprite(), new Point((int)event.getX(), 
//					(int)event.getY()));
//			
		}else if(event.getAction() == MotionEvent.ACTION_MOVE && bScroll)
		{
//			if(mlShow!=null) {
//				mlShow.onScreen();
//			}
//			
////			for(int i=0; i<unitlist.length; i++)
////			{
////				if(unitlist[i].JudgeSelect())
////				{
////					
////					slider.onMove(unitlist[i].getSprite(), new Point((int)event.getX(), (int)event.getY()));
////					for(int j=0; j<unitlist.length; j++)
////					{			 
////						unitlist[j].setCenter(unitlist[j].getSprite().getPosition());
////					}
////				     invalidate();
////				}
////			}
//		 
//			//������ￄ1�7�1�7
//			slider.onMove(unitlist[slider.getActive()].getSprite(), new Point((int)event.getX(), 
//					(int)event.getY()));
//			
//			for(int j=0; j<unitlist.length; j++)
// 				{			 
// 					unitlist[j].setCenter(unitlist[j].getSprite().getPosition());
// 				}
// 			     invalidate();
//			
		}else if(event.getAction() == MotionEvent.ACTION_UP && bScroll)
		{
			 
//			bScroll = false;
////			for(int i=0; i<unitlist.length; i++)
////			{
////				if(unitlist[i].JudgeSelect())
////				{
////					slider.onUpup(unitlist[i].getSprite(), new Point((int)event.getX(), (int)event.getY()));
////					slider.refresh();
////					bDoing = true;
////					timer.startTimer(_refreshtime);
////				}
////			}
//			
//			 
//			//������ￄ1�7�1�7
//			slider.onUpup(unitlist[slider.getActive()].getSprite(), new Point((int)event.getX(), 
//					(int)event.getY()));
// 			slider.refresh();
//// 			bDoing = true;
// 			timer.startTimer(_refreshtime);
//			
//		
		}else if(event.getAction() == MotionEvent.ACTION_UP)
		{
		 
//			for(int i=0; i<unitlist.length; i++)
//			{			 
//				if(i != slider.getActive())
//					unitlist[i].setFocuse(false);
//				else
//					unitlist[i].setFocuse(true);
//			}
//			postInvalidate();
			
		 
		}
		return false;
	}

	boolean bDiable = true; //�ж�view�Ƿ���ʾor����
	public boolean isbDiable()
	{
		return bDiable;
	}

	boolean bFirstDisplay = false; // �ж�view�ǲ��ǵ�һ�α�����
	
	@Override
	public void onTime(long startTime) {
		// TODO Auto-generated method stub
		if(slider.getState() == SliderState.PXE_SLIDER_STATE_AUTO)
		{
			// ����������Ĺ��
			slider.refresh();
			
			if(bFirstDisplay == true && this.getVisibility() != View.VISIBLE)
			{
//				mHandler.post(new Runnable(){
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						setVisibility(View.VISIBLE);
//					}
//					
//				});
				
				
			}
			
			
			for(int i=0; i<unitlist.length; i++)
			{			 
				unitlist[i].setCenter(unitlist[i].getSprite().getPosition());
				if(bFirstDisplay == true || bDiable == true) //ֻ�е������ջص�ʱ����Ҫ���¼�����ￄ1�7�1�7
				{
					if(i == slider.getActive())
				{
					centerrect.left = unitlist[i].getSprite().getPosition().x- centerfocusebitmap.getWidth()/2;
					centerrect.right = unitlist[i].getSprite().getPosition().x+centerfocusebitmap.getWidth()/2;
					centerrect.top = unitlist[i].getSprite().getPosition().y - centerfocusebitmap.getHeight()/2;
					centerrect.bottom = unitlist[i].getSprite().getPosition().y + centerfocusebitmap.getHeight()/2;
				}
				}
				
			}					
			double angle = slider.getSprite(selectedIdx).getAngle();
			
			angleSanjiao = (float)(angle*180/3.1415926);
			positionx = unitlist[selectedIdx].getSprite().getPosition().x;
			positiony = unitlist[selectedIdx].getSprite().getPosition().y;
			//this.invalidate();
			postInvalidate();
			
			 
		}else
		{
			if(bDiable == false)
			{
				  //����������Ǹￄ1�7�1�7 
				for(int i=0; i<unitlist.length; i++)
				{			 
					if(i != slider.getActive())
						unitlist[i].setFocuse(false);
					else{
						unitlist[i].setFocuse(true);
						centerrect.left = unitlist[i].getSprite().getPosition().x- centerfocusebitmap.getWidth()/2;
						centerrect.right = unitlist[i].getSprite().getPosition().x+centerfocusebitmap.getWidth()/2;
						centerrect.top = unitlist[i].getSprite().getPosition().y - centerfocusebitmap.getHeight()/2;
						centerrect.bottom = unitlist[i].getSprite().getPosition().y + centerfocusebitmap.getHeight()/2;
						
					}
				}
				postInvalidate();
				
				if(bFirstDisplay == false)
				{
					if(mClickListener != null)
					{
						mClickListener.onclickslider(slider.getActive(), bSingleTap);
					}
				}else
					bFirstDisplay = false;
				
				
				timer.stopTimer();
				
			}else{
				timer.stopTimer();
//				mHandler.post(new Runnable(){
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						cheelview.this.setVisibility(View.INVISIBLE);
//					}
//					
//				});
				
			}
		 
			bDoing = false;
		 	
			
		}
	}
	
	public int GetActiveIndex()
	{
		return slider.getActive();
		
	}
	
	public void SetActiveIndex(int index)
	{
		if(index > 0 && index <= _unitnum-1)
		{
//			LogUtil.logV("cheerview", "SetActiveIndex"+index);
			slider.setActive(index);
			slider.refresh();
			
			for(int i=0; i<unitlist.length; i++)
			{			 
				if(i != slider.getActive())
					unitlist[i].setFocuse(false);
				else
					unitlist[i].setFocuse(true);
			}
			
		}
		
	}
 
	public void show(boolean bShow) {
		if(bShow) {
			
			slider.onEject();
			slider.refresh();
			
			bDiable = false;
			bFirstDisplay = true;
		} else {
			
			 
			slider.onClose();
			slider.refresh();
			bDiable = true;
		}
		
		bDoing = true;
		timer.startTimer(_refreshtime);
		if(mlShow!=null) {
			mlShow.onShow(bShow);
		}
	}
	
	public boolean getDoing()
	{
		return bDoing;
	}

	private OnShowListener mlShow = null;
	
	public void setOnShowListener(OnShowListener l) {
		mlShow = l;
	}
	
	public static interface OnShowListener {
		void onShow(boolean bShow);
		void onScreen();
	}
	
	public void rotate180(){
		int nActive = slider.getActive();
		slider.onSTap(unitlist[(nActive + 8) % 16].getSprite(), new Point(0,0));
		slider.refresh();
//		bDoing = true;
		bSingleTap = true;
		//if(mtapListener!=null)
		//{
		//	mtapListener.onsingleclick(i);
		//}
		timer.startTimer(_refreshtime);
		//break;
	}
	
	public int getActive() {
		return slider.getActive();
	}
}
