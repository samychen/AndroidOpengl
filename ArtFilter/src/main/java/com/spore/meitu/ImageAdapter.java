/**
 * Name        : ImageAdapter.java
 * Copyright   : Copyright (c) Tencent Inc. All rights reserved.
 * Description : TODO
 */

package com.spore.meitu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

/**
 * @author ianmao
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    
    private int mCurrPos = 0;

    private Integer[] mImageIds = {
            R.drawable.img_1, R.drawable.img_2, R.drawable.img_3, R.drawable.img_4, R.drawable.img_5, R.drawable.img_6
    };

    public ImageAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mImageIds.length;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }
    
    public int getCurrPos(){
        return mCurrPos;
    }
    
    public void setCurrPos(int pos){
        mCurrPos = pos;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public int getImgId(int position) {
        return mImageIds[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ImageView i = new ImageView(mContext);

        i.setImageResource(mImageIds[position]);

        i.setLayoutParams(new Gallery.LayoutParams(200, 200));

        i.setScaleType(ImageView.ScaleType.FIT_XY);
        return i;
    }

}
