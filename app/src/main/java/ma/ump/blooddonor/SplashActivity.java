package ma.ump.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import ma.ump.blooddonor.utils.AuthUtils;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            try {
                Thread.sleep(SPLASH_DELAY);
                boolean isLoggedIn = checkAuthState();
                String role = getTokenRole();

                runOnUiThread(() -> {
                    navigateBasedOnRole(isLoggedIn, role);
                    finish();
                });

            } catch (InterruptedException e) {
                Log.e(TAG, "Splash thread interrupted", e);
                navigateToLogin();
            }
        }).start();
    }

    private boolean checkAuthState() {
        try {
            return AuthUtils.isUserLoggedIn(this);
        } catch (Exception e) {
            Log.e(TAG, "Auth check failed", e);
            return false;
        }
    }

    private String getTokenRole() {
        try {
            return AuthUtils.getTokenRole(this);
        } catch (Exception e) {
            Log.e(TAG, "Role check failed", e);
            return null;
        }
    }

    private void navigateBasedOnRole(boolean isLoggedIn, String role) {
        if (!isLoggedIn) {
            startLoginActivity();
            return;
        }

        if (role == null) {
            Log.w(TAG, "No role in token, logging out");
            AuthUtils.clearAuthCredentials(this);
            startLoginActivity();
            return;
        }

        switch (role.toUpperCase(Locale.ROOT)) {
            case "DONOR":
                startMainActivity();
                break;
            case "HOSPITAL":
                startHospitalActivity();
                break;
            default:
                Log.w(TAG, "Unknown role: " + role);
                AuthUtils.clearAuthCredentials(this);
                startLoginActivity();
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void startHospitalActivity() {
        startActivity(new Intent(this, HospitalActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void navigateToLogin() {
        startLoginActivity();
        finish();
    }

    @Override
    protected void onDestroy() {
        // Clean up any potential leaks
        super.onDestroy();
    }
}