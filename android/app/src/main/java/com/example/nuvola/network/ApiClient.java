package com.example.nuvola.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit;
    private static Context appContext;

    /**
     * Initialize the API client with application context
     * Call this from your Application class or MainActivity
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
        // Force recreation of retrofit instance with new context
        retrofit = null;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException(
                        "ApiClient not initialized. Call ApiClient.init(context) first!");
            }

            // Logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Build OkHttpClient with interceptors
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(appContext))  // Add auth token
                    .addInterceptor(logging)                          // Add logging
                    .build();

            // Build Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    /**
     * Clear the retrofit instance (useful when logging out)
     */
    public static void clearInstance() {
        retrofit = null;
    }
}
