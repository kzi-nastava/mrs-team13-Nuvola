package com.example.nuvola.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {

    @GET("api/support/chat/{userId}")
    Call<List<ChatMessageDTO>> getChatMessages(@Path("userId") long userId);

    @GET("api/support/admin/inbox")
    Call<AdminInboxPageDTO> getAdminInbox(
            @Query("adminId") long adminId,
            @Query("page") int page,
            @Query("size") int size
    );
}