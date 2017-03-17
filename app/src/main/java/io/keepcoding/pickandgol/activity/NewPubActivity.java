package io.keepcoding.pickandgol.activity;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.util.PermissionChecker;


/**
 * This class represents the activity with the New Pub form.
 */
public class NewPubActivity extends AppCompatActivity {

    private final static String LOG_TAG = "NewPubActivity";

    public final static String NEW_PUB_KEY = "NEW_PUB_KEY";


    private SessionManager sm;
    private ImageManager im;

    private PermissionChecker cameraChecker;

    private Double latitude, longitude;
    private ArrayList<File> imageFilesToUpload;
    private ArrayList<ImageView> imageHolders;

    @BindView(R.id.activity_new_pub_name_text)         EditText txtName;
    @BindView(R.id.activity_new_pub_location_text)     EditText txtLocation;
    @BindView(R.id.activity_new_pub_web_text)           EditText txtUrl;

    @BindView(R.id.activity_new_event_image_button)     Button btnRemoveImage;
    @BindView(R.id.activity_new_event_button_cancel)    Button btnCancel;
    @BindView(R.id.activity_new_event_button_create)    Button btnCreate;


}
