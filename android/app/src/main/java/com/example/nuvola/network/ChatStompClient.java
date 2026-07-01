package com.example.nuvola.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatStompClient {

    private static final String TAG = "ChatStompClient";
    private static final String WS_URL = ServerConfig.WS_URL;

    // STOMP frames must be terminated with a NULL byte (\0).
    // Without it Spring's BufferingStompDecoder keeps the frame in its buffer
    // and merges the next frame into it, causing parse errors.
    private static final String NUL = "\0";

    public interface MessageListener {
        void onMessage(ChatMessageDTO message);
    }

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WebSocket webSocket;
    private String token;
    private String topic;
    private MessageListener listener;
    private boolean destroyed = false;

    public void connect(String token, String topic, MessageListener listener) {
        this.token = token;
        this.topic = topic;
        this.listener = listener;
        this.destroyed = false;
        doConnect();
    }

    private void doConnect() {
        if (destroyed) return;

        Request request = new Request.Builder()
                .url(WS_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                String frame = "CONNECT\n" +
                        "accept-version:1.2\n" +
                        "host:" + ServerConfig.HOST + "\n" +
                        "Authorization:Bearer " + token + "\n" +
                        "\n" + NUL;
                ws.send(frame);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                handleFrame(ws, text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WS failure", t);
                mainHandler.postDelayed(() -> {
                    if (!destroyed) doConnect();
                }, 5000);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WS closed: " + reason);
                if (code != 1000 && !destroyed) {
                    mainHandler.postDelayed(() -> {
                        if (!destroyed) doConnect();
                    }, 5000);
                }
            }
        });
    }

    private void handleFrame(WebSocket ws, String frame) {
        if (frame.startsWith("CONNECTED")) {
            String sub = "SUBSCRIBE\n" +
                    "id:sub-chat\n" +
                    "destination:" + topic + "\n" +
                    "\n" + NUL;
            ws.send(sub);
            Log.d(TAG, "Subscribed to " + topic);
        } else if (frame.startsWith("MESSAGE")) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart >= 0) {
                // Only strip trailing NUL/whitespace — preserve JSON content (spaces matter)
                String body = frame.substring(bodyStart + 2).replace("\0", "").trim();
                try {
                    ChatMessageDTO msg = gson.fromJson(body, ChatMessageDTO.class);
                    if (msg != null && listener != null) {
                        mainHandler.post(() -> listener.onMessage(msg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + body, e);
                }
            }
        } else if (frame.startsWith("ERROR")) {
            Log.e(TAG, "STOMP ERROR: " + frame);
        }
    }

    public void sendUserMessage(long senderId, String content) {
        if (webSocket == null) return;
        String body = gson.toJson(new UserSendPayload(senderId, content));
        int len = body.getBytes(StandardCharsets.UTF_8).length;
        String frame = "SEND\n" +
                "destination:/app/chats/send\n" +
                "content-type:application/json\n" +
                "content-length:" + len + "\n" +
                "\n" + body + NUL;
        webSocket.send(frame);
    }

    public void sendAdminMessage(long senderId, long receiverId, String content) {
        if (webSocket == null) return;
        String body = gson.toJson(new AdminSendPayload(senderId, receiverId, content));
        int len = body.getBytes(StandardCharsets.UTF_8).length;
        String frame = "SEND\n" +
                "destination:/app/admin/chats/send\n" +
                "content-type:application/json\n" +
                "content-length:" + len + "\n" +
                "\n" + body + NUL;
        webSocket.send(frame);
    }

    public void disconnect() {
        destroyed = true;
        mainHandler.removeCallbacksAndMessages(null);
        if (webSocket != null) {
            webSocket.close(1000, "done");
            webSocket = null;
        }
    }

    private static class UserSendPayload {
        long senderId;
        String content;
        UserSendPayload(long senderId, String content) {
            this.senderId = senderId;
            this.content = content;
        }
    }

    private static class AdminSendPayload {
        long senderId;
        long receiverId;
        String content;
        AdminSendPayload(long senderId, long receiverId, String content) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.content = content;
        }
    }
}