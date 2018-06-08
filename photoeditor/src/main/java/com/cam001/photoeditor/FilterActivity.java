package com.cam001.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cam001.photoeditor.filter.FilterEngine;
import com.cam001.photoeditor.filter.FilterListAdapter;
import com.cam001.photoeditor.filter.FilterListItem;
import com.cam001.photoeditor.resource.Filter;
import com.cam001.util.BitmapUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;


public class FilterActivity extends BaseActivity
    implements View.OnClickListener{

    private ImageView mView = null;
    private LinearLayout mThumbLayout = null;
    private Uri mUri = null;
    private Bitmap mBmpOrig = null;
    private Bitmap mBmpFilter = null;
    private FilterEngine mFilterEngine = null;
    private FilterListAdapter mFilterAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        init();
    }

    private void init() {
        mFilterAdapter = new FilterListAdapter(this);
        mView = (ImageView) findViewById(R.id.img_filter_disp);
        mThumbLayout = (LinearLayout) findViewById(R.id.lyt_filter_thumbs);

        findViewById(R.id.btn_filter_cancel).setOnClickListener(this);
        findViewById(R.id.btn_filter_sure).setOnClickListener(this);

        mUri = getIntent().getData();
        if(mUri==null) {
            ToastUtil.showShortToast(this, R.string.invalid_file);
            finish();
            return;
        }

        Util.startBackgroundJob(this, null, null, new Runnable() {
            @Override
            public void run() {
                mBmpOrig = BitmapUtil.getBitmap(mUri, mConfig.appContext, 1024, 1024);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.setImageBitmap(mBmpOrig);
                        mFilterEngine = new FilterEngine();
                        mBmpFilter = Bitmap.createBitmap(mBmpOrig.getWidth(), mBmpOrig.getHeight(), Bitmap.Config.ARGB_8888);
                    }
                });
            }
        }, mHandler);

        for(int i=0; i<mFilterAdapter.getCount(); i++) {
            View v = mFilterAdapter.getView(i, null, null);
            v.setOnClickListener(this);
            mThumbLayout.addView(v);
        }

    }

    private FilterListItem mLastItem;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_filter_cancel:
                finish();
                break;
            case R.id.btn_filter_sure:
                Util.startBackgroundJob(FilterActivity.this, "", "", new Runnable() {
                    @Override
                    public void run() {
                        if (mLastItem != null) {
                            mFilterEngine.process(FilterActivity.this, mUri, mLastItem.getFilter());
                        }
                        setResult(RESULT_OK, new Intent().setData(mUri));
                        finish();
                    }
                }, mHandler);

                break;
            default:
                FilterListItem item = (FilterListItem) v;
                if (mLastItem!=null){
                    mLastItem.setSelected(false);
                }
                mLastItem=item;
                item.setSelected(true);

                Filter filter = item.getFilter();
                mFilterEngine.process(mBmpOrig, mBmpFilter, filter);
                mView.setImageBitmap(mBmpFilter);
                break;
        }
    }
}
