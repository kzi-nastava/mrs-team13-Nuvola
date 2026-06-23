package com.example.nuvola.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NotificationApi {
    @GET("api/notifications/{userId}")
    Call<List<NotificationDTO>> getNotifications(@Path("userId") long userId);
}