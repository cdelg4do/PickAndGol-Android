package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.dialog.ChooseRemoteUrlDialog;
import io.keepcoding.pickandgol.dialog.LoginDialog;
import io.keepcoding.pickandgol.fragment.MainContentFragment;
import io.keepcoding.pickandgol.interactor.LoginInteractor;
import io.keepcoding.pickandgol.interactor.UserDetailInteractor;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImageResizeListener;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.activity.MainActivity.ImagePurpose.UPLOAD_TO_CLOUD;
import static io.keepcoding.pickandgol.dialog.ChooseRemoteUrlDialog.ChooseRemoteUrlListener;
import static io.keepcoding.pickandgol.interactor.LoginInteractor.LoginInteractorListener;
import static io.keepcoding.pickandgol.interactor.UserDetailInteractor.UserDetailInteractorListener;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;
import static io.keepcoding.pickandgol.util.PermissionChecker.CheckPermissionListener;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.CAMERA_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.RW_STORAGE_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_CAMERA_PERMISSION;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_STORAGE_PERMISSION;


/**
 * This class is the application main activity
 */
public class MainActivity extends AppCompatActivity {

    // When choosing an image from the picker, this helps to know what to do with it
    // (like upload the selected image to the cloud, load it into an ImageView, etc.)
    public enum ImagePurpose {
        UPLOAD_TO_CLOUD
    }

    private final String ACTIONBAR_TITLE_SAVED_STATE = "ACTIONBAR_TITLE_SAVED_STATE";
    private final int DEFAULT_DRAWER_ITEM = R.id.drawer_menu_item_1;

    private SessionManager sm;
    private ImageManager im;

    private ImagePurpose lastPickedImagePurpose;  // stores the purpose of the last picked image

    private Fragment mainFragment;

    private DrawerLayout mainDrawer;
    private View drawerHeader;
    private String actionBarTitle;

    // Declare as many Permission Checkers as permission requests we need on this activity
    private PermissionChecker storageChecker;
    private PermissionChecker cameraChecker;

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
        cameraChecker = new PermissionChecker(CAMERA_SET, this);

