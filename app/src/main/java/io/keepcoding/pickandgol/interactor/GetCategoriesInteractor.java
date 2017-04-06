package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.CategoryListResponse.CategoryListData;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.model.mapper.CategoryListDataToCategoryAggregateMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.CATEGORY_LIST;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_CATEGORIES;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends a remote request to retrieve the existing categories and builds
 *   a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given GetUserInfoInteractorListener.
 */
public class GetCategoriesInteractor {

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface GetCategoriesInteractorListener {
        void onGetCategoriesSuccess(CategoryAggregate categories);
        void onGetCategoriesFail(Exception e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(Context context, final @NonNull GetCategoriesInteractorListener listener) {

        if (listener == null)
            return;

        NetworkManager networkManager = new NetworkManager(context);
        RequestParams getCategoriesParams = new RequestParams();

        String remoteUrl = getUrl();

        networkManager.launchGETStringRequest(remoteUrl, getCategoriesParams, CATEGORY_LIST,
                                              new NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onGetCategoriesFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {

                CategoryAggregate categories = new CategoryListDataToCategoryAggregateMapper()
                                                .map((CategoryListData) parsedData);
                listener.onGetCategoriesSuccess(categories);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_CATEGORIES;
    }
}
