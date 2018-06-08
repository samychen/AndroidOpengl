/**
 * 
 */
//package PhXEngine;
package com.cam001.widget.slider;

import java.util.ArrayList;
import java.util.List;

//import java.awt.Point;
import android.R.integer;
import android.graphics.Point;

/**
 * @author DeepBlue
 * 
 */
public class Slider
{

	/**
	 * 
	 */
	/**
	 * 1. 锟节诧拷统一锟斤拷锟姐，锟斤拷锟解部锟睫癸拷
	 * 2. 锟解部锟借定锟斤拷示锟斤拷锟斤拷目锟饺ｏ拷锟斤拷取位锟斤拷锟斤拷息时锟斤拷锟斤拷锟斤拷映锟斤拄1�7
	 * 3. 锟斤拷锟斤拷锟斤拷锟解部指锟斤拷
	 * 4. 锟斤拷Gesture锟斤拷锟绞憋拷锟阶刺拷锟轿狝UTO锟斤拷锟皆讹拷锟斤拷锟斤拷�1�7�锟斤拷锟絖cursor
	 * 5. (a)锟斤拷AUTO时锟斤拷锟斤拷锟铰碉拷Gesture锟斤拷锟斤拷锟斤拷锟斤拷停止锟斤拷锟斤拷始锟斤拷应Gesture
	 *    (b)锟斤拷AUTO时锟斤拷直锟斤拷AUTO全锟斤拷锟斤拷桑锟斤拷趴锟绞硷拷锟接esture
	 *    (c)锟斤拷锟斤拷(a)锟斤拄1�7(b)锟斤拷�1�7�一
	 * 6. AUTO时锟斤拷时锟戒函锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷瓒拷锟街灰拷锟斤拷锟斤拷锟斤拷锟斤拷锟酵Ｖ癸拷锟斤拷锟ￄ1�7
	 */
	protected SliderState _state = SliderState.PXE_SLIDER_STATE_NONE;
	protected List<Sprite> _sprites = new ArrayList<Sprite>();
	protected List<Double> _distarr = new ArrayList<Double>();
	protected double _cursor = 0.0;
	protected double _crprev = 0.0;
	protected double _crnext = 0.0;
	
	protected final double DEFAULT_FIELD = 100.0 * Math.PI / 180.0;
//	protected final double DEFAULT_FIELD = 100.0 * Math.PI / 180.0;
	protected final double DEFAULT_SPACE = 23.0 * Math.PI / 180.0;
	protected final double DEFAULT_CHORD = 720.0;
//	protected final double DEFAULT_CHORD = 720.0;
	protected final double DEFAULT_SHIFT = 0.0;
	protected final double DEFAULT_HEIGHT = 1080.0;
	protected double _field = DEFAULT_FIELD;	// all visiable angle range
	protected double _space = DEFAULT_SPACE;	// angle of two sprite
	protected double _chord = DEFAULT_CHORD;
	protected double _shift = DEFAULT_SHIFT;
	protected double _height = DEFAULT_HEIGHT;
	//chord = R * sin(field / 2.0) * 2.0
	//x = chord * 0.5 * (1.0 + sin(delta) / sin(field /2.0)
	//y = chord * 0.5 * (cos(delta) - cos(field / 2.0)) / sin(field / 2.0)
	
	protected Point _lastpt = new Point(-1, -1);
	protected Sprite _target = null;
	
	protected final double DEFAULT_SPEED = 2.0;//D/S
	protected double _speed = DEFAULT_SPEED;
	
	protected long _timebeg = 0;
	protected long _timenow = 0;
	
	protected final int SWITCH_DONNE = 0;
	protected final int SWITCH_EJECT = 1;
	protected final int SWITCH_CLOSE = 2;
	protected final int DEFAULT_DT_EJECT = 500;
	protected final int DEFAULT_DT_EJECT_DELAY = 40;
	protected final double DEFAULT_EJECT_OVER = 10.0;
	
	protected final int DEFAULT_DT_CLOSE = 500;
	protected final double DEFAULT_RSLOPE = 1.0;	// rate of slope
	
	protected final double DEFAULT_RAD_END = 50.0;
	
	protected int _switch = SWITCH_DONNE;
	protected double _radius = 0.0;
	//protected double _radcur = 0.0;
	protected double _radend = DEFAULT_RAD_END;
	
