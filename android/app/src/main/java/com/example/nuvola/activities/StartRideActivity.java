package com.example.nuvola.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.DriverApi;
import com.example.nuvola.network.RideApi;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dto.CancelRideRequestDTO;
import dto.CreatedRideDTO;
import dto.DriverAssignedRideDTO;
import dto.StopRideRequestDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartRideActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 104;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private LinearLayout ridesContainer;
    private LinearLayout emptyStateContainer;

    private ProgressBar progressBar;
    private TextView errorText;
    private MaterialButton refreshButton;

    private DriverApi driverApi;
    private RideApi rideApi;

    private String driverEmail;

    private boolean firstResume = true;
    private boolean hasActiveRide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_ride);

        driverApi = ApiClient.getRetrofit()
                .create(DriverApi.class);

        rideApi = ApiClient.getRetrofit()
                .create(RideApi.class);

        bindViews();
        setupDrawer();
        setupListeners();

        refreshDriverEmail();
        loadAssignedRides();
    }

    private void bindViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);

        ridesContainer = findViewById(R.id.ridesContainer);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);

        progressBar = findViewById(R.id.progressAssignedRides);
        errorText = findViewById(R.id.tvAssignedRidesError);
        refreshButton = findViewById(R.id.btnRefreshRides);
    }

    private void setupDrawer() {
        View menuButton = findViewById(R.id.ivMenu);

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
        refreshButton.setOnClickListener(
                view -> {
                    refreshDriverEmail();
                    loadAssignedRides();
                }
        );
    }

    private void refreshDriverEmail() {
        driverEmail = TokenStorage.getUserEmail(this);

        if (driverEmail == null
                || driverEmail.trim().isEmpty()) {

            driverEmail =
                    TokenStorage.getRememberedEmail(this);
        }
    }

    private void loadAssignedRides() {
        hideMessages();
        ridesContainer.removeAllViews();
        setLoading(true);

        refreshDriverEmail();

        if (driverEmail == null
                || driverEmail.trim().isEmpty()) {

            setLoading(false);

            showError(
                    "Driver email is unavailable. "
                            + "Please log out and log in again."
            );

            return;
        }

        driverApi.assignedRides(driverEmail.trim())
                .enqueue(
                        new Callback<List<DriverAssignedRideDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<DriverAssignedRideDTO>> call,

                                    @NonNull
                                    Response<List<DriverAssignedRideDTO>> response
                            ) {
                                setLoading(false);
                                ridesContainer.removeAllViews();

                                if (!response.isSuccessful()) {
                                    hasActiveRide = false;

                                    showError(
                                            "Assigned rides could not be loaded. "
                                                    + "Error code: "
                                                    + response.code()
                                    );

                                    return;
                                }

                                List<DriverAssignedRideDTO> responseRides =
                                        response.body();

                                if (responseRides == null
                                        || responseRides.isEmpty()) {

                                    hasActiveRide = false;

                                    emptyStateContainer.setVisibility(
                                            View.VISIBLE
                                    );

                                    return;
                                }

                                List<DriverAssignedRideDTO> rides =
                                        new ArrayList<>(responseRides);

                                hasActiveRide =
                                        containsActiveRide(rides);

                                sortRides(rides);

                                for (DriverAssignedRideDTO ride : rides) {
                                    addRideCard(ride);
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<DriverAssignedRideDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);
                                hasActiveRide = false;

                                showError(
                                        "Network error: "
                                                + safeThrowableMessage(
                                                throwable
                                        )
                                );
                            }
                        }
                );
    }

    private boolean containsActiveRide(
            List<DriverAssignedRideDTO> rides
    ) {
        if (rides == null) {
            return false;
        }

        for (DriverAssignedRideDTO ride : rides) {
            if (ride == null
                    || ride.getStatus() == null) {

                continue;
            }

            if ("IN_PROGRESS".equalsIgnoreCase(
                    ride.getStatus()
            )) {
                return true;
            }
        }

        return false;
    }

    private void sortRides(
            List<DriverAssignedRideDTO> rides
    ) {
        rides.sort(
                Comparator
                        .comparingInt(this::getRidePriority)
                        .thenComparing(
                                ride -> {
                                    if (ride == null
                                            || ride.getScheduledTime() == null) {

                                        return "";
                                    }

                                    return ride.getScheduledTime();
                                }
                        )
        );
    }

    private int getRidePriority(
            DriverAssignedRideDTO ride
    ) {
        if (ride == null
                || ride.getStatus() == null) {

            return 99;
        }

        String status = ride.getStatus()
                .toUpperCase(Locale.ROOT);

        if ("IN_PROGRESS".equals(status)) {
            return 0;
        }

        if ("SCHEDULED".equals(status)) {
            return 1;
        }

        return 2;
    }

    private void addRideCard(
            DriverAssignedRideDTO ride
    ) {
        View item = LayoutInflater.from(this)
                .inflate(
                        R.layout.item_assigned_ride,
                        ridesContainer,
                        false
                );

        TextView routeText =
                item.findViewById(R.id.tvRoute);

        TextView statusText =
                item.findViewById(R.id.tvStatus);

        TextView scheduledTimeText =
                item.findViewById(R.id.tvSchedule);

        TextView stopsText =
                item.findViewById(R.id.tvStops);

        TextView passengersText =
                item.findViewById(R.id.tvPassengers);

        TextView priceText =
                item.findViewById(R.id.tvPrice);

        TextView rideSectionText =
                item.findViewById(R.id.tvRideSection);

        View leftAccent =
                item.findViewById(R.id.leftAccent);

        CardView cardRide =
                item.findViewById(R.id.cardRide);

        MaterialButton startButton =
                item.findViewById(R.id.btnStart);

        MaterialButton cancelButton =
                item.findViewById(R.id.btnCancel);

        MaterialButton panicButton =
                item.findViewById(R.id.btnPanic);

        MaterialButton stopButton =
                item.findViewById(R.id.btnStop);

        MaterialButton endButton =
                item.findViewById(R.id.btnEndRide);

        String pickup = valueOrFallback(
                ride.getPickup(),
                "Pickup"
        );

        String dropoff = valueOrFallback(
                ride.getDropoff(),
                "Destination"
        );

        routeText.setText(
                pickup + " → " + dropoff
        );

        String status = valueOrFallback(
                ride.getStatus(),
                "UNKNOWN"
        ).toUpperCase(Locale.ROOT);

        statusText.setText(status);

        scheduledTimeText.setText(
                "Scheduled: "
                        + formatDateTime(
                        ride.getScheduledTime()
                )
        );

        stopsText.setText(
                "Stops: "
                        + formatList(
                        ride.getStops(),
                        "None"
                )
        );

        passengersText.setText(
                "Passengers: "
                        + formatList(
                        ride.getPassengers(),
                        "None"
                )
        );

        if (ride.getPrice() == null) {
            priceText.setText(
                    "Price: Not calculated"
            );

        } else {
            priceText.setText(
                    String.format(
                            Locale.US,
                            "Price: %.2f RSD",
                            ride.getPrice()
                    )
            );
        }

        boolean scheduled =
                "SCHEDULED".equals(status);

        boolean inProgress =
                "IN_PROGRESS".equals(status);

        if (inProgress) {
            configureCurrentRideCard(
                    rideSectionText,
                    leftAccent,
                    cardRide,
                    statusText,
                    startButton,
                    cancelButton,
                    panicButton,
                    stopButton,
                    endButton
            );

        } else if (scheduled) {
            configureScheduledRideCard(
                    rideSectionText,
                    leftAccent,
                    cardRide,
                    statusText,
                    startButton,
                    cancelButton,
                    panicButton,
                    stopButton,
                    endButton
            );

        } else {
            configureOtherRideCard(
                    rideSectionText,
                    leftAccent,
                    startButton,
                    cancelButton,
                    panicButton,
                    stopButton,
                    endButton
            );
        }

        boolean canStart =
                scheduled && !hasActiveRide;

        startButton.setEnabled(canStart);

        startButton.setAlpha(
                canStart ? 1.0f : 0.45f
        );

        startButton.setText(
                hasActiveRide && scheduled
                        ? "Active ride"
                        : "Start"
        );

        startButton.setOnClickListener(
                view -> {
                    if (hasActiveRide) {
                        Toast.makeText(
                                StartRideActivity.this,
                                "You cannot start another ride while "
                                        + "one ride is already in progress.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (ride.getId() != null) {
                        startRide(ride.getId());
                    }
                }
        );

        cancelButton.setOnClickListener(
                view -> showCancelDialog(ride)
        );

        panicButton.setOnClickListener(
                view -> confirmPanic(ride)
        );

        stopButton.setOnClickListener(
                view -> stopRideAtCurrentLocation(ride)
        );

        endButton.setOnClickListener(
                view -> confirmEndRide()
        );

        ridesContainer.addView(item);
    }

    private void configureCurrentRideCard(
            TextView rideSectionText,
            View leftAccent,
            CardView cardRide,
            TextView statusText,
            MaterialButton startButton,
            MaterialButton cancelButton,
            MaterialButton panicButton,
            MaterialButton stopButton,
            MaterialButton endButton
    ) {
        rideSectionText.setText("Current ride");
        rideSectionText.setTextColor(0xFF166534);

        leftAccent.setVisibility(View.VISIBLE);

        cardRide.setCardBackgroundColor(
                0xFFF8FFFB
        );

        cardRide.setCardElevation(
                dpToPxFloat(8)
        );

        statusText.setBackgroundResource(
                R.drawable.bg_status_in_progress
        );

        statusText.setTextColor(
                0xFF166534
        );

        panicButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.VISIBLE);
        endButton.setVisibility(View.VISIBLE);

        startButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

    private void configureScheduledRideCard(
            TextView rideSectionText,
            View leftAccent,
            CardView cardRide,
            TextView statusText,
            MaterialButton startButton,
            MaterialButton cancelButton,
            MaterialButton panicButton,
            MaterialButton stopButton,
            MaterialButton endButton
    ) {
        rideSectionText.setText("Upcoming ride");
        rideSectionText.setTextColor(0xFF1F2937);

        leftAccent.setVisibility(View.GONE);

        cardRide.setCardBackgroundColor(
                0xFFFFFFFF
        );

        cardRide.setCardElevation(
                dpToPxFloat(4)
        );

        statusText.setBackgroundResource(
                R.drawable.bg_status_scheduled
        );

        statusText.setTextColor(
                0xFF8A5A00
        );

        panicButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        endButton.setVisibility(View.GONE);

        startButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
    }

    private void configureOtherRideCard(
            TextView rideSectionText,
            View leftAccent,
            MaterialButton startButton,
            MaterialButton cancelButton,
            MaterialButton panicButton,
            MaterialButton stopButton,
            MaterialButton endButton
    ) {
        rideSectionText.setText("Ride");
        rideSectionText.setTextColor(0xFF1F2937);

        leftAccent.setVisibility(View.GONE);

        panicButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.GONE);
        endButton.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
    }

    private float dpToPxFloat(
            int dp
    ) {
        return dp
                * getResources()
                .getDisplayMetrics()
                .density;
    }

    private void startRide(
            Long rideId
    ) {
        if (hasActiveRide) {
            Toast.makeText(
                    this,
                    "You already have a ride in progress.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (rideId == null) {
            return;
        }

        setLoading(true);

        rideApi.startRide(rideId)
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<Void> call,

                                    @NonNull
                                    Response<Void> response
                            ) {
                                setLoading(false);

                                if (response.isSuccessful()) {
                                    hasActiveRide = true;

                                    Toast.makeText(
                                            StartRideActivity.this,
                                            "Ride started successfully.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    loadAssignedRides();

                                } else if (response.code() == 409) {
                                    hasActiveRide = true;

                                    Toast.makeText(
                                            StartRideActivity.this,
                                            "Another ride is already in progress.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    loadAssignedRides();

                                } else {
                                    showApiError(
                                            "Ride could not be started",
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<Void> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);
                                showNetworkError(throwable);
                            }
                        }
                );
    }

    private void showCancelDialog(
            DriverAssignedRideDTO ride
    ) {
        if (ride == null
                || ride.getId() == null) {

            return;
        }

        EditText reasonInput =
                new EditText(this);

        reasonInput.setHint(
                "Reason for cancellation"
        );

        reasonInput.setPadding(
                40,
                24,
                40,
                24
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Cancel ride")
                        .setMessage(
                                "Enter the reason for cancelling this ride."
                        )
                        .setView(reasonInput)
                        .setPositiveButton(
                                "Cancel ride",
                                null
                        )
                        .setNegativeButton(
                                "Back",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                listener -> dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(
                        view -> {
                            String reason =
                                    reasonInput.getText()
                                            .toString()
                                            .trim();

                            if (reason.isEmpty()) {
                                reasonInput.setError(
                                        "Reason is required"
                                );

                                return;
                            }

                            dialog.dismiss();

                            cancelRide(
                                    ride.getId(),
                                    reason
                            );
                        }
                )
        );

        dialog.show();
    }

    private void cancelRide(
            Long rideId,
            String reason
    ) {
        if (rideId == null) {
            return;
        }

        setLoading(true);

        rideApi.cancelRideByDriver(
                rideId,
                new CancelRideRequestDTO(reason)
        ).enqueue(
                new Callback<Object>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<Object> call,

                            @NonNull
                            Response<Object> response
                    ) {
                        setLoading(false);

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    StartRideActivity.this,
                                    "Ride cancelled.",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadAssignedRides();

                        } else {
                            showApiError(
                                    "Ride could not be cancelled",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<Object> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        setLoading(false);
                        showNetworkError(throwable);
                    }
                }
        );
    }

    private void confirmPanic(
            DriverAssignedRideDTO ride
    ) {
        if (ride == null
                || ride.getId() == null) {

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("PANIC")
                .setMessage(
                        "Activate panic for this ride?"
                )
                .setPositiveButton(
                        "Activate",
                        (dialog, which) ->
                                triggerPanic(ride.getId())
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void triggerPanic(
            Long rideId
    ) {
        if (rideId == null) {
            return;
        }

        rideApi.triggerPanic(rideId)
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<Void> call,

                                    @NonNull
                                    Response<Void> response
                            ) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(
                                            StartRideActivity.this,
                                            "Panic notification sent.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                } else {
                                    showApiError(
                                            "Panic could not be activated",
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<Void> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                showNetworkError(throwable);
                            }
                        }
                );
    }

    private void stopRideAtCurrentLocation(
            DriverAssignedRideDTO ride
    ) {
        if (ride == null
                || ride.getId() == null) {

            return;
        }

        Location location =
                getLastKnownLocation();

        if (location == null) {
            Toast.makeText(
                    this,
                    "Current location is unavailable. "
                            + "Enable location and try again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String stoppedAt =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.US
                ).format(new Date());

        StopRideRequestDTO request =
                new StopRideRequestDTO(
                        location.getLatitude(),
                        location.getLongitude(),
                        stoppedAt,
                        "Current vehicle location"
                );

        setLoading(true);

        rideApi.stopRide(
                ride.getId(),
                request
        ).enqueue(
                new Callback<CreatedRideDTO>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<CreatedRideDTO> call,

                            @NonNull
                            Response<CreatedRideDTO> response
                    ) {
                        setLoading(false);

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    StartRideActivity.this,
                                    "Ride stopped at current location.",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadAssignedRides();

                        } else {
                            showApiError(
                                    "Ride could not be stopped",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<CreatedRideDTO> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        setLoading(false);
                        showNetworkError(throwable);
                    }
                }
        );
    }

    private Location getLastKnownLocation() {
        boolean fineLocationMissing =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED;

        boolean coarseLocationMissing =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED;

        if (fineLocationMissing
                && coarseLocationMissing) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );

            return null;
        }

        LocationManager manager =
                (LocationManager) getSystemService(
                        LOCATION_SERVICE
                );

        if (manager == null) {
            return null;
        }

        Location gps = null;
        Location network = null;

        try {
            gps = manager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER
            );

        } catch (Exception ignored) {
        }

        try {
            network = manager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER
            );

        } catch (Exception ignored) {
        }

        if (gps == null) {
            return network;
        }

        if (network == null) {
            return gps;
        }

        return gps.getTime() > network.getTime()
                ? gps
                : network;
    }

    private void confirmEndRide() {
        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_end_ride,
                                null,
                                false
                        );

        MaterialButton cancelButton =
                dialogView.findViewById(
                        R.id.btnCancelEndRide
                );

        MaterialButton confirmButton =
                dialogView.findViewById(
                        R.id.btnConfirmEndRide
                );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create();

        cancelButton.setOnClickListener(
                view -> dialog.dismiss()
        );

        confirmButton.setOnClickListener(
                view -> {
                    dialog.dismiss();
                    endRide();
                }
        );

        dialog.setOnShowListener(
                listener -> {
                    Window window =
                            dialog.getWindow();

                    if (window == null) {
                        return;
                    }

                    window.setBackgroundDrawable(
                            new ColorDrawable(
                                    Color.TRANSPARENT
                            )
                    );

                    int width =
                            (int) (
                                    getResources()
                                            .getDisplayMetrics()
                                            .widthPixels
                                            * 0.88f
                            );

                    window.setLayout(
                            width,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

                    window.setDimAmount(0.60f);

                    window.addFlags(
                            WindowManager.LayoutParams
                                    .FLAG_DIM_BEHIND
                    );
                }
        );

        dialog.show();
    }

    private void endRide() {
        refreshDriverEmail();

        if (driverEmail == null
                || driverEmail.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Driver email is unavailable.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        setLoading(true);

        rideApi.endRide(driverEmail.trim())
                .enqueue(
                        new Callback<Long>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<Long> call,

                                    @NonNull
                                    Response<Long> response
                            ) {
                                setLoading(false);

                                if (response.isSuccessful()) {
                                    hasActiveRide = false;

                                    Toast.makeText(
                                            StartRideActivity.this,
                                            "Ride ended successfully.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    loadAssignedRides();

                                } else {
                                    showApiError(
                                            "Ride could not be ended",
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<Long> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);
                                showNetworkError(throwable);
                            }
                        }
                );
    }

    private String formatDateTime(
            String value
    ) {
        if (value == null
                || value.trim().isEmpty()) {

            return "Now";
        }

        return value
                .replace("T", " ")
                .replaceAll("\\.\\d+$", "");
    }

    private void showApiError(
            String prefix,
            int code
    ) {
        Toast.makeText(
                this,
                prefix
                        + ". Error code: "
                        + code,
                Toast.LENGTH_LONG
        ).show();
    }

    private void showNetworkError(
            Throwable throwable
    ) {
        Toast.makeText(
                this,
                "Network error: "
                        + safeThrowableMessage(
                        throwable
                ),
                Toast.LENGTH_LONG
        ).show();
    }

    private String safeThrowableMessage(
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

    private void setLoading(
            boolean loading
    ) {
        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        refreshButton.setEnabled(!loading);

        refreshButton.setText(
                loading
                        ? "Loading..."
                        : "Refresh"
        );
    }

    private void hideMessages() {
        errorText.setVisibility(View.GONE);

        emptyStateContainer.setVisibility(
                View.GONE
        );
    }

    private void showError(
            String message
    ) {
        errorText.setText(message);

        errorText.setVisibility(
                View.VISIBLE
        );
    }

    private String valueOrFallback(
            String value,
            String fallback
    ) {
        if (value == null
                || value.trim().isEmpty()) {

            return fallback;
        }

        return value;
    }

    private String formatList(
            List<String> values,
            String fallback
    ) {
        if (values == null
                || values.isEmpty()) {

            return fallback;
        }

        return String.join(
                ", ",
                values
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (firstResume) {
            firstResume = false;
            return;
        }

        refreshDriverEmail();
        loadAssignedRides();
    }
}