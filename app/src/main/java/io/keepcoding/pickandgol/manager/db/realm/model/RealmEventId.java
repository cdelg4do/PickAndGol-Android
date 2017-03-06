package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * This class is just an RealmEvent Id contanier to use in RealmLists of other RealmObjects
 */
public class RealmEventId extends RealmObject {

    @PrimaryKey
    private String id;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmEventId() {
    }

    public RealmEventId(@NonNull final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public RealmEventId setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RealmEventId)) {
            return false;
        }

        RealmEventId param = (RealmEventId) obj;
        return id.equals(param.getId());
    }
}
