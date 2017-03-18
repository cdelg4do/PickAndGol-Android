package io.keepcoding.pickandgol.manager.net;

import io.keepcoding.pickandgol.BuildConfig;

/**
 * This class contains static settings to be used by the NewtworkManager class.
 */
public class NetworkManagerSettings {

    // Possible 'result' values for JSON responses
    public static final String JSON_RESULT_OK = "OK";
    public static final String JSON_RESULT_ERROR = "ERROR";

    // Remote URLs and endpoints
    public static final String URL_LOGIN = BuildConfig.HOST + "/api/v1/users/login";
    public static final String URL_REGISTER_USER = "http://www.mocky.io/v2/58a547fb290000fb076d69c6";
    public static final String URL_USER_DETAIL = BuildConfig.HOST + "/api/v1/users";
    public static final String URL_SEARCH_EVENTS = BuildConfig.HOST + "/api/v1/events";
    //public static final String URL_SEARCH_EVENTS = "http://www.mocky.io/v2/58c5ea6d0f0000a92cb59e12";
    public static final String URL_EVENT_DETAIL = "http://www.mocky.io/v2/58bf9d000f000077107b9576";
    public static final String URL_CREATE_EVENT = BuildConfig.HOST + "/api/v1/events";
    public static final String URL_CATEGORIES = BuildConfig.HOST + "/api/v1/categories";

    // Available JSON response types expected from the server
    public static enum JsonResponseType {

        LOGIN,           // LoginResponse
        USER,            // UserResponse
        EVENT_LIST,      // EventListResponse
        EVENT_DETAIL,    // EventDetailResponse
        CATEGORY_LIST    // CategoryListResponse
    }
}
