package com.cam001.photoeditor;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cam001.util.ExifUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;
import com.cam001.widget.PhotoImage;

/**
 * Created by dell on 15-6-11.
 */
public class PhotoDetailActivity extends BaseActivity{

    private PhotoImage mPhotoImage;
    private ListView listView;
    private DetailItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main_detail_dialog);
         listView= (ListView) findViewById(R.id.detail_list);
         itemAdapter=new DetailItemAdapter();

        findViewById(R.id.detail_close_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Util.startBackgroundJob(PhotoDetailActivity.this, "", "", new Runnable() {
            @Override
            public void run() {
                initexifInData();
               mHandler.sendEmptyMessage(0x001);
            }
        }, mHandler);

    }

    @Override
    protected void handleMessage(Message msg) {
       if (msg.what==0x001){
           listView.setAdapter(itemAdapter);
       }
    }

    private void initexifInData() {
        Uri mUri=getIntent().getData();
        if(mUri==null) {
            ToastUtil.showShortToast(this, R.string.invalid_file);
            finish();
            return;
        }
        String path= FileUtil.getPath(this, mUri);
        if (path==null){
            ToastUtil.showShortToast(this, R.string.invalid_file);
            finish();
            return;
        }

        mPhotoImage= ExifUtil.getPhotExif(this,path);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class DetailItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mPhotoImage.mDetailList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view=getLayoutInflater().inflate(R.layout.main_dialog_item,null);
            TextView txt= (TextView) view.findViewById(R.id.txt_detail);
            txt.setText(mPhotoImage.mDetailList.get(i));
            return view;
        }
    }
}
