package com.example.nuvola.network;

import dto.AdminRideDetailsDTO;
import dto.TrackingRideDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AdminRideApi {
    @GET("api/admin/rides/{rideId}")
    Call<AdminRideDetailsDTO> getRideDetails(@Path("rideId") long rideId);

    @GET("api/admin/drivers/info/{driverId}")
    Call<TrackingRideDTO> getDriverCurrentRide(@Path("driverId") long driverId);
}