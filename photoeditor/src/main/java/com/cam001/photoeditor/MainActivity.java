package com.cam001.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.cam001.util.BitmapUtil;


public class MainActivity extends BaseActivity
    implements View.OnClickListener{

    public static final int MAX_IMAGE_WIDTH = 1024;
    private Uri mPickedUri = null;
    private ImageView mDisplayImage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mPickedUri=getIntent().getData();
//        if(mPickedUri==null) {
//            ToastUtil.showShortToast(this, R.string.invalid_file);
//            finish();
//            return;
//        }
        init();
    }

    private void init() {
        findViewById(R.id.btn_main_back).setOnClickListener(this);
        findViewById(R.id.btn_main_share).setOnClickListener(this);
        findViewById(R.id.btn_main_rotate).setOnClickListener(this);
        findViewById(R.id.btn_main_crop).setOnClickListener(this);
        findViewById(R.id.btn_main_filter).setOnClickListener(this);
        findViewById(R.id.btn_main_enhance).setOnClickListener(this);
        findViewById(R.id.btn_main_marker).setOnClickListener(this);
        findViewById(R.id.btn_main_beautify).setOnClickListener(this);

        findViewById(R.id.detal_image).setOnClickListener(this);
        findViewById(R.id.delect_image).setOnClickListener(this);

        mDisplayImage=(ImageView)findViewById(R.id.add_image);
    }

    @Override
    protected void onResume() {
        if (mConfig.mCurrUri==null){
            mConfig.mCurrUri=getIntent().getData();
        }
        mPickedUri=mConfig.mCurrUri;
        mDisplayImage.setImageBitmap(loadImage(mPickedUri));
        super.onResume();
    }

    public Bitmap loadImage(Uri uri) {
        Bitmap bmp = BitmapUtil.getBitmap(uri, this, MAX_IMAGE_WIDTH,
                MAX_IMAGE_WIDTH);
        return bmp;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_back:
                finish();
                break;
            case R.id.btn_main_share:
                if (mConfig.mMainView!=null){
                    mConfig.mMainView=null;
                }
                mConfig.mMainView=getWindow().getDecorView();
                Intent ints=new Intent(this,ShareActivity.class);
                ints.setData(mPickedUri);
                startActivity(ints);
                break;
            case R.id.btn_main_rotate:
                onBtnRotateClick();
                break;
            case R.id.btn_main_crop:
                onBtnCropClick();
                break;
            case R.id.btn_main_filter:
                onBtnFilterClick();
                break;
            case R.id.btn_main_enhance:
                onBtnEnhanceClick();
                break;
            case R.id.detal_image:
                Intent intent=new Intent(this,PhotoDetailActivity.class);
                intent.setData(mPickedUri);
                startActivity(intent);
                break;
            case R.id.delect_image:
                Intent inDel=new Intent(this,PhotoDelectActivity.class);
                inDel.setData(mPickedUri);
                startActivity(inDel);
                break;
            case R.id.btn_main_marker:
                Intent inMark=new Intent(this,BeautyActivity.class);
                inMark.setData(mPickedUri);
                startActivity(inMark);
                break;
            case R.id.btn_main_beautify:
                Intent inBeatify=new Intent(this,MakeupActivity.class);
                inBeatify.setData(mPickedUri);
                startActivity(inBeatify);
                break;
            default:
                break;
        }
    }

    private void onBtnRotateClick() {
        if(mPickedUri==null) return;
        Intent intent = new Intent();
        intent.setClass(this, RotateActivity.class);
        intent.setData(mPickedUri);
        startActivity(intent);
    }

    private void onBtnCropClick() {
        if(mPickedUri==null) return;
        Intent intent = new Intent();
        intent.setClass(this, CropActivity.class);
        intent.setData(mPickedUri);
        startActivity(intent);
    }

    private void onBtnFilterClick() {
        if(mPickedUri==null) return;
        Intent intent = new Intent();
        intent.setClass(this, FilterActivity.class);
        intent.setData(mPickedUri);
        startActivity(intent);
    }

    private void onBtnEnhanceClick() {
        if(mPickedUri==null) return;
        Intent intent = new Intent();
        intent.setClass(this, AdjustActivity.class);
        intent.setData(mPickedUri);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        mConfig.mCurrUri=null;
        if (mConfig.mMainView!=null){
            mConfig.mMainView=null;
        }
        super.onDestroy();
    }
}
