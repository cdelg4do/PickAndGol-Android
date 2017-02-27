package io.keepcoding.pickandgol.manager.image;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.services.s3.AmazonS3;

import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.S3_BUCKET;


/**
 * This class is in charge of the delete operation of AWS S3 remote images in background.
 * It is an auxiliary class of ImageManager, and has package-private visibility.
 */
class ImageDeleter extends AsyncTask<Void, Void, Void> {

    private final static String LOG_TAG = "ImageDeleter";

    private AmazonS3 s3Client;
    private String remoteFilename;
    private ImageManager.ImageDeletionListener listener;
    private Exception error;

    ImageDeleter(String remoteFilename, @NonNull AmazonS3 s3Client, ImageManager.ImageDeletionListener listener) {
        this.s3Client = s3Client;
        this.remoteFilename = remoteFilename;
        this.listener = listener;
        error = null;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... inputs) {

        Log.d(LOG_TAG, "Deleting remote image '"+ remoteFilename +"'...");

        try {
            boolean objectExists = s3Client.doesObjectExist(S3_BUCKET, remoteFilename);

            if (objectExists)
                s3Client.deleteObject(S3_BUCKET, remoteFilename);
            else
                error = new Exception("The remote file does not exist.");
        }
        catch (Exception e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        if ( error != null) {
            Log.d(LOG_TAG, "Error deleting remote image '"+ remoteFilename +"': "+ error.toString());
            listener.onDeletionError(error);
        }

        else {
            Log.d(LOG_TAG, "The remote image '"+ remoteFilename +"' has been deleted");
            listener.onDeletionSuccess();
        }
    }
}
