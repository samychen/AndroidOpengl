package com.cam001.photoeditor.beauty;

import com.cam001.photoeditor.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ConfirmDialog extends Dialog{

	private View.OnClickListener mlClick = null;
	private int mMsgId = 0;
//	private boolean mIsMain=false;
	
	public ConfirmDialog(Context context, int msgId) {
		super(context, R.style.Theme_dialog);
		mMsgId = msgId;
//		mIsMain = (msgId == R.string.net_info);
	}

	public void setOnClickListener(View.OnClickListener l) {
		mlClick = l;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_confirm);
		((TextView)findViewById(R.id.confirm_message)).setText(mMsgId);
		findViewById(R.id.confirm_button_cancel).setOnClickListener(mlClick);
		findViewById(R.id.confirm_button_sure).setOnClickListener(mlClick);
		
//		if (mIsMain) {
//			((TextView)findViewById(R.id.cancel_txt)).setText(R.string.exit_info);
//			((TextView)findViewById(R.id.sure_txt)).setText(R.string.continue_info);
//		}
	}
	
}
