package com.example.nuvola.apis;

import com.example.nuvola.network.ServerConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static NuvolaApi create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(NuvolaApi.class);
    }
}
