package io.keepcoding.pickandgol.model.db;

import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.model.User;

public interface DBManager {
    void saveUser(@NonNull final User user);
    User getUser(@NonNull final String userId);
}
