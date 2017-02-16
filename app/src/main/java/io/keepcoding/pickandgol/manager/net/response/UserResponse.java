package io.keepcoding.pickandgol.manager.net.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;


/**
 * This class represents a User JSON response.
 */
public class UserResponse extends ParsedResponse {

    /**
     * This class represents the 'data' field of the response.
     */
    public class UserData extends ParsedResponse.ParsedData {

        // These fields only in case of 'OK' result
        @SerializedName("id")               private String id;
        @SerializedName("email")            private String email;
        @SerializedName("name")             private String name;
        @SerializedName("favorite_pubs")    private List<Integer> favoritePubs;

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
    }
}
