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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        TextView menuLogin = findViewById(R.id.menuLogin);
        if (menuLogin != null && drawerLayout != null) {
            menuLogin.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, LoginActivity.class));
            });
        }

        TextView menuRegister = findViewById(R.id.menuRegister);
        if (menuRegister != null && drawerLayout != null) {
            menuRegister.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }

        TextInputLayout tilEmail = findViewById(R.id.tilEmailReset);
        TextInputEditText etEmail = findViewById(R.id.etEmailReset);
        MaterialButton btnSend = findViewById(R.id.btnSendReset);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        if (etEmail != null) {
            etEmail.addTextChangedListener(simpleWatcher(() -> validateEmail(tilEmail, etEmail)));
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (validateEmail(tilEmail, etEmail)) {
                    Toast.makeText(this, "Reset link sent ✅ (demo)", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, ResetPasswordActivity.class));
                }
            });
        }

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    private boolean validateEmail(TextInputLayout tilEmail, TextInputEditText etEmail) {
        if (tilEmail == null || etEmail == null) return false;

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
