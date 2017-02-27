package io.keepcoding.pickandgol.manager.image;

import com.amazonaws.regions.Regions;

import java.io.File;

import io.keepcoding.pickandgol.PickAndGolApp;
import io.keepcoding.pickandgol.R;

import static com.amazonaws.regions.Regions.EU_WEST_1;

/**
 * This class contains static settings to be used by the ImageManager class.
 */
public class ImageManagerSettings {

    // Settings for Picasso:
    public static final String CACHE_DIR = "picasso-cache";
    public static final int CACHE_SIZE_MB = 75;
    public static final boolean ENABLE_LOGGING = false;
    public static final boolean DEBUG_INDICATORS = true;

    // Settings for AWS SDK:
    public static final String S3_BUCKET = "pickandgol";
    public static final String S3_POOL_ID = PickAndGolApp.getContext().getString(R.string.aws_identity_pool_id);
    public static final Regions S3_POOL_REGION = EU_WEST_1;

    // Settings for image resizing:
    public static final int RESIZE_MAX_WIDTH = 800;
    public static final int RESIZE_MAX_HEIGHT = 600;
    public static final int RESIZE_COMPRESS = 90;
    public static final String RESIZE_TEMP_OUTPUT =
            PickAndGolApp.getContext().getExternalCacheDir().getAbsolutePath()
            + File.separator
            + "resized.tmp.jpg";


    // Settings for the image picker:
    public final static int IMAGE_PICKER_REQUEST_CODE = 36248;

    // Set this to true if you want the taken pictures be stored in a custom temporary file.
    // Set to false if you want them to be stored in the camera folder, like any other cam photo.
    static final boolean USE_CUSTOM_CAMERA_OUTPUT = false;
    static final File CUSTOM_CAMERA_DIR = PickAndGolApp.getContext().getExternalCacheDir();
    static final String CUSTOM_CAMERA_FILENAME = "camera.tmp.png";
}
