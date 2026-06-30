package com.example.nuvola.network;

import dto.OsrmRouteResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OsrmApi {

    @GET("route/v1/driving/{coordinates}")
    Call<OsrmRouteResponse> getRoute(
            @Path(value = "coordinates", encoded = true)
            String coordinates,

            @Query("overview")
            String overview,

            @Query("geometries")
            String geometries
    );
}