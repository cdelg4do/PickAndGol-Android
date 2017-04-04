package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.UserResponse;
import io.keepcoding.pickandgol.model.User;
import io.keepcoding.pickandgol.model.mapper.UserDataToUserMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.USER;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_USER_FAVORITES;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends a request to the server asking to add/remove a pub from a user
 * favorites, and builds a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given ToggleFavoriteInteractorListener.
 */
public class ToggleFavoriteInteractor {

    // Param keynames for the toggle favorite operation
    private static final String REQUEST_PARAM_KEY_TOKEN = "token";

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface ToggleFavoriteInteractorListener {
        void onToggleFavoriteSuccess(User user);
        void onToggleFavoriteFail(Exception e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context           context for the operation.
     * @param addToFavorites    if true, will add the pub to favorites. If false, will remove it.
     * @param pubId             id of the pub we are adding/removing from the user favorites.
     * @param userId            id of the user we are asking for.
     * @param token             session token to send to the server.
     * @param listener          listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final boolean addToFavorites,
                        final @NonNull String pubId,
                        final @NonNull String userId,
                        final @NonNull String token,
                        final @NonNull ToggleFavoriteInteractorListener listener) {

        if (listener == null)
            return;

        if (token == null) {
            listener.onToggleFavoriteFail(new Exception("This operation requires a session token."));
            return;
        }

        if (userId == null) {
            listener.onToggleFavoriteFail(new Exception("This operation requires a user ID."));
            return;
        }

        if (pubId == null) {
            listener.onToggleFavoriteFail(new Exception("This operation requires a pub ID."));
            return;
        }

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams toggleFavoriteParams = new RequestParams();

        toggleFavoriteParams.addParam(REQUEST_PARAM_KEY_TOKEN, token);

        String remoteUrl = getUrl(userId, pubId);

        // Listener for the network operation (to use both in case of adding and removing favorite)
        NetworkRequestListener networkListener = new NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onToggleFavoriteFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {

                User user = new UserDataToUserMapper().map( (UserResponse.UserData)parsedData );
                listener.onToggleFavoriteSuccess(user);
            }
        };

        // If we are adding the pub to favorites, send a POST request
        if (addToFavorites) {
            networkMgr.launchPOSTStringRequest(remoteUrl, toggleFavoriteParams, USER, networkListener);
        }

        // If we are removing the pub from favorites, send a DELETE request
        else {
            // TODO: implement DELETE requests (in both the network manager and the backend)
            //networkMgr.launchDELETEStringRequest(remoteUrl, toggleFavoriteParams, USER, networkListener);

            listener.onToggleFavoriteFail(new Exception("Removing a pub from favorites is not (yet) supported."));
        }
    }


    // Gets the remote url for the operation
    private String getUrl(String userId, String pubId) {
        return URL_USER_FAVORITES +"/"+ userId +"/favorites/"+ pubId;
    }

}
