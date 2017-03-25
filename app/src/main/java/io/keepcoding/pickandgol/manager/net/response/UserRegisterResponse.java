package io.keepcoding.pickandgol.manager.net.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.ParsedResponse;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JSON_RESULT_OK;

public class UserRegisterResponse implements ParsedResponse {
    @SerializedName("result") private String result;
    @SerializedName("data") private UserRegisterData data;

    @Override
    public boolean resultIsOK() {
        return (result != null && result.equals(JSON_RESULT_OK));
    }

    @NonNull
    @Override
    public ParsedData getData() {
        return data;
    }

    @Override
    public String debugString() {
        return null;
    }

    public class UserRegisterData implements ParsedData {
        // These fields exist only in case of 'ERROR' result
        @SerializedName("code")         private String errorCode;
        @SerializedName("description")  private String errorDescription;

        // These fields only in case of 'OK' result
        @SerializedName("id")              private String id;
        @SerializedName("email")            private String email;
        @SerializedName("name")             private String name;

        @Nullable
        @Override
        public String getErrorCode() {
            return errorCode;
        }

        @Nullable
        @Override
        public String getErrorDescription() {
            return errorDescription;
        }
    }
}