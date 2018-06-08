package com.spore.meitu;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

public class ImageUtil
{

	// 放大缩小图片
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return newbmp;
	}

	// Drawable to Bitmap
	public static Bitmap drawableToBitmap(Drawable drawable)
	{
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	// 圆角化
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx)
	{

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	// 倒影
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap)
	{
		final int reflectionGap = 4;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
				width, height / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}

	public static Bitmap createShadowBitmap(Bitmap bitmap)
	{
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);

		Canvas canvas = new Canvas(output);
		canvas.drawColor(Color.WHITE);
		// canvas.save(Canvas.MATRIX_SAVE_FLAG);

		int PicWidth = bitmap.getWidth();
		int PicHegiht = bitmap.getHeight();
		int posX = 20;
		int posY = 50;

		Rect rect = new Rect(0, 0, PicWidth, PicHegiht);

		Paint paint = new Paint();// ��ʼ�����ʣ�Ϊ������ӰЧ��ʹ�á�
		paint.setAntiAlias(true);// ȥ���ݡ�
		// paint.setColor(Color.CYAN);
		paint.setShadowLayer(10f, 10.0f, 10.0f, Color.BLACK);// ������Ӱ�㣬���ǹؼ�
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		RectF rectF = new RectF(rect);
		// canvas.drawRoundRect(rectF,20,20, paint);
		// canvas.drawRect(rectF, paint);
		// canvas.drawBitmap(bitmap, 2, 2, null);//����ԭͼ��
		return output;
	}

	// 灰度
	public static Bitmap toGrayscale(Bitmap bmpOriginal)
	{
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	// 浮雕
	public static Bitmap toFuDiao(Bitmap mBitmap)
	{
		Paint mPaint;

		int mBitmapWidth = 0;
		int mBitmapHeight = 0;

		int mArrayColor[] = null;
		int mArrayColorLengh = 0;
		long startTime = 0;
		int mBackVolume = 0;

		mBitmapWidth = mBitmap.getWidth();
		mBitmapHeight = mBitmap.getHeight();

		Bitmap bmpReturn = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
				Bitmap.Config.RGB_565);
		mArrayColorLengh = mBitmapWidth * mBitmapHeight;
		int count = 0;
		int preColor = 0;
		int prepreColor = 0;
		int color = 0;
		preColor = mBitmap.getPixel(0, 0);

		for (int i = 0; i < mBitmapWidth; i++)
		{
			for (int j = 0; j < mBitmapHeight; j++)
			{
				int curr_color = mBitmap.getPixel(i, j);
				int r = Color.red(curr_color) - Color.red(prepreColor) + 128;
				int g = Color.green(curr_color) - Color.red(prepreColor) + 128;
				int b = Color.green(curr_color) - Color.blue(prepreColor) + 128;
				int a = Color.alpha(curr_color);
				int modif_color = Color.argb(a, r, g, b);
				bmpReturn.setPixel(i, j, modif_color);
				prepreColor = preColor;
				preColor = curr_color;
			}
		}

		Canvas c = new Canvas(bmpReturn);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpReturn, 0, 0, paint);

		return bmpReturn;
	}

	/* 模糊 */
	public static Bitmap toMohu(Bitmap bmpSource, int Blur)
	{
		Bitmap bmpReturn = Bitmap.createBitmap(bmpSource.getWidth(),
				bmpSource.getHeight(), Bitmap.Config.ARGB_8888);
		int pixels[] = new int[bmpSource.getWidth() * bmpSource.getHeight()];
		int pixelsRawSource[] = new int[bmpSource.getWidth()
				* bmpSource.getHeight() * 3];
		int pixelsRawNew[] = new int[bmpSource.getWidth()
				* bmpSource.getHeight() * 3];

		bmpSource.getPixels(pixels, 0, bmpSource.getWidth(), 0, 0,
				bmpSource.getWidth(), bmpSource.getHeight());

        for (int k = 1; k <= Blur; k++) 
        {
			// ��ͼƬ�л�ȡÿ��������ԭɫ��ֵ
			for (int i = 0; i < pixels.length; i++)
			{
				pixelsRawSource[i * 3 + 0] = Color.red(pixels[i]);
				pixelsRawSource[i * 3 + 1] = Color.green(pixels[i]);
				pixelsRawSource[i * 3 + 2] = Color.blue(pixels[i]);
			}
			// ȡÿ����������ҵ��ƽ��ֵ���Լ���ֵ
			int CurrentPixel = bmpSource.getWidth() * 3 + 3;
			// ��ǰ��������ص㣬�ӵ�(2,2)��ʼ
			for (int i = 0; i < bmpSource.getHeight() - 3; i++) // �߶�ѭ��
			{
				for (int j = 0; j < bmpSource.getWidth() * 3; j++) // ���ѭ��
				{
					CurrentPixel += 1; // ȡ�������ң�ȡƽ��ֵ
					int sumColor = 0; // ��ɫ��
					sumColor = pixelsRawSource[CurrentPixel
							- bmpSource.getWidth() * 3]; // ��һ��
					sumColor = sumColor + pixelsRawSource[CurrentPixel - 3]; // ��һ��
					sumColor = sumColor + pixelsRawSource[CurrentPixel + 3]; // ��һ��
					sumColor = sumColor
							+ pixelsRawSource[CurrentPixel
									+ bmpSource.getWidth() * 3]; // ��һ��
					pixelsRawNew[CurrentPixel] = Math.round(sumColor / 4); // �������ص�
				}
			}

			for (int i = 0; i < pixels.length; i++)
			{
				pixels[i] = Color.rgb(pixelsRawNew[i * 3 + 0],
						pixelsRawNew[i * 3 + 1], pixelsRawNew[i * 3 + 2]);
			}
		}

		bmpReturn.setPixels(pixels, 0, bmpSource.getWidth(), 0, 0,
				bmpSource.getWidth(), bmpSource.getHeight()); // �����½�λͼȻ����䣬����ֱ�����Դͼ�񣬷����ڴ汨��
		return bmpReturn;
	}

	/* 积木 */
	public static Bitmap toHeibai(Bitmap mBitmap)
	{
		Paint mPaint;

		int mBitmapWidth = 0;
		int mBitmapHeight = 0;

		int mArrayColor[] = null;
		int mArrayColorLengh = 0;
		long startTime = 0;
		int mBackVolume = 0;

		mBitmapWidth = mBitmap.getWidth();
		mBitmapHeight = mBitmap.getHeight();
		Bitmap bmpReturn = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight,
				Bitmap.Config.ARGB_8888);
		mArrayColorLengh = mBitmapWidth * mBitmapHeight;
		int count = 0;
		int preColor = 0;
		int color = 0;

		int iPixel = 0;
		for (int i = 0; i < mBitmapWidth; i++)
		{
			for (int j = 0; j < mBitmapHeight; j++)
			{
				int curr_color = mBitmap.getPixel(i, j);

				int avg = (Color.red(curr_color) + Color.green(curr_color) + Color
						.blue(curr_color)) / 3;
				if (avg >= 100)
				{
					iPixel = 255;
				}
				else
				{
					iPixel = 0;
				}
				int modif_color = Color.argb(255, iPixel, iPixel, iPixel);

				bmpReturn.setPixel(i, j, modif_color);
			}
		}
		return bmpReturn;
	}

	/* 油画*/
	public static Bitmap toYouHua(Bitmap bmpSource)
	{
		Bitmap bmpReturn = Bitmap.createBitmap(bmpSource.getWidth(),
				bmpSource.getHeight(), Bitmap.Config.RGB_565);
		int color = 0;
		int Radio = 0;
		int width = bmpSource.getWidth();
		int height = bmpSource.getHeight();

		Random rnd = new Random();
		int iModel = 10;
		int i = width - iModel;
		while (i > 1)
		{
			int j = height - iModel;
			while (j > 1)
			{
				int iPos = rnd.nextInt(100000) % iModel;
				color = bmpSource.getPixel(i + iPos, j + iPos);
				bmpReturn.setPixel(i, j, color);
				j = j - 1;
			}
			i = i - 1;
		}
		return bmpReturn;
	}
	
	public static Bitmap testBitmap(Bitmap bitmap)
    {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.RGB_565);

        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();        
        ColorMatrix cm = new ColorMatrix();
        float[] array = {1,0,0,0,50,
                0,1,0,0,50,
                0,0,1,0,0,
                0,0,0,1,0};
        cm.set(array);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));

        canvas.drawBitmap(bitmap, 0, 0, paint);
        return output;
    }
	
	public static Bitmap scaleBitmap(Bitmap bitmap,float scale)
    {
        Bitmap output = Bitmap.createBitmap((int)(bitmap.getWidth() * scale),
                (int)(bitmap.getHeight() * scale), Config.RGB_565);

        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();        
        Matrix cm = new Matrix();
        
        float[] array = {1 * scale,0,0,
                0,1 * scale,0,
                0,0,1};
        cm.setValues(array);
        canvas.drawBitmap(bitmap, cm, paint);
        bitmap.recycle();
        return output;
    }
}