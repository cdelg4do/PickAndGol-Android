package io.keepcoding.pickandgol.manager.image;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.lang.ref.WeakReference;

import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.CACHE_DIR;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.CACHE_SIZE_MB;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.DEBUG_INDICATORS;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.ENABLE_LOGGING;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.S3_BUCKET;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.S3_POOL_ID;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.S3_POOL_REGION;


/**
 * This class manages all the tasks about uploading, downloading, caching and showing images.
 * (it uses Picasso for remote image caching and loading, and AWS S3 for uploading local images)
 */
public class ImageManager {

    private final static String LOG_TAG = "ImageManager";

    // Listener interface for image catching operations
    public interface ImageCachingListener {
        void onImageCachingError();
        void onImageCachingSuccess();
    }

    // Listener interface for image transfer operations
    public interface ImageTransferListener {
        void onProgressChanged(int transferId, long bytesCurrent, long bytesTotal);
        void onImageTransferError(int transferId, Exception e);
        void onImageTransferCompletion(int transferId);
    }

    // Listener interface for image deletion operations
    public interface ImageDeletionListener {
        void onDeletionError(Exception error);
        void onDeletionSuccess();
    }


    private static ImageManager sharedInstance;    // ImageManager is a singleton

    private WeakReference<Context> context;
    private LruCache picassoCache;
    private AmazonS3 s3Client;
    private TransferUtility s3TransferUtility;


    // The constructor is private, call getInstance() to get a reference to the singleton
    private ImageManager(Context context) {

        this.context = new WeakReference<Context>(context);

        // Picasso setup:
        // - Setup a custom memory cache for Picasso, so that we can keep a reference to it
        //   and clean it later, if necessary.
        // - Setup a custom downloader for Picasso, so that we can define an appropriate disk
        //   cache size (Picasso does not manage disk cache directly, the downloader does it).
        // - Configure a custom instance of Picasso, and set it as the default Picasso instance.
        // - Set the logging options.

        picassoCache = new LruCache(context);

        Downloader picassoDownloader = new OkHttp3Downloader(context, CACHE_SIZE_MB * 1024 * 1024);

        Picasso.Builder builder = new Picasso.Builder(context);
        builder.memoryCache(picassoCache);
        builder.downloader(picassoDownloader);
        Picasso.setSingletonInstance(builder.build());

        Picasso.with(context).setLoggingEnabled(ENABLE_LOGGING);
        Picasso.with(context).setIndicatorsEnabled(DEBUG_INDICATORS);


        // Amazon S3 setup:
        // - Initialize Amazon Cognito credentials provider.
        // - Initialize the S3 client & the S3 Transfer Utility.

        CognitoCachingCredentialsProvider credsProvider = new CognitoCachingCredentialsProvider(
                context,
                S3_POOL_ID,
                S3_POOL_REGION
        );

        s3Client = new AmazonS3Client(credsProvider);
        s3TransferUtility = new TransferUtility(s3Client,context);
    }

    /**
     * Gets a reference to the Image Manager.
     *
     * @param context   a context for the manager operations.
     * @return          an initialized reference to the manager.
     */
    public synchronized static ImageManager getInstance(Context context) {

        if (sharedInstance == null)
            sharedInstance = new ImageManager( context.getApplicationContext() );

        return sharedInstance;
    }


