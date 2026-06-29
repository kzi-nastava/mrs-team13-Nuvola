package com.example.nuvola.network;

import dto.CreateReportDTO;
import dto.TrackingRideDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RideTrackingApi {
    @GET("api/rides/now/user/{username}")
    Call<TrackingRideDTO> getCurrentRide(@Path("username") String username);

    @POST("api/rides/report")
    Call<Void> createReport(@Body CreateReportDTO report);
}