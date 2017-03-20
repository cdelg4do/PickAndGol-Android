package io.keepcoding.pickandgol.manager.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.Map;

import io.keepcoding.pickandgol.manager.net.response.CategoryListResponse;
import io.keepcoding.pickandgol.manager.net.response.EventDetailResponse;
import io.keepcoding.pickandgol.manager.net.response.EventListResponse;
import io.keepcoding.pickandgol.manager.net.response.LoginResponse;
import io.keepcoding.pickandgol.manager.net.response.PubDetailResponse;
import io.keepcoding.pickandgol.manager.net.response.UserResponse;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType;


/**
 * This class manages the following network operations:
 *
 * - Detecting the device connection status
 * - Sending requests to remote URLs
 * - Parsing JSON responses and returning the data to a listener
 *
 * Does not include image requests, see the ImageManager for that.
 */
public class NetworkManager {

    private final static String LOG_TAG = "NetworkManager";

    private WeakReference<Context> context;

    public NetworkManager(final @NonNull Context context) {
        this.context = new WeakReference<>(context);
    }

    /**
     * This interface describes a listener waiting for the completion of the network operation.
     *
     * onNetworkRequestSuccess() should be called only if the JSON request was parsed successfully
     * and the 'result' field value was OK. Otherwise, call onNetworkRequestFail().
     */
    public interface NetworkRequestListener {
        void onNetworkRequestSuccess(ParsedData parsedData);
        void onNetworkRequestFail(Exception e);
    }

    /**
     * Types of network connection the manager can detect
     */
    public static enum ConnectionType {

        NONE,
        WIFI,
        OTHER   // mobile, wimax, vpn, etc.
    }

    /**
     * Determines the device's current connection type, if any (static method).
     *
     * @param context a context for the operation.
     * @return the type of internet connection
     */
    public static ConnectionType getInternetConnectionType(final @NonNull Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        if ( !activeNetworkInfo.isConnectedOrConnecting() )
            return ConnectionType.NONE;

        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            return ConnectionType.WIFI;

        return ConnectionType.OTHER;
    }


    /**
     * Queues a new GET request through the network.
     *
     * If the request was successful, will call listener.onNetworkRequestSuccess() passing
     * a ParsedData object to the listener (the listener should cast it to the appropriate class).
     *
     * Otherwise, it will call listener.onNetworkRequestFail() passing the error to the listener.
     *
     * @param url the url to address the request
     * @param urlParams a Map with the parameters to add to the request url
     * @param expectedResponseType the expected response type for the request
     * @param listener the listener that will be waiting for the response
     */
    public void launchGETStringRequest(
            final @NonNull String url,
            final @NonNull RequestParams urlParams,
            final JsonResponseType expectedResponseType,
            final @NonNull NetworkRequestListener listener) {

        if (url == null || urlParams == null || listener == null)
            return;

        String urlWithParams = urlParams.addParamsToUrl(url);

        StringRequest getRequest = new StringRequest(
                Request.Method.GET,
                urlWithParams,
                getNewInternalListener(expectedResponseType, listener),
                getNewInternalErrorListener(listener));

        Log.d(LOG_TAG, "Launching [GET] request to URL: "+ url);
        Log.d(LOG_TAG, "[GET] URL params: "+ urlParams.debugString());

        RequestQueue queue = Volley.newRequestQueue( context.get() );
        queue.add(getRequest);
    }


    /**
     * Queues a new POST request through the network.
     *
     * If the request was successful, will call listener.onNetworkRequestSuccess() passing
     * a ParsedData object to the listener (the listener should cast it to the appropriate class).
     *
     * Otherwise, it will call listener.onNetworkRequestFail() passing the error to the listener.
     *
     * @param url the url to address the request
     * @param bodyParams a Map with the parameters to add to the request body
     * @param expectedResponseType the response type expected for the request
     * @param listener the listener that will be waiting for the response
     */
    public void launchPOSTStringRequest(
            final @NonNull String url,
            final @NonNull RequestParams bodyParams,
            final JsonResponseType expectedResponseType,
            final @NonNull NetworkRequestListener listener) {

        if (url == null || bodyParams == null || listener == null)
            return;

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                getNewInternalListener(expectedResponseType, listener),
                getNewInternalErrorListener(listener))
        {
            // This override is necessary in order to pass params to the POST request
            @Override
            protected Map<String,String> getParams() {
                return bodyParams.getParams();
            }
        };

        Log.d(LOG_TAG, "Launching [POST] request to URL: "+ url);
        Log.d(LOG_TAG, "[POST] Body params: "+ bodyParams.debugString());

