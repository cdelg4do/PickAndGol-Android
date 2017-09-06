package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.interactor.GetCategoryInteractor;
import io.keepcoding.pickandgol.interactor.GetCategoryInteractor.GetCategoryInteractorListener;
import io.keepcoding.pickandgol.interactor.LinkEventToPubInteractor;
import io.keepcoding.pickandgol.interactor.LinkEventToPubInteractor.LinkEventToPubInteractorListener;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.Category;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.activity.PubSelectorActivity.SELECTED_PUB_KEY;
import static io.keepcoding.pickandgol.navigator.Navigator.PUB_SELECTOR_ACTIVITY_REQUEST_CODE;


/**
 * This activity shows the detail of a given Event object passed as argument in the intent.
 */
public class EventDetailActivity extends AppCompatActivity {

    private final static String LOG_TAG = "EventDetailActivity";

    // Key strings for arguments passed in the intent
    public final static String EVENT_MODEL_KEY = "EVENT_MODEL_KEY";

    private Event model;

    private SessionManager sm;
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

        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);

        setupActionBar();
        setupButtons();

        loadModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.event_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null);
                return true;

            case R.id.event_detail_menu_link_to_pub:
                attemptToLinkEvent();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the pub selector activity
        if (requestCode == PUB_SELECTOR_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            final Pub selectedPub = (Pub) data.getSerializableExtra(SELECTED_PUB_KEY);

            if (selectedPub != null) {

                Utils.questionDialog(
                        EventDetailActivity.this,
                        getString(R.string.event_detail_activity_link_pub_title),
                        getString(R.string.event_detail_activity_message_1)
                            + selectedPub.getName() + getString(R.string.event_detail_activity_message_2),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                linkToPub(selectedPub);
                            }
                        }
                );


            }
        }
    }


    /*** Auxiliary methods: ***/

    // From the Link Event menu option, attempts to link the event to a pub
    private void attemptToLinkEvent() {

        if ( !sm.hasSessionStored() ) {
            Utils.simpleDialog(this, getString(R.string.not_logged_in),
                               getString(R.string.event_detail_activity_session_error_message));
            return;
        }

        Navigator.fromEventDetailActivityToPubSelectorActivity(this);
    }

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle(getString(R.string.event_detail_activity_title));

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

    // Gets the event passed from the intent and shows its data on screen
    // (image, category name, description, date, time and num. of pubs where it is shown)
    private void loadModel() {

        Intent i = getIntent();
        model = (Event) i.getSerializableExtra(EVENT_MODEL_KEY);

        if (model == null)
            finishActivity(new Error("No event to show was provided"));

        txtName.setText( model.getName() );

        if ( model.getPhotoUrl() == null )
            imageLayout.setVisibility(View.GONE);
        else
            im.loadImage(model.getPhotoUrl(), imgPhoto, R.drawable.error_placeholder);

        new GetCategoryInteractor().execute(model.getCategory(), new GetCategoryInteractorListener() {
            @Override
            public void onGetCategoryFail(Throwable e) {
                txtCategory.setText(R.string.event_detail_activity_unspecified_category);
            }

            @Override
            public void onGetCategorySuccess(Category category) {

                if (category == null)   txtCategory.setText(R.string.event_detail_activity_unspecified_category);
                else                    txtCategory.setText( category.getName() );
            }
        });

        String description = model.getDescription();
        if (description == null)
            description = getString(R.string.event_detail_activity_no_description);

        txtDescription.setText(description);

        Date date = model.getDate();
        txtDate.setText( Utils.getYyyyMmDdFormattedString(date) );
        txtTime.setText( Utils.getHhMmFormattedString(date) );

        String strPubCount;
        int pubCount = model.getPubs().size();
        if (pubCount > 0)
            strPubCount = pubCount + " " + getString(R.string.event_detail_activity_pub);
        else {
            strPubCount = getString(R.string.event_detail_activity_no_pubs);
            btnMap.setVisibility(View.GONE);
        }

        txtPubCount.setText(strPubCount);
    }

    // Launches the map activity to show the associated pubs
    private void showMap() {

        boolean showUserLocation = GeoManager.isLocationAccessGranted(this);
        Navigator.fromEventDetailActivityToEventPubsMapActivity(this, model, showUserLocation);
    }

    // Associates the given pub to the model
    private void linkToPub(final @NonNull Pub pub) {

        final ProgressDialog pDialog = Utils.newProgressDialog(this, getString(R.string.event_detail_activity_linking_event));
        pDialog.show();

        String eventId = model.getId();
        final String pubId = pub.getId();
        String token = sm.getSessionToken();

        new LinkEventToPubInteractor().execute(this, eventId, pubId, token,
                                               new LinkEventToPubInteractorListener() {
            @Override
            public void onLinkEventPubFail(Exception e) {
                pDialog.dismiss();

                Log.e(LOG_TAG, "Failed to link event to pub '" + pubId + "': " + e.getMessage() );
                Utils.simpleDialog(EventDetailActivity.this, getString(R.string.event_detail_activity_link_event_fail_title), e.getMessage());
            }

            @Override
            public void onLinkEventPubSuccess(Pub updatedPub, Event updatedEvent) {
                pDialog.dismiss();

                Utils.simpleDialog(EventDetailActivity.this,
                        getString(R.string.event_detail_activity_link_event_success_title),
                        getString(R.string.event_detail_activity_link_event_success_message) + updatedPub.getName() +"'");
            }
        });
    }

    // Finishes this activity, logging the passed error (if not null)
    private void finishActivity(@Nullable Error error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.toString());

        this.finish();
    }
}
