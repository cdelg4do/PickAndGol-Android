package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;
import io.keepcoding.pickandgol.manager.net.response.LoginResponse.LoginData;
import io.keepcoding.pickandgol.model.Login;
import io.keepcoding.pickandgol.model.mapper.LoginDataToLoginMapper;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.LOGIN;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_LOGIN;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): requests a new login into the remote server and builds a new model object with the login info.
 * - Second (in the main thread): pass the model object to the received LoginInteractorListener.
 */
public class LoginInteractor {

    // Param keynames for the login operation
    public static final String REQUEST_PARAM_KEY_EMAIL = "email";
    public static final String REQUEST_PARAM_KEY_PASSWORD = "password";


    // This interface describes the behavior of a listener waiting for the the async operation
    public interface LoginInteractorListener {
        void onLoginSuccess(Login login);
        void onLoginFail(Exception e);
    }

    /**
     * Sends the request, gets the response and then builds a model object with the retrieved data,
     * then passes it to the listener. In case of fail, passes the error exception to the listener.
     *
     * @param context   context for the operation.
     * @param email     email identifying the account
     * @param password  password for the account
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String email,
                        final @NonNull String password,
                        final @NonNull LoginInteractorListener listener) {

        if (listener == null)
            return;

        NetworkManager networkMgr = new NetworkManager(context);
        RequestParams loginParams = new RequestParams();

        loginParams
                .addParam(REQUEST_PARAM_KEY_EMAIL, email)
                .addParam(REQUEST_PARAM_KEY_PASSWORD, password);

        String remoteUrl = getUrl();

        networkMgr.launchPOSTStringRequest(remoteUrl, loginParams, LOGIN, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                Login login = new LoginDataToLoginMapper().map( (LoginData) parsedData );
                listener.onLoginSuccess(login);
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onLoginFail(e);
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_LOGIN;
    }

}
