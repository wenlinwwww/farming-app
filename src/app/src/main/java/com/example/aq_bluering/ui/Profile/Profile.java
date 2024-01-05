package com.example.aq_bluering.ui.Profile;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult;
import com.amplifyframework.auth.cognito.result.GlobalSignOutError;
import com.amplifyframework.auth.cognito.result.HostedUIError;
import com.amplifyframework.auth.cognito.result.RevokeTokenError;
import com.amplifyframework.core.Amplify;
import com.example.aq_bluering.LoginActivity;
import com.example.aq_bluering.R;
import com.example.aq_bluering.databinding.ProfileFragmentBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Profile extends DialogFragment {

    private ProfileFragmentBinding binding;
    private String modifiedEmailText;
    private boolean isSubmitClicked = false;

    private AuthUserAttribute userEmail;

    private String originalEmail;

    private MutableLiveData<String> addressInfo = new MutableLiveData<>();
    private MutableLiveData<String> phoneInfo = new MutableLiveData<>();
    private MutableLiveData<String> emailInfo = new MutableLiveData<>();
    private MutableLiveData<String> sub_type = new MutableLiveData<>();
    private MutableLiveData<String> sub_expire = new MutableLiveData<>();
    private String username;

    public Profile(String username){
        this.username=username;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = ProfileFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.backButton.setOnClickListener(view -> dismiss());
        EditText phoneText = binding.phoneContent;
        EditText addressText = binding.addressBox;
        EditText emailText = binding.emailBox;
        EditText verifyCode = binding.emailVerify;
        EditText oldPassword = binding.passwordBoxOld;
        EditText newPassword = binding.passwordBoxNew;
        EditText confirmPassword = binding.passwordBoxConfirm;
        AppCompatButton phoneButton = binding.phoneButton;
        AppCompatButton addressButton = binding.addressButton;
        AppCompatButton emailButton = binding.emailButton;
        AppCompatButton passwordButton = binding.passwordButton;
        AppCompatButton logout = binding.logoutButton;
        AppCompatButton resendButton = binding.submitCodeButton;
        TextView oldError = binding.passwordTextOldError;
        TextView noNeed = binding.passwordTextOldNoNeed;
        TextView newError = binding.passwordTextNewError;
        TextView confirmError = binding.passwordTextConfirmError;
        TextView sub_type_text=binding.subscriptionBox;
        binding.usernameBox.setText(username);
        sub_type.observe(getViewLifecycleOwner(), s -> sub_type_text.setText(s));
        sub_expire.observe(getViewLifecycleOwner(), s -> {
            String expire_info="valid until: "+s;
            binding.expiredDataBox.setText(expire_info);
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate inputDate = LocalDate.parse(s, formatter);
                LocalDate currentDate = LocalDate.now();
                long daysDifference = inputDate.toEpochDay()-currentDate.toEpochDay();
                if (daysDifference>=0){
                    binding.expiredDataNotice.setText(String.valueOf(daysDifference)+" days left");
                } else {
                    binding.expiredDataNotice.setText("Already expired "+String.valueOf(daysDifference)+" days");
                }
            } catch (Exception e){
                Log.e("invalid date","check databased value of this date");
            }

        });
        addressInfo.observe(getViewLifecycleOwner(), s -> addressText.setText(s));
        phoneInfo.observe(getViewLifecycleOwner(), s -> phoneText.setText(s));
        emailInfo.observe(getViewLifecycleOwner(), s -> {
            originalEmail = s;
            emailText.setText(s);
        });
        // amplify
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isUserLoggedIn = sharedPref.getBoolean("isUserLoggedIn", false);
        if (isUserLoggedIn) {
            Amplify.Auth.fetchUserAttributes(
                    attributes -> {
                        addressInfo.postValue(getDataFromUserAttributes(attributes, "address"));
                        phoneInfo.postValue(getDataFromUserAttributes(attributes, "phone_number"));
                        emailInfo.postValue(getDataFromUserAttributes(attributes, "email"));
                        sub_type.postValue(getDataFromUserAttributes(attributes, "custom:subscription_level"));
                        sub_expire.postValue(getDataFromUserAttributes(attributes, "custom:sub_expiry_date"));
                    },
                    error -> Log.e("AuthDemo", "Failed to fetch user attributes.", error)
            );
        } else {
            Log.e("AuthDemo", "Failed to check whether login.");
        }

        phoneButton.setOnClickListener(view -> {
            String modifiedPhoneText = phoneText.getText().toString();
            Log.i("phoneCheck", "Phone Text: " + modifiedPhoneText);

            AuthUserAttribute userPhone =
                    new AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), modifiedPhoneText);
            Amplify.Auth.updateUserAttribute(userPhone,
                    result -> Log.i("AuthPhone", "Updated user attribute = " + result.toString()),
                    error -> Log.e("AuthPhone", "Failed to update user attribute.", error)
            );
            phoneText.setText(modifiedPhoneText);
        });

        addressButton.setOnClickListener(view -> {
            String modifiedAddressText = addressText.getText().toString();
            AuthUserAttribute userAddress =
                    new AuthUserAttribute(AuthUserAttributeKey.address(), modifiedAddressText);
            Amplify.Auth.updateUserAttribute(userAddress,
                    result -> Log.i("AuthAddress", "Updated user attribute = " + result.toString()),
                    error -> Log.e("AuthAddress", "Failed to update user attribute.", error)
            );
            addressText.setText(modifiedAddressText);
        });


        resendButton.setOnClickListener(view -> {
            modifiedEmailText = emailText.getText().toString();
            emailText.setText(modifiedEmailText);
            if (!isSubmitClicked) {
                resendButton.setText("Resend code");
                isSubmitClicked = true;
                userEmail = new AuthUserAttribute(AuthUserAttributeKey.email(), modifiedEmailText);

                // update user email
                Amplify.Auth.updateUserAttribute(userEmail,
                        result -> Log.i("AuthEmail", "Updated user attribute = " + result.toString()),
                        error -> Log.e("AuthEmail", "Failed to update user attribute.", error)
                );
            } else {
                Amplify.Auth.resendUserAttributeConfirmationCode(AuthUserAttributeKey.email(),
                        result -> Log.i("AuthResend", "Code was sent again: " + result.toString()),
                        error -> Log.e("AuthResend", "Failed to resend code.", error)
                );
            }
        });

        emailButton.setOnClickListener(view -> {
            String code = verifyCode.getText().toString();
            // confirm user email
            Amplify.Auth.confirmUserAttribute(AuthUserAttributeKey.email(), code,
                    () -> {
                        Log.i("AuthVerify", "Confirmed user attribute with correct code.");
                        runOnUiThread(() -> {
                            verifyCode.setVisibility(View.GONE);
                            resendButton.setVisibility(View.GONE);
                            emailText.setText(modifiedEmailText);
                        });
                    },
                    // when code is not correct
                    error -> {
                        Log.e("AuthVerify", "Failed to confirm user attribute. Bad code?", error);
                        runOnUiThread(() -> {
                            verifyCode.setText("");
                            resendButton.setText("Send code");
                            isSubmitClicked = false;
                            emailText.setText(originalEmail);
                            Toast.makeText(getContext(), "Confirmation code is incorrect.", Toast.LENGTH_SHORT).show();
                            AuthUserAttribute original = new AuthUserAttribute(AuthUserAttributeKey.email(), originalEmail);
                            // update user email
                            Amplify.Auth.updateUserAttribute(original,
                                    result -> Log.i("AuthEmail", "Updated user attribute = " + result.toString()),
                                    updateError -> Log.e("AuthEmail", "Failed to update user attribute.", updateError)
                            );
                        });
                    }
            );
        });

        oldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (newPassword.getText().toString().length() == 0) {
                    noNeed.setVisibility(View.VISIBLE);
                } else {
                    noNeed.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (newPassword.getText().toString().length() != 0 && oldPassword.getText().toString().length() == 0) {
                    oldError.setVisibility(View.VISIBLE);
                } else if (newPassword.getText().toString().length() == 0 && oldPassword.getText().toString().length() != 0) {
                    noNeed.setVisibility(View.VISIBLE);
                } else {
                    noNeed.setVisibility(View.GONE);
                    oldError.setVisibility(View.GONE);
                }
            }
        });

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isPasswordValid(newPassword.getText().toString()) &&
                        oldPassword.getText().toString().length() != 0) {
                    newError.setVisibility(View.VISIBLE);
                    noNeed.setVisibility(View.GONE);
                } else if (oldPassword.getText().toString().length() == 0 &&
                        newPassword.getText().toString().length() != 0) {
                    noNeed.setVisibility(View.GONE);
                    oldError.setVisibility(View.VISIBLE);
                } else {
                    newError.setVisibility(View.GONE);
                    noNeed.setVisibility(View.GONE);
                    oldError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (confirmPassword.getText().toString().length() == 0 || !confirmPassword.getText().toString().equals(newPassword.getText().toString())) {
                    confirmError.setVisibility(View.VISIBLE);
                } else {
                    confirmError.setVisibility(View.GONE);
                }
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (confirmPassword.getText().toString().length() == 0 || !confirmPassword.getText().toString().equals(newPassword.getText().toString())) {
                    confirmError.setVisibility(View.VISIBLE);
                } else {
                    confirmError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        passwordButton.setOnClickListener(view -> {
            String newPasswd = confirmPassword.getText().toString();
            String oldPasswd = oldPassword.getText().toString();

            if (newPasswd.equals(newPassword.getText().toString()) && oldPasswd.length() != 0) {
                Amplify.Auth.updatePassword(
                        oldPasswd,
                        newPasswd,
                        () -> {
                            oldPassword.setText("");
                            newPassword.setText("");
                            confirmPassword.setText("");
                        },
                        error -> Log.e("AuthQuickstart", error.toString())
                );
            }
        });

        // when click logout button
        logout.setOnClickListener(view -> {
            File cacheDir = Objects.requireNonNull(getActivity()).getApplicationContext().getCacheDir();
            File[] cacheFiles = cacheDir.listFiles();
            assert cacheFiles != null;
            for (File cacheFile : cacheFiles) {
                boolean isDeleted = cacheFile.delete(); // delete cache file
                if (isDeleted) {
                    Log.i("DeleteCacheSucc", "Successful delete cache");
                } else {
                    Log.e("DeleteCacheFail", "Fail to delete cache");
                }
            }
            signOut();
            // close dialogFragment
            dismiss();
            // open login page
            Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        });
        return root;
    }

    private String getDataFromUserAttributes(List<AuthUserAttribute> userAttributes, String info) {
        for (AuthUserAttribute attribute : userAttributes) {
            if (attribute.getKey().getKeyString().equals(info)) {
                return attribute.getValue();
            }
        }
        // if not find
        return "";
    }

    private boolean isPasswordValid(String password) {
        // Define a regular expression pattern to match the password requirements
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

        // Use Pattern and Matcher classes to apply the regex pattern
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(password);

        // Return true if the password matches the pattern, indicating it's valid
        return matcher.matches();
    }

    private void signOut() {

        Amplify.Auth.signOut(signOutResult -> {
            Amplify.Auth.fetchAuthSession(
                    result -> Log.i("Sign-out Result", result.toString()),
                    error -> Log.e("Sign-out Result", error.toString())
            );
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

    private void checkSession() {
        Amplify.Auth.fetchAuthSession(
                result -> Log.i("Sign_in_status", result.toString()),
                error -> Log.e("Sign_in_status", error.toString())
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        originalEmail = null;
    }
}