package com.example.nuvola.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.nuvola.R;
import com.example.nuvola.network.JwtRoleHelper;
import com.example.nuvola.network.ServerConfig;
import com.example.nuvola.network.TokenStorage;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class DriverLocationPublisherService extends Service {

    private static final String TAG = "DriverLocService";
    private static final String WS_URL = ServerConfig.WS_URL;
    private static final String CHANNEL_ID = "nuvola_driver_loc";
    private static final int FOREGROUND_ID = 2;
    private static final long LOCATION_MIN_TIME_MS = 2000;
    private static final long RECONNECT_DELAY_MS = 5000;
    private static final String NUL = "\0";

    private OkHttpClient httpClient;
    private WebSocket webSocket;
    private String token;
    private long driverId;
    private boolean connected = false;
    private boolean destroyed = false;

    private LocationManager locationManager;
    private final Gson gson = new Gson();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            sendPosition(location.getLatitude(), location.getLongitude());
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        httpClient = new OkHttpClient();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_ID, buildForegroundNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);

        token = TokenStorage.getToken(this);
        if (token == null) { stopSelf(); return START_NOT_STICKY; }

        driverId = JwtRoleHelper.getUserId(token);
        if (driverId < 0) { stopSelf(); return START_NOT_STICKY; }

        if (!startLocationUpdates()) { stopSelf(); return START_NOT_STICKY; }
        connectWs();
        return START_STICKY;
    }

    private boolean startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            return false;
        }

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        String provider = gpsEnabled ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME_MS, 0f,
                locationListener, Looper.getMainLooper());
        Log.d(TAG, "Location updates started via " + provider);
        return true;
    }

    private void connectWs() {
        if (destroyed) return;

        Request request = new Request.Builder()
                .url(WS_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
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
                if (text.startsWith("CONNECTED")) {
                    connected = true;
                    Log.d(TAG, "STOMP connected");
                } else if (text.startsWith("ERROR")) {
                    Log.e(TAG, "STOMP ERROR: " + text);
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WS failure, reconnecting", t);
                connected = false;
                handler.postDelayed(() -> { if (!destroyed) connectWs(); }, RECONNECT_DELAY_MS);
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                connected = false;
                Log.d(TAG, "WS closed: " + reason);
                if (code != 1000 && !destroyed) {
                    handler.postDelayed(() -> { if (!destroyed) connectWs(); }, RECONNECT_DELAY_MS);
                }
            }
        });
    }

    private void sendPosition(double lat, double lng) {
        if (!connected || webSocket == null) return;

        String body = gson.toJson(new PositionPayload(driverId, lat, lng));
        int len = body.getBytes(StandardCharsets.UTF_8).length;
        String frame = "SEND\n" +
                "destination:/app/vehicle/position\n" +
                "content-type:application/json\n" +
                "content-length:" + len + "\n" +
                "\n" + body + NUL;
        webSocket.send(frame);
        Log.d(TAG, "Sent position lat=" + lat + " lng=" + lng);
    }

    private void sendRemove() {
        if (!connected || webSocket == null) return;

        String body = gson.toJson(new RemovePayload(driverId));
        int len = body.getBytes(StandardCharsets.UTF_8).length;
        String frame = "SEND\n" +
                "destination:/app/vehicle/position\n" +
                "content-type:application/json\n" +
                "content-length:" + len + "\n" +
                "\n" + body + NUL;
        webSocket.send(frame);
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        handler.removeCallbacksAndMessages(null);
        sendRemove();
        locationManager.removeUpdates(locationListener);
        if (webSocket != null) webSocket.close(1000, "Service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Driver Location", NotificationManager.IMPORTANCE_MIN);
        nm.createNotificationChannel(ch);
    }

    private Notification buildForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Nuvola")
                .setContentText("Sharing your location")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    private static class PositionPayload {
        long driverId;
        double latitude;
        double longitude;

        PositionPayload(long driverId, double latitude, double longitude) {
            this.driverId = driverId;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static class RemovePayload {
        long driverId;
        boolean toRemove = true;

        RemovePayload(long driverId) {
            this.driverId = driverId;
        }
    }
}