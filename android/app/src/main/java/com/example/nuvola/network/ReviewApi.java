package com.example.nuvola.network;

import dto.RatingRequestDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReviewApi {
    @POST("api/reviews")
    Call<Void> submitReview(@Body RatingRequestDTO dto);
}