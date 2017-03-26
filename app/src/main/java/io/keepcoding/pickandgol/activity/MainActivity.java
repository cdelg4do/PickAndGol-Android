package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.dialog.LoginDialog;
import io.keepcoding.pickandgol.fragment.EventListFragment;
import io.keepcoding.pickandgol.fragment.MainContentFragment;
import io.keepcoding.pickandgol.interactor.LoginInteractor;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor.SearchEventsInteractorListener;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImageProcessingListener;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.EventSearchParams;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.activity.MainActivity.ImagePurpose.UPLOAD_TO_CLOUD;
import static io.keepcoding.pickandgol.interactor.LoginInteractor.LoginInteractorListener;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;
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
public class MainActivity extends AppCompatActivity implements EventListFragment.EventListListener {

    private final static String LOG_TAG = "MainActivity";

    public static final int LOCATION_PICKER_PERMISSION_REQ_CODE = 501;

    public static String CURRENT_EVENT_SEARCH_PARAMS_KEY = "CURRENT_EVENT_SEARCH_PARAMS_KEY";
    public static String SHOW_DISTANCE_SELECTOR_KEY = "SHOW_DISTANCE_SELECTOR_KEY";
    public static String NEW_EVENT_SEARCH_PARAMS_KEY = "NEW_EVENT_SEARCH_PARAMS_KEY";

    // This helps to know what was the fragment showing before the activity was destroyed
    // (if it is OTHER, it means that does not mind)
    private enum ShowingFragment {
        EVENT_LIST,
        OTHER
    }

    // When choosing an image from the picker, this helps to know what to do with it
    // (like upload the selected image to the cloud, load it into an ImageView, etc.)
    public enum ImagePurpose {
        UPLOAD_TO_CLOUD
    }

    private final String ACTIONBAR_TITLE_SAVED_STATE = "ACTIONBAR_TITLE_SAVED_STATE";
    private final String LAST_EVENT_SEARCH_SAVED_STATE = "LAST_EVENT_SEARCH_SAVED_STATE";
    private final String LAST_TOTAL_RESULTS_SAVED_STATE = "LAST_TOTAL_RESULTS_SAVED_STATE";
    private final String SHOWING_FRAGMENT_SAVED_STATE = "SHOWING_FRAGMENT_SAVED_STATE";

    private final int DEFAULT_DRAWER_ITEM = R.id.event_search;

    private SessionManager sm;
    private ImageManager im;
    private GeoManager gm;

    private ImagePurpose lastPickedImagePurpose;  // stores the purpose of the last picked image

    private Fragment mainFragment;
    private EventListFragment eventListFragment;

    private EventSearchParams lastEventSearchParams;
    private int lastEventSearchTotalResults;

    private DrawerLayout mainDrawer;
    private View drawerHeader;
    private String actionBarTitle;

    private ShowingFragment showingFragment;


    // Declare as many Permission Checkers as permission requests we need on this activity
    private PermissionChecker storageChecker;
    private PermissionChecker picturesChecker;
    private PermissionChecker locationChecker;

    // Reference to UI elements to be bind with Butterknife (not before the header is inflated)
    @BindView(R.id.drawer_profile_username) TextView profileNameText;
    @BindView(R.id.drawer_profile_email) TextView profileEmailText;
    @BindView(R.id.drawer_profile_image) ImageView profileImage;


    /*** Activity life cycle and setup methods ***/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init all the permission checkers that will be used in this activity
        storageChecker = new PermissionChecker(RW_STORAGE_SET, this);
        picturesChecker = new PermissionChecker(PICTURES_SET, this);
        locationChecker = new PermissionChecker(LOCATION_SET, this);

