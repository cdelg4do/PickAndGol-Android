package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.IntegerStringSpinnerAdapter;
import io.keepcoding.pickandgol.interactor.GetCategoriesInteractor;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.EventSearchParams;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.activity.MainActivity.CURRENT_EVENT_SEARCH_PARAMS_KEY;
import static io.keepcoding.pickandgol.activity.MainActivity.SHOW_DISTANCE_SELECTOR_KEY;

public class EventSearchSettingsActivity extends AppCompatActivity {
    private static final String LOG_TAG = EventSearchSettingsActivity.class.getCanonicalName();

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
        loadCurrentSearchSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                EventSearchParams newSearchParams = validateForm();
                if (newSearchParams != null)
                    finishActivity(newSearchParams);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Set the layout toolbar as the activity action bar
    // and show the home button
    private void setupActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }


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


    private void setupCategorySpinner() {
        GetCategoriesInteractor interactor = new GetCategoriesInteractor();
        interactor.execute(this, new GetCategoriesInteractor.Listener() {
            @Override
            public void onFail(String message) {
                Log.e(LOG_TAG, message);
            }

            @Override
            public void onSuccess(CategoryAggregate categories) {
                IntegerStringSpinnerAdapter adapter = IntegerStringSpinnerAdapter.createAdapterForCategoriesSpinner(EventSearchSettingsActivity.this, categories, getString(R.string.event_search_settings_activity_spinner_default_text));
                spnCategory.setAdapter(adapter);
            }
        });
    }


    private void loadCurrentSearchSettings() {

        Intent i = getIntent();
        currentSearchParams = (EventSearchParams) i.getSerializableExtra(CURRENT_EVENT_SEARCH_PARAMS_KEY);
        useLocation = i.getBooleanExtra(SHOW_DISTANCE_SELECTOR_KEY, false);

        String currentKeyWords = currentSearchParams.getKeyWords();
        Integer currentCategoryId = currentSearchParams.getCategoryId();
        Integer currentRadiusKm = currentSearchParams.getRadiusKm();

        if (useLocation)
            distanceLayout.setVisibility(VISIBLE);
        else
            distanceLayout.setVisibility(GONE);

        if (currentKeyWords != null)
            txtKeywords.setText(currentKeyWords);

        if (currentCategoryId != null) {
            IntegerStringSpinnerAdapter adapter = (IntegerStringSpinnerAdapter) spnCategory.getAdapter();

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


    private @Nullable EventSearchParams validateForm() {

        String keyWords = txtKeywords.getText().toString();
        Integer selectedCategoryId = (int) spnCategory.getSelectedItemId();
        Integer radius = null;

        if (keyWords.equals(""))
            keyWords = null;

        if (useLocation)
            radius = getRadiusKm(distanceBar.getProgress());

        if (selectedCategoryId == 0)
            selectedCategoryId = null;

        // The returned object will not include location coordinates (they will be determined later)
        return new EventSearchParams(null, keyWords, selectedCategoryId, radius, 0, null, null);
    }


    private void finishActivity(@NonNull EventSearchParams newSearchParams) {

        Navigator.backFromEventSearchActivity(this, newSearchParams);
    }
}
