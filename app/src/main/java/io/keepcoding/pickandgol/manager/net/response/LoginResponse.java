package io.keepcoding.pickandgol.manager.net.response;

import com.google.gson.annotations.SerializedName;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;

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

    public LoginData getData() {
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


        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorDescription() {
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
}
