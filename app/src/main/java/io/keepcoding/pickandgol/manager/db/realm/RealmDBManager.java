package io.keepcoding.pickandgol.manager.db.realm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmCategory;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmEvent;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmEventId;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmPub;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmPubId;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmUser;
import io.keepcoding.pickandgol.model.Category;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.model.User;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


/**
 * This class is a DBManager implementation using Realm.
 */
public class RealmDBManager implements DBManager {

    private static RealmDBManager instance;     // The DBManager is a singleton
    private Realm realm;

    // Constructor is private, use getDBManager() instead
    private RealmDBManager() {
        realm = Realm.getDefaultInstance();
    }

    // Gets a reference to the singleton
    public static DBManager getDBManager() {
        if (instance == null)
            instance = new RealmDBManager();

        return instance;
    }


    // DBManager interface methods:

    // Gets the event from the database for a given event id
    @Override
    public void getEvent(@NonNull final String eventId, final DBManagerListener listener) {

        // Use a list to store the item instead of an Event (must be declared final)
        final ArrayList<Event> eventList = new ArrayList<>();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        RealmEvent realmEvent = realm.where(RealmEvent.class)
                                                     .equalTo("id", eventId)
                                                     .findFirst();

                        if (realmEvent != null)
                            eventList.add( realmEvent.mapToModel() );
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            if (eventList.size() == 0)  listener.onSuccess(null);
                            else                        listener.onSuccess(eventList.get(0));
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Gets the pub from the database for a given pub id
    @Override
    public void getPub(@NonNull final String pubId, final DBManagerListener listener) {

        final ArrayList<Pub> pubList = new ArrayList<>();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        RealmPub realmPub = realm.where(RealmPub.class)
                                                 .equalTo("id", pubId)
                                                 .findFirst();

                        if (realmPub != null)
                            pubList.add( realmPub.mapToModel() );
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            if (pubList.size() == 0)    listener.onSuccess(null);
                            else                        listener.onSuccess(pubList.get(0));
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Gets the user from the database for a given user id
    @Override
    public void getUser(@NonNull final String userId, final DBManagerListener listener) {

        final ArrayList<User> userList = new ArrayList<>();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        RealmUser realmUser = realm.where(RealmUser.class)
                                                   .equalTo("id", userId)
                                                   .findFirst();

                        if (realmUser != null)
                            userList.add( realmUser.mapToModel() );
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            if (userList.size() == 0)   listener.onSuccess(null);
                            else                        listener.onSuccess(userList.get(0));
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Gets the category from the database for a given category id
    @Override
    public void getCategory(@NonNull final String categoryId, final DBManagerListener listener) {

        final ArrayList<Category> categoryList = new ArrayList<>();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        RealmCategory realmCategory = realm.where(RealmCategory.class)
                                                           .equalTo("id", categoryId)
                                                           .findFirst();

                        if (realmCategory != null)
                            categoryList.add( realmCategory.mapToModel() );
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            if (categoryList.size() == 0)   listener.onSuccess(null);
                            else                            listener.onSuccess(categoryList.get(0));
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Gets all existing categories in the database, alphabetically
    @Override
    public void getAllCategories(final DBManagerListener listener) {

        final CategoryAggregate categories = CategoryAggregate.buildEmpty();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<RealmCategory> res = realm.where(RealmCategory.class).findAllSorted("name");

                        if (res != null) {

                            List<Category> categoryList = new ArrayList<>();
                            for (RealmCategory category : res)
                                categoryList.add( category.mapToModel() );

                            categories.setAll(categoryList);
                        }
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(categories);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }


    // Gets all events in the pub with the given id
    @Override
    public void getEventsFromPub(@NonNull final String pubId, final DBManagerListener listener) {

        final EventAggregate events = EventAggregate.buildEmpty();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        // First, get the Pub we are looking for
                        RealmPub realmPub = realm.where(RealmPub.class)
                                .equalTo("id", pubId)
                                .findFirst();

                        if (realmPub != null) {

                            // Next, query for all events in the pub's event list
                            // (events whose id="" OR id=id1 OR id=id2 OR ...)
                            RealmQuery<RealmEvent> query = realm.where(RealmEvent.class).equalTo("id", "");
                            for (RealmEventId eventId : realmPub.getEvents())
                                query = query.or().equalTo("id", eventId.getId() );

                            RealmResults<RealmEvent> realmEvents = query.findAll();

                            if (realmEvents.size() > 0) {

                                // Last, store all the matches (if any) in the EventAggregate object
                                List<Event> eventList = new ArrayList<>();
                                for (RealmEvent event : realmEvents)
                                    eventList.add( event.mapToModel() );

                                events.setAll(eventList);
                            }
                        }
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(events);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Gets all pubs for the event with the given id
    @Override
    public void getPubsFromEvent(@NonNull final String eventId, final DBManagerListener listener) {

        final PubAggregate pubs = PubAggregate.buildEmpty();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        // First, get the Event we are looking for
                        RealmEvent realmEvent = realm.where(RealmEvent.class)
                                .equalTo("id", eventId)
                                .findFirst();

                        if (realmEvent != null) {

                            // Next, query for all pubs in the event's pub list
                            // (pubs whose id="" OR id=id1 OR id=id2 OR ...)
                            RealmQuery<RealmPub> query = realm.where(RealmPub.class).equalTo("id", "");
                            for (RealmPubId pubId : realmEvent.getPubs())
                                query = query.or().equalTo("id", pubId.getId() );

                            RealmResults<RealmPub> realmPubs = query.findAll();

                            if (realmPubs.size() > 0) {

                                // Last, store all the matches (if any) in the PubAggregate object
                                List<Pub> pubList = new ArrayList<>();
                                for (RealmPub pub : realmPubs)
                                    pubList.add( pub.mapToModel() );

                                pubs.setAll(pubList);
                            }
                        }
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(pubs);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Gets all favorite pubs for the user with the given id
    @Override
    public void getFavoritesFromUser(@NonNull final String userId, final DBManagerListener listener) {

        final PubAggregate favorites = PubAggregate.buildEmpty();

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        // First, get the User we are looking for
                        RealmUser realmUser = realm.where(RealmUser.class)
                                .equalTo("id", userId)
                                .findFirst();

                        if (realmUser != null) {

                            // Next, query for all pubs in the user's favorites
                            // (pubs whose id="" OR id=id1 OR id=id2 OR ...)
                            RealmQuery<RealmPub> query = realm.where(RealmPub.class).equalTo("id", "");
                            for (RealmPubId pubId : realmUser.getFavorites())
                                query = query.or().equalTo("id", pubId.getId() );

                            RealmResults<RealmPub> realmPubs = query.findAll();

                            if (realmPubs.size() > 0) {

                                // Last, store all the matches (if any) in the PubAggregate object
                                List<Pub> pubList = new ArrayList<>();
                                for (RealmPub pub : realmPubs)
                                    pubList.add( pub.mapToModel() );

                                favorites.setAll(pubList);
                            }
                        }
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(favorites);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }


    // Saves the given event to the database, then calls the passed listener
    @Override
    public void saveEvent(@NonNull final Event event, final DBManagerListener listener) {

        realm.executeTransactionAsync(

                new Realm.Transaction() {
                    @Override
                    public void execute(Realm backgroundRealm) {
                        RealmEvent realmEvent = RealmEvent.mapFromModel(event);
                        backgroundRealm.copyToRealmOrUpdate(realmEvent);
                    }
                },

                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null)
                            listener.onSuccess(null);
                    }
                },

                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null)
                            listener.onError(error);
                    }
                }
        );
    }

