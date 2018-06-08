package com.cam001.widget.slider;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class cheelunit {
	
	private boolean bFocuse;
	private Bitmap normalBitmap, focusBitmap;
	private Rect drawRect, focusdrawRect;
	private Point centerPoint;
	private int dimX, dimY, focuseimX, focuseimY;
	private boolean bSelect = false;
	private Sprite sprite;
	Paint textpaint;

	public cheelunit() {
		super();
		// TODO Auto-generated constructor stub		
		drawRect = new Rect();
		focusdrawRect = new Rect();
		centerPoint = new Point();
		
		dimX = 60;  //default value;
		dimY = 60;
		
		
		sprite = new Sprite();
		
		textpaint = new Paint();
		textpaint.setColor(Color.BLUE);
		textpaint.setTextSize(50);
		
		bFocuse = false;
		
	}
	
	public void setBitmap(Bitmap normalbitmap, Bitmap focusbitmap, String st)
	{
				 
		normalBitmap = normalbitmap;
		focusBitmap = focusbitmap;
	}
	
	 
	
	
	public void setDeminsion(int x, int y, int focusx, int focusy)
	{
		dimX = x;
		dimY = y;
		
		focuseimX = focusx;
		focuseimY = focusy;
	}
	
	 
	
	
	public void setCenter(Point point)
	{
		
		centerPoint.x = point.x;
		centerPoint.y = point.y;
		drawRect.left = centerPoint.x- dimX/2;
		drawRect.right = centerPoint.x+dimX/2;
		drawRect.top = centerPoint.y - dimY/2;
		drawRect.bottom = centerPoint.y + dimY/2;
		
		focusdrawRect.left = centerPoint.x- focuseimX/2;
		focusdrawRect.right = centerPoint.x+focuseimX/2;
		focusdrawRect.top = centerPoint.y - focuseimY/2;
		focusdrawRect.bottom = centerPoint.y + focuseimY/2;
		
		
 
	}
	
	
	public void Draw(Canvas canvas)
	{
		if(bFocuse)
		   canvas.drawBitmap(focusBitmap, null, focusdrawRect, null); 
		else
		   canvas.drawBitmap(normalBitmap, null, drawRect, null);
	} 
	
	public void setFocuse(boolean focuse)
	{
		bFocuse = focuse;
	}
	
	public boolean JudgePoint(int x, int y)
	{
		if(bFocuse)
		  bSelect =  focusdrawRect.contains(x, y);
		else
		  bSelect =  drawRect.contains(x, y);
		 
		return bSelect;
	}
	
	public boolean JudgeSelect()
	{
		return bSelect;
	}
	 
	
	public Sprite getSprite()
	{
		return sprite;
	}

}
