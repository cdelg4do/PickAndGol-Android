package io.keepcoding.pickandgol.model;

import java.util.Date;
import java.util.List;


/**
 * This class contains the info about an Event managed by the client app
 */
public class Event implements Collectible {

    private String id;
    private String name;
    private Date date;
    private String description;
    private String photoUrl;
    private String category;
    private List<String> pubs;


    public Event(String id, String name, Date date, String description,
                 String photoUrl, String category, List<String> pubs) {

        this.id = id;
        this.name = name;
        this.date = date;
        this.description = description;
        this.photoUrl = photoUrl;
        this.category = category;
        this.pubs = pubs;
    }


    // Getters:

    @Override
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

    public List<String> getPubs() {
        return pubs;
    }
}
