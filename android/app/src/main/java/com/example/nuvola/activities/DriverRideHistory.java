package com.example.nuvola.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.nuvola.R;
import com.example.nuvola.fragments.DriversRideHistoryFragment;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.google.android.material.navigation.NavigationView;

public class DriverRideHistory extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_driver_ride_history
        );

        Log.d(
                "DriverRideHistory",
                "onCreate started"
        );

        setupToolbarAndDrawer();

        if (savedInstanceState == null) {
            showRideHistoryFragment();
        }

        navigationView.setCheckedItem(
                R.id.nav_ridehistory
        );
    }

    private void setupToolbarAndDrawer() {
        toolbar =
                findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setDisplayShowTitleEnabled(false);
        }

        drawerLayout =
                findViewById(
                        R.id.drawer_layout
                );

        navigationView =
                findViewById(
                        R.id.navView
                );

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                );

        drawerLayout.addDrawerListener(
                toggle
        );

        toggle.syncState();

        NavigationMenuManager.setup(
                this,
                drawerLayout,
                navigationView
        );
    }

    private void showRideHistoryFragment() {
        DriversRideHistoryFragment fragment =
                new DriversRideHistoryFragment();

        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction();

        transaction.replace(
                R.id.fragment_container,
                fragment
        );

        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(
            Menu menu
    ) {
        getMenuInflater().inflate(
                R.menu.toolbar_menu,
                menu
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            @NonNull MenuItem item
    ) {
        return super.onOptionsItemSelected(
                item
        );
    }
}