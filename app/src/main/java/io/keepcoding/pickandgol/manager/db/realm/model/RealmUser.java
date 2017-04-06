package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.model.User;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;


/**
 * This class is the equivalent to the User class, managed by Realm
 */
public class RealmUser extends RealmObject {

    @PrimaryKey @Index
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private RealmList<RealmPubId> favorites;


    // Getters:

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public RealmList<RealmPubId> getFavorites() {
        return favorites;
    }


    // Setters:

    public RealmUser setId(String id) {
        this.id = id;
        return this;
    }

    public RealmUser setName(String name) {
        this.name = name;
        return this;
    }

    public RealmUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public RealmUser setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public RealmUser setFavorites(RealmList<RealmPubId> favorites) {
        this.favorites = favorites;
        return this;
    }


    // Mapping methods (memory <-> database):

    public static @Nullable RealmUser mapFromModel(User user) {

        if (user == null)
            return null;

        RealmList<RealmPubId> favorites = new RealmList<>();
        for (String pubId: user.getFavorites() )
            favorites.add( new RealmPubId(pubId) );

        RealmUser realmUser = new RealmUser()
                .setId(user.getId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setPhotoUrl(user.getPhotoUrl())
                .setFavorites(favorites);

        return realmUser;
    }

    public User mapToModel() {

        List<String> favorites = new ArrayList<>();
        for (RealmPubId pubId: this.getFavorites() )
            favorites.add( pubId.getId() );

        User user = new User( this.getId() )
                .setName( this.getName() )
                .setEmail( this.getEmail() )
                .setPhotoUrl( this.getPhotoUrl() )
                .setFavorites(favorites);

        return user;
    }
}
