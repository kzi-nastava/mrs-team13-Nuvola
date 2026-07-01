package com.example.nuvola.activities;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.network.ApiClient;
import com.example.nuvola.network.RideTrackingApi;
import com.example.nuvola.network.ServerConfig;
import com.example.nuvola.network.TokenStorage;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import dto.CoordinateDTO;
import dto.CreateReportDTO;
import dto.TrackingRideDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

public class RideTrackingActivity extends AppCompatActivity {

    private static final String TAG = "RideTrackingActivity";
    private static final String WS_URL = ServerConfig.WS_URL;
    private static final GeoPoint NOVI_SAD = new GeoPoint(45.2671, 19.8335);

    private MapView mapView;
    private TextView tvPickup, tvDropoff, tvPrice, tvNoRide, tvRemainingInfo;
    private LinearLayout layoutRideInfo;
    private MaterialButton btnReport;

    private final Gson gson = new Gson();
    private OkHttpClient wsClient;
    private final OkHttpClient httpClient = new OkHttpClient();
    private WebSocket webSocket;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean destroyed = false;

    private TrackingRideDTO currentRide;
    private Marker driverMarker;
    private String username;
    private GeoPoint dropoffPoint;
    private long lastOsrmCallAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidTileCache(getCacheDir());
        setContentView(R.layout.activity_ride_tracking);

        username = TokenStorage.getUserEmail(this);

