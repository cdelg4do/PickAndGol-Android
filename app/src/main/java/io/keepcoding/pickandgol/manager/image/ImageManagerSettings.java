package io.keepcoding.pickandgol.manager.image;

import com.amazonaws.regions.Regions;

import io.keepcoding.pickandgol.PickAndGolApp;
import io.keepcoding.pickandgol.R;

import static com.amazonaws.regions.Regions.EU_WEST_1;

/**
 * This class contains static settings to be used by the ImageManager class.
 */
public class ImageManagerSettings {

    // Settings for Picasso
    public static final String CACHE_DIR = "picasso-cache";
    public static final int CACHE_SIZE_MB = 75;
    public static final boolean ENABLE_LOGGING = false;
    public static final boolean DEBUG_INDICATORS = true;

    // Settings for AWS SDK
    public static final String S3_BUCKET = "pickandgol";
    public static final String S3_POOL_ID = PickAndGolApp.getAppContext().getString(R.string.aws_identity_pool_id);
    public static final Regions S3_POOL_REGION = EU_WEST_1;
}
