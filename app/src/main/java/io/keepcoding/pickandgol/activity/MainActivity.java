package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.PubListAdapter;
import io.keepcoding.pickandgol.dialog.LoginDialog;
import io.keepcoding.pickandgol.fragment.EventListFragment;
import io.keepcoding.pickandgol.fragment.PubListFragment;
import io.keepcoding.pickandgol.interactor.LoginInteractor;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor.SearchEventsInteractorListener;
import io.keepcoding.pickandgol.interactor.SearchPubsInteractor;
import io.keepcoding.pickandgol.interactor.SearchPubsInteractor.SearchPubsInteractorListener;
import io.keepcoding.pickandgol.interactor.UpdateUserInfoInteractor;
import io.keepcoding.pickandgol.interactor.UpdateUserInfoInteractor.UpdateUserInfoInteractorListener;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.EventSearchParams;
import io.keepcoding.pickandgol.search.PubSearchParams;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.Utils;
import io.keepcoding.pickandgol.view.EventListListener;
import io.keepcoding.pickandgol.view.PubListListener;

import static io.keepcoding.pickandgol.activity.LocationPickerActivity.STANDARD_MAP_LATITUDE;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.STANDARD_MAP_LONGITUDE;
import static io.keepcoding.pickandgol.activity.MainActivity.ShowingFragment.EVENT_LIST;
import static io.keepcoding.pickandgol.activity.MainActivity.ShowingFragment.PUB_LIST;
import static io.keepcoding.pickandgol.interactor.LoginInteractor.LoginInteractorListener;
import static io.keepcoding.pickandgol.util.PermissionChecker.CheckPermissionListener;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.LOCATION_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.PICTURES_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.RW_STORAGE_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_LOCATION_PERMISSION;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_PICTURES_PERMISSION;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_STORAGE_PERMISSION;


/**
 * This class is the application main activity
 */
public class MainActivity extends AppCompatActivity implements EventListListener, PubListListener {

    private final static String LOG_TAG = "MainActivity";

    // Keys used to pass/receive parameters inside an intent to/from other activity
    public static String CURRENT_EVENT_SEARCH_PARAMS_KEY = "CURRENT_EVENT_SEARCH_PARAMS_KEY";
    public static String CURRENT_PUB_SEARCH_PARAMS_KEY = "CURRENT_PUB_SEARCH_PARAMS_KEY";
    public static String SHOW_DISTANCE_SELECTOR_KEY = "SHOW_DISTANCE_SELECTOR_KEY";
    public static String NEW_EVENT_SEARCH_PARAMS_KEY = "NEW_EVENT_SEARCH_PARAMS_KEY";
    public static String NEW_PUB_SEARCH_PARAMS_KEY = "NEW_PUB_SEARCH_PARAMS_KEY";

    // Key strings to save/recover the activity state
    private final String ACTIONBAR_TITLE_SAVED_STATE = "ACTIONBAR_TITLE_SAVED_STATE";
    private final String LAST_EVENT_SEARCH_SAVED_STATE = "LAST_EVENT_SEARCH_SAVED_STATE";
    private final String LAST_PUB_SEARCH_SAVED_STATE = "LAST_PUB_SEARCH_SAVED_STATE";
    private final String EVENT_TOTAL_RESULTS_SAVED_STATE = "EVENT_TOTAL_RESULTS_SAVED_STATE";
    private final String PUB_TOTAL_RESULTS_SAVED_STATE = "PUB_TOTAL_RESULTS_SAVED_STATE";
    private final String SHOWING_FRAGMENT_SAVED_STATE = "SHOWING_FRAGMENT_SAVED_STATE";

    // This is the drawer option that will be selected as default when the activity starts
    private final int DEFAULT_DRAWER_ITEM = R.id.drawer_menu_event_search;

    // This helps to know what was the fragment showing before the activity was destroyed
    // (if it is OTHER, it means that does not mind)
    public enum ShowingFragment {
        EVENT_LIST,
        PUB_LIST,
        OTHER
    }

    // Managers used by this activity
    private SessionManager sm;
    private ImageManager im;
    private GeoManager gm;

    // Permission Checkers used by this activity
    private PermissionChecker storageChecker;
    private PermissionChecker picturesChecker;
    private PermissionChecker locationChecker;

    // References to the fragments used by this activity
    private EventListFragment eventListFragment;
    private PubListFragment pubListFragment;

