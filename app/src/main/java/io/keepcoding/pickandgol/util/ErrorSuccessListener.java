package io.keepcoding.pickandgol.util;


import android.support.annotation.Nullable;

/**
 * This is just a generic interface for an error/success listener.
 */
public interface ErrorSuccessListener {

    void onError(@Nullable Object result);
    void onSuccess(@Nullable Object result);
}
