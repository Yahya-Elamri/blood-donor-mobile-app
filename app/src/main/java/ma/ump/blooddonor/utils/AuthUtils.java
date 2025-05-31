package ma.ump.blooddonor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class AuthUtils {
    private static final String TAG = "AuthUtils";
    private static final String PREFS_NAME = "secure_prefs";
    private static final String KEY_JWT = "jwt_token";
    private static final String KEY_EXPIRY = "token_expiry";
    private static final long EXPIRATION_BUFFER_MS = 30000; // 30 seconds buffer

    public static void saveAuthToken(Context context, String token, long expiresInSeconds) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            prefs.edit()
                    .putString(KEY_JWT, token)
                    .putLong(KEY_EXPIRY, System.currentTimeMillis() + (expiresInSeconds * 1000))
                    .apply();
            Log.d(TAG, "Token saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving token", e);
            clearAuthCredentials(context);
        }
    }

    public static String getAuthToken(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            String token = prefs.getString(KEY_JWT, null);

            if (token == null) {
                Log.d(TAG, "No token found in storage");
                return null;
            }

            // Verify token hasn't expired
            if (isTokenExpired(prefs)) {
                Log.w(TAG, "Retrieved expired token, clearing credentials");
                clearAuthCredentials(context);
                return null;
            }

            return token;
        } catch (Exception e) {
            Log.e(TAG, "Error reading token", e);
            clearAuthCredentials(context);
            return null;
        }
    }

    public static boolean isUserLoggedIn(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            return !isTokenExpired(prefs);
        } catch (Exception e) {
            Log.e(TAG, "Auth check error", e);
            clearAuthCredentials(context);
            return false;
        }
    }

    private static boolean isTokenExpired(SharedPreferences prefs) {
        String token = prefs.getString(KEY_JWT, null);
        long expiresAt = prefs.getLong(KEY_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();

        if (token == null) {
            Log.d(TAG, "Token check: No token exists");
            return true;
        }

        if (currentTime > (expiresAt - EXPIRATION_BUFFER_MS)) {
            Log.w(TAG, "Token expired or nearing expiration. Current: " + currentTime
                    + ", Expires: " + expiresAt);
            return true;
        }
        Log.w(TAG, "Token expired or nearing expiration. Current: " + currentTime
                + ", Expires: " + expiresAt);
        return false;
    }

    public static String getTokenRole(Context context) {
        try {
            String token = getAuthToken(context);
            if (token == null) return null;

            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(
                    Base64.decode(parts[1], Base64.URL_SAFE),
                    StandardCharsets.UTF_8
            );

            JSONObject json = new JSONObject(payload);
            return json.optString("role", null);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing token role", e);
            return null;
        }
    }

    public static String getTokenId(Context context) {
        try {
            String token = getAuthToken(context);
            if (token == null) return null;

            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(
                    Base64.decode(parts[1], Base64.URL_SAFE),
                    StandardCharsets.UTF_8
            );

            JSONObject json = new JSONObject(payload);
            return json.optString("id", null);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing token id", e);
            return null;
        }
    }


    public static void clearAuthCredentials(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            prefs.edit()
                    .remove(KEY_JWT)
                    .remove(KEY_EXPIRY)
                    .apply();
            Log.i(TAG, "Credentials cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing credentials", e);
            context.deleteSharedPreferences(PREFS_NAME);
        }
    }

    private static SharedPreferences getEncryptedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to get encrypted preferences", e);
            context.deleteSharedPreferences(PREFS_NAME);
            throw new SecurityException("Failed to initialize secure storage", e);
        }
    }
}