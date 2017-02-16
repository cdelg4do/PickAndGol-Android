package io.keepcoding.pickandgol.manager.net;

/**
 * This class represents an exception returned by the network manager
 * when a JSON response was retrieved, but it was malformed or its 'result' field was not OK
 */
public class IncorrectResponseException extends Exception {

    public IncorrectResponseException(String messsage) {
        super(messsage);
    }
}
