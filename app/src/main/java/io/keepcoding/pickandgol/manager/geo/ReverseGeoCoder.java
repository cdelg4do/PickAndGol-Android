package io.keepcoding.pickandgol.manager.geo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.Locale;


/**
 * This class is in charge of the reverse geocoding operations in background.
 * It is an auxiliary class of GeoManager, and has package-private visibility.
 */
class ReverseGeoCoder extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = "ReverseGeoCoder";

    private Geocoder geocoder;
    private double latitude, longitude;
    private int maxResults;
    private GeoManager.GeoReverseLocationListener listener;
    private List<Address> addresses;
    private Exception error;

    ReverseGeoCoder(Context context, double latitude, double longitude, int maxResults,
                    GeoManager.GeoReverseLocationListener listener) {

        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxResults = maxResults;
        this.listener = listener;
        error = null;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... inputs) {

        Log.d(LOG_TAG, "Getting address for ("+ latitude +", "+ longitude +")...");

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, maxResults);
        }
        catch (Exception e) {
            error = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        if ( error != null) {
            Log.e(LOG_TAG, "Error geetting address: "+ error.toString());
            listener.onReverseLocationError(error);
        }

        else if (addresses == null) {
            error = new Exception("An error ocurred: unable to retreive address list");
            Log.e(LOG_TAG, error.toString());
            listener.onReverseLocationError(error);
        }

        else if (addresses.size() == 0) {
            error = new Exception("Unable to retreive any address for the location");
            Log.e(LOG_TAG, error.toString());
            listener.onReverseLocationError(error);
        }

        else {
            Log.d(LOG_TAG, "Got "+ addresses.size() +" addresses for the location");
            listener.onReverseLocationSuccess(addresses);
        }
    }
}
