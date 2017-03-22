package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Date;

import io.keepcoding.pickandgol.interactor.CreateEventInteractor;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.util.ErrorSuccessListener;
import io.keepcoding.pickandgol.util.Utils;

/**
 * This class represents the activity with the New Event form.
 */
public class NewEventActivity extends EventActivity {
    public final static String NEW_EVENT_KEY = "NEW_EVENT_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCreateButton();
    }

    protected void setupCreateButton() {
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event validatedData = validateFormData();

                if (validatedData != null) {
                    uploadImageIfNecessaryThenRegisterNewEvent(validatedData);
                }
            }
        });
    }

    private void uploadImageIfNecessaryThenRegisterNewEvent(final Event event) {

        if (event.getPhotoUrl() == null) {
            registerNewEvent(event);
            return;
        }

        String sourcePath = getImageSourcePath();
        String remoteFileName = event.getPhotoUrl();

        // First, attempt to upload to the cloud the selected image
        doUploadImage(sourcePath, remoteFileName, new ErrorSuccessListener() {
            @Override
            public void onError(@Nullable Object result) {
                Utils.simpleDialog(NewEventActivity.this, "Image Upload error",
                        "The selected image could not be uploaded. Please try again.");
            }

            @Override
            public void onSuccess(@Nullable Object result) {
                registerNewEvent(event);
            }
        });
    }

    private void registerNewEvent(Event event) {
        SessionManager sm = SessionManager.getInstance(this);
        ImageManager im = ImageManager.getInstance(this);
        String name = event.getName();
        Date date = event.getDate();
        String pubId = event.getPubs().get(0);
        String categoryId = event.getCategory();
        String description = event.getDescription();
        String photoUrl = im.getRemoteImageUrl( event.getPhotoUrl() );
        String token = sm.getSessionToken();

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Registering event...");
        pDialog.show();

        new CreateEventInteractor().execute(this, name, date, pubId, categoryId,
                description, photoUrl, token,
                new CreateEventInteractor.CreateEventInteractorListener() {

                    @Override
                    public void onCreateEventFail(Exception e) {
                        pDialog.dismiss();

                        Utils.simpleDialog(NewEventActivity.this, "Error registering new event", e.getMessage());
                    }

                    @Override
                    public void onCreateEventSuccess(final Event createdEvent) {
                        pDialog.dismiss();

                        Utils.simpleDialog(NewEventActivity.this,
                                "New Event",
                                "The event '"+ createdEvent.getName() +"' has been created.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finishActivity(createdEvent, null);
                                    }
                                });
                    }
                });
    }
}
