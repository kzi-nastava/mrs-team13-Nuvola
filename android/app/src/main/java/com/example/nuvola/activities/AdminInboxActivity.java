package com.example.nuvola.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.nuvola.adapters.AdminInboxAdapter;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.AdminInboxPageDTO;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ChatApi;
import com.example.nuvola.network.ChatStompClient;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminInboxActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RecyclerView rvInbox;

    private TextView tvError;
    private TextView tvEmpty;

    private MaterialButton btnRefresh;

    private AdminInboxAdapter adapter;
    private ChatStompClient stompClient;

    private long myId;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_admin_inbox
        );

        setupToolbarAndDrawer();
        bindViews();
        setupInbox();

        String token =
                TokenStorage.getToken(this);

        myId =
                JwtRoleHelper.getUserId(token);

        loadAll();
        connectInboxWs(token);
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
                findViewById(R.id.drawer_layout);

        navigationView =
                findViewById(R.id.navView);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationMenuManager.setup(
                this,
                drawerLayout,
                navigationView
        );
    }

    private void bindViews() {
        tvError =
                findViewById(
                        R.id.tvInboxError
                );

        tvEmpty =
                findViewById(
                        R.id.tvInboxEmpty
                );

        btnRefresh =
                findViewById(
                        R.id.btnRefresh
                );

        rvInbox =
                findViewById(
                        R.id.rvInbox
                );
    }

    private void setupInbox() {
        adapter =
                new AdminInboxAdapter();

        rvInbox.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvInbox.setAdapter(adapter);

        adapter.setOnItemClickListener(
                item -> {
                    Intent intent =
                            new Intent(
                                    AdminInboxActivity.this,
                                    SupportChatActivity.class
                            );

                    intent.putExtra(
                            SupportChatActivity.EXTRA_IS_ADMIN,
                            true
                    );

                    intent.putExtra(
                            SupportChatActivity.EXTRA_RECEIVER_USER_ID,
                            item.userId
                    );

                    intent.putExtra(
                            SupportChatActivity.EXTRA_RECEIVER_NAME,
                            item.ownerName
                    );

                    startActivity(intent);
                }
        );

        btnRefresh.setOnClickListener(
                view -> loadAll()
        );
    }

    private void connectInboxWs(
            String token
    ) {
        stompClient =
                new ChatStompClient();

        stompClient.connect(
                token,
                "/topic/chats/users/all",
                message -> {
                    boolean found =
                            adapter.updateLastMessage(
                                    message
                            );

                    if (!found) {
                        loadAll();
                    }
                }
        );
    }

    private void loadAll() {
        tvError.setVisibility(
                View.GONE
        );

        tvEmpty.setVisibility(
                View.GONE
        );

        btnRefresh.setEnabled(false);
        btnRefresh.setText("Loading...");

        ChatApi api =
                ApiClient.getRetrofit()
                        .create(ChatApi.class);

        api.getAdminInbox(
                myId,
                0,
                500
        ).enqueue(
                new Callback<AdminInboxPageDTO>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<AdminInboxPageDTO> call,

                            @NonNull
                            Response<AdminInboxPageDTO> response
                    ) {
                        btnRefresh.setEnabled(true);
                        btnRefresh.setText("Refresh");

                        if (response.isSuccessful()
                                && response.body() != null) {

                            AdminInboxPageDTO page =
                                    response.body();

                            if (page.content == null
                                    || page.content.isEmpty()) {

                                tvEmpty.setVisibility(
                                        View.VISIBLE
                                );

                                adapter.setItems(
                                        java.util.Collections
                                                .emptyList()
                                );

                            } else {
                                adapter.setItems(
                                        page.content
                                );
                            }

                        } else {
                            showError(
                                    "Failed to load inbox ("
                                            + response.code()
                                            + ")"
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<AdminInboxPageDTO> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        btnRefresh.setEnabled(true);
                        btnRefresh.setText("Refresh");

                        String message =
                                throwable == null
                                        || throwable.getMessage() == null
                                        ? "Unknown network error"
                                        : throwable.getMessage();

                        showError(
                                "Network error: "
                                        + message
                        );
                    }
                }
        );
    }

    private void showError(
            String message
    ) {
        tvError.setText(message);

        tvError.setVisibility(
                View.VISIBLE
        );
    }

    @Override
    protected void onDestroy() {
        if (stompClient != null) {
            stompClient.disconnect();
        }

        super.onDestroy();
    }
}