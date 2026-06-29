package com.example.nuvola.network;

import java.util.List;
import java.util.Map;

import dto.AdminUserDTO;
import dto.ProfileChangeRequestDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface AdminApi {

    @GET("/api/admin/users/registeredUsers")
    Call<List<AdminUserDTO>> getRegisteredUsers();

    @GET("/api/admin/users/drivers")
    Call<List<AdminUserDTO>> getDrivers(@Query("search") String search);

    @POST("/api/admin/users/{id}/block")
    Call<AdminUserDTO> blockUser(@Path("id") long id, @Body Map<String, String> body);

    @POST("/api/admin/users/{id}/unblock")
    Call<AdminUserDTO> unblockUser(@Path("id") long id, @Body Map<String, String> body);

    @GET("/api/admin/profile-change-requests")
    Call<List<ProfileChangeRequestDTO>>
    getProfileChangeRequests();

    @PUT("/api/admin/profile-change-requests/{id}/approve")
    Call<Void> approveProfileChangeRequest(
            @Path("id") Long requestId
    );

    @PUT("/api/admin/profile-change-requests/{id}/reject")
    Call<Void> rejectProfileChangeRequest(
            @Path("id") Long requestId
    );
}