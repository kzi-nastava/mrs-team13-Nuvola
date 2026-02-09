package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.AuthApi;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.LoginRequest;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.network.UserTokenState;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ApiClient with context
        ApiClient.init(this);

        // ===== Drawer =====
        setupDrawer();

        // ===== Inputs =====
        initInputs();

        // ===== LOGIN =====
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
        }

        // ===== Links =====
        setupLinks();
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (navView != null && drawerLayout != null) {
            navView.setNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_register) {
                    startActivity(new Intent(this, RegisterActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }

    private void initInputs() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        if (etEmail != null) {
            etEmail.addTextChangedListener(simpleWatcher(() -> {
                if (tilEmail != null) tilEmail.setError(null);
            }));
        }

        if (etPassword != null) {
            etPassword.addTextChangedListener(simpleWatcher(() -> {
                if (tilPassword != null) tilPassword.setError(null);
            }));
        }
    }

    private void setupLinks() {
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));
        }

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(this, ForgotPasswordActivity.class)));
        }
    }

    private void performLogin() {
        if (!validateLogin()) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        Log.d(TAG, "Attempting login for: " + email);

        AuthApi api = ApiClient.getRetrofit().create(AuthApi.class);

        api.login(new LoginRequest(email, password))
                .enqueue(new Callback<UserTokenState>() {
                    @Override
                    public void onResponse(Call<UserTokenState> call,
                                           Response<UserTokenState> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            handleSuccessfulLogin(response.body());
                        } else {
                            handleLoginError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserTokenState> call, Throwable t) {
                        Log.e(TAG, "Login network error", t);
                        Toast.makeText(LoginActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleSuccessfulLogin(UserTokenState userTokenState) {
        String token = userTokenState.getAccessToken();

        Log.d(TAG, "Login successful!");
        Log.d(TAG, "Token received: " + token.substring(0, Math.min(50, token.length())) + "...");

        // Debug the token
        JwtRoleHelper.debugToken(token);

        // Save token
        TokenStorage.saveToken(LoginActivity.this, token);

        // Extract and save user role
        String userType = JwtRoleHelper.getUserType(token);
        Log.d(TAG, "Extracted user type: " + userType);

        TokenStorage.saveUserRole(LoginActivity.this, userType);

        // Navigate to profile
        Intent intent = new Intent(LoginActivity.this,
                com.example.nuvola.activities.DriverRideHistory.class);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(Response<UserTokenState> response) {
        String err = "Unknown error";
        try {
            if (response.errorBody() != null) {
                err = response.errorBody().string();
            }
        } catch (Exception e) {
            err = e.getMessage();
        }

        Log.e(TAG, "Login failed - Code: " + response.code() + " Body: " + err);

        String userMessage;
        switch (response.code()) {
            case 401:
                userMessage = "Invalid email or password";
                break;
            case 403:
                userMessage = "Account not activated or blocked";
                break;
            default:
                userMessage = "Login failed (" + response.code() + ")";
                break;
        }

        Toast.makeText(LoginActivity.this, userMessage, Toast.LENGTH_LONG).show();
    }

    // ===== Validation =====
    private boolean validateLogin() {
        boolean ok = true;

        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            ok = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            ok = false;
        } else if (password.length() < 8) {
            tilPassword.setError("Min 8 characters");
            ok = false;
        } else {
            tilPassword.setError(null);
        }

        return ok;
    }

    private TextWatcher simpleWatcher(Runnable onTextChanged) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTextChanged.run();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }
}
