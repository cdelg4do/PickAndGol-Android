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
import android.view.MenuItem;
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
import io.keepcoding.pickandgol.adapter.PubListAdapter;
import io.keepcoding.pickandgol.fragment.PubListFragment;
import io.keepcoding.pickandgol.interactor.SearchPubsInteractor;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.PubSearchParams;
import io.keepcoding.pickandgol.util.Utils;
import io.keepcoding.pickandgol.view.PubListListener;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;


/**
 * This activity shows both the list and the map of Pubs associated to an Event.
 * It sends requests to the server to query for the next page of Pubs, then show them on screen.
 */
public class EventPubsActivity extends AppCompatActivity implements PubListListener {

    private final static String LOG_TAG = "EventPubsActivity";

    // Key strings for arguments passed in the intent
    public static final String MODEL_KEY = "MODEL_KEY";
    public static final String SHOW_USER_LOCATION_KEY = "SHOW_USER_LOCATION_KEY";

    // Use this not to filter results by distance when sending the location in the queries
    private static final int MAX_DISTANCE_EARTH_KM = 20100;

    // Key strings to save/recover the activity state
    private static final String SAVED_STATE_PUB_LIST_KEY = "SAVED_STATE_PUB_LIST_KEY";
    private static final String SAVED_STATE_PUB_COUNT_KEY = "SAVED_STATE_PUB_COUNT_KEY";
    private static final String SAVED_STATE_MAP_LATITUDE_KEY = "SAVED_STATE_MAP_LATITUDE_KEY";
    private static final String SAVED_STATE_MAP_LONGITUDE_KEY = "SAVED_STATE_MAP_LONGITUDE_KEY";
    private static final String SAVED_STATE_MAP_ZOOM_KEY = "SAVED_STATE_MAP_ZOOM_KEY";
    private static final String SAVED_STATE_MAP_TYPE_KEY = "SAVED_STATE_MAP_TYPE_KEY";
    private static final String SAVED_STATE_LAST_SEARCH_KEY = "SAVED_STATE_LAST_SEARCH_KEY";
    private static final String SAVED_STATE_SCROLL_POSITION_KEY = "SAVED_STATE_SCROLL_POSITION_KEY";

    // Default Map location settings
    private static final double DEFAULT_MAP_LATITUDE = 40.41665;
    private static final double DEFAULT_MAP_LONGITUDE = -3.70381;
    private static final int DEFAULT_MAP_ZOOM = 5;

    // This the event the showed pubs are related to
    private Event model;

    // Reference to both fragments and elements of the map fragment
    private PubListFragment pubListFragment;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private ArrayList<Marker> currentMapMarkers;

    // Reference to UI elements to be bound with Butterknife (not before the map fragment is inflated)
    @BindView(R.id.activity_event_pubs_map_toggle_view) ImageView toggleMapView;

    // Reference to last query and total results counter (necessary to load next pages)
    private PubSearchParams lastPubSearchParams;
    private int lastPubSearchTotalResults;