    // References to the last event/pub search settings and their total results count
    private EventSearchParams lastEventSearchParams;
    private PubSearchParams lastPubSearchParams;
    private int lastEventSearchTotalResults;
    private int lastPubSearchTotalResults;

    // References to the activity action bar title and indicator of what fragment is currently shown
    // (useful to save/restore the activity state)
    private String actionBarTitle;
    private ShowingFragment showingFragment;

    // References to the drawer and to the drawer header
    private DrawerLayout mainDrawer;
    private View drawerHeader;

    // References to the header UI elements (bind with Butterknife, after the header is inflated)
    @BindView(R.id.drawer_profile_username) TextView profileNameText;
    @BindView(R.id.drawer_profile_email) TextView profileEmailText;
    @BindView(R.id.drawer_profile_image) ImageView profileImage;

    // Reference to the activity UI elements
    private FloatingActionButton btnNew;   // only for Pub list


    /*** Activity life cycle and setup methods ***/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageChecker = new PermissionChecker(RW_STORAGE_SET, this);
        picturesChecker = new PermissionChecker(PICTURES_SET, this);
        locationChecker = new PermissionChecker(LOCATION_SET, this);

        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);
        gm = new GeoManager(this);

        setupActionBar();
        setupDrawer();

        updateHeaderFromSessionInfo();

        if (savedInstanceState == null) {

            showingFragment = ShowingFragment.OTHER;

            // Set initial event search and initial pub search params
            lastEventSearchParams = EventSearchParams.buildEmptyParams();
            lastEventSearchTotalResults = 0;

            lastPubSearchParams = PubSearchParams.buildEmptyParams();
            lastPubSearchTotalResults = 0;

            doDefaultOperation();
        }

        else
            restoreActivityState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                mainDrawer.openDrawer(GravityCompat.START);
                return true;

            case R.id.main_menu_search:

                if (showingFragment == EVENT_LIST)

                    Navigator.fromMainActivityToEventSearchActivity(
                            this,
                            lastEventSearchParams,
                            GeoManager.isLocationAccessGranted(this)
                    );

                else if (showingFragment == PUB_LIST)

                    Navigator.fromMainActivityToPubSearchActivity(
                            this,
                            lastPubSearchParams
                    );

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "Saving activity state...");

        outState.putString(ACTIONBAR_TITLE_SAVED_STATE, actionBarTitle);
        outState.putSerializable(LAST_EVENT_SEARCH_SAVED_STATE, lastEventSearchParams);
        outState.putInt(EVENT_TOTAL_RESULTS_SAVED_STATE, lastEventSearchTotalResults);
        outState.putSerializable(LAST_PUB_SEARCH_SAVED_STATE, lastPubSearchParams);
        outState.putInt(PUB_TOTAL_RESULTS_SAVED_STATE, lastPubSearchTotalResults);
        outState.putSerializable(SHOWING_FRAGMENT_SAVED_STATE, showingFragment);

        super.onSaveInstanceState(outState);
    }

    // Restore the state the activity had just before it was destroyed
    private void restoreActivityState(final @NonNull Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Restoring activity state...");

        actionBarTitle = savedInstanceState.getString(ACTIONBAR_TITLE_SAVED_STATE, "");
        setTitle(actionBarTitle);

        lastEventSearchParams = (EventSearchParams) savedInstanceState.getSerializable(LAST_EVENT_SEARCH_SAVED_STATE);
        lastEventSearchTotalResults = savedInstanceState.getInt(EVENT_TOTAL_RESULTS_SAVED_STATE);

        lastPubSearchParams = (PubSearchParams) savedInstanceState.getSerializable(LAST_PUB_SEARCH_SAVED_STATE);
        lastPubSearchTotalResults = savedInstanceState.getInt(PUB_TOTAL_RESULTS_SAVED_STATE);

        showingFragment = (ShowingFragment) savedInstanceState.getSerializable(SHOWING_FRAGMENT_SAVED_STATE);

        if (showingFragment == EVENT_LIST) {
            eventListFragment = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.mainContentFragment_placeholder);
            hideFloatingButton();
        }

        else if (showingFragment == PUB_LIST) {
            pubListFragment = (PubListFragment) getSupportFragmentManager().findFragmentById(R.id.mainContentFragment_placeholder);
            showNewPubButton();
        }
    }

    // Set the layout toolbar as the activity action bar
    // and show the icon to open/close the drawer as the home button
    private void setupActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // Gets references to the drawer and the drawer header.
    // Binds the header inner views (with Butterknife).
    // Sets listeners for the drawer items and drawer header.
    private void setupDrawer() {

        mainDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView == null)
            return;

        drawerHeader = navigationView.getHeaderView(0);
        if (drawerHeader == null)
            return;

        ButterKnife.bind(this, drawerHeader);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        onDrawerItemSelected( menuItem );
                        return true;
                    }
                }
        );

        drawerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDrawerHeaderSelected();
            }
        });
    }

    // Automatically selects the drawer item given by DEFAULT_DRAWER_ITEM
    private void doDefaultOperation() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView == null)
            return;

        MenuItem defaultDrawerItem = navigationView.getMenu().findItem(DEFAULT_DRAWER_ITEM);
        onDrawerItemSelected(defaultDrawerItem);
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    // Actions to perform after a permission checker asks for some permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_FOR_STORAGE_PERMISSION)
            storageChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_PICTURES_PERMISSION)
            picturesChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_LOCATION_PERMISSION)
            locationChecker.checkAfterAsking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the Event Search Settings Activity, launch a new Event search
        if (requestCode == Navigator.EVENT_SEARCH_ACTIVITY_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            final EventSearchParams newSearchParams =
                    (EventSearchParams) data.getSerializableExtra(NEW_EVENT_SEARCH_PARAMS_KEY);

            // Check if we have permission to access the device location, before performing a search
            locationChecker.checkBeforeAsking(new CheckPermissionListener() {
                @Override
                public void onPermissionDenied() {
                    String msg = getString(R.string.main_activity_location_denied);
                    Utils.shortToast(MainActivity.this, msg);

                    searchEventsFirstPage(newSearchParams, null);
                    mainDrawer.closeDrawers();
                }

                @Override
                public void onPermissionGranted() {
                    searchEventsFirstPage(newSearchParams, null);
                    mainDrawer.closeDrawers();
                }
            });
        }

        // If we are coming from the Pub Search Settings Activity, launch a new Pub search
        else if (requestCode == Navigator.PUB_SEARCH_ACTIVITY_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            final PubSearchParams newSearchParams =
                    (PubSearchParams) data.getSerializableExtra(NEW_PUB_SEARCH_PARAMS_KEY);

            searchPubsFirstPage(newSearchParams, null);
            mainDrawer.closeDrawers();
        }

        // If we come from the Edit User Activity, update the session info and the drawer header
        else if (requestCode == Navigator.EDIT_USER_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {

            User userModified = (User) data.getSerializableExtra(EditUserActivity.SAVED_USER_KEY);

            if (userModified != null) {
                updateSessionInfo(userModified);
                updateHeaderFromSessionInfo();
            }
        }
    }


    /*** Menu action selectors (from the drawer & the action bar) ***/

    // Action to perform when the drawer header is selected
    private void onDrawerHeaderSelected() {

        if ( sm.hasSessionStored() )
            Navigator.fromMainActivityToEditUserActivity(this);

        else
            Utils.simpleDialog(this, getString(R.string.main_activity_not_in_session_title),
                    getString(R.string.main_activity_not_in_session_message));

        mainDrawer.closeDrawers();
    }

    // Action to perform when an item of the drawer menu is selected
    private void onDrawerItemSelected(final MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.drawer_menu_event_search:

                showingFragment = EVENT_LIST;

                actionBarTitle = menuItem.getTitle().toString();
                setTitle(actionBarTitle);
                hideFloatingButton();

                // Check if we have permission to access the device location, before performing a search
                locationChecker.checkBeforeAsking(new CheckPermissionListener() {
                    @Override
                    public void onPermissionDenied() {
                        String msg = getString(R.string.main_activity_location_denied);
                        Utils.shortToast(MainActivity.this, msg);

                        searchEventsFirstPage(lastEventSearchParams, null);
                        mainDrawer.closeDrawers();
                    }

                    @Override
                    public void onPermissionGranted() {
                        searchEventsFirstPage(lastEventSearchParams, null);
                        mainDrawer.closeDrawers();
                    }
                });

                break;

            case R.id.drawer_menu_pub_search:

                showingFragment = PUB_LIST;

                actionBarTitle = menuItem.getTitle().toString();
                setTitle(actionBarTitle);
                showNewPubButton();

                // Set a default location for the search (used in case of Device location unavailable)
                lastPubSearchParams.setCoordinates(STANDARD_MAP_LATITUDE, STANDARD_MAP_LONGITUDE);

                // Check if we have permission to access the device location, before performing a search
                locationChecker.checkBeforeAsking(new CheckPermissionListener() {
                    @Override
                    public void onPermissionDenied() {
                        String msg = getString(R.string.main_activity_location_denied);
                        Utils.shortToast(MainActivity.this, msg);

                        searchPubsFirstPage(lastPubSearchParams, null);
                        mainDrawer.closeDrawers();
                    }

                    @Override
                    public void onPermissionGranted() {

                        gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                            @Override
                            public void onLocationError(Throwable error) {

                                searchPubsFirstPage(lastPubSearchParams, null);
                                mainDrawer.closeDrawers();
                            }

                            @Override
                            public void onLocationSuccess(double latitude, double longitude) {

                                lastPubSearchParams.setCoordinates(latitude, longitude);

                                searchPubsFirstPage(lastPubSearchParams, null);
                                mainDrawer.closeDrawers();
                            }
                        });
                    }
                });

                break;

            case R.id.drawer_menu_my_favorites:

                Navigator.fromMainActivityToFavoritesActivity(this);
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_log_in:

                attemptToLogIn();
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_log_out:

                attemptToLogOut();
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_register:

                attemptToRegister();
                mainDrawer.closeDrawers();
                break;

            default:

                break;
        }
    }


    /*** Operations triggered by the menu action selectors ***/

    // Launches an Event search for the first page of results, using the given search parameters
    private void searchEventsFirstPage(final @NonNull EventSearchParams searchParams,
                                       final @Nullable SwipeRefreshLayout swipeCaller) {

        searchParams.setOffset(0);

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, getString(R.string.main_activity_searching_events));
        if ( swipeCaller == null )
            pDialog.show();

        // Define what to do with the search results
        final SearchEventsInteractorListener interactorListener = new SearchEventsInteractorListener() {

            @Override
            public void onSearchEventsFail(Exception e) {

                if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                else                        pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to search events: "+ e.toString() );
                Utils.simpleDialog(MainActivity.this, getString(R.string.main_activity_event_search_error), e.getMessage());
            }

            @Override
            public void onSearchEventsSuccess(EventAggregate events) {

                if (swipeCaller == null)    pDialog.dismiss();
                else                        swipeCaller.setRefreshing(false);

                lastEventSearchTotalResults = events.getTotalResults();

                eventListFragment = EventListFragment.newInstance(events, false);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContentFragment_placeholder, eventListFragment)
                        .commit();

                Utils.shortSnack(MainActivity.this, events.getTotalResults() + " " + getString(R.string.main_activity_events_found));
            }
        };


        // The current location will be sent only if it is available
        searchParams.setCoordinates(null, null);

        if ( !GeoManager.isLocationAccessGranted(this) ) {
            lastEventSearchParams = searchParams;
            new SearchEventsInteractor().execute(MainActivity.this, searchParams, interactorListener);
        }
        else {
            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                    lastEventSearchParams = searchParams;
                    new SearchEventsInteractor().execute(MainActivity.this, searchParams, interactorListener);
                }

                @Override
                public void onLocationSuccess(double latitude, double longitude) {
                    searchParams.setCoordinates(latitude, longitude);
                    lastEventSearchParams = searchParams;
                    new SearchEventsInteractor().execute(MainActivity.this, searchParams, interactorListener);
                }
            });
        }
    }

    // Launches an Event search for the next page of results, using the given search parameters
    private void searchEventsNextPage(final @NonNull EventSearchParams searchParams) {

        int newOffset = searchParams.getOffset() + searchParams.getLimit();

        // If we are already at the last page of results, do nothing and return
        if (newOffset >= lastEventSearchTotalResults)
            return;

        searchParams.setOffset(newOffset);

        lastEventSearchParams = searchParams;

        // No need to set the location for "next page" requests,
        // so we can launch the request without checking permissions for the location services.
        new SearchEventsInteractor().execute(MainActivity.this, searchParams, new SearchEventsInteractorListener() {

            @Override
            public void onSearchEventsFail(Exception e) {

                Log.e(LOG_TAG, "Failed to search more events: "+ e.toString() );
                Utils.shortSnack(MainActivity.this, getString(R.string.main_activity_error) + e.getMessage());
            }

            @Override
            public void onSearchEventsSuccess(EventAggregate events) {

                eventListFragment.addMoreEvents(events);
            }
        });
    }

    // Launches a Pub search for the first page of results, using the given search parameters
    private void searchPubsFirstPage(final @NonNull PubSearchParams searchParams,
                                     final @Nullable SwipeRefreshLayout swipeCaller) {

        searchParams.setOffset(0);

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, getString(R.string.searching_pubs));
        if ( swipeCaller == null )
            pDialog.show();

        // Define what to do with the search results
        final SearchPubsInteractorListener interactorListener = new SearchPubsInteractorListener() {

            @Override
            public void onSearchPubsFail(Exception e) {

                if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                else                        pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to search pubs: "+ e.toString() );
                Utils.simpleDialog(MainActivity.this, getString(R.string.pub_search_error), e.getMessage());
            }

            @Override
            public void onSearchPubsSuccess(PubAggregate pubs) {

                if (swipeCaller == null)    pDialog.dismiss();
                else                        swipeCaller.setRefreshing(false);

                lastPubSearchTotalResults = pubs.getTotalResults();

                pubListFragment = PubListFragment.newInstance(pubs, 0, PubListAdapter.LayoutType.CELLS);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContentFragment_placeholder, pubListFragment)
                        .commit();

                Utils.shortSnack(MainActivity.this, pubs.getTotalResults() + " " + getString(R.string.pubs_found));
            }
        };

        // Save this search parameters for future use and launch the new search
        lastPubSearchParams = searchParams;
        new SearchPubsInteractor().execute(MainActivity.this, searchParams, interactorListener);
    }

    // Launches a Pub search for the next page of results, using the given search parameters
    private void searchPubsNextPage(final @NonNull PubSearchParams searchParams) {

        int newOffset = searchParams.getOffset() + searchParams.getLimit();

        // If we are already at the last page of results, do nothing and return
        if (newOffset >= lastPubSearchTotalResults)
            return;

        searchParams.setOffset(newOffset);

        lastPubSearchParams = searchParams;

        // No need to set the location for "next page" requests,
        // so we can launch the request without checking permissions for the location services.
        new SearchPubsInteractor().execute(MainActivity.this, searchParams, new SearchPubsInteractorListener() {

            @Override
            public void onSearchPubsFail(Exception e) {

                Log.e(LOG_TAG, "Failed to search more pubs: "+ e.toString() );
                Utils.shortSnack(MainActivity.this, getString(R.string.main_activity_error) + e.getMessage());
            }

            @Override
            public void onSearchPubsSuccess(PubAggregate pubs) {

                pubListFragment.addMorePubs(pubs);
            }
        });
    }

    // Attempts to authenticate against the server, with an user email and password
    private void doLoginOperation(final @NonNull String email, final @NonNull String password) {

        final ProgressDialog pDialog = Utils.newProgressDialog(this, getString(R.string.main_activity_login_in_progress));
        pDialog.show();

        new LoginInteractor().execute(this, email, password, new LoginInteractorListener() {

            @Override
            public void onLoginFail(Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "Failed to login: "+ e.toString() );

                Utils.simpleDialog(MainActivity.this, getString(R.string.main_activity_login_error), e.getMessage());
            }

            @Override
            public void onLoginSuccess() {
                pDialog.dismiss();

                updateHeaderFromSessionInfo();
                Utils.shortSnack(MainActivity.this, getString(R.string.main_activity_login_as)+ sm.getUserName() +"'.");

                final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                sendRegistrationToServer(refreshedToken);
            }
        });
    }

    // Stores the given registration token in the server
    private void sendRegistrationToServer(String refreshedToken) {

        User user = new User(sm.getUserId());
        user.setRegistrationToken(refreshedToken);

        new UpdateUserInfoInteractor().execute(this, sm.getSessionToken(), user,
                                               new UpdateUserInfoInteractorListener() {
            @Override
            public void onUpdateUserSuccess(User user) {
            }

            @Override
            public void onUpdateUserFail(Exception e) {
            }
        });
    }

    // Updates the local session info, after the user profile has been changed
    private void updateSessionInfo(final User newUserInfo) {

        if ( sm.getUserEmail() == null || !sm.getUserEmail().equals(newUserInfo.getEmail()) )
            sm.updateUserEmail(newUserInfo.getEmail());

        if ( sm.getUserName() == null || !sm.getUserName().equals(newUserInfo.getName()) )
            sm.updateUserName(newUserInfo.getName());
	
        if (sm.getUserPhotoUrl() == null || !sm.getUserPhotoUrl().equals(newUserInfo.getPhotoUrl())) {
            sm.updatePhotoUrl(newUserInfo.getPhotoUrl());
        }
    }


    /*** Auxiliary recurrent methods ***/

    // Shows the floating button and configures it to redirect to NewPubActivity
    private void showNewPubButton() {

        if (btnNew == null)
            btnNew = (FloatingActionButton) findViewById(R.id.activity_main_button_new);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptToCreatePub();
            }
        });

        btnNew.setVisibility(View.VISIBLE);
    }

    // Hides the floating button
    private void hideFloatingButton() {

        if (btnNew == null)
            btnNew = (FloatingActionButton) findViewById(R.id.activity_main_button_new);

        btnNew.setVisibility(View.GONE);
    }

    // From the log in drawer option, attempts to log in to the system
    private void attemptToLogIn() {

        if ( sm.hasSessionStored() ) {
            Utils.shortSnack(this, getString(R.string.main_activity_already_logged_in));
            return;
        }

        new LoginDialog(this, new LoginDialog.LoginDialogListener() {
            @Override
            public void onLoginClick(String email, String password) {
                doLoginOperation(email,password);
            }
        }).show();
    }

    // From the log out drawer option, attempts to log out the current session (if any)
    private void attemptToLogOut() {

        if ( !sm.hasSessionStored() ) {
            Utils.shortSnack(this, getString(R.string.main_activity_already_logged_out));
            return;
        }

        sm.destroySession();
        updateHeaderFromSessionInfo();
        Utils.simpleDialog(this, getString(R.string.main_activity_log_out_title),
                getString(R.string.main_activity_log_out_message));
    }

    // From the sign in drawer option, attempts to register a new user account
    private void attemptToRegister() {

        if ( sm.hasSessionStored() ) {
            Utils.shortSnack(this, getString(R.string.main_activity_no_new_account_while_logged_in));
            return;
        }

        Navigator.fromMainActivityToNewUserActivity(this);
    }

    // From the create pub floating button, attempts to redirect to NewPubActivity
    private void attemptToCreatePub() {

        if ( !sm.hasSessionStored() ) {
            Utils.simpleDialog(this, getString(R.string.main_activity_not_logged_in_title),
                    getString(R.string.main_activity_not_logged_in_message));
            return;
        }

        Navigator.fromMainActivityToNewPubActivity(MainActivity.this);
    }

    // Updates the header views with the information in the device's session
    // (if there is no session stored, just update the views with the default values)
    private void updateHeaderFromSessionInfo() {

        if (sm.hasSessionStored()) {

            profileNameText.setText( sm.getUserName() );
            profileEmailText.setText( sm.getUserEmail() );

            if ( sm.getUserPhotoUrl() != null )
                im.loadImage(sm.getUserPhotoUrl(), profileImage, R.drawable.default_avatar);
        }

        else {
            profileNameText.setText(R.string.main_activity_profile_name);
            profileEmailText.setText(R.string.main_activity_to_pick_and_gol);
            im.loadImage(R.drawable.default_avatar, profileImage);
        }
    }


    /*** Implementation of EventListListener interface ***/

    @Override
    public void onEventClicked(Event event, int position) {

        Navigator.fromMainActivityToEventDetailActivity(this, event);
    }

    @Override
    public void onEventListSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        // Check if we have permission to access the device location, before performing a search
        locationChecker.checkBeforeAsking(new CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String msg = getString(R.string.main_activity_location_denied);
                Utils.shortToast(MainActivity.this, msg);

                searchEventsFirstPage(lastEventSearchParams, swipeCaller);
            }

            @Override
            public void onPermissionGranted() {
                searchEventsFirstPage(lastEventSearchParams, swipeCaller);
            }
        });
    }

    @Override
    public void onEventListLoadNextPage() {
        searchEventsNextPage(lastEventSearchParams);
    }


    /*** Implementation of PubListListener interface ***/

    @Override
    public void onPubClicked(Pub pub, int position) {

        Navigator.fromMainActivityToPubDetailActivity(this, pub);
    }

    @Override
    public void onPubListSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        // Check if we have permission to access the device location, before performing a search
        locationChecker.checkBeforeAsking(new CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String msg = getString(R.string.main_activity_location_denied);
                Utils.shortToast(MainActivity.this, msg);

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
}
