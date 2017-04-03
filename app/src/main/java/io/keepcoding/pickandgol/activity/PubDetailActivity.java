package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.ImagePagerAdapter;
import io.keepcoding.pickandgol.interactor.GetFavoritesInteractor;
import io.keepcoding.pickandgol.interactor.GetFavoritesInteractor.GetFavoritesInteractorListener;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor;
import io.keepcoding.pickandgol.interactor.SearchEventsInteractor.SearchEventsInteractorListener;
import io.keepcoding.pickandgol.interactor.ToggleFavoriteInteractor;
import io.keepcoding.pickandgol.interactor.ToggleFavoriteInteractor.ToggleFavoriteInteractorListener;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.search.EventSearchParams;
import io.keepcoding.pickandgol.util.Utils;
import me.relex.circleindicator.CircleIndicator;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.navigator.Navigator.NEW_EVENT_ACTIVITY_REQUEST_CODE;


/**
 * This activity shows the detail of a given Pub object passed as argument in the intent.
 */
public class PubDetailActivity extends AppCompatActivity {

    private final static String LOG_TAG = "PubDetailActivity";

    // Key strings for arguments passed in the intent
    public final static String PUB_MODEL_KEY = "PUB_MODEL_KEY";

    private Pub model;
    private boolean isFavorite;

    private SessionManager sm;
    private GeoManager gm;

