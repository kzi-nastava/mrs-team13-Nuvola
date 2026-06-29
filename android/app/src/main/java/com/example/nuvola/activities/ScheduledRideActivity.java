package com.example.nuvola.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.network.RideService;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import dto.ScheduledRideDTO;

public class ScheduledRideActivity extends AppCompatActivity {

    public static final String EXTRA_RIDE_ID = "scheduled_ride_id";

    private ProgressBar progressBar;
    private TextView tvError, tvPickup, tvDropoff, tvStartTime, tvDriver, tvPrice;
    private ScrollView scrollContent;
    private MaterialButton btnStartRide;

    private long rideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_ride);

        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvPickup = findViewById(R.id.tvPickup);
        tvDropoff = findViewById(R.id.tvDropoff);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvDriver = findViewById(R.id.tvDriver);
        tvPrice = findViewById(R.id.tvPrice);
        scrollContent = findViewById(R.id.scrollContent);
        btnStartRide = findViewById(R.id.btnStartRide);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rideId = getIntent().getLongExtra(EXTRA_RIDE_ID, -1);
        if (rideId == -1) {
            showError("Invalid ride ID.");
            return;
        }

        loadScheduledRide();

        btnStartRide.setOnClickListener(v -> startRide());
    }

    private void loadScheduledRide() {
        showLoading();
        RideService.getScheduledRide(rideId, new RideService.ScheduledRideCallback() {
            @Override
            public void onSuccess(ScheduledRideDTO ride) {
                runOnUiThread(() -> bindRide(ride));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showError(message));
            }
        });
    }

    private void bindRide(ScheduledRideDTO ride) {
        tvPickup.setText(formatLocation(ride.pickup != null ? ride.pickup.latitude : null,
                ride.pickup != null ? ride.pickup.longitude : null));
        tvDropoff.setText(formatLocation(ride.dropoff != null ? ride.dropoff.latitude : null,
                ride.dropoff != null ? ride.dropoff.longitude : null));
        tvStartTime.setText(ride.startingTime != null ? ride.startingTime : "—");
        tvDriver.setText(ride.driver != null ? ride.driver : "—");
        tvPrice.setText(String.format(Locale.getDefault(), "%.2f RSD", ride.price));

        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        scrollContent.setVisibility(View.VISIBLE);
    }

    private void startRide() {
        btnStartRide.setEnabled(false);
        RideService.startRide(rideId, new RideService.StartRideCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ScheduledRideActivity.this, "Ride started!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ScheduledRideActivity.this, message, Toast.LENGTH_SHORT).show();
                    btnStartRide.setEnabled(true);
                });
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        scrollContent.setVisibility(View.GONE);
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private String formatLocation(Double lat, Double lng) {
        if (lat == null || lng == null) return "—";
        return String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng);
    }
}