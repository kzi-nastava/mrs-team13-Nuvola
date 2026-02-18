package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import dto.ResetPasswordRequestDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private MaterialButton btnReset;
    private String resetToken;

    private TextInputLayout tilNew;
    private TextInputLayout tilConfirm;
    private TextInputEditText etNew;
    private TextInputEditText etConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // =========================
        // 1) TOKEN (deep link ili extra)
        // =========================
        resetToken = extractTokenFromIntent(getIntent());

        // =========================
        // 2) Drawer/menu (kao kod tebe)
        // =========================
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

        // =========================
        // 3) UI reference
        // =========================
        tilNew = findViewById(R.id.tilNewPassword);
        tilConfirm = findViewById(R.id.tilConfirmPassword);
        etNew = findViewById(R.id.etNewPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);

        btnReset = findViewById(R.id.btnResetPassword);
        TextView tvBack = findViewById(R.id.tvBackToLoginFromReset);

        // =========================
        // 4) Validation watchers
        // =========================
        Runnable validate = this::validateAll;

        if (etNew != null) etNew.addTextChangedListener(simpleWatcher(validate));
        if (etConfirm != null) etConfirm.addTextChangedListener(simpleWatcher(validate));

        // =========================
        // 5) Token check -> disable button if missing
        // =========================
        if (btnReset != null) {
            boolean hasToken = resetToken != null && !resetToken.trim().isEmpty();
            btnReset.setEnabled(hasToken);

            if (!hasToken) {
                Toast.makeText(this, "Missing reset token. Open the link from email.", Toast.LENGTH_LONG).show();
            }

            btnReset.setOnClickListener(v -> {
                // token must exist
                if (resetToken == null || resetToken.trim().isEmpty()) {
                    Toast.makeText(this, "Missing reset token. Open the link from email.", Toast.LENGTH_LONG).show();
                    return;
                }

                // validate passwords
                if (!validateAll()) return;

                String newPass = etNew.getText() == null ? "" : etNew.getText().toString();

                btnReset.setEnabled(false);

                AuthService.api()
                        .resetPassword(resetToken, new ResetPasswordRequestDTO(newPass))
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                btnReset.setEnabled(true);

                                if (response.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this,
                                            "Password updated ✅", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    // Ako backend vraća body sa porukom, možeš da pročitaš response.errorBody() (ali ne mora sad)
                                    Toast.makeText(ResetPasswordActivity.this,
                                            "Reset failed: " + response.code(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                btnReset.setEnabled(true);
                                Toast.makeText(ResetPasswordActivity.this,
                                        "Network error: " + (t.getMessage() == null ? "unknown" : t.getMessage()),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }

        if (tvBack != null) {
            tvBack.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    // =========================
    // Extract token helper
    // =========================
    private String extractTokenFromIntent(Intent intent) {
        if (intent == null) return null;

        // 1) deep link: nuvola://reset-password?token=...
        Uri data = intent.getData();
        if (data != null) {
            String t = data.getQueryParameter("token");
            if (t != null && !t.trim().isEmpty()) return t;
        }

        // 2) fallback: normal extra from previous screen
        String extra = intent.getStringExtra("RESET_TOKEN");
        if (extra != null && !extra.trim().isEmpty()) return extra;

        return null;
    }

    // =========================
    // Validation
    // =========================
    private boolean validateAll() {
        if (tilNew == null || tilConfirm == null || etNew == null || etConfirm == null) return false;

        boolean ok = true;

        String p1 = etNew.getText() == null ? "" : etNew.getText().toString();
        String p2 = etConfirm.getText() == null ? "" : etConfirm.getText().toString();

        if (p1.trim().isEmpty()) {
            tilNew.setError("Password is required");
            ok = false;
        } else if (p1.length() < 8) {
            tilNew.setError("Min 8 characters");
            ok = false;
        } else {
            tilNew.setError(null);
        }

        if (p2.trim().isEmpty()) {
            tilConfirm.setError("Please confirm password");
            ok = false;
        } else if (!p1.equals(p2)) {
            tilConfirm.setError("Passwords do not match");
            ok = false;
        } else {
            tilConfirm.setError(null);
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
