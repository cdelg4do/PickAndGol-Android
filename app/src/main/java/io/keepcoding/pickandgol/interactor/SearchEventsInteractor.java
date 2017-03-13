package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.EventListResponse;
import io.keepcoding.pickandgol.model.EventAggregate;
import io.keepcoding.pickandgol.model.mapper.EventListDataToEventAggregateMapper;
import io.keepcoding.pickandgol.search.EventSearchParams;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.EVENT_LIST;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_SEARCH_EVENTS;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): Download a list of events, according to the given filters.
 * - Second (in the main thread): Map the received data to a model object and call the listener.
 */
public class SearchEventsInteractor {

    // Param keynames for the events search operation
    public static final String REQUEST_PARAM_KEY_OFFSET = "offset";
    public static final String REQUEST_PARAM_KEY_LIMIT = "limit";
    public static final String REQUEST_PARAM_KEY_PUB = "pub";
    public static final String REQUEST_PARAM_KEY_TEXT = "text";
    public static final String REQUEST_PARAM_KEY_CATEGORY = "category";
    public static final String REQUEST_PARAM_KEY_LATITUDE = "latitude";
    public static final String REQUEST_PARAM_KEY_LONGITUDE = "longitude";
    public static final String REQUEST_PARAM_KEY_RADIUS = "radius";


    // This interface describes the behavior of a listener waiting for the the async operation
    public interface SearchEventsInteractorListener {
        void onSearchEventsFail(Exception e);
        void onSearchEventsSuccess(EventAggregate events);
    }

    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(Context context,
                        final @NonNull EventSearchParams searchParams,
                        final @NonNull SearchEventsInteractorListener listener) {

        Integer offset = searchParams.getOffset();
        Integer limit = searchParams.getLimit();
        String pubId = searchParams.getPubId();
        String keyWords = searchParams.getKeyWords();
        Integer categoryId = searchParams.getCategoryId();
        Double latitude = searchParams.getLatitude();
        Double longitude = searchParams.getLongitude();
        Integer radiusKm = searchParams.getRadiusKm();


        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams searchEventsParams = new RequestParams();

        if (offset != null)
            searchEventsParams.addParam(REQUEST_PARAM_KEY_OFFSET, offset.toString());

        if (limit != null)
            searchEventsParams.addParam(REQUEST_PARAM_KEY_LIMIT, limit.toString());

        if (pubId != null)
            searchEventsParams.addParam(REQUEST_PARAM_KEY_PUB, pubId);

        if (keyWords != null)
            searchEventsParams.addParam(REQUEST_PARAM_KEY_TEXT, keyWords);

        if (categoryId != null)
            searchEventsParams.addParam(REQUEST_PARAM_KEY_CATEGORY, categoryId.toString());

        if (latitude != null && longitude != null && radiusKm != null && radiusKm > 0) {

            searchEventsParams.addParam(REQUEST_PARAM_KEY_LATITUDE, latitude.toString());
            searchEventsParams.addParam(REQUEST_PARAM_KEY_LONGITUDE, longitude.toString());
            searchEventsParams.addParam(REQUEST_PARAM_KEY_RADIUS, radiusKm.toString());
        }

        String remoteUrl = getUrl();

        networkMgr.launchGETStringRequest(remoteUrl, searchEventsParams, EVENT_LIST, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                EventAggregate events = new EventListDataToEventAggregateMapper().map( (EventListResponse.EventListData)parsedData );
                listener.onSearchEventsSuccess(events);
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onSearchEventsFail(e);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_SEARCH_EVENTS;
    }

}
