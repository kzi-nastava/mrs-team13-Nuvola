package com.example.nuvola.network;

import dto.CreateReportDTO;
import dto.TrackingRideDTO;
import dto.ScheduledRideDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RideTrackingApi {
    @GET("api/rides/now/user/{username}")
    Call<TrackingRideDTO> getCurrentRide(@Path("username") String username);

    @POST("api/rides/report")
    Call<Void> createReport(@Body CreateReportDTO report);

    @PUT("api/rides/{username}/end")
    Call<ResponseBody> endRide(@Path("username") String username);

    @GET("api/rides/scheduled-ride/{rideId}")
    Call<ScheduledRideDTO> getScheduledRide(@Path("rideId") long rideId);

    @PUT("api/rides/{rideId}/start")
    Call<Void> startRide(@Path("rideId") long rideId);
}