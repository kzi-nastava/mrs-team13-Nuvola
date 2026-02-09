package com.example.nuvola.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ProfileApi;
import com.example.nuvola.network.DriverProfileApi;
import com.example.nuvola.network.TokenStorage;

import dto.ChangePasswordDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private TextView tvErrorCurrentPassword, tvErrorNewPassword, tvErrorConfirmPassword;
    private Button btnChangePassword;
    private ProfileApi profileApi;
    private DriverProfileApi driverProfileApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmPassword);
        tvErrorCurrentPassword = findViewById(R.id.tvErrorCurrentPassword);
        tvErrorNewPassword = findViewById(R.id.tvErrorNewPassword);
        tvErrorConfirmPassword = findViewById(R.id.tvErrorConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Initialize API based on user type
        if (isDriver()) {
            driverProfileApi = ApiClient.getRetrofit().create(DriverProfileApi.class);
        } else {
            profileApi = ApiClient.getRetrofit().create(ProfileApi.class);
        }

        // On click listener for change password button
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

            if (validateInputs(currentPassword, newPassword, confirmNewPassword)) {
                changePassword(currentPassword, newPassword);
            }
        });
    }

    // Validation for the inputs
    private boolean validateInputs(String currentPassword, String newPassword, String confirmNewPassword) {
        boolean isValid = true;

        // Validate Current Password
        if (TextUtils.isEmpty(currentPassword)) {
            tvErrorCurrentPassword.setText("Current password is required.");
            tvErrorCurrentPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorCurrentPassword.setVisibility(View.GONE);
        }

        // Validate New Password
        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            tvErrorNewPassword.setText("New password must be at least 6 characters.");
            tvErrorNewPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorNewPassword.setVisibility(View.GONE);
        }

        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmNewPassword) || !newPassword.equals(confirmNewPassword)) {
            tvErrorConfirmPassword.setText("Passwords do not match.");
            tvErrorConfirmPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            tvErrorConfirmPassword.setVisibility(View.GONE);
        }

        return isValid;
    }

    // Make API call to change password
    private void changePassword(String currentPassword, String newPassword) {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.currentPassword = currentPassword;
        dto.newPassword = newPassword;

        // Check if it's a driver or regular user
        if (isDriver()) {
            // Call the driver API
            driverProfileApi.changePassword(dto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    handlePasswordChangeResponse(response);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ChangePasswordActivity.this, "Network error. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Call the regular user API
            profileApi.changePassword(dto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    handlePasswordChangeResponse(response);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ChangePasswordActivity.this, "Network error. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Handle the response after changing the password
    private void handlePasswordChangeResponse(Response<Void> response) {
        if (response.isSuccessful()) {
            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully.", Toast.LENGTH_SHORT).show();
            // Log out the user after changing password
            TokenStorage.clear(ChangePasswordActivity.this);
            // Redirect to login screen
            finish();
        } else {
            Toast.makeText(ChangePasswordActivity.this, "Failed to change password.", Toast.LENGTH_SHORT).show();
        }
    }

    // Determine if the user is a driver
    private boolean isDriver() {
        // Add logic to check if the user is a driver (e.g., based on user role stored in shared preferences)
        // For simplicity, we'll assume this method returns true if the user is a driver and false otherwise.
        return TokenStorage.getUserRole(this).equals("DRIVER");
    }
}
