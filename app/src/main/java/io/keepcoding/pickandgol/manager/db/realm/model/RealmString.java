package io.keepcoding.pickandgol.manager.db.realm.model;

import io.realm.RealmObject;


/**
 * This class is just a wrapper for a String object.
 * By extending RealmObject it can be used in RealmList (Realm does not support RealmList of String)
 */
public class RealmString extends RealmObject {

    private String stringValue;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmString() {}

    public RealmString(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getValue() {
        return stringValue;
    }

    public RealmString setValue(String stringValue) {
        this.stringValue = stringValue;
        return this;
    }
}
