package com.example.nuvola.network;

import dto.*;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ProfileApi {

    @GET("/api/profile")
    Call<ProfileResponseDTO> getProfile();

    @PUT("/api/profile")
    Call<Void> updateProfile(@Body UpdateProfileDTO dto);

    @Multipart
    @POST("/api/profile/picture")
    Call<PictureResponse> uploadPicture(@Part MultipartBody.Part file);

    @PUT("/api/profile/password")
    Call<Void> changePassword(@Body ChangePasswordDTO dto);
}