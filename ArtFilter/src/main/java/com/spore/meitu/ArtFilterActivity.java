
package com.spore.meitu;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;

import com.spore.meitu.jni.ImageUtilEngine;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ArtFilterActivity extends Activity implements OnGestureListener {
    Gallery mGallery;

    MagicImageView mImage;

    ImageAdapter mImageAdapter;

    FilterAdapter mFilterAdapter;

    TextView mCurr_filter_text;

    ImageUtilEngine imageEngine;

    GestureDetector mGestureDetector;

    float mCurrentScale;

    float last_x = -1;

    float last_y = -1;

    boolean move = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_process_gallery_panel);

        mImage = (MagicImageView) findViewById(R.id.image);
        mGallery = (Gallery) findViewById(R.id.image_gallery);
        mImageAdapter = new ImageAdapter(this);
        mGallery.setAdapter(mImageAdapter);
        mGallery.setOnItemClickListener(new OnItemClickListener() {
            // 点击监听事件
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                mImage.setImageResource(mImageAdapter.getImgId(position));
                mImageAdapter.setCurrPos(position);
            }
        });
        mImage.setImageResource(mImageAdapter.getImgId(mImageAdapter.getCurrPos()));

        mGestureDetector = new GestureDetector(this);
        mImage.setLongClickable(true);
        mImage.setOnTouchListener(new OnTouchListener() {
            float baseValue;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                // return ArtFilterActivity.this.mGestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    baseValue = 0;
                    float x = last_x = event.getRawX();
                    float y = last_y = event.getRawY();
                    move = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getPointerCount() == 2) {
                        float x = event.getX(0) - event.getX(1);
                        float y = event.getY(0) - event.getY(1);
                        float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
                        if (baseValue == 0) {
                            baseValue = value;
                        } else {
                            if (value - baseValue >= 10 || value - baseValue <= -10) {
                                float scale = value / baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
                                img_scale(scale);
                            }

                        }
                    } else if (event.getPointerCount() == 1) {
                        float x = event.getRawX();
                        float y = event.getRawY();
                        x -= last_x;
                        y -= last_y;
                        if (x >= 10 || y >= 10 || x <= -10 || y <= -10)
                            img_transport(x, y);
                        last_x = event.getRawX();
                        last_y = event.getRawY();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                }
                return true;
            }
        });

        GridView glv = (GridView) findViewById(R.id.filter_view);
        glv.setNumColumns(1);
        glv.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mFilterAdapter = new FilterAdapter(this);
        glv.setAdapter(mFilterAdapter);

        imageEngine = new ImageUtilEngine();
        Toast.makeText(this, imageEngine.getResultFromJni(), Toast.LENGTH_LONG).show();
    }

    public void img_scale(float scale) {
        mImage.img_scale(scale);
    }

    public void img_transport(float x, float y) {
        mImage.img_transport(x, y);
    }

    public void choose_filter(int index) {
        mImage.setImageResource(mImageAdapter.getImgId(mImageAdapter.getCurrPos()));
        String filter_name = mFilterAdapter.getItem(index).toString();
        Bitmap bitmap = ImageUtil.drawableToBitmap(mImage.getDrawable());
        if (filter_name == "圆角") {
            bitmap = ImageUtil.getRoundedCornerBitmap(bitmap, 30);
        } else if (filter_name == "倒影") {
            bitmap = ImageUtil.createReflectionImageWithOrigin(bitmap);
        } else if (filter_name == "灰度") {
            bitmap = ImageUtil.toGrayscale(bitmap);
        } else if (filter_name == "灰度J") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toGray(buf, width, height);
            bitmap = Bitmap.createBitmap(result, width, height, Config.RGB_565);
            result = null;
        } else if (filter_name == "浮雕") {
            bitmap = ImageUtil.toFuDiao(bitmap);
        } else if (filter_name == "浮雕J") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            // for(int i=0;i<width;++i)
            // for(int j=0;j<height;++j)
            // buf[j*height+i] = bitmap.getPixel(i, j);
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toFudiao(buf, width, height);
            bitmap = Bitmap.createBitmap(result, width, height, Config.RGB_565);
            result = null;
        } else if (filter_name == "模糊") {
            bitmap = ImageUtil.toMohu(bitmap, 20);
        } else if (filter_name == "模糊J") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toMohu(buf, width, height, 10);
            bitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
            result = null;
        } else if (filter_name == "黑白") {
            bitmap = ImageUtil.toHeibai(bitmap);
        } else if (filter_name == "黑白J") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toHeibai(buf, width, height);
            bitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
            result = null;
        } else if (filter_name == "油画") {
            bitmap = ImageUtil.toYouHua(bitmap);
        } else if (filter_name == "底片") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toDipian(buf, width, height);
            bitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
            result = null;
        } else if (filter_name == "光照") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            Math.pow(2, 1);
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toSunshine(buf, width, height, 100, 100, 20, 150);
            bitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
            result = null;
        } else if (filter_name == "泛黄") {
            bitmap = ImageUtil.testBitmap(bitmap);
        } else if (filter_name == "放大镜") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            Math.pow(2, 1);
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toFangdajing(buf, width, height, width / 2, height / 2, 100, 2);
            bitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
            result = null;
        } else if (filter_name == "哈哈镜") {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] buf = new int[width * height];
            Math.pow(2, 1);
            bitmap.getPixels(buf, 0, width, 1, 1, width - 1, height - 1);
            int[] result = imageEngine.toHahajing(buf, width, height, width / 2, height / 2, 100, 2);
            bitmap = Bitmap.createBitmap(result, width, height, Config.ARGB_8888);
            result = null;
        }

        mImage.setImageBitmap(bitmap);
    }

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
     */
    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        // Toast.makeText(this, "Down", Toast.LENGTH_SHORT).show();
        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent,
     * android.view.MotionEvent, float, float)
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        // Toast.makeText(this, "Fling", Toast.LENGTH_SHORT).show();
        return false;
    }

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
     */
    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        // Toast.makeText(this, "LongPress", Toast.LENGTH_SHORT).show();
    }

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent,
     * android.view.MotionEvent, float, float)
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        // Toast.makeText(this, "Scroll", Toast.LENGTH_SHORT).show();
        // img_transport(distanceX, distanceY);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
     */
    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        // Toast.makeText(this, "ShowPress", Toast.LENGTH_SHORT).show();
    }

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }
}