	protected double _rslope = DEFAULT_RSLOPE;
	protected double _centx = 0.0;
	protected double _centy = 0.0;
	protected double _offsetx = 0.0;
	protected int _dteject = DEFAULT_DT_EJECT;
	protected int _dleject = DEFAULT_DT_EJECT_DELAY;
	protected double _oveject = DEFAULT_EJECT_OVER;
	protected int _dtclose = DEFAULT_DT_CLOSE;
	
	protected int _sndflag = 0;
	//protected final double DEFAULT_SND_THD_

	private OnSlideOverListener mlSlideOver = null;
	
	public static interface OnSlideOverListener {
		void onSlideOver(int position);
	}
	
	public Slider()
	{
		// TODO Auto-generated constructor stub
		_radius = (_chord / 2.0) / Math.sin(_field / 2.0);
		//_radcur = _radius;
		
		getCenter();
	}
	
	public void setOnSlideOverListener(OnSlideOverListener l) {
		mlSlideOver = l;
	}
	
	public void setField(final double field)
	{
		_field = field;
		_radius = (_chord / 2.0) / Math.sin(_field / 2.0);
		//_radcur = _radius;
		
		getCenter();
		calcViewPts();
	}
	
	public void setSpace(final double space)
	{
		_space = space;
		calcViewPts();
	}
	
	public void setChord(final double chord)
	{
		_chord = chord;
		_radius = (_chord / 2.0) / Math.sin(_field / 2.0);
		//_radcur = _radius;
		
		getCenter();
		calcViewPts();
	}
	
	public void setOffsetX(final double offsetX)
	{
		_offsetx = offsetX;
		
		getCenter();
		calcViewPts();
	}
	
	public void setShift(final double shift)
	{
		_shift = shift;
		
		getCenter();
		calcViewPts();
	}
	
	public void setHeight(final double height)
	{
		_height = height;
		
		getCenter();
		calcViewPts();
	}
	
	public void setSpeed(final double speed)
	{
		_speed = speed;
	}
	
	public int getActive()
	{
		int index = (int)(_crnext + 0.5);
		if(index < 0) index = 0;
		if(index > _sprites.size()-1) index = _sprites.size()-1;
		return index;
	}
	
	public SliderState getState()
	{
		return _state;
	}

	public int insert(Sprite sprite)
	{
		_sprites.add(sprite);
		_distarr.add(0.0);
		
		calcDistArr();
		calcViewPts();

		return _sprites.size();
	}

	public int insert(Sprite sprite, final int index)
	{
		if (index >= 0 && index <= _sprites.size())
		{
			_sprites.add(sprite);
			_distarr.add(0.0);
			
			calcDistArr();
			calcViewPts();
			
			return _sprites.size();
		}
		
		return -1;
	}

	public boolean remove(Sprite sprite)
	{
		boolean ret = false;
		ret = _sprites.remove(sprite);
		if (ret)
		{
			_distarr.remove(0);
			
			calcDistArr();
			calcViewPts();
		}
		
		return ret;
	}
	
	public boolean remove(final int index)
	{
		boolean ret = false;
		ret = (null != _sprites.remove(index));
		if (ret)
		{
			_distarr.remove(0);
			
			calcDistArr();
			calcViewPts();
		}
		
		return ret;
	}

	public void init(Sprite[] sprites, final double cursor)
	{
		reset();

		for (Sprite s : sprites)
		{
			_sprites.add(s);
			_distarr.add(0.0);
		}
		
		_cursor = Math.min(Math.max(0.0, cursor), _sprites.size());
		calcDistArr();
		calcViewPts();
	}
	
	public void reset()
	{
		for (int i = _sprites.size() - 1; i >= 0; --i)
		{
			_sprites.remove(i);
			_distarr.remove(i);
		}
		
		_cursor = 0;
	}
	
	public void refresh()
	{
		if (SliderState.PXE_SLIDER_STATE_AUTO == _state)
		{
			if (SWITCH_DONNE == _switch)
			{
				calcIntimeCursor();
				setCursor(_cursor);
			}
			else if (SWITCH_EJECT == _switch)
			{
				calcIntimeRadius();
				setCursor(_cursor);
			}
			else if (SWITCH_CLOSE == _switch)
			{
				calcIntimeRadius();
				setCursor(_cursor);
			}
		}
	}
	
