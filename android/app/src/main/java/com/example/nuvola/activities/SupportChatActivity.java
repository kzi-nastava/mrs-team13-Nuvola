package com.example.nuvola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
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
import com.example.nuvola.adapters.ChatMessagesAdapter;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ChatApi;
import com.example.nuvola.network.ChatMessageDTO;
import com.example.nuvola.network.ChatStompClient;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.TokenStorage;
import com.example.nuvola.services.StompNotificationService;
import com.example.nuvola.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportChatActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_IS_ADMIN = "is_admin";
    public static final String EXTRA_RECEIVER_USER_ID = "receiver_user_id";
    public static final String EXTRA_RECEIVER_NAME = "receiver_name";

    private DrawerLayout drawerLayout;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private MaterialButton btnSend;
    private TextView tvError, tvChatTitle, tvChatSubtitle;

    private ChatMessagesAdapter adapter;
    private ChatStompClient stompClient;

    private boolean isAdmin;
    private long myId;
    private long receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

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

        boolean isAdminRole = "ADMIN".equals(TokenStorage.getUserRole(this));
        navView.getMenu().findItem(R.id.nav_change_price).setVisible(isAdminRole);

        tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatSubtitle = findViewById(R.id.tvChatSubtitle);
        tvError = findViewById(R.id.tvChatError);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        String token = TokenStorage.getToken(this);
        myId = JwtRoleHelper.getUserId(token);
        isAdmin = getIntent().getBooleanExtra(EXTRA_IS_ADMIN, false);
        receiverUserId = getIntent().getLongExtra(EXTRA_RECEIVER_USER_ID, -1);
        String receiverName = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);

        if (isAdmin) {
            tvChatTitle.setText("Chat with " + (receiverName != null ? receiverName : "User #" + receiverUserId));
            tvChatSubtitle.setVisibility(View.VISIBLE);
            tvChatSubtitle.setText("Admin view");
        }

        adapter = new ChatMessagesAdapter(myId);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });

        long targetUserId = isAdmin ? receiverUserId : myId;
        if (targetUserId < 0) {
            showError("Invalid user session. Please log in again.");
            return;
        }

        loadHistory(targetUserId);
        connectWs(token, targetUserId);
    }

    private void loadHistory(long userId) {
        ChatApi api = ApiClient.getRetrofit().create(ChatApi.class);
        api.getChatMessages(userId).enqueue(new Callback<List<ChatMessageDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatMessageDTO>> call,
                                   @NonNull Response<List<ChatMessageDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setMessages(response.body());
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatMessageDTO>> call, @NonNull Throwable t) {
                showError("Could not load history: " + t.getMessage());
            }
        });
    }

    private void connectWs(String token, long targetUserId) {
        stompClient = new ChatStompClient();
        String topic = "/topic/chats/users/" + targetUserId;
        stompClient.connect(token, topic, msg -> {
            if (adapter.getItemCount() > 0) {
                // skip duplicate if same id as last
            }
            adapter.addMessage(msg);
            scrollToBottom();
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        if (isAdmin) {
            if (receiverUserId < 0) {
                Toast.makeText(this, "No receiver specified", Toast.LENGTH_SHORT).show();
                return;
            }
            stompClient.sendAdminMessage(myId, receiverUserId, text);
        } else {
            stompClient.sendUserMessage(myId, text);
        }

        etMessage.setText("");
    }

    private void scrollToBottom() {
        int count = adapter.getItemCount();
        if (count > 0) rvMessages.smoothScrollToPosition(count - 1);
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) stompClient.disconnect();
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
        } else if (id == R.id.nav_support_chat) {
            // already here (user flow)
        } else if (id == R.id.nav_logout) {
            stopService(new Intent(this, StompNotificationService.class));
            if (stompClient != null) stompClient.disconnect();
            TokenStorage.clear(this);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}