package com.example.nuvola.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nuvola.R;
import com.example.nuvola.model.Ride;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DriversRideHistoryListAdapter extends ArrayAdapter<Ride> {
//    public DriversRideHistoryListAdapter(@NonNull Context context, int resource, @NonNull List<Ride> objects) {
//        super(context, resource, objects);
//    }
    public DriversRideHistoryListAdapter(@NonNull Context context, @NonNull List<Ride> rides) {
        super(context, R.layout.drivers_history_ride_card, rides);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public Ride getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Ride ride =getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.drivers_history_ride_card, parent, false);
        }
        LinearLayout rideCard = convertView.findViewById(R.id.drivers_history_ride_card_item);
        TextView dateAndTime = convertView.findViewById(R.id.ride_date_time);
        TextView driverName = convertView.findViewById(R.id.driver_name);
        TextView pickup = convertView.findViewById(R.id.pickup_location);
        TextView dropoff = convertView.findViewById(R.id.dropoff_location);
        TextView price = convertView.findViewById(R.id.ride_price);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm a");
        ImageView favoriteIcon = convertView.findViewById(R.id.favorite_icon);

        if (ride != null) {
            dateAndTime.setText(ride.startingTime.format(formatter));
            driverName.setText(ride.driver);
            pickup.setText(ride.pickup);
            dropoff.setText(ride.dropoff);
            price.setText(String.valueOf(ride.price + " RSD"));

            updateFavoriteIcon(favoriteIcon, ride.isFavouriteRoute);
            favoriteIcon.setOnClickListener(null);
            favoriteIcon.setOnClickListener(v -> {

                ride.isFavouriteRoute = !ride.isFavouriteRoute;
                updateFavoriteIcon(favoriteIcon, ride.isFavouriteRoute);
            });
        }

        return convertView;
    }

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        if (isFavorite) {
            // Puna žuta zvezdica
            icon.setImageResource(android.R.drawable.btn_star_big_on);
            icon.setColorFilter(0xFFFFD700, PorterDuff.Mode.SRC_IN); // Zlatna boja
        } else {
            // Prazna siva zvezdica
            icon.setImageResource(android.R.drawable.btn_star_big_off);
            icon.setColorFilter(0xFF666666, PorterDuff.Mode.SRC_IN); // Siva boja
        }
    }
}
