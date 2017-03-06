package io.keepcoding.pickandgol.manager.session;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.securepreferences.SecurePreferences;

import io.keepcoding.pickandgol.model.SessionInfo;


/**
 * This class manages the device's session state.
 * It uses an encrypted preferences xml file to persist the session data.
 */
public class SessionManager {

    // Filename for the encrypted file (in shared_preferences/ )
    public static final String PREFS_FILENAME = "secured_prefs";    //secured_prefs.xml

    private SecurePreferences prefs;
    private SecurePreferences.Editor editor;

    private static SessionManager sharedInstance;   // SessionManager is a singleton

    // Keys for values stored
    private static final String STORED_SESSION = "session_exists";
    private static final String USER_ID_KEY = "id";
    private static final String USER_EMAIL_KEY = "email";
    private static final String USER_NAME_KEY = "name";
    private static final String USER_PHOTO_KEY = "photo";
    private static final String SESSION_TOKEN_KEY = "token";


    // Constructor is private, use getInstance() to get a reference to the manager
    private SessionManager(@NonNull Context context) {

        prefs = new SecurePreferences(context, (String) null, PREFS_FILENAME);
        editor = prefs.edit();
    }


    /**
     * Gets access to the Session Manager.
     *
     * @param context   a context for the manager operations
     * @return          a reference to the Session Manager singleton
     */
    public synchronized static SessionManager getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context (http://bit.ly/6LRzfx)
        if (sharedInstance == null)
            sharedInstance = new SessionManager( context.getApplicationContext() );

        return sharedInstance;
    }


    /**
     * Persists and encrypts the given session information on the local storage.
     *
     * @param sessionInfo an object with the session data to persist
     * @return true if it was able to store an authenticated user session, false in other case
     */
    public boolean storeSession(@NonNull SessionInfo sessionInfo) {

        if ( sessionInfo == null || sessionInfo.isAnonymousSession() )
            return false;

        editor.putString(USER_ID_KEY, sessionInfo.getId());
        editor.putString(USER_EMAIL_KEY, sessionInfo.getEmail());
        editor.putString(USER_NAME_KEY, sessionInfo.getName());
        editor.putString(USER_PHOTO_KEY, sessionInfo.getPhotoUrl());
        editor.putString(SESSION_TOKEN_KEY, sessionInfo.getToken());
        editor.putBoolean(STORED_SESSION, true);

        editor.commit();
        return true;
    }


    /**
     * Removes all existing session data (equivalent to finish the device session).
     */
    public void destroySession() {
        editor.clear();
        editor.commit();
    }


    /**
     * Determines if there is session data stored.
     *
     * @return true if there is session data stored, false in other case
     */
    public boolean hasSessionStored() {
        return prefs.getBoolean(STORED_SESSION, false);
    }


    // Methods to get the session data values:

    public @Nullable String getUserId() {
        return prefs.getString(USER_ID_KEY, null);
    }

    public @Nullable String getUserEmail() {
        return prefs.getString(USER_EMAIL_KEY, null);
    }

    public @Nullable String getUserName() {
        return prefs.getString(USER_NAME_KEY, null);
    }

    public @Nullable String getUserPhotoUrl() {
        return prefs.getString(USER_PHOTO_KEY, null);
    }

    public @Nullable String getSessionToken() {
        return prefs.getString(SESSION_TOKEN_KEY, null);
    }


    // Methods to update some session data values (a session must be already stored):

    public boolean updateUserEmail(@NonNull String newEmail) {

        if ( newEmail == null || !hasSessionStored() )
            return false;

        editor.putString(USER_EMAIL_KEY, newEmail);
        editor.commit();
        return true;
    }

    public boolean updateUserName(@NonNull String newName) {

        if ( newName == null || !hasSessionStored() )
            return false;

        editor.putString(USER_NAME_KEY, newName);
        editor.commit();
        return true;
    }

    public boolean updatePhotoUrl(@NonNull String newUrl) {

        if ( newUrl == null || !hasSessionStored() )
            return false;

        editor.putString(USER_PHOTO_KEY, newUrl);
        editor.commit();
        return true;
    }
}
