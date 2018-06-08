package com.cam001.photoeditor.filter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cam001.photoeditor.R;
import com.cam001.photoeditor.resource.Filter;

/**
 * Created by hzhao on 15/6/8.
 */
public class FilterListItem extends FrameLayout{

    private ImageView mImageView = null;
    private ImageView mFocusImage = null;
    private TextView mTextView = null;
    private Filter mFilter = null;

    public FilterListItem(Context context) {
        super(context);
        init();
    }

    public FilterListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.filter_thumb_item, this);
        mImageView = (ImageView) findViewById(R.id.img_filter_item_thumb);
        mFocusImage= (ImageView) findViewById(R.id.color_item_image_focus1);
        mTextView = (TextView) findViewById(R.id.txt_filter_item_thumb);
    }

    public void setSelected(boolean isSelect){
        if (isSelect){
            mFocusImage.setVisibility(View.VISIBLE);
            mTextView.setTextColor(getResources().getColor(R.color.main_btn_pressed));
        }else {
            mFocusImage.setVisibility(View.GONE);
            mTextView.setTextColor(getResources().getColor(R.color.color_textview));
        }
    }

    public void setFilter(Filter filter,int position) {
        mFilter = filter;
        mImageView.setImageBitmap(filter.getThumbnail());
        mTextView.setText("L" + (position + 1));
    }

    public Filter getFilter() {
        return mFilter;
    }

}
