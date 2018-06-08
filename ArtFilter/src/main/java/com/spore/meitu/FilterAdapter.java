/**
 * Name : RegularDataAdapterEXP.java Copyright : Copyright (c) Tencent Inc. All
 * rights reserved. Description : TODO
 */
package com.spore.meitu;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.opengl.Visibility;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Administrator
 * 
 */
public class FilterAdapter extends BaseAdapter
{
	public static final String TAG = "TVContactAdapter"; // TODO move

	private List<String> mDataList = new ArrayList<String>();
	
	static class ViewCache
    {
        public Button Filter_Btn;
    }

	private ArtFilterActivity mParent = null;
	private LayoutInflater mFactory = null;

	public FilterAdapter(Context context)
	{
	    mParent = (ArtFilterActivity) context;
		mFactory = LayoutInflater.from(context);
		
		mDataList.add("圆角");
		mDataList.add("倒影");
		//mDataList.add("灰度");
		mDataList.add("灰度J");
		mDataList.add("浮雕");
		mDataList.add("浮雕J");
		mDataList.add("模糊");
		mDataList.add("模糊J");
		mDataList.add("黑白");
		mDataList.add("黑白J");
		mDataList.add("油画");
		mDataList.add("底片");
		mDataList.add("光照");
		mDataList.add("泛黄");
		mDataList.add("放大镜");
		mDataList.add("哈哈镜");
	}

	@Override
	public int getCount()
	{
		return mDataList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewCache item = null;
		final int index = position;

		if (convertView == null)
		{
			convertView = mFactory
					.inflate(R.layout.filter_item, null);
			item = new ViewCache();
			item.Filter_Btn = (Button) convertView
					.findViewById(R.id.filter_btn);
			convertView.setTag(item);
		}
		else
		{
			item = (ViewCache) convertView.getTag();
		}

		item.Filter_Btn.setText(mDataList.get(position).toString());

		item.Filter_Btn.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mParent.choose_filter(index);
            }
		});

		return convertView;
	}
}