	public void setActive(final double cursor)
	{
		_cursor = Math.min(Math.max(0.0, cursor), _sprites.size());
		_crprev = _cursor;
		_crnext = _cursor;
		calcDistArr();
		calcViewPts();
	}
	
	protected void setCursor(final double cursor)
	{
		_cursor = Math.min(Math.max(0.0, cursor), _sprites.size());
		 
		calcDistArr();
		calcViewPts();
		
		double dlt = _cursor - (int)_cursor;
		if (_sndflag == 0)
		{
			if (dlt < 0.1 || dlt > 0.9)
			{
				_sndflag = 1;
				
				if (SliderState.PXE_SLIDER_STATE_NONE == _state)
				{
					// stop
					if(mlSlideOver!=null) mlSlideOver.onSlideOver(0);
				}
				else
				{
					// cross
					if(mlSlideOver!=null) mlSlideOver.onSlideOver(1);
				}
			}
		}
		else if (_sndflag == 1)
		{
			if (dlt > 0.2 || dlt < 0.8)
				_sndflag = 0;
		}
	}
	
	public Sprite getSprite(final int index)
	{
		if (index >= 0 && index < _sprites.size())
		{
			return _sprites.get(index);
		}
		else
		{
			return null;
		}
	}
	
	public void onEject()
	{
		_switch = SWITCH_EJECT;
		_state = SliderState.PXE_SLIDER_STATE_AUTO;
		_crprev = _cursor;
		_crnext = _cursor;
		doEject();
	}
	
	public void onClose()
	{
		_switch = SWITCH_CLOSE;
		_state = SliderState.PXE_SLIDER_STATE_AUTO;
		_crprev = _cursor;
		_crnext = _cursor;
		doClose();
	}
	
	public void onDown(Sprite sprite, Point point)
	{
		_state = SliderState.PXE_SLIDER_STATE_DOWN;
		doDown(sprite, point);
	}

	public void onMove(Sprite sprite, Point point)
	{
		_state = SliderState.PXE_SLIDER_STATE_MOVE;
		doMove(sprite, point);
	}

	public void onUpup(Sprite sprite, Point point)
	{
		_state = SliderState.PXE_SLIDER_STATE_UPUP;
		//goto auto mode
		_state = SliderState.PXE_SLIDER_STATE_AUTO;
		doUpup(sprite, point);
	}

	public void onSTap(Sprite sprite, Point point)
	{
		_state = SliderState.PXE_SLIDER_STATE_STAP;
		//goto auto mode
		_state = SliderState.PXE_SLIDER_STATE_AUTO;
		doSTap(sprite, point);
	}
	
	protected void doEject()
	{
		_timebeg = System.currentTimeMillis();
	}
	
	protected void doClose()
	{
		_timebeg = System.currentTimeMillis();
	}

	protected void doDown(Sprite sprite, Point point)
	{
		_lastpt.x = point.x;
		_lastpt.y = point.y;
		_target = sprite;
	}

	protected void doMove(Sprite sprite, Point point)
	{
		double dx = calcDeltaCursor(_lastpt, point);
		_cursor += dx;
		
		if (_cursor < 0)
		{
			_cursor += _sprites.size();
		}
		else if (_cursor > _sprites.size())
		{
			_cursor -= _sprites.size();
		}

		
		_lastpt.x = point.x;
		_lastpt.y = point.y;
		_target = sprite;
		
		setCursor(_cursor);
	}

	protected void doUpup(Sprite sprite, Point point)
	{
		double dx = calcDeltaCursor(_lastpt, point);
		_cursor += dx;
		
		setCursor(_cursor);
		
		_timebeg = System.currentTimeMillis();
		_lastpt.x = point.x;
		_lastpt.y = point.y;
		_target = sprite;
		
		calcTargetCursor(SliderState.PXE_SLIDER_STATE_UPUP);
	}

	protected void doSTap(Sprite sprite, Point point)
	{
		_timebeg = System.currentTimeMillis();
		_lastpt.x = point.x;
		_lastpt.y = point.y;
		_target = sprite;
		
		calcTargetCursor(SliderState.PXE_SLIDER_STATE_STAP);
	}
	
