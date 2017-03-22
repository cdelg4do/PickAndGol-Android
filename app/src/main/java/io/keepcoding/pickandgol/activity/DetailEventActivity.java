package io.keepcoding.pickandgol.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.model.Event;

import static io.keepcoding.pickandgol.util.Utils.getDateString;
import static io.keepcoding.pickandgol.util.Utils.getTimeString;

public class DetailEventActivity extends EventActivity {
    public static final String EVENT_MODEL_KEY = "EVENT_MODEL_KEY";

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.event_activity_event));
        loadModel();
    }

    private void setWidgetsReadOnly() {
        spnCategory.setEnabled(false);
        txtDate.setOnClickListener(null);
        txtTime.setOnClickListener(null);
        imageHolder.setOnClickListener(null);
        txtName.setFocusable(false);
        spnCategory.setFocusable(false);
        txtDate.setFocusable(false);
        txtTime.setFocusable(false);
        txtDescription.setFocusable(false);
        imageHolder.setFocusable(false);
        LinearLayout buttonsLayout = (LinearLayout) findViewById(R.id.activity_new_event_buttons_layout);
        buttonsLayout.setVisibility(View.INVISIBLE);
        buttonsLayout.removeAllViews();
    }

    @Override
    protected void onCategoriesDownloaded() {
        super.onCategoriesDownloaded();
        updateUI();
        setWidgetsReadOnly();
    }

    private void updateUI() {
        txtName.setText(event.getName());
        selectCategoryInSpinner(event.getCategory());
        txtDate.setText(getDateString(event.getDate()));
        txtTime.setText(getTimeString(event.getDate()));
        txtDescription.setText(event.getDescription());
        Picasso.with(this)
                .load(event.getPhotoUrl())
                .into(imageHolder);
    }

    private void loadModel() {
        Intent i = getIntent();
        event = (Event) i.getSerializableExtra(EVENT_MODEL_KEY);
    }
}