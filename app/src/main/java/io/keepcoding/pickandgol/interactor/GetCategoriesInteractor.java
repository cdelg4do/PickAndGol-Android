package io.keepcoding.pickandgol.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.manager.db.realm.RealmDBManager;
import io.keepcoding.pickandgol.model.CategoryAggregate;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): retrieve the existing categories from the local database and builds
 *   a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given GetUserInfoInteractorListener.
 */
public class GetCategoriesInteractor {

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface GetCategoriesInteractorListener {
        void onGetCategoriesFail(Throwable e);
        void onGetCategoriesSuccess(CategoryAggregate categories);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener.
     *
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull GetCategoriesInteractorListener listener) {

        if (listener == null)
            return;

        final DBManager dbManager = RealmDBManager.getDefaultInstance();

        dbManager.getAllCategories(new DBManagerListener() {
            @Override
            public void onError(Throwable e) {
                listener.onGetCategoriesFail(e);
            }

            @Override
            public void onSuccess(@Nullable Object result) {

                CategoryAggregate categories = null;

                if (result != null && result instanceof CategoryAggregate)
                    categories = (CategoryAggregate) result;

                listener.onGetCategoriesSuccess(categories);
            }
        });
    }

}
