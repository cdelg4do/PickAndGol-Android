package io.keepcoding.pickandgol.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;


/**
 * This class stores all the parameters to perform an Event search.
 * (implements the Serializable interface so that it can be passed inside an Intent)
 */
public class EventSearchParams implements Serializable {

    private static Integer DEFAULT_LIMIT = 20;
    private static Integer DEFAULT_RADIUS = 5;


    // Fixed filters (the user cannot set it at any moment)
    private @Nullable Integer limit;

    // General filtering params (set at object creation only)
    private @Nullable String pubId;
    private @Nullable String keyWords;
    private @Nullable String categoryId;
    private @Nullable Integer radiusKm;

    // Specific filtering params (can be set after the object is created)
    private @Nullable Integer offset;
    private @Nullable Double latitude;
    private @Nullable Double longitude;


    /**
     * Creates a new search parameter set.
     * (the null parameters will not be sent, the server will use default values then)
     *
     * @param pubId         id of the pub the events should belong to.
     * @param keyWords      the string to search with in event names and descriptions.
     * @param categoryId    id of the category the events should belong to.
     * @param radiusKm      radius (in km) of the search area from the given coordinates (if any).
     * @param offset        the search should return events only from this result.
     * @param latitude      latitude of the location used as center for the search.
     * @param longitude     longitude of the location used as center for the search.
     */
    public EventSearchParams(@Nullable String pubId, @Nullable String keyWords,
                             @Nullable String categoryId, @Nullable Integer radiusKm,
                             @Nullable Integer offset, @Nullable Double latitude,
                             @Nullable Double longitude) {

        this.pubId = pubId;
        this.keyWords = keyWords;
        this.categoryId = categoryId;
        this.radiusKm = radiusKm;
        this.offset = offset;
        this.limit = DEFAULT_LIMIT;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Creates a new EventSearchParams object with most filters set to null, to use as default.
     * RadiusKm is set to a default value (but it will take effect only if location is added later)
     *
     * @return a new event search filter set to use as default.
     */
    public static @NonNull EventSearchParams buildEmptyParams() {

        return new EventSearchParams(null, null, null, DEFAULT_RADIUS, 0, null, null);
    }


    // Getters:

    public @Nullable Integer getOffset() {
        return offset;
    }

    public @Nullable Integer getLimit() {
        return limit;
    }

    public @Nullable String getPubId() {
        return pubId;
    }

    public @Nullable String getKeyWords() {
        return keyWords;
    }

    public @Nullable String getCategoryId() {
        return categoryId;
    }

    public @Nullable Double getLatitude() {
        return latitude;
    }

    public @Nullable Double getLongitude() {
        return longitude;
    }

    public @Nullable Integer getRadiusKm() {
        return radiusKm;
    }


    // Setters

    public EventSearchParams setOffset(@Nullable Integer offset) {
        this.offset = offset;
        return this;
    }

    public EventSearchParams setCoordinates(@Nullable Double latitude, @Nullable Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }
}
