package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a Pub Detail JSON response.
 */
public class PubDetailResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private PubDetailData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull PubDetailData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class PubDetailData implements ParsedData {

        // This field exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String description;

        // These fields only in case of 'OK' result
        @SerializedName("_id")          private String id;
        @SerializedName("name")         private String name;
        @SerializedName("location")     private Location location;
        @SerializedName("url")          private String url;    // Optional field (may come as "")
        @SerializedName("owner")        private String owner;
        @SerializedName("events")       private List<String> events;
        @SerializedName("photos")       private List<String> photos;


        public @Nullable String getErrorCode() {
            return errorCode;
        }

        public @Nullable String getErrorDescription() {
            return description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Location getLocation() {
            return location;
        }

        public String getUrl() {
            return url;
        }

        public String getOwner() {
            return owner;
        }

        public List<String> getEvents() {

            if (events == null)
                events = new ArrayList<>();

            return events;
        }

        public List<String> getPhotos() {

            if (photos == null)
                photos = new ArrayList<>();

            return photos;
        }


        /**
         * This class represents the 'location' field of a pub.
         */
        public class Location {

            @SerializedName("coordinates")       private List<Double> coordinates;

            public List<Double> getCoordinates() {
                return coordinates;
            }
        }
    }


    // Outputs the response data as a String (for debugging purposes)
    public String debugString() {

        StringBuilder str = new StringBuilder();

        str.append("result: "+ Utils.safeString(result) +"\n");

        if ( !resultIsOK() ) {
            str.append("code: "+ Utils.safeString(data.errorCode) +"\n");
            str.append("description: "+ Utils.safeString(data.description) +"\n");
        }

        else {
            str.append("id: "+ Utils.safeString(data.getId()) +"\n");
            str.append("name: "+ Utils.safeString(data.getName()) +"\n");

            if ( data.getLocation() != null &&
                 data.getLocation().getCoordinates() != null &&
                 data.getLocation().getCoordinates().size() == 2 ) {

                Double latitude = data.getLocation().getCoordinates().get(0);
                Double longitude = data.getLocation().getCoordinates().get(1);

                str.append("latitude: "+ latitude +"\n");
                str.append("longitude: "+ longitude +"\n");
            }
            else {
                str.append("latitude: <INVALID_DATA>\n");
                str.append("longitude: <INVALID_DATA>\n");
            }

            str.append("url: "+ Utils.safeString(data.getUrl()) +"\n");
            str.append("owner: "+ Utils.safeString(data.getOwner()) +"\n");

            if (data.getEvents() == null || data.getEvents().size() == 0)
                str.append("events: [ ] \n");
            else {
                str.append("events: [ ");

                for (String eventId : data.getEvents())
                    str.append(eventId + ", ");

                str.setLength(str.length() - 2);    // to remove the last ', '
                str.append(" ] \n");
            }

            if (data.getPhotos() == null || data.getPhotos().size() == 0)
                str.append("photos: [ ] \n");
            else {
                str.append("photos: [ ");

                for (String photoUrl : data.getPhotos())
                    str.append(photoUrl + ", ");

                str.setLength(str.length() - 2);    // to remove the last ', '
                str.append(" ] \n");
            }
        }

        return str.toString();
    }
}