    /**
     * Removes both the memory and the disk image caches
     */
    public void clearCache() {

        picassoCache.clear();

        File cacheDir = new File(context.get().getCacheDir(), CACHE_DIR);
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            for (String aFile : cacheDir.list())
                new File(cacheDir, aFile).delete();
        }
    }


    /**
     * Forces the download of a remote image (bypass the local caches) and stores it locally.
     *
     * @param imageUrl  url of the remote image.
     * @param listener  the object which is waiting to take action when the operation finishes
     */
    public void cacheImage(final @NonNull String imageUrl,
                           final @NonNull ImageCachingListener listener) {

        if (listener == null)
            return;

        if (imageUrl == null) {
            listener.onImageCachingError();
            return;
        }

        Log.d(LOG_TAG, "Caching remote image: "+ imageUrl);

        Picasso.with(context.get())
                .load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE)     // Do not use the memory cache here
                .networkPolicy(NetworkPolicy.NO_CACHE)   // Do not look for the image in the disk
                .fetch(new Callback() {

                    @Override
                    public void onError() {
                        Log.e(LOG_TAG,"Failed to cache remote image: "+ imageUrl);
                        listener.onImageCachingError();
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "Successfully cached remote image: "+ imageUrl);
                        listener.onImageCachingSuccess();
                    }
                });
    }


    /**
     * Asynchronously loads a remote image into an ImageView, then calls a listener.
     * (first it will look for the image in the local caches, before looking for it on the internet)
     *
     * @param target        the ImageView to load the image on.
     * @param imageUrl      the url of the image to load.
     * @param placeholderId resource id of the image used as placeholder during the operation.
     * @param brokenImageId resource id of the image to show in case the operation fails.
     * @param listener      listener for the operation.
     */
    public void loadImage(final @NonNull ImageView target,
                          final @NonNull String imageUrl,
                          final @Nullable Integer placeholderId,
                          final @Nullable Integer brokenImageId,
                          final @Nullable Callback listener) {

        Log.d(LOG_TAG, "Loading remote image: "+ imageUrl);

        RequestCreator loadRequest = Picasso.with(context.get()).load(imageUrl);

        if (placeholderId != null)
            loadRequest.placeholder(placeholderId);

        if (brokenImageId != null)
            loadRequest.error(brokenImageId);

        loadRequest.into(target, new Callback() {

            @Override
            public void onError() {
                Log.e(LOG_TAG, "Failed to load remote image: "+ imageUrl);

                if (listener != null)
                    listener.onError();
            }

            @Override
            public void onSuccess() {
                Log.d(LOG_TAG, "Successfully loaded remote image: "+ imageUrl);

                if (listener != null)
                    listener.onSuccess();
            }
        });
    }


    /**
     * Asynchronously loads a remote image into an ImageView, without using any listener.
     * (first it will look for the image in the local caches, before looking for it on the internet)
     *
     * @param target        the ImageView to load the image on.
     * @param imageUrl      the url of the image to load.
     * @param placeholderId resource id of the image used as placeholder during the operation.
     * @param brokenImageId resource id of the image to show in case the operation fails.
     */
    public void loadImage(final @NonNull ImageView target,
                          final @NonNull String imageUrl,
                          final @Nullable Integer placeholderId,
                          final @Nullable Integer brokenImageId) {

        loadImage(target, imageUrl, placeholderId, brokenImageId, null);
    }


    /**
     * Attempts to upload the contents of a given file to the S3 bucket
     * (permissions required: s3:PutObject on the bucket).
     *
     * @param imageFile         the file where the data to upload exists.
     * @param remoteFilename    the remote file name the data will be stored under.
     * @param listener          listener for the transfer operation.
     */
    public void uploadImage(@NonNull File imageFile,
                            @NonNull String remoteFilename,
                            @NonNull final ImageTransferListener listener) {

        String filePath = "";
        try                 {   filePath = imageFile.getCanonicalPath();    }
        catch (Exception e) {   listener.onImageTransferError(-1, e);       }

        TransferObserver observer = s3TransferUtility.upload(S3_BUCKET, remoteFilename, imageFile);
        setTransferListener(observer, listener);

        Log.d(LOG_TAG, "Transferring (id "+ observer.getId() +") local image '"+ filePath +"' to '"+ remoteFilename +"'...");
    }

    /**
     * Attempts to upload the contents of a given file to the S3 bucket
     * (permissions required: s3:ListBucket permission on the bucket,
     *                        s3:GetObject + s3:DeleteObject on the file).
     *
     * @param remoteFilename    the remote file name the data to delete is stored under.
     * @param listener          listener for the deletion operation.
     */
    public void deleteImage(String remoteFilename, ImageDeletionListener listener) {

        new DeleteImageTask(remoteFilename, listener).execute();
    }


    /* Private methods and classes */

    // Auxiliary method: assigns our TransferListener listener to a given S3 TransferObserver object
    private void setTransferListener(TransferObserver observer, final ImageTransferListener listener) {

        if (observer == null || listener == null )
            return;

        observer.setTransferListener(new TransferListener() {

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                listener.onProgressChanged(id, bytesCurrent, bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d(LOG_TAG, "Error transferring image (id "+ id +"): "+ ex.toString());
                listener.onImageTransferError(id, ex);
            }

            @Override
            public void onStateChanged(int id, TransferState state) {

                switch(state) {

                    case CANCELED:
                        Log.d(LOG_TAG, "Image transfer canceled (id "+ id +")");
                        listener.onImageTransferError(id, new Exception("Transfer cancelled"));
                        break;

                    case FAILED:
                        Log.d(LOG_TAG, "Image transfer failed (id "+ id +")");
                        listener.onImageTransferError(id, new Exception("Transfer failed"));
                        break;

                    case COMPLETED:
                        Log.d(LOG_TAG, "Image transfer completed (id "+ id +")");
                        listener.onImageTransferCompletion(id);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    // Auxiliary class: AsyncTask to delete a remote file in background
    private class DeleteImageTask extends AsyncTask<Void, Void, Void> {

        private String remoteFilename;
        private ImageDeletionListener listener;
        private Exception error;

        public DeleteImageTask(String remoteFilename, ImageDeletionListener listener) {
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

}
