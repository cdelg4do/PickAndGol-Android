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

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.interactor.EditUserInteractor;
import io.keepcoding.pickandgol.interactor.UserDetailInteractor;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.Utils;

import static android.R.attr.id;
import static io.keepcoding.pickandgol.PickAndGolApp.getContext;

public class EditUserActivity extends AppCompatActivity {
    private static final String LOG_TAG = EditUserActivity.class.getCanonicalName();

    public static final String SAVED_USER_KEY = "SAVED_USER_KEY";

    @BindView(R.id.edit_user_email_edit_text)
    EditText emailText;

    @BindView(R.id.edit_user_name_edit_text)
    EditText nameText;

    @BindView(R.id.edit_user_old_password_edit_text)
    EditText oldPasswordText;

    @BindView(R.id.edit_user_new_password_edit_text)
    EditText newPasswordText;

    @BindView(R.id.edit_user_circle_image)
    ImageView userImage;

    @BindView(R.id.edit_user_button_save)
    Button saveButton;

    @BindView(R.id.edit_user_button_cancel)
    Button cancelButton;

    private User user;
    private String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        ButterKnife.bind(this);

        setupActionBar();
        loadModel();
        setupButtons();
    }

    private void setupActionBar() {
        setTitle(getString(R.string.activity_edit_user_title));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadModel() {
        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Fetching user '" + id + "' info...");
        pDialog.show();

        SessionManager sm = SessionManager.getInstance(this);
        final String id = sm.getUserId();
        token = sm.getSessionToken();

        UserDetailInteractor interactor = new UserDetailInteractor();
        interactor.execute(this, id, token, new UserDetailInteractor.UserDetailInteractorListener() {
            @Override
            public void onUserDetailSuccess(User user) {
                pDialog.dismiss();
                EditUserActivity.this.user = user;
                loadUserDataInWidgets();
            }

            @Override
            public void onUserDetailFail(Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "Failed to retrieve detail for user '" + id + "': " + e.toString() );
                Utils.simpleDialog(EditUserActivity.this, getString(R.string.activity_edit_user_load_error), e.getMessage());
            }
        });
    }

    private void loadUserDataInWidgets() {
        if (user != null) {
            emailText.setText(user.getEmail());
            nameText.setText(user.getName());

            Picasso.with(getContext())
                    .load(user.getPhotoUrl())
                    .into(userImage);
        }
    }

    private void setupButtons() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validatePasswordEntries()) {
                    return;
                }

                saveUser();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null);
            }
        });
    }

    private void saveUser() {
        final User userModified = createUserWithModifiedFields();
        updateModelWithUserModified(userModified);
        EditUserInteractor interactor = new EditUserInteractor();
        interactor.execute(this, token, userModified, new EditUserInteractor.Listener() {
            @Override
            public void onEditUserSuccess() {
                finishActivity(user);
            }

            @Override
            public void onEditUserFail(String message) {
                Utils.simpleDialog(EditUserActivity.this, getString(R.string.activity_edit_user_modify_error), message);
            }
        });
    }

    private boolean validatePasswordEntries() {
        boolean isOldPasswordEmpty = oldPasswordText.getText().toString().isEmpty();
        boolean isNewPasswordEmpty = newPasswordText.getText().toString().isEmpty();

        if ((isOldPasswordEmpty && !isNewPasswordEmpty) || (!isOldPasswordEmpty && isNewPasswordEmpty)) {
            Utils.simpleDialog(this, getString(R.string.activity_edit_user_title), getString(R.string.activity_edit_user_validate_error));
            return false;
        }

        return true;
    }

    private User createUserWithModifiedFields() {
        User userModified = new User(user.getId());
        final String name = nameText.getText().toString();
        final String email = emailText.getText().toString();
        final String oldPassword = oldPasswordText.getText().toString();
        final String newPassword = newPasswordText.getText().toString();

        if (user.getName() != null && !name.equals(user.getName())) {
            userModified.setName(name);
        }

        if (user.getEmail() != null && !email.equals(user.getEmail())) {
            userModified.setEmail(email);
        }

        if (!oldPassword.isEmpty()) {
            userModified.setOldPassword(oldPassword);
        }

        if (!newPassword.isEmpty()) {
            userModified.setNewPassword(newPassword);
        }

        return userModified;
    }

    private void updateModelWithUserModified(final User userModified) {
        user.setName(userModified.getName());
        user.setEmail(userModified.getEmail());
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

    private void finishActivity(final User user) {
        Navigator.backFromEditUserActivity(this, user);
    }
}
