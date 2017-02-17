package io.keepcoding.pickandgol.manager.net;

import android.support.annotation.NonNull;

/**
 * This interface defines the behavior of a JSON-parsed response.
 */
public interface ParsedResponse {

    boolean resultIsOK();
    @NonNull ParsedData getData();
}
