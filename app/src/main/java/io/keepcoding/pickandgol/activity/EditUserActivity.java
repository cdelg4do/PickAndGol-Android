package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.interactor.GetUserInfoInteractor;
import io.keepcoding.pickandgol.interactor.GetUserInfoInteractor.GetUserInfoInteractorListener;
import io.keepcoding.pickandgol.interactor.UpdateUserInfoInteractor;
import io.keepcoding.pickandgol.interactor.UpdateUserInfoInteractor.UpdateUserInfoInteractorListener;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.Utils;


/**
 * This activity shows the detail of a given User object passed as argument in the intent,
 * and allows the user to update his profile info on the server.
 */
public class EditUserActivity extends AppCompatActivity {

    private static final String LOG_TAG = "EditUserActivity";

    // Key strings for arguments received/sent in the intent
    public static final String SAVED_USER_KEY = "SAVED_USER_KEY";

    private User user;
    private String token;
    private ImageManager im;

    // Reference to UI elements to be bound with Butterknife
    @BindView(R.id.edit_user_email_edit_text) EditText emailText;
    @BindView(R.id.edit_user_name_edit_text) EditText nameText;
    @BindView(R.id.edit_user_old_password_edit_text) EditText oldPasswordText;
    @BindView(R.id.edit_user_new_password_edit_text) EditText newPasswordText;
    @BindView(R.id.edit_user_circle_image) ImageView userImage;
    @BindView(R.id.edit_user_button_save) Button saveButton;
    @BindView(R.id.edit_user_button_cancel) Button cancelButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        ButterKnife.bind(this);

        im = ImageManager.getInstance(this);

        setupActionBar();
        setupButtons();

        loadUserInfoFromServer();
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

        setTitle(getString(R.string.activity_edit_user_title));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // Loads the profile info from the server and, in case of success, shows it on screen
    private void loadUserInfoFromServer() {

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Fetching user info...");
        pDialog.show();

        SessionManager sm = SessionManager.getInstance(this);
        final String id = sm.getUserId();
        token = sm.getSessionToken();

        new GetUserInfoInteractor().execute(this, id, token, new GetUserInfoInteractorListener() {
            @Override
            public void onUserDetailFail(Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "Failed to retrieve detail for user '" + id + "': " + e.toString() );

                Utils.simpleDialog(EditUserActivity.this, getString(R.string.activity_edit_user_load_error), e.getMessage());
            }

            @Override
            public void onUserDetailSuccess(User user) {
                pDialog.dismiss();

                EditUserActivity.this.user = user;
                loadUserDataInWidgets();
            }
        });
    }

    // Shows the downloaded user info on screen
    private void loadUserDataInWidgets() {

        if (user != null) {

            emailText.setText(user.getEmail());
            nameText.setText(user.getName());

            if (user.getPhotoUrl() != null)
                im.loadImage(user.getPhotoUrl(), userImage, R.drawable.error_placeholder);

            else
                im.loadImage(R.drawable.default_placeholder, userImage);
        }
    }

    // Set listeners for the activity buttons
    private void setupButtons() {

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validatePasswordEntries()) {
                    return;
                }

                updateUserData();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null);
            }
        });
    }

    // Updates the user profile in the server with the data from the activity form
    private void updateUserData() {

        final User userModified = createUserWithModifiedFields();
        updateModelWithUserModified(userModified);

        new UpdateUserInfoInteractor().execute(this, token, userModified,
                                               new UpdateUserInfoInteractorListener() {

            @Override
            public void onUpdateUserFail(Exception e) {
                Utils.simpleDialog(EditUserActivity.this, getString(R.string.activity_edit_user_modify_error), e.getMessage());
            }

            @Override
            public void onUpdateUserSuccess(User updatedUser) {
                finishActivity(user);
            }
        });
    }

    // Returns true if the password entries in the form are valid, false in other case.
    private boolean validatePasswordEntries() {

        boolean isOldPasswordEmpty = oldPasswordText.getText().toString().isEmpty();
        boolean isNewPasswordEmpty = newPasswordText.getText().toString().isEmpty();

        if ((isOldPasswordEmpty && !isNewPasswordEmpty) || (!isOldPasswordEmpty && isNewPasswordEmpty)) {
            Utils.simpleDialog(this, getString(R.string.activity_edit_user_title), getString(R.string.activity_edit_user_validate_password_error));
            return false;
        }

        return true;
    }

    // Builds a new user object using the data in the form
    private User createUserWithModifiedFields() {

        User modifiedUser = new User( user.getId() );

        final String name = nameText.getText().toString();
        final String email = emailText.getText().toString();
        final String oldPassword = oldPasswordText.getText().toString();
        final String newPassword = newPasswordText.getText().toString();

        if ( user.getName() != null && !name.equals(user.getName()) )
            modifiedUser.setName(name);

        if ( user.getEmail() != null && !email.equals(user.getEmail()) )
            modifiedUser.setEmail(email);

        if ( !oldPassword.isEmpty() )
            modifiedUser.setOldPassword(oldPassword);

        if ( !newPassword.isEmpty() )
            modifiedUser.setNewPassword(newPassword);

        return modifiedUser;
    }

    // Update the model info with the data from the given User object
    private void updateModelWithUserModified(final User userModified) {

        user.setName( userModified.getName() );
        user.setEmail( userModified.getEmail() );
    }

    // Finishes this activity, sending back the given User object
    private void finishActivity(final User user) {
        Navigator.backFromEditUserActivity(this, user);
    }
}
