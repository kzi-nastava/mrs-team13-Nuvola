package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import android.view.View;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;

import android.widget.ImageView;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;

import com.example.nuvola.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilNew, tilConfirm;
    private TextInputEditText etNew, etConfirm;
    private MaterialButton btnReset;
    private TextView tvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
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


        ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        findViewById(R.id.menuLogin).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, LoginActivity.class));
        });

        findViewById(R.id.menuRegister).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, RegisterActivity.class));
        });


        tilNew = findViewById(R.id.tilNewPassword);
        tilConfirm = findViewById(R.id.tilConfirmPassword);
        etNew = findViewById(R.id.etNewPassword);
        etConfirm = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnResetPassword);
        tvBack = findViewById(R.id.tvBackToLoginFromReset);

        etNew.addTextChangedListener(simpleWatcher(this::validateAll));
        etConfirm.addTextChangedListener(simpleWatcher(this::validateAll));

        btnReset.setOnClickListener(v -> {
            if (validateAll()) {
                // TODO: kasnije: poziv API-ja za reset
                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                finish();
            }
        });

        tvBack.setOnClickListener(v -> {
            startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateAll() {
        boolean ok = true;

        String p1 = etNew.getText() == null ? "" : etNew.getText().toString();
        String p2 = etConfirm.getText() == null ? "" : etConfirm.getText().toString();

        // New password
        if (p1.trim().isEmpty()) {
            tilNew.setError("Password is required");
            ok = false;
        } else if (p1.length() < 8) {
            tilNew.setError("Min 8 characters");
            ok = false;
        } else {
            tilNew.setError(null);
        }

        // Confirm
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
