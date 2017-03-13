package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents an Event Detail JSON response.
 */
public class EventDetailResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private EventDetailData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull EventDetailData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class EventDetailData implements ParsedData {

        // This field exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;

        // This field is mandatory in case of 'ERROR' result, and optional (may come as "") in case of 'OK' result
        @SerializedName("description")  private String description;

        // These fields only in case of 'OK' result
        @SerializedName("_id")          private String id;
        @SerializedName("name")         private String name;
        @SerializedName("date")         private String dateString;
        @SerializedName("photo_url")    private String photoUrl;    // Optional field (may come as "")
        @SerializedName("pubs")         private List<String> pubs;
        @SerializedName("category")     private List<String> categories;


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

        public @Nullable Date getDate() {
            return Utils.getDateFromMongo(dateString);
        }

        public @Nullable String getDescription() {
            if (description.equals(""))
                return null;

            return description;
        }

        public @Nullable String getPhotoUrl() {
            if (photoUrl.equals(""))
                return null;

            return photoUrl;
        }

        public List<String> getPubs() {
            return pubs;
        }

        public List<String> getCategories() {
            return categories;
        }
    }


    // Outputs the response data as a String (for debugging purposes)
    public String debugString() {

        StringBuilder str = new StringBuilder();

        str.append("result: "+ Utils.safeString(result) +"\n");

        str.append("code: "+ Utils.safeString(data.errorCode) +"\n");
        str.append("description: "+ Utils.safeString(data.description) +"\n");
/*
        str.append("id: "+ Utils.safeString(data.id) +"\n");
        str.append("email: "+ Utils.safeString(data.email) +"\n");
        str.append("name: "+ Utils.safeString(data.name) +"\n");
        str.append("photoUrl: "+ Utils.safeString(data.photoUrl) +"\n");

        if (data.favoritePubs == null || data.favoritePubs.size() == 0)
            str.append("pubs: [ ] \n");

        else {
            str.append("pubs: [ ");

            for (String pub : data.favoritePubs)
                str.append(pub + ", ");

            str.setLength(str.length() - 2);    // to remove the last ', '
            str.append(" ] \n");
        }
*/
        return str.toString();
    }
}
