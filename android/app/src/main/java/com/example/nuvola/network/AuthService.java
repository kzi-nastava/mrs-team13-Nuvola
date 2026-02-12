package com.example.nuvola.network;

public class AuthService {

    private static AuthApi authApi;

    public static AuthApi api() {
        if (authApi == null) {
            authApi = ApiClient.getRetrofit().create(AuthApi.class);
        }
        return authApi;
    }
}
