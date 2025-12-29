package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.activities.DriverRideHistory;
import com.example.nuvola.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (navView != null && drawerLayout != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_login) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true; // ništa, već si na loginu
                }

                if (id == R.id.nav_register) {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);

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

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                if (validateLogin()) {
                    Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, DriverRideHistory.class));
                    finish();
                }
            });
        }

        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        if (tvGoToRegister != null) {
            tvGoToRegister.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
            );
        }

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
            );
        }
    }

    private boolean validateLogin() {
        boolean ok = true;

        String email = (etEmail == null || etEmail.getText() == null) ? "" : etEmail.getText().toString().trim();
        String password = (etPassword == null || etPassword.getText() == null) ? "" : etPassword.getText().toString();

        if (tilEmail != null) {
            if (email.isEmpty()) {
                tilEmail.setError("Email is required");
                ok = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError("Invalid email");
                ok = false;
            } else {
                tilEmail.setError(null);
            }
        } else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ok = false;
        }

        if (tilPassword != null) {
            if (password.isEmpty()) {
                tilPassword.setError("Password is required");
                ok = false;
            } else if (password.length() < 8) {
                tilPassword.setError("Min 8 characters");
                ok = false;
            } else {
                tilPassword.setError(null);
            }
        } else if (password.isEmpty() || password.length() < 8) {
            ok = false;
        }

        return ok;
    }

    private TextWatcher simpleWatcher(Runnable onTextChanged) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChanged.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }
}
