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


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends an user update request to the remote server and builds
 *   a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given GetUserInfoInteractorListener.
 */
public class UpdateUserInfoInteractor {

    // Param keynames for the update user info operation
    private static final String PUT_USER_TOKEN = "token";
    private static final String PUT_USER_EMAIL = "email";
    private static final String PUT_USER_NAME = "name";
    private static final String PUT_USER_OLD_PASSWORD = "old_password";
    private static final String PUT_USER_NEW_PASSWORD = "new_password";


    // This interface describes the behavior of a listener waiting for the the async operation
    public interface UpdateUserInfoInteractorListener {
        void onUpdateUserSuccess(User user);
        void onUpdateUserFail(Exception e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param token     session token to send to the server
     * @param user      object with the user info to update
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String token,
                        final @NonNull User user,
                        final @NonNull UpdateUserInfoInteractorListener listener) {

        if (listener == null)
            return;

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams userUpdateParams = new RequestParams();

        userUpdateParams.addParam(PUT_USER_TOKEN, token);

        if (user.getEmail() != null)
            userUpdateParams.addParam(PUT_USER_EMAIL, user.getEmail());

        if (user.getName() != null)
            userUpdateParams.addParam(PUT_USER_NAME, user.getName());

        if (user.getOldPassword() != null)
            userUpdateParams.addParam(PUT_USER_OLD_PASSWORD, user.getOldPassword());

        if (user.getNewPassword() != null)
            userUpdateParams.addParam(PUT_USER_NEW_PASSWORD, user.getNewPassword());

        String remoteUrl = getUrl(user.getId());

        networkMgr.launchPUTStringRequest(remoteUrl, userUpdateParams, USER, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                User user = new UserDataToUserMapper().map( (UserResponse.UserData)parsedData );
                listener.onUpdateUserSuccess(user);
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onUpdateUserFail(e);
            }
        });
    }

    // Gets the remote url for the operation
    private String getUrl(final String id) {
        return URL_USER_DETAIL + "/" + id;
    }
}
