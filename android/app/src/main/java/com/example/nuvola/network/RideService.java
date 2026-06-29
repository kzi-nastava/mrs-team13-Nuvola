package com.example.nuvola.network;

import dto.ScheduledRideDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideService {

    private static RideTrackingApi rideApi;

    public interface EndRideCallback {
        void onRideEnded(long scheduledRideId);
        void onRideEndedNoNext();
        void onError(String message);
    }

    public interface ScheduledRideCallback {
        void onSuccess(ScheduledRideDTO ride);
        void onError(String message);
    }

    public interface StartRideCallback {
        void onSuccess();
        void onError(String message);
    }

    public static RideTrackingApi api() {
        if (rideApi == null) {
            rideApi = ApiClient.getRetrofit().create(RideTrackingApi.class);
        }
        return rideApi;
    }

    public static void endRide(String username, EndRideCallback callback) {
        api().endRide(username).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Failed to end ride. Code: " + response.code());
                    return;
                }
                try {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String raw = body.string().trim();
                        if (!raw.isEmpty()) {
                            long scheduledRideId = Long.parseLong(raw);
                            if (scheduledRideId != 0) {
                                callback.onRideEnded(scheduledRideId);
                                return;
                            }
                        }
                    }
                } catch (Exception ignored) {}
                callback.onRideEndedNoNext();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public static void getScheduledRide(long rideId, ScheduledRideCallback callback) {
        api().getScheduledRide(rideId).enqueue(new Callback<ScheduledRideDTO>() {
            @Override
            public void onResponse(Call<ScheduledRideDTO> call, Response<ScheduledRideDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load scheduled ride. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ScheduledRideDTO> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public static void startRide(long rideId, StartRideCallback callback) {
        api().startRide(rideId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to start ride. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}