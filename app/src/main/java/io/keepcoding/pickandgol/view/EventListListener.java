package io.keepcoding.pickandgol.view;

import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;

import io.keepcoding.pickandgol.model.Event;


/**
 * This interface defines the behavior of a list of Event objects.
 */
public interface EventListListener {

    void onEventClicked(Event event, int position);
    void onEventListSwipeRefresh(@Nullable SwipeRefreshLayout swipe);
    void onEventListLoadNextPage();
}