	//要锟斤拷锟絊Tap锟斤拷Upup锟斤拷目锟斤拷位锟斤拷锟角诧拷一锟斤拷锟ￄ1�7>锟窖斤拷锟�
	protected void calcTargetCursor(final SliderState state)
	{
		if (_sprites.size() < 1)
			return;
		
		if (SliderState.PXE_SLIDER_STATE_STAP == state)
		{
			_crprev = _cursor;
			
			int idx = indexOf(_target);
			_crnext = idx;
		}
		else if (SliderState.PXE_SLIDER_STATE_UPUP == state)
		{
			_crprev = _cursor;
			
			int idx = 0;
			double dist = Math.abs(_distarr.get(idx));
			int size = _sprites.size();
			for (int i = 1; i < size; ++i)
			{
				if (dist > Math.abs(_distarr.get(i)))
				{
					idx = i;
					dist = Math.abs(_distarr.get(i));
				}
			}
			_crnext = idx;
		}
	}
	
	protected void calcIntimeCursor()
	{
		_timenow = System.currentTimeMillis();
		long dt = _timenow - _timebeg;
		
		_cursor = calcCursorFormula(_crprev, _crnext, dt);
	}
	
	//锟斤拷尾锟斤拷锟斤拷锟斤拷锟解还没锟叫匡拷锟斤拷==>锟窖斤拷锟�
	protected double calcCursorFormula(double prev, double next, long dt)
	{
		double curr = prev;
		double size = _sprites.size();
		double half = size / 2.0;
		double dist = Math.abs(next - prev);
		int sign = -1;
		if (next < prev)
		{
			//sign = -1;
		}
		if (dist > half)
		{
			sign *= -1;
			dist = size - dist;
		}
		double lgth = sign * _speed * dt / 1000.0;
		if (Math.abs(lgth) > dist)
		{
			curr = next;
			_state = SliderState.PXE_SLIDER_STATE_NONE;
		}
		else
		{
			curr = prev + lgth;
			if (curr < 0)
			{
				curr += size;
			}
			else if (curr > size)
			{
				curr -= size;
			}
		}
		return curr;
	}
	
	protected void calcIntimeRadius()
	{
		_timenow = System.currentTimeMillis();
	//	 long dt = _timenow - _timebeg;
	//	 Log.e("sllider", "dt="+dt);  
		
		//_radcur = calcRadiusFormula(dt);
	}
	
