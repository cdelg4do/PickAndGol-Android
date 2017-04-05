package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.PubDetailResponse.PubDetailData;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.model.mapper.PubDetailDataToPubMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.PUB_DETAIL;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_CREATE_PUB;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): request the creation of a new pub in the system.
 * - Second (in the main thread): call the given listener with the result of the operation.
 */
public class CreatePubInteractor {

    // Param keynames for the event creation operation
    private static final String REQUEST_PARAM_KEY_TOKEN = "token";
    private static final String REQUEST_PARAM_KEY_NAME = "name";
    private static final String REQUEST_PARAM_KEY_LATITUDE = "latitude";
    private static final String REQUEST_PARAM_KEY_LONGITUDE = "longitude";
    private static final String REQUEST_PARAM_KEY_USER_ID = "user_id";
    private static final String REQUEST_PARAM_KEY_URL = "url";
    private static final String REQUEST_PARAM_KEY_PHOTO_URL = "photo_url";


    // This interface describes the behavior of a listener waiting for the the async operation
    public interface CreatePubInteractorListener {
        void onCreatePubFail(Exception e);
        void onCreatePubSuccess(Pub pub);
    }

    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context       context for the operation.
     * @param name          name for the new pub.
     * @param latitude      latitude for the new pub.
     * @param longitude     longitude for the new pub.
     * @param url           url for the new pub.
     * @param photoUrls     list of picture urls the new pub.
     * @param token         session token to send to the server
     * @param listener      listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String name,
                        final @NonNull double latitude,
                        final @NonNull double longitude,
                        final @Nullable String url,
                        final @Nullable List<String> photoUrls,
                        final @NonNull String token,
                        final @NonNull CreatePubInteractorListener listener) {

        if (listener == null)
            return;

        if (name == null) {
            listener.onCreatePubFail(new Exception("This operation requires a name for the pub."));
            return;
        }

        if (token == null) {
            listener.onCreatePubFail(new Exception("This operation requires a session token."));
            return;
        }


        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams createPubParams = new RequestParams();

        createPubParams
                .addParam(REQUEST_PARAM_KEY_NAME, name)
                .addParam(REQUEST_PARAM_KEY_LATITUDE, ""+latitude)
                .addParam(REQUEST_PARAM_KEY_LONGITUDE, ""+longitude)
                .addParam(REQUEST_PARAM_KEY_TOKEN, token);

        if (url != null)
            createPubParams.addParam(REQUEST_PARAM_KEY_URL, url);

        if (photoUrls != null && photoUrls.size() > 0) {

            for (int i = 0; i < photoUrls.size(); i++)
                createPubParams.addParam(REQUEST_PARAM_KEY_PHOTO_URL, photoUrls.get(i));
        }

        String remoteUrl = getUrl();

        networkMgr.launchPOSTStringRequest(remoteUrl, createPubParams, PUB_DETAIL, new NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onCreatePubFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {

                Pub createdPub = new PubDetailDataToPubMapper().map( (PubDetailData)parsedData );
                listener.onCreatePubSuccess(createdPub);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_CREATE_PUB;
    }

}
