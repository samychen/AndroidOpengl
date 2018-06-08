package com.cam001.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by hzhao on 15/6/4.
 */
public class AdjustImageView extends ImageView {

    private ColorMatrix mColorMatrix = new ColorMatrix();
    private Paint mPaint = new Paint();
    private Bitmap mImage = null;

    private float mBrightness = 0.0f; //-50 ~ 50
    private float mSaturation = 1.0f; //0 ~ 2
    private float mContrast = 1.0f; //0.5 ~ 3

    public AdjustImageView(Context context) {
        super(context);
    }

    public AdjustImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageBitmap(Bitmap image) {
        mImage = image;
        super.setImageBitmap(image);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mImage==null || mImage.isRecycled()) return;

        mColorMatrix.reset();
        setColorParam(mColorMatrix, mBrightness, mSaturation);
        setColorParam(mColorMatrix, mContrast);
        mPaint.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
        canvas.drawBitmap(mImage, getImageMatrix(), mPaint);
    }

    private void setColorParam(ColorMatrix cm, float light, float saturation) {
        ColorMatrix cm1 = new ColorMatrix();
        final float invSat = 1 - saturation;
        final float R = 0.213f * invSat;
        final float G = 0.715f * invSat;
        final float B = 0.072f * invSat;

        cm1.set(new float[]{R + saturation, G, B, 0, light, R,
                G + saturation, B, 0, light, R, G, B + saturation, 0, light, 0,
                0, 0, 1, 0});
        cm.postConcat(cm1);
    }

    private void setColorParam(ColorMatrix cm, float contrast) {
        ColorMatrix cm1 = new ColorMatrix();
        cm1.set(new float[]{contrast, 0, 0, 0, 128 - 128 * contrast, 0,
                contrast, 0, 0, 128 - 128 * contrast, 0, 0, contrast, 0,
                128 - 128 * contrast, 0, 0, 0, 1, 0});
        cm.postConcat(cm1);
    }

    /**
     * 0 ~ 100
     * @param progress
     */
    public void setBrightness(int progress) {
        mBrightness = progress - 50;
    }

    /**
     * 0 ~ 100
     * @param progress
     */
    public void setSaturation(int progress) {
        mSaturation = progress/50.0f;
    }

    /**
     * 0 ~ 100
     * @param progress
     */
    public void setContrast(int progress) {
        if(progress>50) {
            mContrast = 2.0f*(progress-50)/50.0f + 1.0f;
        } else {
            mContrast = 1.0f - (50-progress)/100.f;
        }
    }

    public float[] getColorMatrix() {
        ColorMatrix mat = new ColorMatrix();
        setColorParam(mat, mBrightness, mSaturation);
        setColorParam(mat, mContrast);
        return mat.getArray();
    }
}
