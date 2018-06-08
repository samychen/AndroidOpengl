package com.cam001.photoeditor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cam001.share.QQTool;
import com.cam001.share.ShareItem;
import com.cam001.share.ShareItemAdapter;
import com.cam001.share.ShareItemIDs;
import com.cam001.share.ShareUtils;
import com.cam001.share.SinaTool;
import com.cam001.share.Util;
import com.cam001.share.WeChatTool;
import com.cam001.stat.StatApi;
import com.cam001.util.BitmapUtil;
import com.cam001.util.FileUtil;
import com.cam001.util.ToastUtil;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.onekeyshare.OnekeyShare;
import com.onekeyshare.ShareContentCustomizeCallback;
import com.facebook.widget.FacebookDialog.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by dell on 15-6-12.
 */
public class ShareActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "ShareActivity";
    public static final String FILEPATH = "filePath";
    private ShareItemAdapter mShareItemAdapter = null;
    private int mShareItemID = -1;
    private TextView mCancelTxt;
    private UiLifecycleHelper mUiHelper = null;
    private ImageView mShareIconImage;
    private Bitmap mBmpBlur = null;
    private Uri mImageUri;
    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUri=getIntent().getData();
        if(mImageUri==null){
            finish();
        }
        mImagePath= FileUtil.getPath(this,mImageUri);
        if (mImagePath==null){
            ToastUtil.showShortToast(this,R.string.crop_decode_bmp_err);
            finish();
        }

        setContentView(R.layout.activity_share);
        mUiHelper = new UiLifecycleHelper(this, null);
        ShareSDK.initSDK(this);
        mCancelTxt=(TextView) findViewById(R.id.bottom_cancel_txt);

        mShareIconImage = (ImageView) findViewById(R.id.iv_photo);
        setShareIcon();

        findViewById(R.id.empty_area_rl).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               finish();
            }
        });
        GridView mGridView = (GridView) findViewById(R.id.share_item_grid);
        mShareItemAdapter = new ShareItemAdapter(this);
        mGridView.setAdapter(mShareItemAdapter);
        mGridView.setOnItemClickListener(this);

        final RelativeLayout mCancelLayout=(RelativeLayout)findViewById(R.id.bottom_icon_rl);
        mCancelLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action=event.getAction();
                if (action==MotionEvent.ACTION_DOWN) {
//                    mCancelLayout.setBackgroundColor(Color.parseColor("#323232"));
                    mCancelTxt.setTextColor(Color.parseColor("#f05164"));
                }else if (action==MotionEvent.ACTION_UP ||
                        action==MotionEvent.ACTION_CANCEL) {
//                    mCancelLayout.setBackgroundColor(Color.parseColor("#101010"));
                    mCancelTxt.setTextColor(Color.parseColor("#ffffff"));
                    if (action==MotionEvent.ACTION_UP) {
                       finish();
                    }
                }
                return true;
            }
        });

    }

    private void setShareIcon() {
        if(mBmpBlur!=null) {
            mShareIconImage.setImageBitmap(null);
            mBmpBlur.recycle();
            mBmpBlur = null;
        }
        if (mBmpBlur == null) {
             createBlurBG();
        }
    }

    private void createBlurBG() {
        View root = mConfig.mMainView;
        root.setDrawingCacheEnabled(true);
        Bitmap bmpThumb = root.getDrawingCache();

        if (bmpThumb != null) {
            int sample = 8;
            int width = bmpThumb.getWidth()/sample;
            int height = bmpThumb.getHeight() / sample;
            Bitmap bmp = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            c.drawBitmap(bmpThumb, null, new Rect(0, 0, width, height),
                    null);
            mBmpBlur = BitmapUtil.fastblur(this,bmp, 4);
            bmp.recycle();
        }
        root.setDrawingCacheEnabled(false);
        mShareIconImage.setImageBitmap(mBmpBlur);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        if (!Util.isNetWork(this)) {
           mHandler.post(new Runnable() {
               @Override
               public void run() {
                   ToastUtil.showShortToast(ShareActivity.this,
                           getString(R.string.sns_msg_network_unavailable));
               }
           });
        } else {
            ShareItem mItem = (ShareItem) mShareItemAdapter.getItem(position);
            mShareItemID = mItem.getId();
            String shareName = null;
            switch (mShareItemID) {
                case ShareItemIDs.FACEBOOK:
                    StatApi.onEvent(this, StatApi.UMENG_EVENT_btnShareFacebook);
                    Facebook();
                    shareName = "facebook";
                    break;
                case ShareItemIDs.TWITTER:
                    StatApi.onEvent(this, StatApi.UMENG_EVENT_btnShareTwitter);
                    Twitter();
                    shareName = "twitter";
                    break;
                case ShareItemIDs.INSTAGRAM:
                    StatApi.onEvent(this, StatApi.UMENG_EVENT_btnShareInstagram);
                    Instagram();
                    shareName = "instagram";
                    break;
                case ShareItemIDs.PINTEREST:
                    StatApi.onEvent(this, StatApi.UMENG_EVENT_btnSharePinterest);
                    Pinterest();
                    shareName = "pinterest";
                    break;
                case ShareItemIDs.OTHER:
                    StatApi.onEvent(this, StatApi.UMENG_EVENT_btnShareMore);
                    More();
                    shareName = "Other";
                    break;
                case ShareItemIDs.QQ:
                    QQ();
                    shareName = "QQ";
                    break;
                case ShareItemIDs.WECHATGP:
                    WeChatFG();
                    shareName = "wecgatgp";
                    break;
                case ShareItemIDs.SINA:
                    Sina();
                    shareName = "Sina";
                    break;
                case ShareItemIDs.WECHAT:
                    WeChat();
                    shareName = "Wechat";
                    break;
                default:
                    break;
            }
        }

    }

    private void QQ(){
        if (Util.isAppInstalled("com.tencent.mobileqq", this)) {
//			String tag = mEditTag.getText() == null ? "" : mEditTag.getText()
//					.toString();
//			Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
//			shareIntent.setType("image/*");
//			shareIntent.putExtra(Intent.EXTRA_STREAM, mUri);
//			shareIntent.putExtra(Intent.EXTRA_TEXT, tag + "#FilterCollage");
//			shareIntent.setPackage("com.tencent.mobileqq");
//			startActivity(shareIntent);
            QQTool.getInstance(this).Upload(mImagePath);
        } else {
            ToastUtil.showShortToast(this,
                    getString(R.string.qq_notinstall_alert));
        }

    }

    private void WeChat() {
        WeChatTool.getInstance(this).Upload(mImagePath);
    }

    private void WeChatFG() {
        WeChatTool.getInstance(this).UploadFG(mImagePath);
    }
    private SinaTool mSinaTool = null;

    private void Sina() {
        String tag = getResources().getString(R.string.app_name);
        mSinaTool = new SinaTool(this);
        mSinaTool.Upload(mImagePath, "#" + tag+"#");
//		SinaTool.getInstance(this).Upload(mFilePath);
    }

    private void Pinterest(){
        if(Util.isAppInstalled("com.pinterest", this)){
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
            shareIntent.setPackage("com.pinterest");
            startActivity(shareIntent);
        }else{
            ToastUtil.showShortToast(this,
                    getString(R.string.pinterest_notinstall_alert));
        }
    }
    private void Instagram(){
        if(Util.isAppInstalled("com.instagram.android", this)){
            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, mImageUri);
            shareIntent.setPackage("com.instagram.android");
            startActivity(shareIntent);
        }else{
            ToastUtil.showShortToast(this,
                    getString(R.string.instagram_notinstall_alert));
        }
    }
    private void Facebook() {
//		showShare(false, cn.sharesdk.facebook.Facebook.NAME);
        if (FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.PHOTOS)) {
            // Publish the post using the Photo Share Dialog
            List<File> images = new ArrayList<File>();
            File file = new File(mImagePath);
            images.add(file);
            FacebookDialog shareDialog = new FacebookDialog.PhotoShareDialogBuilder(this)
                    .addPhotoFiles(images)
                    .build();
            mUiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            ToastUtil.showShortToast(this,
                    this.getString(R.string.facebook_notinstall_alert));
        }
    }

    private void Twitter() {
        showShare(false, cn.sharesdk.twitter.Twitter.NAME);
    }

    public void More() {
        ShareUtils.otherShare(this, mImageUri, null,
                Bitmap.CompressFormat.JPEG);
    }

    private void showShare(final boolean silent, final String platform) {
        final OnekeyShare oks = new OnekeyShare();
        oks.setNotification(R.drawable.ic_launcher,
                getString(R.string.app_name));
        oks.setText(" ");
        oks.setImagePath(mImagePath);
        oks.setSilent(silent);
        if (platform != null) {
            oks.setPlatform(platform);
        }
        oks.setDialogMode();
        oks.disableSSOWhenAuthorize();
        oks.setCallback(new PlatformActionListener() {

            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2) {
                showShareFailed();
            }

            @Override
            public void onComplete(Platform arg0, int arg1,
                                   HashMap<String, Object> arg2) {
                showShareComplete();
            }

            @Override
            public void onCancel(Platform arg0, int arg1) {
                // showShareFailed();
            }
        });// OneKeyShareCallback());
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
            @Override
            public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
                Log.d(TAG, "onShare=" + platform.getName());
            }

        });
        oks.show(this);
    }

    private void showShareFailed() {
       mHandler.post(new Runnable() {
           @Override
           public void run() {
               ToastUtil.showShortToast(ShareActivity.this,
                       R.string.share_fail);
           }
       });
    }

    private void showShareComplete() {
      mHandler.post(new Runnable() {
          @Override
          public void run() {
              ToastUtil.showShortToast(ShareActivity.this,
                      R.string.share_succ);
          }
      });
    }

    public void recycleBmp(){
        if(mBmpBlur!=null){
            mBmpBlur.recycle();
            mBmpBlur=null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUiHelper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUiHelper.onResume();
    }

    @Override
    protected void onDestroy() {
        mUiHelper.onDestroy();
        ShareSDK.stopSDK(this);
        recycleBmp();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
                    try {
                        QQTool.getInstance(ShareActivity.this).onActivityResult(requestCode, resultCode, data);
                        SinaTool.getInstance(this).authorizeCallBack(requestCode, resultCode,
                                data);
                    } catch (Exception e) {
                    }

        }
        super.onActivityResult(requestCode, resultCode, data);
        mUiHelper.onActivityResult(requestCode, resultCode, data, new Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("bug", String.format("Error: %s", error.toString()));
            }
            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.e("bug", "Success!");
            }
        });//(requestCode, resultCode, data);
    }
}
