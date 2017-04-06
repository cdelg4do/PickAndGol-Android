package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.fragment.EventListFragment;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.EventSearchParams;
import io.keepcoding.pickandgol.util.Utils;
import io.keepcoding.pickandgol.view.EventListListener;

import static io.keepcoding.pickandgol.navigator.Navigator.NEW_EVENT_ACTIVITY_REQUEST_CODE;

public class PubEventsActivity extends AppCompatActivity implements EventListListener {

    private final static String LOG_TAG = "PubEventsActivity";

    // Key strings for arguments passed in the intent
    public static final String MODEL_KEY = "MODEL_KEY";

    // Reference to the pub the showed events are related to, and to the list fragment
    private Pub model;
    private EventListFragment eventListFragment;

    // Reference to last query and total results counter (necessary to load next pages)
    private EventSearchParams lastEventSearchParams;
    private int lastEventSearchTotalResults;

    // GeoManager needed to send the user location (if available) with the query
    private GeoManager gm;

    // Reference to UI elements to be bound with Butterknife
    @BindView(R.id.activity_pub_events_button_new) FloatingActionButton btnCreateEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_events);
        ButterKnife.bind(this);

        gm = new GeoManager(this);

        setupActionBar();
        setupButtons();
        loadModel();

        if (model == null) {
            finishActivity(new Error("No Event was provided to the Activity"));
            return;
        }

        loadPubEventsThenShowThem();
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


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the new event activity
        if (requestCode == NEW_EVENT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            Utils.shortSnack(PubEventsActivity.this, "Refresh the list to see the new event.");
        }
    }


    /*** Auxiliary methods: ***/

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Future events in this pub");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Set listeners for the activity buttons
    private void setupButtons() {

        btnCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigator.fromPubEventsActivityToNewEventActivity(PubEventsActivity.this, model);
            }
        });
    }

    // Gets the model passed from the intent
    private void loadModel() {
        model = (Pub) getIntent().getSerializableExtra(MODEL_KEY);
    }

    // Creates the activity fragment and loads it with the given data
    private void initActivityFragment(@NonNull final EventAggregate events) {

        // Set list content
        eventListFragment = EventListFragment.newInstance(events, true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_pub_events_fragment_list, eventListFragment)
                .commit();
    }

    // Configures and launches a new server query to get the first page of results
    private void loadPubEventsThenShowThem() {

        if (model == null || model.getId() == null)
            return;

        String pubId = model.getId();

        lastEventSearchTotalResults = 0;
        lastEventSearchParams = new EventSearchParams(pubId, null, null, null, null, null, null);

        searchEventsFirstPage(lastEventSearchParams, null);
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
    private void searchEventsFirstPage(final @NonNull EventSearchParams searchParams,
                                       final @Nullable SwipeRefreshLayout swipeCaller) {

        searchParams.setOffset(0);

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Searching events...");
        if ( swipeCaller == null )
            pDialog.show();

        // Define what to do with the search results
        final SearchEventsInteractor.SearchEventsInteractorListener interactorListener =
                new SearchEventsInteractor.SearchEventsInteractorListener() {

            @Override
            public void onSearchEventsFail(Exception e) {
                if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                else                        pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to search events: "+ e.toString() );
                Utils.simpleDialog(PubEventsActivity.this, "Event search error", e.getMessage());
            }

            @Override
            public void onSearchEventsSuccess(EventAggregate events) {
                if (swipeCaller == null)    pDialog.dismiss();
                else                        swipeCaller.setRefreshing(false);

                Utils.shortSnack(PubEventsActivity.this, events.getTotalResults() +" event(s) found");

                lastEventSearchTotalResults = events.getTotalResults();
                initActivityFragment(events);
            }
        };

        // The current location will be sent only if it is available
        searchParams.setCoordinates(null, null);

        if ( !GeoManager.isLocationAccessGranted(this) ) {
            lastEventSearchParams = searchParams;

            new SearchEventsInteractor().execute(PubEventsActivity.this,
                    searchParams,
                    interactorListener);
        }
        else {
            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                    lastEventSearchParams = searchParams;

                    new SearchEventsInteractor().execute(PubEventsActivity.this,
                            searchParams,
                            interactorListener);
                }

                @Override
                public void onLocationSuccess(double latitude, double longitude) {
                    searchParams.setCoordinates(latitude, longitude);
                    lastEventSearchParams = searchParams;

                    new SearchEventsInteractor().execute(PubEventsActivity.this,
                            searchParams,
                            interactorListener);
                }
            });
        }
    }

    // Launches a server query to get the next page of results (if we are not in the last page yet).
    // If the query is successful, shows the new data among the already existing data in the fragment.
    private void searchEventsNextPage(final @NonNull EventSearchParams searchParams) {

        int newOffset = searchParams.getOffset() + searchParams.getLimit();

        // If we are already at the last page of results, do nothing and return
        if (newOffset >= lastEventSearchTotalResults)
            return;

        searchParams.setOffset(newOffset);
        lastEventSearchParams = searchParams;

        // No need to set the location for "next page" requests,
        // so we can launch the request without checking permissions for the location services.
        new SearchEventsInteractor().execute(PubEventsActivity.this,
                searchParams,
                new SearchEventsInteractor.SearchEventsInteractorListener() {
                    @Override
                    public void onSearchEventsFail(Exception e) {
                        Log.e(LOG_TAG, "Failed to search more events: "+ e.toString() );
                        Utils.shortSnack(PubEventsActivity.this, "Error: "+ e.getMessage());
                    }

                    @Override
                    public void onSearchEventsSuccess(EventAggregate events) {
                        eventListFragment.addMoreEvents(events);
                    }
                });
    }


    /*** Implementation of EventListListener interface ***/

    @Override
    public void onEventClicked(Event event, int position) {

        Navigator.fromPubEventsActivityToEventDetailActivity(PubEventsActivity.this, event);
    }

    @Override
    public void onEventListSwipeRefresh(@Nullable SwipeRefreshLayout swipeCaller) {

        searchEventsFirstPage(lastEventSearchParams, swipeCaller);
    }

    @Override
    public void onEventListLoadNextPage() {

        searchEventsNextPage(lastEventSearchParams);
    }
}
