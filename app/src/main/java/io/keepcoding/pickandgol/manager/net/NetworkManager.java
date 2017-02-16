package io.keepcoding.pickandgol.manager.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import java.net.URLEncoder;
import java.util.Map;

import io.keepcoding.pickandgol.manager.net.response.LoginResponse;
import io.keepcoding.pickandgol.manager.net.response.ParsedResponse;
import io.keepcoding.pickandgol.manager.net.response.UserResponse;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType;


/**
 * This class manages the app network operations:
 * sending requests, parsing JSON responses and detecting the connection status
 * (does not include image requests).
 */
public class NetworkManager {

    private WeakReference<Context> context;

    public NetworkManager(final @NonNull Context context) {
        this.context = new WeakReference<>(context);
    }

    /**
     * This interface describes a listener waiting for the completion of the network operation.
     *
     * onNetworkRequestSuccess() should be called only if the JSON request was parsed successfully
     * and the 'result' field value was OK. Otherwise, call onNetworkRequestFail().
     *
     */
    public interface NetworkRequestListener {

        void onNetworkRequestSuccess(ParsedResponse.ParsedData parsedData); // Cast to appropriate subclass
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
     * Determines the device's current connection type (static method).
     *
     * @param context a context for the operation.
     * @return the type of internet connection
     */
    public static ConnectionType getInternetConnectionType(final @NonNull Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        if (activeNetworkInfo.isConnectedOrConnecting()) {

            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return ConnectionType.WIFI;
            else
                return ConnectionType.OTHER;
        }
        else {
            return ConnectionType.NONE;
        }
    }


    /**
     * Queues a new GET request through the network.
     *
     * @param url the url to address the request
     * @param urlParams a Map with the parameters to add to the request url
     * @param expectedResponseType the response type expected for the request
     * @param listener the listener that will be waiting for the response
     */
    public void launchGETStringRequest(
            final @NonNull String url,
            final @NonNull Map<String,String> urlParams,
            final JsonResponseType expectedResponseType,
            final @NonNull NetworkRequestListener listener) {

        if (url == null || urlParams == null || listener == null)
            return;

        String urlWithParams = addParamsToUrl(url, urlParams);

        StringRequest getRequest = new StringRequest(
                Request.Method.GET,
                urlWithParams,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParsedResponse parsedResponse = parseStringResponse(response, expectedResponseType);

                        if ( parsedResponse != null && parsedResponse.wasSuccessful() ) {
                            listener.onNetworkRequestSuccess(parsedResponse.getData());
                        }
                        else {
                            String errorMsg = parsedResponse.getData().getErrorCode() +": "+ parsedResponse.getData().getErrorDescription();
                            listener.onNetworkRequestFail(new IncorrectResponseException(errorMsg));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onNetworkRequestFail(error);
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue( context.get() );
        queue.add(getRequest);
    }


    /**
     * Queues a new POST request through the network.
     *
     * @param url the url to address the request
     * @param bodyParams a Map with the parameters to add to the request body
     * @param expectedResponseType the response type expected for the request
     * @param listener the listener that will be waiting for the response
     */
    public void launchPOSTStringRequest(
            final @NonNull String url,
            final @NonNull Map<String,String> bodyParams,
            final JsonResponseType expectedResponseType,
            final @NonNull NetworkRequestListener listener) {

        if (url == null || bodyParams == null || listener == null)
            return;

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ParsedResponse parsedResponse = parseStringResponse(response, expectedResponseType);

                        if ( parsedResponse != null && parsedResponse.wasSuccessful() ) {
                            listener.onNetworkRequestSuccess(parsedResponse.getData());
                        }
                        else {
                            String errorMsg = "NULL parsed response";
                            if (parsedResponse != null)
                                errorMsg = parsedResponse.getData().getErrorCode() +": "+ parsedResponse.getData().getErrorDescription();

                            listener.onNetworkRequestFail( new IncorrectResponseException(errorMsg) );
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onNetworkRequestFail(error);
                    }
                }
        )
        {
            @Override
            protected Map<String,String> getParams(){
                return bodyParams;
            }
        };

        RequestQueue queue = Volley.newRequestQueue( context.get() );
        queue.add(postRequest);
    }


    // Auxiliary methods:

    // Add the parameters in the map to a given url
    private String addParamsToUrl(final @NonNull String url, final @Nullable Map<String,String> urlParams) {

        StringBuilder strBuilder;

        if (urlParams == null || urlParams.size() == 0)
            return url;

        try {
            strBuilder = new StringBuilder(url);
            strBuilder.append("?");

            for (Map.Entry<String,String> entry : urlParams.entrySet()) {
                String paramKey = entry.getKey();
                String paramValue = URLEncoder.encode(entry.getValue(), "UTF-8");

                strBuilder.append(paramKey +"="+ paramValue +"&");
            }

            strBuilder.setLength(strBuilder.length()-1);    // to remove the last '&'
        }
        catch (Exception e) {
            return url;
        }

        return strBuilder.toString();
    }

    // This method tries to parse a String response according to the given expected response type
    @Nullable
    private ParsedResponse parseStringResponse(String response, JsonResponseType expectedType) {

        switch (expectedType) {

            case LOGIN:
                return parseLoginResponse(response);

            case USER:
                return parseUserResponse(response);

            default:
                return null;
        }
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

}