    // Saves the given pub to the database, then calls the passed listener
    @Override
    public void savePub(@NonNull final Pub pub, final DBManagerListener listener) {

        realm.executeTransactionAsync(

                new Realm.Transaction() {
                    @Override
                    public void execute(Realm backgroundRealm) {
                        RealmPub realmPub = RealmPub.mapFromModel(pub);
                        backgroundRealm.copyToRealmOrUpdate(realmPub);
                    }
                },

                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null)
                            listener.onSuccess(null);
                    }
                },

                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null)
                            listener.onError(error);
                    }
                }
        );
    }

    // Saves the given user to the database, then calls the passed listener
    @Override
    public void saveUser(@NonNull final User user, final DBManagerListener listener) {

        realm.executeTransactionAsync(

                new Realm.Transaction() {
                    @Override
                    public void execute(Realm backgroundRealm) {
                        RealmUser realmUser = RealmUser.mapFromModel(user);
                        backgroundRealm.copyToRealmOrUpdate(realmUser);
                    }
                },

                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null)
                            listener.onSuccess(null);
                    }
                },

                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null)
                            listener.onError(error);
                    }
                }
        );
    }

    // Saves the given category to the database, then calls the passed listener
    @Override
    public void saveCategory(@NonNull final Category category, final DBManagerListener listener) {

        realm.executeTransactionAsync(

                new Realm.Transaction() {
                    @Override
                    public void execute(Realm backgroundRealm) {
                        RealmCategory realmCategory = RealmCategory.mapFromModel(category);
                        backgroundRealm.copyToRealmOrUpdate(realmCategory);
                    }
                },

                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null)
                            listener.onSuccess(null);
                    }
                },

                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null)
                            listener.onError(error);
                    }
                }
        );
    }

    // Stores all the categories contained in the aggregate into the database, then calls the listener.
    // (if one fails, the operation is interrupted and the remaining categories will not be saved)
    @Override
    public void saveCategories(@NonNull final CategoryAggregate categories, final DBManagerListener listener) {

        // Counter for remaining categories (use an array instead an int because it must be final)
        final int[] remaining = new int[1];
        remaining[0] = categories.size();

        for (Category category : categories.getAll()) {

            saveCategory(category, new DBManagerListener() {

                @Override
                public void onError(Throwable e) {
                    listener.onError(e);
                    return;
                }

                @Override
                public void onSuccess(@Nullable Object result) {
                    remaining[0] -= 1;

                    // Only return control to the listener when no more categories left to save
                    if (remaining[0] == 0)
                        listener.onSuccess(null);
                }
            });
        }
    }


    // Removes the user for the given id from the database, then calls the passed listener
    @Override
    public void removeUser(@NonNull final String userId, final DBManagerListener listener) {
        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<RealmUser> results = realm.where(RealmUser.class).equalTo("id", userId).findAll();
                        results.deleteAllFromRealm();
                    }
                },

                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(null);
                        }
                    }
                },

                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );

    }

    // Removes the pub for the given id from the database, then calls the passed listener
    @Override
    public void removePub(@NonNull final String pubId, final DBManagerListener listener) {
        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<RealmPub> results = realm.where(RealmPub.class).equalTo("id", pubId).findAll();
                        results.deleteAllFromRealm();
                    }
                },

                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(null);
                        }
                    }
                },

                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Removes the event for the given id from the database, then calls the passed listener
    @Override
    public void removeEvent(@NonNull final String eventId, final DBManagerListener listener) {
        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<RealmEvent> results = realm.where(RealmEvent.class).equalTo("id", eventId).findAll();
                        results.deleteAllFromRealm();
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(null);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Removes the category for the given id from the database, then calls the passed listener
    @Override
    public void removeCategory(@NonNull final String categoryId, final DBManagerListener listener) {

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmResults<RealmCategory> results = realm.where(RealmCategory.class).equalTo("id", categoryId).findAll();
                        results.deleteAllFromRealm();
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(null);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

    // Removes all categories from the local database
    @Override
    public void removeAllCategories(final DBManagerListener listener) {

        realm.executeTransactionAsync(
                new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.delete(RealmCategory.class);
                    }
                },
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        if (listener != null) {
                            listener.onSuccess(null);
                        }
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );
    }

}
