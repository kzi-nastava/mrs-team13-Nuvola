package com.example.nuvola.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.PricingApi;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import dto.UpdateVehicleTypePriceDTO;
import dto.VehicleTypePricingDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePriceActivity extends AppCompatActivity {

    private EditText etPriceStandard;
    private EditText etPriceLuxury;
    private EditText etPriceVan;

    private TextView tvSuccess;
    private TextView tvError;

    private PricingApi pricingApi;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_change_price
        );

        bindViews();
        setupDrawer();

        pricingApi =
                ApiClient.getRetrofit()
                        .create(PricingApi.class);

        setupListeners();
        loadPrices();
    }

    private void bindViews() {
        drawerLayout =
                findViewById(
                        R.id.drawerLayout
                );

        navigationView =
                findViewById(
                        R.id.navView
                );

        etPriceStandard =
                findViewById(
                        R.id.etPriceStandard
                );

        etPriceLuxury =
                findViewById(
                        R.id.etPriceLuxury
                );

        etPriceVan =
                findViewById(
                        R.id.etPriceVan
                );

        tvSuccess =
                findViewById(
                        R.id.tvPricingSuccess
                );

        tvError =
                findViewById(
                        R.id.tvPricingError
                );
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

    private void setupListeners() {
        findViewById(
                R.id.btnSaveStandard
        ).setOnClickListener(
                view -> savePrice(
                        "STANDARD",
                        etPriceStandard
                )
        );

        findViewById(
                R.id.btnSaveLuxury
        ).setOnClickListener(
                view -> savePrice(
                        "LUXURY",
                        etPriceLuxury
                )
        );

        findViewById(
                R.id.btnSaveVan
        ).setOnClickListener(
                view -> savePrice(
                        "VAN",
                        etPriceVan
                )
        );

        findViewById(
                R.id.btnSaveAll
        ).setOnClickListener(
                view -> saveAll()
        );
    }

    private void loadPrices() {
        pricingApi.getAllVehicleTypePrices()
                .enqueue(
                        new Callback<List<VehicleTypePricingDTO>>() {

                            @Override
                            public void onResponse(
                                    Call<List<VehicleTypePricingDTO>> call,
                                    Response<List<VehicleTypePricingDTO>> response
                            ) {
                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    for (VehicleTypePricingDTO dto
                                            : response.body()) {

                                        fillPriceField(
                                                dto.vehicleType,
                                                dto.basePrice
                                        );
                                    }

                                } else {
                                    showError(
                                            "Failed to load prices ("
                                                    + response.code()
                                                    + ")"
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<List<VehicleTypePricingDTO>> call,
                                    Throwable throwable
                            ) {
                                showError(
                                        "Network error: "
                                                + getThrowableMessage(
                                                throwable
                                        )
                                );
                            }
                        }
                );
    }

    private void fillPriceField(
            String vehicleType,
            String basePrice
    ) {
        if (vehicleType == null) {
            return;
        }

        switch (vehicleType) {
            case "STANDARD":
                etPriceStandard.setText(
                        basePrice
                );
                break;

            case "LUXURY":
                etPriceLuxury.setText(
                        basePrice
                );
                break;

            case "VAN":
                etPriceVan.setText(
                        basePrice
                );
                break;
        }
    }

    private void savePrice(
            String vehicleType,
            EditText field
    ) {
        String price =
                field.getText()
                        .toString()
                        .trim();

        if (price.isEmpty()) {
            field.setError(
                    "Price cannot be empty"
            );

            return;
        }

        clearMessages();

        UpdateVehicleTypePriceDTO dto =
                new UpdateVehicleTypePriceDTO(
                        price
                );

        pricingApi.upsertVehicleTypePrice(
                vehicleType,
                dto
        ).enqueue(
                new Callback<VehicleTypePricingDTO>() {

                    @Override
                    public void onResponse(
                            Call<VehicleTypePricingDTO> call,
                            Response<VehicleTypePricingDTO> response
                    ) {
                        if (response.isSuccessful()) {
                            showSuccess(
                                    vehicleType
                                            + " price saved."
                            );

                        } else {
                            showError(
                                    "Failed to save "
                                            + vehicleType
                                            + " price ("
                                            + response.code()
                                            + ")"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<VehicleTypePricingDTO> call,
                            Throwable throwable
                    ) {
                        showError(
                                "Network error: "
                                        + getThrowableMessage(
                                        throwable
                                )
                        );
                    }
                }
        );
    }

    private void saveAll() {
        String standardPrice =
                etPriceStandard.getText()
                        .toString()
                        .trim();

        String luxuryPrice =
                etPriceLuxury.getText()
                        .toString()
                        .trim();

        String vanPrice =
                etPriceVan.getText()
                        .toString()
                        .trim();

        if (standardPrice.isEmpty()) {
            etPriceStandard.setError(
                    "Required"
            );

            return;
        }

        if (luxuryPrice.isEmpty()) {
            etPriceLuxury.setError(
                    "Required"
            );

            return;
        }

        if (vanPrice.isEmpty()) {
            etPriceVan.setError(
                    "Required"
            );

            return;
        }

        clearMessages();

        String[][] entries = {
                {
                        "STANDARD",
                        standardPrice
                },
                {
                        "LUXURY",
                        luxuryPrice
                },
                {
                        "VAN",
                        vanPrice
                }
        };

        final int[] remaining = {
                entries.length
        };

        final boolean[] hasError = {
                false
        };

        for (String[] entry : entries) {
            String type =
                    entry[0];

            String price =
                    entry[1];

            pricingApi.upsertVehicleTypePrice(
                    type,
                    new UpdateVehicleTypePriceDTO(
                            price
                    )
            ).enqueue(
                    new Callback<VehicleTypePricingDTO>() {

                        @Override
                        public void onResponse(
                                Call<VehicleTypePricingDTO> call,
                                Response<VehicleTypePricingDTO> response
                        ) {
                            if (!response.isSuccessful()) {
                                hasError[0] = true;
                            }

                            remaining[0]--;

                            if (remaining[0] == 0) {
                                if (hasError[0]) {
                                    showError(
                                            "Some prices could not be saved."
                                    );

                                } else {
                                    showSuccess(
                                            "All prices saved."
                                    );
                                }
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<VehicleTypePricingDTO> call,
                                Throwable throwable
                        ) {
                            hasError[0] = true;
                            remaining[0]--;

                            if (remaining[0] == 0) {
                                showError(
                                        "Network error while saving prices."
                                );
                            }
                        }
                    }
            );
        }
    }

    private void showSuccess(
            String message
    ) {
        tvSuccess.setText(message);

        tvSuccess.setVisibility(
                View.VISIBLE
        );

        tvError.setVisibility(
                View.GONE
        );
    }

    private void showError(
            String message
    ) {
        tvError.setText(message);

        tvError.setVisibility(
                View.VISIBLE
        );

        tvSuccess.setVisibility(
                View.GONE
        );
    }

    private void clearMessages() {
        tvSuccess.setVisibility(
                View.GONE
        );

        tvError.setVisibility(
                View.GONE
        );
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