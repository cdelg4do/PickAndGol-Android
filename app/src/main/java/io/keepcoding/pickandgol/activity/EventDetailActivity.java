package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.interactor.GetCategoriesInteractor;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.model.Category;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This activity shows the detail of a given Event object passed as argument in the intent.
 */
public class EventDetailActivity extends AppCompatActivity {

    private final static String LOG_TAG = "EventDetailActivity";

    // Key strings for arguments passed in the intent
    public final static String EVENT_MODEL_KEY = "EVENT_MODEL_KEY";

    private Event model;
    private CategoryAggregate categories;
    private ImageManager im;

    // Reference to UI elements to be bound with Butterknife
    @BindView(R.id.activity_event_detail_name)               TextView txtName;
    @BindView(R.id.activity_event_detail_image_layout)      LinearLayout imageLayout;
    @BindView(R.id.activity_event_detail_image_holder)      ImageView imgPhoto;
    @BindView(R.id.activity_event_detail_category_text)     TextView txtCategory;
    @BindView(R.id.activity_event_detail_description_text) TextView txtDescription;
    @BindView(R.id.activity_event_detail_date_text)         TextView txtDate;
    @BindView(R.id.activity_event_detail_time_text)         TextView txtTime;
    @BindView(R.id.activity_event_detail_pubs_text)         TextView txtPubCount;
    @BindView(R.id.activity_event_detail_pubs_button)       Button btnMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        im = ImageManager.getInstance(this);

        setupActionBar();
        setupButtons();

        loadCategoriesThenLoadModel();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*** Auxiliary methods: ***/

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Event Detail");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Set listeners for the activity buttons
    private void setupButtons() {

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMap();
            }
        });
    }

    // Loads the categories from the server, if succeeds then loads the model
    private void loadCategoriesThenLoadModel() {

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Loading categories...");
        pDialog.show();

        new GetCategoriesInteractor().execute(this, new GetCategoriesInteractor.GetCategoriesInteractorListener() {

            @Override
            public void onGetCategoriesFail(Exception e) {
                pDialog.dismiss();
                finishActivity(new Exception(e.getMessage()));
            }

            @Override
            public void onGetCategoriesSuccess(CategoryAggregate categories) {
                pDialog.dismiss();

                EventDetailActivity.this.categories = categories;
                loadModel();
            }
        });
    }

    // Gets the event passed from the intent and shows its data on screen
    // (first make sure categories is not null)
    private void loadModel() {

        Intent i = getIntent();
        model = (Event) i.getSerializableExtra(EVENT_MODEL_KEY);

        if (model == null)
            finishActivity(new Exception("No event to show was provided"));

        txtName.setText( model.getName() );

        if ( model.getPhotoUrl() == null )
            imageLayout.setVisibility(View.GONE);
        else
            im.loadImage(model.getPhotoUrl(), imgPhoto, R.drawable.error_placeholder);

        String categoryName;
        Category cat = categories.search(model.getCategory());
        if (cat != null)
            categoryName = cat.getName();
        else
            categoryName = "< Unknown Category >";

        txtCategory.setText(categoryName);

        String description = model.getDescription();
        if (description == null)
            description = "< No description available >";

        txtDescription.setText(description);

        Date date = model.getDate();
        txtDate.setText( Utils.getYyyyMmDdFormattedString(date) );
        txtTime.setText( Utils.getHhMmFormattedString(date) );

        String strPubCount;
        int pubCount = model.getPubs().size();
        if (pubCount > 0)
            strPubCount = pubCount +" pub(s)";
        else {
            strPubCount = "No pubs are showing this event yet.";
            btnMap.setVisibility(View.GONE);
        }

        txtPubCount.setText(strPubCount);
    }

    // Launches the map activity to show the associated pubs
    private void showMap() {

        boolean showUserLocation = GeoManager.isLocationAccessGranted(this);
        Navigator.fromEventDetailActivityToEventPubsMapActivity(this, model, showUserLocation);
    }

    // Finishes this activity, logging the passed error (if not null)
    private void finishActivity(@Nullable Exception error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.toString());

        this.finish();
    }
}
