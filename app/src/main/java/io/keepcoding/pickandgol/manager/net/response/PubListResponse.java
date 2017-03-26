package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.manager.net.response.PubDetailResponse.PubDetailData;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a Pub List JSON response.
 */
public class PubListResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private PubListData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull PubListData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class PubListData implements ParsedData {

        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields only in case of 'OK' result
        @SerializedName("total")        private int total;
        @SerializedName("items")        private List<PubDetailData> items;


        public @Nullable String getErrorCode() {
            return errorCode;
        }

        public @Nullable String getErrorDescription() {
            return errorDescription;
        }

        public int getTotal() {
            return total;
        }

        public List<PubDetailData> getPubList() {
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
                PubDetailData pub = data.items.get(i);

                boolean hasLocation = ( pub.getLocation() != null &&
                                        pub.getLocation().getCoordinates() != null &&
                                        pub.getLocation().getCoordinates().size() == 2 );

                str.append("\tid: "+ Utils.safeString(pub.getId()) +"\n");
                str.append("\tname: "+ Utils.safeString(pub.getName()) +"\n");

                if (hasLocation) {
                    str.append("\tlatitude: "+ pub.getLocation().getCoordinates().get(0) +"\n");
                    str.append("\tlongitude: "+ pub.getLocation().getCoordinates().get(1) +"\n");
                }
                else {
                    str.append("\tlatitude: <INVALID_DATA>\n");
                    str.append("\tlongitude: <INVALID_DATA>\n");
                }

                str.append("\turl: "+ Utils.safeString(pub.getUrl()) +"\n");
                str.append("\towner: "+ Utils.safeString(pub.getOwner()) +"\n");

                str.append("\tphotos: [  ");

                for (String photoUrl : pub.getPhotos())
                    str.append(photoUrl + ", ");

                str.setLength(str.length() - 2);    // to remove the last ', '
                str.append(" ] \n");

                if (i < data.items.size()-1)
                    str.append("\t, \n");
            }

            str.append("]");
        }

        return str.toString();
    }
}
