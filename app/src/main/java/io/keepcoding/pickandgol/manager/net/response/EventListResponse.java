package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.manager.net.response.EventDetailResponse.EventDetailData;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a Event List JSON response.
 */
public class EventListResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private EventListData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull EventListData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class EventListData implements ParsedData {

        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields only in case of 'OK' result
        @SerializedName("total")        private int total;
        @SerializedName("items")        private List<EventDetailData> items;


        public @Nullable String getErrorCode() {
            return errorCode;
        }

        public @Nullable String getErrorDescription() {
            return errorDescription;
        }

        public int getTotal() {
            return total;
        }

        public List<EventDetailData> getEventList() {
            return items;
        }
    }


    // Outputs the response data as a String (for debugging purposes)
    public String debugString() {

        StringBuilder str = new StringBuilder();

        str.append("result: "+ Utils.safeString(result) +"\n");
        str.append("code: "+ Utils.safeString(data.errorCode) +"\n");
        str.append("description: "+ Utils.safeString(data.errorDescription) +"\n");

        str.append("total: "+ data.total +"\n");

        if (data.items == null || data.items.size() == 0)
            str.append("items: [ ] \n");

        else {
            str.append("items: [ \n");

            for (int i = 0; i < data.items.size() ; i++) {
                EventDetailData event = data.items.get(i);

                str.append("\t_id: "+ Utils.safeString(event.getId()) +"\n");
                str.append("\tname: "+ Utils.safeString(event.getName()) +"\n");
                str.append("\tdate: "+ event.getDate() +"\n");
                str.append("\tdescription: "+ Utils.safeString(event.getDescription()) +"\n");
                str.append("\tphotoUrl: "+ Utils.safeString(event.getPhotoUrl()) +"\n");
                str.append("\tcategory\t: "+ event.getCategories().get(0) +"\n");

                str.append("\tpubs: [ ");

                for (String pub : event.getPubs())
                    str.append(pub + ", ");

                str.setLength(str.length() - 2);    // to remove the last ', '
                str.append(" ] \n");

                if (i < data.items.size()-1)
                    str.append("\t, \n");
            }

            str.append("\n]");
        }

        return str.toString();
    }
}