        bindViews();
        setupMap();
        loadCurrentRide();
    }

    private void bindViews() {
        tvPickup = findViewById(R.id.tvPickup);
        tvDropoff = findViewById(R.id.tvDropoff);
        tvPrice = findViewById(R.id.tvPrice);
        tvNoRide = findViewById(R.id.tvNoRide);
        tvRemainingInfo = findViewById(R.id.tvRemainingInfo);
        layoutRideInfo = findViewById(R.id.layoutRideInfo);
        btnReport = findViewById(R.id.btnReport);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnReport.setOnClickListener(v -> showReportDialog());
    }

    private void setupMap() {
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(NOVI_SAD);
    }

    private void loadCurrentRide() {
        if (username.isEmpty()) {
            tvNoRide.setText("Not logged in.");
            return;
        }

        RideTrackingApi api = ApiClient.getRetrofit().create(RideTrackingApi.class);
        api.getCurrentRide(username).enqueue(new Callback<TrackingRideDTO>() {
            @Override
            public void onResponse(Call<TrackingRideDTO> call,
                                   retrofit2.Response<TrackingRideDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRide = response.body();
                    onRideLoaded(currentRide);
                } else {
                    tvNoRide.setText("No active ride found.");
                    Log.w(TAG, "No active ride: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TrackingRideDTO> call, Throwable t) {
                tvNoRide.setText("Could not load ride.");
                Log.e(TAG, "Failed to load ride", t);
            }
        });
    }

    private void onRideLoaded(TrackingRideDTO ride) {
        tvNoRide.setVisibility(View.GONE);
        layoutRideInfo.setVisibility(View.VISIBLE);
        btnReport.setVisibility(View.VISIBLE);

        tvPickup.setText(ride.pickup != null ? ride.pickup : "—");
        tvDropoff.setText(ride.dropoff != null ? ride.dropoff : "—");
        tvPrice.setText(String.format("%.2f RSD", ride.price));

        drawRoute(ride);
        connectWebSocket(ride.driverId);
    }

    private void drawRoute(TrackingRideDTO ride) {
        if (ride.route == null || ride.route.stops == null || ride.route.stops.isEmpty()) return;

        List<GeoPoint> points = new ArrayList<>();
        for (CoordinateDTO c : ride.route.stops) {
            points.add(new GeoPoint(c.latitude, c.longitude));
        }

        Polyline polyline = new Polyline();
        polyline.setPoints(points);
        polyline.getOutlinePaint().setColor(Color.parseColor("#6366f1"));
        polyline.getOutlinePaint().setStrokeWidth(8f);
        mapView.getOverlays().add(polyline);

        // Pickup marker (green)
        GeoPoint first = points.get(0);
        Marker pickupMarker = new Marker(mapView);
        pickupMarker.setPosition(first);
        pickupMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        pickupMarker.setIcon(pinIcon(Color.parseColor("#22c55e")));
        pickupMarker.setTitle("Pickup");
        mapView.getOverlays().add(pickupMarker);

        // Dropoff marker (red)
        GeoPoint last = points.get(points.size() - 1);
        dropoffPoint = last;
        Marker dropoffMarker = new Marker(mapView);
        dropoffMarker.setPosition(last);
        dropoffMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        dropoffMarker.setIcon(pinIcon(Color.parseColor("#ef4444")));
        dropoffMarker.setTitle("Dropoff");
        mapView.getOverlays().add(dropoffMarker);

        // Zoom to fit route
        try {
            BoundingBox box = BoundingBox.fromGeoPoints(points);
            mapView.post(() -> mapView.zoomToBoundingBox(box, true, 80));
        } catch (Exception e) {
            mapView.getController().setCenter(first);
        }

        mapView.invalidate();
    }

    private void connectWebSocket(long driverId) {
        if (destroyed) return;
        Log.d(TAG, "Connecting WebSocket for driver " + driverId);
        wsClient = new OkHttpClient();
        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket opened, sending CONNECT");
                ws.send("CONNECT\naccept-version:1.0,1.1,1.2\nhost:" + ServerConfig.HOST + "\n\n\0");
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                handleStompFrame(ws, text, driverId);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket failure: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + code);
            }
        });
    }

    private void handleStompFrame(WebSocket ws, String frame, long driverId) {
        if (frame.startsWith("CONNECTED")) {
            Log.d(TAG, "STOMP CONNECTED, subscribing to driver " + driverId);
            ws.send("SUBSCRIBE\nid:sub-tracking\ndestination:/topic/position/" + driverId + "\n\n\0");
        } else if (frame.startsWith("MESSAGE")) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart < 0) bodyStart = frame.indexOf("\r\n\r\n");
            if (bodyStart >= 0) {
                String body = frame.substring(bodyStart + 2).replace("\0", "").trim();
                if (body.isEmpty()) return;
                try {
                    dto.DriverPositionUpdateDTO update = gson.fromJson(body, dto.DriverPositionUpdateDTO.class);
                    if (update != null) handler.post(() -> updateDriverMarker(update));
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + body, e);
                }
            }
        } else if (frame.startsWith("ERROR")) {
            Log.e(TAG, "STOMP ERROR: " + frame);
        }
    }

    private void updateDriverMarker(dto.DriverPositionUpdateDTO update) {
        if (update.toRemove) {
            if (driverMarker != null) {
                mapView.getOverlays().remove(driverMarker);
                driverMarker = null;
                mapView.invalidate();
            }
            tvRemainingInfo.setText("Ride ended.");
            return;
        }
        GeoPoint pos = new GeoPoint(update.latitude, update.longitude);
        if (driverMarker == null) {
            driverMarker = new Marker(mapView);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            driverMarker.setIcon(circleIcon());
            driverMarker.setTitle("Your driver");
            mapView.getOverlays().add(driverMarker);
        }
        driverMarker.setPosition(pos);
        mapView.invalidate();

        if (dropoffPoint != null) {
            calcRemaining(update.latitude, update.longitude,
                    dropoffPoint.getLatitude(), dropoffPoint.getLongitude());
        }
    }

    private void calcRemaining(double driverLat, double driverLng,
                                double destLat, double destLng) {
        long now = System.currentTimeMillis();
        if (now - lastOsrmCallAt < 5000) return;
        lastOsrmCallAt = now;

        // OSRM uses lon,lat order
        String url = String.format(Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=false",
                driverLng, driverLat, destLng, destLat);

        Request req = new Request.Builder().url(url).build();
        httpClient.newCall(req).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.w(TAG, "OSRM request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                try {
                    String body = response.body().string();
                    JsonObject json = gson.fromJson(body, JsonObject.class);
                    JsonArray routes = json.getAsJsonArray("routes");
                    if (routes == null || routes.size() == 0) return;
                    JsonObject route = routes.get(0).getAsJsonObject();
                    double distM = route.get("distance").getAsDouble();
                    double durSec = route.get("duration").getAsDouble();
                    double km = distM / 1000.0;
                    int min = (int) Math.max(1, Math.round(durSec / 60.0));
                    handler.post(() -> tvRemainingInfo.setText(
                            String.format(Locale.US, "%.1f km · ~%d min", km, min)));
                } catch (Exception e) {
                    Log.w(TAG, "OSRM parse error", e);
                }
            }
        });
    }

    private void showReportDialog() {
        if (currentRide == null) return;

        EditText etReason = new EditText(this);
        etReason.setHint("Describe the issue...");
        etReason.setMinLines(3);
        etReason.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(this)
                .setTitle("Create Report")
                .setView(etReason)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Please describe the issue.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitReport(reason);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitReport(String reason) {
        CreateReportDTO dto = new CreateReportDTO();
        dto.reason = reason;
        dto.authorUsername = username;
        dto.rideId = currentRide.id;

        RideTrackingApi api = ApiClient.getRetrofit().create(RideTrackingApi.class);
        api.createReport(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RideTrackingActivity.this,
                            "Report submitted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RideTrackingActivity.this,
                            "Failed to submit report.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RideTrackingActivity.this,
                        "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private BitmapDrawable circleIcon() {
        int size = 60;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.parseColor("#6366f1"));
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3, fill);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.WHITE);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(5f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 3, stroke);
        return new BitmapDrawable(getResources(), bmp);
    }

    private BitmapDrawable pinIcon(int color) {
        int w = 36, h = 36;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(color);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(w / 2f, h / 2f, w / 2f - 2, fill);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.WHITE);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(4f);
        canvas.drawCircle(w / 2f, h / 2f, w / 2f - 2, stroke);
        return new BitmapDrawable(getResources(), bmp);
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