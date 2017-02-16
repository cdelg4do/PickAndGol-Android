package io.keepcoding.pickandgol.viewmodel;

import android.content.Context;

import java.io.Serializable;

import io.keepcoding.pickandgol.model.Login;


/**
 * This class represents the view model for a given Login model object.
 * It implements the Serializable interface so that it can be passed inside an Intent.
 */
public class LoginViewModel implements Serializable {

    private Context context;
    private Login mLogin;


    public LoginViewModel(Context context, Login login) {

        this.context = context;
        this.mLogin = login;
    }

}