        // Init all the managers used in this activity
        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);
        gm = new GeoManager(this);

        setupActionBar();
        setupDrawer(savedInstanceState);

        updateHeaderFromSessionInfo();

        if (savedInstanceState == null) {

            showingFragment = ShowingFragment.OTHER;

            // Set initial event search params: empty
            lastEventSearchParams = EventSearchParams.buildEmptyParams();
            lastEventSearchTotalResults = 0;

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
                Navigator.fromMainActivityToEventSearchActivity(
                        this,
                        lastEventSearchParams,
                        GeoManager.isLocationAccessGranted(this));
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
        outState.putInt(LAST_TOTAL_RESULTS_SAVED_STATE, lastEventSearchTotalResults);
        outState.putSerializable(SHOWING_FRAGMENT_SAVED_STATE, showingFragment);

        super.onSaveInstanceState(outState);
    }

    // Restore the state the activity had just before it was destroyed
    private void restoreActivityState(final @NonNull Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Restoring activity state...");

        actionBarTitle = savedInstanceState.getString(ACTIONBAR_TITLE_SAVED_STATE, "");
        setTitle(actionBarTitle);

        lastEventSearchParams = (EventSearchParams) savedInstanceState.getSerializable(LAST_EVENT_SEARCH_SAVED_STATE);
        lastEventSearchTotalResults = savedInstanceState.getInt(LAST_TOTAL_RESULTS_SAVED_STATE);

        showingFragment = (ShowingFragment) savedInstanceState.getSerializable(SHOWING_FRAGMENT_SAVED_STATE);
        if (showingFragment == ShowingFragment.EVENT_LIST)
            eventListFragment = (EventListFragment) getSupportFragmentManager().findFragmentById(R.id.mainContentFragment_placeholder);
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

    // Get references to the drawer and the drawer header.
    // Bind the header inner views (with Butterknife).
    // Set listeners for the drawer items and drawer header.
    // Forces the selection of the default item (if savedInstanceState == null)
    private void setupDrawer(Bundle savedInstanceState) {

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


    private void doDefaultOperation() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView == null)
            return;

        MenuItem defaultDrawerItem = navigationView.getMenu().findItem(DEFAULT_DRAWER_ITEM);
        onDrawerItemSelected(defaultDrawerItem);
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_FOR_STORAGE_PERMISSION)
            storageChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_PICTURES_PERMISSION)
            picturesChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_LOCATION_PERMISSION)
            locationChecker.checkAfterAsking();

        else if (requestCode == LOCATION_PICKER_PERMISSION_REQ_CODE)
            locationChecker.checkAfterAsking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the Event Search Settings Activity
        if (requestCode == Navigator.EVENT_SEARCH_ACTIVITY_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            final EventSearchParams newSearchParams = (EventSearchParams) data.getSerializableExtra(NEW_EVENT_SEARCH_PARAMS_KEY);

            // Check if we have permission to access the device location, before performing a search
            locationChecker.checkBeforeAsking(new CheckPermissionListener() {
                @Override
                public void onPermissionDenied() {
                    String msg = "Pick And Gol will not be able to search based on your location.";
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

        // If we are coming from the image picker
        else if (requestCode == IMAGE_PICKER_REQUEST_CODE) {

            im.handleImagePickerResult(MainActivity.this, requestCode, resultCode, data, new ImageManager.ImagePickingListener() {
                @Override
                public void onImagePicked(String imagePath) {

                    if (imagePath == null) {
                        Log.e(LOG_TAG, "Failed to get the path from the image picker");
                        return;
                    }

                    else if (lastPickedImagePurpose == UPLOAD_TO_CLOUD) {
                        doImageResizeThenUploadOperation( new File(imagePath) );
                    }

                    /*
                    else if (lastPickedImagePurpose == SHOW_IN_FRAGMENT) {
                        doShowLocalImageOperation( new File(imagePath) );
                    }
                    */

                }
            });
        }

        else if (requestCode == Navigator.LOCATION_PICKER_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                final Double selectedLat = (Double) data.getSerializableExtra(LocationPickerActivity.SELECTED_LATITUDE_KEY);
                final Double selectedLong = (Double) data.getSerializableExtra(LocationPickerActivity.SELECTED_LONGITUDE_KEY);

                gm.requestReverseDecodedAddress(selectedLat, selectedLong,
                        new GeoManager.GeoReverseLocationListener() {
                            @Override
                            public void onReverseLocationError(Throwable error) {
                                Utils.simpleDialog(MainActivity.this,
                                        "Location selected",
                                        selectedLat +", "+ selectedLong +"\n\n"+
                                                "< "+ error.getMessage() +" >");
                            }

                            @Override
                            public void onReverseLocationSuccess(@NonNull List<Address> addresses) {

                                Address address = addresses.get(0);
                                String fullAddress = "\n";

                                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                                    fullAddress = fullAddress + address.getAddressLine(i) +"\n";

                                Utils.simpleDialog(MainActivity.this,
                                        "Location selected",
                                        selectedLat + ", " + selectedLong + "\n" +
                                                fullAddress);
                            }
                        });

            }
            else
                Utils.simpleDialog(this, "Location selected", "No location was selected.");
        }

        else if (requestCode == Navigator.EDIT_USER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            User userModified = (User) data.getSerializableExtra(EditUserActivity.SAVED_USER_KEY);

            if (userModified != null) {
                updateSessionManagerWithUserModified(userModified);
                updateHeaderFromSessionInfo();
            }
        }
    }


    /** Menu action selectors (from the drawer & the action bar) **/

    // Action to perform when the drawer header is selected
    private void onDrawerHeaderSelected() {

        doShowSessionInfoOperation();
        mainDrawer.closeDrawers();
    }

    // Action to perform when an item of the drawer menu is selected
    private void onDrawerItemSelected(final MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.event_search:

                showingFragment = ShowingFragment.EVENT_LIST;

                actionBarTitle = menuItem.getTitle().toString();
                setTitle(actionBarTitle);

                // Check if we have permission to access the device location, before performing a search
                locationChecker.checkBeforeAsking(new CheckPermissionListener() {
                    @Override
                    public void onPermissionDenied() {
                        String msg = "Pick And Gol will not be able to search based on your location.";
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

            case R.id.create_event:

                Pub currentPub = new Pub("58c782770c0ef45dfc5875df", "La Biblioteca",
                        43.558096, -5.923779,
                        "https://www.facebook.com/Cafe.LaBiblioteca",
                        "58b471ddd9f0163f6eee6375",
                        new ArrayList<String>(), new ArrayList<String>()
                );

                Navigator.fromPubDetailActivityToNewEventActivity(this, currentPub);
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_upload_image:

                // Check if we have permission to access the camera, before opening the image picker
                picturesChecker.checkBeforeAsking(new CheckPermissionListener() {
                    @Override
                    public void onPermissionDenied() {
                        String title = "Pictures access denied";
                        String msg = "Pick And Gol might not be able to take pictures from your camera or gallery.";
                        Utils.simpleDialog(MainActivity.this, title, msg);

                        lastPickedImagePurpose = UPLOAD_TO_CLOUD;
                        im.showImagePicker(MainActivity.this);
                    }

                    @Override
                    public void onPermissionGranted() {
                        lastPickedImagePurpose = UPLOAD_TO_CLOUD;
                        im.showImagePicker(MainActivity.this);
                    }
                });

                break;

            case R.id.drawer_menu_create_pub:

                Navigator.fromMainActivityToNewPubActivity(this);

                /**
                 locationChecker.checkBeforeAsking(new CheckPermissionListener() {
                @Override
                public void onPermissionDenied() {
                Navigator.fromNewPubActivityToLocationPickerActivity(MainActivity.this, null, null);
                }

                @Override
                public void onPermissionGranted() {
                gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                Navigator.fromNewPubActivityToLocationPickerActivity(MainActivity.this, null, null);
                }

                @Override
                public void onLocationSuccess(double latitude, double longitude) {
                Navigator.fromNewPubActivityToLocationPickerActivity(MainActivity.this, latitude, longitude);
                }
                });
                }
                });
                 **/

                mainDrawer.closeDrawers();
                break;

            /*
            case R.id.drawer_menu_show_remote_image:

                new ChooseRemoteUrlDialog(MainActivity.this,
                                          "Enter a remote image url to load",
                                          "https://pickandgol.s3.amazonaws.com/test01.jpg",
                                          new ChooseRemoteUrlListener() {
                    @Override
                    public void onChooseRemoteUrl(String url) {
                        doShowRemoteImageOperation(menuItem.getTitle().toString(), url);
                    }
                }).show();
                break;
            */

            case R.id.drawer_menu_user_detail:

                doGetUserDetailOperation( sm.getUserId() );     // ask for our own user
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_log_in:

                new LoginDialog(this, new LoginDialog.LoginDialogListener() {
                    @Override
                    public void onLoginClick(String email, String password) {
                        doLoginOperation(email,password);
                    }
                }).show();

                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_log_out:

                doLogOutOperation();
                mainDrawer.closeDrawers();
                break;

            default:

                updateFragment(menuItem.getTitle().toString(), null);

                actionBarTitle = menuItem.getTitle().toString();
                setTitle(actionBarTitle);

                Utils.shortSnack(this, menuItem.toString() +" selected");
                mainDrawer.closeDrawers();
                break;
        }
    }


    /*** Operations triggered by the menu action selectors ***/

    private void searchEventsFirstPage(final @NonNull EventSearchParams searchParams, final @Nullable SwipeRefreshLayout swipeCaller) {

        searchParams.setOffset(0);

        // If we didn't come from a swipe gesture, show a progress dialog
        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Searching events...");
        if ( swipeCaller == null )
            pDialog.show();

        // Define what to do with the search results
        final SearchEventsInteractorListener interactorListener = new SearchEventsInteractorListener() {

            @Override
            public void onSearchEventsFail(Exception e) {

                if (swipeCaller != null)    swipeCaller.setRefreshing(false);
                else                        pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to search events: "+ e.toString() );
                Utils.simpleDialog(MainActivity.this, "Event search error", e.getMessage());
            }

            @Override
            public void onSearchEventsSuccess(EventAggregate events) {

                if (swipeCaller == null)    pDialog.dismiss();
                else                        swipeCaller.setRefreshing(false);

                lastEventSearchTotalResults = events.getTotalResults();

                eventListFragment = EventListFragment.newInstance(events);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContentFragment_placeholder, eventListFragment)
                        .commit();

                Utils.shortSnack(MainActivity.this, events.getTotalResults() +" event(s) found");
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
                Utils.shortSnack(MainActivity.this, "Error: "+ e.getMessage());
            }

            @Override
            public void onSearchEventsSuccess(EventAggregate events) {

                eventListFragment.addMoreEvents(events);
            }
        });
    }


    // Resize and upload the given image file
    private void doImageResizeThenUploadOperation(File imageFile) {

        if (imageFile == null || ! imageFile.isFile() )
            return;

        im.processImage(imageFile, new ImageProcessingListener() {
            @Override
            public void onProcessError(Exception error) {
                Utils.simpleDialog(MainActivity.this, "Unable to resize image", error.toString());
            }

            @Override
            public void onProcessSuccess(File resizedFile) {
                doUploadImageOperation(resizedFile.getAbsolutePath());
            }
        });
    }

    // Upload file to Amazon S3 bucket
    private void doUploadImageOperation(final String filePath) {

        if( ! new File(filePath).isFile() ) {
            Log.e(LOG_TAG, "An error occurred: '"+ filePath +"' does not exist or is not a file");
            Utils.simpleDialog(MainActivity.this, "Upload error",
                    "The file '" + filePath + "' could not be uploaded: \n\nFile does not exist or is not a file");
            return;
        }

        File sourceFile = new File(filePath);
        final String remoteFileName = UUID.randomUUID().toString() +".jpg";

        int kbTotal = (int) (sourceFile.length() / 1024.0f);        // progress will be measured in KB
        String fileSize = Utils.readableSize(sourceFile.length());   // a string ended with B/KB/MB/...

        final ProgressDialog pDialog = Utils.newProgressBarDialog(this, kbTotal, "Uploading file ("+ fileSize +")...");
        pDialog.show();

        Log.d(LOG_TAG, "Uploading file '"+ filePath +"' ("+ fileSize +")...");

        im.uploadImage(new File(filePath), remoteFileName, new ImageManager.ImageUploadListener() {

            @Override
            public void onProgressChanged(int transferId, long bytesCurrent, long bytesTotal) {
                int kbCurrent = (int) (bytesCurrent / 1024.0f);
                pDialog.setProgress( kbCurrent );
            }

            @Override
            public void onImageUploadError(int transferId, Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "[id "+ transferId +"] An error occurred: "+ e.toString());
                Utils.simpleDialog(MainActivity.this, "Upload error",
                        "The file '" + filePath + "' could not be uploaded: \n\n"+ e.toString());
            }

            @Override
            public void onImageUploadCompletion(int transferId) {
                pDialog.dismiss();
                Log.d(LOG_TAG, "[id "+ transferId +"] File '" + filePath + "' has been stored as: "+ remoteFileName);
                Utils.simpleDialog(MainActivity.this, "Upload successful",
                        "The file has been stored as '" + remoteFileName + "'.");
            }
        });
    }

    // Use a LoginInteractor to perform the login operation
    private void doLoginOperation(final @NonNull String email, final @NonNull String password) {

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Login in progress...");
        pDialog.show();

        new LoginInteractor().execute(this, email, password, new LoginInteractorListener() {

            @Override
            public void onLoginFail(Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "Failed to login: "+ e.toString() );
                Utils.simpleDialog(MainActivity.this, "Login error", e.getMessage());
            }

            @Override
            public void onLoginSuccess() {
                pDialog.dismiss();

                updateHeaderFromSessionInfo();

                Utils.simpleDialog(MainActivity.this,
                        "Login successful",
                        "Now you are logged as '"+ sm.getUserName() +"'.");
                updateHeaderFromSessionInfo();
            }
        });
    }

    // Destroy the stored session and update the header views
    private void doLogOutOperation() {

        if ( sm.hasSessionStored() ) {
            sm.destroySession();
            updateHeaderFromSessionInfo();
            Utils.simpleDialog(this, "Log out", "You just finished your session.");
        }
        else {
            Utils.simpleDialog(this, "Log out", "You are already logged out.");
        }
    }

    // Use a UserDetailInteractor to perform the user detail operation
    private void doGetUserDetailOperation(final String id) {

        if (id == null) {
            Utils.simpleDialog(this, "User detail error", "No user id to ask for (try logging in first).");
            return;
        }

        // Only authenticated users should ask for user details
        if ( !sm.hasSessionStored() ) {
            Utils.simpleDialog(this, "User detail error", "You must be logged in to perform this operation.");
            return;
        }

        Navigator.fromMainActivityToEditUserActivity(this);
    }

    private void updateSessionManagerWithUserModified(final User userModified) {
        if (sm.getUserEmail() == null || !sm.getUserEmail().equals(userModified.getEmail())) {

            sm.updateUserEmail(userModified.getEmail());
        }

        if (sm.getUserName() == null || !sm.getUserName().equals(userModified.getName())) {

            sm.updateUserName(userModified.getName());
        }
    }

    // Shows the info from the local stored session
    private void doShowSessionInfoOperation() {

        if ( !sm.hasSessionStored() ) {
            Utils.simpleDialog(this, "Session info", "No session stored on this device.");
            return;
        }

        String id = sm.getUserId();
        String email = sm.getUserEmail();
        String name = sm.getUserName();
        String photoUrl = sm.getUserPhotoUrl();
        String token = sm.getSessionToken();

        Utils.simpleDialog(this, "Session info",
                "Id: "+ id
                        +"\nEmail: "+ email
                        +"\nName: "+ name
                        +"\nPhoto: \n"+ photoUrl
                        +"\n\nToken: \n"+ token);
    }

    // Show remote image from remote url
    private void doShowRemoteImageOperation(final String fragmentCaption, final String url) {

        updateFragment(fragmentCaption, url);

        actionBarTitle = fragmentCaption;
        setTitle(actionBarTitle);

        Utils.shortSnack(MainActivity.this, fragmentCaption +" selected");
        mainDrawer.closeDrawers();
    }

    // Clear the image cache
    private void doClearImageCacheOperation() {

        im.clearCache();
        Utils.shortSnack(this, "Image cache cleared");
    }


    /*** Auxiliary recurrent methods ***/

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
            profileNameText.setText("Please register or log in");
            profileEmailText.setText("to Pick And Gol");
            im.loadImage(R.drawable.default_avatar, profileImage);
        }
    }

    // Update content fragment
    private void updateFragment(String caption, String imageUri) {

        mainFragment = MainContentFragment.newInstance(imageUri, caption);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainContentFragment_placeholder, mainFragment)
                .commit();
    }



    /*** Implementation of EventListFragment.EventListListener interface ***/

    @Override
    public void onItemClicked(Event event, int position) {

        Navigator.fromMainActivityToEventDetailActivity(this, event);
    }

    @Override
    public void onSwipeRefresh(@Nullable final SwipeRefreshLayout swipeCaller) {

        // Check if we have permission to access the device location, before performing a search
        locationChecker.checkBeforeAsking(new CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String msg = "Pick And Gol will not be able to search based on your location.";
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
    public void onLoadNextPage() {
        searchEventsNextPage(lastEventSearchParams);
    }
}
