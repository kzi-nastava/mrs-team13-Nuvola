package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;
import android.widget.TextView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import android.widget.ImageView;
import androidx.appcompat.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private DrawerLayout drawerLayout;
    private NavigationView navView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);

                popup.getMenu().add(0, 1, 1, "Log in");
                popup.getMenu().add(0, 2, 2, "Register");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        // već si na Login-u, ali možemo samo da ostavimo true
                        return true;
                    } else if (item.getItemId() == 2) {
                        startActivity(new Intent(this, RegisterActivity.class));
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }


        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);

        findViewById(R.id.ivMenu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_login) {
                // već si na loginu
            } else if (id == R.id.nav_register) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });





        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Clear errors while typing
        etEmail.addTextChangedListener(simpleWatcher(() -> tilEmail.setError(null)));
        etPassword.addTextChangedListener(simpleWatcher(() -> tilPassword.setError(null)));

        btnLogin.setOnClickListener(v -> {
            if (validateLogin()) {

                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

    }

    private boolean validateLogin() {
        boolean ok = true;

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // EMAIL
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            ok = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            ok = false;
        } else {
            tilEmail.setError(null);
        }

        // PASSWORD
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChanged.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }
}
