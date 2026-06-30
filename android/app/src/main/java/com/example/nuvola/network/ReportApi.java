package com.example.nuvola.network;

import dto.RideReportResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReportApi {

    @GET("/api/reports/my")
    Call<RideReportResponse> getMyReport(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("/api/reports/admin")
    Call<RideReportResponse> getAdminReport(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("target") String target,
            @Query("email") String email
    );
}