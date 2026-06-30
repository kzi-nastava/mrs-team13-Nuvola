package com.example.nuvola.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nuvola.R;
import com.example.nuvola.adapters.ChatMessagesAdapter;
import com.example.nuvola.navigation.NavigationMenuManager;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.ChatApi;
import com.example.nuvola.network.ChatMessageDTO;
import com.example.nuvola.network.ChatStompClient;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SupportChatActivity extends AppCompatActivity {

    public static final String EXTRA_IS_ADMIN =
            "is_admin";

    public static final String EXTRA_RECEIVER_USER_ID =
            "receiver_user_id";

    public static final String EXTRA_RECEIVER_NAME =
            "receiver_name";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RecyclerView rvMessages;

    private EditText etMessage;

    private MaterialButton btnSend;

    private TextView tvError;
    private TextView tvChatTitle;
    private TextView tvChatSubtitle;

    private ChatMessagesAdapter adapter;
    private ChatStompClient stompClient;

    private boolean isAdmin;

    private long myId;
    private long receiverUserId;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_support_chat
        );

        setupToolbarAndDrawer();
        bindViews();
        readIntentData();
        setupChat();

        String token =
                TokenStorage.getToken(this);

        myId =
                JwtRoleHelper.getUserId(token);

        long targetUserId =
                isAdmin
                        ? receiverUserId
                        : myId;

        if (targetUserId < 0) {
            showError(
                    "Invalid user session. "
                            + "Please log in again."
            );

            return;
        }

        loadHistory(targetUserId);
        connectWs(token, targetUserId);
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

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationMenuManager.setup(
                this,
                drawerLayout,
                navigationView
        );
    }

    private void bindViews() {
        tvChatTitle =
                findViewById(
                        R.id.tvChatTitle
                );

        tvChatSubtitle =
                findViewById(
                        R.id.tvChatSubtitle
                );

        tvError =
                findViewById(
                        R.id.tvChatError
                );

        rvMessages =
                findViewById(
                        R.id.rvMessages
                );

        etMessage =
                findViewById(
                        R.id.etMessage
                );

        btnSend =
                findViewById(
                        R.id.btnSend
                );
    }

    private void readIntentData() {
        isAdmin =
                getIntent()
                        .getBooleanExtra(
                                EXTRA_IS_ADMIN,
                                false
                        );

        receiverUserId =
                getIntent()
                        .getLongExtra(
                                EXTRA_RECEIVER_USER_ID,
                                -1
                        );

        String receiverName =
                getIntent()
                        .getStringExtra(
                                EXTRA_RECEIVER_NAME
                        );

        if (isAdmin) {
            String displayedName =
                    receiverName != null
                            && !receiverName
                            .trim()
                            .isEmpty()
                            ? receiverName
                            : "User #"
                            + receiverUserId;

            tvChatTitle.setText(
                    "Chat with "
                            + displayedName
            );

            tvChatSubtitle.setVisibility(
                    View.VISIBLE
            );

            tvChatSubtitle.setText(
                    "Admin view"
            );
        }
    }

    private void setupChat() {
        adapter =
                new ChatMessagesAdapter(myId);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);

        rvMessages.setLayoutManager(
                layoutManager
        );

        rvMessages.setAdapter(
                adapter
        );

        btnSend.setOnClickListener(
                view -> sendMessage()
        );

        etMessage.setOnEditorActionListener(
                (view, actionId, event) -> {
                    boolean sendAction =
                            actionId
                                    == EditorInfo.IME_ACTION_SEND;

                    boolean enterPressed =
                            event != null
                                    && event.getKeyCode()
                                    == KeyEvent.KEYCODE_ENTER
                                    && event.getAction()
                                    == KeyEvent.ACTION_DOWN;

                    if (sendAction
                            || enterPressed) {

                        sendMessage();
                        return true;
                    }

                    return false;
                }
        );
    }

    private void loadHistory(
            long userId
    ) {
        ChatApi api =
                ApiClient.getRetrofit()
                        .create(ChatApi.class);

        api.getChatMessages(userId)
                .enqueue(
                        new Callback<List<ChatMessageDTO>>() {

                            @Override
                            public void onResponse(
                                    @NonNull
                                    Call<List<ChatMessageDTO>> call,

                                    @NonNull
                                    Response<List<ChatMessageDTO>> response
                            ) {
                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    adapter.setMessages(
                                            response.body()
                                    );

                                    scrollToBottom();

                                } else {
                                    showError(
                                            "Could not load history ("
                                                    + response.code()
                                                    + ")"
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull
                                    Call<List<ChatMessageDTO>> call,

                                    @NonNull
                                    Throwable throwable
                            ) {
                                showError(
                                        "Could not load history: "
                                                + getThrowableMessage(
                                                throwable
                                        )
                                );
                            }
                        }
                );
    }

    private void connectWs(
            String token,
            long targetUserId
    ) {
        stompClient =
                new ChatStompClient();

        String topic =
                "/topic/chats/users/"
                        + targetUserId;

        stompClient.connect(
                token,
                topic,
                message -> {
                    adapter.addMessage(
                            message
                    );

                    scrollToBottom();
                }
        );
    }

    private void sendMessage() {
        String text =
                etMessage.getText()
                        .toString()
                        .trim();

        if (text.isEmpty()) {
            return;
        }

        if (stompClient == null) {
            showError(
                    "Chat connection is unavailable."
            );

            return;
        }

        if (isAdmin) {
            if (receiverUserId < 0) {
                Toast.makeText(
                        this,
                        "No receiver specified",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            stompClient.sendAdminMessage(
                    myId,
                    receiverUserId,
                    text
            );

        } else {
            stompClient.sendUserMessage(
                    myId,
                    text
            );
        }

        etMessage.setText("");
    }

    private void scrollToBottom() {
        int count =
                adapter.getItemCount();

        if (count > 0) {
            rvMessages.smoothScrollToPosition(
                    count - 1
            );
        }
    }

    private void showError(
            String message
    ) {
        tvError.setText(message);

        tvError.setVisibility(
                View.VISIBLE
        );
    }

    private String getThrowableMessage(
            Throwable throwable
    ) {
        if (throwable == null
                || throwable.getMessage() == null
                || throwable.getMessage()
                .trim()
                .isEmpty()) {

            return "Unknown network error";
        }

        return throwable.getMessage();
    }

    @Override
    protected void onDestroy() {
        if (stompClient != null) {
            stompClient.disconnect();
        }

        super.onDestroy();
    }
}