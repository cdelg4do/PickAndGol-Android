package io.keepcoding.pickandgol.manager.geo;

import android.content.Context;
import android.location.Location;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;


/**
 * This class manages some location functionalities, like getting the last know device location.
 */
public class GeoManager {

    private final static String LOG_TAG = "GeoManager";

    // Use this interface to listen to location requests
    public interface GeoManagerListener {

        void onLocationError(Throwable error);
        void onLocationSuccess(double latitude, double longitude);
    }


    private GoogleApiClient googleClient;
    private GeoManagerListener listener;


    public GeoManager(Context context) {

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
    public void requestLastLocation(GeoManagerListener listener) {

        this.listener = listener;
        googleClient.connect();
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
}
