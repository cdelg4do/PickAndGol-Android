package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.fragment.PubListFragment;
import io.keepcoding.pickandgol.interactor.GetFavoritesInteractor;
import io.keepcoding.pickandgol.interactor.GetFavoritesInteractor.GetFavoritesInteractorListener;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.Utils;
import io.keepcoding.pickandgol.view.PubListListener;


public class FavoritesActivity extends AppCompatActivity implements PubListListener {

    private final static String LOG_TAG = "FavoritesActivity";

    // List fragment to show the favorites
    private PubListFragment pubListFragment;

    private SessionManager sm;
    private GeoManager gm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        sm = SessionManager.getInstance(this);
        gm = new GeoManager(this);

        setupActionBar();

        if (!sm.hasSessionStored()) {

            Utils.simpleDialog(this,
                               "You are not logged in",
                               "First you must log in to the system.",
                               new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishActivity(new Error("The user is not logged in"));
                }
            });

            return;
        }

        loadPubsThenShowThem(null);
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


    /*** Auxiliary methods: ***/

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("My Favorite Pubs");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Creates the activity fragment and loads it with the given data
    private void initActivityFragment(@NonNull final PubAggregate pubs,
                                      @Nullable Integer scrollPosition) {

        // Set list content and position
        scrollPosition = (scrollPosition != null && scrollPosition >= 0) ? scrollPosition : 0;

        // Set list content
        pubListFragment = PubListFragment.newInstance(pubs, scrollPosition, true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_favorites_fragment_list, pubListFragment)
                .commit();
    }

    // Configures and launches a new server query to get the first page of results
    private void loadPubsThenShowThem(final @Nullable SwipeRefreshLayout swipeCaller) {

        if (!sm.hasSessionStored())
            return;

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Searching favorites...");
        if ( swipeCaller == null )
            pDialog.show();

        final String userId = sm.getUserId();
        final String token = sm.getSessionToken();

        new GetFavoritesInteractor().execute(
                FavoritesActivity.this,
                userId,
                token,
                new GetFavoritesInteractorListener() {

                    @Override
                    public void onGetFavoritesFail(Exception e) {

                        if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                        else                        pDialog.dismiss();

                        Log.e(LOG_TAG, "Failed to retrieve favorites for user '" + userId + "': " + e.getMessage() );
                        Utils.simpleDialog(FavoritesActivity.this, "Favorite listing error", e.getMessage());
                    }

                    @Override
                    public void onGetFavoritesSuccess(PubAggregate favorites) {

                        if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                        else                        pDialog.dismiss();

                        Utils.shortSnack(FavoritesActivity.this, "Showing "+ favorites.getTotalResults() +" pub(s)");
                        initActivityFragment(favorites, 0);
                    }
                });
    }

    // Finishes the activity, logging the given error message (if not null)
    private void finishActivity(@Nullable Error error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.getMessage());

        this.finish();
    }


    /*** Implementation of PubListListener interface ***/

    @Override
    public void onPubClicked(Pub pub, int position) {

        Navigator.fromFavoritesActivityToPubDetailActivity(this, pub);
    }

    @Override
    public void onPubListSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        loadPubsThenShowThem(swipeCaller);
    }

    @Override
    public void onPubListLoadNextPage() {

        // The query for favorites is not paginated, so nothing to do here
    }
}
