package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.PubInfoWindowAdapter;
import io.keepcoding.pickandgol.fragment.PubListFragment;
import io.keepcoding.pickandgol.interactor.SearchPubsInteractor;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.search.PubSearchParams;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.PermissionChecker.CheckPermissionListener;
import io.keepcoding.pickandgol.util.Utils;
import io.keepcoding.pickandgol.view.PubListListener;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

public class EventPubsMapActivity extends AppCompatActivity implements PubListListener {

    private final static String LOG_TAG = "EventPubsMapActivity";

    public static final String MODEL_KEY = "MODEL_KEY";
    public static final String SHOW_USER_LOCATION_KEY = "SHOW_USER_LOCATION_KEY";

    private static final String SAVED_STATE_PUB_LIST_KEY = "SAVED_STATE_PUB_LIST_KEY";
    private static final String SAVED_STATE_PUB_COUNT_KEY = "SAVED_STATE_PUB_COUNT_KEY";
    private static final String SAVED_STATE_MAP_LATITUDE_KEY = "SAVED_STATE_MAP_LATITUDE_KEY";
    private static final String SAVED_STATE_MAP_LONGITUDE_KEY = "SAVED_STATE_MAP_LONGITUDE_KEY";
    private static final String SAVED_STATE_MAP_ZOOM_KEY = "SAVED_STATE_MAP_ZOOM_KEY";
    private static final String SAVED_STATE_MAP_TYPE_KEY = "SAVED_STATE_MAP_TYPE_KEY";

    // Standard Map settings
    private static final double STANDARD_MAP_LATITUDE = 40.41665;
    private static final double STANDARD_MAP_LONGITUDE = -3.70381;
    private static final int STANDARD_MAP_ZOOM = 5;

    private Event model;

    private PubListFragment pubListFragment;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private ArrayList<Marker> currentMapMarkers;

    //private PubAggregate eventPubs; // a reference to the pubs currently shown, at any moment
    private PubSearchParams lastPubSearchParams;
    private int lastPubSearchTotalResults;

    private PermissionChecker locationChecker;
    private GeoManager gm;

