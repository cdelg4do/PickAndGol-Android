package io.keepcoding.pickandgol.view;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;

import io.keepcoding.pickandgol.model.Pub;

/**
 * This interface defines the behavior of a list of Pub objects.
 */
public interface PubListListener {

    void onPubClicked(Pub pub, int position);
    void onPubListSwipeRefresh(@Nullable SwipeRefreshLayout swipe);
    void onPubListLoadNextPage();
}
