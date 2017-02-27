package io.keepcoding.pickandgol.manager.image;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.RESIZE_TEMP_OUTPUT;


/**
 * This class is in charge of all image saving operations in background.
 * It is an auxiliary class of ImageManager, and has package-private visibility.
 */
class ImageSaver extends AsyncTask<Void, Void, Void> {

    private final static String LOG_TAG = "ImageSaver";

    private Bitmap sourceBitmap;
    private String destinationPath;
    private ImageManager.ImageSavingListener listener;
    private Exception error;

    private File savedFile;

    ImageSaver(Bitmap sourceBitmap, String destinationPath, ImageManager.ImageSavingListener listener) {
        this.sourceBitmap = sourceBitmap;
        this.destinationPath = destinationPath;
        this.listener = listener;
        error = null;
        savedFile = null;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... inputs) {

        Log.d(LOG_TAG, "Saving bitmap to '"+ destinationPath +"'...");

        try {
            savedFile = new File(destinationPath);
            FileOutputStream outStream = new FileOutputStream(savedFile);
            sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        }
        catch (Exception e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        if ( error != null) {
            Log.e(LOG_TAG, "Error saving bitmap to '"+ destinationPath +"': "+ error.toString());
            listener.onSavingError(error);
        }

        else {
            Log.d(LOG_TAG, "The bitmap has been saved to '"+ destinationPath +"'");
            listener.onSavingSuccess(savedFile);
        }
    }
}
