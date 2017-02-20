package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a Login JSON response.
 */
public class LoginResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private LoginData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull LoginData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class LoginData implements ParsedData {

        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields exist only in case of 'OK' result
        @SerializedName("id")           private String id;
        @SerializedName("email")        private String email;
        @SerializedName("name")         private String name;
        @SerializedName("token")        private String token;
        @SerializedName("photo_url")    private String photoUrl;    // Optional field


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

        public String getToken() {
            return token;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }
    }


    // Outputs the response data as a String (for debugging purposes)
    public String debugString() {

        StringBuilder str = new StringBuilder();

        str.append("result: "+ Utils.safeString(result) +"\n");

        str.append("code: "+ Utils.safeString(data.errorCode) +"\n");
        str.append("description: "+ Utils.safeString(data.errorDescription) +"\n");

        str.append("id: "+ Utils.safeString(data.id) +"\n");
        str.append("email: "+ Utils.safeString(data.email) +"\n");
        str.append("name: "+ Utils.safeString(data.name) +"\n");
        str.append("token: "+ Utils.safeString(data.token) +"\n");
        str.append("photoUrl: "+ Utils.safeString(data.photoUrl) +"\n");

        return str.toString();
    }
}
