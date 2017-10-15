package io.keepcoding.pickandgol.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.keepcoding.pickandgol.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.LOCATION_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.PICTURES_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.RW_STORAGE_SET;


/**
 * This class simplifies all the boilerplate code to request permissions from an activity.
 *
 * USAGE:
 *
 * 1- In your activity, create as many PermissionChecker objects as permission requests are needed.
 *
 * 2- When there is an operation that requires some permission, call checkBeforeAsking() and pass it
 *      a CheckPermissionListener. Put your operation inside onPermissionGranted().
 *
 * 3- In the onRequestPermissionsResult() of the activity, check the appropriate request code and
 *      call checkAfterAsking(). The previous CheckPermissionListener code will be used.
 *
 *      If you need to use a different request code than the default, just add it as the first
 *      parameter of checkBeforeAsking() in step 2.
 *
 * 4- If you just want to check if the permissions are already granted (without asking),
 *      ignore steps 2 & 3 and call arePermissionsGranted() instead.
 *
 *      Alternatively, you can check if some specific permission set is already granted without
 *      instantiating a PermissionChecker. To do so, ignore all the previous steps and
 *      just call the isThisPermissionSetGranted() static method.
 *
 *      You will need to provide the permission set you want to check and the caller activity.
 */
public class PermissionChecker {

    // Static object that associates a PermissionTag with a predefined set of permissions to check
    private static Map<PermissionTag, PermissionSet> permissionMap;

    // Default request codes (to use in activity.onRequestPermissionsResult() )
    public static final int REQUEST_FOR_STORAGE_PERMISSION = 100;
    public static final int REQUEST_FOR_PICTURES_PERMISSION = 101;
    public static final int REQUEST_FOR_LOCATION_PERMISSION = 102;

    // Available permission sets to check
    public enum PermissionTag {
        RW_STORAGE_SET,     // Read & Write access to the external storage
        PICTURES_SET,       // Access to the camera & Read access to the external storage
        LOCATION_SET        // Access to device fine location
    }

    // Use this interface to listen for permission requests
    public interface CheckPermissionListener {
        void onPermissionDenied();
        void onPermissionGranted();
    }

    private Activity activity;
    private PermissionTag tag;
    private String explanationTitle;
    private String explanationMsg;
    private CheckPermissionListener listener;


    /**
     * Constructor (will init the static class map the first time this is called)
     *
     * @param tag       the tag that identifies the set of permissions to check.
     * @param activity  the activity in where the operation takes place.
     */
    public PermissionChecker(@NonNull PermissionTag tag, final @NonNull Activity activity) {

        if (permissionMap == null)
            initMap();

        this.tag = tag;
        this.activity = activity;

        switch (tag) {

            case RW_STORAGE_SET:
                explanationTitle = activity.getString(R.string.permission_checker_storage_title);
                explanationMsg = activity.getString(R.string.permission_checker_storage_message);
                break;

            case PICTURES_SET:
                explanationTitle = activity.getString(R.string.permission_checker_pictures_title);
                explanationMsg = activity.getString(R.string.permission_checker_pictures_message);
                break;

            case LOCATION_SET:
                explanationTitle = activity.getString(R.string.permission_checker_location_title);
                explanationMsg = activity.getString(R.string.permission_checker_location_message);
                break;

            default:
                break;
        }
    }

