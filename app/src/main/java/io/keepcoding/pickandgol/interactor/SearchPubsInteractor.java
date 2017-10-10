package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.PubListResponse;
import io.keepcoding.pickandgol.model.PubAggregate;
import io.keepcoding.pickandgol.model.mapper.PubListDataToPubAggregateMapper;
import io.keepcoding.pickandgol.search.PubSearchParams;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.PUB_LIST;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_SEARCH_PUBS;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): Download a list of pubs, according to the given filters.
 * - Second (in the main thread): Map the received data to a model object and call the listener.
 */
public class SearchPubsInteractor {

    // Param keynames for the pub search operation
    public static final String REQUEST_PARAM_KEY_OFFSET = "offset";
    public static final String REQUEST_PARAM_KEY_LIMIT = "limit";
    public static final String REQUEST_PARAM_KEY_SORT = "sort";
    public static final String REQUEST_PARAM_KEY_TEXT = "text";
    public static final String REQUEST_PARAM_KEY_LATITUDE = "latitude";
    public static final String REQUEST_PARAM_KEY_LONGITUDE = "longitude";
    public static final String REQUEST_PARAM_KEY_RADIUS = "radius"; // radius in meters
    public static final String REQUEST_PARAM_KEY_EVENT = "event";


    // This interface describes the behavior of a listener waiting for the the async operation
    public interface SearchPubsInteractorListener {
        void onSearchPubsFail(Exception e);
        void onSearchPubsSuccess(PubAggregate pubs);
    }

    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(Context context,
                        final @NonNull PubSearchParams searchParams,
                        final @NonNull SearchPubsInteractorListener listener) {

        Integer offset = searchParams.getOffset();
        Integer limit = searchParams.getLimit();
        String sort = searchParams.getSort();
        String keyWords = searchParams.getKeyWords();
        Double latitude = searchParams.getLatitude();
        Double longitude = searchParams.getLongitude();
        Integer radius = (searchParams.getRadiusKm() != null) ? 1000 * searchParams.getRadiusKm() : null;
        String eventId = searchParams.getEventId();


        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams searchPubsParams = new RequestParams();

        if (offset != null)
            searchPubsParams.addParam(REQUEST_PARAM_KEY_OFFSET, offset.toString());

        if (limit != null)
            searchPubsParams.addParam(REQUEST_PARAM_KEY_LIMIT, limit.toString());

        if (sort != null)
            searchPubsParams.addParam(REQUEST_PARAM_KEY_SORT, sort);

        if (keyWords != null)
            searchPubsParams.addParam(REQUEST_PARAM_KEY_TEXT, keyWords);

        if (latitude != null && longitude != null && radius != null && radius > 0) {

            searchPubsParams.addParam(REQUEST_PARAM_KEY_LATITUDE, latitude.toString());
            searchPubsParams.addParam(REQUEST_PARAM_KEY_LONGITUDE, longitude.toString());
            searchPubsParams.addParam(REQUEST_PARAM_KEY_RADIUS, radius.toString());
        }

        if (eventId != null)
            searchPubsParams.addParam(REQUEST_PARAM_KEY_EVENT, eventId);

        String remoteUrl = getUrl();

        networkMgr.launchGETStringRequest(remoteUrl, searchPubsParams, PUB_LIST, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onSearchPubsFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                PubAggregate pubs = new PubListDataToPubAggregateMapper().map( (PubListResponse.PubListData)parsedData );
                listener.onSearchPubsSuccess(pubs);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_SEARCH_PUBS;
    }

}
