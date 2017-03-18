package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.CategoryListResponse;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.model.mapper.CategoryListDataToCategoryAggregateMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.CATEGORY_LIST;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_CATEGORIES;

public class GetCategoriesInteractor {
    public interface Listener {
        void onFail(final String message);
        void onSuccess(CategoryAggregate categories);
    }

    public void execute(Context context, @NonNull final Listener listener) {
        NetworkManager networkManager = new NetworkManager(context);
        RequestParams params = new RequestParams();
        networkManager.launchGETStringRequest(URL_CATEGORIES, params, CATEGORY_LIST, new NetworkManager.NetworkRequestListener() {
            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                CategoryAggregate categories = new CategoryListDataToCategoryAggregateMapper().map((CategoryListResponse.CategoryListData) parsedData);
                listener.onSuccess(categories);
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onFail(e.getMessage());
            }
        });
    }
}
