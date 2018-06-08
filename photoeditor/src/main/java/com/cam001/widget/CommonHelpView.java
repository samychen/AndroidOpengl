package com.cam001.widget;

import com.cam001.photoeditor.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CommonHelpView extends FrameLayout {

	private TextView mTxtMessage = null;
	
	public CommonHelpView(Context context) {
		super(context);
		initControls();
	}
	
	public CommonHelpView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initControls();
	}
	
	private void initControls() {
		inflate(getContext(), R.layout.widget_common_helpview, this);
		mTxtMessage = (TextView)findViewById(R.id.helpview_message);
	}

	public void setText(int resid) {
		mTxtMessage.setText(resid);
	}
}
