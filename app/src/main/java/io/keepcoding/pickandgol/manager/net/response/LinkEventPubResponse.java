package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a Link-Event-Pub JSON response.
 */
public class LinkEventPubResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private LinkEventPubData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull LinkEventPubData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class LinkEventPubData implements ParsedData {

        // This field exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String description;

        // These fields only in case of 'OK' result
        @SerializedName("pub")          private PubDetailResponse.PubDetailData pub;
        @SerializedName("event")        private EventDetailResponse.EventDetailData event;


        public @Nullable String getErrorCode() {
            return errorCode;
        }

        public @Nullable String getErrorDescription() {
            return description;
        }

        public PubDetailResponse.PubDetailData getPub() {
            return pub;
        }

        public EventDetailResponse.EventDetailData getEvent() {
            return event;
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
            if (data.pub == null)
                str.append("pub: null\n");
            else {
                str.append("pub: \n");

                str.append("\tid: "+ Utils.safeString(data.pub.getId()) +"\n");
                str.append("\tname: "+ Utils.safeString(data.pub.getName()) +"\n");

                if ( data.pub.getLocation() != null &&
                        data.pub.getLocation().getCoordinates() != null &&
                        data.pub.getLocation().getCoordinates().size() == 2 ) {

                    Double longitude = data.pub.getLocation().getCoordinates().get(0);
                    Double latitude = data.pub.getLocation().getCoordinates().get(1);

                    str.append("\tlatitude: "+ latitude +"\n");
                    str.append("\tlongitude: "+ longitude +"\n");
                }
                else {
                    str.append("\tlatitude: <INVALID_DATA>\n");
                    str.append("\tlongitude: <INVALID_DATA>\n");
                }

                str.append("\turl: "+ Utils.safeString(data.pub.getUrl()) +"\n");
                str.append("\towner: "+ Utils.safeString(data.pub.getOwner()) +"\n");

                if (data.pub.getEvents() == null || data.pub.getEvents().size() == 0)
                    str.append("\tevents: [ ] \n");
                else {
                    str.append("\tevents: [ ");

                    for (String eventId : data.pub.getEvents())
                        str.append(eventId + ", ");

                    str.setLength(str.length() - 2);    // to remove the last ', '
                    str.append(" ] \n");
                }

                if (data.pub.getPhotos() == null || data.pub.getPhotos().size() == 0)
                    str.append("\tphotos: [ ] \n");
                else {
                    str.append("\tphotos: [ ");

                    for (String photoUrl : data.pub.getPhotos())
                        str.append(photoUrl + ", ");

                    str.setLength(str.length() - 2);    // to remove the last ', '
                    str.append(" ] \n");
                }
            }

            if (data.event == null)
                str.append("event: null\n");
            else {
                str.append("event: \n");

                str.append("\tid: "+ Utils.safeString(data.event.getId()) +"\n");
                str.append("\tname: "+ Utils.safeString(data.event.getName()) +"\n");
                str.append("\tdescription: "+ Utils.safeString(data.event.getDescription()) +"\n");
                str.append("\tdateString: "+ (data.event.getDate() != null ? data.event.getDate().toString() : "empty date") +"\n");
                str.append("\tphotoUrl: "+ Utils.safeString(data.event.getPhotoUrl()) +"\n");

                if (data.event.getPubs() == null || data.event.getPubs().size() == 0)
                    str.append("\tpubs: [ ] \n");
                else {
                    str.append("\tpubs: [ ");

                    for (String pub : data.event.getPubs())
                        str.append(pub + ", ");

                    str.setLength(str.length() - 2);    // to remove the last ', '
                    str.append(" ] \n");
                }

                if (data.event.getCategories() == null || data.event.getCategories().size() == 0)
                    str.append("\tcategory: [ ] \n");
                else {
                    str.append("\tcategory: [ ");

                    for (String cat : data.event.getCategories())
                        str.append(cat + ", ");

                    str.setLength(str.length() - 2);    // to remove the last ', '
                    str.append(" ] \n");
                }
            }
        }

        return str.toString();
    }
}
