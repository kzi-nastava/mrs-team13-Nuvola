package com.example.nuvola.network;

import android.content.Context;
import android.content.SharedPreferences;

//public class TokenStorage {
//    private static final String PREFS = "nuvola_prefs";
//    private static final String KEY_TOKEN = "jwt_token";
//
//    public static void saveToken(Context ctx, String token) {
//        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
//        sp.edit().putString(KEY_TOKEN, token).apply();
//    }
//
//    public static String getToken(Context ctx) {
//        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
//        return sp.getString(KEY_TOKEN, null);
//    }
//
//    public static void clear(Context ctx) {
//        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
//        sp.edit().remove(KEY_TOKEN).apply();
//    }
//}

public class TokenStorage {

    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    public static void saveToken(Context context, String token) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public static String getToken(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_TOKEN, null);
    }

    public static void saveUserRole(Context context, String role) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public static String getUserRole(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_USER_ROLE, "UNKNOWN");
    }

    public static void saveRememberMe(Context context, boolean rememberMe, String email) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        if (rememberMe) {
            editor.putString(KEY_REMEMBERED_EMAIL, email);
        } else {
            editor.remove(KEY_REMEMBERED_EMAIL);
        }
        editor.apply();
    }

    public static boolean isRememberMeEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public static String getRememberedEmail(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_REMEMBERED_EMAIL, "");
    }

    public static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}

