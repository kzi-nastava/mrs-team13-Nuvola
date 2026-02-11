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
import com.example.nuvola.network.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import dto.ForgotPasswordRequestDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private MaterialButton btnSend;

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
        btnSend = findViewById(R.id.btnSendReset);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        if (etEmail != null) {
            etEmail.addTextChangedListener(simpleWatcher(() -> validateEmail(tilEmail, etEmail)));
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (!validateEmail(tilEmail, etEmail)) return;

                String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                btnSend.setEnabled(false);

                AuthService.api()
                        .forgotPassword(new ForgotPasswordRequestDTO(email))
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                btnSend.setEnabled(true);

                                if (response.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Reset email sent ✅", Toast.LENGTH_SHORT).show();

                                    // Pošto je backend stub, nastavi na Reset ekran (token flow ćete kasnije)
                                    startActivity(new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class));
                                } else {
                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Forgot password failed: " + response.code(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                btnSend.setEnabled(true);
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Network error: " + (t.getMessage() == null ? "unknown" : t.getMessage()),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
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
