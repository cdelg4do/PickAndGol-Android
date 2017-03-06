package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.keepcoding.pickandgol.model.Event;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * This class is the equivalent to the Event class, managed by Realm
 */
public class RealmEvent extends RealmObject {

    @PrimaryKey
    String id;
    String name;
    Date date;
    String description;
    String photoUrl;
    String category;
    RealmList<RealmPubId> pubs;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmEvent() {
    }

    public RealmEvent(String id, String name, Date date, String description,
                      String photoUrl, String category, RealmList<RealmPubId> pubs) {

        this.id = id;
        this.name = name;
        this.date = date;
        this.description = description;
        this.photoUrl = photoUrl;
        this.category = category;
        this.pubs = pubs;
    }


    // Getters:

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getCategory() {
        return category;
    }

    public RealmList<RealmPubId> getPubs() {
        return pubs;
    }


    // Setters:

    public RealmEvent setId(String id) {
        this.id = id;
        return this;
    }

    public RealmEvent setName(String name) {
        this.name = name;
        return this;
    }

    public RealmEvent setDate(Date date) {
        this.date = date;
        return this;
    }

    public RealmEvent setDescription(String description) {
        this.description = description;
        return this;
    }

    public RealmEvent setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public RealmEvent setCategory(String category) {
        this.category = category;
        return this;
    }

    public RealmEvent setPubs(RealmList<RealmPubId> pubs) {
        this.pubs = pubs;
        return this;
    }


    // Mapping methods (memory <-> database):

    public static @Nullable RealmEvent mapFromModel(Event event) {

        if (event == null)
            return null;

        RealmList<RealmPubId> pubs = new RealmList<>();
        for (String pubId: event.getPubs() )
            pubs.add( new RealmPubId(pubId) );

        RealmEvent realmEvent = new RealmEvent()
                .setId( event.getId() )
                .setName( event.getName() )
                .setDate( event.getDate() )
                .setDescription( event.getDescription() )
                .setPhotoUrl( event.getPhotoUrl() )
                .setCategory( event.getCategory() )
                .setPubs(pubs);

        return realmEvent;
    }

    public Event mapToModel() {

        List<String> pubs = new ArrayList<>();
        for (RealmPubId pubId : this.getPubs() )
            pubs.add( pubId.getId() );

        Event event = new Event(this.getId(),
                                this.getName(),
                                this.getDate(),
                                this.getDescription(),
                                this.getPhotoUrl(),
                                this.getCategory(),
                                pubs
        );

        return event;
    }
}
