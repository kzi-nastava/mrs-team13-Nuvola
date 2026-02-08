package com.example.nuvola.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {
    private static final String PREFS = "nuvola_prefs";
    private static final String KEY_TOKEN = "jwt_token";

    public static void saveToken(Context ctx, String token) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, null);
    }

    public static void clear(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_TOKEN).apply();
    }
}
