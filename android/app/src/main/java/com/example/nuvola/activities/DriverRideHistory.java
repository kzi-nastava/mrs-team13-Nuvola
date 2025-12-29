package com.example.nuvola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
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

        // Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            ArrayList<Ride> rides = createTestRides();
            DriversRideHistoryFragment fragment = DriversRideHistoryFragment.newInstance(rides);

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
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DriverRideHistory.this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            startActivity(new Intent(DriverRideHistory.this, LoginActivity.class));
        }


        // close drawer after click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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