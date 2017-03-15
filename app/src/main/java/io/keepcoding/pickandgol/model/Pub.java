package io.keepcoding.pickandgol.model;

import java.io.Serializable;
import java.util.List;


/**
 * This class contains the info about a Pub managed by the client app.
 * (implements the Serializable interface so that it can be passed inside an Intent)
 */
public class Pub implements Collectible, Serializable {

    private String id;
    private String name;
    private boolean hasLocation;
    private double latitude;
    private double longitude;
    private String url;
    private String owner;
    private List<String> events;


    // Full constructor
    public Pub(String id, String name, boolean hasLocation, double latitude, double longitude,
                String url, String owner, List<String> events) {

        this.id = id;
        this.name = name;
        this.hasLocation = hasLocation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.url = url;
        this.owner = owner;
        this.events = events;
    }

    // Constructor for Pub with a location
    public Pub(String id, String name, double latitude, double longitude,
                    String url, String owner, List<String> events) {

        this(id, name, true, latitude, longitude, url, owner, events);
    }

    // Constructor for Pub without location
    public Pub(String id, String name, String url, String owner, List<String> events) {

        this(id, name, false, 0, 0, url, owner, events);
    }


    // Getters:

    @Override
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

    public String getOwner() {
        return owner;
    }

    public List<String> getEvents() {
        return events;
    }


    // Setters:

    public Pub setId(String id) {
        this.id = id;
        return this;
    }

    public Pub setName(String name) {
        this.name = name;
        return this;
    }

    public Pub setHasLocation(boolean hasLocation) {
        this.hasLocation = hasLocation;
        return this;
    }

    public Pub setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Pub setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Pub setUrl(String url) {
        this.url = url;
        return this;
    }

    public Pub setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Pub setEvents(List<String> events) {
        this.events = events;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Pub)) {
            return false;
        }

        Pub pub = (Pub) obj;

        return id.equals(pub.id) && name.equals(pub.name) && hasLocation == pub.hasLocation
                && latitude == pub.latitude && longitude == pub.longitude && url.equals(pub.url)
                && owner.equals(pub.owner) && events.equals(pub.events);
    }
}
