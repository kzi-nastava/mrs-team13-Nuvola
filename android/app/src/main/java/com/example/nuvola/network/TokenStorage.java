package com.example.nuvola.network;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {

    private static final String PREF_NAME = "user_preferences";

    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ROLE = "user_role";

    private static final String KEY_USER_EMAIL = "user_email";

    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    private TokenStorage() {
    }

    public static void saveToken(
            Context context,
            String token
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public static String getToken(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                KEY_TOKEN,
                null
        );
    }

    public static void saveUserRole(
            Context context,
            String role
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public static String getUserRole(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                KEY_USER_ROLE,
                "UNKNOWN"
        );
    }
    public static void saveUserEmail(
            Context context,
            String email
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(
                        KEY_USER_EMAIL,
                        email == null ? "" : email.trim()
                )
                .apply();
    }

    public static String getUserEmail(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                KEY_USER_EMAIL,
                ""
        );
    }

    public static void saveRememberMe(
            Context context,
            boolean rememberMe,
            String email
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        SharedPreferences.Editor editor =
                preferences.edit();

        editor.putBoolean(
                KEY_REMEMBER_ME,
                rememberMe
        );

        if (rememberMe) {
            editor.putString(
                    KEY_REMEMBERED_EMAIL,
                    email == null ? "" : email.trim()
            );

        } else {
            editor.remove(
                    KEY_REMEMBERED_EMAIL
            );
        }

        editor.apply();
    }

    public static boolean isRememberMeEnabled(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getBoolean(
                KEY_REMEMBER_ME,
                false
        );
    }

    public static String getRememberedEmail(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                KEY_REMEMBERED_EMAIL,
                ""
        );
    }

    public static void clear(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .clear()
                .apply();
    }
}