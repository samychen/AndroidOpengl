package com.cam001.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cam001.util.BitmapUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ImageUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;
import com.cam001.widget.AdjustImageView;


public class AdjustActivity extends BaseActivity
    implements SeekBar.OnSeekBarChangeListener,View.OnClickListener {

    private AdjustImageView mView = null;
    private Uri mUri = null;
    private TextView mPercentTxt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust);
        init();
    }

    private void init() {
        findViewById(R.id.btn_adjust_cancel).setOnClickListener(this);
        findViewById(R.id.btn_adjust_sure).setOnClickListener(this);

        mView = (AdjustImageView) findViewById(R.id.img_adjust);
        ((SeekBar) findViewById(R.id.seek_adjust_brightness)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.seek_adjust_saturation)).setOnSeekBarChangeListener(this);
        ((SeekBar) findViewById(R.id.seek_adjust_contrast)).setOnSeekBarChangeListener(this);

        mPercentTxt = (TextView) findViewById(R.id.tip_txt);
        mPercentTxt.setShadowLayer(3, 0, 1, Color.parseColor("#4D000000"));
        mPercentAnim = AnimationUtils.loadAnimation(this,
                R.anim.push_out);
        mPercentAnim.setAnimationListener(mPercentAnimListener);

        mUri = getIntent().getData();
        if (mUri == null) {
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

    Animation mPercentAnim;
    public void startPercentPushOnAnim(){
        mPercentTxt.startAnimation(mPercentAnim);
    }

    public Animation.AnimationListener mPercentAnimListener=new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mPercentTxt.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int resId=0;
        switch(seekBar.getId()) {
            case R.id.seek_adjust_brightness:
                mView.setBrightness(progress);
                resId=R.string.text_brightness;
                break;
            case R.id.seek_adjust_saturation:
                mView.setSaturation(progress);
                resId=R.string.text_saturation;
                break;
            case R.id.seek_adjust_contrast:
                mView.setContrast(progress);
                resId=R.string.text_contrast;
                break;
            default:
                break;
        }

        mPercentAnim.setAnimationListener(null);
        mPercentTxt.clearAnimation();

        mPercentTxt.setVisibility(View.VISIBLE);
        mPercentTxt.setText(getResources().getString(resId) + progress);
        mPercentAnim.setAnimationListener(mPercentAnimListener);
        mView.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mPercentTxt.getVisibility() == View.VISIBLE) {
            startPercentPushOnAnim();
        }
    }

    String path;
    @Override
    public void onClick(View view) {
      switch (view.getId()){
          case R.id.btn_adjust_cancel:
              finish();
              break;
          case R.id.btn_adjust_sure:
             Util.startBackgroundJob(AdjustActivity.this, "", "", new Runnable() {
                  @Override
                  public void run() {
                      path= FileUtil.getPath(AdjustActivity.this, mUri);
                      mConfig.mCurrUri=ImageUtil.enhance(AdjustActivity.this, mUri, mView.getColorMatrix(),path);
                      setResult(RESULT_OK, new Intent().setData(mConfig.mCurrUri));
                      finish();
                  }
              }, mHandler);
              break;
          default:
              break;
      }
    }
}
