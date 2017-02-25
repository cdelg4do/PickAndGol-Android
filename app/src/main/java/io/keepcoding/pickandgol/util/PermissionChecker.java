package io.keepcoding.pickandgol.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.CAMERA_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.LOCATION_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.RW_STORAGE_SET;


/**
 * This class simplifies all the boilerplate code to request permissions from an activity.
 */
public class PermissionChecker {

    // Available request codes (to use in activity.onRequestPermissionsResult() )
    public static final int REQUEST_FOR_STORAGE_PERMISSION = 100;
    public static final int REQUEST_FOR_CAMERA_PERMISSION = 101;
    public static final int REQUEST_FOR_LOCATION_PERMISSION = 102;

    // Available permissions to check
    public enum PermissionTag {
        RW_STORAGE_SET,
        CAMERA_SET,
        LOCATION_SET
    }

    // Use this interface to listen for permission requests
    public interface CheckPermissionListener {
        void onPermissionDenied();
        void onPermissionGranted();
    }

    private static Map<PermissionTag, PermissionSet> permissionMap;
    private Activity activity;
    private PermissionTag tag;
    private String title;
    private String msg;
    private CheckPermissionListener listener;


    /**
     * Constructor (will init the static class map the first time this is called)
     *
     * @param tag       the tag that identifies the set of permissions to check.
     * @param activity  the activity in where the operation takes place.
     * @param title     dialog title to show the user (in case explanation is needed).
     * @param msg       dialog message to show the user (in case explanation is needed).
     * @param listener  listener for the operation.
     */
    public PermissionChecker(@NonNull PermissionTag tag,
                            final Activity activity,
                             @Nullable String title,
                             @Nullable String msg,
                             @NonNull CheckPermissionListener listener) {

        this.tag = tag;
        this.activity = activity;
        this.title = title;
        this.msg = msg;
        this.listener = listener;

        if (permissionMap == null)
            initMap();
    }

    /**
     * Checks if the given permission set has been granted. If not, asks the user for them.
     * (the calling activity should implement onRequestPermissionsResult() to handle the answer)
     */
    public void checkBeforeAsking() {

        PermissionSet pSet = permissionMap.get(tag);

        String permissionToCheck = pSet.getPermissionToCheck();
        final String[] permissionsToRequest = pSet.getPermissionArray();
        final int requestCode = pSet.getRequestCode();

        boolean showExplanation = (
                title != null
                && msg != null
                && shouldShowRequestPermissionRationale(activity,permissionToCheck)
        );

        if (ContextCompat.checkSelfPermission(activity, permissionToCheck) == PERMISSION_GRANTED)
            listener.onPermissionGranted();

        else {

            if (showExplanation)
                Utils.simpleDialog(activity, title, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(permissionsToRequest, requestCode);
                    }
                });

            else
                requestPermissions(permissionsToRequest, requestCode);
        }
    }

    /**
     * Checks if the given permission set has been granted, then calls the listener.
     * (this should be called in onRequestPermissionsResult() only)
     */
    public void checkAfterAsking() {

        String permissionToCheck = permissionMap.get(tag).getPermissionToCheck();

        if (ContextCompat.checkSelfPermission(activity, permissionToCheck) == PERMISSION_GRANTED)
            listener.onPermissionGranted();
        else
            listener.onPermissionDenied();
    }


    /** Private methods and classes **/

    // Inits the static class map, building the necessary PermissionSet objects
    // and assigning them to their respective Permission Tags
    private void initMap() {

        permissionMap = new HashMap<>();

        PermissionSet storageSet = new PermissionSet(
                REQUEST_FOR_STORAGE_PERMISSION,
                READ_EXTERNAL_STORAGE,
                new String[] {  READ_EXTERNAL_STORAGE,
                        WRITE_EXTERNAL_STORAGE  }
        );

        PermissionSet cameraSet = new PermissionSet(
                REQUEST_FOR_CAMERA_PERMISSION,
                CAMERA,
                new String[] {  CAMERA  }
        );

        PermissionSet locationSet = new PermissionSet(
                REQUEST_FOR_LOCATION_PERMISSION,
                ACCESS_FINE_LOCATION,
                new String[] {  ACCESS_FINE_LOCATION  }
        );


        permissionMap.put(RW_STORAGE_SET, storageSet);
        permissionMap.put(CAMERA_SET, cameraSet);
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

    // This class contains the permission to check, permissions to request and request code
    // that will be associated to some specific PermissionTag
    private class PermissionSet {

        int requestCode;
        private String permissionToCheck;
        private List<String> permissionList;


        public PermissionSet(int requestCode, String permissionToCheck, String[] permissionArray) {

            this.requestCode = requestCode;
            this.permissionToCheck = permissionToCheck;
            this.permissionList = new ArrayList<>();

            for (String p : permissionArray)
                permissionList.add(p);
        }

        public int getRequestCode() {   return requestCode; }

        public String getPermissionToCheck() {  return permissionToCheck;   }

        public String[] getPermissionArray() {

            String[] array = new String[ permissionList.size() ];

            for (int i=0; i<permissionList.size(); i++)
                array[i] = permissionList.get(i);

            return array;
        }
    }

}
