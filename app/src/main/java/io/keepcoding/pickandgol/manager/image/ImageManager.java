package io.keepcoding.pickandgol.manager.image;

import android.content.Context;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;

import static com.amazonaws.regions.Regions.US_EAST_1;
import static io.keepcoding.pickandgol.manager.image.ImageManager.ImageTransferStatus.CANCELED;
import static io.keepcoding.pickandgol.manager.image.ImageManager.ImageTransferStatus.COMPLETED;
import static io.keepcoding.pickandgol.manager.image.ImageManager.ImageTransferStatus.FAILED;
import static io.keepcoding.pickandgol.manager.image.ImageManager.ImageTransferStatus.IN_PROGRESS;
import static io.keepcoding.pickandgol.manager.image.ImageManager.ImageTransferStatus.WAITING;


public class ImageManager {

    // Manager settings
    private static final String CACHE_DIR_NAME = "picasso-cache";
    private static final int IMAGE_MANAGER_DISK_CACHE_SIZE_MB = 50;
    private static final String AMAZON_S3_IMAGE_BUCKET = "pickandgol-images";
    private static final String AMAZON_S3_IDENTITY_POOL_ID = "us-east-1:e131590f-7525-4a4e-aaa8-ff76a5e764da";
    private static final Regions AMAZON_S3_IDENTITY_POOL_REGION = US_EAST_1;

    // Transfer status recognized by the Image Manager
    // (normal flow is WAITING -> IN_PROGRESS -> ... -> COMPLETED / FAILED)
    public static enum ImageTransferStatus {

        WAITING,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        CANCELED,
        FAILED,
        WAITING_FOR_NETWORK
    }


    public interface ImageCacheProcessListener {
        void onImageCachingSuccess();
        void onImageCachingError();
    }

    public interface ImageTransferListener {
        void onProgressChanged(long bytesCurrent, long bytesTotal);
        void onStatusChanged(ImageTransferStatus newStatus);
        void onImageTransferError(Exception e);
        void onImageTransferCompletion();
    }


    private static ImageManager sharedInstance;    // ImageManager is a singleton

    private WeakReference<Context> context;
    private LruCache picassoCache;
    private Downloader picassoDownloader;

    private AmazonS3 s3Client;
    private TransferUtility s3TransferUtility;


    // The constructor is private, call getInstance() to get a reference to the singleton
    private ImageManager(Context context) {

        this.context = new WeakReference<Context>(context);


        // Picasso setup:

        // Setup a custom memory cache for Picasso, so that we can keep a reference to it
        // (to be able to clean it later if necessary)
        picassoCache = new LruCache(context);

        // Setup a custom downloader for Picasso, so that we can define an appropriate disk cache size
        // (Picasso does not manage disk cache directly, lets the downloader to do it)
        long diskCacheSizeBytes = IMAGE_MANAGER_DISK_CACHE_SIZE_MB * 1024 * 1024;
        picassoDownloader = new OkHttp3Downloader(context, diskCacheSizeBytes);

        // Configure a custom instance of Picasso, and set it as the default Picasso instance
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.memoryCache(picassoCache);
        builder.downloader(picassoDownloader);
        Picasso.setSingletonInstance(builder.build());

        // Picasso logging options
        Picasso.with(context).setLoggingEnabled(false);
        Picasso.with(context).setIndicatorsEnabled(true);


        // Amazon S3 Client setup:

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,	                        // Context for the operations
                AMAZON_S3_IDENTITY_POOL_ID,      // The Identity Pool ID
                AMAZON_S3_IDENTITY_POOL_REGION	// Region for the identity pool
        );

        // Initialize the S3 Client and the S3 Transfer Utility
        s3Client = new AmazonS3Client(credentialsProvider);
        s3TransferUtility = new TransferUtility(s3Client, context);
    }


    public synchronized static ImageManager getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context (http://bit.ly/6LRzfx)
        if (sharedInstance == null)
            sharedInstance = new ImageManager( context.getApplicationContext() );

        return sharedInstance;
    }


    public void uploadImage(File imageFile, String imageKey, final ImageTransferListener listener) {   // un File o un String con el path??

        TransferObserver observer = s3TransferUtility.upload(
                AMAZON_S3_IMAGE_BUCKET,  // the bucket to upload to
                imageKey,                   // the key for the uploaded object
                imageFile                   // the file where the data to upload exists
        );

        observer.setTransferListener(new TransferListener(){

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                listener.onProgressChanged(bytesCurrent, bytesTotal);
            }

            @Override
            public void onError(int id, Exception ex) {
                listener.onImageTransferError(ex);
            }

            @Override
            public void onStateChanged(int id, TransferState state) {

                switch(state) {
                    case WAITING:
                        listener.onStatusChanged(WAITING);
                        break;

                    case IN_PROGRESS:
                        listener.onStatusChanged(IN_PROGRESS);
                        break;

                    case COMPLETED:
                        listener.onStatusChanged(COMPLETED);
                        listener.onImageTransferCompletion();
                        break;

                    case CANCELED:
                        listener.onStatusChanged(CANCELED);
                        break;

                    case FAILED:
                        listener.onStatusChanged(FAILED);
                        break;

                    default:
                        break;
                }
            }
        });
    }


    private String generatePresignedUrl(String bucketName, String objectKey) {

        com.amazonaws.HttpMethod method = HttpMethod.GET;     // if not specified, GET is the default

        // Set an "infinite" expiration date
        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60 * 24 * 365 * 1000; // 1,000 years
        expiration.setTime(msec);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey);

        generatePresignedUrlRequest.setMethod(method);          // default is GET
        generatePresignedUrlRequest.setExpiration(expiration);

        URL presignedUrl = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

        return presignedUrl.toString();
    }


}