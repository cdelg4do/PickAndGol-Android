package io.keepcoding.pickandgol.manager.net.response;

import com.google.gson.annotations.SerializedName;


/**
 * This class represents a Login JSON response.
 */
public class LoginResponse extends ParsedResponse {

    /**
     * This class represents the 'data' field of the response.
     */
    public class LoginData extends ParsedResponse.ParsedData {

        // These fields only in case of 'OK' result
        @SerializedName("id")           private String id;
        @SerializedName("email")        private String email;
        @SerializedName("name")         private String name;
        @SerializedName("token")        private String token;
        @SerializedName("photo_url")    private String photoUrl;    // Optional

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
