package com.example.nuvola.network;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class JwtRoleHelper {

    private static final String TAG = "JWT_DEBUG";

    public static String getUserType(String token) {
        try {
            if (token == null) {
                Log.e(TAG, "Token is NULL");
                return "UNKNOWN";
            }

            Log.d(TAG, "RAW TOKEN: " + token);

            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Token does not have 2 parts");
                return "UNKNOWN";
            }

            String payloadJson =
                    new String(Base64.decode(parts[1], Base64.URL_SAFE));

            Log.d(TAG, "JWT PAYLOAD: " + payloadJson);

            JSONObject payload = new JSONObject(payloadJson);

            // ===== CASE 1: authorities (Spring Security default) =====
            if (payload.has("authorities")) {
                JSONArray authorities = payload.getJSONArray("authorities");
                Log.d(TAG, "Authorities array size: " + authorities.length());

                for (int i = 0; i < authorities.length(); i++) {
                    JSONObject auth = authorities.getJSONObject(i);
                    String role = auth.optString("authority");

                    Log.d(TAG, "Found authority: " + role);

                    if ("ROLE_DRIVER".equals(role)) return "DRIVER";
                    if ("ROLE_REGISTERED_USER".equals(role)) return "PASSENGER";
                    if ("ROLE_ADMIN".equals(role)) return "ADMIN";
                }
            }

            // ===== CASE 2: roles as array =====
            if (payload.has("roles")) {
                Object rolesObj = payload.get("roles");
                Log.d(TAG, "roles field type: " + rolesObj.getClass().getName());

                if (rolesObj instanceof JSONArray) {
                    JSONArray roles = (JSONArray) rolesObj;
                    for (int i = 0; i < roles.length(); i++) {
                        String role = roles.getString(i);
                        Log.d(TAG, "Found role: " + role);

                        if ("ROLE_DRIVER".equals(role)) return "DRIVER";
                        if ("ROLE_REGISTERED_USER".equals(role)) return "PASSENGER";
                        if ("ROLE_ADMIN".equals(role)) return "ADMIN";
                    }
                }

                if (rolesObj instanceof String) {
                    String roles = (String) rolesObj;
                    Log.d(TAG, "Roles string: " + roles);

                    if (roles.contains("ROLE_DRIVER")) return "DRIVER";
                    if (roles.contains("ROLE_REGISTERED_USER")) return "PASSENGER";
                    if (roles.contains("ROLE_ADMIN")) return "ADMIN";
                }
            }

            Log.e(TAG, "NO ROLE FOUND → UNKNOWN");
            return "UNKNOWN";

        } catch (Exception e) {
            Log.e(TAG, "JWT PARSE ERROR", e);
            return "UNKNOWN";
        }
    }
}
