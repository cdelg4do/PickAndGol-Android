package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.EventDetailResponse;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.mapper.EventDetailDataToEventMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.EVENT_DETAIL;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_LINK_EVENT_PUB;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends a request to the server to link a given event to a given pub
 *   and builds a new model object with the retrieved data.
 * - Second (in the main thread): pass the model object to the given LinkEventToPubInteractorListener.
 */
public class LinkEventToPubInteractor {

    // Param keynames for the update user info operation
    private static final String PUT_USER_TOKEN = "token";

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface LinkEventToPubInteractorListener {
        void onEventLinkSuccess(Event event);
        void onEventLinkFail(Exception e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation
     * @param eventId   the event id we are linking
     * @param pubId     the pub id we are linking the event to
     * @param token     session token to send to the server
     * @param listener  listener that will process the result of the operation
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String eventId,
                        final @NonNull String pubId,
                        final @NonNull String token,
                        final @NonNull LinkEventToPubInteractorListener listener) {

        if (listener == null)
            return;

        if (token == null) {
            listener.onEventLinkFail(new Exception("This operation requires a session token."));
            return;
        }

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams linkEventToPubParams = new RequestParams();

        linkEventToPubParams.addParam(PUT_USER_TOKEN, token);

        String remoteUrl = getUrl(eventId, pubId);

        // TODO: use the proper expected response type (create a new one if necessary)
        networkMgr.launchPUTStringRequest(remoteUrl, linkEventToPubParams, EVENT_DETAIL,
                                          new NetworkRequestListener() {
            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onEventLinkFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                Event event = new EventDetailDataToEventMapper().map((EventDetailResponse.EventDetailData)parsedData);
                listener.onEventLinkSuccess(event);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl(final String eventId, final String pubId) {

        return URL_LINK_EVENT_PUB +"/"+ eventId +"/pubs/"+ pubId;
    }
}
