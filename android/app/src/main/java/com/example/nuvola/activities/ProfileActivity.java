package com.example.nuvola.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.nuvola.R;
import com.example.nuvola.network.*;
import dto.*;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "PROFILE_DEBUG";
    private static final int PICK_IMAGE = 1;

    private boolean isDriver = false;
    private String userRole;

    ImageView ivProfile;
    EditText etFirstName, etLastName, etEmail, etPhone, etAddress;
    EditText etVehicleModel, etLicensePlates, etSeats;
    CheckBox cbBabyFriendly, cbPetFriendly;
    Spinner spinnerVehicleType;

    TextView tvSuccess;
    ProfileApi profileApi;
    DriverProfileApi driverProfileApi;
    private View driverContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_user);

        Log.d(TAG, "========== ProfileActivity onCreate ==========");

        // CRITICAL: Initialize everything in the correct order
        initViews();
        initApis();
        setupDrawer();

        // Get token and decode it for debugging
        String token = TokenStorage.getToken(this);
        Log.d(TAG, "TOKEN = " + (token != null ? token : "NULL"));

        // Debug the token structure
        if (token != null) {
            JwtRoleHelper.debugToken(token);
        }

        // Get and log the user role
        userRole = TokenStorage.getUserRole(this);
        Log.d(TAG, "USER ROLE from TokenStorage = " + (userRole != null ? userRole : "NULL"));

        // Check what role detection returns
        String roleFromJwt = JwtRoleHelper.getUserType(token);
        Log.d(TAG, "ROLE from JwtRoleHelper = " + roleFromJwt);

        // Set isDriver flag
        isDriver = "DRIVER".equals(userRole);
        Log.d(TAG, "IS DRIVER = " + isDriver);
        Log.d(TAG, "driverContainer = " + driverContainer);

        // Set UI based on role
        if (isDriver) {
            Log.d(TAG, ">>> Setting up DRIVER UI <<<");
            showDriverUI();
            loadDriverProfile();
        } else {
            Log.d(TAG, ">>> Setting up PASSENGER/ADMIN UI <<<");
            showPassengerUI();
            loadUserProfile();
        }

        // Set up button listeners
        findViewById(R.id.ivUpload).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validate()) {
                saveProfile();
            }
        });

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    // ================= INIT =================

    private void initViews() {
        Log.d(TAG, "initViews() called");

        driverContainer = findViewById(R.id.driverInfoContainer);
        if (driverContainer != null) {
            driverContainer.setVisibility(View.GONE);
            Log.d(TAG, "driverContainer initialized and set to GONE");
        } else {
            Log.e(TAG, "ERROR: driverContainer is NULL!");
        }

        ivProfile = findViewById(R.id.ivProfile);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        etVehicleModel = findViewById(R.id.etVehicleModel);
        etLicensePlates = findViewById(R.id.etLicensePlates);
        etSeats = findViewById(R.id.etSeats);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
        cbBabyFriendly = findViewById(R.id.cbBabyFriendly);
        cbPetFriendly = findViewById(R.id.cbPetFriendly);

        tvSuccess = findViewById(R.id.tvSuccess);

        if (spinnerVehicleType != null) {
            ArrayAdapter<CharSequence> adapter =
                    ArrayAdapter.createFromResource(this,
                            R.array.vehicle_types,
                            android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVehicleType.setAdapter(adapter);
        }

        Log.d(TAG, "All views initialized");
    }

    private void initApis() {
        Log.d(TAG, "initApis() called");
        profileApi = ApiClient.getRetrofit().create(ProfileApi.class);
        driverProfileApi = ApiClient.getRetrofit().create(DriverProfileApi.class);
        Log.d(TAG, "APIs initialized");
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        if (drawerLayout == null || navView == null) {
            Log.w(TAG, "Drawer components not found in layout");
            return;
        }

        findViewById(R.id.ivMenu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                TokenStorage.clear(this);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // ================= UI DISPLAY =================

    private void showDriverUI() {
        Log.d(TAG, "showDriverUI() called");
        if (driverContainer != null) {
            Log.d(TAG, "BEFORE: driverContainer visibility = " + driverContainer.getVisibility());
            driverContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "AFTER: driverContainer visibility = " + driverContainer.getVisibility());
            driverContainer.requestLayout();
            Log.d(TAG, "✓ Driver container made VISIBLE");
        } else {
            Log.e(TAG, "✗ ERROR: Cannot show driver UI - driverContainer is NULL!");
        }
    }

    private void showPassengerUI() {
        Log.d(TAG, "showPassengerUI() called");
        if (driverContainer != null) {
            Log.d(TAG, "BEFORE: driverContainer visibility = " + driverContainer.getVisibility());
            driverContainer.setVisibility(View.GONE);
            Log.d(TAG, "AFTER: driverContainer visibility = " + driverContainer.getVisibility());
            Log.d(TAG, "✓ Driver container hidden");
        } else {
            Log.e(TAG, "✗ driverContainer is NULL in showPassengerUI");
        }
    }

    // ================= LOAD PROFILE =================

    private void loadDriverProfile() {
        Log.d(TAG, "loadDriverProfile() - Making API call...");

        driverProfileApi.getDriverProfile().enqueue(new Callback<DriverProfileResponseDTO>() {
            @Override
            public void onResponse(Call<DriverProfileResponseDTO> call, Response<DriverProfileResponseDTO> response) {
                Log.d(TAG, "Driver profile API response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✓ Driver profile loaded successfully");
                    Log.d(TAG, "Driver data: " + response.body().toString());
                    fillDriverProfile(response.body());
                } else {
                    Log.e(TAG, "✗ Failed to load driver profile: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "no error body";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(ProfileActivity.this,
                            "Failed to load driver profile", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DriverProfileResponseDTO> call, Throwable t) {
                Log.e(TAG, "✗ Driver profile load FAILED", t);
                Toast.makeText(ProfileActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fillDriverProfile(DriverProfileResponseDTO p) {
        Log.d(TAG, "fillDriverProfile() called");
        etFirstName.setText(p.firstName);
        etLastName.setText(p.lastName);
        etEmail.setText(p.email);
        etEmail.setEnabled(false);
        etPhone.setText(p.phone);
        etAddress.setText(p.address);

        etVehicleModel.setText(p.model);
        etLicensePlates.setText(p.regNumber);
        etSeats.setText(String.valueOf(p.numOfSeats));

        cbBabyFriendly.setChecked(p.babyFriendly);
        cbPetFriendly.setChecked(p.petFriendly);

        setVehicleTypeSpinner(p.type);

        if (p.picture != null && !p.picture.isEmpty()) {
            Glide.with(this)
                    .load("http://10.0.2.2:8080/api/profile/picture/" + p.picture)
                    .circleCrop()
                    .into(ivProfile);
        }
        Log.d(TAG, "✓ Driver profile fields filled");
    }

    private void loadUserProfile() {
        Log.d(TAG, "loadUserProfile() - Making API call...");

        profileApi.getProfile().enqueue(new Callback<ProfileResponseDTO>() {
            @Override
            public void onResponse(Call<ProfileResponseDTO> call, Response<ProfileResponseDTO> response) {
                Log.d(TAG, "User profile API response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✓ User profile loaded successfully");
                    fillUserProfile(response.body());
                } else {
                    Log.e(TAG, "✗ Failed to load user profile: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "no error body";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Toast.makeText(ProfileActivity.this,
                            "Failed to load user profile", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponseDTO> call, Throwable t) {
                Log.e(TAG, "✗ User profile load FAILED", t);
                Toast.makeText(ProfileActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fillUserProfile(ProfileResponseDTO profile) {
        Log.d(TAG, "fillUserProfile() called");
        etFirstName.setText(profile.firstName);
        etLastName.setText(profile.lastName);
        etEmail.setText(profile.email);
        etEmail.setEnabled(false);
        etPhone.setText(profile.phone);
        etAddress.setText(profile.address);

        if (profile.picture != null) {
            Glide.with(this)
                    .load("http://10.0.2.2:8080/api/profile/picture/" + profile.picture)
                    .circleCrop()
                    .into(ivProfile);
        }
        Log.d(TAG, "✓ User profile fields filled");
    }

    private void setVehicleTypeSpinner(String type) {
        if (spinnerVehicleType == null) return;

        String[] values = getResources().getStringArray(R.array.vehicle_types);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(type)) {
                spinnerVehicleType.setSelection(i);
                break;
            }
        }
    }

    // ================= SAVE =================

    private void saveProfile() {
        if (isDriver) {
            String type = spinnerVehicleType.getSelectedItem().toString().toUpperCase();

            DriverProfileUpdateDTO dto = new DriverProfileUpdateDTO(
                    etFirstName.getText().toString(),
                    etLastName.getText().toString(),
                    etPhone.getText().toString(),
                    etAddress.getText().toString(),
                    etVehicleModel.getText().toString(),
                    type,
                    Integer.parseInt(etSeats.getText().toString()),
                    cbBabyFriendly.isChecked(),
                    cbPetFriendly.isChecked()
            );

            driverProfileApi.requestProfileChange(dto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        tvSuccess.setText("Changes sent for admin approval");
                        tvSuccess.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(ProfileActivity.this,
                                "Request failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this,
                            "Failed to submit changes", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            UpdateProfileDTO dto = new UpdateProfileDTO(
                    etFirstName.getText().toString(),
                    etLastName.getText().toString(),
                    etPhone.getText().toString(),
                    etAddress.getText().toString()
            );

            profileApi.updateProfile(dto).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        tvSuccess.setText("Changes saved!");
                        tvSuccess.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(ProfileActivity.this,
                                "Update failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this,
                            "Failed to save profile", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // ================= IMAGE UPLOAD =================

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ivProfile.setImageURI(uri);
            uploadImage(uri);
        }
    }

    private void uploadImage(Uri uri) {
        File file = new File(uri.getPath());
        RequestBody req = RequestBody.create(file, MediaType.parse("image/*"));

        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), req);

        profileApi.uploadPicture(part).enqueue(new Callback<PictureResponse>() {
            @Override
            public void onResponse(Call<PictureResponse> call, Response<PictureResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Picture uploaded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PictureResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= VALIDATION =================

    private boolean validate() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (firstName.isEmpty()) {
            etFirstName.setError("First name cannot be empty");
            return false;
        }
        if (firstName.length() < 3 || firstName.length() > 255) {
            etFirstName.setError("First name must have at least 3 characters");
            return false;
        }
        if (!firstName.matches("^\\p{Lu}\\p{Ll}+$")) {
            etFirstName.setError("First name must start with a capital letter and contain only letters");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name cannot be empty");
            return false;
        }
        if (lastName.length() < 3 || lastName.length() > 255) {
            etLastName.setError("Last name must have at least 3 characters");
            return false;
        }
        if (!lastName.matches("^\\p{Lu}\\p{Ll}+$")) {
            etLastName.setError("Last name must start with a capital letter and contain only letters");
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number cannot be empty");
            return false;
        }
        if (!phone.matches("^[0-9+\\s]+$")) {
            etPhone.setError("Phone can contain only numbers, + and spaces");
            return false;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address cannot be empty");
            return false;
        }
        if (address.length() < 3 || address.length() > 255) {
            etAddress.setError("Address must have at least 3 characters");
            return false;
        }
        if (!address.matches("^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$")) {
            etAddress.setError("Address must start with a capital letter and contain only letters, numbers and spaces");
            return false;
        }

        if (isDriver) {
            String model = etVehicleModel.getText().toString().trim();
            String seats = etSeats.getText().toString().trim();

            if (model.isEmpty() || model.length() < 3) {
                etVehicleModel.setError("Model must have at least 3 characters");
                return false;
            }
            if (!model.matches("^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$")) {
                etVehicleModel.setError("Model must start with a capital letter");
                return false;
            }

            if (spinnerVehicleType.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Vehicle type is required", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (seats.isEmpty()) {
                etSeats.setError("Number of seats is required");
                return false;
            }

            int numSeats = Integer.parseInt(seats);
            if (numSeats < 4) {
                etSeats.setError("Minimum 4 seats required");
                return false;
            }
        }

        return true;
    }
}
