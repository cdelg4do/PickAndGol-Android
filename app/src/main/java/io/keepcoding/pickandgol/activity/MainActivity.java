package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.dialog.ChooseFileDialog;
import io.keepcoding.pickandgol.dialog.LoginDialog;
import io.keepcoding.pickandgol.fragment.MainContentFragment;
import io.keepcoding.pickandgol.interactor.LoginInteractor;
import io.keepcoding.pickandgol.interactor.LoginInteractor.LoginInteractorListener;
import io.keepcoding.pickandgol.interactor.UserDetailInteractor;
import io.keepcoding.pickandgol.interactor.UserDetailInteractor.UserDetailInteractorListener;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.PermissionChecker.CheckPermissionListener;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.CAMERA_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.RW_STORAGE_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_CAMERA_PERMISSION;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_LOCATION_PERMISSION;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_STORAGE_PERMISSION;


/**
 * This class is the application main activity
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_FOR_STORAGE_PERMISSION = 123;

    private final String ACTIONBAR_TITLE_SAVED_STATE = "ACTIONBAR_TITLE_SAVED_STATE";
    private final int DEFAULT_DRAWER_ITEM = R.id.drawer_menu_item1;  // Should be an item from "menu/drawer_menu"

    private SessionManager sm;
    private ImageManager im;

    // There should be as many Permission Checkers as permission requests we need on this activity
    private PermissionChecker storageChecker;
    private PermissionChecker cameraChecker;

    private Fragment mainFragment;

    private DrawerLayout mainDrawer;
    private View drawerHeader;
    private String actionBarTitle;
    private ImageView fragmentImage;

    // Reference to UI elements to be bind with Butterknife (not before the header is inflated)
    @BindView(R.id.drawer_profile_username) TextView profileNameText;
    @BindView(R.id.drawer_profile_email) TextView profileEmailText;
    @BindView(R.id.drawer_profile_image) ImageView profileImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);

        setupActionBar();

        if (savedInstanceState != null)
            restoreActivityState(savedInstanceState);

        setupDrawer(savedInstanceState);
        updateHeaderFromSessionInfo();

        checkStoragePermission(new CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String title = "Storage access required";
                String msg = "Pick And Gol will not be able to access your device storage.";
                Utils.simpleDialog(MainActivity.this, title, msg);
            }

            @Override
            public void onPermissionGranted() {
                Utils.shortSnack(MainActivity.this, "Storage access granted");
            }
        });
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

            case R.id.main_menu_action_settings:
                Utils.shortSnack(this, "Settings selected");
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
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
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


    // Updates the header views with the information in the device's session
    // (if there is no session stored, just update the views with the default values)
    private void updateHeaderFromSessionInfo() {

        if (sm.hasSessionStored()) {

            profileNameText.setText( sm.getUserName() );
            profileEmailText.setText( sm.getUserEmail() );

            if ( sm.getUserPhotoUrl() != null ) {
                Picasso.with(MainActivity.this)
                        .load(sm.getUserPhotoUrl())
                        .into(profileImage);
            }
        }

        else {
            profileNameText.setText("Please register or log in");
            profileEmailText.setText("to Pick And Gol");
            Picasso.with(MainActivity.this)
                    .load(R.drawable.default_avatar)
                    .into(profileImage);
        }
    }


    // Action to perform when the drawer header is selected
    private void onDrawerHeaderSelected() {

        Utils.shortSnack(this, "Profile selected");
        mainDrawer.closeDrawers();
    }


    // Action to perform when an item of the drawer menu is selected
    private void onDrawerItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {

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

            case R.id.drawer_menu_user_detail:
                doGetUserDetailOperation( sm.getUserId() );     // ask for our own user
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_session_info:
                doShowSessionInfoOperation();
                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_upload_image:

                new ChooseFileDialog(this, "/storage/emulated/0/Download", "test.jpg", new ChooseFileDialog.ChooseFileDialogListener() {
                    @Override
                    public void onChooseFileClick(String filePath) {
                        doUploadImageOperation(filePath);
                    }
                }).show();

                mainDrawer.closeDrawers();
                break;

            case R.id.drawer_menu_load_image:

                fragmentImage = ((MainContentFragment)mainFragment).getImage();

                if (fragmentImage != null)
                    doDownloadImageOperation(fragmentImage, "https://pickandgol.s3.amazonaws.com/test.jpg");

                mainDrawer.closeDrawers();
                break;

            default:

                mainFragment = MainContentFragment.newInstance(menuItem.getTitle().toString());
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContentFragment_placeholder, mainFragment)
                        .commit();

                actionBarTitle = menuItem.getTitle().toString();
                setTitle(actionBarTitle);

                Utils.shortSnack(this, menuItem.toString() +" selected");
                mainDrawer.closeDrawers();
        }
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
    private void doGetUserDetailOperation(final @NonNull String id) {

        // Only authenticated users should ask for user details
        if ( !sm.hasSessionStored() ) {
            Utils.simpleDialog(this, "Unauthorized operation", "You must be logged in to perform this operation.");
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


    // Upload file to Amazon S3 bucket
    private void doUploadImageOperation(final String filePath) {

        if( new File(filePath).isFile() ) {

            final ProgressDialog pDialog = Utils.newProgressDialog(this, "Uploaded 0%");
            pDialog.show();

            final String imageKey = UUID.randomUUID().toString() +"."+ Utils.getFileExtension(filePath);

            im.uploadImage(new File(filePath), imageKey, new ImageManager.ImageTransferListener() {

                @Override
                public void onProgressChanged(int transferId, long bytesCurrent, long bytesTotal) {
                    int percent = (int) ((bytesCurrent * 100.0f) / bytesTotal);
                    int kbTotal = (int) (bytesTotal / 1024.0f);

                    pDialog.setMessage("Uploaded " + String.valueOf(percent) + "% of "+ String.valueOf(kbTotal) +" kb");
                }

                @Override
                public void onImageTransferError(int transferId, Exception e) {
                    pDialog.dismiss();

                    Log.e("File Upload", "An error occurred: " + e.toString());
                    Utils.simpleDialog(MainActivity.this, "Upload error", "The file '" + filePath + "' could not be uploaded.");
                }

                @Override
                public void onImageTransferCompletion(int transferId) {
                    pDialog.dismiss();
                    Utils.simpleDialog(MainActivity.this, "Upload successful", "The file '" + filePath + "' has been stored as '" + imageKey + "'.");
                }
            });
        }
    }


    // Load remote image to an ImageView
    private void doDownloadImageOperation(ImageView view, String url) {

        im.loadImage(view, url, null, null);
    }


    /** Permission checking methods **/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        // Request for permissions
        if (requestCode == REQUEST_FOR_STORAGE_PERMISSION)
            this.storageChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_CAMERA_PERMISSION)
            this.cameraChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_LOCATION_PERMISSION);
    }

    // Checks the storage permissions
    private void checkStoragePermission(@NonNull CheckPermissionListener listener) {

        String explanationTitle = "Storage access required";
        String explanationMsg = "Pick And Gol will request access to your device storage in order to load and send pictures.";

        // Keep a reference to the checker, to use it at onRequestPermissionsResult()
        this.storageChecker = new PermissionChecker(RW_STORAGE_SET, this, explanationTitle, explanationMsg, listener);
        this.storageChecker.checkBeforeAsking();
    }

    // Checks the camera permissions
    private void checkCameraPermission(@NonNull CheckPermissionListener listener) {

        String explanationTitle = "Camera access required";
        String explanationMsg = "Pick And Gol will request access to your device camera in order take pictures.";

        // Keep a reference to the checker, to use it at onRequestPermissionsResult()
        this.cameraChecker = new PermissionChecker(CAMERA_SET, this, explanationTitle, explanationMsg, listener);
        this.cameraChecker.checkBeforeAsking();
    }

}
