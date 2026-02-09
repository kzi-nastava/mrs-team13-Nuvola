package com.example.nuvola.network;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class JwtRoleHelper {

    private static final String TAG = "JWT_ROLE_HELPER";

    public static String getUserType(String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.e(TAG, "Token is NULL or empty");
                return "UNKNOWN";
            }

            Log.d(TAG, "Processing token: " + token.substring(0, Math.min(50, token.length())) + "...");

            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Token does not have enough parts. Parts count: " + parts.length);
                return "UNKNOWN";
            }

            // Decode the payload (second part of JWT)
            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));
            Log.d(TAG, "JWT PAYLOAD: " + payloadJson);

            JSONObject payload = new JSONObject(payloadJson);

            // Try to extract role from different possible JWT structures
            String role = extractRole(payload);

            Log.d(TAG, "Extracted role: " + role);
            return role;

        } catch (Exception e) {
            Log.e(TAG, "JWT PARSE ERROR", e);
            return "UNKNOWN";
        }
    }

    private static String extractRole(JSONObject payload) {
        try {
            // Method 1: Check "authorities" array (Spring Security default)
            if (payload.has("authorities")) {
                Log.d(TAG, "Found 'authorities' field");
                JSONArray authorities = payload.getJSONArray("authorities");
                Log.d(TAG, "Authorities array size: " + authorities.length());

                for (int i = 0; i < authorities.length(); i++) {
                    JSONObject auth = authorities.getJSONObject(i);
                    String authority = auth.optString("authority");
                    Log.d(TAG, "Authority " + i + ": " + authority);

                    String mappedRole = mapRoleToUserType(authority);
                    if (!mappedRole.equals("UNKNOWN")) {
                        return mappedRole;
                    }
                }
            }

            // Method 2: Check "roles" as array
            if (payload.has("roles")) {
                Log.d(TAG, "Found 'roles' field");
                Object rolesObj = payload.get("roles");
                Log.d(TAG, "roles field type: " + rolesObj.getClass().getName());

                if (rolesObj instanceof JSONArray) {
                    JSONArray roles = (JSONArray) rolesObj;
                    Log.d(TAG, "Roles array size: " + roles.length());

                    for (int i = 0; i < roles.length(); i++) {
                        String role = roles.getString(i);
                        Log.d(TAG, "Role " + i + ": " + role);

                        String mappedRole = mapRoleToUserType(role);
                        if (!mappedRole.equals("UNKNOWN")) {
                            return mappedRole;
                        }
                    }
                } else if (rolesObj instanceof String) {
                    String rolesStr = (String) rolesObj;
                    Log.d(TAG, "Roles string: " + rolesStr);

                    // Handle string format like "[ROLE_DRIVER]" or "ROLE_DRIVER"
                    rolesStr = rolesStr.replace("[", "").replace("]", "").trim();
                    String mappedRole = mapRoleToUserType(rolesStr);
                    if (!mappedRole.equals("UNKNOWN")) {
                        return mappedRole;
                    }
                }
            }

            // Method 3: Check "role" as single value
            if (payload.has("role")) {
                String role = payload.getString("role");
                Log.d(TAG, "Found single 'role': " + role);
                String mappedRole = mapRoleToUserType(role);
                if (!mappedRole.equals("UNKNOWN")) {
                    return mappedRole;
                }
            }

            Log.w(TAG, "NO ROLE FOUND in JWT payload");
            return "UNKNOWN";

        } catch (Exception e) {
            Log.e(TAG, "Error extracting role", e);
            return "UNKNOWN";
        }
    }

    private static String mapRoleToUserType(String role) {
        if (role == null || role.isEmpty()) {
            return "UNKNOWN";
        }

        // Normalize the role string
        String normalizedRole = role.toUpperCase().trim();

        Log.d(TAG, "Mapping role: " + normalizedRole);

        // Check for driver role
        if (normalizedRole.equals("ROLE_DRIVER") || normalizedRole.equals("DRIVER")) {
            return "DRIVER";
        }

        // Check for passenger/registered user role
        if (normalizedRole.equals("ROLE_REGISTERED_USER") ||
                normalizedRole.equals("REGISTERED_USER") ||
                normalizedRole.equals("PASSENGER") ||
                normalizedRole.equals("ROLE_PASSENGER")) {
            return "PASSENGER";
        }

        // Check for admin role
        if (normalizedRole.equals("ROLE_ADMIN") || normalizedRole.equals("ADMIN")) {
            return "ADMIN";
        }

        return "UNKNOWN";
    }

    // Helper method to decode and log the entire JWT for debugging
    public static void debugToken(String token) {
        try {
            if (token == null) {
                Log.d(TAG, "DEBUG: Token is null");
                return;
            }

            String[] parts = token.split("\\.");
            Log.d(TAG, "DEBUG: Token has " + parts.length + " parts");

            if (parts.length >= 2) {
                String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP));
                Log.d(TAG, "DEBUG: Full JWT payload: " + payloadJson);
            }
        } catch (Exception e) {
            Log.e(TAG, "DEBUG: Error decoding token", e);
        }
    }
}
