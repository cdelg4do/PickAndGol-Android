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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.util.UUID;

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
import io.keepcoding.pickandgol.util.ErrorSuccessListener;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.PICTURES_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_PICTURES_PERMISSION;


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
    private ImageManager imageManager;
    private File imageFileToUpload;
    private PermissionChecker picturesChecker;

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

        picturesChecker = new PermissionChecker(PICTURES_SET, this);
        imageManager = ImageManager.getInstance(this);

        setupActionBar();
        setupButtons();

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageFileToUpload == null) {
                    showImagePicker();
                }
            }
        });

        loadUserInfoFromServer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FOR_PICTURES_PERMISSION) {
            picturesChecker.checkAfterAsking();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the image picker
        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {

            imageManager.handleImagePickerResult(EditUserActivity.this, requestCode, resultCode, data,
                    new ImageManager.ImagePickingListener() {

                        @Override
                        public void onImagePicked(String imagePath) {

                            if (imagePath == null) {
                                Log.e(LOG_TAG, "Failed to get the path from the image picker");
                                return;
                            }

                            else {
                                processImageThenLoadIt(new File(imagePath));
                            }
                        }
                    });
        }
    }

    // Prepares an image file to be uploaded, then shows it in the activity image holder
    private void processImageThenLoadIt(File sourceImageFile) {

        if (sourceImageFile == null || ! sourceImageFile.isFile() )
            return;

        imageManager.processImage(sourceImageFile, new ImageManager.ImageProcessingListener() {

            @Override
            public void onProcessError(Exception error) {
                Utils.simpleDialog(EditUserActivity.this, "Unable to process image", error.toString());

                imageFileToUpload = null;
                imageManager.loadImage(R.drawable.add_image_placeholder, userImage);
            }

            @Override
            public void onProcessSuccess(File resizedFile) {
                imageFileToUpload = resizedFile;
                imageManager.loadImage(imageFileToUpload, userImage, R.drawable.error_placeholder);
            }
        });
    }

    private void showImagePicker() {
        // Check if we have permission to access the camera, before opening the image picker
        picturesChecker.checkBeforeAsking(new PermissionChecker.CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String title = "Pictures access denied";
                String msg = "Pick And Gol might not be able to take pictures from your camera or gallery.";
                Utils.simpleDialog(EditUserActivity.this, title, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        imageManager.showImagePicker(EditUserActivity.this);
                    }
                });
            }

            @Override
            public void onPermissionGranted() {
                imageManager.showImagePicker(EditUserActivity.this);
            }
        });
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
                imageManager.loadImage(user.getPhotoUrl(), userImage, R.drawable.error_placeholder);

            else
                imageManager.loadImage(R.drawable.default_placeholder, userImage);
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

                final User userModified = createUserWithModifiedFields();
                if (userModified == null) {
                    return;
                }

                uploadImageIfNecessaryThenRegisterNewUser(userModified);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null);
            }
        });
    }

    private void uploadImageIfNecessaryThenRegisterNewUser(final User user) {
        if (user.getPhotoUrl() == null) {
            updateUserData(user);
            return;
        }

        final String sourcePath = imageFileToUpload.getAbsolutePath();
        final String remoteFileName = user.getPhotoUrl();

        // First, attempt to upload to the cloud the selected image
        doUploadImage(sourcePath, remoteFileName, new ErrorSuccessListener() {
            @Override
            public void onError(@Nullable Object result) {
                Utils.simpleDialog(EditUserActivity.this, "Image Upload error",
                        "The selected image could not be uploaded. Please try again.");
            }

            @Override
            public void onSuccess(@Nullable Object result) {
                User userToUpdate = new User(user.getId(), user.getEmail(), user.getName(), user.getFavorites(), user.getPhotoUrl());
                if (user.getPhotoUrl() != null) {
                    userToUpdate.setPhotoUrl(imageManager.getRemoteImageUrl(user.getPhotoUrl()));
                }

                updateUserData(userToUpdate);
            }
        });
    }

    // Updates the user profile in the server with the data from the activity form
    private void updateUserData(final User userModified) {
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

        final String name = nameText.getText().toString();
        final String email = emailText.getText().toString();
        final String oldPassword = oldPasswordText.getText().toString();
        final String newPassword = newPasswordText.getText().toString();

        User modifiedUser = new User( user.getId() );

        if ( user.getName() != null && !name.equals(user.getName()) )
            modifiedUser.setName(name);

        if ( user.getEmail() != null && !email.equals(user.getEmail()) )
            modifiedUser.setEmail(email);

        if ( !oldPassword.isEmpty() )
            modifiedUser.setOldPassword(oldPassword);

        if ( !newPassword.isEmpty() )
            modifiedUser.setNewPassword(newPassword);

        if (imageFileToUpload != null) {
            String remoteFileName = UUID.randomUUID().toString() + ".jpg";  // NOTE: this is NOT the full URL
            modifiedUser.setPhotoUrl(remoteFileName);
        }

        if (modifiedUser.getName() == null && modifiedUser.getEmail() == null && modifiedUser.getOldPassword() == null &&
                modifiedUser.getNewPassword() == null && modifiedUser.getPhotoUrl() == null) {
            return null;
        }

        return modifiedUser;
    }

    // Update the model info with the data from the given User object
    private void updateModelWithUserModified(final User userModified) {

        user.setName( userModified.getName() );
        user.setEmail( userModified.getEmail() );
        user.setPhotoUrl(userModified.getPhotoUrl());
    }

    // Finishes this activity, sending back the given User object
    private void finishActivity(final User user) {
        Navigator.backFromEditUserActivity(this, user);
    }

    // Upload file to Amazon S3 bucket
    private void doUploadImage(final String filePath, final String remoteFileName, final ErrorSuccessListener listener) {
        if( ! new File(filePath).isFile() ) {
            Log.e(LOG_TAG, "An error occurred: '"+ filePath +"' does not exist or is not a file");
            Utils.simpleDialog(EditUserActivity.this, "Image Upload error",
                    "The selected image could not be uploaded. Please try again.");
            return;
        }

        File sourceFile = new File(filePath);

        int kbTotal = (int) (sourceFile.length() / 1024.0f);         // progress will be measured in KB
        String fileSize = Utils.readableSize(sourceFile.length());   // a string ended with B/KB/MB/...

        Log.d(LOG_TAG, "Uploading file '"+ filePath +"' ("+ fileSize +")...");
        final ProgressDialog pDialog =
                Utils.newProgressBarDialog(this, kbTotal, "Uploading image ("+ fileSize +")...");
        pDialog.show();


        imageManager.uploadImage(sourceFile, remoteFileName, new ImageManager.ImageUploadListener() {

            @Override
            public void onProgressChanged(int transferId, long bytesCurrent, long bytesTotal) {
                int kbCurrent = (int) (bytesCurrent / 1024.0f);
                pDialog.setProgress( kbCurrent );
            }

            @Override
            public void onImageUploadError(int transferId, Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "[id "+ transferId +"] An error occurred: "+ e.toString());

                listener.onError(null);
            }

            @Override
            public void onImageUploadCompletion(int transferId) {
                pDialog.dismiss();
                Log.d(LOG_TAG, "[id "+ transferId +"] File '" + filePath + "' has been stored as: "+ remoteFileName);

                listener.onSuccess(null);
            }
        });
    }
}
