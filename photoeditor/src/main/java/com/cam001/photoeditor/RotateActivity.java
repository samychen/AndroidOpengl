package com.cam001.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.cam001.util.BitmapUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ImageUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;
import com.cam001.widget.RotateImageView;


public class RotateActivity extends BaseActivity
    implements View.OnClickListener {

    private RotateImageView mView = null;
    private Uri mUri = null;
    private String path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);
        init();
    }


    private void init() {
        findViewById(R.id.btn_rotate_cancel).setOnClickListener(this);
        findViewById(R.id.btn_rotate_left).setOnClickListener(this);
        findViewById(R.id.btn_rotate_right).setOnClickListener(this);
        findViewById(R.id.btn_rotate_mirrorx).setOnClickListener(this);
        findViewById(R.id.btn_rotate_mirrory).setOnClickListener(this);
        findViewById(R.id.btn_rotate_sure).setOnClickListener(this);
        mView = (RotateImageView) findViewById(R.id.view_rotate);

        mUri = getIntent().getData();
        if(mUri==null) {
            mUri=mConfig.mCurrUri;
//            ToastUtil.showShortToast(this, R.string.invalid_file);
//            finish();
//            return;
        }
        path=FileUtil.getPath(this,mUri);
        if (path==null){
            ToastUtil.showShortToast(this, R.string.invalid_file);
            finish();
            return;
        }

        Util.startBackgroundJob(this, null, null, new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = BitmapUtil.getBitmap(mUri, mConfig.appContext, 1024, 1024);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.setImageBitmap(bmp);
                    }
                });
            }
        }, mHandler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rotate_cancel:
                finish();
                break;
            case R.id.btn_rotate_left:
                mView.rotate(-90);
                break;
            case R.id.btn_rotate_right:
                mView.rotate(90);
                break;
            case R.id.btn_rotate_mirrorx:
                mView.mirrorX();
                break;
            case R.id.btn_rotate_mirrory:
                mView.mirrorY();
                break;
            case R.id.btn_rotate_sure:
                Util.startBackgroundJob(RotateActivity.this, "", "", new Runnable() {
                    @Override
                    public void run() {
                     mConfig.mCurrUri=  ImageUtil.rotate(RotateActivity.this, path, mUri,
                                mView.getRotate(), mView.getMirrorX(), mView.getMirrorY());
                        setResult(RESULT_OK, new Intent().setData( mConfig.mCurrUri));
                        finish();
                    }
                }, mHandler);
                break;
            default:
                break;
        }
    }
}
