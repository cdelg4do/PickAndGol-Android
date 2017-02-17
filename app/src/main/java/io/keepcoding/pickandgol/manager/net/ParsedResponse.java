package io.keepcoding.pickandgol.manager.net;

/**
 * This interface defines the behavior of a JSON-parsed response.
 */
public interface ParsedResponse {

    boolean resultIsOK();
    ParsedData getData();
}
