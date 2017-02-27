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
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.manager.image.ImageManager.ImagePickingListener;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.CUSTOM_CAMERA_DIR;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.CUSTOM_CAMERA_FILENAME;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.USE_CUSTOM_CAMERA_OUTPUT;


/**
 * This class manages all the operations to show a camera/gallery image picker.
 *
 * This class is abstract, and all its methods are static.
 * It is an auxiliary class of ImageManager, and has package-private visibility.
 */
abstract class ImagePicker {

    private final static String LOG_TAG = "ImagePicker";

    /**
     * Shows the camera/gallery image chooser.
     *
     * @param activity  the activity from where the picker is shown.
     */
    static void showImagePicker(Activity activity) {

        Intent imagePickerChooser = createImagePickerIntent(activity);
        activity.startActivityForResult(imagePickerChooser, IMAGE_PICKER_REQUEST_CODE);
    }


    /**
     * Handles the image picker result from activity.onActivityResult().
     *
     * @param activity  the activity from where the picker is shown.
     */
    static void handleImagePickerResult(Activity activity,
                                        int requestCode,
                                        int resultCode,
                                        Intent data,
                                        ImagePickingListener listener) {

        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                if (getPickerResultUri(data) != null) {

                    try {
                        Uri imageUri = getPickerResultUri(data);

                        String imagePath = getRealPathFromUri(activity, imageUri);
                        listener.onImagePicked(imagePath);
                    }
                    catch (Exception e) {
                        Log.e(LOG_TAG, "Error getting image: "+ e.toString());
                        listener.onImagePicked(null);
                    }
                }
                else {
                    Log.e(LOG_TAG, "Image picker: result does not contain  was not OK");
                    listener.onImagePicked(null);
                }
            }
            else {
                Log.e(LOG_TAG, "Image picker: result code was not OK");
                listener.onImagePicked(null);
            }
        }
    }


    /** Auxiliary methods **/

    // Returns a chooser intent with all registered options for image picking (camera and gallery)
    private static Intent createImagePickerIntent(Activity activity) {

        // Custom uri for the images taken with the camera
        // (if it is null, images will be stored in the camera folder like any other camera photo)
        Uri cameraOutputUri = null;

        if (USE_CUSTOM_CAMERA_OUTPUT) {

            if (CUSTOM_CAMERA_DIR != null)
                cameraOutputUri = Uri.fromFile(new File(CUSTOM_CAMERA_DIR.getPath(), CUSTOM_CAMERA_FILENAME));
        }


        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = activity.getPackageManager();

        // Collect all camera intents
        Intent cameraIntent = new Intent(ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(cameraIntent, 0);

        for (ResolveInfo res : listCam) {

            Intent intent = new Intent(cameraIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);

            // In case we are using a custom uri for the camera pictures, this will make the file
            // available on the disk, and no data will be included in the onActivityResult() call.
            if (cameraOutputUri != null)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);

            allIntents.add(intent);
        }

        // Collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");

        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);

        for (ResolveInfo res : listGallery) {

            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);

            allIntents.add(intent);
        }

        // Extract the main intent (for Documents system app) from the list
        Intent mainIntent = allIntents.get(allIntents.size() - 1);

        for (Intent intent : allIntents)
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }

        allIntents.remove(mainIntent);

        // Create a chooser from the main intent, and add all other intents in the list
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select a source for the image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                allIntents.toArray( new Parcelable[allIntents.size()] ));

        return chooserIntent;
    }

    // Returns the correct uri for camera and gallery intents
    private static Uri getPickerResultUri(Intent data) {

        Uri uri = null;

        if ( ! USE_CUSTOM_CAMERA_OUTPUT ) {

            if (data != null)
                uri = data.getData();

            return uri;
        }

        // If using custom camera output files, we need to determine what kind of uri need
        // (for camera images or for gallery images)
        boolean isCameraCustomUri = true;

        if (data != null)
            if ( data.getAction() == null || ! data.getAction().equals(ACTION_IMAGE_CAPTURE) )
                isCameraCustomUri = false;

        if (isCameraCustomUri) {
            if (CUSTOM_CAMERA_DIR != null)
                uri = Uri.fromFile( new File(CUSTOM_CAMERA_DIR.getPath(), CUSTOM_CAMERA_FILENAME) );

            return uri;
        }
        else
            return data.getData();
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