    // Reference to UI elements to be bound with Butterknife
    @BindView(R.id.activity_pub_detail_name)             TextView txtName;
    @BindView(R.id.activity_pub_detail_favorite)        ImageView favoriteStatusIndicator;
    @BindView(R.id.activity_pub_detail_image_layout)    LinearLayout imgLayout;
    @BindView(R.id.activity_pub_detail_image_frame)     FrameLayout imgFrame;
    @BindView(R.id.activity_pub_detail_image_pager)     ViewPager imagePager;
    @BindView(R.id.activity_pub_detail_image_indicator) CircleIndicator circleIndicator;
    @BindView(R.id.activity_pub_detail_address_text)    TextView txtAddress;
    @BindView(R.id.activity_pub_detail_url_text)         TextView txtUrl;
    @BindView(R.id.activity_pub_detail_events_text)     TextView txtEventCounter;
    @BindView(R.id.activity_pub_detail_events_button)   Button btnEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_detail);
        ButterKnife.bind(this);

        sm = SessionManager.getInstance(this);
        gm = new GeoManager(this);

        setupActionBar();
        setupButtons();

        loadModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.pub_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null);
                return true;

            case R.id.pub_detail_menu_new_event:
                Navigator.fromPubDetailActivityToNewEventActivity(PubDetailActivity.this, model);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the new event activity
        if (requestCode == NEW_EVENT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            Utils.shortSnack(PubDetailActivity.this, "The new event has been created.");
        }
    }


    /*** Auxiliary methods: ***/

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Pub Detail");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Set listeners for the activity buttons
    private void setupButtons() {

        favoriteStatusIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFavoriteStatus();
            }
        });

        btnEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPubEvents();
            }
        });
    }

    // Gets the pub passed from the intent and shows its data on screen
    private void loadModel() {

        Intent i = getIntent();
        model = (Pub) i.getSerializableExtra(PUB_MODEL_KEY);

        if (model == null)
            finishActivity(new Exception("No pub was provided to the activity"));

        // Show the pub name and url (if any)
        txtName.setText( model.getName() );

        String url = (model.getUrl() != null) ? model.getUrl() : "< No website defined >";
        txtUrl.setText(url);

        // Data that requires additional async operations to show on screen
        showPubImages();
        showFavoriteStatus();
        showPubAddress();
        showEventCounter();
    }

    // Loads the Pub images (if any)
    private void showPubImages() {

        if (model.getPhotos() == null || model.getPhotos().size() == 0) {
            imgLayout.setVisibility(GONE);
            return;
        }

        ImagePagerAdapter adapter = new ImagePagerAdapter(this, model.getPhotos());
        imagePager.setAdapter(adapter);

        // The circle indicator will be shown only if there are more than one photos to show
        if (model.getPhotos().size() > 1)  circleIndicator.setViewPager(imagePager);
        else                               circleIndicator.setVisibility(GONE);
    }

    // Shows the favorite status of the pub (favorite: a filled star, non-favorite: an empty star)
    private void showFavoriteStatus() {

        // The favorite indicator will be visible only when we can determine the pub status
        // (if we are not logged in, or the operation fails, it will not be visible)
        favoriteStatusIndicator.setVisibility(INVISIBLE);

        if (!sm.hasSessionStored())
            return;

        final String userId = sm.getUserId();
        final String token = sm.getSessionToken();

        new GetFavoritesInteractor().execute(PubDetailActivity.this,
                userId,
                token,
                new GetFavoritesInteractorListener() {
                    @Override
                    public void onGetFavoritesFail(Exception e) {
                        Log.e(LOG_TAG, "Failed to retrieve favorites for user '" + userId + "': " + e.toString() );
                    }

                    @Override
                    public void onGetFavoritesSuccess(PubAggregate favorites) {

                        isFavorite = (favorites.search(model.getId()) != null );

                        if (isFavorite) favoriteStatusIndicator.setImageResource(R.drawable.filled_star);
                        else            favoriteStatusIndicator.setImageResource(R.drawable.empty_star);

                        favoriteStatusIndicator.setVisibility(VISIBLE);
                    }
                });
    }

    // Shows the pub address
    private void showPubAddress() {

        txtAddress.setText("< Resolving address... >");

        final String undefinedAddress = "< Undefined address >";

        if ( !model.hasLocation() ) {
            txtAddress.setText(undefinedAddress);
            return;
        }

        double lat = model.getLatitude();
        double lon = model.getLongitude();

        gm.requestReverseDecodedAddress(lat, lon, new GeoManager.GeoReverseLocationListener() {
            @Override
            public void onReverseLocationError(Throwable error) {
                txtAddress.setText(undefinedAddress);
            }

            @Override
            public void onReverseLocationSuccess(@NonNull List<Address> addresses) {
                txtAddress.setText( Utils.getAddressString(addresses.get(0)) );
            }
        });
    }

    // Get the list of future events in this pub, then show the event counter
    // and (if there is any future event) show the events button as well
    private void showEventCounter() {

        txtEventCounter.setText("< Fetching events... >");
        btnEvents.setVisibility(INVISIBLE);

        // TODO: search events by pub ID
        //final String pubId = model.getId();
        final String pubId = null;
        EventSearchParams searchParams = new EventSearchParams(pubId, null, null, null, null, null, null);

        new SearchEventsInteractor().execute(this, searchParams, new SearchEventsInteractorListener() {
            @Override
            public void onSearchEventsFail(Exception e) {
                Log.e(LOG_TAG, "Failed to load events for pub '" + pubId + "': " + e.toString() );

                txtEventCounter.setText("< Unable to retrieve events for this pub >");
            }

            @Override
            public void onSearchEventsSuccess(EventAggregate events) {

                if (events.getTotalResults() > 0) {
                    txtEventCounter.setText(events.getTotalResults() +" event(s).");
                    btnEvents.setVisibility(VISIBLE);
                }
                else
                    txtEventCounter.setText("This pub has no events yet.");
            }
        });
    }

    // Sends a request to the server in order to toggle the favorite status of the pub
    private void toggleFavoriteStatus() {

        if (!sm.hasSessionStored())
            return;

        final String pubId = model.getId();
        String userId = sm.getUserId();
        String token = sm.getSessionToken();

        new ToggleFavoriteInteractor().execute(this, !isFavorite, pubId, userId, token,
                                             new ToggleFavoriteInteractorListener() {
            @Override
            public void onToggleFavoriteFail(Exception e) {
                Log.e(LOG_TAG, "Failed to change favorite status to '"+ !isFavorite +"' for pub '" + pubId + "': " + e.getMessage() );

                String errorMsg;
                if (isFavorite) errorMsg = "Unable to remove the pub from your favorites.";
                else            errorMsg = "Unable to add the pub to your favorites.";

                Utils.simpleDialog(PubDetailActivity.this, "Toggle Favorite error", errorMsg);
            }

            @Override
            public void onToggleFavoriteSuccess(User user) {

                isFavorite = !isFavorite;

                if (isFavorite) favoriteStatusIndicator.setImageResource(R.drawable.filled_star);
                else            favoriteStatusIndicator.setImageResource(R.drawable.empty_star);

                String msg;
                if (isFavorite) msg = "Pub added to your favorites";
                else            msg = "Pub removed from your favorites";

                Utils.shortSnack(PubDetailActivity.this, msg);
            }
        });
    }

    // Launches the activity to show the associated events
    private void showPubEvents() {

        Navigator.fromPubDetailActivityToPubEventsActivity(this, model);
    }

    // Finishes this activity, logging the passed error (if not null)
    private void finishActivity(@Nullable Exception error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.toString());

        this.finish();
    }
}
