package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;
import io.keepcoding.pickandgol.util.Utils;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;


/**
 * This class represents a User register JSON response.
 */
public class UserRegisterResponse implements ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private UserRegisterData data;

    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public @NonNull ParsedData getData() {
        return data;
    }


    /**
     * This class represents the 'data' field of the response.
     */
    public class UserRegisterData implements ParsedData {

        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields only in case of 'OK' result
        @SerializedName("id")       private String id;
        @SerializedName("email")    private String email;
        @SerializedName("name")     private String name;

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

        return str.toString();
    }
}
