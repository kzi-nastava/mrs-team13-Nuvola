package com.example.nuvola.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.nuvola.R;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.DriverApi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import dto.CreateDriverDTO;
import dto.CreatedDriverDTO;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterDriverActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageView profileImage;
    private ImageView uploadImageButton;

    private TextInputLayout firstNameLayout;
    private TextInputLayout lastNameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout addressLayout;
    private TextInputLayout modelLayout;
    private TextInputLayout registrationLayout;
    private TextInputLayout seatsLayout;
    private TextInputLayout vehicleTypeLayout;

    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressInput;
    private TextInputEditText modelInput;
    private TextInputEditText registrationInput;
    private TextInputEditText seatsInput;

    private AutoCompleteTextView vehicleTypeInput;

    private CheckBox babyFriendlyCheckBox;
    private CheckBox petFriendlyCheckBox;

    private MaterialButton createAccountButton;
    private ProgressBar progressBar;
    private TextView successMessage;

    private DriverApi driverApi;

    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri == null) {
                            return;
                        }

                        selectedImageUri = uri;

                        Glide.with(RegisterDriverActivity.this)
                                .load(uri)
                                .circleCrop()
                                .into(profileImage);
                    }
            );

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        driverApi = ApiClient
                .getRetrofit()
                .create(DriverApi.class);

        bindViews();
        setupDrawer();
        setupVehicleTypeDropdown();
        setupListeners();
    }

    private void bindViews() {
        drawerLayout =
                findViewById(R.id.drawerLayout);

        navigationView =
                findViewById(R.id.navView);

        profileImage =
                findViewById(R.id.ivDriverProfile);

        uploadImageButton =
                findViewById(R.id.ivUploadPicture);

        firstNameLayout =
                findViewById(R.id.layoutFirstName);

        lastNameLayout =
                findViewById(R.id.layoutLastName);

        emailLayout =
                findViewById(R.id.layoutEmail);

        phoneLayout =
                findViewById(R.id.layoutPhone);

        addressLayout =
                findViewById(R.id.layoutAddress);

        modelLayout =
                findViewById(R.id.layoutModel);

        registrationLayout =
                findViewById(
                        R.id.layoutRegistrationNumber
                );

        seatsLayout =
                findViewById(R.id.layoutSeats);

        vehicleTypeLayout =
                findViewById(R.id.layoutVehicleType);

        firstNameInput =
                findViewById(R.id.etFirstName);

        lastNameInput =
                findViewById(R.id.etLastName);

        emailInput =
                findViewById(R.id.etEmail);

        phoneInput =
                findViewById(R.id.etPhone);

        addressInput =
                findViewById(R.id.etAddress);

        modelInput =
                findViewById(R.id.etVehicleModel);

        registrationInput =
                findViewById(
                        R.id.etRegistrationNumber
                );

        seatsInput =
                findViewById(R.id.etSeats);

        vehicleTypeInput =
                findViewById(R.id.actVehicleType);

        babyFriendlyCheckBox =
                findViewById(R.id.cbBabyFriendly);

        petFriendlyCheckBox =
                findViewById(R.id.cbPetFriendly);

        createAccountButton =
                findViewById(R.id.btnCreateDriver);

        progressBar =
                findViewById(R.id.progressCreateDriver);

        successMessage =
                findViewById(R.id.tvSuccessMessage);
    }

    private void setupDrawer() {
        View menuButton =
                findViewById(R.id.ivMenu);

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

    private void setupVehicleTypeDropdown() {
        String[] vehicleTypes = {
                "STANDARD",
                "LUXURY",
                "VAN"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        vehicleTypes
                );

        vehicleTypeInput.setAdapter(adapter);

        vehicleTypeInput.setText(
                "STANDARD",
                false
        );
    }

    private void setupListeners() {
        uploadImageButton.setOnClickListener(
                view -> imagePickerLauncher.launch(
                        "image/*"
                )
        );

        profileImage.setOnClickListener(
                view -> imagePickerLauncher.launch(
                        "image/*"
                )
        );

        createAccountButton.setOnClickListener(
                view -> submitDriver()
        );
    }

    private void submitDriver() {
        clearErrors();

        successMessage.setVisibility(
                View.GONE
        );

        if (!validateForm()) {
            return;
        }

        CreateDriverDTO request =
                createRequest();

        setLoading(true);

        driverApi.createDriver(request)
                .enqueue(
                        new Callback<CreatedDriverDTO>() {

                            @Override
                            public void onResponse(
                                    Call<CreatedDriverDTO> call,
                                    Response<CreatedDriverDTO> response
                            ) {
                                if (!response.isSuccessful()
                                        || response.body() == null) {

                                    setLoading(false);

                                    handleCreateDriverError(
                                            response
                                    );

                                    return;
                                }

                                CreatedDriverDTO createdDriver =
                                        response.body();

                                if (selectedImageUri != null
                                        && createdDriver.id != null) {

                                    uploadDriverPicture(
                                            createdDriver.id
                                    );

                                } else {
                                    showSuccess();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<CreatedDriverDTO> call,
                                    Throwable throwable
                            ) {
                                setLoading(false);

                                String message =
                                        throwable == null
                                                || throwable.getMessage() == null
                                                ? "Unknown network error"
                                                : throwable.getMessage();

                                Toast.makeText(
                                        RegisterDriverActivity.this,
                                        "Network error: "
                                                + message,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private CreateDriverDTO createRequest() {
        CreateDriverDTO dto =
                new CreateDriverDTO();

        dto.email =
                textOf(emailInput);

        dto.firstName =
                textOf(firstNameInput);

        dto.lastName =
                textOf(lastNameInput);

        dto.phone =
                textOf(phoneInput);

        dto.address =
                textOf(addressInput);

        dto.picture = "";

        dto.model =
                textOf(modelInput);

        dto.type =
                vehicleTypeInput
                        .getText()
                        .toString()
                        .trim();

        dto.regNumber =
                textOf(registrationInput)
                        .toUpperCase(
                                Locale.ROOT
                        );

        dto.numOfSeats =
                Integer.parseInt(
                        textOf(seatsInput)
                );

        dto.babyFriendly =
                babyFriendlyCheckBox.isChecked();

        dto.petFriendly =
                petFriendlyCheckBox.isChecked();

        return dto;
    }

    private boolean validateForm() {
        boolean valid = true;

        String firstName =
                textOf(firstNameInput);

        String lastName =
                textOf(lastNameInput);

        String email =
                textOf(emailInput);

        String phone =
                textOf(phoneInput);

        String address =
                textOf(addressInput);

        String model =
                textOf(modelInput);

        String registration =
                textOf(registrationInput)
                        .toUpperCase(
                                Locale.ROOT
                        );

        String seatsText =
                textOf(seatsInput);

        String vehicleType =
                vehicleTypeInput
                        .getText()
                        .toString()
                        .trim();

        String namePattern =
                "^[A-ZČĆŠĐŽ][a-zčćšđž]+$";

        String phonePattern =
                "^[0-9+\\s]+$";

        String addressPattern =
                "^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$";

        String modelPattern =
                "^[A-ZČĆŠĐŽ][A-Za-zČĆŠĐŽčćšđž0-9\\s]+$";

        String registrationPattern =
                "^[A-Z]{2}-\\d{3,4}-[A-Z]{2}$";

        if (firstName.isEmpty()) {
            firstNameLayout.setError(
                    "First name is required"
            );

            valid = false;

        } else if (!firstName.matches(
                namePattern
        )) {
            firstNameLayout.setError(
                    "Start with a capital letter and use only letters"
            );

            valid = false;

        } else if (firstName.length() < 3) {
            firstNameLayout.setError(
                    "First name must contain at least 3 characters"
            );

            valid = false;
        }

        if (lastName.isEmpty()) {
            lastNameLayout.setError(
                    "Last name is required"
            );

            valid = false;

        } else if (!lastName.matches(
                namePattern
        )) {
            lastNameLayout.setError(
                    "Start with a capital letter and use only letters"
            );

            valid = false;

        } else if (lastName.length() < 3) {
            lastNameLayout.setError(
                    "Last name must contain at least 3 characters"
            );

            valid = false;
        }

        if (email.isEmpty()) {
            emailLayout.setError(
                    "Email address is required"
            );

            valid = false;

        } else if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailLayout.setError(
                    "Enter a valid email address"
            );

            valid = false;
        }

        if (phone.isEmpty()) {
            phoneLayout.setError(
                    "Phone number is required"
            );

            valid = false;

        } else if (!phone.matches(
                phonePattern
        )) {
            phoneLayout.setError(
                    "Only numbers, spaces and + are allowed"
            );

            valid = false;
        }

        if (address.isEmpty()) {
            addressLayout.setError(
                    "Home address is required"
            );

            valid = false;

        } else if (!address.matches(
                addressPattern
        )) {
            addressLayout.setError(
                    "Address must start with a capital letter"
            );

            valid = false;

        } else if (address.length() < 3) {
            addressLayout.setError(
                    "Address must contain at least 3 characters"
            );

            valid = false;
        }

        if (model.isEmpty()) {
            modelLayout.setError(
                    "Vehicle model is required"
            );

            valid = false;

        } else if (!model.matches(
                modelPattern
        )) {
            modelLayout.setError(
                    "Model must start with a capital letter"
            );

            valid = false;

        } else if (model.length() < 3) {
            modelLayout.setError(
                    "Model must contain at least 3 characters"
            );

            valid = false;
        }

        if (vehicleType.isEmpty()) {
            vehicleTypeLayout.setError(
                    "Select a vehicle type"
            );

            valid = false;
        }

        if (registration.isEmpty()) {
            registrationLayout.setError(
                    "Registration number is required"
            );

            valid = false;

        } else if (!registration.matches(
                registrationPattern
        )) {
            registrationLayout.setError(
                    "Use format BG-123-RV"
            );

            valid = false;
        }

        if (seatsText.isEmpty()) {
            seatsLayout.setError(
                    "Number of seats is required"
            );

            valid = false;

        } else {
            try {
                int seats =
                        Integer.parseInt(
                                seatsText
                        );

                if (seats < 4) {
                    seatsLayout.setError(
                            "Vehicle must have at least 4 seats"
                    );

                    valid = false;
                }

            } catch (NumberFormatException exception) {
                seatsLayout.setError(
                        "Enter a valid number"
                );

                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        firstNameLayout.setError(null);
        lastNameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        addressLayout.setError(null);
        modelLayout.setError(null);
        registrationLayout.setError(null);
        seatsLayout.setError(null);
        vehicleTypeLayout.setError(null);
    }

    private void uploadDriverPicture(
            Long driverId
    ) {
        try {
            String fileName =
                    getFileName(
                            selectedImageUri
                    );

            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(
                                    selectedImageUri
                            );

            if (inputStream == null) {
                showSuccess();
                return;
            }

            byte[] bytes =
                    readAllBytes(
                            inputStream
                    );

            String mimeType =
                    getContentResolver()
                            .getType(
                                    selectedImageUri
                            );

            if (mimeType == null) {
                mimeType = "image/*";
            }

            RequestBody requestBody =
                    RequestBody.create(
                            bytes,
                            MediaType.parse(
                                    mimeType
                            )
                    );

            MultipartBody.Part filePart =
                    MultipartBody.Part.createFormData(
                            "file",
                            fileName,
                            requestBody
                    );

            driverApi.uploadDriverPicture(
                    driverId,
                    filePart
            ).enqueue(
                    new Callback<Object>() {

                        @Override
                        public void onResponse(
                                Call<Object> call,
                                Response<Object> response
                        ) {
                            showSuccess();
                        }

                        @Override
                        public void onFailure(
                                Call<Object> call,
                                Throwable throwable
                        ) {
                            showSuccess();

                            Toast.makeText(
                                    RegisterDriverActivity.this,
                                    "Driver was created, but the "
                                            + "picture could not be uploaded.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

        } catch (Exception exception) {
            showSuccess();

            Toast.makeText(
                    this,
                    "Driver was created, but the "
                            + "picture could not be uploaded.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private byte[] readAllBytes(
            InputStream inputStream
    ) throws IOException {

        try (
                InputStream stream =
                        inputStream;

                java.io.ByteArrayOutputStream output =
                        new java.io.ByteArrayOutputStream()
        ) {
            byte[] buffer =
                    new byte[8192];

            int count;

            while ((count = stream.read(buffer)) != -1) {
                output.write(
                        buffer,
                        0,
                        count
                );
            }

            return output.toByteArray();
        }
    }

    private String getFileName(
            Uri uri
    ) {
        String result =
                "profile_picture.jpg";

        try (
                android.database.Cursor cursor =
                        getContentResolver()
                                .query(
                                        uri,
                                        null,
                                        null,
                                        null,
                                        null
                                )
        ) {
            if (cursor != null
                    && cursor.moveToFirst()) {

                int nameIndex =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (nameIndex >= 0) {
                    result =
                            cursor.getString(
                                    nameIndex
                            );
                }
            }
        }

        return result;
    }

    private void handleCreateDriverError(
            Response<CreatedDriverDTO> response
    ) {
        if (response.code() == 409) {
            Toast.makeText(
                    this,
                    "Email address or registration number "
                            + "is already in use.",
                    Toast.LENGTH_LONG
            ).show();

        } else if (response.code() == 400) {
            Toast.makeText(
                    this,
                    "Some entered values are invalid. "
                            + "Check the form and try again.",
                    Toast.LENGTH_LONG
            ).show();

        } else if (response.code() == 401
                || response.code() == 403) {

            Toast.makeText(
                    this,
                    "Only an administrator can create "
                            + "a driver account.",
                    Toast.LENGTH_LONG
            ).show();

        } else {
            Toast.makeText(
                    this,
                    "Driver account could not be created. "
                            + "Error code: "
                            + response.code(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showSuccess() {
        setLoading(false);

        successMessage.setVisibility(
                View.VISIBLE
        );

        successMessage.setText(
                "Account successfully created. "
                        + "Activation email was sent to the driver."
        );

        clearForm();

        successMessage.postDelayed(
                () -> {
                    Intent intent =
                            new Intent(
                                    RegisterDriverActivity.this,
                                    UsersActivity.class
                            );

                    intent.putExtra(
                            "OPEN_DRIVERS_TAB",
                            true
                    );

                    startActivity(intent);
                    finish();
                },
                1800
        );
    }

    private void clearForm() {
        firstNameInput.setText("");
        lastNameInput.setText("");
        emailInput.setText("");
        phoneInput.setText("");
        addressInput.setText("");
        modelInput.setText("");
        registrationInput.setText("");
        seatsInput.setText("");

        vehicleTypeInput.setText(
                "STANDARD",
                false
        );

        babyFriendlyCheckBox.setChecked(false);
        petFriendlyCheckBox.setChecked(false);

        selectedImageUri = null;

        profileImage.setImageResource(
                R.drawable.default_avatar
        );
    }

    private void setLoading(
            boolean loading
    ) {
        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        createAccountButton.setEnabled(
                !loading
        );

        createAccountButton.setText(
                loading
                        ? "Creating account..."
                        : "Create Account"
        );
    }

    private String textOf(
            TextInputEditText input
    ) {
        if (input == null
                || input.getText() == null) {

            return "";
        }

        return input.getText()
                .toString()
                .trim();
    }
}