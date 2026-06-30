package com.example.nuvola.network;

import java.util.List;

import dto.CreateDriverDTO;
import dto.CreatedDriverDTO;
import dto.DriverAssignedRideDTO;
import dto.DriverRideHistoryItemDTO;
import dto.PageResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DriverApi {

    @POST("/api/drivers")
    Call<CreatedDriverDTO> createDriver(
            @Body CreateDriverDTO dto
    );

    @Multipart
    @POST("/api/drivers/{id}/picture")
    Call<Object> uploadDriverPicture(
            @Path("id") Long driverId,
            @Part MultipartBody.Part file
    );

    @GET("/api/drivers/{username}/rides")
    Call<PageResponse<DriverRideHistoryItemDTO>> getDriverRideHistory(
            @Path("username") String username,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("/api/drivers/{username}/assigned-rides")
    Call<List<DriverAssignedRideDTO>> assignedRides(
            @Path("username") String username
    );
}