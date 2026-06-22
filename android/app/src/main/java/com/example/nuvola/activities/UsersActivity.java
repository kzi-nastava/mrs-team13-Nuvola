package com.example.nuvola.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.app.Dialog;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.network.AdminApi;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dto.AdminUserDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity {

    private static final String TAG = "UsersActivity";

    // Tabs
    private MaterialButton btnTabCustomers, btnTabDrivers, btnTabRequests;

    // Sections
    private RecyclerView rvUsers;
    private LinearLayout layoutDriverExtras;
    private TextView tvSectionTitle, tvRegisterDriver;
    private EditText etSearch;
    private View searchWrap;

    private AdminApi adminApi;
    private UserCardAdapter adapter;

    private enum Tab { CUSTOMERS, DRIVERS, REQUESTS }
    private Tab currentTab = Tab.CUSTOMERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        adminApi = ApiClient.getRetrofit().create(AdminApi.class);

        bindViews();
        setupDrawer();
        setupTabs();
        setupSearch();
        setupAdapter();

        // default: show customers
        switchTab(Tab.CUSTOMERS);
    }

    // ────────────────────────────────────────────
    //  BIND
    // ────────────────────────────────────────────

    private void bindViews() {
        btnTabCustomers  = findViewById(R.id.btnTabCustomers);
        btnTabDrivers    = findViewById(R.id.btnTabDrivers);
        btnTabRequests   = findViewById(R.id.btnTabRequests);
        tvSectionTitle   = findViewById(R.id.tvSectionTitle);
        rvUsers          = findViewById(R.id.rvUsers);
        layoutDriverExtras = findViewById(R.id.layoutDriverExtras);
        tvRegisterDriver = findViewById(R.id.tvRegisterDriver);
        etSearch         = findViewById(R.id.etSearch);
        searchWrap       = findViewById(R.id.searchWrap);
    }

    // ────────────────────────────────────────────
    //  DRAWER
    // ────────────────────────────────────────────

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView    = findViewById(R.id.navView);

        if (drawerLayout == null || navView == null) return;

        findViewById(R.id.ivMenu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                TokenStorage.clear(this);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else if (id == R.id.nav_account) {
                startActivity(new Intent(this, ProfileActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // ────────────────────────────────────────────
    //  TABS
    // ────────────────────────────────────────────

    private void setupTabs() {
        btnTabCustomers.setOnClickListener(v -> switchTab(Tab.CUSTOMERS));
        btnTabDrivers.setOnClickListener(v   -> switchTab(Tab.DRIVERS));
        btnTabRequests.setOnClickListener(v  -> switchTab(Tab.REQUESTS));

        // "Register a new driver" — logika dolazi kasnije
        tvRegisterDriver.setOnClickListener(v -> {
            // TODO: navigate to RegisterDriverActivity
            Toast.makeText(this, "Register driver — coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        updateTabButtons();

        switch (tab) {
            case CUSTOMERS:
                tvSectionTitle.setText("All customers");
                layoutDriverExtras.setVisibility(View.GONE);
                searchWrap.setVisibility(View.GONE);
                loadCustomers();
                break;

            case DRIVERS:
                tvSectionTitle.setText("All drivers");
                layoutDriverExtras.setVisibility(View.VISIBLE);
                searchWrap.setVisibility(View.VISIBLE);
                loadDrivers(null);
                break;

            case REQUESTS:
                tvSectionTitle.setText("Profile Change Requests");
                layoutDriverExtras.setVisibility(View.GONE);
                searchWrap.setVisibility(View.GONE);
                // No logic yet — show empty list
                if (adapter != null) adapter.setUsers(new ArrayList<>(), false);
                break;
        }
    }

    private void updateTabButtons() {
        // active = dark bg, inactive = white bg
        int dark  = getResources().getColor(R.color.dark_blue, null);
        int white = android.graphics.Color.WHITE;
        int darkText  = android.graphics.Color.WHITE;
        int whiteText = dark;

        btnTabCustomers.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                currentTab == Tab.CUSTOMERS ? dark : white));
        btnTabCustomers.setTextColor(currentTab == Tab.CUSTOMERS ? darkText : whiteText);

        btnTabDrivers.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                currentTab == Tab.DRIVERS ? dark : white));
        btnTabDrivers.setTextColor(currentTab == Tab.DRIVERS ? darkText : whiteText);

        btnTabRequests.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                currentTab == Tab.REQUESTS ? dark : white));
        btnTabRequests.setTextColor(currentTab == Tab.REQUESTS ? darkText : whiteText);
    }

    // ────────────────────────────────────────────
    //  SEARCH
    // ────────────────────────────────────────────

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentTab == Tab.DRIVERS) {
                    String q = s.toString().trim();
                    loadDrivers(q.isEmpty() ? null : q);
                }
            }
        });
    }

    // ────────────────────────────────────────────
    //  ADAPTER
    // ────────────────────────────────────────────

    private void setupAdapter() {
        adapter = new UserCardAdapter(this, new ArrayList<>(), false,
                new UserCardAdapter.UserActionListener() {
                    @Override
                    public void onBlock(AdminUserDTO user) {
                        showBlockDialog(user);
                    }

                    @Override
                    public void onUnblock(AdminUserDTO user) {
                        showUnblockDialog(user);
                    }

                    @Override
                    public void onInfo(AdminUserDTO user) {
                        // logika dolazi kasnije
                        Toast.makeText(UsersActivity.this,
                                "Info " + user.firstName + " — coming soon",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    // ────────────────────────────────────────────
    //  BLOCK DIALOG
    // ────────────────────────────────────────────

    private void showBlockDialog(AdminUserDTO user) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_block_user);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setGravity(Gravity.CENTER);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.6f);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvBlockTitle);
        tvTitle.setText("Block " + user.firstName + "?");

        EditText etReason = dialog.findViewById(R.id.etBlockReason);

        dialog.findViewById(R.id.btnCancelBlock).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnConfirmBlock).setOnClickListener(v -> {
            String reason = etReason.getText().toString().trim();
            Map<String, String> body = new HashMap<>();
            if (!reason.isEmpty()) body.put("blockingReason", reason);

            adminApi.blockUser(user.id, body).enqueue(new Callback<AdminUserDTO>() {
                @Override
                public void onResponse(Call<AdminUserDTO> call, Response<AdminUserDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateUser(response.body());
                        dialog.dismiss();
                    } else {
                        Toast.makeText(UsersActivity.this, "Block failed", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<AdminUserDTO> call, Throwable t) {
                    Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    // ────────────────────────────────────────────
    //  UNBLOCK DIALOG
    // ────────────────────────────────────────────

    private void showUnblockDialog(AdminUserDTO user) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_unblock_user);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.88),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setGravity(Gravity.CENTER);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.6f);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvUnblockTitle);
        tvTitle.setText("Are you sure you want to unblock " + user.firstName + "?");

        dialog.findViewById(R.id.btnCancelUnblock).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnConfirmUnblock).setOnClickListener(v -> {
            adminApi.unblockUser(user.id, new HashMap<>()).enqueue(new Callback<AdminUserDTO>() {
                @Override
                public void onResponse(Call<AdminUserDTO> call, Response<AdminUserDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.updateUser(response.body());
                        dialog.dismiss();
                    } else {
                        Toast.makeText(UsersActivity.this, "Unblock failed", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<AdminUserDTO> call, Throwable t) {
                    Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    // ────────────────────────────────────────────
    //  LOAD DATA
    // ────────────────────────────────────────────

    private void loadCustomers() {
        adminApi.getRegisteredUsers().enqueue(new Callback<List<AdminUserDTO>>() {
            @Override
            public void onResponse(Call<List<AdminUserDTO>> call, Response<List<AdminUserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setUsers(response.body(), false);
                } else {
                    Log.e(TAG, "loadCustomers error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AdminUserDTO>> call, Throwable t) {
                Log.e(TAG, "loadCustomers failure", t);
                Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDrivers(String search) {
        adminApi.getDrivers(search).enqueue(new Callback<List<AdminUserDTO>>() {
            @Override
            public void onResponse(Call<List<AdminUserDTO>> call, Response<List<AdminUserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setUsers(response.body(), true);
                } else {
                    Log.e(TAG, "loadDrivers error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<AdminUserDTO>> call, Throwable t) {
                Log.e(TAG, "loadDrivers failure", t);
                Toast.makeText(UsersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}