package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.StringStringSpinnerAdapter;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.geo.GeoManager.GeoReverseLocationListener;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.PubSearchParams;
import io.keepcoding.pickandgol.util.Utils;

import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.SELECTED_LATITUDE_KEY;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.SELECTED_LONGITUDE_KEY;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.STANDARD_MAP_LATITUDE;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.STANDARD_MAP_LONGITUDE;
import static io.keepcoding.pickandgol.activity.MainActivity.CURRENT_PUB_SEARCH_PARAMS_KEY;
import static io.keepcoding.pickandgol.navigator.Navigator.LOCATION_PICKER_ACTIVITY_REQUEST_CODE;


/**
 * This class represents an activity where the user can adjust the settings to perform a Pub search.
 */
public class PubSearchSettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "PubSearchSettings";

    private PubSearchParams currentSearchParams;
    private boolean isGpsAvailable = false;

    private GeoManager gm;
    private final boolean[] geoOperationInProgress = { false };

    // Array of coordinates: [0] -> lat, [1] -> lon
    private final Double[] searchOrigin = new Double[2];

    @BindView(R.id.activity_pub_search_scrollView_content)  LinearLayout contentLayout;
    @BindView(R.id.activity_pub_search_keywords_text)       EditText txtKeywords;
    @BindView(R.id.activity_pub_search_sort_spinner)        Spinner spnSort;
    @BindView(R.id.activity_pub_search_distance_layout)     RelativeLayout distanceLayout;
    @BindView(R.id.activity_pub_search_check_myLocation)    CheckBox chkCurrentLocation;
    @BindView(R.id.activity_pub_search_check_mapLocation)   CheckBox chkMapLocation;
    @BindView(R.id.activity_pub_search_location_text)       EditText txtLocation;
    @BindView(R.id.activity_pub_search_distance_label)      TextView txtDistance;
    @BindView(R.id.activity_pub_search_distance_bar)        SeekBar distanceBar;
    @BindView(R.id.activity_pub_search_button_cancel)       Button btnCancel;
    @BindView(R.id.activity_pub_search_button_apply)        Button btnApply;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_search_settings);
        ButterKnife.bind(this);

        gm = new GeoManager(this);

        setupActionBar();
        setupDistanceBar();
        setupSortSpinner();
        setupButtons();
        setupLocationPickerThenLoadCurrentSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.pub_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null);
                return true;

            case R.id.pub_search_menu_reset:
                resetForm();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the location picker
        if (requestCode == LOCATION_PICKER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            final Double selectedLat = (double) data.getSerializableExtra(SELECTED_LATITUDE_KEY);
            final Double selectedLon = (double) data.getSerializableExtra(SELECTED_LONGITUDE_KEY);

            if (selectedLat != null && selectedLon != null)
                setSearchOrigin(selectedLat, selectedLon);

            chkMapLocation.setChecked(true);
            chkMapLocation.setClickable(false);

            chkCurrentLocation.setChecked(false);
            chkCurrentLocation.setClickable(true);
        }
    }


    /** Auxiliary methods **/

    // Reset all data in the form
    private void resetForm() {

        txtKeywords.setText("");

        //chkCurrentLocation.setChecked(true);
        //chkMapLocation.setChecked(false);
        //txtLocation.setText("");

        int defaultDistanceBarValue = 1;
        distanceBar.setProgress(defaultDistanceBarValue);
        txtDistance.setText( getDistanceText(defaultDistanceBarValue) );

        spnSort.setSelection(0);
    }

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle(getString(R.string.pub_search_settings_activity_title));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Sets the behavior for the search location GUI elements (CheckBoxes and location text box).
    // If the Gps location is available, sets the current location as the search origin.
    // If not, disables the "My Location" checkBox and sets an arbitrary initial search origin.
    //
    // Once the setup is done, loads the received search settings into the activity form.
    private void setupLocationPickerThenLoadCurrentSettings() {

        setupLocationCheckBoxes();

        isGpsAvailable = GeoManager.canGetLocationFromGps(this);
        if ( isGpsAvailable ) {

            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                @Override
                public void onLocationError(Throwable error) {
                    setSearchOrigin(STANDARD_MAP_LATITUDE, STANDARD_MAP_LONGITUDE);
                    setupLocationTxt();

                    loadCurrentSearchSettings();
                }

                @Override
                public void onLocationSuccess(final double currentLat, final double currentLon) {
                    setSearchOrigin(currentLat, currentLon);
                    setupLocationTxt();

                    loadCurrentSearchSettings();
                }
            });
        }

        else {
            chkCurrentLocation.setChecked(false);
            chkCurrentLocation.setEnabled(false);
            chkCurrentLocation.setText("My current location (unavailable)");

            chkMapLocation.setChecked(true);
            chkMapLocation.setClickable(false);
            txtLocation.setVisibility(VISIBLE);

            setSearchOrigin(STANDARD_MAP_LATITUDE, STANDARD_MAP_LONGITUDE);
            setupLocationTxt();

            loadCurrentSearchSettings();
        }
    }

    // Defines what to do when some of the location checkboxes is clicked
    private void setupLocationCheckBoxes() {

        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!(view instanceof CheckBox))
                    return;

                CheckBox checkBox = (CheckBox) view;
                switch ( checkBox.getId() ) {

                    case R.id.activity_pub_search_check_myLocation:

                        if (checkBox.isChecked()) {
                            checkBox.setClickable(false);
                            chkMapLocation.setChecked(false);
                            chkMapLocation.setClickable(true);

                            gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                                @Override
                                public void onLocationError(Throwable error) {
                                }

                                @Override
                                public void onLocationSuccess(final double currentLat, final double currentLon) {
                                    setSearchOrigin(currentLat, currentLon);
                                }
                            });
                        }
                        break;

                    case R.id.activity_pub_search_check_mapLocation:

                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);

                            Navigator.fromAnyActivityToLocationPickerActivity(PubSearchSettingsActivity.this,
                                    searchOrigin[0],
                                    searchOrigin[1]);
                        }
                        break;

                    default:
                        break;
                }
            }
        };

        chkCurrentLocation.setOnClickListener(checkBoxListener);
        chkMapLocation.setOnClickListener(checkBoxListener);
    }

    // Sets the behavior when the location text box is clicked
    // (go to the location picker activity, passing the original coordinates)
    private void setupLocationTxt() {

        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Navigator.fromAnyActivityToLocationPickerActivity(PubSearchSettingsActivity.this,
                        searchOrigin[0],
                        searchOrigin[1]);
            }
        });
    }

    // Sets the given coordinates as the search origin, and updates the location address view.
    private void setSearchOrigin(final Double latitude, final Double longitude) {

        searchOrigin[0] = latitude;
        searchOrigin[1] = longitude;

        gm.requestReverseDecodedAddress(latitude, longitude, new GeoReverseLocationListener() {
            @Override
            public void onReverseLocationError(Throwable error) {
                txtLocation.setText("Undefined location (" + latitude + ", " + longitude + ")");
            }

            @Override
            public void onReverseLocationSuccess(@NonNull List<Address> addresses) {
                txtLocation.setText( Utils.getAddressString(addresses.get(0)) );
                Log.d("ZZ", "OK");
            }
        });
    }


    // Sets the listener for the distance bar events
    private void setupDistanceBar() {

        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean initiatedByUser) {

                txtDistance.setText( getDistanceText(value) );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Populates the sort spinner
    private void setupSortSpinner() {

        String[] fieldNames = {"name", "owner", "distance"}; // this strings will be used as url parameters
        String[] fieldNamesToShow = {
                getString(R.string.pub_search_settings_activity_field_names_name),
                getString(R.string.pub_search_settings_activity_field_names_owner),
                getString(R.string.pub_search_settings_activity_field_names_distance)
        };

        StringStringSpinnerAdapter adapter = new StringStringSpinnerAdapter(this,
                fieldNames, fieldNamesToShow,
                getString(R.string.pub_search_settings_activity_field_names_any_order));

        spnSort.setAdapter(adapter);
    }

    // Set listeners for the activity buttons
    private void setupButtons() {

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null);
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PubSearchParams newSearchParams = validateForm();
                if (newSearchParams != null)
                    finishActivity(newSearchParams);
            }
        });
    }

    // Loads the current search settings into the form
    private void loadCurrentSearchSettings() {

        Intent i = getIntent();
        currentSearchParams = (PubSearchParams) i.getSerializableExtra(CURRENT_PUB_SEARCH_PARAMS_KEY);

        final String currentKeyWords = currentSearchParams.getKeyWords();
        final boolean useCurrentLocation = currentSearchParams.isUsingCurrentLocation();
        final Double lat = currentSearchParams.getLatitude();
        final Double lon = currentSearchParams.getLongitude();
        final String currentSortField = currentSearchParams.getSort();
        final Integer currentRadiusKm = currentSearchParams.getRadiusKm();

        if (useCurrentLocation && isGpsAvailable) {
            chkCurrentLocation.setChecked(true);
            chkMapLocation.setChecked(false);
        }
        else {
            chkCurrentLocation.setChecked(false);
            chkMapLocation.setChecked(true);

            Double initialLat = (lat != null) ? lat : STANDARD_MAP_LATITUDE;
            Double initialLon = (lon != null) ? lon : STANDARD_MAP_LONGITUDE;

            setSearchOrigin(initialLat, initialLon);
        }

        if (currentKeyWords != null)
            txtKeywords.setText(currentKeyWords);

        int distanceBarValue;

        if (currentRadiusKm == null || currentRadiusKm < 5)     distanceBarValue = 0;
        else if (currentRadiusKm < 10)                          distanceBarValue = 1;
        else if (currentRadiusKm < 50)                          distanceBarValue = 2;
        else                                                    distanceBarValue = 3;

        distanceBar.setProgress(distanceBarValue);
        txtDistance.setText( getDistanceText(distanceBarValue) );

        if (currentSortField != null) {
            StringStringSpinnerAdapter adapter = (StringStringSpinnerAdapter) spnSort.getAdapter();

            int pos = adapter.getKeyPosition(currentSortField);
            if (pos >= 0)
                spnSort.setSelection(pos);
        }

        contentLayout.setVisibility(VISIBLE);
    }

    // Gets the text to show when a value in the distance bar is selected
    private String getDistanceText(int distanceValue) {

        String distanceText = "";

        if (distanceValue == 0)
            distanceText = getString(R.string.pub_search_settings_activity_distance_close);

        else if (distanceValue == 1)
            distanceText = getString(R.string.pub_search_settings_activity_distance_area);

        else if (distanceValue == 2)
            distanceText = getString(R.string.pub_search_settings_activity_distance_city);

        else if (distanceValue == 3)
            distanceText = getString(R.string.pub_search_settings_activity_distance_far);

        return distanceText;
    }

    // Gets the search radius (in km) corresponding to a selected value in the distance bar
    private int getRadiusKm(int distanceValue) {

        int radius = 1;

        if (distanceValue == 0)
            radius = 1;

        else if (distanceValue == 1)
            radius = 5;

        else if (distanceValue == 2)
            radius = 10;

        else if (distanceValue == 3)
            radius = 50;

        return radius;
    }

    // Validates the form data. If some input is invalid then shows a message and returns null.
    // If all inputs are valid, returns a new PubSearchParams object with the data in the form.
    private @Nullable PubSearchParams validateForm() {

        int offset = 0;

        String keyWords = txtKeywords.getText().toString();
        if (keyWords.equals(""))
            keyWords = null;

        // This has no effect to launch the search
        // (it is used only to restore the search settings the next time this activity is invoked)
        boolean useCurrentLocation = false;
        if (chkCurrentLocation.isChecked())
            useCurrentLocation = true;

        Double lat = searchOrigin[0];
        Double lon = searchOrigin[1];

        StringStringSpinnerAdapter adapter = (StringStringSpinnerAdapter) spnSort.getAdapter();
        String sortBy = adapter.getKey( spnSort.getSelectedItemPosition() );
        if (sortBy.equals(StringStringSpinnerAdapter.DEFAULT_KEY))
            sortBy = null;

        Integer radiusKm = getRadiusKm(distanceBar.getProgress());

        return new PubSearchParams(sortBy, keyWords, radiusKm, offset, lat, lon, useCurrentLocation);
    }

    // Gets back to the previous activity, passing the new pub search parameters (if any)
    private void finishActivity(@Nullable PubSearchParams newSearchParams) {

        Navigator.backFromPubSearchActivity(this, newSearchParams);
    }
}
