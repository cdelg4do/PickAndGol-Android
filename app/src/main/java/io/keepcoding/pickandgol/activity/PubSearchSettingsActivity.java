package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.StringStringSpinnerAdapter;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.PubSearchParams;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.activity.MainActivity.CURRENT_PUB_SEARCH_PARAMS_KEY;
import static io.keepcoding.pickandgol.activity.MainActivity.SHOW_DISTANCE_SELECTOR_KEY;


/**
 * This class represents an activity where the user can adjust the settings to perform a Pub search.
 */
public class PubSearchSettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "PubSearchSettings";

    private PubSearchParams currentSearchParams;
    private boolean useLocation;

    @BindView(R.id.activity_pub_search_keywords_text)       EditText txtKeywords;
    @BindView(R.id.activity_pub_search_sort_spinner)        Spinner spnSort;
    @BindView(R.id.activity_pub_search_distance_layout)     RelativeLayout distanceLayout;
    @BindView(R.id.activity_pub_search_distance_label)      TextView txtDistance;
    @BindView(R.id.activity_pub_search_distance_bar)        SeekBar distanceBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_search_settings);
        ButterKnife.bind(this);

        setupActionBar();
        setupDistanceBar();
        setupSortSpinner();
        loadCurrentSearchSettings();
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
                PubSearchParams newSearchParams = validateForm();
                if (newSearchParams != null)
                    finishActivity(newSearchParams);

                return true;

            case R.id.pub_search_menu_reset:
                resetForm();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /** Auxiliary methods **/

    // Reset all data in the form
    private void resetForm() {

        txtKeywords.setText("");

        spnSort.setSelection(0);

        int defaultDistanceBarValue = 0;
        distanceBar.setProgress(defaultDistanceBarValue);
        txtDistance.setText( getDistanceText(defaultDistanceBarValue) );
    }

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Pub Search Settings");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        String[] fieldNamesToShow = {"Pub name", "Pub owner", "Distance from me"};

        StringStringSpinnerAdapter adapter = new StringStringSpinnerAdapter(this, fieldNames, fieldNamesToShow, "<Any order>");

        spnSort.setAdapter(adapter);
    }

    // Loads the current search settings into the form
    private void loadCurrentSearchSettings() {

        Intent i = getIntent();
        currentSearchParams = (PubSearchParams) i.getSerializableExtra(CURRENT_PUB_SEARCH_PARAMS_KEY);
        useLocation = i.getBooleanExtra(SHOW_DISTANCE_SELECTOR_KEY, false);

        String currentKeyWords = currentSearchParams.getKeyWords();
        String currentSortField = currentSearchParams.getSort();
        Integer currentRadiusKm = currentSearchParams.getRadiusKm();

        if (useLocation)
            distanceLayout.setVisibility(VISIBLE);
        else
            distanceLayout.setVisibility(GONE);

        if (currentKeyWords != null)
            txtKeywords.setText(currentKeyWords);

        if (currentSortField != null) {
            StringStringSpinnerAdapter adapter = (StringStringSpinnerAdapter) spnSort.getAdapter();

            int pos = adapter.getKeyPosition(currentSortField);
            if (pos >= 0)
                spnSort.setSelection(pos);
        }

        int distanceBarValue;

        if (currentRadiusKm == null || currentRadiusKm < 5)     distanceBarValue = 0;
        else if (currentRadiusKm < 10)                          distanceBarValue = 1;
        else if (currentRadiusKm < 50)                          distanceBarValue = 2;
        else                                                    distanceBarValue = 3;

        distanceBar.setProgress(distanceBarValue);
        txtDistance.setText( getDistanceText(distanceBarValue) );
    }

    // Gets the text to show when a value in the distance bar is selected
    private String getDistanceText(int distanceValue) {

        String distanceText = "";

        if (distanceValue == 0)
            distanceText = "Very close (1 km)";

        else if (distanceValue == 1)
            distanceText = "In my area (5 km)";

        else if (distanceValue == 2)
            distanceText = "In my city (10 km)";

        else if (distanceValue == 3)
            distanceText = "Far away (50 km)";

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

        String keyWords = txtKeywords.getText().toString();
        if (keyWords.equals(""))
            keyWords = null;

        StringStringSpinnerAdapter adapter = (StringStringSpinnerAdapter) spnSort.getAdapter();
        String selectedSortField = adapter.getKey( spnSort.getSelectedItemPosition() );
        if (selectedSortField.equals(StringStringSpinnerAdapter.DEFAULT_KEY))
            selectedSortField = null;

        Integer radius = null;
        if (useLocation)
            radius = getRadiusKm(distanceBar.getProgress());

        // The returned object will not include location coordinates (they will be determined later)
        return new PubSearchParams(selectedSortField, keyWords, radius, 0, null, null);
    }

    // Gets back to the previous activity, passing the new pub search parameters
    private void finishActivity(@NonNull PubSearchParams newSearchParams) {

        Navigator.backFromPubSearchActivity(this, newSearchParams);
    }
}
