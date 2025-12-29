package com.example.nuvola.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.nuvola.R;
import com.example.nuvola.fragments.DriversRideHistoryFragment;
import com.example.nuvola.model.Ride;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class DriverRideHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_ride_history);
        Log.d("DriverRideHistory", "onCreate started");
        ArrayList<Ride> rides = createTestRides();
        if (savedInstanceState == null) {
            DriversRideHistoryFragment fragment = DriversRideHistoryFragment.newInstance(rides);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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