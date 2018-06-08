package com.cam001.photoeditor.beauty.makeup.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class RectView extends View {

	 Rect[] Rectlist ;
	 Point oldPoint;
     Point[] Pointlist;
	 int RectNum;
	 int nSelectIndex;
	private Paint redPaint, bluePaint;
	
	final int MAX_RECT= 1;
	final int RADIUS = 10;
 
	
	public RectView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}


	public RectView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	public RectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		RectNum = 0;
		Rectlist = new Rect[MAX_RECT];
		oldPoint = new Point();
		
		Pointlist = new Point[MAX_RECT*3];
		nSelectIndex = -1;
		ResetSelect();
//		Rectlist[0] = new Rect();
//		Rectlist[0].left = 10;
//		Rectlist[0].right = 100;
//		Rectlist[0].top = 20;
//		Rectlist[0].bottom = 200;
//		
    	redPaint = new Paint();
		redPaint.setColor(Color.RED);
		redPaint.setStyle(Paint.Style.STROKE);
		redPaint.setStrokeWidth(2);
		
		bluePaint =  new Paint();
		bluePaint.setColor(Color.BLUE);
		bluePaint.setStyle(Paint.Style.STROKE);
		bluePaint.setStrokeWidth(2);
	}

	private void ResetSelect()
	{
		nSelectIndex = -1;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		if(RectNum > 0)
		{
			int i=0;
			for(i=0; i<RectNum; i++){				 
				  canvas.drawRect(Rectlist[i], bluePaint);	
			}
			
//			if(MakeupApp.bDebug){
//				for(i=0; i<RectNum*3; i++)
//				{	 			
//					if(i == nSelectIndex)
//					   canvas.drawCircle(Pointlist[i].x, Pointlist[i].y,
//							   RADIUS, redPaint) ;
//					else
//					canvas.drawCircle(Pointlist[i].x, Pointlist[i].y,
//							RADIUS, bluePaint) ;					
//				
//				}
//			}
			
			 
		}
		super.onDraw(canvas);
	}
	
	
	public void SetRect(Rect[] rect, Point[] point1, Point[] point2, int num)
	{
		
		num = Math.min(num, MAX_RECT);
		RectNum = num;
 	
		for(int i=0; i<RectNum; i++)
		{
			Rectlist[i] = rect[i];
			Pointlist[3*i] = point1[2*i];
			Pointlist[3*i+1] = point1[2*i+1];
			Pointlist[3*i+2] = point2[i];
		}
		this.invalidate();
	}
	
	public void SetSelectPoint(int x, int y)
	{
		 
		if(x == -1 || y == -1)
		{
			ResetSelect();
		}else
		{
			for(int i=0; i<RectNum*3; i++)
			{
				if(Math.abs(Pointlist[i].x -x) < RADIUS && Math.abs(Pointlist[i].y -y) < RADIUS)
				{
					nSelectIndex = i; 
					
					oldPoint.x = Pointlist[i].x;
					oldPoint.y = Pointlist[i].y;
					
					break;
				}
			}
		}
		
		
		this.invalidate();
	}
	
	public void MovePoint(int xOffset, int yOffset)
	{
		if(nSelectIndex != -1)
		{
			Pointlist[nSelectIndex].x = oldPoint.x + xOffset;
			Pointlist[nSelectIndex].y = oldPoint.y + yOffset;
			 
			this.invalidate();
		}
		
	}
	
	public Rect[] getRects() {
		return Rectlist;
	}
	
	

}
