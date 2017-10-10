package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.Utils;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;


/**
 * This class represents an activity where the user can select a location on a map.
 */
public class LocationPickerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LocationPickerActivity";

    public static final String INITIAL_LOCATION_LATITUDE_KEY = "INITIAL_LOCATION_LATITUDE_KEY";
    public static final String INITIAL_LOCATION_LONGITUDE_KEY = "INITIAL_LOCATION_LONGITUDE_KEY";

    public static final String SELECTED_LATITUDE_KEY = "SELECTED_LATITUDE_KEY";
    public static final String SELECTED_LONGITUDE_KEY = "SELECTED_LONGITUDE_KEY";

    private static final String SAVED_STATE_MAP_TYPE_KEY = "SAVED_STATE_MAP_TYPE_KEY";
    private static final String SAVED_STATE_MAP_LATITUDE_KEY = "SAVED_STATE_MAP_LATITUDE_KEY";
    private static final String SAVED_STATE_MAP_LONGITUDE_KEY = "SAVED_STATE_MAP_LONGITUDE_KEY";
    private static final String SAVED_STATE_MAP_ZOOM_KEY = "SAVED_STATE_MAP_ZOOM_KEY";

    // Map settings in case no initial location is provided in the intent
    public static final double STANDARD_MAP_LATITUDE = 40.41665;
    public static final double STANDARD_MAP_LONGITUDE = -3.70381;
    private static final int STANDARD_MAP_ZOOM = 5;

    // Map settings in case an initial location is provided in the intent
    private static final int STANDARD_MAP_WITH_LOCATION_ZOOM = 17;

    // Reference to UI elements to be bound with Butterknife (not before the fragment is inflated)
    @BindView(R.id.activity_location_picker_button_cancel)  Button btnCancel;
    @BindView(R.id.activity_location_picker_button_select)  Button btnSelect;
    @BindView(R.id.activity_location_picker_toggle_view)    ImageView toggleMapView;

    private SupportMapFragment mapFragment;
    private GoogleMap map;

    private Double initialLat, initialLong;
    private int initialZoom;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        setupActionBar();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.activity_location_picker_map_fragment);
        if (mapFragment != null)
            ButterKnife.bind(this);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                setupMap(savedInstanceState);
                setupControls();

                // If the activity is being restored (for example after an orientation change),
                // get back to the previous state
                if (savedInstanceState != null) {
                    restoreActivityState(savedInstanceState);
                }
            }
        });
    }


    // In case of need, we just have to save the current map type and position
    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (map == null)
            return;

        Log.d(LOG_TAG, "Saving activity state...");

        int type = map.getMapType();
        double lat = map.getCameraPosition().target.latitude;
        double lon = map.getCameraPosition().target.longitude;
        Float zoom = map.getCameraPosition().zoom;

        outState.putInt(SAVED_STATE_MAP_TYPE_KEY, type);
        outState.putDouble(SAVED_STATE_MAP_LATITUDE_KEY, lat);
        outState.putDouble(SAVED_STATE_MAP_LONGITUDE_KEY, lon);
        outState.putFloat(SAVED_STATE_MAP_ZOOM_KEY, zoom);

        super.onSaveInstanceState(outState);
    }


    // Get the map type and position that were showing before destroying the activity,
    // and then paint it all like it was before
    private void restoreActivityState(final @NonNull Bundle savedInstanceState) {

        if (map == null)
            return;

        Log.d(LOG_TAG, "Restoring activity state...");

        int type = savedInstanceState.getInt(SAVED_STATE_MAP_TYPE_KEY, MAP_TYPE_NORMAL);
        double lat = savedInstanceState.getDouble(SAVED_STATE_MAP_LATITUDE_KEY, STANDARD_MAP_LATITUDE);
        double lon = savedInstanceState.getDouble(SAVED_STATE_MAP_LONGITUDE_KEY, STANDARD_MAP_LONGITUDE);
        float zoom = savedInstanceState.getFloat(SAVED_STATE_MAP_ZOOM_KEY, STANDARD_MAP_ZOOM);

        map.setMapType(type);
        map.animateCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), zoom) );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null, null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle(getString(R.string.location_picker_activity_location_on_the_map));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }


    // Initial settings for the map object
    private void setupMap(Bundle savedInstanceState) {

        if (map == null)
            return;

        // Attempt to recover the passed location, to start map the map with
        Intent i = getIntent();

        boolean isLocationProvided =
                        ((Double) i.getSerializableExtra(INITIAL_LOCATION_LATITUDE_KEY)) != null &&
                        ((Double) i.getSerializableExtra(INITIAL_LOCATION_LONGITUDE_KEY)) != null;

        if (isLocationProvided) {
            initialLat = (Double) i.getSerializableExtra(INITIAL_LOCATION_LATITUDE_KEY);
            initialLong = (Double) i.getSerializableExtra(INITIAL_LOCATION_LONGITUDE_KEY);
            initialZoom = STANDARD_MAP_WITH_LOCATION_ZOOM;
        }
        else {
            initialLat = STANDARD_MAP_LATITUDE;
            initialLong = STANDARD_MAP_LONGITUDE;
            initialZoom = STANDARD_MAP_ZOOM;

            if (savedInstanceState == null)
                Utils.shortToast(this, getString(R.string.location_picker_activity_unable_determine_location));
        }

        // Set the map type, user cannot rotate it, and show the zoom buttons and the my-location button
        map.setMapType(MAP_TYPE_NORMAL);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);

        if (isLocationProvided)
            map.getUiSettings().setMyLocationButtonEnabled(true);

        // Center the map to its initial position and zoom
        LatLng mapCenter = new LatLng(initialLat, initialLong);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mapCenter)
                .zoom(initialZoom)
                .build();

        map.animateCamera( CameraUpdateFactory.newCameraPosition(cameraPosition) );

        // Show the user location (if possible)
        if (isLocationProvided)
            try {
                map.setMyLocationEnabled(true);
            }
            catch (SecurityException e) {}
    }


    private void setupControls() {

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null, null);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = map.getCameraPosition().target.latitude;
                double lon = map.getCameraPosition().target.longitude;

                finishActivity(lat, lon);
            }
        });

        toggleMapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (map.getMapType() == MAP_TYPE_NORMAL) {
                    map.setMapType(MAP_TYPE_HYBRID);
                    toggleMapView.setImageResource(R.drawable.ic_map_view);
                }
                else if (map.getMapType() == MAP_TYPE_HYBRID) {
                    map.setMapType(MAP_TYPE_NORMAL);
                    toggleMapView.setImageResource(R.drawable.ic_satellite_view);
                }
            }
        });
    }


    private void finishActivity(@Nullable Double latitude, @Nullable Double longitude) {

        if (latitude != null && longitude != null)
            Log.e(LOG_TAG, "Location selected: ("+ latitude +", "+ longitude +")");
        else
            Log.e(LOG_TAG, "No location selected");

        Navigator.backFromLocationPickerActivity(this, latitude, longitude);
    }
}
