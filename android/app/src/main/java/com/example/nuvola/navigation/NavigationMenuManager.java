package com.example.nuvola.navigation;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.nuvola.R;
import com.example.nuvola.activities.AdminInboxActivity;
import com.example.nuvola.activities.AdminRideDetailsActivity;
import com.example.nuvola.activities.ChangePriceActivity;
import com.example.nuvola.activities.DriverRideHistory;
import com.example.nuvola.activities.NotificationsActivity;
import com.example.nuvola.activities.ProfileActivity;
import com.example.nuvola.activities.ReportsActivity;
import com.example.nuvola.activities.RideOrderActivity;
import com.example.nuvola.activities.RideTrackingActivity;
import com.example.nuvola.activities.StartRideActivity;
import com.example.nuvola.activities.SupportChatActivity;
import com.example.nuvola.activities.UsersActivity;
import com.example.nuvola.activities.VehicleMapActivity;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.AuthApi;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.services.DriverLocationPublisherService;
import com.example.nuvola.services.StompNotificationService;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NavigationMenuManager {

    private NavigationMenuManager() {
        // Utility klasa se ne instancira.
    }

    public static void setup(
            AppCompatActivity activity,
            DrawerLayout drawerLayout,
            NavigationView navigationView
    ) {
        if (activity == null
                || drawerLayout == null
                || navigationView == null) {

            return;
        }

        configureVisibility(
                activity,
                navigationView
        );

        navigationView.setNavigationItemSelectedListener(
                item -> handleNavigationItem(
                        activity,
                        drawerLayout,
                        item
                )
        );
    }

    private static void configureVisibility(
            AppCompatActivity activity,
            NavigationView navigationView
    ) {
        Menu menu =
                navigationView.getMenu();

        hideAllRoleSpecificItems(menu);

        String role =
                normalizeRole(
                        TokenStorage.getUserRole(activity)
                );

        showItem(
                menu,
                R.id.nav_home,
                true
        );

        showItem(
                menu,
                R.id.nav_reports,
                true
        );

        showItem(
                menu,
                R.id.nav_notifications,
                true
        );

        showItem(
                menu,
                R.id.nav_support_chat,
                true
        );

        showItem(
                menu,
                R.id.nav_account,
                true
        );

        showItem(
                menu,
                R.id.nav_logout,
                true
        );

        if ("ADMIN".equals(role)) {
            configureAdminMenu(menu);

        } else if ("DRIVER".equals(role)) {
            configureDriverMenu(menu);

        } else {
            configureCustomerMenu(menu);
        }
    }

    private static void hideAllRoleSpecificItems(
            Menu menu
    ) {
        showItem(menu, R.id.nav_users, false);
        showItem(menu, R.id.nav_history, false);
        showItem(menu, R.id.nav_order_ride, false);

        showItem(menu, R.id.nav_start_ride, false);
        showItem(menu, R.id.nav_ridehistory, false);
        showItem(menu, R.id.nav_track_ride, false);

        showItem(menu, R.id.nav_change_price, false);
        showItem(menu, R.id.nav_driver_ride_details, false);
        showItem(menu, R.id.nav_admin_inbox, false);
    }

    private static void configureAdminMenu(
            Menu menu
    ) {
        showItem(menu, R.id.nav_users, true);
        showItem(menu, R.id.nav_change_price, true);
        showItem(menu, R.id.nav_ridehistory, true);
        showItem(menu, R.id.nav_driver_ride_details, true);
        showItem(menu, R.id.nav_admin_inbox, true);

        showItem(menu, R.id.nav_history, false);
        showItem(menu, R.id.nav_order_ride, false);
        showItem(menu, R.id.nav_start_ride, false);
        showItem(menu, R.id.nav_track_ride, false);
    }

    private static void configureDriverMenu(
            Menu menu
    ) {
        showItem(menu, R.id.nav_start_ride, true);
        showItem(menu, R.id.nav_ridehistory, true);

        showItem(menu, R.id.nav_users, false);
        showItem(menu, R.id.nav_history, false);
        showItem(menu, R.id.nav_order_ride, false);
        showItem(menu, R.id.nav_track_ride, false);
        showItem(menu, R.id.nav_change_price, false);
        showItem(menu, R.id.nav_driver_ride_details, false);
        showItem(menu, R.id.nav_admin_inbox, false);
    }

    private static void configureCustomerMenu(
            Menu menu
    ) {
        showItem(menu, R.id.nav_history, true);
        showItem(menu, R.id.nav_order_ride, true);
        showItem(menu, R.id.nav_track_ride, true);

        showItem(menu, R.id.nav_users, false);
        showItem(menu, R.id.nav_start_ride, false);
        showItem(menu, R.id.nav_ridehistory, false);
        showItem(menu, R.id.nav_change_price, false);
        showItem(menu, R.id.nav_driver_ride_details, false);
        showItem(menu, R.id.nav_admin_inbox, false);
    }

    private static boolean handleNavigationItem(
            AppCompatActivity activity,
            DrawerLayout drawerLayout,
            MenuItem item
    ) {
        int id =
                item.getItemId();

        String role =
                normalizeRole(
                        TokenStorage.getUserRole(activity)
                );

        if (id == R.id.nav_home) {
            openHome(
                    activity,
                    role
            );

        } else if (id == R.id.nav_users) {
            openActivity(
                    activity,
                    UsersActivity.class
            );

        } else if (id == R.id.nav_history) {
            openActivity(
                    activity,
                    DriverRideHistory.class
            );

        } else if (id == R.id.nav_order_ride) {
            openActivity(
                    activity,
                    RideOrderActivity.class
            );

        } else if (id == R.id.nav_start_ride) {
            openActivity(
                    activity,
                    StartRideActivity.class
            );

        } else if (id == R.id.nav_ridehistory) {
            openActivity(
                    activity,
                    DriverRideHistory.class
            );

        } else if (id == R.id.nav_track_ride) {
            openActivity(
                    activity,
                    RideTrackingActivity.class
            );

        } else if (id == R.id.nav_change_price) {
            openActivity(
                    activity,
                    ChangePriceActivity.class
            );

        } else if (id == R.id.nav_driver_ride_details) {
            showDriverIdDialog(activity);

        } else if (id == R.id.nav_admin_inbox) {
            openActivity(
                    activity,
                    AdminInboxActivity.class
            );

        } else if (id == R.id.nav_reports) {
            openActivity(
                    activity,
                    ReportsActivity.class
            );

        } else if (id == R.id.nav_notifications) {
            openActivity(
                    activity,
                    NotificationsActivity.class
            );

        } else if (id == R.id.nav_support_chat) {
            openSupportChat(
                    activity,
                    role
            );

        } else if (id == R.id.nav_account) {
            openActivity(
                    activity,
                    ProfileActivity.class
            );

        } else if (id == R.id.nav_logout) {
            performLogout(activity);
        }

        drawerLayout.closeDrawer(
                GravityCompat.START
        );

        return true;
    }

    private static void openHome(
            AppCompatActivity activity,
            String role
    ) {
        if ("ADMIN".equals(role)) {
            openActivity(
                    activity,
                    UsersActivity.class
            );

        } else {
            openActivity(
                    activity,
                    VehicleMapActivity.class
            );
        }
    }

    private static void openSupportChat(
            AppCompatActivity activity,
            String role
    ) {
        if ("ADMIN".equals(role)) {
            openActivity(
                    activity,
                    AdminInboxActivity.class
            );

        } else {
            openActivity(
                    activity,
                    SupportChatActivity.class
            );
        }
    }

    private static void showDriverIdDialog(
            AppCompatActivity activity
    ) {
        EditText driverIdInput =
                new EditText(activity);

        driverIdInput.setHint(
                "Driver ID"
        );

        driverIdInput.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        int horizontalPadding =
                dpToPx(activity, 22);

        int verticalPadding =
                dpToPx(activity, 12);

        driverIdInput.setPadding(
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                verticalPadding
        );

        AlertDialog dialog =
                new AlertDialog.Builder(activity)
                        .setTitle("Track driver")
                        .setMessage(
                                "Enter the ID of the driver whose ride details you want to view."
                        )
                        .setView(driverIdInput)
                        .setPositiveButton(
                                "Open",
                                null
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                listener -> dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(
                        view -> {
                            String value =
                                    driverIdInput.getText()
                                            .toString()
                                            .trim();

                            if (value.isEmpty()) {
                                driverIdInput.setError(
                                        "Driver ID is required"
                                );

                                return;
                            }

                            try {
                                long driverId =
                                        Long.parseLong(value);

                                Intent intent =
                                        new Intent(
                                                activity,
                                                AdminRideDetailsActivity.class
                                        );

                                intent.putExtra(
                                        "driverId",
                                        driverId
                                );

                                activity.startActivity(intent);
                                dialog.dismiss();

                            } catch (NumberFormatException exception) {
                                driverIdInput.setError(
                                        "Invalid driver ID"
                                );
                            }
                        }
                )
        );

        dialog.show();
    }

    private static void performLogout(
            AppCompatActivity activity
    ) {
        AuthApi authApi =
                ApiClient.getRetrofit()
                        .create(AuthApi.class);

        authApi.logout()
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {
                                finishLogout(activity);
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable throwable
                            ) {
                                finishLogout(activity);
                            }
                        }
                );
    }

    private static void finishLogout(
            AppCompatActivity activity
    ) {
        activity.stopService(
                new Intent(
                        activity,
                        DriverLocationPublisherService.class
                )
        );

        activity.stopService(
                new Intent(
                        activity,
                        StompNotificationService.class
                )
        );

        TokenStorage.clear(activity);
        ApiClient.clearInstance();

        Intent intent =
                new Intent(
                        activity,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        activity.startActivity(intent);
        activity.finish();
    }

    private static void openActivity(
            AppCompatActivity currentActivity,
            Class<?> destination
    ) {
        if (destination.isInstance(currentActivity)) {
            return;
        }

        Intent intent =
                new Intent(
                        currentActivity,
                        destination
                );

        currentActivity.startActivity(intent);
    }

    private static void showItem(
            Menu menu,
            @IdRes int itemId,
            boolean visible
    ) {
        if (menu == null) {
            return;
        }

        MenuItem item =
                menu.findItem(itemId);

        if (item != null) {
            item.setVisible(visible);
        }
    }

    private static String normalizeRole(
            String role
    ) {
        if (role == null) {
            return "CUSTOMER";
        }

        String normalized =
                role.trim()
                        .toUpperCase();

        if ("USER".equals(normalized)
                || "PASSENGER".equals(normalized)
                || "CUSTOMER".equals(normalized)) {

            return "CUSTOMER";
        }

        return normalized;
    }

    private static int dpToPx(
            AppCompatActivity activity,
            int dp
    ) {
        return Math.round(
                dp
                        * activity.getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}