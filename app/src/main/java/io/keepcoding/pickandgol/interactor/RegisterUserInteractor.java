package io.keepcoding.pickandgol.interactor;

import android.content.Context;
import android.support.annotation.NonNull;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.NetworkManager.NetworkRequestListener;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.REGISTER;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_REGISTER_USER;


/**
 * This class is an interactor in charge of:
 *
 * - First (in background): sends a new user request to the remote server, and waits for response.
 * - Second (in the main thread): returns control to the received RegisterUserInteractorListener.
 */
public class RegisterUserInteractor {

    // Param keynames for the register user operation
    private static final String REQUEST_PARAM_KEY_NAME = "name";
    private static final String REQUEST_PARAM_KEY_EMAIL = "email";
    private static final String REQUEST_PARAM_KEY_PASSWORD = "password";

    // This interface describes the behavior of a listener waiting for the the async operation
    public interface RegisterUserInteractorListener {
        void onRegistrationSuccess();
        void onRegistrationFail(Exception e);
    }


    /**
     * Sends the request, gets the response and notifies the listener (fail/success).
     *
     * @param context   context for the operation.
     * @param name      name to show of the new account
     * @param email     email address of the new account
     * @param password  password of the new account
     * @param listener  listener that will process the result of the operation.
     */
    public void execute(final @NonNull Context context,
                        final @NonNull String name,
                        final @NonNull String email,
                        final @NonNull String password,
                        final @NonNull RegisterUserInteractorListener listener) {

        if (listener == null)
            return;

        NetworkManager manager = new NetworkManager(context);
        RequestParams registerParams = new RequestParams();

        registerParams
                .addParam(REQUEST_PARAM_KEY_NAME, name)
                .addParam(REQUEST_PARAM_KEY_EMAIL, email)
                .addParam(REQUEST_PARAM_KEY_PASSWORD, password);

        String remoteUrl = getUrl();

        manager.launchPOSTStringRequest(remoteUrl, registerParams, REGISTER, new NetworkRequestListener() {

            @Override
            public void onNetworkRequestFail(Exception e) {

                listener.onRegistrationFail(e);
            }

            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {

                listener.onRegistrationSuccess();
            }
        });
    }


    // Gets the remote url for the operation
    private String getUrl() {
        return URL_REGISTER_USER;
    }
}
