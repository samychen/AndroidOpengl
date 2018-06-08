/**
 * Name        : MagicImageView.java
 * Copyright   : Copyright (c) Tencent Inc. All rights reserved.
 * Description : TODO
 */
package com.spore.meitu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author ianmao
 *
 */
public class MagicImageView extends ImageView {
    float mScale = 1;
    public float getScale() {
        return mScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    float dis_x = 0;
    float dis_y = 0;
    
    public float getDis_x() {
        return dis_x;
    }

    public void setDis_x(float dis_x) {
        this.dis_x = dis_x;
    }

    public float getDis_y() {
        return dis_y;
    }

    public void setDis_y(float dis_y) {
        this.dis_y = dis_y;
    }

    
    /**
     * @param context
     * @param attrs
     */
    public MagicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
    public void img_scale(float scale){
        mScale *= scale;
        if(mScale >= 2)
            mScale = 2;
        if(mScale <= 0.5)
            mScale = 0.5f;
        
        this.invalidate();
    }
    
    public void img_transport(float x,float y){
        dis_x += x;
        dis_y += y;
        if(dis_x >= 400)
            dis_x = 400;
        else if(dis_x <= -400)
            dis_x = -400;
        if(dis_y >= 200)
            dis_y = 200;
        else if (dis_y <= -200)
            dis_y = -200;
            
        this.invalidate();
    }

    protected void onDraw(Canvas canvas){   
        
        Matrix cm = new Matrix();
        
        float[] array = {1 * mScale,0,dis_x,
                0,1 * mScale,dis_y,
                0,0,1};
        cm.setValues(array);
        canvas.setMatrix(cm);
        super.onDraw(canvas);
    }
}
