package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.UserResponse;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.model.mapper.UserDataToUserMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.USER;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_USER_DETAIL;

public class EditUserInteractor {

    private static final String PUT_USER_TOKEN = "token";
    private static final String PUT_USER_EMAIL = "email";
    private static final String PUT_USER_NAME = "name";
    private static final String PUT_USER_OLD_PASSWORD = "old_password";
    private static final String PUT_USER_NEW_PASSWORD = "new_password";

    public interface Listener {
        void onEditUserSuccess();
        void onEditUserFail(final String message);
    }

    public void execute(final @NonNull Context context,
                        final @NonNull String token,
                        final @NonNull User user,
                        final @NonNull Listener listener) {
        if (listener == null) {
            return;
        }

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams userDetailParams = new RequestParams();
        userDetailParams.addParam(PUT_USER_TOKEN, token);
        if (user.getEmail() != null) {
            userDetailParams.addParam(PUT_USER_EMAIL, user.getEmail());
        }

        if (user.getName() != null) {
            userDetailParams.addParam(PUT_USER_NAME, user.getName());
        }

        if (user.getOldPassword() != null) {
            userDetailParams.addParam(PUT_USER_OLD_PASSWORD, user.getOldPassword());
        }

        if (user.getNewPassword() != null) {
            userDetailParams.addParam(PUT_USER_NEW_PASSWORD, user.getNewPassword());
        }

        String remoteUrl = getUrl(user.getId());

        networkMgr.launchPUTStringRequest(remoteUrl, userDetailParams, USER, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                User user = new UserDataToUserMapper().map( (UserResponse.UserData)parsedData );
                listener.onEditUserSuccess();
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onEditUserFail(e.getMessage());
            }
        });
    }

    // Gets the remote url for the operation
    private String getUrl(final String id) {
        return URL_USER_DETAIL + "/" + id;
    }
}
