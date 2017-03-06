package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class RealmPubId extends RealmObject {
    @PrimaryKey @Index
    private String id;

    public RealmPubId() {}

    public RealmPubId(@NonNull final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
