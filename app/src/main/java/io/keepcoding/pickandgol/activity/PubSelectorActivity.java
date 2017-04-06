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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.PubListAdapter;
import io.keepcoding.pickandgol.fragment.PubListFragment;
import io.keepcoding.pickandgol.interactor.SearchPubsInteractor;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.PubSearchParams;
import io.keepcoding.pickandgol.util.Utils;
import io.keepcoding.pickandgol.view.PubListListener;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

/**
 * This activity allows the user to search pubs by name
 * and select one of the matches to perform some operation with it on the previous activity.
 */
public class PubSelectorActivity extends AppCompatActivity implements PubListListener {

    private final static String LOG_TAG = "PubSelectorActivity";

    public final static String SELECTED_PUB_KEY = "SELECTED_PUB_KEY"; // Used to return the selected pub in the intent

    private SearchView searchView;

    // List fragment to show the pubs that match with the user's query
    private PubListFragment pubListFragment;

    private GeoManager gm;

    // References to the last pub search settings and their total results count
    // (both are necessary to paginate the results)
    private PubSearchParams lastPubSearchParams;
    private int lastPubSearchTotalResults;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_selector);

        gm = new GeoManager(this);

        setupActionBar();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pub_selector_menu, menu);

        // Configure the search view
        MenuItem searchItem = menu.findItem(R.id.activity_pub_selector_item_search);
        searchView = (SearchView) searchItem.getActionView();
        setupSearchView();

        return super.onCreateOptionsMenu(menu);
    }


    /*** Auxiliary methods: ***/

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Search Pub by name");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }


    // Sets the hint text and the behavior when the user types on the search box,
    // when he clicks the search button and when he closes the search view.
    private void setupSearchView() {

        if (searchView == null)
            return;

        searchView.setQueryHint("Enter Pub name");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {

                clearPreviousSearchResults();   // Empty the list
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                removeFocusFromSearchView();
                searchPubsFirstPage(buildSearchParams(query), null);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {

                clearPreviousSearchResults();   // Empty the list
                return false;
            }
        });
    }


    // Removes the focus from the search view and hides the screen keyboard
    // (useful when the user clicks the search button)
    private void removeFocusFromSearchView() {

        if (searchView != null)
            searchView.clearFocus();

        InputMethodManager inputManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), HIDE_NOT_ALWAYS);
    }

    private PubSearchParams buildSearchParams(String queryString) {

        return new PubSearchParams(null, queryString, null, 0, null, null);
    }

    // Creates the activity fragment and loads it with the given data
    private void initActivityFragment(@NonNull final PubAggregate pubs,
                                      @Nullable Integer scrollPosition) {

        // Set list content and position
        scrollPosition = (scrollPosition != null && scrollPosition >= 0) ? scrollPosition : 0;

        // Set list content
        pubListFragment = PubListFragment.newInstance(
                pubs, scrollPosition, PubListAdapter.LayoutType.ROWS_WITH_DETAIL_BUTTON);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_pub_selector_fragment_list, pubListFragment)
                .commit();
    }

    // Finishes this activity, passing back the selected pub (if any)
    private void finishActivity(@Nullable Pub selectedPub, @Nullable Exception error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.getMessage());

        Navigator.backFromPubSelectorActivity(this, selectedPub);
    }


    /*** Implementation of PubListListener interface ***/

    @Override
    public void onPubClicked(Pub pub, int position) {

        finishActivity(pub, null);
    }

    @Override
    public void onPubListSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        searchPubsFirstPage(lastPubSearchParams, swipeCaller);
    }

    @Override
    public void onPubListLoadNextPage() {
        searchPubsNextPage(lastPubSearchParams);
    }


    /*** List managing methods ***/

    // Removes all existing markers from the map, and empties the list
    private void clearPreviousSearchResults() {

        PubAggregate emptyPubs = PubAggregate.buildEmpty();
        lastPubSearchTotalResults = 0;

        initActivityFragment(emptyPubs, 0);
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

                        Log.e(LOG_TAG, "Failed to search pubs: "+ e.getMessage() );
                        Utils.simpleDialog(PubSelectorActivity.this, "Pub search error", e.getMessage());
                    }

                    @Override
                    public void onSearchPubsSuccess(PubAggregate pubs) {
                        if (swipeCaller == null)    pDialog.dismiss();
                        else                        swipeCaller.setRefreshing(false);

                        Utils.shortSnack(PubSelectorActivity.this, pubs.getTotalResults() +" pub(s) found");

                        lastPubSearchTotalResults = pubs.getTotalResults();
                        initActivityFragment(pubs, 0);
                    }
                };

        // The current location will be sent only if it is available
        searchParams.setCoordinates(null, null);

        if ( !GeoManager.isLocationAccessGranted(this) ) {
            lastPubSearchParams = searchParams;

            new SearchPubsInteractor().execute(PubSelectorActivity.this,
                                               searchParams,
                                               interactorListener);
        }
        else {
            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                    lastPubSearchParams = searchParams;

                    new SearchPubsInteractor().execute(PubSelectorActivity.this,
                                                       searchParams,
                                                       interactorListener);
                }

                @Override
                public void onLocationSuccess(double latitude, double longitude) {
                    searchParams.setCoordinates(latitude, longitude);
                    lastPubSearchParams = searchParams;

                    new SearchPubsInteractor().execute(PubSelectorActivity.this,
                                                       searchParams,
                                                       interactorListener);
                }
            });
        }
    }

    // Launches a server query to get the next page of results (if we are not in the last page yet).
    // If the query is successful, shows the new data among the already existing data in the fragment.
    private void searchPubsNextPage(final @NonNull PubSearchParams searchParams) {

        int newOffset = searchParams.getOffset() + searchParams.getLimit();

        // If we are already at the last page of results, do nothing and return
        if (newOffset >= lastPubSearchTotalResults)
            return;

        searchParams.setOffset(newOffset);
        lastPubSearchParams = searchParams;

        // No need to set the location for "next page" requests,
        // so we can launch the request without checking permissions for the location services.
        new SearchPubsInteractor().execute(PubSelectorActivity.this,
                                           searchParams,
                                           new SearchPubsInteractor.SearchPubsInteractorListener() {
                    @Override
                    public void onSearchPubsFail(Exception e) {
                        Log.e(LOG_TAG, "Failed to search more events: "+ e.getMessage() );
                        Utils.shortSnack(PubSelectorActivity.this, "Error: "+ e.getMessage());
                    }

                    @Override
                    public void onSearchPubsSuccess(PubAggregate events) {
                        pubListFragment.addMorePubs(events);
                    }
                });
    }
}
