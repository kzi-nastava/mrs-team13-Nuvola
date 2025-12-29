package com.example.nuvola.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private boolean isDriver = true;
    ImageView ivProfile;
    EditText etFirstName, etLastName, etEmail, etPhone, etAddress;
    TextView tvErrorFirstName, tvErrorLastName, tvErrorEmail, tvErrorPhone, tvErrorAddress, tvSuccess;
    EditText etVehicleModel, etLicensePlates, etSeats;
    Spinner spinnerVehicleType;
    TextView tvErrorVehicleModel, tvErrorVehicleType, tvErrorLicense, tvErrorSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_user);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        ImageView ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null && drawerLayout != null) {
            ivMenu.setOnClickListener(v ->
                    drawerLayout.openDrawer(GravityCompat.START)
            );
        }

        if (navView != null && drawerLayout != null) {
            navView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                }
                else if (id == R.id.nav_ridehistory) {
                    startActivity(new Intent(this, DriverRideHistory.class));
                }
                else if (id == R.id.nav_account) {
                    // već si na profilu
                }
                else if (id == R.id.nav_logout) {
                    startActivity(new Intent(this, LoginActivity.class));
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }


        ivProfile = findViewById(R.id.ivProfile);
        findViewById(R.id.ivUpload).setOnClickListener(v -> openGallery());

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        tvErrorFirstName = findViewById(R.id.tvErrorFirstName);
        tvErrorLastName = findViewById(R.id.tvErrorLastName);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);
        tvErrorAddress = findViewById(R.id.tvErrorAddress);
        tvSuccess = findViewById(R.id.tvSuccess);

        etFirstName.setText("Milica");
        etLastName.setText("Lukic");
        etEmail.setText("milica.lukic@gmail.com");
        etPhone.setText("+381 64 556655");
        etAddress.setText("Fruskogorska 37");

        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.vehicle_types,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
        spinnerVehicleType.setSelection(1);

        if (isDriver) {
            findViewById(R.id.driverInfoContainer).setVisibility(View.VISIBLE);
        }

        etVehicleModel = findViewById(R.id.etVehicleModel);
        etLicensePlates = findViewById(R.id.etLicensePlates);
        etSeats = findViewById(R.id.etSeats);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);

        tvErrorVehicleModel = findViewById(R.id.tvErrorVehicleModel);
        tvErrorVehicleType = findViewById(R.id.tvErrorVehicleType);
        tvErrorLicense = findViewById(R.id.tvErrorLicense);
        tvErrorSeats = findViewById(R.id.tvErrorSeats);

        if (isDriver) {
            findViewById(R.id.driverInfoContainer).setVisibility(View.VISIBLE);
        }

        if (isDriver) {
            etVehicleModel.setText("Toyota Corolla");
            etLicensePlates.setText("NS-123-AB");
            etSeats.setText("4");
            spinnerVehicleType.setSelection(1); // Standard
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (validate()) {

                if (isDriver) {
                    tvSuccess.setText("Changes submitted. Waiting for admin approval.");
                } else {
                    tvSuccess.setText("Changes saved!");
                }

                tvSuccess.setVisibility(View.VISIBLE);
            }
        });

    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivProfile.setImageURI(imageUri);
        }
    }

    private boolean validate() {
        boolean valid = true;
        hideErrors();

        if (!etFirstName.getText().toString().matches("[a-zA-Z]+")) {
            tvErrorFirstName.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!etLastName.getText().toString().matches("[a-zA-Z]+")) {
            tvErrorLastName.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(etEmail.getText().toString()).matches()) {
            tvErrorEmail.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (!etPhone.getText().toString().matches("[+0-9 ]+")) {
            tvErrorPhone.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            tvErrorAddress.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (isDriver) {
            if (etVehicleModel.getText().toString().trim().isEmpty()) {
                tvErrorVehicleModel.setVisibility(View.VISIBLE);
                valid = false;
            }

            if (spinnerVehicleType.getSelectedItemPosition() == 0) {
                tvErrorVehicleType.setVisibility(View.VISIBLE);
                valid = false;
            }


            if (etLicensePlates.getText().toString().trim().isEmpty()) {
                tvErrorLicense.setVisibility(View.VISIBLE);
                valid = false;
            }

            if (etSeats.getText().toString().trim().isEmpty()) {
                tvErrorSeats.setVisibility(View.VISIBLE);
                valid = false;
            }
        }

        return valid;
    }


    private void hideErrors() {
        tvErrorFirstName.setVisibility(View.GONE);
        tvErrorLastName.setVisibility(View.GONE);
        tvErrorEmail.setVisibility(View.GONE);
        tvErrorPhone.setVisibility(View.GONE);
        tvErrorAddress.setVisibility(View.GONE);
        if (isDriver) {
            tvErrorVehicleModel.setVisibility(View.GONE);
            tvErrorVehicleType.setVisibility(View.GONE);
            tvErrorLicense.setVisibility(View.GONE);
            tvErrorSeats.setVisibility(View.GONE);
        }

        tvSuccess.setVisibility(View.GONE);
    }
}