        RequestQueue queue = Volley.newRequestQueue( context.get() );
        queue.add(postRequest);
    }

    /**
     * Queues a new PUT request through the network.
     *
     * If the request was successful, will call listener.onNetworkRequestSuccess() passing
     * a ParsedData object to the listener (the listener should cast it to the appropriate class).
     *
     * Otherwise, it will call listener.onNetworkRequestFail() passing the error to the listener.
     *
     * @param url the url to address the request
     * @param bodyParams a Map with the parameters to add to the request body
     * @param expectedResponseType the response type expected for the request
     * @param listener the listener that will be waiting for the response
     */
    public void launchPUTStringRequest(
            final @NonNull String url,
            final @NonNull RequestParams bodyParams,
            final JsonResponseType expectedResponseType,
            final @NonNull NetworkRequestListener listener) {

        if (url == null || bodyParams == null || listener == null) {
            return;
        }

        StringRequest putRequest = new StringRequest(
                Request.Method.PUT,
                url,
                getNewInternalListener(expectedResponseType, listener),
                getNewInternalErrorListener(listener))
        {
            // This override is necessary in order to pass params to the PUT request
            @Override
            protected Map<String,String> getParams() {
                return bodyParams.getParams();
            }
        };

        Log.d(LOG_TAG, "Launching [PUT] request to URL: " + url);
        Log.d(LOG_TAG, "[PUT] Body params: "+ bodyParams.debugString());

        RequestQueue queue = Volley.newRequestQueue(context.get());
        queue.add(putRequest);
    }


    // Auxiliary methods:

    // Creates a new custom Volley listener for a String request
    private Response.Listener<String> getNewInternalListener(
            final JsonResponseType expectedResponseType,
            final NetworkRequestListener externalListener) {

        Response.Listener<String> newResponseListener;

        newResponseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, "Response retrieved");

                ParsedResponse parsedResponse = parseStringResponse(response, expectedResponseType);

                if ( parsedResponse != null && parsedResponse.resultIsOK() ) {
                    externalListener.onNetworkRequestSuccess(parsedResponse.getData());
                }
                else {
                    String errorMsg = "NULL parsed response";
                    if (parsedResponse != null)
                        errorMsg = parsedResponse.getData().getErrorCode()
                                +": "+ parsedResponse.getData().getErrorDescription();

                    externalListener.onNetworkRequestFail( new IncorrectResponseException(errorMsg) );
                }
            }
        };

        return newResponseListener;
    }

    // Creates a new custom Volley error listener for a request
    private Response.ErrorListener getNewInternalErrorListener(
            final NetworkRequestListener externalListener) {

        Response.ErrorListener newResponseErrorListener;

        newResponseErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Unable to get response");
                externalListener.onNetworkRequestFail(error);
            }
        };

        return newResponseErrorListener;
    }


    // This method tries to parse a String response according to the given expected response type
    @Nullable
    private ParsedResponse parseStringResponse(String response, JsonResponseType expectedType) {

        if (response != null)
            Log.d(LOG_TAG, "Received response: \n" +response);

        ParsedResponse parsedResponse;

        switch (expectedType) {

            case LOGIN:
                parsedResponse = parseLoginResponse(response);
                break;

            case USER:
                parsedResponse = parseUserResponse(response);
                break;

            case EVENT_LIST:
                parsedResponse = parseEventListResponse(response);
                break;

            case EVENT_DETAIL:
                parsedResponse = parseEventDetailResponse(response);
                break;

            case PUB_DETAIL:
                parsedResponse = parsePubDetailResponse(response);
                break;

            case CATEGORY_LIST:
                parsedResponse = parseCategoryListResponse(response);
                break;

            default:
                parsedResponse = null;
                break;
        }

        if (parsedResponse != null)
            Log.d(LOG_TAG, "Parsed response (expected type '"+ expectedType.toString() +"'): \n"+ parsedResponse.debugString());

        return parsedResponse;
    }


    // Parsing methods for each expected response type:

    // JsonResponseType: LOGIN
    @Nullable
    private LoginResponse parseLoginResponse(String responseString) {

        LoginResponse loginResponse = null;

        try {
            Reader reader = new StringReader(responseString);
            Gson gson = new GsonBuilder().create();
            loginResponse = gson.fromJson(reader, LoginResponse.class);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return loginResponse;
    }

    // JsonResponseType: USER
    @Nullable
    private UserResponse parseUserResponse(String responseString) {

        UserResponse userEntity = null;

        try {
            Reader reader = new StringReader(responseString);
            Gson gson = new GsonBuilder().create();
            userEntity = gson.fromJson(reader, UserResponse.class);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return userEntity;
    }

    // JsonResponseType: EVENT_LIST
    @Nullable
    private EventListResponse parseEventListResponse(String responseString) {

        EventListResponse eventListResponse = null;

        try {
            Reader reader = new StringReader(responseString);
            Gson gson = new GsonBuilder().create();
            eventListResponse = gson.fromJson(reader, EventListResponse.class);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return eventListResponse;
    }

    // JsonResponseType: EVENT_DETAIL
    @Nullable
    private EventDetailResponse parseEventDetailResponse(String responseString) {

        EventDetailResponse eventDetailResponse = null;

        try {
            Reader reader = new StringReader(responseString);
            Gson gson = new GsonBuilder().create();
            eventDetailResponse = gson.fromJson(reader, EventDetailResponse.class);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return eventDetailResponse;
    }

    // JsonResponseType: PUB_DETAIL
    @Nullable
    private PubDetailResponse parsePubDetailResponse(String responseString) {

        PubDetailResponse pubDetailResponse = null;

        try {
            Reader reader = new StringReader(responseString);
            Gson gson = new GsonBuilder().create();
            pubDetailResponse = gson.fromJson(reader, PubDetailResponse.class);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return pubDetailResponse;
    }

    // JsonResponseType: CATEGORY_LIST
    @Nullable
    private CategoryListResponse parseCategoryListResponse(String responseString) {

        CategoryListResponse categoryListResponse = null;

        try {
            Reader reader = new StringReader(responseString);
            Gson gson = new GsonBuilder().create();
            categoryListResponse = gson.fromJson(reader, CategoryListResponse.class);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }

        return categoryListResponse;
    }

}
