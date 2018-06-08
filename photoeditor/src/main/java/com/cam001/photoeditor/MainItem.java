package com.cam001.photoeditor;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainItem extends FrameLayout{

	protected ImageView mImage = null;
	protected ImageView mNewImage = null;
	protected TextView mText = null;
	protected TextView mRedText = null;

	protected int mImgRes = 0;
	
	public MainItem(Context context) {
		super(context);
		 initControls();
	}
	
	public MainItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		 initControls();
		 TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.Item);
		 int imageId = types.getResourceId(R.styleable.Item_image, 0);
		 int textId = types.getResourceId(R.styleable.Item_text, 0);
		 int txtColorId=types.getResourceId(R.styleable.Item_textColor, 0);
		setTxtColor(txtColorId,textId);
		 setImage(imageId);
		 types.recycle();
	}

	private void initControls() {
		inflate(getContext(), R.layout.editor_item_main, this);
		mImage = (ImageView) findViewById(R.id.editor_main_btn_image);
		mText = (TextView) findViewById(R.id.editor_main_btn_text);
		mRedText = (TextView) findViewById(R.id.editor_red_btn_text);
		mNewImage=(ImageView) findViewById(R.id.editor_image_new_icon);
	}

	public void setTxtColor(int resid,int txtId) {
		if (resid==0){
			mText.setVisibility(View.VISIBLE);
			mText.setText(txtId);
			mRedText.setVisibility(View.GONE);
		}else {
			mText.setVisibility(View.GONE);
			mRedText.setVisibility(View.VISIBLE);
			mRedText.setText(txtId);
		}
	}

	public void setImage(int resId) {
		mImgRes = resId;
		mImage.setImageResource(resId);
	}
	
	@Override
	public void setSelected(boolean bSelect) {
		super.setSelected(bSelect);
		mImage.setSelected(bSelect);
		mText.setSelected(bSelect);
	}
	
	public void setNewImageVisible(){
		mNewImage.setVisibility(View.VISIBLE);
	}
}