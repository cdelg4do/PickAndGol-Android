package io.keepcoding.pickandgol.manager.db.realm.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.model.Pub;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * This class is the equivalent to the Pub class, managed by Realm
 */
public class RealmPub extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private boolean hasLocation;
    private double latitude;
    private double longitude;
    private String url;
    private RealmUserId owner;
    private RealmList<RealmEventId> events;

    // An empty public constructor is mandatory for Realm when using customized constructors
    public RealmPub() {
    }

    // Full constructor is private, use the public constructors instead
    private RealmPub(String id, String name, boolean hasLocation, double latitude, double longitude,
                     String url, RealmUserId owner, RealmList<RealmEventId> events) {

        this.id = id;
        this.name = name;
        this.hasLocation = hasLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.owner = owner;
        this.events = events;
    }

    // Constructor for RealmPub with a location
    public RealmPub(String id, String name, double latitude, double longitude,
                    String url, RealmUserId owner, RealmList<RealmEventId> events) {

        this(id, name, true, latitude, longitude, url, owner, events);
    }

    // Constructor for RealmPub without location
    public RealmPub(String id, String name, String url, RealmUserId owner, RealmList<RealmEventId> events) {

        this(id, name, false, 0, 0, url, owner, events);
    }


    // Getters:

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasLocation() {
        return hasLocation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getUrl() {
        return url;
    }

    public RealmUserId getOwner() {
        return owner;
    }

    public RealmList<RealmEventId> getEvents() {
        return events;
    }


    // Setters:

    public RealmPub setId(String id) {
        this.id = id;
        return this;
    }

    public RealmPub setName(String name) {
        this.name = name;
        return this;
    }

    public RealmPub setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
        return this;
    }

    public RealmPub setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public RealmPub setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public RealmPub setUrl(String url) {
        this.url = url;
        return this;
    }

    public RealmPub setOwner(RealmUserId owner) {
        this.owner = owner;
        return this;
    }

    public RealmPub setEvents(RealmList<RealmEventId> events) {
        this.events = events;
        return this;
    }


    // Mapping methods (memory <-> database):

    public static @Nullable RealmPub mapFromModel(Pub pub) {

        if (pub == null)
            return null;

        RealmList<RealmEventId> events = new RealmList<>();
        for (String eventId: pub.getEvents() )
            events.add( new RealmEventId(eventId) );

        RealmUserId owner = new RealmUserId( pub.getOwner() );

        RealmPub realmPub = new RealmPub()
                .setId( pub.getId() )
                .setName( pub.getName() )
                .setHasLocation( pub.hasLocation() )
                .setLatitude( pub.getLatitude() )
                .setLongitude( pub.getLongitude() )
                .setUrl( pub.getUrl() )
                .setOwner(owner)
                .setEvents(events);

        return realmPub;
    }

    public Pub mapToModel() {

        List<String> events = new ArrayList<>();
        for (RealmEventId eventId : this.getEvents() )
            events.add( eventId.getId() );

        Pub pub = new Pub(this.getId(),
                          this.getName(),
                          this.hasLocation(),
                          this.getLatitude(),
                          this.getLongitude(),
                          this.getUrl(),
                          this.getOwner().getId(),
                          events
        );

        return pub;
    }
}
