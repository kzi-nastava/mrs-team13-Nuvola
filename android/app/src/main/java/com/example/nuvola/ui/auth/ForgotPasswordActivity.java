package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import android.view.View;
import android.widget.ImageView;


import android.widget.ImageView;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.nuvola.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSend;
    private TextView tvBackToLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


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


        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        View navbar = findViewById(R.id.navbar);


        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.menuLogin).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, LoginActivity.class));
        });

        findViewById(R.id.menuRegister).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, RegisterActivity.class));
        });


        tilEmail = findViewById(R.id.tilEmailReset);
        etEmail = findViewById(R.id.etEmailReset);
        btnSend = findViewById(R.id.btnSendReset);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        etEmail.addTextChangedListener(simpleWatcher(this::validateEmail));

        btnSend.setOnClickListener(v -> {
            if (validateEmail()) {
                // TODO: kasnije: poziv API-ja za reset
                Toast.makeText(this, "Reset link sent ✅ (demo)", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class));

            }
        });

        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateEmail() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            return false;
        }

        tilEmail.setError(null);
        return true;
    }

    private TextWatcher simpleWatcher(Runnable onTextChanged) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChanged.run(); }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

}
