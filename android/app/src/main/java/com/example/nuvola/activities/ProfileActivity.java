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

        // Detect role from TokenStorage
        String userRole = TokenStorage.getUserRole(this);

        // Set UI based on role
        if ("DRIVER".equals(userRole)) {
            isDriver = true;
            showDriverUI();
            loadDriverProfile();
        } else {
            isDriver = false;
            showPassengerUI();
            loadUserProfile();
        }

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

    private void loadDriverProfile() {
        driverProfileApi.getDriverProfile().enqueue(new Callback<DriverProfileResponseDTO>() {
            @Override
            public void onResponse(Call<DriverProfileResponseDTO> call, Response<DriverProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fillDriverProfile(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load driver profile", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<DriverProfileResponseDTO> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void showDriverUI() {
        driverContainer.setVisibility(View.VISIBLE);
        driverContainer.requestLayout();
    }

    private void showPassengerUI() {
        driverContainer.setVisibility(View.GONE);
    }

    private void fillDriverProfile(DriverProfileResponseDTO p) {
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
    }

    private void loadUserProfile() {
        profileApi.getProfile().enqueue(new Callback<ProfileResponseDTO>() {
            @Override
            public void onResponse(Call<ProfileResponseDTO> call, Response<ProfileResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fillUserProfile(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load user profile", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponseDTO> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fillUserProfile(ProfileResponseDTO profile) {
        etFirstName.setText(profile.firstName);
        etLastName.setText(profile.lastName);
        etEmail.setText(profile.email);
        etPhone.setText(profile.phone);
        etAddress.setText(profile.address);

        // Load profile picture if available
        if (profile.picture != null) {
            Glide.with(this)
                    .load("http://10.0.2.2:8080/api/profile/picture/" + profile.picture)
                    .circleCrop()
                    .into(ivProfile);
        }
    }

    // ================= INIT =================

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        findViewById(R.id.ivMenu).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

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

    private void initViews() {
        driverContainer = findViewById(R.id.driverInfoContainer);
        driverContainer.setVisibility(View.GONE);

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
        cbPetFriendly  = findViewById(R.id.cbPetFriendly);

        tvSuccess = findViewById(R.id.tvSuccess);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this,
                        R.array.vehicle_types,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
    }

    private void initApis() {
        profileApi = ApiClient.getRetrofit(this).create(ProfileApi.class);
        driverProfileApi = ApiClient.getRetrofit(this).create(DriverProfileApi.class);
    }

    // ================= LOAD PROFILE =================


    private void setVehicleTypeSpinner(String type) {
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
                        Toast.makeText(ProfileActivity.this, "Request failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "Failed to submit changes", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ProfileActivity.this, "Update failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(ProfileActivity.this, "Failed to save profile", Toast.LENGTH_LONG).show();
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
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            ivProfile.setImageURI(uri);
            uploadImage(uri);
        }
    }

    private void uploadImage(Uri uri) {
        File file = new File(uri.getPath());
        RequestBody req = RequestBody.create(
                file, MediaType.parse("image/*"));

        MultipartBody.Part part =
                MultipartBody.Part.createFormData("file",
                        file.getName(), req);

        profileApi.uploadPicture(part).enqueue(new Callback<PictureResponse>() {
            @Override
            public void onResponse(Call<PictureResponse> call,
                                   Response<PictureResponse> response) {}

            @Override
            public void onFailure(Call<PictureResponse> call, Throwable t) {}
        });
    }

    // ================= VALIDATION =================

    private boolean validate() {

        String firstName = etFirstName.getText().toString().trim();
        String lastName  = etLastName.getText().toString().trim();
        String phone     = etPhone.getText().toString().trim();
        String address   = etAddress.getText().toString().trim();

        // ===== First name =====
        if (firstName.isEmpty()) {
            etFirstName.setError("First name cannot be empty");
            return false;
        }
        if (firstName.length() < 3 || firstName.length() > 255) {
            etFirstName.setError("First name must have at least 3 characters");
            return false;
        }
        if (!firstName.matches("^\\p{Lu}\\p{Ll}+$")) {
            etFirstName.setError(
                    "First name must start with a capital letter and contain only letters"
            );
            return false;
        }

        // ===== Last name =====
        if (lastName.isEmpty()) {
            etLastName.setError("Last name cannot be empty");
            return false;
        }
        if (lastName.length() < 3 || lastName.length() > 255) {
            etLastName.setError("Last name must have at least 3 characters");
            return false;
        }

        if (!lastName.matches("^\\p{Lu}\\p{Ll}+$")) {
            etLastName.setError(
                    "Last name must start with a capital letter and contain only letters"
            );
            return false;
        }

        // ===== Phone =====
        if (phone.isEmpty()) {
            etPhone.setError("Phone number cannot be empty");
            return false;
        }
        if (!phone.matches("^[0-9+\\s]+$")) {
            etPhone.setError("Phone can contain only numbers, + and spaces");
            return false;
        }

        // ===== Address =====
        if (address.isEmpty()) {
            etAddress.setError("Address cannot be empty");
            return false;
        }
        if (address.length() < 3 || address.length() > 255) {
            etAddress.setError("Address must have at least 3 characters");
            return false;
        }
        if (!address.matches("^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$")) {
            etAddress.setError(
                    "Address must start with a capital letter and contain only letters, numbers and spaces"
            );
            return false;
        }

        // ===== DRIVER dodatno (za kasnije) =====
        if (isDriver) {
            String model = etVehicleModel.getText().toString().trim();
            String seats = etSeats.getText().toString().trim();

            if (model.isEmpty() || model.length() < 3) {
                etVehicleModel.setError("Model must have at least 3 characters");
                return false;
            }
            if (!model.matches("^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$")) {
                etVehicleModel.setError(
                        "Model must start with a capital letter"
                );
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
