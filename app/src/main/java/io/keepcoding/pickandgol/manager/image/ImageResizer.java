package io.keepcoding.pickandgol.manager.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_COMPRESS;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_MAX_HEIGHT;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_MAX_WIDTH;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_TEMP_OUTPUT;


/**
 * This class manages all the resize/rotate operations for local image files in background.
 * It is an auxiliary class of ImageManager, and has package-private visibility.
 */
class ImageResizer extends AsyncTask<Void, Void, Void> {

    private final static String LOG_TAG = "ImageResizer";

    private File sourceFile;
    private ImageManager.ImageResizeListener listener;
    private Exception error;
    private boolean useSourceFile;

    ImageResizer(File sourceFile, ImageManager.ImageResizeListener listener) {
        this.sourceFile = sourceFile;
        this.listener = listener;
        error = null;

        useSourceFile = false;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... inputs) {

        if( !sourceFile.isFile() ) {
            error = new Exception("File does not exist or is inaccessible");
            return null;
        }

        Bitmap sourceImage = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
        int sourceHeight = sourceImage.getHeight();
        int sourceWidth = sourceImage.getWidth();

        if (sourceHeight <= RESIZE_MAX_HEIGHT && sourceWidth <= RESIZE_MAX_WIDTH) {
            useSourceFile = true;
            return null;
        }

        float widthRatio  = (float) RESIZE_MAX_WIDTH  / sourceWidth;
        float heightRatio = (float) RESIZE_MAX_HEIGHT / sourceHeight;

        int newHeight, newWidth;
        if(widthRatio > heightRatio) {
            newHeight = (int) (sourceHeight * heightRatio);
            newWidth = (int) (sourceWidth * heightRatio);
        }
        else {
            newHeight = (int) (sourceHeight * widthRatio);
            newWidth = (int) (sourceWidth * widthRatio);
        }

        Log.d(LOG_TAG, "Resizing image: "+
                "("+sourceWidth+", "+sourceHeight+") --> ("+newWidth+", "+newHeight+")");

        Bitmap resized = Bitmap.createScaledBitmap(sourceImage, newWidth, newHeight, true);

        // The resized image does not contain Exif rotation info, we get it from the original
        Bitmap rotatedResized = rotateIfNeeded(resized, sourceFile.getAbsolutePath());

        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(RESIZE_TEMP_OUTPUT);
            rotatedResized.compress(Bitmap.CompressFormat.JPEG, RESIZE_COMPRESS, outStream);
            outStream.close();
        }
        catch (Exception e) {
            error = e;
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        if ( error != null) {
            Log.d(LOG_TAG, "Error resizing local image '"+ sourceFile.getAbsolutePath()
                    +"': "+ error.toString());

            listener.onResizeError(error);
        }
        else if (useSourceFile) {
            Log.d(LOG_TAG, "The local image '"+ sourceFile.getAbsolutePath()
                    +"' did not require resizing");

            listener.onResizeSuccess(sourceFile);
        }
        else {
            Log.d(LOG_TAG, "The local image '"+ sourceFile.getAbsolutePath()
                    +"' was resized and stored at '"+ RESIZE_TEMP_OUTPUT +"'");

            listener.onResizeSuccess( new File(RESIZE_TEMP_OUTPUT) );
        }
    }

    // Returns a rotated bitmap from the given one,
    // depending on the Exif rotation info stored in the given file path.
    private Bitmap rotateIfNeeded(Bitmap image, String imagePath) {

        ExifInterface ei;
        try                     {   ei = new ExifInterface(imagePath);  }
        catch (IOException e)   {   return image;                       }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        );

        int degrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:   degrees = 90;   break;
            case ExifInterface.ORIENTATION_ROTATE_180:  degrees = 180;  break;
            case ExifInterface.ORIENTATION_ROTATE_270:  degrees = 270;  break;
            default: break;
        }

        // If no rotation needed, return the original bitmap
        if (degrees == 0)
            return image;

        // Create the rotation matrix and apply it to the bitmap
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate(degrees);

        Log.d(LOG_TAG, "Rotating image  ("+degrees+" degrees)...");

        Bitmap rotatedImg = Bitmap.createBitmap(image, 0, 0,
                image.getWidth(),
                image.getHeight(),
                rotationMatrix,
                true
        );

        image.recycle();
        return rotatedImg;
    }

}
