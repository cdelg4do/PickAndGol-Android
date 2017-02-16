package io.keepcoding.pickandgol.manager.net.response;

import com.google.gson.annotations.SerializedName;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;

/**
 * This is a base class for all JSON-parsed responses.
 */
public abstract class ParsedResponse {

    @SerializedName("result") private String result;
    @SerializedName("data") private ParsedData data;

    /**
     * This is a base class for all JSON-parsed 'data' fields.
     */
    public abstract class ParsedData {

        // These fields only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorDescription() {
            return errorDescription;
        }
    }


    // Getter methods:

    public boolean wasSuccessful() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    public ParsedData getData() {
        return data;
    }
}
