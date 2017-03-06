package io.keepcoding.pickandgol.manager.db.realm;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmEvent;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmEventId;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmPub;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmPubId;
import io.keepcoding.pickandgol.manager.db.realm.model.RealmUser;
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
    private static RealmDBManager instance;

    private Realm realm;

    private RealmDBManager() {
        realm = Realm.getDefaultInstance();
    }

    public static RealmDBManager getDefaultInstance() {
        if (instance == null)
            instance = new RealmDBManager();

        return instance;
    }


    // DBManager methods:

    @Override
    public @Nullable Event getEvent(@NonNull final String eventId) {
        RealmEvent realmEvent = realm.where(RealmEvent.class).equalTo("id", eventId).findFirst();
        if (realmEvent == null) {
            return null;
        }

        return realmEvent.mapToModel();
    }

    @Override
    public @Nullable Pub getPub(@NonNull final String pubId) {
        RealmPub realmPub = realm.where(RealmPub.class).equalTo("id", pubId).findFirst();
        if (realmPub == null) {
            return null;
        }

        return realmPub.mapToModel();
    }

    @Override
    public @Nullable User getUser(@NonNull final String userId) {
        RealmUser realmUser = realm.where(RealmUser.class).equalTo("id", userId).findFirst();
        if (realmUser == null) {
            return null;
        }

        return realmUser.mapToModel();
    }

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

    @Override
    public @NonNull EventAggregate getEventsFromPub(@NonNull String pubId) {

        EventAggregate events = EventAggregate.buildEmpty();

        RealmPub realmPub = realm.where(RealmPub.class).equalTo("id", pubId).findFirst();
        if (realmPub != null) {

            RealmQuery<RealmEvent> query = realm.where(RealmEvent.class).equalTo("id", "");
            for (RealmEventId eventId : realmPub.getEvents())
                query = query.or().equalTo("id", eventId.getId() );

            RealmResults<RealmEvent> realmEvents = query.findAll();
            if (realmEvents.size() > 0) {

                List<Event> eventList = new ArrayList<>();
                for (RealmEvent event : realmEvents)
                    eventList.add( event.mapToModel() );

                events.setAll(eventList);
            }
        }

        return events;
    }

    @Override
    public @NonNull PubAggregate getPubsFromEvent(@NonNull String eventId) {

        PubAggregate pubs = PubAggregate.buildEmpty();

        RealmEvent realmEvent = realm.where(RealmEvent.class).equalTo("id", eventId).findFirst();
        if (realmEvent != null) {

            RealmQuery<RealmPub> query = realm.where(RealmPub.class).equalTo("id", "");
            for (RealmPubId pubId : realmEvent.getPubs())
                query = query.or().equalTo("id", pubId.getId() );

            RealmResults<RealmPub> realmPubs = query.findAll();
            if (realmPubs.size() > 0) {

                List<Pub> pubList = new ArrayList<>();
                for (RealmPub pub : realmPubs)
                    pubList.add( pub.mapToModel() );

                pubs.setAll(pubList);
            }
        }

        return pubs;
    }

    @Override
    public @NonNull PubAggregate getFavoritesFromUser(@NonNull String userId) {

        PubAggregate pubs = PubAggregate.buildEmpty();

        RealmUser realmUser = realm.where(RealmUser.class).equalTo("id", userId).findFirst();
        if (realmUser != null) {

            RealmQuery<RealmPub> query = realm.where(RealmPub.class).equalTo("id", "");
            for (RealmPubId pubId : realmUser.getFavorites())
                query = query.or().equalTo("id", pubId.getId() );

            RealmResults<RealmPub> realmPubs = query.findAll();
            if (realmPubs.size() > 0) {

                List<Pub> pubList = new ArrayList<>();
                for (RealmPub pub : realmPubs)
                    pubList.add( pub.mapToModel() );

                pubs.setAll(pubList);
            }
        }

        return pubs;
    }

}
