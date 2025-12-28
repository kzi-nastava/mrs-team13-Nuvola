package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

import com.example.nuvola.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilAddress, tilPhone;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etAddress, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        ImageView ivMenu = findViewById(R.id.ivMenu);

        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, v);

                popup.getMenu().add(0, 1, 1, "Log in");
                popup.getMenu().add(0, 2, 2, "Register");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        startActivity(new Intent(this, LoginActivity.class));
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


        ivMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        findViewById(R.id.menuLogin).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, LoginActivity.class));
        });

        findViewById(R.id.menuRegister).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });



        bindViews();

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> onRegister());

        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // da ne ostaje Register u back-stacku
        });



    }

    private void bindViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilAddress = findViewById(R.id.tilAddress);
        tilPhone = findViewById(R.id.tilPhone);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
    }

    private void onRegister() {
        clearErrors();

        String email = text(etEmail);
        String pass = text(etPassword);
        String confirm = text(etConfirmPassword);

        boolean ok = true;

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            ok = false;
        }
        if (pass.length() < 8) {
            tilPassword.setError("Min 8 characters");
            ok = false;
        }
        if (!pass.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            ok = false;
        }

        if (!ok) return;

        Toast.makeText(this, "Registration sent. Check email for activation link (valid 24h).", Toast.LENGTH_SHORT).show();
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilAddress.setError(null);
        tilPhone.setError(null);
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
