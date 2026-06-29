package com.example.nuvola.ui.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.activities.RideOrderActivity;
import com.example.nuvola.activities.StartRideActivity;
import com.example.nuvola.activities.UsersActivity;
import com.example.nuvola.activities.VehicleMapActivity;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.AuthApi;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.LoginRequest;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.network.UserTokenState;
import com.example.nuvola.services.DriverLocationPublisherService;
import com.example.nuvola.services.StompNotificationService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int NOTIFICATION_PERMISSION_REQUEST = 100;
    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private CheckBox cbRemember;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ApiClient.init(this);

        requestNotificationPermission();
        requestLocationPermission();

        setupDrawer();
        setupLogo();
        initInputs();
        setupLoginButton();
        setupLinks();

        restoreRememberedLogin();
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout =
                findViewById(R.id.drawerLayout);

        NavigationView navigationView =
                findViewById(R.id.navView);

        ImageView menuButton =
                findViewById(R.id.ivMenu);

        if (menuButton != null && drawerLayout != null) {
            menuButton.setOnClickListener(
                    view -> drawerLayout.openDrawer(
                            GravityCompat.START
                    )
            );
        }

        if (navigationView != null && drawerLayout != null) {
            navigationView.setNavigationItemSelectedListener(
                    item -> {
                        int id = item.getItemId();

                        if (id == R.id.nav_register) {
                            startActivity(
                                    new Intent(
                                            LoginActivity.this,
                                            RegisterActivity.class
                                    )
                            );
                        }

                        drawerLayout.closeDrawer(
                                GravityCompat.START
                        );

                        return true;
                    }
            );
        }
    }

    private void setupLogo() {
        ImageView logo =
                findViewById(R.id.ivLogo);

        if (logo != null) {
            logo.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    LoginActivity.this,
                                    VehicleMapActivity.class
                            )
                    )
            );
        }
    }

    private void initInputs() {
        tilEmail =
                findViewById(R.id.tilEmail);

        tilPassword =
                findViewById(R.id.tilPassword);

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        cbRemember =
                findViewById(R.id.cbRemember);

        boolean rememberMe =
                TokenStorage.isRememberMeEnabled(this);

        if (cbRemember != null) {
            cbRemember.setChecked(rememberMe);
        }

        if (rememberMe && etEmail != null) {
            etEmail.setText(
                    TokenStorage.getRememberedEmail(this)
            );
        }

        if (etEmail != null) {
            etEmail.addTextChangedListener(
                    simpleWatcher(() -> {
                        if (tilEmail != null) {
                            tilEmail.setError(null);
                        }
                    })
            );
        }

        if (etPassword != null) {
            etPassword.addTextChangedListener(
                    simpleWatcher(() -> {
                        if (tilPassword != null) {
                            tilPassword.setError(null);
                        }
                    })
            );
        }
    }

    private void setupLoginButton() {
        loginButton =
                findViewById(R.id.btnLogin);

        if (loginButton != null) {
            loginButton.setOnClickListener(
                    view -> performLogin()
            );
        }
    }

    private void setupLinks() {
        TextView registerLink =
                findViewById(R.id.tvGoToRegister);

        if (registerLink != null) {
            registerLink.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    LoginActivity.this,
                                    RegisterActivity.class
                            )
                    )
            );
        }

        TextView forgotPasswordLink =
                findViewById(R.id.tvForgotPassword);

        if (forgotPasswordLink != null) {
            forgotPasswordLink.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    LoginActivity.this,
                                    ForgotPasswordActivity.class
                            )
                    )
            );
        }
    }

    private void restoreRememberedLogin() {
        boolean rememberMe =
                TokenStorage.isRememberMeEnabled(this);

        String token =
                TokenStorage.getToken(this);

        if (!rememberMe
                || token == null
                || token.trim().isEmpty()) {

            return;
        }

        String savedEmail =
                TokenStorage.getUserEmail(this);

        if (savedEmail == null
                || savedEmail.trim().isEmpty()) {

            savedEmail =
                    TokenStorage.getRememberedEmail(this);

            if (savedEmail != null
                    && !savedEmail.trim().isEmpty()) {

                TokenStorage.saveUserEmail(
                        this,
                        savedEmail
                );
            }
        }

        startNotificationService();

        if ("DRIVER".equalsIgnoreCase(
                TokenStorage.getUserRole(this)
        )) {
            startLocationService();
        }

        navigateAfterLogin();
    }

    private void performLogin() {
        if (!validateLogin()) {
            return;
        }

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString();

        setLoading(true);

        Log.d(
                TAG,
                "Attempting login for: " + email
        );

        AuthApi authApi =
                ApiClient.getRetrofit()
                        .create(AuthApi.class);

        authApi.login(
                new LoginRequest(
                        email,
                        password
                )
        ).enqueue(
                new Callback<UserTokenState>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<UserTokenState> call,
                            @NonNull Response<UserTokenState> response
                    ) {
                        setLoading(false);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            handleSuccessfulLogin(
                                    response.body(),
                                    email
                            );

                        } else {
                            handleLoginError(response);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<UserTokenState> call,
                            @NonNull Throwable throwable
                    ) {
                        setLoading(false);

                        Log.e(
                                TAG,
                                "Login network error",
                                throwable
                        );

                        Toast.makeText(
                                LoginActivity.this,
                                "Network error: "
                                        + throwable.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void handleSuccessfulLogin(
            UserTokenState userTokenState,
            String email
    ) {
        String token =
                userTokenState.getAccessToken();

        if (token == null
                || token.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "The server did not return an access token.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Log.d(TAG, "Login successful");

        JwtRoleHelper.debugToken(token);

        String userType =
                JwtRoleHelper.getUserType(token);

        if (userType == null
                || userType.trim().isEmpty()) {

            userType = "UNKNOWN";
        }

        Log.d(
                TAG,
                "Extracted user type: " + userType
        );

        TokenStorage.saveToken(
                this,
                token
        );

        TokenStorage.saveUserRole(
                this,
                userType
        );

        TokenStorage.saveUserEmail(
                this,
                email
        );

        boolean rememberMe =
                cbRemember != null
                        && cbRemember.isChecked();

        TokenStorage.saveRememberMe(
                this,
                rememberMe,
                email
        );

        startNotificationService();

        if ("DRIVER".equalsIgnoreCase(userType)) {
            startLocationService();
        }

        navigateAfterLogin();
    }

    private void navigateAfterLogin() {
        String role =
                TokenStorage.getUserRole(this);

        Class<?> destination;

        if ("ADMIN".equalsIgnoreCase(role)) {
            destination = UsersActivity.class;

        } else if ("DRIVER".equalsIgnoreCase(role)) {
            destination = StartRideActivity.class;

        } else {
            destination = RideOrderActivity.class;
        }

        Intent intent =
                new Intent(
                        LoginActivity.this,
                        destination
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.TIRAMISU) {

            return;
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    NOTIFICATION_PERMISSION_REQUEST
            );
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }

    private void startNotificationService() {
        Intent serviceIntent =
                new Intent(
                        this,
                        StompNotificationService.class
                );

        try {
            ContextCompat.startForegroundService(
                    this,
                    serviceIntent
            );
        } catch (Exception exception) {
            Log.e(
                    TAG,
                    "Notification service could not be started",
                    exception
            );
        }
    }

    private void startLocationService() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        Intent serviceIntent =
                new Intent(
                        this,
                        DriverLocationPublisherService.class
                );

        try {
            ContextCompat.startForegroundService(
                    this,
                    serviceIntent
            );
        } catch (Exception exception) {
            Log.e(
                    TAG,
                    "Location service could not be started",
                    exception
            );
        }
    }

    private void handleLoginError(
            Response<UserTokenState> response
    ) {
        String errorBody = "Unknown error";

        try {
            if (response.errorBody() != null) {
                errorBody =
                        response.errorBody().string();
            }

        } catch (Exception exception) {
            errorBody =
                    exception.getMessage();
        }

        Log.e(
                TAG,
                "Login failed - Code: "
                        + response.code()
                        + " Body: "
                        + errorBody
        );

        String userMessage;

        switch (response.code()) {
            case 401:
                userMessage =
                        "Invalid email or password";
                break;

            case 403:
                userMessage =
                        "Account not activated or blocked";
                break;

            default:
                userMessage =
                        "Login failed ("
                                + response.code()
                                + ")";
                break;
        }

        Toast.makeText(
                this,
                userMessage,
                Toast.LENGTH_LONG
        ).show();
    }

    private boolean validateLogin() {
        boolean valid = true;

        String email =
                etEmail == null
                        || etEmail.getText() == null
                        ? ""
                        : etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword == null
                        || etPassword.getText() == null
                        ? ""
                        : etPassword.getText()
                        .toString();

        if (email.isEmpty()) {
            if (tilEmail != null) {
                tilEmail.setError(
                        "Email is required"
                );
            }

            valid = false;

        } else if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            if (tilEmail != null) {
                tilEmail.setError(
                        "Invalid email"
                );
            }

            valid = false;

        } else if (tilEmail != null) {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            if (tilPassword != null) {
                tilPassword.setError(
                        "Password is required"
                );
            }

            valid = false;

        } else if (password.length() < 8) {
            if (tilPassword != null) {
                tilPassword.setError(
                        "Minimum 8 characters"
                );
            }

            valid = false;

        } else if (tilPassword != null) {
            tilPassword.setError(null);
        }

        return valid;
    }

    private void setLoading(
            boolean loading
    ) {
        if (loginButton == null) {
            return;
        }

        loginButton.setEnabled(!loading);

        loginButton.setText(
                loading
                        ? "Logging in..."
                        : "Login"
        );
    }

    private TextWatcher simpleWatcher(
            Runnable onTextChanged
    ) {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                onTextChanged.run();
            }

            @Override
            public void afterTextChanged(
                    Editable editable
            ) {
            }
        };
    }
}