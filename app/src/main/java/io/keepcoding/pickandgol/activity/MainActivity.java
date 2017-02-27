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

import com.squareup.picasso.Picasso;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.dialog.LoginDialog;
import io.keepcoding.pickandgol.fragment.EditUserFragment;
import io.keepcoding.pickandgol.fragment.MainContentFragment;
import io.keepcoding.pickandgol.interactor.EditUserInteractor;
import io.keepcoding.pickandgol.interactor.LoginInteractor;
import io.keepcoding.pickandgol.interactor.LoginInteractor.LoginInteractorListener;
import io.keepcoding.pickandgol.interactor.UserDetailInteractor;
import io.keepcoding.pickandgol.interactor.UserDetailInteractor.UserDetailInteractorListener;
import io.keepcoding.pickandgol.model.Login;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This class is the application main activity
 */
public class MainActivity extends AppCompatActivity {

    private final String ACTIONBAR_TITLE_SAVED_STATE = "ACTIONBAR_TITLE_SAVED_STATE";
    private final String DEFAULT_DRAWER_ITEM = "Menu Item #1";  // Should be a string from "menu/drawer_menu"

    private DrawerLayout mainDrawer;
    private View drawerHeader;
    private String actionBarTitle;
    private Login login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupActionBar();

        boolean selectDefault = (savedInstanceState == null);
        setupDrawer(selectDefault);

        if (savedInstanceState == null) {
            setupDrawer(true);
        }
        else {
            restoreActivityState(savedInstanceState);
            setupDrawer(false);
        }
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


    // Set listeners for the header and the drawer items.
    // Also, force the selection of the default drawer item if needed.
    private void setupDrawer(boolean forceSelectDefaultItem) {

        mainDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {

            drawerHeader = navigationView.getHeaderView(0);

            navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        onDrawerItemSelected( menuItem.getTitle().toString() );
                        return true;
                    }
                }
            );
        }

        if (drawerHeader != null) {

            drawerHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDrawerHeaderSelected();
                }
            });
        }

        if (forceSelectDefaultItem)
            onDrawerItemSelected(DEFAULT_DRAWER_ITEM);
    }


    // Action to perform when the drawer header is selected
    private void onDrawerHeaderSelected() {

        Utils.shortSnack(this, "Profile selected");
        mainDrawer.closeDrawers();
    }


    // Action to perform when an item of the drawer menu is selected
    private void onDrawerItemSelected(String selectedItem) {

        switch (selectedItem) {

            case "User detail":
                if (login == null) {
                    Utils.simpleDialog(MainActivity.this, "User detail error", "You have to login before");
                    return;
                }

                doGetUserDetailOperation(login.getId(), login.getToken());
                mainDrawer.closeDrawers();
                break;

            case "Log in":

                new LoginDialog(this, new LoginDialog.LoginDialogListener() {
                    @Override
                    public void onLoginClick(String email, String password) {
                        doLoginOperation(email,password);
                    }
                }).show();

                mainDrawer.closeDrawers();
                break;

            case "Register":
                Utils.shortSnack(this, "Register selected");
                mainDrawer.closeDrawers();
                break;

            default:
                Fragment newFragment = MainContentFragment.newInstance(selectedItem);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContentFragment_placeholder, newFragment)
                        .commit();

                mainDrawer.closeDrawers();
                actionBarTitle = selectedItem;
                setTitle(actionBarTitle);
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
            public void onLoginSuccess(Login login) {
                pDialog.dismiss();
                MainActivity.this.login = login;
                updateLoginPicture();
            }
        });
    }


    // Use a UserDetailInteractor to perform the user detail operation
    private void doGetUserDetailOperation(final @NonNull String id, final @NonNull String token) {

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

                EditUserFragment editUserFragment = EditUserFragment.newInstance(user);
                editUserFragment.setListener(new EditUserFragment.Listener() {
                    @Override
                    public void onSaveButtonPushed(final User userModified) {
                        new EditUserInteractor().execute(MainActivity.this, token, userModified, new EditUserInteractor.Listener() {
                            @Override
                            public void onEditUserSuccess() {
                                updateLogin(userModified.getEmail(), userModified.getName(), userModified.getPhotoUrl());
                                updateLoginPicture();
                                Utils.simpleDialog(MainActivity.this, "User", "User modified");
                            }

                            @Override
                            public void onEditUserFail(final String message) {
                                Utils.simpleDialog(MainActivity.this, "User modify error", message);
                            }
                        });
                    }
                });

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainContentFragment_placeholder, editUserFragment)
                        .commit();
            }
        });
    }

    private void updateLogin(final String email, final String name, final String photoUrl) {
        String newEmail = login.getEmail();
        if (email != null) {
            newEmail = email;
        }

        String newName = login.getName();
        if (name != null) {
            newName = name;
        }

        String newPhotoUrl = login.getPhotoUrl();
        if (photoUrl != null) {
            newPhotoUrl = photoUrl;
        }

        login = new Login(login.getId(), newEmail, newName, login.getToken(), newPhotoUrl);
    }

    private void updateLoginPicture() {
        ImageView circleImageView = (ImageView) drawerHeader.findViewById(R.id.circle_image);

        if (login.getPhotoUrl() == null) {
            circleImageView.setImageResource(R.drawable.default_avatar);
            return;
        }

        Picasso.with(MainActivity.this)
                .load(login.getPhotoUrl())
                .into(circleImageView);
    }
}
