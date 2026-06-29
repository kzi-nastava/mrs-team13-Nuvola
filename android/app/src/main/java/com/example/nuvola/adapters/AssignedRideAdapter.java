package com.example.nuvola.adapters;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

import dto.DriverAssignedRideDTO;

public class AssignedRideAdapter
        extends RecyclerView.Adapter<AssignedRideAdapter.AssignedRideViewHolder> {

    public interface RideActionListener {
        void onAction(DriverAssignedRideDTO ride);
    }

    private final List<DriverAssignedRideDTO> rides;

    private final RideActionListener startListener;
    private final RideActionListener cancelListener;
    private final RideActionListener stopListener;
    private final RideActionListener endListener;
    private final RideActionListener panicListener;

    public AssignedRideAdapter(
            List<DriverAssignedRideDTO> rides,
            RideActionListener startListener,
            RideActionListener cancelListener,
            RideActionListener stopListener,
            RideActionListener endListener,
            RideActionListener panicListener
    ) {
        this.rides = rides;
        this.startListener = startListener;
        this.cancelListener = cancelListener;
        this.stopListener = stopListener;
        this.endListener = endListener;
        this.panicListener = panicListener;
    }

    @NonNull
    @Override
    public AssignedRideViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_assigned_ride,
                        parent,
                        false
                );

        return new AssignedRideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull AssignedRideViewHolder holder,
            int position
    ) {
        DriverAssignedRideDTO ride = rides.get(position);

        String status = valueOrFallback(
                ride.getStatus(),
                "UNKNOWN"
        ).toUpperCase(Locale.ROOT);

        boolean isCurrentRide =
                "IN_PROGRESS".equals(status);

        boolean isScheduledRide =
                "SCHEDULED".equals(status);

        boolean hasActiveRide =
                containsActiveRide();

        holder.tvRideSection.setText(
                isCurrentRide
                        ? "Current ride"
                        : "Upcoming ride"
        );

        holder.tvRoute.setText(
                buildRoute(ride)
        );

        holder.tvSchedule.setText(
                "Scheduled: " + formatSchedule(ride)
        );

        holder.tvStops.setText(
                "Stops: " + formatStops(ride)
        );

        holder.tvPassengers.setText(
                "Passengers: " + formatPassengers(ride)
        );

        holder.tvPrice.setText(
                "Price: " + formatPrice(ride)
        );

        holder.tvStatus.setText(status);

        configureCardAppearance(
                holder,
                isCurrentRide
        );

        configureActions(
                holder,
                isCurrentRide,
                isScheduledRide,
                hasActiveRide
        );

        holder.btnStart.setOnClickListener(view -> {
            if (!hasActiveRide
                    && startListener != null) {

                startListener.onAction(ride);
            }
        });

        holder.btnCancel.setOnClickListener(view -> {
            if (cancelListener != null) {
                cancelListener.onAction(ride);
            }
        });

        holder.btnStop.setOnClickListener(view -> {
            if (stopListener != null) {
                stopListener.onAction(ride);
            }
        });

        holder.btnEndRide.setOnClickListener(view -> {
            if (endListener != null) {
                endListener.onAction(ride);
            }
        });

        holder.btnPanic.setOnClickListener(view -> {
            if (panicListener != null) {
                panicListener.onAction(ride);
            }
        });
    }

    private void configureCardAppearance(
            AssignedRideViewHolder holder,
            boolean isCurrentRide
    ) {
        if (isCurrentRide) {
            holder.cardRide.setCardBackgroundColor(
                    Color.parseColor("#F8FFFB")
            );

            holder.cardRide.setCardElevation(
                    dpToPx(holder.itemView, 8)
            );

            holder.leftAccent.setVisibility(
                    View.VISIBLE
            );

            holder.leftAccent.setBackgroundColor(
                    Color.parseColor("#16A34A")
            );

            holder.tvRideSection.setTextColor(
                    Color.parseColor("#166534")
            );

            holder.statusContainer.setBackgroundResource(
                    R.drawable.bg_status_in_progress
            );

            holder.tvStatus.setTextColor(
                    Color.parseColor("#166534")
            );

        } else {
            holder.cardRide.setCardBackgroundColor(
                    Color.WHITE
            );

            holder.cardRide.setCardElevation(
                    dpToPx(holder.itemView, 4)
            );

            holder.leftAccent.setVisibility(
                    View.GONE
            );

            holder.tvRideSection.setTextColor(
                    Color.parseColor("#1F2937")
            );

            holder.statusContainer.setBackgroundResource(
                    R.drawable.bg_status_scheduled
            );

            holder.tvStatus.setTextColor(
                    Color.parseColor("#8A5A00")
            );
        }
    }

    private void configureActions(
            AssignedRideViewHolder holder,
            boolean isCurrentRide,
            boolean isScheduledRide,
            boolean hasActiveRide
    ) {
        if (isCurrentRide) {
            holder.btnPanic.setVisibility(
                    View.VISIBLE
            );

            holder.btnStop.setVisibility(
                    View.VISIBLE
            );

            holder.btnEndRide.setVisibility(
                    View.VISIBLE
            );

            holder.btnStart.setVisibility(
                    View.GONE
            );

            holder.btnCancel.setVisibility(
                    View.GONE
            );

            return;
        }

        if (isScheduledRide) {
            holder.btnPanic.setVisibility(
                    View.GONE
            );

            holder.btnStop.setVisibility(
                    View.GONE
            );

            holder.btnEndRide.setVisibility(
                    View.GONE
            );

            holder.btnStart.setVisibility(
                    View.VISIBLE
            );

            holder.btnCancel.setVisibility(
                    View.VISIBLE
            );

            boolean canStart =
                    !hasActiveRide;

            holder.btnStart.setEnabled(
                    canStart
            );

            holder.btnStart.setAlpha(
                    canStart ? 1.0f : 0.45f
            );

            holder.btnStart.setText(
                    canStart
                            ? "Start"
                            : "Active ride"
            );

            return;
        }

        holder.btnPanic.setVisibility(
                View.GONE
        );

        holder.btnStop.setVisibility(
                View.GONE
        );

        holder.btnEndRide.setVisibility(
                View.GONE
        );

        holder.btnStart.setVisibility(
                View.GONE
        );

        holder.btnCancel.setVisibility(
                View.GONE
        );
    }

    private boolean containsActiveRide() {
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

    private String buildRoute(
            DriverAssignedRideDTO ride
    ) {
        String from = valueOrFallback(
                ride.getPickup(),
                "Pickup"
        );

        String to = valueOrFallback(
                ride.getDropoff(),
                "Destination"
        );

        return from + " → " + to;
    }

    private String formatSchedule(
            DriverAssignedRideDTO ride
    ) {
        String scheduledTime =
                ride.getScheduledTime();

        if (scheduledTime == null
                || scheduledTime.trim().isEmpty()) {

            return "Now";
        }

        return scheduledTime
                .replace("T", " ")
                .replaceAll("\\.\\d+$", "");
    }

    private String formatStops(
            DriverAssignedRideDTO ride
    ) {
        List<String> stops =
                ride.getStops();

        if (stops == null
                || stops.isEmpty()) {

            return "None";
        }

        return TextUtils.join(
                ", ",
                stops
        );
    }

    private String formatPassengers(
            DriverAssignedRideDTO ride
    ) {
        List<String> passengers =
                ride.getPassengers();

        if (passengers == null
                || passengers.isEmpty()) {

            return "None";
        }

        return TextUtils.join(
                ", ",
                passengers
        );
    }

    private String formatPrice(
            DriverAssignedRideDTO ride
    ) {
        Double price =
                ride.getPrice();

        if (price == null) {
            return "Not calculated";
        }

        return String.format(
                Locale.US,
                "%.2f RSD",
                price
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

    private float dpToPx(
            View view,
            int dp
    ) {
        return dp
                * view.getResources()
                .getDisplayMetrics()
                .density;
    }

    @Override
    public int getItemCount() {
        return rides == null
                ? 0
                : rides.size();
    }

    static class AssignedRideViewHolder
            extends RecyclerView.ViewHolder {

        final View leftAccent;
        final CardView cardRide;

        final TextView tvRideSection;
        final TextView tvRoute;
        final TextView tvSchedule;
        final TextView tvStops;
        final TextView tvPassengers;
        final TextView tvPrice;
        final TextView tvStatus;

        final View statusContainer;

        final MaterialButton btnStart;
        final MaterialButton btnCancel;
        final MaterialButton btnPanic;
        final MaterialButton btnStop;
        final MaterialButton btnEndRide;

        AssignedRideViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            leftAccent =
                    itemView.findViewById(
                            R.id.leftAccent
                    );

            cardRide =
                    itemView.findViewById(
                            R.id.cardRide
                    );

            tvRideSection =
                    itemView.findViewById(
                            R.id.tvRideSection
                    );

            tvRoute =
                    itemView.findViewById(
                            R.id.tvRoute
                    );

            tvSchedule =
                    itemView.findViewById(
                            R.id.tvSchedule
                    );

            tvStops =
                    itemView.findViewById(
                            R.id.tvStops
                    );

            tvPassengers =
                    itemView.findViewById(
                            R.id.tvPassengers
                    );

            tvPrice =
                    itemView.findViewById(
                            R.id.tvPrice
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvStatus
                    );

            statusContainer =
                    itemView.findViewById(
                            R.id.statusContainer
                    );

            btnStart =
                    itemView.findViewById(
                            R.id.btnStart
                    );

            btnCancel =
                    itemView.findViewById(
                            R.id.btnCancel
                    );

            btnPanic =
                    itemView.findViewById(
                            R.id.btnPanic
                    );

            btnStop =
                    itemView.findViewById(
                            R.id.btnStop
                    );

            btnEndRide =
                    itemView.findViewById(
                            R.id.btnEndRide
                    );
        }
    }
}