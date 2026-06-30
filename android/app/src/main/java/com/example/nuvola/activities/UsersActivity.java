package com.example.nuvola.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.adapters.ProfileChangeRequestAdapter;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.AdminApi;
import com.example.nuvola.network.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dto.AdminUserDTO;
import dto.ProfileChangeRequestDTO;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersActivity extends AppCompatActivity {

    private static final String TAG = "UsersActivity";

    private MaterialButton btnTabCustomers;
    private MaterialButton btnTabDrivers;
    private MaterialButton btnTabRequests;

    private RecyclerView rvUsers;
    private LinearLayout layoutDriverExtras;

    private TextView tvSectionTitle;
    private TextView tvRegisterDriver;

    private EditText etSearch;
    private View searchWrap;
    private View requestsEmptyState;

    private AdminApi adminApi;

    private UserCardAdapter userAdapter;
    private ProfileChangeRequestAdapter requestAdapter;

    private enum Tab {
        CUSTOMERS,
        DRIVERS,
        REQUESTS
    }

    private Tab currentTab = Tab.CUSTOMERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        adminApi = ApiClient
                .getRetrofit()
                .create(AdminApi.class);

        bindViews();
        setupDrawer();
        setupAdapters();
        setupTabs();
        setupSearch();

        switchTab(Tab.CUSTOMERS);
    }

    private void bindViews() {
        btnTabCustomers =
                findViewById(R.id.btnTabCustomers);

        btnTabDrivers =
                findViewById(R.id.btnTabDrivers);

        btnTabRequests =
                findViewById(R.id.btnTabRequests);

        tvSectionTitle =
                findViewById(R.id.tvSectionTitle);

        rvUsers =
                findViewById(R.id.rvUsers);

        layoutDriverExtras =
                findViewById(R.id.layoutDriverExtras);

        tvRegisterDriver =
                findViewById(R.id.tvRegisterDriver);

        etSearch =
                findViewById(R.id.etSearch);

        searchWrap =
                findViewById(R.id.searchWrap);

        requestsEmptyState =
                findViewById(R.id.requestsEmptyState);
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout =
                findViewById(R.id.drawerLayout);

        NavigationView navigationView =
                findViewById(R.id.navView);

        View menuButton =
                findViewById(R.id.ivMenu);

        if (drawerLayout == null
                || navigationView == null) {
            return;
        }

        if (menuButton != null) {
            menuButton.setOnClickListener(
                    view -> drawerLayout.openDrawer(
                            GravityCompat.START
                    )
            );
        }

        NavigationMenuManager.setup(
                this,
                drawerLayout,
                navigationView
        );
    }

    private void setupTabs() {
        btnTabCustomers.setOnClickListener(
                view -> switchTab(Tab.CUSTOMERS)
        );

        btnTabDrivers.setOnClickListener(
                view -> switchTab(Tab.DRIVERS)
        );

        btnTabRequests.setOnClickListener(
                view -> switchTab(Tab.REQUESTS)
        );

        tvRegisterDriver.setOnClickListener(
                view -> startActivity(
                        new Intent(
                                UsersActivity.this,
                                RegisterDriverActivity.class
                        )
                )
        );
    }

    private void setupAdapters() {
        rvUsers.setLayoutManager(
                new LinearLayoutManager(this)
        );

        userAdapter =
                new UserCardAdapter(
                        this,
                        new ArrayList<>(),
                        false,
                        new UserCardAdapter.UserActionListener() {

                            @Override
                            public void onBlock(
                                    AdminUserDTO user
                            ) {
                                showBlockDialog(user);
                            }

                            @Override
                            public void onUnblock(
                                    AdminUserDTO user
                            ) {
                                showUnblockDialog(user);
                            }

                            @Override
                            public void onInfo(
                                    AdminUserDTO user
                            ) {
                                Intent intent = new Intent(
                                        UsersActivity.this,
                                        AdminRideDetailsActivity.class
                                );
                                intent.putExtra("driverId", user.id);
                                startActivity(intent);
                            }
                        }
                );

        requestAdapter =
                new ProfileChangeRequestAdapter(
                        new ArrayList<>(),
                        new ProfileChangeRequestAdapter
                                .RequestActionListener() {

                            @Override
                            public void onApprove(
                                    ProfileChangeRequestDTO request
                            ) {
                                confirmApproveRequest(request);
                            }

                            @Override
                            public void onReject(
                                    ProfileChangeRequestDTO request
                            ) {
                                confirmRejectRequest(request);
                            }
                        }
                );

        rvUsers.setAdapter(userAdapter);
    }

    private void switchTab(Tab tab) {
        currentTab = tab;

        requestsEmptyState.setVisibility(
                View.GONE
        );

        rvUsers.setVisibility(
                View.VISIBLE
        );

        updateTabButtons();

        switch (tab) {
            case CUSTOMERS:
                tvSectionTitle.setText(
                        "All customers"
                );

                layoutDriverExtras.setVisibility(
                        View.GONE
                );

                searchWrap.setVisibility(
                        View.GONE
                );

                rvUsers.setAdapter(userAdapter);

                loadCustomers();
                break;

            case DRIVERS:
                tvSectionTitle.setText(
                        "All drivers"
                );

                layoutDriverExtras.setVisibility(
                        View.VISIBLE
                );

                searchWrap.setVisibility(
                        View.VISIBLE
                );

                rvUsers.setAdapter(userAdapter);

                loadDrivers(null);
                break;

            case REQUESTS:
                tvSectionTitle.setText(
                        "Profile Change Requests"
                );

                layoutDriverExtras.setVisibility(
                        View.GONE
                );

                searchWrap.setVisibility(
                        View.GONE
                );

                rvUsers.setAdapter(requestAdapter);

                loadProfileChangeRequests();
                break;
        }
    }

    private void updateTabButtons() {
        int darkBlue =
                getResources().getColor(
                        R.color.dark_blue,
                        getTheme()
                );

        int white = Color.WHITE;

        updateTabButton(
                btnTabCustomers,
                currentTab == Tab.CUSTOMERS,
                darkBlue,
                white
        );

        updateTabButton(
                btnTabDrivers,
                currentTab == Tab.DRIVERS,
                darkBlue,
                white
        );

        updateTabButton(
                btnTabRequests,
                currentTab == Tab.REQUESTS,
                darkBlue,
                white
        );
    }

    private void updateTabButton(
            MaterialButton button,
            boolean selected,
            int darkBlue,
            int white
    ) {
        button.setBackgroundTintList(
                ColorStateList.valueOf(
                        selected
                                ? darkBlue
                                : white
                )
        );

        button.setTextColor(
                selected
                        ? white
                        : darkBlue
        );
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence text,
                            int start,
                            int before,
                            int count
                    ) {
                        if (currentTab != Tab.DRIVERS) {
                            return;
                        }

                        String search =
                                text.toString()
                                        .trim();

                        loadDrivers(
                                search.isEmpty()
                                        ? null
                                        : search
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {
                    }
                }
        );
    }

    private void loadCustomers() {
        adminApi.getRegisteredUsers()
                .enqueue(
                        new Callback<List<AdminUserDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<AdminUserDTO>> call,

                                    @NonNull
                                    Response<List<AdminUserDTO>> response
                            ) {
                                if (currentTab != Tab.CUSTOMERS) {
                                    return;
                                }

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    userAdapter.setUsers(
                                            response.body(),
                                            false
                                    );

                                } else {
                                    showRequestError(
                                            "Customers could not be loaded",
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<AdminUserDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                Log.e(
                                        TAG,
                                        "loadCustomers failure",
                                        throwable
                                );

                                showNetworkError(throwable);
                            }
                        }
                );
    }

    private void loadDrivers(
            String search
    ) {
        adminApi.getDrivers(search)
                .enqueue(
                        new Callback<List<AdminUserDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<AdminUserDTO>> call,

                                    @NonNull
                                    Response<List<AdminUserDTO>> response
                            ) {
                                if (currentTab != Tab.DRIVERS) {
                                    return;
                                }

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    userAdapter.setUsers(
                                            response.body(),
                                            true
                                    );

                                } else {
                                    showRequestError(
                                            "Drivers could not be loaded",
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<AdminUserDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                Log.e(
                                        TAG,
                                        "loadDrivers failure",
                                        throwable
                                );

                                showNetworkError(throwable);
                            }
                        }
                );
    }

    private void loadProfileChangeRequests() {
        requestsEmptyState.setVisibility(
                View.GONE
        );

        rvUsers.setVisibility(
                View.VISIBLE
        );

        adminApi.getProfileChangeRequests()
                .enqueue(
                        new Callback<
                                List<ProfileChangeRequestDTO>
                                >() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<ProfileChangeRequestDTO>> call,

                                    @NonNull
                                    Response<List<ProfileChangeRequestDTO>> response
                            ) {
                                if (currentTab != Tab.REQUESTS) {
                                    return;
                                }

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    List<ProfileChangeRequestDTO> requests =
                                            response.body();

                                    requestAdapter.setRequests(
                                            requests
                                    );

                                    boolean empty =
                                            requests.isEmpty();

                                    requestsEmptyState.setVisibility(
                                            empty
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );

                                    rvUsers.setVisibility(
                                            empty
                                                    ? View.GONE
                                                    : View.VISIBLE
                                    );

                                } else {
                                    requestAdapter.setRequests(
                                            new ArrayList<>()
                                    );

                                    requestsEmptyState.setVisibility(
                                            View.GONE
                                    );

                                    rvUsers.setVisibility(
                                            View.VISIBLE
                                    );

                                    showRequestError(
                                            "Profile change requests "
                                                    + "could not be loaded",
                                            response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<ProfileChangeRequestDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                requestsEmptyState.setVisibility(
                                        View.GONE
                                );

                                rvUsers.setVisibility(
                                        View.VISIBLE
                                );

                                Log.e(
                                        TAG,
                                        "loadProfileChangeRequests failure",
                                        throwable
                                );

                                showNetworkError(throwable);
                            }
                        }
                );
    }

    private void confirmApproveRequest(
            ProfileChangeRequestDTO request
    ) {
        if (request == null
                || request.id == null) {
            return;
        }

        showProfileRequestConfirmationDialog(
                "Approve request",
                "Approve profile changes for "
                        + safeValue(
                        request.driverName,
                        "this driver"
                )
                        + "?",
                "Approve",
                false,
                () -> approveRequest(request.id)
        );
    }

    private void confirmRejectRequest(
            ProfileChangeRequestDTO request
    ) {
        if (request == null
                || request.id == null) {
            return;
        }

        showProfileRequestConfirmationDialog(
                "Reject request",
                "Reject profile changes for "
                        + safeValue(
                        request.driverName,
                        "this driver"
                )
                        + "?",
                "Reject",
                true,
                () -> rejectRequest(request.id)
        );
    }

    private void showProfileRequestConfirmationDialog(
            String title,
            String message,
            String actionText,
            boolean destructive,
            Runnable action
    ) {
        View dialogView =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.dialog_confirm_profile_request,
                                null,
                                false
                        );

        TextView titleText =
                dialogView.findViewById(
                        R.id.tvConfirmDialogTitle
                );

        TextView messageText =
                dialogView.findViewById(
                        R.id.tvConfirmDialogMessage
                );

        MaterialButton cancelButton =
                dialogView.findViewById(
                        R.id.btnConfirmDialogCancel
                );

        MaterialButton actionButton =
                dialogView.findViewById(
                        R.id.btnConfirmDialogAction
                );

        titleText.setText(title);
        messageText.setText(message);
        actionButton.setText(actionText);

        int actionColor =
                destructive
                        ? Color.parseColor("#E32017")
                        : getResources().getColor(
                        R.color.dark_blue,
                        getTheme()
                );

        actionButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        actionColor
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create();

        dialog.setOnShowListener(listener -> {
            Window window =
                    dialog.getWindow();

            if (window == null) {
                return;
            }

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            int width =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.88f
                    );

            window.setLayout(
                    width,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setDimAmount(0.55f);

            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );
        });

        cancelButton.setOnClickListener(
                view -> dialog.dismiss()
        );

        actionButton.setOnClickListener(
                view -> {
                    dialog.dismiss();

                    if (action != null) {
                        action.run();
                    }
                }
        );

        dialog.show();
    }

    private void approveRequest(
            Long requestId
    ) {
        adminApi.approveProfileChangeRequest(
                requestId
        ).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<Void> call,

                            @NonNull
                            Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    UsersActivity.this,
                                    "Request approved.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadProfileChangeRequests();

                        } else {
                            showRequestError(
                                    "Request could not be approved",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<Void> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        showNetworkError(throwable);
                    }
                }
        );
    }

    private void rejectRequest(
            Long requestId
    ) {
        adminApi.rejectProfileChangeRequest(
                requestId
        ).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<Void> call,

                            @NonNull
                            Response<Void> response
                    ) {
                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    UsersActivity.this,
                                    "Request rejected.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadProfileChangeRequests();

                        } else {
                            showRequestError(
                                    "Request could not be rejected",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<Void> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        showNetworkError(throwable);
                    }
                }
        );
    }

    private void showBlockDialog(
            AdminUserDTO user
    ) {
        Dialog dialog =
                new Dialog(this);

        dialog.setContentView(
                R.layout.dialog_block_user
        );

        Window window =
                dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            window.setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.88f
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setGravity(Gravity.CENTER);

            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            window.setDimAmount(0.6f);
        }

        TextView title =
                dialog.findViewById(
                        R.id.tvBlockTitle
                );

        title.setText(
                "Block "
                        + safeValue(
                        user.firstName,
                        "user"
                )
                        + "?"
        );

        EditText reasonInput =
                dialog.findViewById(
                        R.id.etBlockReason
                );

        dialog.findViewById(
                R.id.btnCancelBlock
        ).setOnClickListener(
                view -> dialog.dismiss()
        );

        dialog.findViewById(
                R.id.btnConfirmBlock
        ).setOnClickListener(
                view -> {
                    String reason =
                            reasonInput.getText()
                                    .toString()
                                    .trim();

                    Map<String, String> body =
                            new HashMap<>();

                    if (!reason.isEmpty()) {
                        body.put(
                                "blockingReason",
                                reason
                        );
                    }

                    blockUser(
                            user,
                            body,
                            dialog
                    );
                }
        );

        dialog.show();
    }

    private void blockUser(
            AdminUserDTO user,
            Map<String, String> body,
            Dialog dialog
    ) {
        adminApi.blockUser(
                user.id,
                body
        ).enqueue(
                new Callback<AdminUserDTO>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<AdminUserDTO> call,

                            @NonNull
                            Response<AdminUserDTO> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            userAdapter.updateUser(
                                    response.body()
                            );

                            dialog.dismiss();

                        } else {
                            showRequestError(
                                    "User could not be blocked",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<AdminUserDTO> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        showNetworkError(throwable);
                    }
                }
        );
    }

    private void showUnblockDialog(
            AdminUserDTO user
    ) {
        Dialog dialog =
                new Dialog(this);

        dialog.setContentView(
                R.layout.dialog_unblock_user
        );

        Window window =
                dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            window.setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels
                                    * 0.88f
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            window.setGravity(Gravity.CENTER);

            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            window.setDimAmount(0.6f);
        }

        TextView title =
                dialog.findViewById(
                        R.id.tvUnblockTitle
                );

        title.setText(
                "Are you sure you want to unblock "
                        + safeValue(
                        user.firstName,
                        "this user"
                )
                        + "?"
        );

        dialog.findViewById(
                R.id.btnCancelUnblock
        ).setOnClickListener(
                view -> dialog.dismiss()
        );

        dialog.findViewById(
                R.id.btnConfirmUnblock
        ).setOnClickListener(
                view -> unblockUser(
                        user,
                        dialog
                )
        );

        dialog.show();
    }

    private void unblockUser(
            AdminUserDTO user,
            Dialog dialog
    ) {
        adminApi.unblockUser(
                user.id,
                new HashMap<>()
        ).enqueue(
                new Callback<AdminUserDTO>() {

                    @Override
                    public void onResponse(
                            @NonNull
                            Call<AdminUserDTO> call,

                            @NonNull
                            Response<AdminUserDTO> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            userAdapter.updateUser(
                                    response.body()
                            );

                            dialog.dismiss();

                        } else {
                            showRequestError(
                                    "User could not be unblocked",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull
                            Call<AdminUserDTO> call,

                            @NonNull
                            Throwable throwable
                    ) {
                        showNetworkError(throwable);
                    }
                }
        );
    }

    private void showRequestError(
            String message,
            int code
    ) {
        Log.e(
                TAG,
                message + ": " + code
        );

        Toast.makeText(
                this,
                message
                        + ". Error code: "
                        + code,
                Toast.LENGTH_LONG
        ).show();
    }

    private void showNetworkError(
            Throwable throwable
    ) {
        Log.e(
                TAG,
                "Network error",
                throwable
        );

        String message =
                throwable == null
                        || throwable.getMessage() == null
                        ? "Unknown network error"
                        : throwable.getMessage();

        Toast.makeText(
                this,
                "Network error: " + message,
                Toast.LENGTH_LONG
        ).show();
    }

    private String safeValue(
            String value,
            String fallback
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            return fallback;
        }

        return value;
    }
}