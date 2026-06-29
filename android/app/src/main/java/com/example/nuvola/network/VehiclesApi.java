package com.example.nuvola.network;

import java.util.List;

import dto.VehicleLocationDTO;
import retrofit2.Call;
import retrofit2.http.GET;

public interface VehiclesApi {
    @GET("api/drivers/active-vehicles")
    Call<List<VehicleLocationDTO>> getActiveVehicles();
}