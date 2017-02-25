package io.keepcoding.pickandgol.manager.image;

import com.amazonaws.regions.Regions;

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
    /**
    public static final String S3_BUCKET = "pickandgol-images";
    public static final String S3_POOL_ID = "us-east-1:e131590f-7525-4a4e-aaa8-ff76a5e764da";
    public static final Regions S3_POOL_REGION = US_EAST_1;
    **/

    /**/
    public static final String S3_BUCKET = "pickandgol";
    public static final String S3_POOL_ID = "eu-west-1:45da37a2-d874-4a37-b94e-752a9120c937";
    public static final Regions S3_POOL_REGION = EU_WEST_1;
    /**/
}
