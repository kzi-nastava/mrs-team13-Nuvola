package com.example.nuvola.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.nuvola.R;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.AuthApi;
import com.example.nuvola.network.DriverProfileApi;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.ProfileApi;
import com.example.nuvola.network.ServerConfig;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.navigation.NavigationView;

import java.io.File;

import dto.ChangeDriverStatusDTO;
import dto.DriverProfileResponseDTO;
import dto.DriverProfileUpdateDTO;
import dto.PictureResponse;
import dto.ProfileResponseDTO;
import dto.UpdateProfileDTO;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG =
            "PROFILE_DEBUG";

    private static final int PICK_IMAGE =
            1;

    private boolean isDriver = false;
    private boolean updatingAvailabilityUi = false;

    private String userRole;

    private ImageView ivProfile;

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etAddress;

    private EditText etVehicleModel;
    private EditText etLicensePlates;
    private EditText etSeats;

    private CheckBox cbBabyFriendly;
    private CheckBox cbPetFriendly;

    private Spinner spinnerVehicleType;

    private TextView tvSuccess;
    private TextView tvDriverStatus;

    private Switch switchDriverAvailability;

    private AuthApi authApi;
    private ProfileApi profileApi;
    private DriverProfileApi driverProfileApi;

    private View driverContainer;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.profile_user
        );

        initViews();
        initApis();
        setupDrawer();

        String token =
                TokenStorage.getToken(this);

        Log.d(
                TAG,
                "TOKEN = "
                        + (token != null
                        ? token
                        : "NULL")
        );

        if (token != null) {
            JwtRoleHelper.debugToken(token);
        }

        userRole =
                TokenStorage.getUserRole(this);

        Log.d(
                TAG,
                "USER ROLE from TokenStorage = "
                        + (userRole != null
                        ? userRole
                        : "NULL")
        );

        isDriver =
                "DRIVER".equalsIgnoreCase(
                        userRole
                );

        if (isDriver) {
            showDriverUI();
            setupDriverAvailabilityListener();
            loadDriverProfile();

        } else {
            showPassengerUI();
            loadUserProfile();
        }

        View uploadButton =
                findViewById(R.id.ivUpload);

        if (uploadButton != null) {
            uploadButton.setOnClickListener(
                    view -> openGallery()
            );
        }

        View saveButton =
                findViewById(R.id.btnSave);

        if (saveButton != null) {
            saveButton.setOnClickListener(
                    view -> {
                        if (validate()) {
                            saveProfile();
                        }
                    }
            );
        }

        View changePasswordButton =
                findViewById(
                        R.id.btnChangePassword
                );

        if (changePasswordButton != null) {
            changePasswordButton.setOnClickListener(
                    view -> {
                        Intent intent =
                                new Intent(
                                        ProfileActivity.this,
                                        ChangePasswordActivity.class
                                );

                        startActivity(intent);
                    }
            );
        }
    }

    private void initViews() {
        driverContainer =
                findViewById(
                        R.id.driverInfoContainer
                );

        if (driverContainer != null) {
            driverContainer.setVisibility(
                    View.GONE
            );

            Log.d(
                    TAG,
                    "driverContainer initialized and set to GONE"
            );

        } else {
            Log.e(
                    TAG,
                    "ERROR: driverContainer is NULL!"
            );
        }

        ivProfile =
                findViewById(R.id.ivProfile);

        etFirstName =
                findViewById(R.id.etFirstName);

        etLastName =
                findViewById(R.id.etLastName);

        etEmail =
                findViewById(R.id.etEmail);

        etPhone =
                findViewById(R.id.etPhone);

        etAddress =
                findViewById(R.id.etAddress);

        etVehicleModel =
                findViewById(
                        R.id.etVehicleModel
                );

        etLicensePlates =
                findViewById(
                        R.id.etLicensePlates
                );

        etSeats =
                findViewById(R.id.etSeats);

        spinnerVehicleType =
                findViewById(
                        R.id.spinnerVehicleType
                );

        cbBabyFriendly =
                findViewById(
                        R.id.cbBabyFriendly
                );

        cbPetFriendly =
                findViewById(
                        R.id.cbPetFriendly
                );

        switchDriverAvailability =
                findViewById(
                        R.id.switchDriverAvailability
                );

        tvDriverStatus =
                findViewById(
                        R.id.tvDriverStatus
                );

        tvSuccess =
                findViewById(R.id.tvSuccess);

        if (spinnerVehicleType != null) {
            ArrayAdapter<CharSequence> adapter =
                    ArrayAdapter.createFromResource(
                            this,
                            R.array.vehicle_types,
                            android.R.layout
                                    .simple_spinner_item
                    );

            adapter.setDropDownViewResource(
                    android.R.layout
                            .simple_spinner_dropdown_item
            );

            spinnerVehicleType.setAdapter(
                    adapter
            );
        }

        Log.d(
                TAG,
                "All views initialized"
        );
    }

    private void initApis() {
        Log.d(
                TAG,
                "initApis() called"
        );

        authApi =
                ApiClient.getRetrofit()
                        .create(AuthApi.class);

        profileApi =
                ApiClient.getRetrofit()
                        .create(ProfileApi.class);

        driverProfileApi =
                ApiClient.getRetrofit()
                        .create(
                                DriverProfileApi.class
                        );

        Log.d(
                TAG,
                "APIs initialized"
        );
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout =
                findViewById(
                        R.id.drawerLayout
                );

        NavigationView navigationView =
                findViewById(
                        R.id.navView
                );

        View menuButton =
                findViewById(R.id.ivMenu);

        if (drawerLayout == null
                || navigationView == null) {

            Log.w(
                    TAG,
                    "Drawer components not found in layout"
            );

            return;
        }

        if (menuButton != null) {
            menuButton.setOnClickListener(
                    view -> drawerLayout.openDrawer(
                            GravityCompat.START
                    )
            );
        }

        NavigationMenuManager.setup(
                this,
                drawerLayout,
                navigationView
        );
    }

    private void showDriverUI() {
        Log.d(
                TAG,
                "showDriverUI() called"
        );

        if (driverContainer != null) {
            Log.d(
                    TAG,
                    "BEFORE: driverContainer visibility = "
                            + driverContainer.getVisibility()
            );

            driverContainer.setVisibility(
                    View.VISIBLE
            );

            Log.d(
                    TAG,
                    "AFTER: driverContainer visibility = "
                            + driverContainer.getVisibility()
            );

            driverContainer.requestLayout();

            Log.d(
                    TAG,
                    "Driver container made VISIBLE"
            );

        } else {
            Log.e(
                    TAG,
                    "Cannot show driver UI - driverContainer is NULL!"
            );
        }
    }

    private void showPassengerUI() {
        Log.d(
                TAG,
                "showPassengerUI() called"
        );

        if (driverContainer != null) {
            driverContainer.setVisibility(
                    View.GONE
            );

            Log.d(
                    TAG,
                    "Driver container hidden"
            );

        } else {
            Log.e(
                    TAG,
                    "driverContainer is NULL in showPassengerUI"
            );
        }
    }

    private void setupDriverAvailabilityListener() {
        if (switchDriverAvailability == null) {
            return;
        }

        switchDriverAvailability
                .setOnCheckedChangeListener(
                        (buttonView, isChecked) -> {
                            if (!updatingAvailabilityUi) {
                                changeDriverAvailability(
                                        isChecked
                                );
                            }
                        }
                );
    }

    private void loadDriverProfile() {
        Log.d(
                TAG,
                "loadDriverProfile() - Making API call..."
        );

        driverProfileApi.getDriverProfile()
                .enqueue(
                        new Callback<DriverProfileResponseDTO>() {

                            @Override
                            public void onResponse(
                                    Call<DriverProfileResponseDTO> call,
                                    Response<DriverProfileResponseDTO> response
                            ) {
                                Log.d(
                                        TAG,
                                        "Driver profile API response code: "
                                                + response.code()
                                );

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    Log.d(
                                            TAG,
                                            "Driver profile loaded successfully"
                                    );

                                    fillDriverProfile(
                                            response.body()
                                    );

                                } else {
                                    Log.e(
                                            TAG,
                                            "Failed to load driver profile: "
                                                    + response.code()
                                    );

                                    try {
                                        String errorBody =
                                                response.errorBody() != null
                                                        ? response.errorBody()
                                                        .string()
                                                        : "no error body";

                                        Log.e(
                                                TAG,
                                                "Error body: "
                                                        + errorBody
                                        );

                                    } catch (Exception exception) {
                                        Log.e(
                                                TAG,
                                                "Error reading error body",
                                                exception
                                        );
                                    }

                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Failed to load driver profile",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<DriverProfileResponseDTO> call,
                                    Throwable throwable
                            ) {
                                Log.e(
                                        TAG,
                                        "Driver profile load failed",
                                        throwable
                                );

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Error: "
                                                + getThrowableMessage(
                                                throwable
                                        ),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void fillDriverProfile(
            DriverProfileResponseDTO profile
    ) {
        Log.d(
                TAG,
                "fillDriverProfile() called"
        );

        etFirstName.setText(
                profile.firstName
        );

        etLastName.setText(
                profile.lastName
        );

        etEmail.setText(
                profile.email
        );

        etEmail.setEnabled(false);

        etPhone.setText(
                profile.phone
        );

        etAddress.setText(
                profile.address
        );

        etVehicleModel.setText(
                profile.model
        );

        etLicensePlates.setText(
                profile.regNumber
        );

        etSeats.setText(
                String.valueOf(
                        profile.numOfSeats
                )
        );

        cbBabyFriendly.setChecked(
                profile.babyFriendly
        );

        cbPetFriendly.setChecked(
                profile.petFriendly
        );

        updateDriverAvailabilityUi(
                profile.status,
                profile.inactiveAfterCurrentRide
        );

        setVehicleTypeSpinner(
                profile.type
        );

        if (profile.picture != null
                && !profile.picture.isEmpty()) {

            Glide.with(this)
                    .load(
                            ServerConfig.BASE_URL + "api/profile/picture/"
                                    + profile.picture
                    )
                    .circleCrop()
                    .into(ivProfile);
        }

        View blockedBanner =
                findViewById(
                        R.id.blockedBanner
                );

        TextView blockingReasonText =
                findViewById(
                        R.id.tvBlockingReason
                );

        if (blockedBanner != null) {
            blockedBanner.setVisibility(
                    profile.blocked
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        if (blockingReasonText != null) {
            if (profile.blocked
                    && profile.blockingReason != null
                    && !profile.blockingReason
                    .trim()
                    .isEmpty()) {

                blockingReasonText.setText(
                        "Reason: "
                                + profile.blockingReason
                );

                blockingReasonText.setVisibility(
                        View.VISIBLE
                );

            } else {
                blockingReasonText.setVisibility(
                        View.GONE
                );
            }
        }

        Log.d(
                TAG,
                "Driver profile fields filled"
        );
    }

    private void loadUserProfile() {
        Log.d(
                TAG,
                "loadUserProfile() - Making API call..."
        );

        profileApi.getProfile()
                .enqueue(
                        new Callback<ProfileResponseDTO>() {

                            @Override
                            public void onResponse(
                                    Call<ProfileResponseDTO> call,
                                    Response<ProfileResponseDTO> response
                            ) {
                                Log.d(
                                        TAG,
                                        "User profile API response code: "
                                                + response.code()
                                );

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    fillUserProfile(
                                            response.body()
                                    );

                                } else {
                                    Log.e(
                                            TAG,
                                            "Failed to load user profile: "
                                                    + response.code()
                                    );

                                    try {
                                        String errorBody =
                                                response.errorBody() != null
                                                        ? response.errorBody()
                                                        .string()
                                                        : "no error body";

                                        Log.e(
                                                TAG,
                                                "Error body: "
                                                        + errorBody
                                        );

                                    } catch (Exception exception) {
                                        Log.e(
                                                TAG,
                                                "Error reading error body",
                                                exception
                                        );
                                    }

                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Failed to load user profile",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<ProfileResponseDTO> call,
                                    Throwable throwable
                            ) {
                                Log.e(
                                        TAG,
                                        "User profile load failed",
                                        throwable
                                );

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Error: "
                                                + getThrowableMessage(
                                                throwable
                                        ),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void fillUserProfile(
            ProfileResponseDTO profile
    ) {
        Log.d(
                TAG,
                "fillUserProfile() called"
        );

        etFirstName.setText(
                profile.firstName
        );

        etLastName.setText(
                profile.lastName
        );

        etEmail.setText(
                profile.email
        );

        etEmail.setEnabled(false);

        etPhone.setText(
                profile.phone
        );

        etAddress.setText(
                profile.address
        );

        if (profile.picture != null
                && !profile.picture.isEmpty()) {

            Glide.with(this)
                    .load(
                            ServerConfig.BASE_URL + "api/profile/picture/"
                                    + profile.picture
                    )
                    .circleCrop()
                    .into(ivProfile);
        }

        View blockedBanner =
                findViewById(
                        R.id.blockedBanner
                );

        TextView blockingReasonText =
                findViewById(
                        R.id.tvBlockingReason
                );

        if (blockedBanner != null) {
            blockedBanner.setVisibility(
                    profile.blocked
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        if (blockingReasonText != null) {
            if (profile.blocked
                    && profile.blockingReason != null
                    && !profile.blockingReason
                    .trim()
                    .isEmpty()) {

                blockingReasonText.setText(
                        "Reason: "
                                + profile.blockingReason
                );

                blockingReasonText.setVisibility(
                        View.VISIBLE
                );

            } else {
                blockingReasonText.setVisibility(
                        View.GONE
                );
            }
        }

        Log.d(
                TAG,
                "User profile fields filled"
        );
    }

    private void setVehicleTypeSpinner(
            String type
    ) {
        if (spinnerVehicleType == null
                || type == null) {

            return;
        }

        String[] values =
                getResources()
                        .getStringArray(
                                R.array.vehicle_types
                        );

        for (int index = 0;
             index < values.length;
             index++) {

            if (values[index].equalsIgnoreCase(
                    type
            )) {
                spinnerVehicleType.setSelection(
                        index
                );

                break;
            }
        }
    }

    private void updateDriverAvailabilityUi(
            String status,
            boolean inactiveAfterCurrentRide
    ) {
        String displayStatus =
                status == null
                        ? "UNKNOWN"
                        : status;

        boolean checked =
                "ACTIVE".equalsIgnoreCase(
                        displayStatus
                )
                        || (
                        "BUSY".equalsIgnoreCase(
                                displayStatus
                        )
                                && !inactiveAfterCurrentRide
                );

        updatingAvailabilityUi = true;

        if (switchDriverAvailability != null) {
            switchDriverAvailability.setChecked(
                    checked
            );

            if ("BUSY".equalsIgnoreCase(
                    displayStatus
            ) && inactiveAfterCurrentRide) {

                switchDriverAvailability.setText(
                        "Available after current ride: no"
                );

            } else {
                switchDriverAvailability.setText(
                        checked
                                ? "Available for rides"
                                : "Not available for rides"
                );
            }
        }

        updatingAvailabilityUi = false;

        if (tvDriverStatus != null) {
            String suffix =
                    inactiveAfterCurrentRide
                            ? " (inactive after current ride)"
                            : "";

            tvDriverStatus.setText(
                    "Status: "
                            + displayStatus
                            + suffix
            );
        }
    }

    private void changeDriverAvailability(
            boolean available
    ) {
        if (authApi == null) {
            return;
        }

        String nextStatus =
                available
                        ? "ACTIVE"
                        : "INACTIVE";

        if (switchDriverAvailability != null) {
            switchDriverAvailability.setEnabled(
                    false
            );
        }

        authApi.changeDriverStatus(
                new ChangeDriverStatusDTO(
                        nextStatus
                )
        ).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {
                        if (switchDriverAvailability != null) {
                            switchDriverAvailability.setEnabled(
                                    true
                            );
                        }

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Driver status changed",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadDriverProfile();

                        } else {
                            updateDriverAvailabilityUi(
                                    available
                                            ? "INACTIVE"
                                            : "ACTIVE",
                                    false
                            );

                            Toast.makeText(
                                    ProfileActivity.this,
                                    "Status change failed ("
                                            + response.code()
                                            + ")",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable throwable
                    ) {
                        if (switchDriverAvailability != null) {
                            switchDriverAvailability.setEnabled(
                                    true
                            );
                        }

                        updateDriverAvailabilityUi(
                                available
                                        ? "INACTIVE"
                                        : "ACTIVE",
                                false
                        );

                        Toast.makeText(
                                ProfileActivity.this,
                                "Network error: "
                                        + getThrowableMessage(
                                        throwable
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void saveProfile() {
        if (isDriver) {
            saveDriverProfile();

        } else {
            savePassengerProfile();
        }
    }

    private void saveDriverProfile() {
        if (spinnerVehicleType == null
                || spinnerVehicleType.getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Vehicle type is required",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String type =
                spinnerVehicleType
                        .getSelectedItem()
                        .toString()
                        .toUpperCase();

        int seats;

        try {
            seats =
                    Integer.parseInt(
                            etSeats.getText()
                                    .toString()
                                    .trim()
                    );

        } catch (NumberFormatException exception) {
            etSeats.setError(
                    "Enter a valid number"
            );

            return;
        }

        DriverProfileUpdateDTO dto =
                new DriverProfileUpdateDTO(
                        etFirstName.getText()
                                .toString()
                                .trim(),
                        etLastName.getText()
                                .toString()
                                .trim(),
                        etPhone.getText()
                                .toString()
                                .trim(),
                        etAddress.getText()
                                .toString()
                                .trim(),
                        etVehicleModel.getText()
                                .toString()
                                .trim(),
                        type,
                        seats,
                        cbBabyFriendly.isChecked(),
                        cbPetFriendly.isChecked()
                );

        driverProfileApi.requestProfileChange(dto)
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {
                                if (response.isSuccessful()) {
                                    tvSuccess.setText(
                                            "Changes sent for admin approval"
                                    );

                                    tvSuccess.setVisibility(
                                            View.VISIBLE
                                    );

                                } else {
                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Request failed ("
                                                    + response.code()
                                                    + ")",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {
                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Failed to submit changes",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void savePassengerProfile() {
        UpdateProfileDTO dto =
                new UpdateProfileDTO(
                        etFirstName.getText()
                                .toString()
                                .trim(),
                        etLastName.getText()
                                .toString()
                                .trim(),
                        etPhone.getText()
                                .toString()
                                .trim(),
                        etAddress.getText()
                                .toString()
                                .trim()
                );

        profileApi.updateProfile(dto)
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {
                                if (response.isSuccessful()) {
                                    tvSuccess.setText(
                                            "Changes saved!"
                                    );

                                    tvSuccess.setVisibility(
                                            View.VISIBLE
                                    );

                                } else {
                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Update failed ("
                                                    + response.code()
                                                    + ")",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {
                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Failed to save profile",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void openGallery() {
        Intent intent =
                new Intent(
                        Intent.ACTION_PICK
                );

        intent.setType(
                "image/*"
        );

        startActivityForResult(
                intent,
                PICK_IMAGE
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != PICK_IMAGE
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {

            return;
        }

        Uri uri =
                data.getData();

        ivProfile.setImageURI(uri);

        uploadImage(uri);
    }

    private void uploadImage(
            Uri uri
    ) {
        if (uri == null) {
            return;
        }

        String path =
                uri.getPath();

        if (path == null) {
            Toast.makeText(
                    this,
                    "Selected image could not be read.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        File file =
                new File(path);

        RequestBody requestBody =
                RequestBody.create(
                        file,
                        MediaType.parse(
                                "image/*"
                        )
                );

        MultipartBody.Part part =
                MultipartBody.Part.createFormData(
                        "file",
                        file.getName(),
                        requestBody
                );

        profileApi.uploadPicture(part)
                .enqueue(
                        new Callback<PictureResponse>() {

                            @Override
                            public void onResponse(
                                    Call<PictureResponse> call,
                                    Response<PictureResponse> response
                            ) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Picture uploaded",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {
                                    Toast.makeText(
                                            ProfileActivity.this,
                                            "Picture upload failed ("
                                                    + response.code()
                                                    + ")",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PictureResponse> call,
                                    Throwable throwable
                            ) {
                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Upload failed",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private boolean validate() {
        String firstName =
                etFirstName.getText()
                        .toString()
                        .trim();

        String lastName =
                etLastName.getText()
                        .toString()
                        .trim();

        String phone =
                etPhone.getText()
                        .toString()
                        .trim();

        String address =
                etAddress.getText()
                        .toString()
                        .trim();

        if (firstName.isEmpty()) {
            etFirstName.setError(
                    "First name cannot be empty"
            );

            return false;
        }

        if (firstName.length() < 3
                || firstName.length() > 255) {

            etFirstName.setError(
                    "First name must have at least 3 characters"
            );

            return false;
        }

        if (!firstName.matches(
                "^\\p{Lu}\\p{Ll}+$"
        )) {
            etFirstName.setError(
                    "First name must start with a capital letter "
                            + "and contain only letters"
            );

            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError(
                    "Last name cannot be empty"
            );

            return false;
        }

        if (lastName.length() < 3
                || lastName.length() > 255) {

            etLastName.setError(
                    "Last name must have at least 3 characters"
            );

            return false;
        }

        if (!lastName.matches(
                "^\\p{Lu}\\p{Ll}+$"
        )) {
            etLastName.setError(
                    "Last name must start with a capital letter "
                            + "and contain only letters"
            );

            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError(
                    "Phone number cannot be empty"
            );

            return false;
        }

        if (!phone.matches(
                "^[0-9+\\s]+$"
        )) {
            etPhone.setError(
                    "Phone can contain only numbers, + and spaces"
            );

            return false;
        }

        if (address.isEmpty()) {
            etAddress.setError(
                    "Address cannot be empty"
            );

            return false;
        }

        if (address.length() < 3
                || address.length() > 255) {

            etAddress.setError(
                    "Address must have at least 3 characters"
            );

            return false;
        }

        if (!address.matches(
                "^[A-ZČĆŠĐŽ]"
                        + "[A-Za-zČĆŠĐŽčćšđž0-9\\s]+$"
        )) {
            etAddress.setError(
                    "Address must start with a capital letter and "
                            + "contain only letters, numbers and spaces"
            );

            return false;
        }

        if (isDriver) {
            return validateDriverFields();
        }

        return true;
    }

    private boolean validateDriverFields() {
        String model =
                etVehicleModel.getText()
                        .toString()
                        .trim();

        String seatsText =
                etSeats.getText()
                        .toString()
                        .trim();

        if (model.isEmpty()
                || model.length() < 3) {

            etVehicleModel.setError(
                    "Model must have at least 3 characters"
            );

            return false;
        }

        if (!model.matches(
                "^[A-ZČĆŠĐŽ]"
                        + "[A-Za-zČĆŠĐŽčćšđž0-9\\s]+$"
        )) {
            etVehicleModel.setError(
                    "Model must start with a capital letter"
            );

            return false;
        }

        if (spinnerVehicleType == null
                || spinnerVehicleType
                .getSelectedItem() == null) {

            Toast.makeText(
                    this,
                    "Vehicle type is required",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (seatsText.isEmpty()) {
            etSeats.setError(
                    "Number of seats is required"
            );

            return false;
        }

        int numberOfSeats;

        try {
            numberOfSeats =
                    Integer.parseInt(
                            seatsText
                    );

        } catch (NumberFormatException exception) {
            etSeats.setError(
                    "Enter a valid number"
            );

            return false;
        }

        if (numberOfSeats < 4) {
            etSeats.setError(
                    "Minimum 4 seats required"
            );

            return false;
        }

        return true;
    }

    private String getThrowableMessage(
            Throwable throwable
    ) {
        if (throwable == null
                || throwable.getMessage() == null
                || throwable.getMessage()
                .trim()
                .isEmpty()) {

            return "Unknown network error";
        }

        return throwable.getMessage();
    }
}