package com.example.nuvola.network;

import java.util.List;

import dto.FavoriteRouteDTO;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FavoriteRouteApi {

    @GET("/api/favorites")
    Call<List<FavoriteRouteDTO>> getFavorites();

    @DELETE("/api/favorites/{id}")
    Call<Void> removeFavorite(
            @Path("id") Long id
    );
}