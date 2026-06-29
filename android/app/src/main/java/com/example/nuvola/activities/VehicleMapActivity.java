package com.example.nuvola.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.google.gson.Gson;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

import dto.DriverPositionUpdateDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class VehicleMapActivity extends AppCompatActivity {

    private static final String TAG = "VehicleMapActivity";
    private static final GeoPoint NOVI_SAD = new GeoPoint(45.2671, 19.8335);
    private static final String WS_URL = "ws://10.0.2.2:8080/ws-native";
    private static final long RECONNECT_DELAY_MS = 5000;

    private MapView mapView;
    private final Map<Long, Marker> driverMarkers = new HashMap<>();
    private final Gson gson = new Gson();
    private OkHttpClient wsClient;
    private WebSocket webSocket;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidTileCache(getCacheDir());
        setContentView(R.layout.activity_vehicle_map);

        setupMap();
        connectWebSocket();
    }

    private void setupMap() {
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(NOVI_SAD);
        Log.d(TAG, "Map setup done");
    }

    private void connectWebSocket() {
        if (destroyed) return;
        Log.d(TAG, "Connecting to WebSocket: " + WS_URL);
        wsClient = new OkHttpClient();
        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket opened, sending CONNECT");
                ws.send("CONNECT\naccept-version:1.0,1.1,1.2\nhost:10.0.2.2\n\n\0");
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                String safe = text.replace("\n", "\\n").replace("\r", "\\r").replace("\0", "\\0");
                Log.d(TAG, "RAW frame: [" + safe + "]");
                handleStompFrame(ws, text);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket FAILURE: " + t.getMessage(), t);
                scheduleReconnect();
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + code + " / " + reason);
                if (code != 1000) scheduleReconnect();
            }
        });
    }

    private void handleStompFrame(WebSocket ws, String frame) {
        if (frame.startsWith("CONNECTED")) {
            Log.d(TAG, "STOMP CONNECTED — sending SUBSCRIBE");
            ws.send("SUBSCRIBE\nid:sub-vehicles\ndestination:/topic/position/all\n\n\0");
        } else if (frame.startsWith("MESSAGE")) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart < 0) bodyStart = frame.indexOf("\r\n\r\n");
            if (bodyStart >= 0) {
                String body = frame.substring(bodyStart + 2)
                        .replace("\0", "")
                        .trim();
                Log.d(TAG, "MESSAGE body: " + body);
                if (body.isEmpty()) return;
                try {
                    DriverPositionUpdateDTO update = gson.fromJson(body, DriverPositionUpdateDTO.class);
                    if (update != null) {
                        handler.post(() -> processPositionUpdate(update));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error for body: " + body, e);
                }
            }
        } else if (frame.startsWith("ERROR")) {
            Log.e(TAG, "STOMP ERROR: " + frame);
        } else {
            Log.d(TAG, "Unhandled STOMP frame type: " + frame.split("\n")[0]);
        }
    }

    private void processPositionUpdate(DriverPositionUpdateDTO update) {
        if (update.toRemove) {
            Marker m = driverMarkers.remove(update.driverId);
            if (m != null) {
                mapView.getOverlays().remove(m);
                mapView.invalidate();
            }
            return;
        }
        boolean occupied = update.occupied != null ? update.occupied : false;
        addOrUpdateMarker(update.driverId, update.latitude, update.longitude, occupied);
    }

    private void addOrUpdateMarker(long id, double lat, double lng, boolean occupied) {
        Log.d(TAG, "addOrUpdateMarker id=" + id + " lat=" + lat + " lng=" + lng + " occupied=" + occupied);
        Marker existing = driverMarkers.get(id);
        if (existing == null) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(lat, lng));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marker.setIcon(circleIcon(occupied));
            marker.setTitle("Driver " + id + " — " + (occupied ? "BUSY" : "FREE"));
            mapView.getOverlays().add(marker);
            driverMarkers.put(id, marker);
        } else {
            existing.setPosition(new GeoPoint(lat, lng));
            existing.setIcon(circleIcon(occupied));
            existing.setTitle("Driver " + id + " — " + (occupied ? "BUSY" : "FREE"));
        }
        mapView.invalidate();
    }

    private BitmapDrawable circleIcon(boolean occupied) {
        int size = 60;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(occupied ? Color.parseColor("#f97316") : Color.parseColor("#22c55e"));
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3, fill);

        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.WHITE);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(5f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3, stroke);

        return new BitmapDrawable(getResources(), bmp);
    }

    private void scheduleReconnect() {
        handler.postDelayed(() -> {
            if (!destroyed) connectWebSocket();
        }, RECONNECT_DELAY_MS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        handler.removeCallbacksAndMessages(null);
        if (webSocket != null) webSocket.close(1000, null);
        mapView.onDetach();
        super.onDestroy();
    }
}