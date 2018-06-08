/*
 * Copyright (C) 2010,2011 Thundersoft Corporation
 * All rights Reserved
 */
package com.cam001.share;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cam001.photoeditor.R;

public class ShareBaseAdapter extends BaseAdapter {
    private Context mContext;
    private int mLayout;
    // mShareItems is a collection which contains the application's icon,text,info.
    private List<ShareInfoItem> mShareItems;
    private LayoutInflater mInflater;

    public ShareBaseAdapter(Context context, int layout, List<ShareInfoItem> shareItems) {
        mContext = context;
        mLayout = layout;
        mShareItems = shareItems;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if(mShareItems != null && mShareItems.size() > 0) {
            return mShareItems.size();
        } else {
            return 0;
        }
    }

    public Object getItem(int position) {
        if(mShareItems != null && mShareItems.size() > 0) {
            return mShareItems.get(position);
        } else {
            return null;
        }
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if(convertView == null) {
            view = mInflater.inflate(mLayout, parent, false);
        } else {
            view = convertView;
        }
        ShareInfoItem shareItem = mShareItems.get(position);
        ((ImageView)view.findViewById(R.id.icon_share)).setImageDrawable((Drawable)shareItem.iconId);
        ((TextView)view.findViewById(R.id.text_share)).setText((CharSequence)shareItem.textId);

        return view;
    }

}
