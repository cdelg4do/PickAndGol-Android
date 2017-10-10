package io.keepcoding.pickandgol.manager.geo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;


/**
 * This class manages some location functionalities, like getting the last know device location
 * or reverse geo-decoding locations.
 */
public class GeoManager {

    private final static String LOG_TAG = "GeoManager";

    // Use this interface to listen to location requests
    public interface GeoDirectLocationListener {

        void onLocationError(Throwable error);
        void onLocationSuccess(double latitude, double longitude);
    }

    // Use this interface to listen to reverse location requests
    public interface GeoReverseLocationListener {

        void onReverseLocationError(Throwable error);
        void onReverseLocationSuccess(@NonNull List<Address> addresses);
    }


    private Context context;
    private GoogleApiClient googleClient;
    private GeoDirectLocationListener listener;


    public GeoManager(Context context) {

        this.context = context.getApplicationContext();

        // Create the necessary callbacks to connect to the Location Services API:

        OnConnectionFailedListener failureListener = new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                if (listener == null)
                    return;

                Error e = new Error("Unable to connect to Google Location Service");
                Log.e(LOG_TAG, "Error: "+ e);

                googleClient.disconnect();
                listener.onLocationError(e);
            }
        };

        ConnectionCallbacks connectionCallback = new ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int i) {

                if (listener == null)
                    return;

                Error e = new Error("Google Location Service failed");
                Log.e(LOG_TAG, "Error: "+ e);

                googleClient.disconnect();
                listener.onLocationError(e);
            }

            @Override
            public void onConnected(@Nullable Bundle bundle) {

                if (listener == null)
                    return;

                Location lastLocation;

                // Calling getLastLocation() without checking location permissions
                // (that should be checked outside the GeoManager)
                try {
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleClient);
                }
                catch (SecurityException e) {
                    Log.e(LOG_TAG, "Error: "+ e);

                    googleClient.disconnect();
                    listener.onLocationError(e);
                    return;
                }

                googleClient.disconnect();

                if (lastLocation == null) {
                    Error e = new Error("Device location is unavailable");
                    Log.e(LOG_TAG, "Error: "+ e);

                    listener.onLocationError(e);
                    return;
                }

                Double lat = lastLocation.getLatitude();
                Double lon = lastLocation.getLongitude();

                Log.w(LOG_TAG, "Device location: "+ lat +", "+ lon);
                listener.onLocationSuccess(lat, lon);
            }
        };


        // Create an instance of GoogleAPIClient to connect to the Location Services API,
        // using the previously defined callbacks:

        googleClient = new GoogleApiClient.Builder(context.getApplicationContext())
                                          .addConnectionCallbacks(connectionCallback)
                                          .addOnConnectionFailedListener(failureListener)
                                          .addApi(LocationServices.API)
                                          .build();
    }


    /**
     * Starts an async request to get the last known location of the device.
     *
     * @param listener  a listener to manage the results of the async operation.
     */
    public void requestLastLocation(GeoDirectLocationListener listener) {

        this.listener = listener;
        googleClient.connect();
    }


    /**
     * Starts an async request to get the associated addresses for a given location.
     *
     * @param latitude      latitude of the location.
     * @param longitude     longitude of the location.
     * @param maxResults    maximum number of addresses retrieved.
     * @param listener      a listener to manage the results of the async operation.
     */
    public void requestReverseDecodedAddresses(double latitude, double longitude, int maxResults,
                                               @NonNull GeoReverseLocationListener listener) {

        if (listener == null)
            return;

        new ReverseGeoCoder(context, latitude, longitude, maxResults, listener)
            .execute();
    }


    /**
     * Starts an async request to get the associated address for a given location.
     * Similar to the previous method, but it will return no more than one results.
     *
     * @param latitude      latitude of the location.
     * @param longitude     longitude of the location.
     * @param listener      a listener to manage the results of the async operation.
     */
    public void requestReverseDecodedAddress(double latitude, double longitude,
                                               @NonNull GeoReverseLocationListener listener) {

        requestReverseDecodedAddresses(latitude, longitude, 1, listener);
    }


    /**
     * Determines if the application has permissions to access the device location (static method).
     *
     * @param context   a context for the operation.
     * @return          a boolean indicating if the device location is accessible.
     */
    public static boolean isLocationAccessGranted(Context context) {

        return ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
    }


    /**
     * Determines if the system has a Gps device present (static method).
     *
     * @param context   a context for the operation.
     * @return          a boolean indicating if there is a Gps device present in the system.
     */
    public static boolean isGpsDevicePresent(Context context) {

        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }


    /**
     * Determines if the system Gps is enabled (static method).
     *
     * NOTE: this can never be true if the ACCESS_FINE_LOCATION permission is not granted
     *       or the system has no Gps devices present.
     *
     * @param context   a context for the operation.
     * @return          a boolean indicating if the system Gps is enabled.
     */
    public static boolean isGpsDeviceEnabled(Context context) {

        LocationManager locationMgr = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        return locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * Determines if the system is currently able to provide the Gps location.
     *
     * Will return true only if the access fine location permission is granted, the system
     * has a Gps device and it is currently enabled.
     *
     * @param context   a context for the operation.
     * @return          a boolean indicating if the system is able to provide the Gps location.
     */
    public static boolean canGetLocationFromGps(Context context) {

        return isLocationAccessGranted(context) &&
               isGpsDevicePresent(context) &&
               isGpsDeviceEnabled(context);
    }
}
