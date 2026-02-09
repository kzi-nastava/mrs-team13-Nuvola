package com.example.nuvola.network;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class JwtRoleHelper {

    public static String getUserType(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "UNKNOWN";

            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            JSONObject payload = new JSONObject(payloadJson);

            String roles = payload.optString("roles", "");

            if (roles.contains("ROLE_DRIVER")) return "DRIVER";
            if (roles.contains("ROLE_REGISTERED_USER")) return "PASSENGER";
            if (roles.contains("ROLE_ADMIN")) return "ADMIN";

            return "UNKNOWN";
        } catch (Exception e) {
            Log.e("JwtRoleHelper", "Failed to parse JWT roles. tokenPresent=" + (token != null), e);
            return "UNKNOWN";
        }
    }
}
