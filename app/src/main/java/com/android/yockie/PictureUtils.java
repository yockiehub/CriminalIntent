package com.android.yockie;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by palme_000 on 08/03/16.
 */
public class PictureUtils {
    /**
     * Get a BitmapDrawable from a local file that is scaled down to
     * fit the current window size
     */
    @SuppressWarnings("deprecation")
    public static BitmapDrawable getScaledDrawable (Activity a, String path){
        Display display = a.getWindowManager().getDefaultDisplay();
        float destWidth = display.getWidth();
        float destHeight = display.getHeight();

        //Read in the dimensions of the picture on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight){
                inSampleSize = Math.round(srcHeight/destHeight);
            }else{
                inSampleSize = Math.round(srcWidth/destWidth);
            }
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return new BitmapDrawable(a.getResources(), bitmap);
    }

    public static void cleanImageView(ImageView imageView){
        if(!(imageView.getDrawable() instanceof BitmapDrawable)){
            return;
        }
        //Clean up the view's image for the sake of memory
        BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();
        if (b.getBitmap() != null)
            b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }

    public static byte[] rotatePicture(byte[] data, CrimeCameraFragment.Orientation orientation) {
        // set options, in case of OutOfMemoryError
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 5;

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                matrix.postRotate(90);
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                matrix.postRotate(270);
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                matrix.postRotate(0);
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                matrix.postRotate(180);
                break;
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        return out.toByteArray();
    }
}
