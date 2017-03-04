package io.keepcoding.pickandgol.model.db.realmmanager;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.model.db.DBManager;
import io.realm.Realm;
import io.realm.RealmList;

public class DBRealmManager implements DBManager {
    private static DBRealmManager instance;

    private WeakReference<Context> context;
    private Realm realm;

    private DBRealmManager(final Context context) {
        this.context = new WeakReference<>(context);
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }

    public static DBRealmManager getDefaultInstance(final Context context) {
        if (instance == null) {
            instance = new DBRealmManager(context);
        }

        return instance;
    }

    @Override
    public void saveUser(@NonNull final io.keepcoding.pickandgol.model.User user) {
        User realmUser = mapUserToRealmUser(user);
        realm.beginTransaction();
        try {
            realm.copyToRealmOrUpdate(realmUser);
            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
            throw new RuntimeException(e);
        }
    }

    @Override
    public io.keepcoding.pickandgol.model.User getUser(@NonNull final String userId) {
        User realmUser = realm.where(User.class).equalTo("id", userId).findFirst();
        return mapRealmUserToUser(realmUser);
    }

    private io.keepcoding.pickandgol.model.User mapRealmUserToUser(User realmUser) {
        List<String> favorites = new ArrayList<>();
        for (PubId pub: realmUser.getFavorites()) {
            favorites.add(pub.getId());
        }

        io.keepcoding.pickandgol.model.User user = new io.keepcoding.pickandgol.model.User(realmUser.getId());
        user.setName(realmUser.getName())
                .setEmail(realmUser.getEmail())
                .setPhotoUrl(realmUser.getPhotoUrl())
                .setFavorites(favorites);

        return user;
    }

    private User mapUserToRealmUser(io.keepcoding.pickandgol.model.User user) {
        RealmList<PubId> favorites = new RealmList<>();

        for (String fav: user.getFavorites()) {
            favorites.add(new PubId(fav));
        }

        User realmUser = new User()
                .setId(user.getId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setPhotoUrl(user.getPhotoUrl())
                .setFavorites(favorites);

        return realmUser;
    }
}
