package com.example.nuvola.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/api/auth/login")
    Call<UserTokenState> login(@Body LoginRequest request);
}
