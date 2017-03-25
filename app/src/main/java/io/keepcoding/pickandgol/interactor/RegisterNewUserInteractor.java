package io.keepcoding.pickandgol.interactor;

import android.content.Context;

import io.keepcoding.pickandgol.manager.net.NetworkManager;
import io.keepcoding.pickandgol.manager.net.ParsedData;
import io.keepcoding.pickandgol.manager.net.RequestParams;

import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.JsonResponseType.REGISTER;
import static io.keepcoding.pickandgol.manager.net.NetworkManagerSettings.URL_REGISTER_USER;

public class RegisterNewUserInteractor {
    public interface Listener {
        void onSuccess();
        void onFail(final String message);
    }

    public void execute(Context context, final String name, final String email, final String password, final Listener listener) {
        NetworkManager manager = new NetworkManager(context);
        RequestParams params = new RequestParams();
        params.addParam("name", name);
        params.addParam("email", email);
        params.addParam("password", password);

        manager.launchPOSTStringRequest(URL_REGISTER_USER, params, REGISTER, new NetworkManager.NetworkRequestListener() {
            @Override
            public void onNetworkRequestSuccess(ParsedData parsedData) {
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onNetworkRequestFail(Exception e) {
                if (listener != null) {
                    listener.onFail(e.getMessage());
                }
            }
        });
    }
}
