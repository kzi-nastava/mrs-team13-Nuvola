package com.example.nuvola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.PricingApi;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import dto.UpdateVehicleTypePriceDTO;
import dto.VehicleTypePricingDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePriceActivity extends AppCompatActivity {

    private static final String TAG = "CHANGE_PRICE";

    private EditText etPriceStandard, etPriceLuxury, etPriceVan;
    private TextView tvSuccess, tvError;
    private PricingApi pricingApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_price);

        etPriceStandard = findViewById(R.id.etPriceStandard);
        etPriceLuxury = findViewById(R.id.etPriceLuxury);
        etPriceVan = findViewById(R.id.etPriceVan);
        tvSuccess = findViewById(R.id.tvPricingSuccess);
        tvError = findViewById(R.id.tvPricingError);

        pricingApi = ApiClient.getRetrofit().create(PricingApi.class);

        setupDrawer();
        loadPrices();

        findViewById(R.id.btnSaveStandard).setOnClickListener(v -> savePrice("STANDARD", etPriceStandard));
        findViewById(R.id.btnSaveLuxury).setOnClickListener(v -> savePrice("LUXURY", etPriceLuxury));
        findViewById(R.id.btnSaveVan).setOnClickListener(v -> savePrice("VAN", etPriceVan));
        findViewById(R.id.btnSaveAll).setOnClickListener(v -> saveAll());
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        if (drawerLayout == null || navView == null) return;

        View ivMenu = findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        navView.getMenu().findItem(R.id.nav_change_price).setVisible(true);

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                logout();
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            } else if (id == R.id.nav_change_price) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void loadPrices() {
        pricingApi.getAllVehicleTypePrices().enqueue(new Callback<List<VehicleTypePricingDTO>>() {
            @Override
            public void onResponse(Call<List<VehicleTypePricingDTO>> call, Response<List<VehicleTypePricingDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (VehicleTypePricingDTO dto : response.body()) {
                        fillPriceField(dto.vehicleType, dto.basePrice);
                    }
                } else {
                    showError("Failed to load prices (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<List<VehicleTypePricingDTO>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void fillPriceField(String vehicleType, String basePrice) {
        switch (vehicleType) {
            case "STANDARD":
                etPriceStandard.setText(basePrice);
                break;
            case "LUXURY":
                etPriceLuxury.setText(basePrice);
                break;
            case "VAN":
                etPriceVan.setText(basePrice);
                break;
        }
    }

    private void savePrice(String vehicleType, EditText field) {
        String priceStr = field.getText().toString().trim();
        if (priceStr.isEmpty()) {
            field.setError("Price cannot be empty");
            return;
        }

        clearMessages();
        UpdateVehicleTypePriceDTO dto = new UpdateVehicleTypePriceDTO(priceStr);

        pricingApi.upsertVehicleTypePrice(vehicleType, dto).enqueue(new Callback<VehicleTypePricingDTO>() {
            @Override
            public void onResponse(Call<VehicleTypePricingDTO> call, Response<VehicleTypePricingDTO> response) {
                if (response.isSuccessful()) {
                    showSuccess(vehicleType + " price saved.");
                } else {
                    showError("Failed to save " + vehicleType + " price (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<VehicleTypePricingDTO> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void saveAll() {
        String standardStr = etPriceStandard.getText().toString().trim();
        String luxuryStr = etPriceLuxury.getText().toString().trim();
        String vanStr = etPriceVan.getText().toString().trim();

        if (standardStr.isEmpty()) { etPriceStandard.setError("Required"); return; }
        if (luxuryStr.isEmpty()) { etPriceLuxury.setError("Required"); return; }
        if (vanStr.isEmpty()) { etPriceVan.setError("Required"); return; }

        clearMessages();

        String[][] entries = {
                {"STANDARD", standardStr},
                {"LUXURY", luxuryStr},
                {"VAN", vanStr}
        };

        final int[] remaining = {entries.length};
        final boolean[] hasError = {false};

        for (String[] entry : entries) {
            String type = entry[0];
            String price = entry[1];

            pricingApi.upsertVehicleTypePrice(type, new UpdateVehicleTypePriceDTO(price)).enqueue(new Callback<VehicleTypePricingDTO>() {
                @Override
                public void onResponse(Call<VehicleTypePricingDTO> call, Response<VehicleTypePricingDTO> response) {
                    if (!response.isSuccessful()) hasError[0] = true;
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        runOnUiThread(() -> {
                            if (hasError[0]) showError("Some prices could not be saved.");
                            else showSuccess("All prices saved.");
                        });
                    }
                }

                @Override
                public void onFailure(Call<VehicleTypePricingDTO> call, Throwable t) {
                    hasError[0] = true;
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        runOnUiThread(() -> showError("Network error while saving prices."));
                    }
                }
            });
        }
    }

    private void logout() {
        TokenStorage.clear(this);
        ApiClient.clearInstance();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void showSuccess(String msg) {
        tvSuccess.setText(msg);
        tvSuccess.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
        tvSuccess.setVisibility(View.GONE);
    }

    private void clearMessages() {
        tvSuccess.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
    }
}