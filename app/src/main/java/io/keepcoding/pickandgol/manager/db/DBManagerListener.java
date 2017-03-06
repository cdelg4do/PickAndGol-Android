package io.keepcoding.pickandgol.manager.db;

import android.support.annotation.Nullable;


/**
 * Use this class to listen for DBManager operations
 */
public interface DBManagerListener {

    void onError(Throwable e);
    void onSuccess(@Nullable Object result);
}
