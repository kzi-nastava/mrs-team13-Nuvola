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
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nuvola.R;
import com.example.nuvola.network.AdminRideApi;
import com.example.nuvola.network.ApiClient;
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
import java.util.Locale;

import dto.CoordinateDTO;
import dto.DriverPositionUpdateDTO;
import dto.TrackingRideDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;

public class AdminRideDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AdminRideDetails";
    private static final String WS_URL = "ws://10.0.2.2:8080/ws-native";

    private MapView mapView;
    private TextView tvTitle, tvNoRide, tvPickup, tvDropoff, tvStartTime, tvPrice, tvPanic;
    private ScrollView scrollDetails;
    private Marker driverMarker;

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
        setContentView(R.layout.activity_admin_ride_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvNoRide = findViewById(R.id.tvNoRide);
        tvPickup = findViewById(R.id.tvPickup);
        tvDropoff = findViewById(R.id.tvDropoff);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvPrice = findViewById(R.id.tvPrice);
        tvPanic = findViewById(R.id.tvPanic);
        scrollDetails = findViewById(R.id.scrollDetails);
        mapView = findViewById(R.id.mapView);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        long driverId = getIntent().getLongExtra("driverId", -1);
        if (driverId == -1) { finish(); return; }

        tvTitle.setText("Track Driver #" + driverId);

        setupMap();
        loadRide(driverId);
        connectWebSocket(driverId);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(45.2671, 19.8335));
    }

    private void loadRide(long driverId) {
        AdminRideApi api = ApiClient.getRetrofit().create(AdminRideApi.class);
        api.getDriverCurrentRide(driverId).enqueue(new Callback<TrackingRideDTO>() {
            @Override
            public void onResponse(Call<TrackingRideDTO> call, retrofit2.Response<TrackingRideDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showRide(response.body());
                } else {
                    tvNoRide.setVisibility(View.VISIBLE);
                    scrollDetails.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TrackingRideDTO> call, Throwable t) {
                Log.w(TAG, "loadRide failed: " + t.getMessage());
                tvNoRide.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showRide(TrackingRideDTO ride) {
        tvNoRide.setVisibility(View.GONE);
        scrollDetails.setVisibility(View.VISIBLE);

        tvPickup.setText(nullStr(ride.pickup));
        tvDropoff.setText(nullStr(ride.dropoff));
        tvStartTime.setText(nullStr(ride.startingTime));
        tvPrice.setText(String.format(Locale.US, "%.2f RSD", ride.price));

        if (ride.isPanic) {
            tvPanic.setText("ACTIVE");
            tvPanic.setTextColor(Color.parseColor("#ef4444"));
        } else {
            tvPanic.setText("No");
            tvPanic.setTextColor(Color.parseColor("#22c55e"));
        }

        if (ride.route != null && ride.route.stops != null) {
            drawRoute(ride.route.stops);
        }
    }

    private void drawRoute(List<CoordinateDTO> stops) {
        if (stops.isEmpty()) return;

        List<GeoPoint> points = new ArrayList<>();
        for (CoordinateDTO c : stops) {
            points.add(new GeoPoint(c.latitude, c.longitude));
        }

        Polyline polyline = new Polyline();
        polyline.setPoints(points);
        polyline.getOutlinePaint().setColor(Color.parseColor("#6366f1"));
        polyline.getOutlinePaint().setStrokeWidth(8f);
        mapView.getOverlays().add(polyline);

        Marker pickup = new Marker(mapView);
        pickup.setPosition(points.get(0));
        pickup.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        pickup.setIcon(pinIcon(Color.parseColor("#22c55e")));
        pickup.setTitle("Pickup");
        mapView.getOverlays().add(pickup);

        if (points.size() > 1) {
            Marker dropoff = new Marker(mapView);
            dropoff.setPosition(points.get(points.size() - 1));
            dropoff.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            dropoff.setIcon(pinIcon(Color.parseColor("#ef4444")));
            dropoff.setTitle("Dropoff");
            mapView.getOverlays().add(dropoff);
        }

        try {
            BoundingBox box = BoundingBox.fromGeoPoints(points);
            mapView.post(() -> mapView.zoomToBoundingBox(box, true, 60));
        } catch (Exception e) {
            mapView.getController().setCenter(points.get(0));
        }

        mapView.invalidate();
    }

    private void connectWebSocket(long driverId) {
        if (destroyed) return;
        wsClient = new OkHttpClient();
        Request request = new Request.Builder().url(WS_URL).build();
        webSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                ws.send("CONNECT\naccept-version:1.0,1.1,1.2\nhost:10.0.2.2\n\n\0");
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
            public void onClosed(WebSocket ws, int code, String reason) {}
        });
    }

    private void handleStompFrame(WebSocket ws, String frame, long driverId) {
        if (frame.startsWith("CONNECTED")) {
            ws.send("SUBSCRIBE\nid:sub-driver\ndestination:/topic/position/" + driverId + "\n\n\0");
        } else if (frame.startsWith("MESSAGE")) {
            int bodyStart = frame.indexOf("\n\n");
            if (bodyStart < 0) bodyStart = frame.indexOf("\r\n\r\n");
            if (bodyStart >= 0) {
                String body = frame.substring(bodyStart + 2).replace("\0", "").trim();
                if (body.isEmpty()) return;
                try {
                    DriverPositionUpdateDTO update = gson.fromJson(body, DriverPositionUpdateDTO.class);
                    if (update != null) handler.post(() -> updateDriverMarker(update));
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + body, e);
                }
            }
        }
    }

    private void updateDriverMarker(DriverPositionUpdateDTO update) {
        if (update.toRemove) {
            if (driverMarker != null) {
                mapView.getOverlays().remove(driverMarker);
                driverMarker = null;
                mapView.invalidate();
            }
            tvNoRide.setVisibility(View.VISIBLE);
            scrollDetails.setVisibility(View.GONE);
            return;
        }

        tvNoRide.setVisibility(View.GONE);
        GeoPoint pos = new GeoPoint(update.latitude, update.longitude);

        if (driverMarker == null) {
            driverMarker = new Marker(mapView);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            driverMarker.setIcon(circleIcon());
            driverMarker.setTitle("Driver");
            mapView.getOverlays().add(driverMarker);
        }

        driverMarker.setPosition(pos);
        mapView.invalidate();
    }

    private BitmapDrawable circleIcon() {
        int size = 60;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.parseColor("#f97316"));
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
        int size = 40;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(color);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, fill);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.WHITE);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(4f);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2, stroke);
        return new BitmapDrawable(getResources(), bmp);
    }

    private String nullStr(String s) {
        return (s != null && !s.isEmpty()) ? s : "N/A";
    }

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    protected void onDestroy() {
        destroyed = true;
        handler.removeCallbacksAndMessages(null);
        if (webSocket != null) webSocket.close(1000, null);
        mapView.onDetach();
        super.onDestroy();
    }
}