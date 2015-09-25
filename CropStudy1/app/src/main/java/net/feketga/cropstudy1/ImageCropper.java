package net.feketga.cropstudy1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCropper {

    private static final String TAG = ImageCropper.class.getSimpleName();

    public static void saveCropToFile(String imageFilePath,
                                      RectF cropRectangle,
                                      String croppedImageFilePath) {
        Bitmap croppedImage = extractCroppedArea(imageFilePath, cropRectangle);

        File croppedImageFile = new File(croppedImageFilePath);
        createCropDirectory(croppedImageFile);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(croppedImageFile);
            croppedImage.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
        } catch (FileNotFoundException e) {
            // TODO
        } catch (IOException e) {
            // TODO
        }

        if (out != null) {
            try {
                out.close();
                // TODO:  Report?
            } catch (IOException e) {
                // TODO
            }
        }

        croppedImage.recycle();
    }

    public static Bitmap extractCroppedArea(String imageFilePath,
                                            RectF cropRectangle) {
        InputStream inputStream = null;
        Bitmap croppedImage = null;
        try {
            inputStream = new FileInputStream(imageFilePath);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(inputStream, false);
            final int width = decoder.getWidth();
            final int height = decoder.getHeight();
            Rect rectangle = new Rect();
            rectangle.top = Math.max(0, Math.round(cropRectangle.top));
            rectangle.bottom = Math.min(height, Math.round(cropRectangle.bottom));
            rectangle.left = Math.max(0, Math.round(cropRectangle.left));
            rectangle.right = Math.min(width, Math.round(cropRectangle.right));

            croppedImage = decoder.decodeRegion(rectangle, new BitmapFactory.Options());

        } catch (FileNotFoundException e) {
            // TODO
        } catch (IOException e) {
            // TODO
        } catch (OutOfMemoryError e) {
            // TODO
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO
                }
            }
        }
        return croppedImage;
    }

    private static void createCropDirectory(File croppedImageFile) {
        File cropDirectory = croppedImageFile.getParentFile();
        if (!cropDirectory.exists()) {
            if (!cropDirectory.mkdirs()) {
                Log.d(TAG, "Cannot create directory: " + cropDirectory.getAbsolutePath());
                return;
            }
        }
    }
}
