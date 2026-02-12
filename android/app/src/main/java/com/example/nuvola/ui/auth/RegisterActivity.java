package com.example.nuvola.ui.auth;

import android.content.Intent;
import android.os.Bundle;
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

import dto.RegisterRequestDTO;
import dto.RegisterResponseDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilFirstName, tilLastName, tilAddress, tilPhone;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etFirstName, etLastName, etAddress, etPhone;

    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
            menuRegister.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        bindViews();

        btnRegister = findViewById(R.id.btnRegister);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> onRegister());
        }

        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        if (tvGoToLogin != null) {
            tvGoToLogin.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            });
        }
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

        String firstName = text(etFirstName);
        String lastName  = text(etLastName);
        String address   = text(etAddress);
        String phone     = text(etPhone);

        String email     = text(etEmail);
        String pass      = text(etPassword);
        String confirm   = text(etConfirmPassword);

        boolean ok = true;

        if (firstName.isEmpty()) { tilFirstName.setError("First name is required"); ok = false; }
        if (lastName.isEmpty())  { tilLastName.setError("Last name is required"); ok = false; }
        if (address.isEmpty())   { tilAddress.setError("Address is required"); ok = false; }

        if (phone.isEmpty()) {
            tilPhone.setError("Phone is required");
            ok = false;
        } else if (!phone.matches("^[0-9+\\-\\s]{6,20}$")) {
            tilPhone.setError("Invalid phone");
            ok = false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email");
            ok = false;
        }

        if (pass.trim().length() < 8) {
            tilPassword.setError("Min 8 characters");
            ok = false;
        }

        if (!pass.equals(confirm)) {
            tilConfirmPassword.setError("Passwords do not match");
            ok = false;
        }

        if (!ok) return;

        if (btnRegister != null) btnRegister.setEnabled(false);


        RegisterRequestDTO body = new RegisterRequestDTO(
                email,
                pass,
                confirm,
                firstName,
                lastName,
                address,
                phone,
                null // picture
        );

        AuthService.api().register(body).enqueue(new Callback<RegisterResponseDTO>() {
            @Override
            public void onResponse(Call<RegisterResponseDTO> call, Response<RegisterResponseDTO> response) {
                if (btnRegister != null) btnRegister.setEnabled(true);

                if (response.isSuccessful()) {
                    String msg = "Registration sent. Check email for activation link (valid 24h).";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }

                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();


                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Register failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponseDTO> call, Throwable t) {
                if (btnRegister != null) btnRegister.setEnabled(true);

                Toast.makeText(RegisterActivity.this,
                        "Network error: " + (t.getMessage() == null ? "unknown" : t.getMessage()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearErrors() {
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);
        if (tilFirstName != null) tilFirstName.setError(null);
        if (tilLastName != null) tilLastName.setError(null);
        if (tilAddress != null) tilAddress.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
    }

    private String text(TextInputEditText et) {
        return (et == null || et.getText() == null) ? "" : et.getText().toString().trim();
    }
}
