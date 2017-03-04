package io.keepcoding.pickandgol.model.db.realmmanager;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
    @PrimaryKey @Index
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private RealmList<PubId> favorites;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public User setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public RealmList<PubId> getFavorites() {
        return favorites;
    }

    public User setFavorites(RealmList<PubId> favorites) {
        this.favorites = favorites;
        return this;
    }
}
