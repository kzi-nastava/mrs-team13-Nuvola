package com.example.nuvola.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.nuvola.R;
import com.example.nuvola.fragments.DriversRideHistoryFragment;
import com.example.nuvola.model.Ride;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.AuthApi;
import com.example.nuvola.network.RideService;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.services.DriverLocationPublisherService;
import com.example.nuvola.services.StompNotificationService;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DriverRideHistory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ride_history);

        Log.d("DriverRideHistory", "onCreate started");

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        // Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        String role = TokenStorage.getUserRole(this);
        boolean isAdmin = "ADMIN".equals(role);
        boolean isDriver = "DRIVER".equals(role);
        boolean isPassenger = "PASSENGER".equals(role);
        navigationView.getMenu().findItem(R.id.nav_change_price).setVisible(isAdmin);
        navigationView.getMenu().findItem(R.id.nav_driver_ride_details).setVisible(isAdmin);
        navigationView.getMenu().findItem(R.id.nav_end_ride).setVisible(isDriver);
        navigationView.getMenu().findItem(R.id.nav_grade_ride).setVisible(isPassenger);

        if (savedInstanceState == null) {
            // ArrayList<Ride> rides = createTestRides();
            //DriversRideHistoryFragment fragment = DriversRideHistoryFragment.newInstance(rides);
            DriversRideHistoryFragment fragment = new DriversRideHistoryFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();

            navigationView.setCheckedItem(R.id.nav_history);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_search) {
//            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
//            return true;
//        } else if (id == R.id.action_settings) {
//            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_history) {
            Toast.makeText(this, "Ride History", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(DriverRideHistory.this, ProfileActivity.class));
        } else if (id == R.id.nav_change_price) {
            startActivity(new Intent(DriverRideHistory.this, ChangePriceActivity.class));
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(DriverRideHistory.this, NotificationsActivity.class));
        } else if (id == R.id.nav_driver_ride_details) {
            showDriverIdDialog();
        } else if (id == R.id.nav_grade_ride) {
            showGradeRideDialog();
        } else if (id == R.id.nav_track_ride) {
            startActivity(new Intent(DriverRideHistory.this, RideTrackingActivity.class));
        } else if (id == R.id.nav_support_chat) {
            boolean isAdmin = "ADMIN".equals(TokenStorage.getUserRole(this));
            if (isAdmin) {
                startActivity(new Intent(DriverRideHistory.this, AdminInboxActivity.class));
            } else {
                startActivity(new Intent(DriverRideHistory.this, SupportChatActivity.class));
            }
        } else if (id == R.id.nav_end_ride) {
            showEndRideDialog();
        } else if (id == R.id.nav_logout) {
            performLogout();
        }


        // close drawer after click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void performLogout() {
        AuthApi authApi = ApiClient.getRetrofit().create(AuthApi.class);
        authApi.logout().enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                finishLogoutLocally();
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                finishLogoutLocally();
            }
        });
    }

    private void finishLogoutLocally() {
        stopService(new Intent(this, DriverLocationPublisherService.class));
        stopService(new Intent(this, StompNotificationService.class));
        TokenStorage.clear(this);
        ApiClient.clearInstance();
        Intent intent = new Intent(DriverRideHistory.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showEndRideDialog() {
        EditText etUsername = new EditText(this);
        etUsername.setHint("Driver username");
        etUsername.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("End Ride")
                .setView(etUsername)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String username = etUsername.getText().toString().trim();
                    if (username.isEmpty()) {
                        Toast.makeText(this, "Please enter a username.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RideService.endRide(username, new RideService.EndRideCallback() {
                        @Override
                        public void onRideEnded(long scheduledRideId) {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(DriverRideHistory.this, ScheduledRideActivity.class);
                                intent.putExtra(ScheduledRideActivity.EXTRA_RIDE_ID, scheduledRideId);
                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onRideEndedNoNext() {
                            runOnUiThread(() ->
                                    Toast.makeText(DriverRideHistory.this,
                                            "Ride ended. No upcoming scheduled ride.", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() ->
                                    Toast.makeText(DriverRideHistory.this, message, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGradeRideDialog() {
        EditText etRideId = new EditText(this);
        etRideId.setHint("Ride ID");
        etRideId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etRideId.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Grade Ride")
                .setView(etRideId)
                .setPositiveButton("Open", (dialog, which) -> {
                    String input = etRideId.getText().toString().trim();
                    if (input.isEmpty()) return;
                    try {
                        long rideId = Long.parseLong(input);
                        Intent intent = new Intent(this, GradeRideActivity.class);
                        intent.putExtra(GradeRideActivity.EXTRA_RIDE_ID, rideId);
                        startActivity(intent);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDriverIdDialog() {
        EditText etDriverId = new EditText(this);
        etDriverId.setHint("Driver ID");
        etDriverId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etDriverId.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Track Driver")
                .setView(etDriverId)
                .setPositiveButton("Open", (dialog, which) -> {
                    String input = etDriverId.getText().toString().trim();
                    if (input.isEmpty()) return;
                    try {
                        long driverId = Long.parseLong(input);
                        Intent intent = new Intent(this, AdminRideDetailsActivity.class);
                        intent.putExtra("driverId", driverId);
                        startActivity(intent);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private ArrayList<Ride> createTestRides() {
        ArrayList<Ride> rides = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm a");

        Ride ride1 = new Ride();
        ride1.id = 1L;
        ride1.driver = "John Doe";
        ride1.pickup = "Location B";
        ride1.dropoff = "Location A";
        ride1.price = 15.0;
        ride1.startingTime = LocalDateTime.parse("01.07.2024 10:00 AM", formatter);
        ride1.isFavouriteRoute = false;

        Ride ride2 = new Ride();
        ride2.id = 2L;
        ride2.driver = "Jane Smith";
        ride2.pickup = "Location D";
        ride2.dropoff = "Location C";
        ride2.price = 20.0;
        ride2.startingTime = LocalDateTime.parse("02.07.2024 12:00 PM", formatter);
        ride2.isFavouriteRoute = true;

        Ride ride3 = new Ride();
        ride3.id = 3L;
        ride3.driver = "Mike Johnson";
        ride3.pickup = "Location F";
        ride3.dropoff = "Location E";
        ride3.price = 38.0;
        ride3.startingTime = LocalDateTime.parse("03.07.2024 02:00 PM", formatter);
        ride3.isFavouriteRoute = false;

        Ride ride4 = new Ride();
        ride4.id = 4L;
        ride4.driver = "Mike Johnson";
        ride4.pickup = "Location F";
        ride4.dropoff = "Location E";
        ride4.price = 38.0;
        ride4.startingTime = LocalDateTime.parse("03.07.2024 02:00 PM", formatter);
        ride4.isFavouriteRoute = false;

        rides.add(ride1);
        rides.add(ride2);
        rides.add(ride3);
        rides.add(ride4);

        return rides;
    }
}