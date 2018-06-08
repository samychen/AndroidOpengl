/**
 * 
 */
package com.cam001.widget.slider;

import android.graphics.Point;

/**
 * @author DeepBlue
 *
 */
public class Sprite
{

	/**
	 * 
	 */
	protected Point _position = new Point(-1, -1);
	protected double _angle = 0.0;
	protected boolean _visible = false;
	
	public Point getPosition()
	{
		return _position;
	}
	
	public double getAngle()
	{
		return _angle;
	}
	
	public double setAngle(double angle)
	{
		_angle = angle;
		return _angle;
	}
	
	public boolean isVisible()
	{
		return _visible;
	}
	
	public boolean setVisible(boolean visible)
	{
		_visible = visible;
		return _visible;
	}
	
	public Sprite()
	{
		// TODO Auto-generated constructor stub
	}

}