        // Init all the managers used in this activity
        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);

        setupActionBar();
        setupDrawer(savedInstanceState);

        if (savedInstanceState != null)
            restoreActivityState(savedInstanceState);

        updateHeaderFromSessionInfo();
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

            case R.id.main_menu_clear_image_cache:
                doClearImageCacheOperation();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d("MainActivity","Saving activity state...");

        outState.putString(ACTIONBAR_TITLE_SAVED_STATE, actionBarTitle);
        super.onSaveInstanceState(outState);
    }

    // Restore the state the activity had just before it was destroyed
    private void restoreActivityState(final @NonNull Bundle savedInstanceState) {
        Log.d("MainActivity","Restoring activity state...");

        actionBarTitle = savedInstanceState.getString(ACTIONBAR_TITLE_SAVED_STATE, "");
        setTitle(actionBarTitle);
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

        if (savedInstanceState == null) {
            MenuItem defaultDrawerItem = navigationView.getMenu().findItem(DEFAULT_DRAWER_ITEM);
            onDrawerItemSelected(defaultDrawerItem);
        }
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_FOR_STORAGE_PERMISSION)
            storageChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_CAMERA_PERMISSION)
            cameraChecker.checkAfterAsking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the image picker
        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {

            im.handleImagePickerResult(MainActivity.this, requestCode, resultCode, data, new ImageManager.ImagePickingListener() {
                @Override
                public void onImagePicked(String imagePath) {

                    if (imagePath == null) {
                        Log.e("MainActivity","Failed to get the path from the image picker");
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

            case R.id.drawer_menu_upload_image:

                // Check if we have permission to access the camera, before opening the image picker
                cameraChecker.checkBeforeAsking(new CheckPermissionListener() {
                    @Override
                    public void onPermissionDenied() {
                        String title = "Camera access denied";
                        String msg = "Pick And Gol will not be able to take images from your device camera.";
                        Utils.simpleDialog(MainActivity.this, title, msg);
                    }

                    @Override
                    public void onPermissionGranted() {
                        lastPickedImagePurpose = UPLOAD_TO_CLOUD;
                        im.showImagePicker(MainActivity.this);
                    }
                });

                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_show_remote_image:

                new ChooseRemoteUrlDialog(MainActivity.this,
                                          "Enter a remote image url to load",
                                          "https://pickandgol.s3.amazonaws.com/test.jpg",
                                          new ChooseRemoteUrlListener() {
                    @Override
                    public void onChooseRemoteUrl(String url) {
                        doShowRemoteImageOperation(menuItem.getTitle().toString(), url);
                    }
                }).show();
                break;

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

    // Resize and upload the given image file
    private void doImageResizeThenUploadOperation(File imageFile) {

        if (imageFile == null || ! imageFile.isFile() )
            return;

        im.resizeImage(imageFile, new ImageResizeListener() {
            @Override
            public void onResizeError(Exception error) {
                Utils.simpleDialog(MainActivity.this, "Unable to resize image", error.toString());
            }

            @Override
            public void onResizeSuccess(File resizedFile) {
                doUploadImageOperation(resizedFile.getAbsolutePath());
            }
        });
    }

    // Upload file to Amazon S3 bucket
    private void doUploadImageOperation(final String filePath) {

        if( ! new File(filePath).isFile() ) {
            Log.e("File Upload", "An error occurred: '"+ filePath +"' does not exist or is not a file");
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

        Log.d("File Upload", "Uploading file '"+ filePath +"' ("+ fileSize +")...");

        im.uploadImage(new File(filePath), remoteFileName, new ImageManager.ImageUploadListener() {

            @Override
            public void onProgressChanged(int transferId, long bytesCurrent, long bytesTotal) {
                int kbCurrent = (int) (bytesCurrent / 1024.0f);
                pDialog.setProgress( kbCurrent );
            }

            @Override
            public void onImageUploadError(int transferId, Exception e) {
                pDialog.dismiss();
                Log.e("File Upload", "[id "+ transferId +"] An error occurred: "+ e.toString());
                Utils.simpleDialog(MainActivity.this, "Upload error",
                        "The file '" + filePath + "' could not be uploaded: \n\n"+ e.toString());
            }

            @Override
            public void onImageUploadCompletion(int transferId) {
                pDialog.dismiss();
                Log.d("File Upload", "[id "+ transferId +"] File '" + filePath + "' has been stored as: "+ remoteFileName);
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
                Log.e("MainActivity","Failed to login: "+ e.toString() );
                Utils.simpleDialog(MainActivity.this, "Login error", e.getMessage());
            }

            @Override
            public void onLoginSuccess() {
                pDialog.dismiss();

                updateHeaderFromSessionInfo();

                Utils.simpleDialog(MainActivity.this,
                        "Login successful",
                        "Now you are logged as '"+ sm.getUserName() +"'.");
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

        String token = sm.getSessionToken();

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Fetching user '"+ id +"' info...");
        pDialog.show();

        new UserDetailInteractor().execute(this, id, token, new UserDetailInteractorListener() {

            @Override
            public void onUserDetailFail(Exception e) {
                pDialog.dismiss();
                Log.e("MainActivity","Failed to retrieve detail for user '"+ id +"': "+ e.toString() );
                Utils.simpleDialog(MainActivity.this, "User detail error", e.getMessage());
            }

            @Override
            public void onUserDetailSuccess(User user) {
                pDialog.dismiss();

                String photoUrl = (user.getPhotoUrl() != null) ? user.getPhotoUrl() : "<none>";

                String favorites = "[ ";
                for (Integer i : user.getFavorites())
                    favorites += i.toString() +" ";
                favorites += "]";

                Utils.simpleDialog(MainActivity.this, "User detail",
                        "Id: "+ user.getId()
                                +"\nName: "+ user.getName()
                                +"\nEmail: "+ user.getEmail()
                                +"\nFavorites: "+ favorites
                                +"\nPhoto: "+ photoUrl);
            }
        });
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

}
