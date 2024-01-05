package com.example.aq_bluering;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult;
import com.amplifyframework.auth.cognito.result.GlobalSignOutError;
import com.amplifyframework.auth.cognito.result.HostedUIError;
import com.amplifyframework.auth.cognito.result.RevokeTokenError;
import com.example.aq_bluering.databinding.LoginBinding;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;


public class LoginActivity extends AppCompatActivity {

    private LoginBinding binding;
    private String username = null;
    private String password = null;

    private MutableLiveData<String> loginResponse=new MutableLiveData<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //configure amplify for login authentication
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("AWS Cognito", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("AWS Cognito", "Could not initialize Amplify", error);
        }

        //set login screen to be full screen
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.failText.setVisibility(View.GONE);
        signOut();
        activateSignButton();
        loginResponse.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.equals("success")){
                    // Sign-up was successful
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    finish(); // finish the login activity to prevent going back to it
                } else {
                    binding.failText.setVisibility(View.VISIBLE);
                    activateSignButton();
                }
            }
        });



    }

    private void activateSignButton() {
        Button loginButton = binding.loginButton;
        EditText userNameEntry = binding.username;
        EditText passwordEntry = binding.password;
        loginButton.setEnabled(true);
        loginButton.setClickable(true);
        loginButton.setOnClickListener(v -> {
            binding.failText.setVisibility(View.GONE);
            binding.noUsername.setVisibility(View.GONE);
            binding.noPassword.setVisibility(View.GONE);
            username = userNameEntry.getText().toString();
            password = passwordEntry.getText().toString();

            //avoid multiple click cause crash
            deactivateSignButton();

            // for dev only, remove before submit
/**
             Intent intent = new Intent(LoginActivity.this, MainActivity.class);
             intent.putExtra("USERNAME", "demo");
             startActivity(intent);
             finish();
             */



            // formal code for login authentication, don't delete
            signIn();
        });
    }
    private void checkSession(){
        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyQuickstart", result.toString()),
                error -> Log.e("AmplifyQuickstart", error.toString())
        );
    }
    private void deactivateSignButton() {
        Button loginButton = binding.loginButton;
        loginButton.setEnabled(false);
        loginButton.setClickable(false);
        loginButton.setOnClickListener(null);
    }

    private void signIn() {
        //check if both username and password has entry
        if (username.isEmpty()) {
            binding.noUsername.setVisibility(View.VISIBLE);
            activateSignButton();
        } else if (password.isEmpty()) {
            binding.noPassword.setVisibility(View.VISIBLE);
            activateSignButton();
        } else {
            Amplify.Auth.signIn(
                    username,
                    password,
                    result -> {
                        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isUserLoggedIn", true);
                        editor.apply();
                        loginResponse.postValue("success");

                    },
                    error -> {
                        Log.e("Auth Error", error.toString());
                        loginResponse.postValue("fail");
                        try {
                            throw error;
                        } catch (Exception e) {
                            Log.e("Login fail", e.getMessage());
                        }
                    }
            );


        }
    }

    private void signOut(){

        Amplify.Auth.signOut(signOutResult -> {
            if (signOutResult instanceof AWSCognitoAuthSignOutResult.CompleteSignOut) {
                // Sign Out completed fully and without errors.
                Log.i("AuthQuickStart", "Signed out successfully");
            } else if (signOutResult instanceof AWSCognitoAuthSignOutResult.PartialSignOut) {
                // Sign Out completed with some errors. User is signed out of the device.
                AWSCognitoAuthSignOutResult.PartialSignOut partialSignOutResult =
                        (AWSCognitoAuthSignOutResult.PartialSignOut) signOutResult;

                HostedUIError hostedUIError = partialSignOutResult.getHostedUIError();
                if (hostedUIError != null) {
                    Log.e("AuthQuickStart", "HostedUI Error", hostedUIError.getException());
                    // Optional: Re-launch hostedUIError.getUrl() in a Custom tab to clear Cognito web session.
                }

                GlobalSignOutError globalSignOutError = partialSignOutResult.getGlobalSignOutError();
                if (globalSignOutError != null) {
                    Log.e("AuthQuickStart", "GlobalSignOut Error", globalSignOutError.getException());
                    // Optional: Use escape hatch to retry revocation of globalSignOutError.getAccessToken().
                }

                RevokeTokenError revokeTokenError = partialSignOutResult.getRevokeTokenError();
                if (revokeTokenError != null) {
                    Log.e("AuthQuickStart", "RevokeToken Error", revokeTokenError.getException());
                    // Optional: Use escape hatch to retry revocation of revokeTokenError.getRefreshToken().
                }
            } else if (signOutResult instanceof AWSCognitoAuthSignOutResult.FailedSignOut) {
                AWSCognitoAuthSignOutResult.FailedSignOut failedSignOutResult =
                        (AWSCognitoAuthSignOutResult.FailedSignOut) signOutResult;
                // Sign Out failed with an exception, leaving the user signed in.
                Log.e("AuthQuickStart", "Sign out Failed", failedSignOutResult.getException());
            }
        });
    }
}
