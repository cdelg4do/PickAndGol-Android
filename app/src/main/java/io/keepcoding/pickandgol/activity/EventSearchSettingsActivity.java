package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import io.keepcoding.pickandgol.interactor.GetCategoriesInteractor;
import io.keepcoding.pickandgol.interactor.GetCategoriesInteractor.GetCategoriesInteractorListener;
import io.keepcoding.pickandgol.model.Category;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.EventSearchParams;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.activity.MainActivity.CURRENT_EVENT_SEARCH_PARAMS_KEY;
import static io.keepcoding.pickandgol.activity.MainActivity.SHOW_DISTANCE_SELECTOR_KEY;


/**
 * This class is the activity where the user can adjust the settings to perform an Event search.
 */
public class EventSearchSettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EventSearchSettings";

    private EventSearchParams currentSearchParams;
    private boolean useLocation;

    @BindView(R.id.activity_event_search_keywords_text)     EditText txtKeywords;
    @BindView(R.id.activity_event_search_category_spinner)  Spinner spnCategory;
    @BindView(R.id.activity_event_search_distance_layout)   RelativeLayout distanceLayout;
    @BindView(R.id.activity_event_search_distance_label)    TextView txtDistance;
    @BindView(R.id.activity_event_search_distance_bar)      SeekBar distanceBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search_settings);
        ButterKnife.bind(this);

        setupActionBar();
        setupDistanceBar();
        setupCategorySpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.event_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                EventSearchParams newSearchParams = validateForm();
                if (newSearchParams != null)
                    finishActivity(newSearchParams);

                return true;

            case R.id.event_search_menu_reset:
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

        spnCategory.setSelection(0);

        int defaultDistanceBarValue = 0;
        distanceBar.setProgress(defaultDistanceBarValue);
        txtDistance.setText( getDistanceText(defaultDistanceBarValue) );
    }

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle(getString(R.string.event_search_settings_activity_title));

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

    // Populates the category spinner and then loads the current search settings
    private void setupCategorySpinner() {

        new GetCategoriesInteractor().execute(this, new GetCategoriesInteractorListener() {
            @Override
            public void onGetCategoriesFail(Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            @Override
            public void onGetCategoriesSuccess(CategoryAggregate categories) {

                String[] categoryIDs = new String[ categories.size() ];
                String[] categoryNames = new String[ categories.size() ];

                for (int i = 0; i < categories.size(); i++) {

                    Category cat = categories.get(i);
                    categoryIDs[i] = cat.getId();
                    categoryNames[i] = cat.getName();
                }

                StringStringSpinnerAdapter adapter = new StringStringSpinnerAdapter(
                        EventSearchSettingsActivity.this,
                        categoryIDs,
                        categoryNames,
                        getString(R.string.event_search_settings_activity_all));

                spnCategory.setAdapter(adapter);

                loadCurrentSearchSettings();
            }
        });
    }

    // Loads the current search settings into the form
    private void loadCurrentSearchSettings() {

        Intent i = getIntent();
        currentSearchParams = (EventSearchParams) i.getSerializableExtra(CURRENT_EVENT_SEARCH_PARAMS_KEY);
        useLocation = i.getBooleanExtra(SHOW_DISTANCE_SELECTOR_KEY, false);

        String currentKeyWords = currentSearchParams.getKeyWords();
        String currentCategoryId = currentSearchParams.getCategoryId();
        Integer currentRadiusKm = currentSearchParams.getRadiusKm();

        if (useLocation)
            distanceLayout.setVisibility(VISIBLE);
        else
            distanceLayout.setVisibility(GONE);

        if (currentKeyWords != null)
            txtKeywords.setText(currentKeyWords);

        if (currentCategoryId != null) {
            StringStringSpinnerAdapter adapter = (StringStringSpinnerAdapter) spnCategory.getAdapter();

            int pos = adapter.getKeyPosition(currentCategoryId);
            if (pos >= 0)
                spnCategory.setSelection(pos);
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
            distanceText = getString(R.string.event_search_settings_activity_very_close);

        else if (distanceValue == 1)
            distanceText = getString(R.string.event_search_settings_activity_my_area);

        else if (distanceValue == 2)
            distanceText = getString(R.string.event_search_settings_activity_my_city);

        else if (distanceValue == 3)
            distanceText = getString(R.string.event_search_settings_activity_far_away);

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
    // If all inputs are valid, returns a new EventSearchParams object with the data in the form.
    private @Nullable EventSearchParams validateForm() {

        String keyWords = txtKeywords.getText().toString();
        if (keyWords.equals(""))
            keyWords = null;

        StringStringSpinnerAdapter adapter = (StringStringSpinnerAdapter) spnCategory.getAdapter();
        String selectedCategoryId = adapter.getKey( spnCategory.getSelectedItemPosition() );
        if (selectedCategoryId.equals(StringStringSpinnerAdapter.DEFAULT_KEY))
            selectedCategoryId = null;

        Integer radius = null;
        if (useLocation)
            radius = getRadiusKm(distanceBar.getProgress());

        // The returned object will not include location coordinates (they will be determined later)
        return new EventSearchParams(null, keyWords, selectedCategoryId, radius, 0, null, null);
    }

    // Gets back to the previous activity, passing the new event search parameters
    private void finishActivity(@NonNull EventSearchParams newSearchParams) {

        Navigator.backFromEventSearchActivity(this, newSearchParams);
    }
}
