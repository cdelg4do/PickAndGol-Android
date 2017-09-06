package io.keepcoding.pickandgol.manager.db;

import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.model.Category;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.User;


/**
 * This interface defines the behavior of a database manager.
 *
 * All database access methods (including queries) are asynchronous,
 * so that all operations can be safely invoked from the main thread.
 */
public interface DBManager {

    // Database query methods
    void getEvent(@NonNull final String eventId, final DBManagerListener listener);
    void getPub(@NonNull final String pubId, final DBManagerListener listener);
    void getUser(@NonNull final String userId, final DBManagerListener listener);
    void getCategory(@NonNull final String categoryId, final DBManagerListener listener);
    void getAllCategories(final DBManagerListener listener);

    void getEventsFromPub(@NonNull final String pubId, final DBManagerListener listener);
    void getPubsFromEvent(@NonNull final String eventId, final DBManagerListener listener);
    void getFavoritesFromUser(@NonNull final String userId, final DBManagerListener listener);

    // Database save methods
    void saveEvent(@NonNull final Event event, final DBManagerListener listener);
    void savePub(@NonNull final Pub pub, final DBManagerListener listener);
    void saveUser(@NonNull final User user, final DBManagerListener listener);
    void saveCategory(@NonNull final Category category, final DBManagerListener listener);
    void saveCategories(@NonNull final CategoryAggregate categories, final DBManagerListener listener);

    // Database remove methods
    void removeUser(@NonNull final String userId, final DBManagerListener listener);
    void removePub(@NonNull final String pubId, final DBManagerListener listener);
    void removeEvent(@NonNull final String eventId, final DBManagerListener listener);
    void removeCategory(@NonNull final String categoryId, final DBManagerListener listener);
    void removeAllCategories(final DBManagerListener listener);
}
