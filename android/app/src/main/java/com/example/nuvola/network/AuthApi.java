package com.example.nuvola.network;

import dto.ForgotPasswordRequestDTO;
import dto.RegisterRequestDTO;
import dto.RegisterResponseDTO;
import dto.ResetPasswordRequestDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface AuthApi {
    @POST("/api/auth/login")
    Call<UserTokenState> login(@Body LoginRequest request);


    @POST("api/auth/forgot-password")
    Call<String> forgotPassword(@Body ForgotPasswordRequestDTO body);

    @POST("api/auth/reset-password")
    Call<String> resetPassword(@Query("token") String token, @Body ResetPasswordRequestDTO body);
    @POST("api/auth/register")
    Call<RegisterResponseDTO> register(@Body RegisterRequestDTO body);

    @GET("/api/auth/activate-email")
    Call<String> activateAccount(@Query("token") String token);

    @POST("api/auth/logout")
    Call<Void> logout();

    @PUT("api/auth/driver/status")
    Call<Void> changeDriverStatus(@Body dto.ChangeDriverStatusDTO body);

}

