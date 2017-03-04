package io.keepcoding.pickandgol.model.db.realmmanager;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PubId extends RealmObject {
    @PrimaryKey @Index
    private String id;

    public PubId() {}

    public PubId(@NonNull final String id) {
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

        if (!(obj instanceof PubId)) {
            return false;
        }

        PubId param = (PubId) obj;
        return id.equals(param.getId());
    }
}
