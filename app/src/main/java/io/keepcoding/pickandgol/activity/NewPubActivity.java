package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.interactor.CreatePubInteractor;
import io.keepcoding.pickandgol.interactor.CreatePubInteractor.CreatePubInteractorListener;
import io.keepcoding.pickandgol.manager.geo.GeoManager;
import io.keepcoding.pickandgol.manager.geo.GeoManager.GeoReverseLocationListener;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImagePickingListener;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImageProcessingListener;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.ErrorSuccessListener;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.Utils;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.SELECTED_LATITUDE_KEY;
import static io.keepcoding.pickandgol.activity.LocationPickerActivity.SELECTED_LONGITUDE_KEY;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;
import static io.keepcoding.pickandgol.navigator.Navigator.LOCATION_PICKER_ACTIVITY_REQUEST_CODE;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.LOCATION_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.PICTURES_SET;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_PICTURES_PERMISSION;
import static io.keepcoding.pickandgol.util.PermissionChecker.REQUEST_FOR_LOCATION_PERMISSION;


/**
 * This class represents the activity with the New Pub form.
 */
public class NewPubActivity extends AppCompatActivity {

    private final static String LOG_TAG = "NewPubActivity";

    public final static String NEW_PUB_KEY = "NEW_PUB_KEY"; // Used to return the new pub in the intent
    public final static int MAX_IMAGES = 4;                 // Max images that can be attached to a pub


    private SessionManager sm;
    private ImageManager im;
    private GeoManager gm;

    private PermissionChecker picturesChecker;
    private PermissionChecker locationChecker;

    private Double latitude, longitude;

    private File[] imageFilesToUpload;
    private TableRow[] imageTableRows;
    private ImageView[] imageHolders;
    private Button[] imageButtons;

