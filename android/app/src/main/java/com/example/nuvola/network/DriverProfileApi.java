    package com.example.nuvola.network;

    import dto.ChangePasswordDTO;
    import dto.DriverProfileResponseDTO;
    import dto.DriverProfileUpdateDTO;

    import retrofit2.Call;
    import retrofit2.http.Body;
    import retrofit2.http.GET;
    import retrofit2.http.PUT;

    public interface DriverProfileApi {

        @GET("/api/driver/profile")
        Call<DriverProfileResponseDTO> getDriverProfile();

        @PUT("/api/driver/profile")
        Call<Void> requestProfileChange(@Body DriverProfileUpdateDTO dto);

        @PUT("/api/driver/profile/password")
        Call<Void> changePassword(@Body ChangePasswordDTO dto);
    }