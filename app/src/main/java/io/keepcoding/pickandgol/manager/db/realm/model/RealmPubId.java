package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;


/**
 * This class is just a RealmPub Id container to use in RealmLists of other RealmObjects
 */
public class RealmPubId extends RealmObject {

    @PrimaryKey @Index
    private String id;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmPubId() {
    }

    public RealmPubId(@NonNull final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public RealmPubId setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RealmPubId)) {
            return false;
        }

        RealmPubId param = (RealmPubId) obj;
        return id.equals(param.getId());
    }
}
