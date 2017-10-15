package io.keepcoding.pickandgol.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;


/**
 * This class stores all the parameters to perform a Pub search.
 * (implements the Serializable interface so that it can be passed inside an Intent)
 */
public class PubSearchParams implements Serializable {

    private static Integer DEFAULT_LIMIT = 20;
    private static Integer DEFAULT_RADIUS = 5;


    // Fixed filters (the user cannot set it at any moment)
    private @Nullable Integer limit;

    // General filtering params (set at object creation only)
    private @Nullable String sort;
    private @Nullable String keyWords;
    private boolean useCurrentLocation; // used only to restore previous search settings
    private @Nullable Integer radiusKm;

    // Specific filtering params (can be set after the object is created)
    private @Nullable Integer offset;
    private @Nullable Double latitude;
    private @Nullable Double longitude;
    private @Nullable String eventId;   // Used only when filtering pubs by event id


    /**
     * Creates a new search parameter set.
     * (the null parameters will not be sent, the server will use default values then)
     *
     * @param sort          field name used to sort the results.
     * @param keyWords      the string to search with in pub names.
     * @param radiusKm      radius (in km) of the search area from the given coordinates (if any).
     * @param offset        the search should return pubs only from this result.
     * @param latitude      latitude of the location used as center for the search.
     * @param longitude     longitude of the location used as center for the search.
     * @param useCurrentLocation    tells if the device current location must be used as search center
     *                              (this param is used only when showing on screen the settings of the last search)
     */
    public PubSearchParams(@Nullable String sort, @Nullable String keyWords,
                           @Nullable Integer radiusKm, @Nullable Integer offset,
                           @Nullable Double latitude, @Nullable Double longitude,
                           boolean useCurrentLocation) {

        this.sort = sort;
        this.keyWords = keyWords;
        this.radiusKm = radiusKm;
        this.offset = offset;
        this.limit = DEFAULT_LIMIT;
        this.latitude = latitude;
        this.longitude = longitude;
        this.useCurrentLocation = useCurrentLocation;
        this.eventId = null;
    }

    /**
     * Creates a new PubSearchParams object with most filters set to null, to use as default.
     * RadiusKm is set to a default value (but it will take effect only if location is added later)
     *
     * @return a new pub search filter set to use as default.
     */
    public static @NonNull PubSearchParams buildEmptyParams() {

        return new PubSearchParams(null, null, DEFAULT_RADIUS, 0, null, null, true);
    }


    // Getters:

    public @Nullable Integer getOffset() {
        return offset;
    }

    public @Nullable Integer getLimit() {
        return limit;
    }

    public @Nullable String getSort() {
        return sort;
    }

    public @Nullable String getKeyWords() {
        return keyWords;
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

    public boolean isUsingCurrentLocation() {
        return useCurrentLocation;
    }

    public @Nullable String getEventId() {
        return eventId;
    }

    // Setters

    public PubSearchParams setOffset(@Nullable Integer offset) {
        this.offset = offset;
        return this;
    }

    public PubSearchParams setCoordinates(@Nullable Double latitude, @Nullable Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public PubSearchParams setEventId(@Nullable String eventId) {
        this.eventId = eventId;
        return this;
    }
}
