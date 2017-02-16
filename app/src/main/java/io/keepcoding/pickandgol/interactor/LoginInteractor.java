package io.keepcoding.pickandgol.interactor;

import android.content.Context;

import java.util.Map;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.response.LoginResponse;
import io.keepcoding.pickandgol.manager.net.response.ParsedResponse;
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

    public static final String REQUEST_PARAM_KEY_EMAIL = "email";
    public static final String REQUEST_PARAM_KEY_PASSWORD = "password";


    // This interface describes the behavior of a listener waiting for the completion of the async operation
    public interface LoginInteractorListener {

        void onLoginSuccess(Login login);
        void onLoginFail(Exception e);
    }

    /**
     * Sends the request, gets the response and then builds an object with the retrieved data.
     *
     * @param context   context for the operation.
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final Context context, Map<String,String> loginParams, final LoginInteractorListener listener) {

        if (listener == null)
            return;

        NetworkManager networkMgr = new NetworkManager(context);

        networkMgr.launchPOSTStringRequest(URL_LOGIN, loginParams, LOGIN, new NetworkManager.NetworkRequestListener() {

            @Override
            public void onNetworkRequestSuccess(ParsedResponse.ParsedData parsedData) {

                LoginResponse.LoginData loginData = (LoginResponse.LoginData) parsedData;
                Login login = new LoginDataToLoginMapper().map(loginData);
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                listener.onLoginFail(e);
            }
        });
    }

}
