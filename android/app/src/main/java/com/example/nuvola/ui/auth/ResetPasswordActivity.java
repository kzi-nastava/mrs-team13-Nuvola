package com.example.nuvola.ui.auth;

import android.content.Intent;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

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

        TextInputLayout tilNew = findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirm = findViewById(R.id.tilConfirmPassword);
        TextInputEditText etNew = findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = findViewById(R.id.etConfirmPassword);
        MaterialButton btnReset = findViewById(R.id.btnResetPassword);
        TextView tvBack = findViewById(R.id.tvBackToLoginFromReset);

        Runnable validate = () -> validateAll(tilNew, tilConfirm, etNew, etConfirm);

        if (etNew != null) etNew.addTextChangedListener(simpleWatcher(validate));
        if (etConfirm != null) etConfirm.addTextChangedListener(simpleWatcher(validate));

        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                if (validateAll(tilNew, tilConfirm, etNew, etConfirm)) {
                    Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            });
        }

        if (tvBack != null) {
            tvBack.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
    }

    private boolean validateAll(
            TextInputLayout tilNew,
            TextInputLayout tilConfirm,
            TextInputEditText etNew,
            TextInputEditText etConfirm
    ) {
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