	protected double calcRadiusFormula(long dt)
	{
		double radcur = 0.0;
		if (SWITCH_EJECT == _switch)
		{
			// eject :
			// y + over = k * (x - a)^2
			// a = Math.sqrt(_radend + over / k)
			// (x - a)^2 + (y + over / 2)^2 = over * over / 2
			// (x - a - over)^2 + (y + over / 2)^2 = over * over / 2
			// speed = (a + over) / _dteject
			
			
			// y + over = k * (x - a)^2
			// a = Math.sqrt(_radend + over / k)
			// (x - a)^2 + (y - 0.414R)^2 = R^2 = over * over / 0.343396
			// (x - 0.586)^2 + (y + R)^2 = R^2 = over * over / 0.343396
			// R = over / 0.586
			
			//int cnt = (int)(_field * 1.5 / _space);
			double k = _rslope;
			double a = Math.sqrt((_radend + _oveject) / k);
			double s = (a + _oveject * 2.4129693) / _dteject;
			double x = s * dt;
			if (x <= a)
			{
				double y = k * (x - a) * (x - a) - _oveject;
				radcur = _radius * (100.0 - y) / 100.0;
			}
			else if (x <= a + _oveject * 1.2064846)//(x <= a + _oveject / 2.0)
			{
				//double y = -Math.sqrt(_oveject * _oveject / 4.0 - (x - a) * (x - a)) - _oveject / 2.0;
				double y = -Math.sqrt(_oveject * _oveject / 0.343396 - (x - a) * (x - a)) + _oveject * 0.414 / 0.586;
				radcur = _radius * (100.0 - y) / 100.0;
			}
			else if (x <= a + _oveject * 2.4129693)
			{
				//double y = Math.sqrt(_oveject * _oveject / 4.0 - (x - a - _oveject) * (x - a - _oveject)) - _oveject / 2.0;
				double y = Math.sqrt(_oveject * _oveject / 0.343396 - (x - a - _oveject * 2.4129693) * (x - a - _oveject * 2.4129693)) - _oveject / 0.586;
				radcur = _radius * (100.0 - y) / 100.0;
			}
			else
			{
				radcur = _radius;
				//_switch = SWITCH_DONNE;
				//_state = SliderState.PXE_SLIDER_STATE_NONE;
			}
		}
		else if (SWITCH_CLOSE == _switch)
		{
			// close :
			// y + over = k * (x - a)^2
			// a = Math.sqrt(_radend + over / k)
			// (x - a)^2 + (y + over / 2)^2 = over * over / 2
			// (x - a - over)^2 + (y + over / 2)^2 = over * over / 2
			// speed = (a + over) / _dteject
			
			
			// y + over = k * (x - a)^2
			// a = Math.sqrt(_radend + over / k)
			// (x - a)^2 + (y - 0.414R)^2 = R^2 = over * over / 0.343396
			// (x - 0.586)^2 + (y + R)^2 = R^2 = over * over / 0.343396
			// R = over / 0.586
			
			//int cnt = (int)(_field * 1.5 / _space);
			double k = _rslope;
			double a = Math.sqrt((_radend + _oveject) / k);
			double s = (a + _oveject * 2.4129693) / _dtclose;
			double x = a + _oveject * 2.4129693 - s * dt;
			if (x <= 0)
			{
				radcur = _radius * _radend / 100.0;
			}
			else if (x <= a)
			{
				double y = k * (x - a) * (x - a) - _oveject;
				radcur = _radius * (100.0 - y) / 100.0;
			}
			else if (x <= a + _oveject * 1.2064846)//(x <= a + _oveject / 2.0)
			{
				//double y = -Math.sqrt(_oveject * _oveject / 4.0 - (x - a) * (x - a)) - _oveject / 2.0;
				double y = -Math.sqrt(_oveject * _oveject / 0.343396 - (x - a) * (x - a)) + _oveject * 0.414 / 0.586;
				radcur = _radius * (100.0 - y) / 100.0;
			}
			else if (x <= a + _oveject * 2.4129693)
			{
				//double y = Math.sqrt(_oveject * _oveject / 4.0 - (x - a - _oveject) * (x - a - _oveject)) - _oveject / 2.0;
				double y = Math.sqrt(_oveject * _oveject / 0.343396 - (x - a - _oveject * 2.4129693) * (x - a - _oveject * 2.4129693)) - _oveject / 0.586;
				radcur = _radius * (100.0 - y) / 100.0;
			}
			else
			{
				radcur = _radius;
				//_switch = SWITCH_DONNE;
				//_state = SliderState.PXE_SLIDER_STATE_NONE;
			}
		}
//		{
//			// close :
//			// y = k * (x - a)^2
//			// a = Math.sqrt(100 / k)
//			// speed = a / _dtclose
//			
//			// y = 100.0 - k * x * x
//			// a = Math.sqrt((100.0 - _radend) / k)
//			// speed = _radend / _dtclose
//			
//			double k = _rslope;
//			double a = Math.sqrt((100.0 - _radend) / k);//Math.sqrt(100.0 / k);
//			double s = a / _dtclose;//(100.0 - _radend) / _dtclose;
//			double x = s * dt;
//			if (x <= a)
//			{
//				double y = 100.0 - k * x * x;//k * (x - a) * (x - a);
//				radcur = _radius * y / 100.0;
//			}
//			else
//			{
//				radcur = _radius * _radend / 100.0;
//				//_switch = SWITCH_DONNE;
//				//_state = SliderState.PXE_SLIDER_STATE_NONE;
//			}
//		}
		
		return radcur;
	}
	
	//锟斤拷锟睫伙拷没锟叫匡拷锟斤拄1�7=>锟斤拷锟斤不锟矫匡拷锟角ｏ拷锟斤拷幕锟斤拷锟斤拷y丄1�7锟斤拷锟斤拷锟斤拄1�7
	protected double calcDeltaCursor(Point last, Point curr)
	{
		double delta = 0.0;
		
		double delta0 = Math.asin(((curr.x * 2.0 / _chord) - 1.0) * Math.sin(_field / 2.0));
		double delta1 = Math.asin(((last.x * 2.0 / _chord) - 1.0) * Math.sin(_field / 2.0));
		
		delta = (delta1 - delta0) / _space;
		return delta;
	}
	
