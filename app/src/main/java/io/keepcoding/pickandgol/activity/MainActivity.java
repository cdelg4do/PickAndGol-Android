package io.keepcoding.pickandgol.activity;

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

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.fragment.MainContentFragment;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This class is the application main activity
 */
public class MainActivity extends AppCompatActivity {

    private final String ACTIONBAR_TITLE_SAVED_STATE = "ACTIONBAR_TITLE_SAVED_STATE";
    private final String DEFAULT_DRAWER_ITEM = "Menu Item #1";  // This should be a string from "drawer_menu"

    private DrawerLayout mainDrawer;
    private View drawerHeader;
    private String actionBarTitle;


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
                        doDrawerMenuAction( menuItem.getTitle().toString() );
                        return true;
                    }
                }
            );
        }

        if (drawerHeader != null) {

            drawerHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doDrawerHeaderAction();
                }
            });
        }

        if (forceSelectDefaultItem)
            doDrawerMenuAction(DEFAULT_DRAWER_ITEM);
    }


    // Action to perform when the drawer header is selected
    private void doDrawerHeaderAction() {

        Utils.shortSnack(this, "Profile selected");
        mainDrawer.closeDrawers();
    }


    // Action to perform when an item of the drawer menu is selected
    private void doDrawerMenuAction(String selectedItem) {

        switch (selectedItem) {

            case "Log in":
                Utils.shortSnack(this, "Log in selected");
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

}