    // Reference to UI elements to be bind with Butterknife (not before the map fragment is inflated)
    @BindView(R.id.activity_event_pubs_map_toggle_view) ImageView toggleMapView;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_pubs_map);

        gm = new GeoManager(this);

        setupActionBar();
        loadModel();

        if (model == null)
            finishActivity(new Error("No Event was provided to the Activity"));

        else if (savedInstanceState != null)
            restoreActivityState(savedInstanceState);

        else
            loadEventPubsThenShowThem();
    }


    // In case of need, we have to save the pubs currently shown,
    // the total pub count and the current map position and mapt type
    @Override
    public void onSaveInstanceState(Bundle outState) {

        Log.d(LOG_TAG, "Saving activity state...");

        ArrayList<Pub> pubList = (ArrayList<Pub>) pubListFragment.getPubs().getAll();
        int pubCount = pubListFragment.getPubs().getTotalResults();
        double lat = map.getCameraPosition().target.latitude;
        double lon = map.getCameraPosition().target.longitude;
        Float zoom = map.getCameraPosition().zoom;
        int mapType = map.getMapType();

        outState.putSerializable(SAVED_STATE_PUB_LIST_KEY, pubList);
        outState.putInt(SAVED_STATE_PUB_COUNT_KEY, pubCount);
        outState.putDouble(SAVED_STATE_MAP_LATITUDE_KEY, lat);
        outState.putDouble(SAVED_STATE_MAP_LONGITUDE_KEY, lon);
        outState.putFloat(SAVED_STATE_MAP_ZOOM_KEY, zoom);
        outState.putInt(SAVED_STATE_MAP_TYPE_KEY, mapType);
        //TODO: save RecyclerView scroll position

        super.onSaveInstanceState(outState);
    }


    // Get the pubs and the map position that were showing before destroying the activity,
    // and then paint it all like it was before
    private void restoreActivityState(final @NonNull Bundle savedInstanceState) {

        Log.d(LOG_TAG, "Restoring activity state...");

        List<Pub> pubList = (List) savedInstanceState.getSerializable(SAVED_STATE_PUB_LIST_KEY);
        int pubCount = savedInstanceState.getInt(SAVED_STATE_PUB_COUNT_KEY, 0);
        double lat = savedInstanceState.getDouble(SAVED_STATE_MAP_LATITUDE_KEY, STANDARD_MAP_LATITUDE);
        double lon = savedInstanceState.getDouble(SAVED_STATE_MAP_LONGITUDE_KEY, STANDARD_MAP_LONGITUDE);
        float zoom = savedInstanceState.getFloat(SAVED_STATE_MAP_ZOOM_KEY, STANDARD_MAP_ZOOM);
        int mapType = savedInstanceState.getInt(SAVED_STATE_MAP_TYPE_KEY, MAP_TYPE_NORMAL);
        //TODO: restore RecyclerView scroll position

        PubAggregate restoredPubs = PubAggregate.buildFromList(pubList, pubCount);

        if (restoredPubs.getAll().size() == 0)
            return;

        setFragmentsContent(restoredPubs, lat, lon, zoom, mapType);
    }


    // Auxiliary methods:

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Pubs showing this event");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Initial settings for the map object
    private void setupMap(@Nullable Double initialLat, @Nullable Double initialLon,
                          @Nullable Float initialZoom, @Nullable Integer initialMapType) {

        if (mapFragment == null || map == null)
            return;

        // Bind UI elements
        ButterKnife.bind(this);

        double lat, lon;
        float zoom;
        int mapType, toggleMapView_ResourceImage;

        lat = (initialLat != null) ? initialLat : STANDARD_MAP_LATITUDE;
        lon = (initialLon != null) ? initialLon : STANDARD_MAP_LONGITUDE;
        zoom = (initialZoom != null) ? initialZoom : STANDARD_MAP_ZOOM;

        mapType = (initialMapType != null && initialMapType == MAP_TYPE_HYBRID)
                ? MAP_TYPE_HYBRID
                : MAP_TYPE_NORMAL;

        toggleMapView_ResourceImage = (mapType == MAP_TYPE_HYBRID)
                ? R.drawable.ic_map_view
                : R.drawable.ic_satellite_view;

        // Set the map type, user cannot rotate it, and show the zoom buttons and the my-location button
        map.setMapType(mapType);
        toggleMapView.setImageResource(toggleMapView_ResourceImage);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        // Center the map to its initial position and zoom
        LatLng mapCenter = new LatLng(lat, lon);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mapCenter)
                .zoom(zoom)
                .build();

        map.animateCamera( CameraUpdateFactory.newCameraPosition(cameraPosition) );

        // Show the user location (if possible)
        boolean showUserLocation = getIntent().getBooleanExtra(SHOW_USER_LOCATION_KEY, false);
        if (showUserLocation)
            try {
                map.setMyLocationEnabled(true);
            }
            catch (SecurityException e) {}

        // Set an adapter to show customized info windows for the markers
        map.setInfoWindowAdapter(new PubInfoWindowAdapter(this));

        // Define a listener to take action when the user clicks on the info window of a marker
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Pub pub = (Pub) marker.getTag();

                // TODO: navigator method
                //Navigator.fromEventPubsMapActivityToPubDetailActivity(EventPubsMapActivity.this, pub);
                Utils.shortSnack(EventPubsMapActivity.this, "'"+ pub.getName() +"' clicked.");
            }
        });

        // Behavior of the toggle map type "button"
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

    // Gets the model passed in the intent
    private void loadModel() {
        model = (Event) getIntent().getSerializableExtra(MODEL_KEY);
    }

    // Gets the list of pubs associated to the model from the server, and then shows it
    private void loadEventPubsThenShowThem() {

        if (model == null)
            return;

        lastPubSearchTotalResults = 0;

        lastPubSearchParams = PubSearchParams.buildEmptyParams();
        lastPubSearchParams.setEventId( model.getId() );

        searchPubsFirstPage(lastPubSearchParams, null);
    }

    // Add markers to the map, corresponding to a given Shops object
    private void addPubMarkersToMap(@NonNull PubAggregate pubs) {

        if (map == null || pubs == null)
            return;

        List<Pub> pubList = pubs.getAll();

        if (currentMapMarkers == null)
            currentMapMarkers = new ArrayList<>();

        for (Pub pub: pubList) {

            LatLng pubLocation = new LatLng(pub.getLatitude(), pub.getLongitude());

            // Adding the title here, allows to show it in the default info window of the marker
            // (in case no custom info window is configured)
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(pubLocation)
                    .title(pub.getName());

            // Add to the map a new marker, and store a reference to the Pub in the marker
            // (it will be used when showing custom info windows)
            Marker newMarker = map.addMarker(markerOptions);
            newMarker.setTag(pub);

            // Keep a reference to the added marker (in case we want clean the map later)
            currentMapMarkers.add(newMarker);
        }
    }


    /*** Pub Searching methods ***/

    private void searchPubsFirstPage(final @NonNull PubSearchParams searchParams, final @Nullable SwipeRefreshLayout swipeCaller) {

        searchParams.setOffset(0);

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Searching pubs...");
        if ( swipeCaller == null )
            pDialog.show();

        // Define what to do with the search results
        final SearchPubsInteractor.SearchPubsInteractorListener interactorListener = new SearchPubsInteractor.SearchPubsInteractorListener() {

            @Override
            public void onSearchPubsFail(Exception e) {

                if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                else                        pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to search pubs: "+ e.toString() );
                Utils.simpleDialog(EventPubsMapActivity.this, "Pub search error", e.getMessage());
            }

            @Override
            public void onSearchPubsSuccess(final PubAggregate pubs) {

                if (swipeCaller == null)    pDialog.dismiss();
                else                        swipeCaller.setRefreshing(false);

                lastPubSearchTotalResults = pubs.getTotalResults();

                /*
                pubListFragment = PubListFragment.newInstance(pubs, true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_event_pubs_map_fragment_list, pubListFragment)
                        .commit();

                mapFragment = (SupportMapFragment) getSupportFragmentManager()
                              .findFragmentById(R.id.activity_event_pubs_map_fragment_map);

                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        map = googleMap;
                        setupMap();

                        addPubMarkersToMap(pubs);
                    }
                });
                */

                setFragmentsContent(pubs, null, null, null, null);

                Utils.shortSnack(EventPubsMapActivity.this, pubs.getTotalResults() +" pub(s) found");
            }
        };


        // The current location will be sent only if it is available
        searchParams.setCoordinates(null, null);

        if ( !GeoManager.isLocationAccessGranted(this) ) {
            lastPubSearchParams = searchParams;
            new SearchPubsInteractor().execute(EventPubsMapActivity.this, searchParams, interactorListener);
        }
        else {
            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                    lastPubSearchParams = searchParams;
                    new SearchPubsInteractor().execute(EventPubsMapActivity.this, searchParams, interactorListener);
                }

                @Override
                public void onLocationSuccess(double latitude, double longitude) {
                    searchParams.setCoordinates(latitude, longitude);
                    lastPubSearchParams = searchParams;
                    new SearchPubsInteractor().execute(EventPubsMapActivity.this, searchParams, interactorListener);
                }
            });
        }
    }


    private void searchPubsNextPage(final @NonNull PubSearchParams searchParams) {

        int newOffset = searchParams.getOffset() + searchParams.getLimit();

        // If we are already at the last page of results, do nothing and return
        if (newOffset >= lastPubSearchTotalResults)
            return;

        searchParams.setOffset(newOffset);

        lastPubSearchParams = searchParams;

        // No need to set the location for "next page" requests,
        // so we can launch the request without checking permissions for the location services.
        new SearchPubsInteractor().execute(EventPubsMapActivity.this, searchParams, new SearchPubsInteractor.SearchPubsInteractorListener() {

            @Override
            public void onSearchPubsFail(Exception e) {

                Log.e(LOG_TAG, "Failed to search more pubs: "+ e.toString() );
                Utils.shortSnack(EventPubsMapActivity.this, "Error: "+ e.getMessage());
            }

            @Override
            public void onSearchPubsSuccess(PubAggregate pubs) {

                pubListFragment.addMorePubs(pubs);
                addPubMarkersToMap(pubs);
            }
        });
    }


    private void setFragmentsContent(@NonNull final PubAggregate pubs,
                                     @Nullable final Double lat,
                                     @Nullable final Double lon,
                                     @Nullable final Float zoom,
                                     @Nullable final Integer mapType) {

        // Set list content
        pubListFragment = PubListFragment.newInstance(pubs, true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_event_pubs_map_fragment_list, pubListFragment)
                .commit();

        // Set map content
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_event_pubs_map_fragment_map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                setupMap(lat, lon, zoom, mapType);

                addPubMarkersToMap(pubs);
            }
        });
    }





    /*** Implementation of PubListListener interface ***/

    @Override
    public void onPubClicked(Pub pub, int position) {
        Utils.shortSnack(EventPubsMapActivity.this, pub.getName() +" clicked.");
    }

    @Override
    public void onPubListSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        // Check if we have permission to access the device location, before performing a search
        locationChecker.checkBeforeAsking(new CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String msg = "Pick And Gol will not be able to search based on your location.";
                Utils.shortToast(EventPubsMapActivity.this, msg);

                searchPubsFirstPage(lastPubSearchParams, swipeCaller);
            }

            @Override
            public void onPermissionGranted() {
                searchPubsFirstPage(lastPubSearchParams, swipeCaller);
            }
        });
    }

    @Override
    public void onPubListLoadNextPage() {
        searchPubsNextPage(lastPubSearchParams);
    }


    private void finishActivity(@Nullable Error error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.getMessage());

        this.finish();
    }
}
