package com.learnopengles.android.render;

import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.view.TextureView;

public class TextureViewHelper {
    public static void adjustAspectRatio(@NonNull TextureView textureView, int videoWidth, int videoHeight) {
        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;

        Matrix matrix = new Matrix();
        textureView.getTransform(matrix);
        matrix.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        matrix.postTranslate(xoff, yoff);
        textureView.setTransform(matrix);
    }
}
