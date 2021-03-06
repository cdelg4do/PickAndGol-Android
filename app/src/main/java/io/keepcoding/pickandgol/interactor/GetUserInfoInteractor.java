package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.UserResponse.UserData;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.model.mapper.UserDataToUserMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.USER;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_USER_DETAIL;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends an user detail request to the remote server and builds
 *   a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given GetUserInfoInteractorListener.
 */
public class GetUserInfoInteractor {

    // Param keynames for the retrieve user info operation
    private static final String REQUEST_PARAM_KEY_TOKEN = "token";

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface GetUserInfoInteractorListener {
        void onUserDetailSuccess(User user);
        void onUserDetailFail(Exception e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param id        id of the user we are asking for
     * @param token     session token to send to the server
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String id,
                        final @NonNull String token,
                        final @NonNull GetUserInfoInteractorListener listener) {

        if (listener == null || token == null)
            return;

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams userInfoParams = new RequestParams();

        userInfoParams.addParam(REQUEST_PARAM_KEY_TOKEN, token);

        String remoteUrl = getUrl(id);

        networkMgr.launchGETStringRequest(remoteUrl, userInfoParams, USER, new NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onUserDetailFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {

                User user = new UserDataToUserMapper().map( (UserData)parsedData );
                listener.onUserDetailSuccess(user);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl(String id) {
        return URL_USER_DETAIL + "/" + id;
    }

}
