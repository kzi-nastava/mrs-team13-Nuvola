package com.example.nuvola.network;

import java.util.List;

import dto.UpdateVehicleTypePriceDTO;
import dto.VehicleTypePricingDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PricingApi {

    @GET("/api/pricing/vehicle-types")
    Call<List<VehicleTypePricingDTO>> getAllVehicleTypePrices();

    @PUT("/api/pricing/vehicle-types/{type}")
    Call<VehicleTypePricingDTO> upsertVehicleTypePrice(
            @Path("type") String type,
            @Body UpdateVehicleTypePriceDTO body
    );
}