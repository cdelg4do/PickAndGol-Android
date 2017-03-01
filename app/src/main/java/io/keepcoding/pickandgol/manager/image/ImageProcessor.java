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

import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.COMPRESS_FORMAT;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.COMPRESS_QUALITY;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_MAX_HEIGHT;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_MAX_WIDTH;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.PROCESSOR_TEMP_DIR;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.PROCESSOR_TEMP_FILENAME;


/**
 * This class manages all the resize/rotate operations for local image files in background.
 * It is an auxiliary class of ImageManager, and has package-private visibility.
 */
class ImageProcessor extends AsyncTask<Void, Void, Void> {

    private final static String LOG_TAG = "ImageProcessor";

    private File sourceFile;
    private ImageManager.ImageResizeListener listener;
    private Exception error;
    private String tempFilePath;

    ImageProcessor(File sourceFile, ImageManager.ImageResizeListener listener) {
        this.sourceFile = sourceFile;
        this.listener = listener;
        error = null;

        try                 {   tempFilePath = new File(PROCESSOR_TEMP_DIR.getPath(),
                PROCESSOR_TEMP_FILENAME
                                                   ).getCanonicalPath();    }

        catch (Exception e) {   tempFilePath = null;    }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... inputs) {

        Log.d(LOG_TAG, "Processing image file '"+ sourceFile.getAbsolutePath() +"'...");

        if( !sourceFile.isFile() ) {
            error = new Exception("File does not exist or is inaccessible");
            return null;
        }

        Bitmap sourceImage = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
        Bitmap resized = resizeIfNeeded(sourceImage);

        // The resized image does not contain Exif rotation info, we get it from the original file
        Bitmap rotatedResized = rotateIfNeeded(resized, sourceFile.getAbsolutePath());

        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(tempFilePath);
            rotatedResized.compress(COMPRESS_FORMAT, COMPRESS_QUALITY, outStream);
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
            Log.e(LOG_TAG, "Error processing the image : "+ error.toString());
            listener.onResizeError(error);
        }
        else {
            Log.d(LOG_TAG, "The processed image has been saved to '"+ tempFilePath +"'");
            listener.onResizeSuccess( new File(tempFilePath) );
        }
    }

    // Returns a bitmap that fits into the max dimensions, scaling the given one if necessary
    private Bitmap resizeIfNeeded(Bitmap image) {

        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();

        // If the source image already fits into the max dimensions, do not resize
        if (imageHeight <= RESIZE_MAX_HEIGHT && imageWidth <= RESIZE_MAX_WIDTH)
            return image;


        float widthRatio  = (float) RESIZE_MAX_WIDTH  / imageWidth;
        float heightRatio = (float) RESIZE_MAX_HEIGHT / imageHeight;

        int newHeight, newWidth;
        if(widthRatio > heightRatio) {
            newHeight = (int) (imageHeight * heightRatio);
            newWidth = (int) (imageWidth * heightRatio);
        }
        else {
            newHeight = (int) (imageHeight * widthRatio);
            newWidth = (int) (imageWidth * widthRatio);
        }

        Log.d(LOG_TAG, "Resizing image "+
                "("+imageWidth +"x"+ imageHeight+") --> ("+newWidth +"x"+ newHeight+")");

        Bitmap resized = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);

        image.recycle();
        return resized;
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

        Log.d(LOG_TAG, "Rotating image "+ degrees +" degrees");

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
