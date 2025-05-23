package ma.ump.blooddonor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class AuthUtils {
    private static final String TAG = "AuthUtils";
    private static final String PREFS_NAME = "secure_prefs";
    private static final String KEY_JWT = "jwt_token";
    private static final String KEY_EXPIRY = "token_expiry";

    public static void saveAuthToken(Context context, String token, long expiresInSeconds) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            prefs.edit()
                    .putString(KEY_JWT, token)
                    .putLong(KEY_EXPIRY, System.currentTimeMillis() + (expiresInSeconds * 1000))
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving token", e);
        }
    }

    public static String getAuthToken(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            return prefs.getString(KEY_JWT, null);
        } catch (Exception e) {
            Log.e(TAG, "Error reading token", e);
            return null;
        }
    }

    public static boolean isUserLoggedIn(Context context) {
        try {
            SharedPreferences prefs = getEncryptedPreferences(context);
            String token = prefs.getString(KEY_JWT, null);
            long expiresAt = prefs.getLong(KEY_EXPIRY, 0);

            return token != null && System.currentTimeMillis() < expiresAt;
        } catch (Exception e) {
            Log.e(TAG, "Auth check error", e);

            // Clear corrupted EncryptedSharedPreferences
            context.deleteSharedPreferences(PREFS_NAME);
            return false;
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

            // Optional: delete corrupted preferences to recover next time
            context.deleteSharedPreferences(PREFS_NAME);
            throw new RuntimeException("EncryptedSharedPreferences corrupted and reset", e);
        }
    }
}