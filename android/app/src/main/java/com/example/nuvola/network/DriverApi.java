package com.example.nuvola.network;

import dto.DriverRideHistoryItemDTO;
import dto.PageResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DriverApi {
    @GET("/api/drivers/{username}/rides")
    Call<PageResponse<DriverRideHistoryItemDTO>> getDriverRideHistory(
            @Path("username") String username,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder,
            @Query("page") int page,
            @Query("size") int size
    );
}