    @BindView(R.id.activity_new_pub_name_text)         EditText txtName;
    @BindView(R.id.activity_new_pub_location_text)     EditText txtLocation;
    @BindView(R.id.activity_new_pub_web_text)           EditText txtUrl;
    @BindView(R.id.activity_new_pub_image_table)        TableLayout imageTable;
    @BindView(R.id.activity_new_pub_button_cancel)    Button btnCancel;
    @BindView(R.id.activity_new_pub_button_create)    Button btnCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pub);
        ButterKnife.bind(this);

        picturesChecker = new PermissionChecker(PICTURES_SET, this);
        locationChecker = new PermissionChecker(LOCATION_SET, this);

        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);
        gm = new GeoManager(this);

        setupActionBar();
        setupPickers();
        setupButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.new_pub_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null, null);
                return true;

            case R.id.new_pub_menu_reset:
                resetForm();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /*** Handlers for activity requests (permissions, intents) ***/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_FOR_PICTURES_PERMISSION)
            picturesChecker.checkAfterAsking();

        else if (requestCode == REQUEST_FOR_LOCATION_PERMISSION)
            locationChecker.checkAfterAsking();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the location picker
        if (requestCode == LOCATION_PICKER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            final Double selectedLat = (double) data.getSerializableExtra(SELECTED_LATITUDE_KEY);
            final Double selectedLong = (double) data.getSerializableExtra(SELECTED_LONGITUDE_KEY);

            if (selectedLat != null && selectedLong != null) {

                latitude = selectedLat;
                longitude = selectedLong;

                gm.requestReverseDecodedAddress(selectedLat, selectedLong,
                        new GeoReverseLocationListener() {
                            @Override
                            public void onReverseLocationError(Throwable error) {
                                txtLocation.setText("Undefined location ("+ latitude +", "+ longitude +")");
                            }

                            @Override
                            public void onReverseLocationSuccess(@NonNull List<Address> addresses) {
                                txtLocation.setText( Utils.getAddressString(addresses.get(0)) );
                            }
                        });
            }
        }

        // If we are coming from the image picker
        else if (requestCode == IMAGE_PICKER_REQUEST_CODE) {

            im.handleImagePickerResult(NewPubActivity.this, requestCode, resultCode, data,
                    new ImagePickingListener() {

                        @Override
                        public void onImagePicked(String imagePath) {

                            if (imagePath == null) {
                                Log.e(LOG_TAG, "Failed to get the path from the image picker");
                                return;
                            }

                            else {
                                processImageThenLoadIt( new File(imagePath) );
                            }
                        }
                    });
        }
    }


    /*** Auxiliary methods ***/

    // Reset all data in the form
    private void resetForm() {

        txtName.setText("");
        txtLocation.setText("");
        txtUrl.setText("");

        latitude = longitude = null;

        imageFilesToUpload = new File[MAX_IMAGES];
        resetImageTableRows();

        txtName.requestFocus();
    }

    // Set the layout toolbar as the activity action bar and show the home button
    private void setupActionBar() {

        setTitle("Create a Pub");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Set listeners for the activity buttons
    private void setupButtons() {

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null, null);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pub validatedData = validateFormData();

                if ( validatedData != null )
                    uploadImagesIfNecessaryThenRegisterNewPub(validatedData);
            }
        });
    }

    // Sets the behaviour for the location and picture pickers
    private void setupPickers() {

        // Location picker:
        latitude = longitude = null;
        txtLocation.setText("");

        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If we have access to the device location, attempt to get it and pass it.
                // If not, just pass a null location to the location picker activity.
                locationChecker.checkBeforeAsking(new PermissionChecker.CheckPermissionListener() {
                    @Override
                    public void onPermissionDenied() {
                        Navigator.fromNewPubActivityToLocationPickerActivity(NewPubActivity.this, null, null);
                    }

                    @Override
                    public void onPermissionGranted() {

                        gm.requestLastLocation(new GeoManager.GeoDirectLocationListener() {
                            @Override
                            public void onLocationError(Throwable error) {
                                Navigator.fromNewPubActivityToLocationPickerActivity(NewPubActivity.this, null, null);
                            }

                            @Override
                            public void onLocationSuccess(double latitude, double longitude) {
                                Navigator.fromNewPubActivityToLocationPickerActivity(NewPubActivity.this, latitude, longitude);
                            }
                        });
                    }
                });
            }
        });


        // Image pickers:

        imageFilesToUpload = new File[MAX_IMAGES];
        imageTableRows = new TableRow[MAX_IMAGES];
        imageHolders = new ImageView[MAX_IMAGES];
        imageButtons = new Button[MAX_IMAGES];

        for (int i = 0; i < MAX_IMAGES; i++) {

            imageFilesToUpload[i] = null;

            imageTableRows[i] = (TableRow) LayoutInflater.from(this).inflate(R.layout.row_add_picture, null);

            imageHolders[i] = (ImageView) imageTableRows[i].findViewById(R.id.image_holder);
            imageHolders[i].setImageResource(R.drawable.add_image_placeholder);

            imageButtons[i] = (Button) imageTableRows[i].findViewById(R.id.image_button);
            imageButtons[i].setVisibility(INVISIBLE);

            imageTable.addView(imageTableRows[i]);

            if (i > 0)
                imageTableRows[i].setVisibility(GONE);

            final int j = i;

            imageHolders[j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // If no image chosen, show the image picker
                    if (imageFilesToUpload[j] == null)
                        showImagePicker();

                    // If an image is already chosen
                    else {
                        // TODO: show selected image in full screen
                    }
                }
            });

            imageButtons[j].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeImage(j);
                }
            });
        }
    }

    // Checks if the app has permission to access the camera/gallery, if so then opens the image picker
    private void showImagePicker() {

        // Check if we have permission to access the camera, before opening the image picker
        picturesChecker.checkBeforeAsking(new PermissionChecker.CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {

                String title = "Pictures access denied";
                String msg = "Pick And Gol might not be able to take pictures from your camera or gallery.";
                Utils.simpleDialog(NewPubActivity.this, title, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        im.showImagePicker(NewPubActivity.this);
                    }
                });
            }

            @Override
            public void onPermissionGranted() {
                im.showImagePicker(NewPubActivity.this);
            }
        });
    }

    // Prepares an image file to be uploaded, then shows it in an activity image holder
    private void processImageThenLoadIt(File sourceImageFile) {

        if (sourceImageFile == null || ! sourceImageFile.isFile() )
            return;

        final int index = getFirstNullImageIndex();
        if (index >= MAX_IMAGES)
            return;

        // The resulting temp image will have a unique filename
        // (this allows to process several images in a row, and keep them all for later use)
        String randomFileName = UUID.randomUUID().toString();

        im.processImage(sourceImageFile, randomFileName, new ImageProcessingListener() {
            @Override
            public void onProcessError(Exception error) {
                Utils.simpleDialog(NewPubActivity.this, "Unable to process image", error.toString());

                imageFilesToUpload[index] = null;
                imageButtons[index].setVisibility(INVISIBLE);
                im.loadImage(R.drawable.add_image_placeholder, imageHolders[index]);
            }

            @Override
            public void onProcessSuccess(File resizedFile) {

                imageFilesToUpload[index] = resizedFile;
                imageButtons[index].setVisibility(VISIBLE);
                im.loadImage(imageFilesToUpload[index], imageHolders[index], R.drawable.error_placeholder);

                // If it is still possible to add more images, show the next image picker row
                if (index+1 < MAX_IMAGES)
                    imageTableRows[index+1].setVisibility(VISIBLE);
            }
        });
    }

    // If all fields in the form are valid, returns a Pub object with the form values.
    // (if there are invalid field values, then returns null)
    private @Nullable Pub validateFormData() {

        if ( !sm.hasSessionStored() ) {

            Utils.simpleDialog(this, "Pub Creation Error", "You are not logged in.");
            return null;
        }

        Pub tempPub = null;     // This object is just a container of some data from the form

        String pubName = txtName.getText().toString();
        String pubUrl = txtUrl.getText().toString();

        if (pubName.equals("")) {
            Utils.simpleDialog(this, "Invalid data", "You must specify a name for the pub.");
            txtName.requestFocus();
        }

        else if (latitude == null || longitude == null) {
            Utils.simpleDialog(this, "Invalid location", "You must select a location from the map.");
            txtLocation.requestFocus();
        }

        // All data are correct, prepare the temp Pub
        else {

            if (pubUrl.equals(""))
                pubUrl = null;

            List<String> events = new ArrayList<String>();
            List<String> remoteImageNames = getRemoteImageFilenames();

            tempPub = new Pub("", pubName, latitude, longitude, pubUrl, "", events, remoteImageNames);
        }

        return tempPub;
    }

    // Attempts to upload the selected images (if any) to S3, then calls to register the given pub
    private void uploadImagesIfNecessaryThenRegisterNewPub(final Pub pub) {

        if (pub.getPhotos().size() == 0) {
            registerNewPub(pub);
            return;
        }

        doUploadImages(imageFilesToUpload, pub.getPhotos(), new ErrorSuccessListener() {
            @Override
            public void onError(@Nullable Object result) {
                Utils.simpleDialog(NewPubActivity.this, "Image Upload error",
                        "There were errors uploading the pictures. The new pub was NOT registered, please try again.");
            }

            @Override
            public void onSuccess(@Nullable Object result) {
                registerNewPub(pub);
            }
        });
    }

    // Attempts to upload the given image files to Amazon S3 (one after each other)
    // In case one of them fails, the process stops and listener.onError() is called.
    // If all the images are uploaded, then listener.onGetCategoriesSuccess() is called.
    private void doUploadImages(final File[] imageFiles, final List<String> remoteFilenames, final ErrorSuccessListener listener) {

        if (remoteFilenames.size() == 0) {
            listener.onSuccess(null);
            return;
        }

        Log.d(LOG_TAG, "Attempting to upload "+ remoteFilenames.size() +" image(s)");

        ProgressDialog pDialog = Utils.newProgressBarDialog(this, 100, "Uploading images...");
        pDialog.show();

        uploadNextImage(0, imageFiles, remoteFilenames, pDialog, listener);
    }

    // Attempts to upload the given current image.
    // In case of success, a recursive call with the next image is executed.
    private void uploadNextImage(final int current, final File[] imageFiles, final List<String> remoteFilenames, final ProgressDialog pDialog, final ErrorSuccessListener listener) {

        // If there are still pictures to upload
        if (current < remoteFilenames.size()) {

            final String filePath = imageFiles[current].getAbsolutePath();
            final String remoteFileName = remoteFilenames.get(current);

            if( ! new File(filePath).isFile() ) {
                Log.e(LOG_TAG, "An error occurred: '"+ filePath +"' does not exist or is not a file");
                listener.onError(null);
                return;
            }

            File sourceFile = new File(filePath);

            int kbTotal = (int) (sourceFile.length() / 1024.0f);         // progress will be measured in KB
            String fileSize = Utils.readableSize(sourceFile.length());   // a string ended with B/KB/MB/...

            Log.d(LOG_TAG, "Uploading file '"+ filePath +"' ("+ fileSize +")...");

            pDialog.setMessage("Uploading pictures ("+ (1+current) +"/"+ remoteFilenames.size() +")...");
            pDialog.setProgress(0);
            pDialog.setMax(kbTotal);

            im.uploadImage(sourceFile, remoteFileName, new ImageManager.ImageUploadListener() {

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
                    Log.d(LOG_TAG, "[id "+ transferId +"] File '" + filePath + "' has been stored as: "+ remoteFileName);

                    uploadNextImage(current+1, imageFiles, remoteFilenames, pDialog, listener);
                }
            });
        }

        // If we already uploaded all the images successfully
        else {
            pDialog.dismiss();
            Log.d(LOG_TAG, "All images uploaded successfully");

            listener.onSuccess(null);
        }
    }

    // Sends a remote request to register a new pub in the system
    private void registerNewPub(Pub pub) {

        String name = pub.getName();
        double latitude = pub.getLatitude();
        double longitude = pub.getLongitude();
        String userId = sm.getUserId();
        String token = sm.getSessionToken();
        String url = pub.getUrl();

        List<String> photoUrls = new ArrayList<>();
        for (int i = 0; i < pub.getPhotos().size(); i++) {
            String newPhotoUrl = im.getRemoteImageUrl( pub.getPhotos().get(i) );
            photoUrls.add(newPhotoUrl);
        }

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Registering pub...");
        pDialog.show();

        new CreatePubInteractor().execute(this, name, latitude, longitude, userId, url, photoUrls, token, new CreatePubInteractorListener() {

                    @Override
                    public void onCreatePubFail(Exception e) {
                        pDialog.dismiss();

                        Utils.simpleDialog(NewPubActivity.this, "Error registering new pub", e.getMessage());
                    }

                    @Override
                    public void onCreatePubSuccess(final Pub createdPub) {
                        pDialog.dismiss();

                        Utils.simpleDialog(NewPubActivity.this,
                                "New Pub",
                                "The pub '"+ createdPub.getName() +"' has been registered.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finishActivity(createdPub, null);
                                    }
                                });
                    }
                });
    }

    // Finishes this activity, passing back the created event (if any)
    private void finishActivity(@Nullable Pub newPub, @Nullable Exception error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.toString());

        // Remove all possible temp image files created by this activity
        im.cleanProcessorTempFolder();

        Navigator.backFromNewPubActivity(this, newPub);
    }


    /** The following methods describe the multiple image picker behavior **/

    // Clear all selected images and resets the image pickers to their initial state
    private void resetImageTableRows() {

        for (int i = 0; i < MAX_IMAGES; i++) {

            if (imageFilesToUpload[i] != null)
                imageFilesToUpload[i].delete();

            imageFilesToUpload[i] = null;
            imageHolders[i].setImageResource(R.drawable.add_image_placeholder);
            imageButtons[i].setVisibility(INVISIBLE);

            if (i != 0)
                imageTableRows[i].setVisibility(GONE);
        }
    }

    // Removes the associated selected picture from its image holder
    private void removeImage(int index) {

        if (index < 0 || index >= MAX_IMAGES)
            return;

        // Remove the image from the selected image
        imageFilesToUpload[index].delete();
        imageFilesToUpload[index] = null;
        imageHolders[index].setImageResource(R.drawable.add_image_placeholder);
        imageButtons[index].setVisibility(INVISIBLE);

        // Copy all the next positions to their previous one
        for (int i = index+1; i < MAX_IMAGES; i++) {

            imageFilesToUpload[i-1] = imageFilesToUpload[i];

            if (imageFilesToUpload[i-1] == null) {
                imageHolders[i-1].setImageResource(R.drawable.add_image_placeholder);
                imageButtons[i-1].setVisibility(INVISIBLE);
            }
            else {
                im.loadImage(imageFilesToUpload[i-1], imageHolders[i-1], R.drawable.error_placeholder);
                imageButtons[i-1].setVisibility(View.VISIBLE);
            }
        }

        // The last position will always be empty
        imageFilesToUpload[MAX_IMAGES - 1] = null;
        imageHolders[MAX_IMAGES - 1].setImageResource(R.drawable.add_image_placeholder);
        imageButtons[MAX_IMAGES - 1].setVisibility(INVISIBLE);

        // Check if there is need to hide some picker, after removing the image
        // (picker #0 always remains visible)
        for (int i = 0; i+1 < MAX_IMAGES; i++) {

            if ( imageFilesToUpload[i] == null )
                imageTableRows[i+1].setVisibility(GONE);
        }
    }

    // Gets a list with the filenames for all the remotely stored images
    private List<String> getRemoteImageFilenames() {

        ArrayList<String> imageNames = new ArrayList<>();

        // Since all local image files have a random name, we will use the same names remotely
        for (int i = 0; i < MAX_IMAGES; i++)
            if (imageFilesToUpload[i] != null)
                imageNames.add( imageFilesToUpload[i].getName() );  // note: this is NOT the full URL

        return imageNames;
    }

    // Gets the index of the first unused image picker (0 .. MAX_IMAGES)
    private @Nullable Integer getFirstNullImageIndex() {

        Integer foundIndex = null;

        for (int i = 0; i < MAX_IMAGES && foundIndex == null; i++) {

            if (imageFilesToUpload[i] == null)
                foundIndex = i;
        }

        return foundIndex;
    }
}
