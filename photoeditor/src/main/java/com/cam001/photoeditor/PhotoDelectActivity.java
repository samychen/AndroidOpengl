package com.cam001.photoeditor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cam001.util.FileUtil;
import com.cam001.util.ToastUtil;
import com.cam001.util.Util;

import java.io.File;

/**
 * Created by dell on 15-6-11.
 */
public class PhotoDelectActivity extends BaseActivity implements View.OnClickListener {
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

         mUri=getIntent().getData();
        if(mUri==null) {
            ToastUtil.showShortToast(this, R.string.invalid_file);
            finish();
            return;
        }

        setContentView(R.layout.photo_delect_dialog);

        findViewById(R.id.confirm_button_cancel).setOnClickListener(this);
        findViewById(R.id.confirm_button_sure).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm_button_cancel:
                finish();
                break;
            case R.id.confirm_button_sure:
                Util.startBackgroundJob(PhotoDelectActivity.this, "", "", new Runnable() {
                    @Override
                    public void run() {
                        String path = FileUtil.getPath(PhotoDelectActivity.this, mUri);
                        deleteImage(path);
                        setResult(RESULT_OK, new Intent().setData(null));
                        finish();
                    }
                }, mHandler);
                break;
        }
    }

    private void deleteImage(String imgPath) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=?",
                new String[]{imgPath}, null);
        boolean result = false;
        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uri = ContentUris.withAppendedId(contentUri, id);
            int count = getContentResolver().delete(uri, null, null);
            result = count == 1;
        }
        File file = new File(imgPath);
        if (result && file.exists()) {
            file.delete();
        }
    }
}
