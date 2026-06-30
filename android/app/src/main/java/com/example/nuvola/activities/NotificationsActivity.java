package com.example.nuvola.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.adapters.NotificationsAdapter;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.NotificationApi;
import com.example.nuvola.network.NotificationDTO;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG =
            "NotificationsActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RecyclerView rvNotifications;

    private TextView tvError;
    private TextView tvEmpty;

    private MaterialButton btnRefresh;

    private long userId;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_notifications
        );

        setupToolbarAndDrawer();
        bindViews();
        setupNotificationsList();
        loadCurrentUser();

        btnRefresh.setOnClickListener(
                view -> loadNotifications()
        );

        loadNotifications();
    }

    private void setupToolbarAndDrawer() {
        Toolbar toolbar =
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

        navigationView.setCheckedItem(
                R.id.nav_notifications
        );
    }

    private void bindViews() {
        rvNotifications =
                findViewById(
                        R.id.rvNotifications
                );

        tvError =
                findViewById(
                        R.id.tvError
                );

        tvEmpty =
                findViewById(
                        R.id.tvEmpty
                );

        btnRefresh =
                findViewById(
                        R.id.btnRefresh
                );
    }

    private void setupNotificationsList() {
        rvNotifications.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvNotifications.setAdapter(
                new NotificationsAdapter(
                        Collections.emptyList()
                )
        );
    }

    private void loadCurrentUser() {
        String token =
                TokenStorage.getToken(this);

        userId =
                JwtRoleHelper.getUserId(token);

        if (userId < 0) {
            showError(
                    "Invalid user session. Please log in again."
            );
        }
    }

    private void loadNotifications() {
        if (userId < 0) {
            showError(
                    "Invalid user session. Please log in again."
            );

            return;
        }

        hideMessages();
        setLoading(true);

        NotificationApi api =
                ApiClient.getRetrofit()
                        .create(
                                NotificationApi.class
                        );

        api.getNotifications(userId)
                .enqueue(
                        new Callback<List<NotificationDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<NotificationDTO>> call,

                                    @NonNull
                                    Response<List<NotificationDTO>> response
                            ) {
                                setLoading(false);

                                if (!response.isSuccessful()) {
                                    clearNotifications();

                                    showError(
                                            "Failed to load notifications ("
                                                    + response.code()
                                                    + ")"
                                    );

                                    return;
                                }

                                List<NotificationDTO> notifications =
                                        response.body();

                                if (notifications == null
                                        || notifications.isEmpty()) {

                                    clearNotifications();

                                    tvEmpty.setVisibility(
                                            View.VISIBLE
                                    );

                                    return;
                                }

                                rvNotifications.setAdapter(
                                        new NotificationsAdapter(
                                                notifications
                                        )
                                );
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<NotificationDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                setLoading(false);
                                clearNotifications();

                                String message =
                                        throwable == null
                                                || throwable.getMessage() == null
                                                || throwable.getMessage()
                                                .trim()
                                                .isEmpty()
                                                ? "Unknown network error"
                                                : throwable.getMessage();

                                showError(
                                        "Network error: "
                                                + message
                                );

                                Log.e(
                                        TAG,
                                        "Load notifications failed",
                                        throwable
                                );
                            }
                        }
                );
    }

    private void clearNotifications() {
        rvNotifications.setAdapter(
                new NotificationsAdapter(
                        Collections.emptyList()
                )
        );
    }

    private void setLoading(
            boolean loading
    ) {
        btnRefresh.setEnabled(
                !loading
        );

        btnRefresh.setText(
                loading
                        ? "Loading..."
                        : "Refresh"
        );
    }

    private void hideMessages() {
        tvError.setVisibility(
                View.GONE
        );

        tvEmpty.setVisibility(
                View.GONE
        );
    }

    private void showError(
            String message
    ) {
        tvError.setText(message);

        tvError.setVisibility(
                View.VISIBLE
        );

        tvEmpty.setVisibility(
                View.GONE
        );
    }
}