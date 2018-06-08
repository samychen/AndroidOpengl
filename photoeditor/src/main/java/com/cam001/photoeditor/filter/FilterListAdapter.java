package com.cam001.photoeditor.filter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.cam001.photoeditor.resource.Filter;
import com.cam001.photoeditor.resource.FilterCategory;
import com.cam001.photoeditor.resource.FilterFactory;

/**
 * Created by hzhao on 15/6/8.
 */
public class FilterListAdapter extends BaseAdapter {

    private FilterCategory[] mCates = FilterFactory.createFilters();
    private int mCount = 0;
    private Context mContext = null;

    public FilterListAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        if(mCount==0) {
            for(FilterCategory cate: mCates) {
                mCount += cate.getFilters().size();
            }
        }
        return mCount;
    }

    @Override
    public Filter getItem(int position) {
        for(FilterCategory cate: mCates) {
            int size = cate.getFilters().size();
            if(size>position) {
                return cate.getFilters().get(position);
            }
            position -= size;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Filter filter = getItem(position);
        FilterListItem item = null;
        if(convertView==null) {
            item = new FilterListItem(mContext);
        } else {
            item = (FilterListItem) convertView;
        }
        item.setFilter(filter, position);
        return item;
    }
}
