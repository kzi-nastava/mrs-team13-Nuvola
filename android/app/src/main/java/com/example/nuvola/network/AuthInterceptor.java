package com.example.nuvola.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor that adds JWT token to all API requests (except login/register)
 */
public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Skip authentication for login and register endpoints
        String url = originalRequest.url().toString();
        if (url.contains("/api/auth/login") || url.contains("/api/auth/register")) {
            Log.d(TAG, "Skipping auth for: " + url);
            return chain.proceed(originalRequest);
        }

        // Get token from storage
        String token = TokenStorage.getToken(context);

        if (token == null || token.isEmpty()) {
            Log.w(TAG, "No token available for request to: " + url);
            return chain.proceed(originalRequest);
        }

        // Add Authorization header
        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        Log.d(TAG, "Added auth token to request: " + url);
        return chain.proceed(authenticatedRequest);
    }
}
