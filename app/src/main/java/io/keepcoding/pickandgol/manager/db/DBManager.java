package io.keepcoding.pickandgol.manager.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.model.User;


/**
 * This interface defines the behavior of a database manager.
 */
public interface DBManager {

    @Nullable Event getEvent(@NonNull final String eventId);
    @Nullable Pub getPub(@NonNull final String pubId);
    @Nullable User getUser(@NonNull final String userId);

    void saveEvent(@NonNull final Event event, final DBManagerListener listener);
    void savePub(@NonNull final Pub pub, final DBManagerListener listener);
    void saveUser(@NonNull final User user, final DBManagerListener listener);

    void removeUser(@NonNull final String userId, final DBManagerListener listener);

    @NonNull EventAggregate getEventsFromPub(@NonNull final String pubId);
    @NonNull PubAggregate getPubsFromEvent(@NonNull final String eventId);
    @NonNull PubAggregate getFavoritesFromUser(@NonNull final String userId);
}
