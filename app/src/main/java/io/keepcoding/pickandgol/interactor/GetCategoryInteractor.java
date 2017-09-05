package io.keepcoding.pickandgol.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.keepcoding.pickandgol.manager.db.DBManager;
import io.keepcoding.pickandgol.manager.db.DBManagerListener;
import io.keepcoding.pickandgol.manager.db.realm.RealmDBManager;
import io.keepcoding.pickandgol.model.Category;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): retrieve the category for a given id from the local database and builds
 *   a new model object with the retrieved info.
 * - Second (in the main thread): pass the model object to the given GetCategoryInteractorListener.
 */
public class GetCategoryInteractor {

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface GetCategoryInteractorListener {
        void onGetCategoryFail(Throwable e);
        void onGetCategorySuccess(Category category);
    }


    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener.
     *
     * @param id        id of the category we are asking for
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull String id,
                        final @NonNull GetCategoryInteractorListener listener) {

        if (listener == null)
            return;

        final DBManager dbManager = RealmDBManager.getDefaultInstance();

        dbManager.getCategory(id, new DBManagerListener() {
            @Override
            public void onError(Throwable e) {
                listener.onGetCategoryFail(e);
            }

            @Override
            public void onSuccess(@Nullable Object result) {

                Category category = null;

                if (result != null && result instanceof Category)
                    category = (Category) result;

                listener.onGetCategorySuccess(category);
            }
        });
    }

}
