package com.example.nuvola.network;

import dto.ActiveRideResponse;
import dto.CancelRideRequestDTO;
import dto.CreateRideDTO;
import dto.CreateRideFromFavoriteDTO;
import dto.CreatedRideDTO;
import dto.StopRideRequestDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RideApi {

    @POST("/api/rides")
    Call<CreatedRideDTO> createRide(
            @Body CreateRideDTO dto
    );

    @POST("/api/rides/from-favorite/{favoriteRouteId}")
    Call<CreatedRideDTO> createRideFromFavorite(
            @Path("favoriteRouteId") Long favoriteRouteId,
            @Body CreateRideFromFavoriteDTO dto
    );

    @GET("/api/rides/active-ride")
    Call<ActiveRideResponse> activeRide();

    @PUT("/api/rides/{rideId}/start")
    Call<Void> startRide(
            @Path("rideId") Long rideId
    );

    @PUT("/api/rides/{rideId}/cancel/driver")
    Call<Object> cancelRideByDriver(
            @Path("rideId") Long rideId,
            @Body CancelRideRequestDTO request
    );

    @POST("/api/rides/{rideId}/panic")
    Call<Void> triggerPanic(
            @Path("rideId") Long rideId
    );

    @PATCH("/api/rides/{rideId}/stop")
    Call<CreatedRideDTO> stopRide(
            @Path("rideId") Long rideId,
            @Body StopRideRequestDTO request
    );

    @PUT("/api/rides/{username}/end")
    Call<Long> endRide(
            @Path("username") String username
    );
}