    // GeoManager needed to send the user location (if available) with the query
    private GeoManager gm;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_pubs_map);

        gm = new GeoManager(this);

        setupActionBar();
        loadModel();

        if (model == null)
            finishActivity(new Error("No Event was provided to the Activity"));

        // If we have to restore the activity state
        else if (savedInstanceState != null)
            restoreActivityState(savedInstanceState);

        // If we have to load data into the activity for the first time
        else
            loadEventPubsThenShowThem();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*** Activity state saving/restoring ***/

    // To restore the activity state later, we have to save:
    //
    // - The last query sent to the server
    // - The total number of pubs associated to the event
    // - The pubs currently shown on screen
    // - The current map position, zoom and map type (normal/hybrid)
    // - The current scroll position of the pub list
    @Override
    public void onSaveInstanceState(Bundle outState) {

        Log.d(LOG_TAG, "Saving activity state...");

        ArrayList<Pub> pubList = (ArrayList<Pub>) pubListFragment.getPubs().getAll();
        double lat = map.getCameraPosition().target.latitude;
        double lon = map.getCameraPosition().target.longitude;
        Float zoom = map.getCameraPosition().zoom;
        int mapType = map.getMapType();

        Integer scrollPosition = pubListFragment.getLinearRecyclerScrollPosition();

        outState.putSerializable(SAVED_STATE_LAST_SEARCH_KEY, lastPubSearchParams);
        outState.putInt(SAVED_STATE_PUB_COUNT_KEY, lastPubSearchTotalResults);
        outState.putSerializable(SAVED_STATE_PUB_LIST_KEY, pubList);
        outState.putDouble(SAVED_STATE_MAP_LATITUDE_KEY, lat);
        outState.putDouble(SAVED_STATE_MAP_LONGITUDE_KEY, lon);
        outState.putFloat(SAVED_STATE_MAP_ZOOM_KEY, zoom);
        outState.putInt(SAVED_STATE_MAP_TYPE_KEY, mapType);

        if (scrollPosition != null)
            outState.putInt(SAVED_STATE_SCROLL_POSITION_KEY, scrollPosition);

        super.onSaveInstanceState(outState);
    }

    // Recover all data saved in onSaveInstanceState(), re-create the activity fragments
    // and paint everything on screen like it was before destroying the activity
    private void restoreActivityState(final @NonNull Bundle savedInstanceState) {

        Log.d(LOG_TAG, "Restoring activity state...");

        lastPubSearchParams = (PubSearchParams) savedInstanceState.getSerializable(SAVED_STATE_LAST_SEARCH_KEY);
        lastPubSearchTotalResults = savedInstanceState.getInt(SAVED_STATE_PUB_COUNT_KEY, 0);
        List<Pub> pubList = (List) savedInstanceState.getSerializable(SAVED_STATE_PUB_LIST_KEY);
        double lat = savedInstanceState.getDouble(SAVED_STATE_MAP_LATITUDE_KEY, DEFAULT_MAP_LATITUDE);
        double lon = savedInstanceState.getDouble(SAVED_STATE_MAP_LONGITUDE_KEY, DEFAULT_MAP_LONGITUDE);
        float zoom = savedInstanceState.getFloat(SAVED_STATE_MAP_ZOOM_KEY, DEFAULT_MAP_ZOOM);
        int mapType = savedInstanceState.getInt(SAVED_STATE_MAP_TYPE_KEY, MAP_TYPE_NORMAL);
        Integer scrollPosition = savedInstanceState.getInt(SAVED_STATE_SCROLL_POSITION_KEY, -1);

        PubAggregate restoredPubs = PubAggregate.buildFromList(pubList, lastPubSearchTotalResults);
        if (restoredPubs.getAll().size() == 0)
            return;

        initActivityFragments(restoredPubs, scrollPosition, lat, lon, zoom, mapType);
    }


    /*** Auxiliary methods: ***/

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Pubs showing this event");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Gets the model passed from the intent
    private void loadModel() {
        model = (Event) getIntent().getSerializableExtra(MODEL_KEY);
    }

    // Creates the activity fragments and loads them with the given data
    // (call this method after a successful server request, or when restoring the activity state)
    private void initActivityFragments(@NonNull final PubAggregate pubs,
                                       @Nullable Integer scrollPosition,
                                       @Nullable final Double lat,
                                       @Nullable final Double lon,
                                       @Nullable final Float zoom,
                                       @Nullable final Integer mapType) {

        // Set list content and position
        scrollPosition = (scrollPosition != null && scrollPosition >= 0) ? scrollPosition : 0;

        pubListFragment = PubListFragment.newInstance(pubs, scrollPosition, PubListAdapter.LayoutType.ROWS);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_event_pubs_map_fragment_list, pubListFragment)
                .commit();

        // Set map content and position
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_event_pubs_map_fragment_map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                setupMap(lat, lon, zoom, mapType);
                setMapPubMarkers(pubs);
            }
        });
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

        lat = (initialLat != null) ? initialLat : DEFAULT_MAP_LATITUDE;
        lon = (initialLon != null) ? initialLon : DEFAULT_MAP_LONGITUDE;
        zoom = (initialZoom != null) ? initialZoom : DEFAULT_MAP_ZOOM;

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

        // Show the user location (if available)
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

                Navigator.fromEventPubsActivityToPubDetailActivity(EventPubsActivity.this, pub);
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

    // Clears all existing map markers and the references to them,
    // then adds the markers for the given pubs to the map
    // (use when initializing the map fragment)
    private void setMapPubMarkers(@NonNull PubAggregate pubs) {

        map.clear();
        currentMapMarkers = null;

        addPubMarkersToMap(pubs);
    }

    // Adds markers to the map, corresponding to the given pubs
    // (it does not clear nor overwrite the existing markers, use when loading next page of results)
    private void addPubMarkersToMap(@NonNull PubAggregate pubs) {

        if (map == null || pubs == null)
            return;

        // If the marker list has not been initialized yet, do it now
        if (currentMapMarkers == null)
            currentMapMarkers = new ArrayList<>();

        for (Pub pub: pubs.getAll()) {

            LatLng pubLocation = new LatLng(pub.getLatitude(), pub.getLongitude());

            // Create a new marker with the pub location and the pub name (allows to show at least
            // the name in the marker's info window if no custom info window is configured later)
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(pubLocation)
                    .title(pub.getName());

            Marker newMarker = map.addMarker(markerOptions);

            // Store a reference to the Pub in the marker
            // (needed to show the Pub data in the custom info window)
            newMarker.setTag(pub);

            // Keep a reference to the added marker (in case we need to save/restore the activity)
            currentMapMarkers.add(newMarker);
        }
    }

    // Configures and launches a new server query to get the first page of results
    private void loadEventPubsThenShowThem() {

        if (model == null || model.getId() == null)
            return;

        lastPubSearchTotalResults = 0;
        lastPubSearchParams = new PubSearchParams(null,null,MAX_DISTANCE_EARTH_KM,0,null,null);
        lastPubSearchParams.setEventId( model.getId() );

        searchPubsFirstPage(lastPubSearchParams, null);
    }

    // Finishes the activity, logging the given error message (if not null)
    private void finishActivity(@Nullable Error error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.getMessage());

        this.finish();
    }


    /*** Pub Searching methods ***/

    // Launches a server query to get the first page of results.
    // If the query is successful, re-creates the activity fragments to show the data obtained.
    private void searchPubsFirstPage(final @NonNull PubSearchParams searchParams,
                                     final @Nullable SwipeRefreshLayout swipeCaller) {

        searchParams.setOffset(0);

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Searching pubs...");
        if ( swipeCaller == null )
            pDialog.show();

        // Define what to do with the search results
        final SearchPubsInteractor.SearchPubsInteractorListener interactorListener =
                new SearchPubsInteractor.SearchPubsInteractorListener() {

            @Override
            public void onSearchPubsFail(Exception e) {

                if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                else                        pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to search pubs: "+ e.toString() );
                Utils.simpleDialog(EventPubsActivity.this, "Pub search error", e.getMessage());
            }

            @Override
            public void onSearchPubsSuccess(final PubAggregate pubs) {

                if (swipeCaller == null)    pDialog.dismiss();
                else                        swipeCaller.setRefreshing(false);

                Utils.shortSnack(EventPubsActivity.this, pubs.getTotalResults() +" pub(s) found");

                lastPubSearchTotalResults = pubs.getTotalResults();
                initActivityFragments(pubs, null, null, null, null, null);
            }
        };


        // The current location will be sent only if it is available
        searchParams.setCoordinates(null, null);

        if ( !GeoManager.isLocationAccessGranted(this) ) {
            lastPubSearchParams = searchParams;

            new SearchPubsInteractor().execute(EventPubsActivity.this,
                                               searchParams,
                                               interactorListener);
        }
        else {
            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                    lastPubSearchParams = searchParams;

                    new SearchPubsInteractor().execute(EventPubsActivity.this,
                                                       searchParams,
                                                       interactorListener);
                }

                @Override
                public void onLocationSuccess(double latitude, double longitude) {
                    searchParams.setCoordinates(latitude, longitude);
                    lastPubSearchParams = searchParams;

                    new SearchPubsInteractor().execute(EventPubsActivity.this,
                                                       searchParams,
                                                       interactorListener);
                }
            });
        }
    }

    // Launches a server query to get the next page of results (if we are not in the last page yet).
    // If the query is successful, shows the new data among the already existing data in the fragments.
    private void searchPubsNextPage(final @NonNull PubSearchParams searchParams) {

        int newOffset = searchParams.getOffset() + searchParams.getLimit();

        // If we are already at the last page of results, do nothing and return
        if (newOffset >= lastPubSearchTotalResults)
            return;

        searchParams.setOffset(newOffset);
        lastPubSearchParams = searchParams;

        // No need to set the location for "next page" requests,
        // so we can launch the request without checking permissions for the location services.
        new SearchPubsInteractor().execute(EventPubsActivity.this,
                                           searchParams,
                                           new SearchPubsInteractor.SearchPubsInteractorListener() {

            @Override
            public void onSearchPubsFail(Exception e) {

                Log.e(LOG_TAG, "Failed to search more pubs: "+ e.toString() );
                Utils.shortSnack(EventPubsActivity.this, "Error: "+ e.getMessage());
            }

            @Override
            public void onSearchPubsSuccess(PubAggregate pubs) {

                pubListFragment.addMorePubs(pubs);
                addPubMarkersToMap(pubs);
            }
        });
    }


    /*** Implementation of PubListListener interface ***/

    @Override
    public void onPubClicked(Pub pub, int position) {

        Navigator.fromEventPubsActivityToPubDetailActivity(this, pub);
    }

    @Override
    public void onPubListSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        searchPubsFirstPage(lastPubSearchParams, swipeCaller);
    }

    @Override
    public void onPubListLoadNextPage() {

        searchPubsNextPage(lastPubSearchParams);
    }
}