	protected int indexOf(final Sprite sprite)
	{
		int index = -1;
		for (int i = 0; i < _sprites.size(); ++i)
		{
			if (sprite == _sprites.get(i))
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	protected void calcDistArr()
	{
		int size = _sprites.size();
		double lgth = _sprites.size();
		double half = lgth / 2.0;
		for (int i = 0; i < size; ++i)
		{
			double dist = i - _cursor;
			if (Math.abs(dist) > half)
			{
				if (dist < 0.0)
				{
					dist += lgth;
				}
				else
				{
					dist -= lgth;
				}
			}
			_distarr.set(i, dist);
		}
	}
	
	//锟斤拷锟睫伙拷没锟叫匡拷锟斤拄1�7=>锟窖斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷圆锟斤拷锟斤拷示锟斤拷围锟斤拷visible锟斤拷为false
	protected void calcViewPts()
	{
		int cnt = (int)(_field * 1.5 / _space);
		long dt = _timenow - _timebeg;
		int size = _sprites.size();
		for (int i = 0; i < size; ++i)
		{
			double dist = _distarr.get(i);
			Sprite sprite = _sprites.get(i);
			double delta = _space * dist + _space / 2.0;
			Point pt = sprite.getPosition();
			sprite.setAngle(delta);
			
			pt.x = (int)(_radius * Math.sin(delta) + _chord / 2.0);
			pt.y = (int)(_radius * (Math.cos(delta) - Math.cos(_field / 2.0)));
			
			pt.x += (_centx - _chord / 2.0);

			//pt.x = (int)(_chord * 0.5 * (1.0 + Math.sin(delta) / Math.sin(_field /2.0)));
			//pt.y = (int)(_chord * 0.5 * (Math.cos(delta) - Math.cos(_field / 2.0)) / Math.sin(_field / 2.0));
			
			pt.y += _shift;
			pt.y = (int)(_height) - pt.y;

			if (SWITCH_EJECT == _switch)
			{
				long delay = Math.abs((long)(_dleject * dist));
				double radcur = 0.0;
				if (delay < dt)
				{
					radcur = calcRadiusFormula(dt - delay);
			    	pt.x = (int)(_centx + (pt.x - _centx) * radcur / _radius);
				    pt.y = (int)(_centy + (pt.y - _centy) * radcur / _radius);
				}
				else
				{
					radcur = _radius * _radend / 100.0;
			    	pt.x = (int)(_centx + (pt.x - _centx) * radcur / _radius);
				    pt.y = (int)(_centy + (pt.y - _centy) * radcur / _radius);
				}
			 
			}
			else if (SWITCH_CLOSE == _switch)
			{
				double radcur = calcRadiusFormula(dt);
				pt.x = (int)(_centx + (pt.x - _centx) * radcur / _radius);
				pt.y = (int)(_centy + (pt.y - _centy) * radcur / _radius);
			}
			
			//锟斤拷锟絛elta锟斤拷锟斤拷锟斤拷锟斤拷锟斤拄1�7
			if (delta >= -Math.PI && delta <= Math.PI)
			{
				sprite.setVisible(true);
			}
			else
			{
				sprite.setVisible(false);
			}
		}

		if (SWITCH_EJECT == _switch)
		{
			if (dt > _dteject + _dleject * cnt)
			{
				_switch = SWITCH_DONNE;
				_state = SliderState.PXE_SLIDER_STATE_NONE;
			}
		}
		else if (SWITCH_CLOSE == _switch)
		{
			if (dt > _dtclose)
			{
				_switch = SWITCH_DONNE;
				_state = SliderState.PXE_SLIDER_STATE_NONE;
			}
		}
	}
	
	public double getRadius()
	{
		return (_chord / 2.0) / Math.sin(_field / 2.0);
	}

	public Point getCenter()
	{
		_centx = _offsetx;// + ((_chord + 0.5) / 2.0);
		_centy = -((_chord / 2.0) / Math.tan(_field / 2.0) + 0.5);
		_centy += _shift;
		_centy = (int)(_height - _centy);
		
		Point pt = new Point((int)_centx, (int)_centy);
		return pt;
	}
	
	public double getAngle(int idx)
	{
		double angle = 0.0;
		return angle;
	}
	
}
