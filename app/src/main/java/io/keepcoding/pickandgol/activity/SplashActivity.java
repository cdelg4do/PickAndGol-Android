package io.keepcoding.pickandgol.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import io.keepcoding.pickandgol.navigator.Navigator;


/**
 * This class represents the splash activity shown after the application launches.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Navigator.fromSplashActivityToMainActivity(this);
    }
}
