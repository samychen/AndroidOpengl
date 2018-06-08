package com.cam001.photoeditor;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.cam001.util.StorageUtil;

import java.io.File;

/**
 * Created by dell on 15-6-8.
 */
public class StartActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_start_activity);

        findViewById(R.id.gallery_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBtnGalleryClick();
            }
        });
    }

    private Uri mPickedUri;
    private void onBtnCameraClick() {
        StorageUtil.ensureOSXCompatible();
        long dateTaken = System.currentTimeMillis();
        String name = String.format("%d", dateTaken) + ".jpg";
        String filename = StorageUtil.DIRECTORY+"/"+name;

        mPickedUri = Uri.fromFile(new File(filename));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPickedUri);
        try {
            startActivityForResult(intent, 0);
        } catch(ActivityNotFoundException e) {
        }
    }


    private void onBtnGalleryClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        try {
            startActivityForResult(intent, 1);
        } catch(ActivityNotFoundException e) {
//        	ToastUtil.showShortToast(mConfig.appContext, R.string.edit_operation_failure_tip);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK) {
            return;
        }

        if(requestCode==1 && data!=null){
            mPickedUri=data.getData();
        }

        Intent intent=new Intent();
        intent.setClass(this,MainActivity.class);
        intent.setData(mPickedUri);
        startActivity(intent);
    }
}
