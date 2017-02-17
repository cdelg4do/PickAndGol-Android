package io.keepcoding.pickandgol.manager.net;

import android.support.annotation.Nullable;

/**
 * This interface defines the behavior of a JSON-parsed 'data' field.
 */
public interface ParsedData {

    @Nullable String getErrorCode();
    @Nullable String getErrorDescription();
}
