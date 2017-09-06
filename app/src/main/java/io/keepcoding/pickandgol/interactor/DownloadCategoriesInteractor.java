package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import io.keepcoding.pickandgol.PickAndGolApp;
import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerBuilder;
import io.keepcoding.pickandgol.manager.db.DBManagerBuilder.DatabaseType;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
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
 * - First (in background): sends a remote request to retrieve the existing categories and stores
 * them in the local database.
 * - Second (in the main thread): returns control to the received DownloadCategoriesInteractor.
 */
public class DownloadCategoriesInteractor {

    private static final String LOG_TAG = "DownloadCategoriesInt";
    private static final DatabaseType DB_TYPE = PickAndGolApp.DBTYPE;

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface DownloadCategoriesInteractorListener {
        void onDownloadCategoriesSuccess(int count);
        void onDownloadCategoriesFail(Throwable e);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context       context for the operation.
     * @param emptyIfFail   true: in case of fail, the database will contain no categories
     *                      false: in case of fail, the database will keep the previous categories
     * @param listener      listener that will process the result of the operation.
     */
    public void execute(Context context, final boolean emptyIfFail, final @NonNull DownloadCategoriesInteractorListener listener) {

        if (listener == null)
            return;

        final DBManager dbManager = new DBManagerBuilder().type(DB_TYPE).build();

        NetworkManager networkManager = new NetworkManager(context);
        RequestParams getCategoriesParams = new RequestParams();

        String remoteUrl = getUrl();

        networkManager.launchGETStringRequest(remoteUrl, getCategoriesParams, CATEGORY_LIST,
                new NetworkRequestListener() {

                    @Override
                    public void onNetworkRequestFail(final Exception e) {

                        if (emptyIfFail) {

                            dbManager.removeAllCategories(new DBManagerListener() {
                                @Override
                                public void onError(Throwable t) {
                                    Log.e(LOG_TAG, "Failed to remove categories from database: ", t);
                                    listener.onDownloadCategoriesFail(e);
                                }

                                @Override
                                public void onSuccess(@Nullable Object result) {
                                    Log.e(LOG_TAG, "Forced to remove all existing categories from the database");
                                    listener.onDownloadCategoriesFail(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onNetworkRequestSuccess(ParsedData parsedData) {

                        final CategoryAggregate categories = new CategoryListDataToCategoryAggregateMapper()
                                .map((CategoryListData) parsedData);

                        dbManager.saveCategories(categories, new DBManagerListener() {

                            @Override
                            public void onError(Throwable t) {
                                listener.onDownloadCategoriesFail(t);
                            }

                            @Override
                            public void onSuccess(@Nullable Object result) {
                                listener.onDownloadCategoriesSuccess(categories.size());
                            }
                        });
                    }
                });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_CATEGORIES;
    }
}
