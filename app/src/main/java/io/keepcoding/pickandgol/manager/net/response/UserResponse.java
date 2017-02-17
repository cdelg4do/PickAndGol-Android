package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a User JSON response.
 */
public class UserResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private UserData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull UserData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class UserData implements ParsedData {

        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields only in case of 'OK' result
        @SerializedName("id")               private String id;
        @SerializedName("email")            private String email;
        @SerializedName("name")             private String name;
        @SerializedName("pubs")             private List<Integer> favoritePubs;
        @SerializedName("photo_url")        private String photoUrl;    // Optional field


        public @Nullable String getErrorCode() {
            return errorCode;
        }

        public @Nullable String getErrorDescription() {
            return errorDescription;
        }

        public String getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public List<Integer> getFavoritePubs() {
            return favoritePubs;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }
    }
}
