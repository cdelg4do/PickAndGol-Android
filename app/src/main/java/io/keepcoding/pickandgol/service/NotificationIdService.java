package io.keepcoding.pickandgol.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.keepcoding.pickandgol.interactor.UpdateUserInfoInteractor;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.User;


public class NotificationIdService extends FirebaseInstanceIdService {
    private static final String TAG = NotificationIdService.class.getCanonicalName();

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        SessionManager sm = SessionManager.getInstance(this);
        User user = new User(null);
        user.setRegistrationToken(refreshedToken);

        UpdateUserInfoInteractor interactor = new UpdateUserInfoInteractor();
        interactor.execute(this, sm.getSessionToken(), user, new UpdateUserInfoInteractor.UpdateUserInfoInteractorListener() {
            @Override
            public void onUpdateUserSuccess(User user) {

            }

            @Override
            public void onUpdateUserFail(Exception e) {

            }
        });

        // get the users who have the pub as favorite

        // get the registration token of every user

        // send the notification
    }
}
