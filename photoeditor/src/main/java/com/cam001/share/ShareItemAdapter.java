package com.cam001.share;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cam001.photoeditor.R;

public class ShareItemAdapter extends BaseAdapter {
	public static final String TAG = "ShareItemAdapter";
	private Context mContext = null;
	private ShareItem[] mShareItems = null;

	public ShareItemAdapter(Context context) {
		this.mContext = context;
		mShareItems = ShareItem.sortedValues();
		Log.d(TAG, ""+mShareItems.length);
	}

	@Override
	public int getCount() {
		return mShareItems.length;
	}

	@Override
	public Object getItem(int position) {
		return mShareItems[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView=LayoutInflater.from(mContext).inflate(R.layout.share_item, null);
		ImageView imageView=(ImageView) convertView.findViewById(R.id.icon_image);
		imageView.setImageResource(mShareItems[position].getIcon());
		TextView txtView=(TextView) convertView.findViewById(R.id.icon_txt);
		txtView.setText(mShareItems[position].getName());
		return convertView;
	}

}
