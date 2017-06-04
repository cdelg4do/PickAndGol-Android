package io.keepcoding.pickandgol.manager.image;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImagePickingListener;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_GET_CONTENT;
import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.CUSTOM_CAMERA_DIR;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.CUSTOM_CAMERA_FILENAME;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;


/**
 * This class manages all the operations to show a camera/gallery image picker.
 *
 * This class is abstract, and all its methods are static.
 * It is an auxiliary class of ImageManager, and has package-private visibility.
 */
abstract class ImagePicker {

    private final static String LOG_TAG = "ImagePicker";

    /**
     * Shows the camera/gallery image chooser, using IMAGE_PICKER_REQUEST_CODE as request code.
     * If there are no associated apps in the system, or not enough permissions, then does nothing.
     *
     * @param activity  the activity from where the picker is shown.
     */
    static void showImagePicker(Activity activity) {

        Intent imageChooser = createImageChooserIntent(activity);

        if (imageChooser != null)
            activity.startActivityForResult(imageChooser, IMAGE_PICKER_REQUEST_CODE);
    }


    /**
     * Handles the image picker result.
     * (this is meant to be called from onActivityResult() method of the activity)
     *
     * @param activity  the activity from where the picker is shown.
     */
    static void handleImagePickerResult(Activity activity,
                                        int requestCode,
                                        int resultCode,
                                        Intent data,
                                        ImagePickingListener listener) {

        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                Log.d(LOG_TAG, "Image picking was canceled");
                listener.onImagePicked(null);
                return;
            }

            Uri imageUri;
            try                 {   imageUri = getPickerResultUri(data);    }
            catch (Exception e) {   imageUri = null;                        }

            if (imageUri == null) {
                Log.e(LOG_TAG, "Error getting image path: result does not contain an uri");
                listener.onImagePicked(null);
                return;
            }

            String imagePath = getRealPathFromUri(activity, imageUri);
            listener.onImagePicked(imagePath);
        }
    }


    /** Auxiliary methods **/

    // Creates a chooser intent with all registered options for image picking (camera and/or gallery)
    // (if no permissions to access the camera nor the gallery were granted, it will return null).
    private static @Nullable Intent createImageChooserIntent(Activity activity) {

        // In order to create a chooser intent, at least one of this permissions
        // (CAMERA or READ_EXTERNAL_STORAGE) should be granted. If not, return null.
        if ( ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED &&
             ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED ) {

            return null;
        }

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = activity.getPackageManager();

        // Only collect camera intents if the app has Camera access permission
        if (ContextCompat.checkSelfPermission(activity, CAMERA) == PERMISSION_GRANTED) {

            // Custom uri for the images taken with the camera
            // (if it is null, images will be stored in the camera folder like any other camera photo)
            Uri cameraOutputUri = null;

            if (CUSTOM_CAMERA_DIR != null)
                cameraOutputUri = Uri.fromFile(new File(CUSTOM_CAMERA_DIR.getPath(), CUSTOM_CAMERA_FILENAME));

            // Collect all camera intents
            Intent cameraIntent = new Intent(ACTION_IMAGE_CAPTURE);
            List<ResolveInfo> listCam = packageManager.queryIntentActivities(cameraIntent, 0);

            for (ResolveInfo res : listCam) {

                Intent intent = new Intent(cameraIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(res.activityInfo.packageName);

                // This will make the file available on the disk,
                // and no data will be included in the onActivityResult() call.
                if (cameraOutputUri != null)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);

                allIntents.add(intent);
            }
        }

        // Only collect gallery intents if the app has Read access permission to the external storage
        if (ContextCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {

            // Collect all gallery intents
            Intent galleryIntent = new Intent(ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");

            List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);

            for (ResolveInfo res : listGallery) {

                Intent intent = new Intent(galleryIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(res.activityInfo.packageName);

                allIntents.add(intent);
            }
        }

        // Get rid of the "Recent documents" system app (it is useless as picture picker)
        for (Intent intent : allIntents)
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                allIntents.remove(intent);
                break;
            }

        // If no intents of any type were collected, return null
        if (allIntents.size() == 0)
            return null;

        // Take one of the collected intents -the last one- out of the list to build the chooser
        // from it, then add to the chooser the other intents from the list (we choose the last
        // intent, so that the chooser options will keep the same order they had in the list)
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, activity.getString(R.string.image_picker_chooser_title));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                allIntents.toArray( new Parcelable[allIntents.size()] ));

        return chooserIntent;
    }

    // Returns the correct uri for camera or gallery intents
    private static Uri getPickerResultUri(Intent data) {

        Uri uri;
        boolean useContentUri = data != null &&
                                data.getData() != null &&
                                data.getData().getScheme().equals("content");

        if (useContentUri)
            uri = data.getData();
        else
            uri = Uri.fromFile(new File(CUSTOM_CAMERA_DIR.getPath(), CUSTOM_CAMERA_FILENAME));

        return uri;
    }

    // Gets the physical path of an image file from its uri
    private static String getRealPathFromUri(Activity activity, Uri uri) {

        String path = null;
        String uriPath = uri.getPath();
        String uriScheme = uri.getScheme();

        // Image from gallery --> ask the content resolver for the image id, then get the path
        if (uriScheme.equals("content")) {

            String[] pathParts = uriPath.split("/");
            String documentID = pathParts[pathParts.length - 1];    // uri path ends with .../<id>

            String mediaData_column = MediaStore.Images.Media.DATA;

            Cursor imageCursor = activity.getContentResolver().query(
                    uri,                                                // uri
                    new String[] { mediaData_column },                  // selection
                    MediaStore.Images.Media._ID + "=" + documentID,     // filter
                    null,                                               // filter args
                    null);                                              // sort order

            if (imageCursor.moveToFirst())
                path = imageCursor.getString(imageCursor.getColumnIndex(mediaData_column));
        }

        // Image from the camera --> get the path directly from the uri path
        else if (uriScheme.equals("file"))
            path = uriPath;

        return path;
    }

}
