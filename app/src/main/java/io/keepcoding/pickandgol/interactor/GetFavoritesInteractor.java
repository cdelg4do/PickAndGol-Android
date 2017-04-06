package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.PubListResponse;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.model.mapper.PubListDataToPubAggregateMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.PUB_LIST;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_USER_FAVORITES;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends an search user favorites request to the remote server and builds
 *   a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given GetFavoritesInteractorListener.
 */
public class GetFavoritesInteractor {

    // Param keynames for the retrieve user info operation
    private static final String REQUEST_PARAM_KEY_TOKEN = "token";

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface GetFavoritesInteractorListener {
        void onGetFavoritesSuccess(PubAggregate favorites);
        void onGetFavoritesFail(Exception e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param userId    id of the user we are asking for
     * @param token     session token to send to the server
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String userId,
                        final @NonNull String token,
                        final @NonNull GetFavoritesInteractorListener listener) {

        if (listener == null)
            return;

        if (token == null) {
            listener.onGetFavoritesFail(new Exception("This operation requires a session token."));
            return;
        }

        if (userId == null) {
            listener.onGetFavoritesFail(new Exception("This operation requires a user ID."));
            return;
        }

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams getFavoritesParams = new RequestParams();

        getFavoritesParams.addParam(REQUEST_PARAM_KEY_TOKEN, token);

        String remoteUrl = getUrl(userId);

        networkMgr.launchGETStringRequest(remoteUrl, getFavoritesParams, PUB_LIST, new NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onGetFavoritesFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {

                PubAggregate pubs = new PubListDataToPubAggregateMapper().map( (PubListResponse.PubListData)parsedData );
                listener.onGetFavoritesSuccess(pubs);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl(String userId) {
        return URL_USER_FAVORITES +"/"+ userId +"/favorites";
    }

}
