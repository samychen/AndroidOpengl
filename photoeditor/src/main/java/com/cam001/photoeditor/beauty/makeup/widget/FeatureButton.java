package com.cam001.photoeditor.beauty.makeup.widget;

import com.cam001.photoeditor.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class FeatureButton extends ImageButton {

	private int normalResId;
	private int checkResId;
	
	private boolean isCheck;

	public FeatureButton(Context context) {
		super(context);
	}

	public FeatureButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	public FeatureButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.FeatureButton);// TypedArray��һ����������
		normalResId = a.getResourceId(
				R.styleable.FeatureButton_unCheckedground, 0);//��ֹ��XML�ļ���û�ж��壬�ͼ�����Ĭ��ֵ30

		checkResId = a.getResourceId(
				R.styleable.FeatureButton_checkedground, 0);//��ֹ��XML�ļ���û�ж��壬�ͼ�����Ĭ��ֵ30
		
		setBackgroundResource(normalResId);
		
		a.recycle();

	}

//	public void setForeImage(int normalId, int checkId) {
//		this.normalResId = normalId;
//		this.checkResId = checkId;
//	}

	public void setChecked(boolean isCheck) {
		this.isCheck=isCheck;
		if (isCheck) {
			setBackgroundResource(checkResId);
		} else
			setBackgroundResource(normalResId);
		invalidate();
	}

	public boolean isCheck() {
		return isCheck;
	}
	
	
	
	

}
