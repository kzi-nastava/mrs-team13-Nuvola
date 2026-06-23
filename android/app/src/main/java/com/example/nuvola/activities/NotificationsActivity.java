package com.example.nuvola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.adapters.NotificationsAdapter;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.NotificationApi;
import com.example.nuvola.network.NotificationDTO;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.services.StompNotificationService;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "NotificationsActivity";

    private DrawerLayout drawerLayout;
    private RecyclerView rvNotifications;
    private TextView tvError, tvEmpty;
    private MaterialButton btnRefresh;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        boolean isAdmin = "ADMIN".equals(TokenStorage.getUserRole(this));
        navView.getMenu().findItem(R.id.nav_change_price).setVisible(isAdmin);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        tvError = findViewById(R.id.tvError);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnRefresh = findViewById(R.id.btnRefresh);

        String token = TokenStorage.getToken(this);
        userId = JwtRoleHelper.getUserId(token);

        btnRefresh.setOnClickListener(v -> loadNotifications());
        loadNotifications();
    }

    private void loadNotifications() {
        tvError.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Loading...");

        NotificationApi api = ApiClient.getRetrofit().create(NotificationApi.class);
        api.getNotifications(userId).enqueue(new Callback<List<NotificationDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificationDTO>> call,
                                   @NonNull Response<List<NotificationDTO>> response) {
                btnRefresh.setEnabled(true);
                btnRefresh.setText("Refresh");
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificationDTO> list = response.body();
                    if (list.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvNotifications.setAdapter(new NotificationsAdapter(list));
                    }
                } else {
                    showError("Failed to load notifications (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificationDTO>> call, @NonNull Throwable t) {
                btnRefresh.setEnabled(true);
                btnRefresh.setText("Refresh");
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Load failed", t);
            }
        });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_history) {
            startActivity(new Intent(this, DriverRideHistory.class));
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_change_price) {
            startActivity(new Intent(this, ChangePriceActivity.class));
        } else if (id == R.id.nav_notifications) {
            // already here
        } else if (id == R.id.nav_logout) {
            stopService(new Intent(this, StompNotificationService.class));
            TokenStorage.clear(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}