package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * This class is just an RealmUser Id contanier to use in RealmLists of other RealmObjects
 */
public class RealmUserId extends RealmObject {

    @PrimaryKey
    private String id;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmUserId() {
    }

    public RealmUserId(@NonNull final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public RealmUserId setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RealmUserId)) {
            return false;
        }

        RealmUserId param = (RealmUserId) obj;
        return id.equals(param.getId());
    }
}
