package com.cam001.photoeditor.filter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;

import com.cam001.gles.FBO;
import com.cam001.gles.GLEnv;
import com.cam001.gles.ShaderUtil;
import com.cam001.gles.Texture;
import com.cam001.photoeditor.resource.Filter;
import com.cam001.util.FileUtil;
import com.cam001.util.ImageUtil;

/**
 * Created by hzhao on 15/6/4.
 */
public class FilterEngine {

    public void process(final Bitmap in, final Bitmap out, final Filter filter) {
        GLEnv gl = new GLEnv();
        gl.queueEvent(new Runnable() {
            @Override
            public void run() {
                FilterProgram program = new FilterProgram(filter);
                Texture texImage = new Texture();
                texImage.load(in);

                int width = out.getWidth();
                int height = out.getHeight();
                FBO fbo = new FBO();
                fbo.initFBO();
                fbo.setTexSize(width, height);
                fbo.bindFrameBuffer();

                GLES20.glViewport(0, 0, width, height);
                program.setFilterParams();
                program.setStrength(1.0f);
                program.setImageTexture(texImage);
                program.draw();
                ShaderUtil.glReadPixelsToBitmap(out);

                fbo.unbindFrameBuffer();
                fbo.uninitFBO();

//                program.readPixels(out);
                program.recycle();
                texImage.recycle();
            }
        });
        gl.destroy();
    }

    public void process(final Context c, final Uri uri, final Filter filter) {
        GLEnv gl = new GLEnv();
        gl.queueEvent(new Runnable() {
            @Override
            public void run() {
                byte[] data = ImageUtil.readBuffer(c, uri);
                FilterProgram program = new FilterProgram(filter);
                Texture texImage = new Texture();
                texImage.load(data);

                int width = texImage.getWidth();
                int height = texImage.getHeight();
                FBO fbo = new FBO();
                fbo.initFBO();
                fbo.setTexSize(width, height);
                fbo.bindFrameBuffer();

                GLES20.glViewport(0, 0, width, height);
                program.setFilterParams();
                program.setStrength(1.0f);
                program.setImageTexture(texImage);
                program.draw();
                data = ShaderUtil.glReadPixelsToJpeg(width, height, data);

                fbo.unbindFrameBuffer();
                fbo.uninitFBO();

//                program.readPixels(out);
                program.recycle();
                texImage.recycle();
                try {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    String path = FileUtil.getPath((Activity) c, uri);
                   ImageUtil.save(c, uri, bmp, path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        gl.destroy();
    }

}