    /**
     * Checks if the given permission set has been granted. If not, asks the user for them.
     * (the calling activity should implement onRequestPermissionsResult() to handle the answer)
     *
     * @param listener a listener for the operation.
     */
    public void checkBeforeAsking(@NonNull CheckPermissionListener listener) {

        if (listener == null)
            return;

        this.listener = listener;
        final String[] permissionsToCheck = permissionMap.get(tag).getPermissionArray();
        final int requestCode = permissionMap.get(tag).getRequestCode();;

        boolean showExplanation = (explanationTitle != null &&
                                   explanationMsg != null &&
                                   shouldShowExplanatoryMessage(permissionsToCheck)
        );

        if ( allPermissionsAreGranted(permissionsToCheck, activity) )
            listener.onPermissionGranted();

        else
            if (showExplanation)
                Utils.simpleDialog(activity, explanationTitle, explanationMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(permissionsToCheck, requestCode);
                    }
                });
            else
                requestPermissions(permissionsToCheck, requestCode);
    }

    /**
     * Checks if the given permission set has been granted. If not, asks the user for them.
     * (the calling activity should implement onRequestPermissionsResult() to handle the answer)
     *
     * This method allows to specify a different request code than the default one.
     * This is useful if you need different callbacks in an Activity for the same permission request.
     *
     * @param requestCode   the request code value that will be used in onRequestPermissionsResult()
     * @param listener      a listener for the operation.
     */
    public void checkBeforeAsking(final int requestCode, @NonNull CheckPermissionListener listener) {

        if (listener == null)
            return;

        this.listener = listener;
        final String[] permissionsToCheck = permissionMap.get(tag).getPermissionArray();

        boolean showExplanation = (explanationTitle != null &&
                                   explanationMsg != null &&
                                   shouldShowExplanatoryMessage(permissionsToCheck)
        );

        if ( allPermissionsAreGranted(permissionsToCheck, activity) )
            listener.onPermissionGranted();

        else
            if (showExplanation)
                Utils.simpleDialog(activity, explanationTitle, explanationMsg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(permissionsToCheck, requestCode);
                    }
                });
            else
                requestPermissions(permissionsToCheck, requestCode);
    }

    /**
     * Checks if the given permission set has been granted, then calls the listener.
     * Never call this method before calling first checkBeforeAsking(), as it will not work.
     * (this should be called in activity.onRequestPermissionsResult() only)
     */
    public void checkAfterAsking() {

        if (listener == null)
            return;

        String[] permissionsToCheck = permissionMap.get(tag).getPermissionArray();

        if ( allPermissionsAreGranted(permissionsToCheck, activity) )     listener.onPermissionGranted();
        else                                                    listener.onPermissionDenied();
    }

    /**
     * Tells whether this checker's permission set has been already granted or not.
     */
    public boolean arePermissionsGranted() {

        final String[] permissionsToCheck = permissionMap.get(tag).getPermissionArray();
        return allPermissionsAreGranted(permissionsToCheck, activity);
    }

    /**
     * Tells whether the given permission set has been already granted or not.
     * (Static method).
     *
     * @param tag       the tag that identifies the set of permissions to check.
     * @param activity  the activity in where the operation takes place.
     */
    public static boolean arePermissionsGranted(@NonNull PermissionTag tag, Activity activity) {

        final String[] permissionsToCheck = permissionMap.get(tag).getPermissionArray();
        return allPermissionsAreGranted(permissionsToCheck, activity);
    }


    /** Private methods and classes **/

    // Init the static class map, building the necessary PermissionSet objects
    // and assigning them to their respective Permission Tags
    private void initMap() {

        permissionMap = new HashMap<>();

        PermissionSet storageSet = new PermissionSet(
                REQUEST_FOR_STORAGE_PERMISSION,
                new String[] {  READ_EXTERNAL_STORAGE,
                                WRITE_EXTERNAL_STORAGE  }
        );

        // Note: the taken pictures and image processing results are stored in the app cache folder,
        //       that does not require any permission to be read/written.
        // (if using another folder, WRITE_EXTERNAL_STORAGE permission might be necessary too)
        PermissionSet picturesSet = new PermissionSet(
                REQUEST_FOR_PICTURES_PERMISSION,
                new String[] {  CAMERA,
                                READ_EXTERNAL_STORAGE   }
        );

        PermissionSet locationSet = new PermissionSet(
                REQUEST_FOR_LOCATION_PERMISSION,
                new String[] {  ACCESS_FINE_LOCATION  }
        );


        permissionMap.put(RW_STORAGE_SET, storageSet);
        permissionMap.put(PICTURES_SET, picturesSet);
        permissionMap.put(LOCATION_SET, locationSet);
    }

    // Requests permissions to be granted to the application
    private void requestPermissions(String[] permissionsToRequest, int requestCode) {

        ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest,
                requestCode
        );
    }

    // Determines if all the given permissions have been granted
    private static boolean allPermissionsAreGranted(String[] permissionsToCheck, Activity activity) {

        boolean allGranted = true;

        for (int i = 0; i < permissionsToCheck.length && allGranted; i++)
            if (ContextCompat.checkSelfPermission(activity, permissionsToCheck[i]) != PERMISSION_GRANTED)
                allGranted = false;

        return allGranted;
    }

    // Determines if an explanatory message should be shown before asking for the given permissions
    private boolean shouldShowExplanatoryMessage(String[] permissionsToCheck) {

        boolean showExplanation = false;

        for (int i = 0; i < permissionsToCheck.length && !showExplanation; i++)
            if ( shouldShowRequestPermissionRationale(activity, permissionsToCheck[i]) )
                showExplanation = true;

        return showExplanation;
    }

    // This class contains the permissions to request and request code
    // that will be associated to some specific PermissionTag
    private class PermissionSet {

        int requestCode;
        private List<String> permissionList;


        public PermissionSet(int requestCode, String[] permissionArray) {

            this.requestCode = requestCode;
            this.permissionList = new ArrayList<>();

            for (String p : permissionArray)
                permissionList.add(p);
        }

        public int getRequestCode() {   return requestCode; }

        public String[] getPermissionArray() {

            String[] array = new String[ permissionList.size() ];

            for (int i=0; i<permissionList.size(); i++)
                array[i] = permissionList.get(i);

            return array;
        }
    }

}
