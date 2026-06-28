package com.example.nuvola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.adapters.AdminInboxAdapter;
import com.example.nuvola.network.AdminInboxPageDTO;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ChatApi;
import com.example.nuvola.network.ChatStompClient;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.services.StompNotificationService;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminInboxActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView rvInbox;
    private TextView tvError, tvEmpty;
    private MaterialButton btnRefresh;

    private AdminInboxAdapter adapter;
    private ChatStompClient stompClient;
    private long myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inbox);

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

        navView.getMenu().findItem(R.id.nav_change_price).setVisible(true);

        tvError = findViewById(R.id.tvInboxError);
        tvEmpty = findViewById(R.id.tvInboxEmpty);
        btnRefresh = findViewById(R.id.btnRefresh);
        rvInbox = findViewById(R.id.rvInbox);

        String token = TokenStorage.getToken(this);
        myId = JwtRoleHelper.getUserId(token);

        adapter = new AdminInboxAdapter();
        rvInbox.setLayoutManager(new LinearLayoutManager(this));
        rvInbox.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(this, SupportChatActivity.class);
            intent.putExtra(SupportChatActivity.EXTRA_IS_ADMIN, true);
            intent.putExtra(SupportChatActivity.EXTRA_RECEIVER_USER_ID, item.userId);
            intent.putExtra(SupportChatActivity.EXTRA_RECEIVER_NAME, item.ownerName);
            startActivity(intent);
        });

        btnRefresh.setOnClickListener(v -> loadAll());

        loadAll();
        connectInboxWs(token);
    }

    private void connectInboxWs(String token) {
        stompClient = new ChatStompClient();
        stompClient.connect(token, "/topic/chats/users/all", msg -> {
            boolean found = adapter.updateLastMessage(msg);
            if (!found) {
                loadAll();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) stompClient.disconnect();
    }

    private void loadAll() {
        tvError.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        btnRefresh.setEnabled(false);
        btnRefresh.setText("Loading...");

        ChatApi api = ApiClient.getRetrofit().create(ChatApi.class);
        api.getAdminInbox(myId, 0, 500).enqueue(new Callback<AdminInboxPageDTO>() {
            @Override
            public void onResponse(@NonNull Call<AdminInboxPageDTO> call,
                                   @NonNull Response<AdminInboxPageDTO> response) {
                btnRefresh.setEnabled(true);
                btnRefresh.setText("Refresh");

                if (response.isSuccessful() && response.body() != null) {
                    AdminInboxPageDTO page = response.body();
                    if (page.content == null || page.content.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        adapter.setItems(java.util.Collections.emptyList());
                    } else {
                        adapter.setItems(page.content);
                    }
                } else {
                    showError("Failed to load inbox (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminInboxPageDTO> call, @NonNull Throwable t) {
                btnRefresh.setEnabled(true);
                btnRefresh.setText("Refresh");
                showError("Network error: " + t.getMessage());
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
        if (id == R.id.nav_ridehistory) {
            startActivity(new Intent(this, DriverRideHistory.class));
        } else if (id == R.id.nav_account) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_change_price) {
            startActivity(new Intent(this, ChangePriceActivity.class));
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
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