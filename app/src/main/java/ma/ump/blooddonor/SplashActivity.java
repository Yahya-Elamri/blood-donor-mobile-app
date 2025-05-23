package ma.ump.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import ma.ump.blooddonor.utils.AuthUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            try {
                // Simuler un temps de chargement
                Thread.sleep(1000);

                runOnUiThread(() -> {
                    if (AuthUtils.isUserLoggedIn(this)) {
                        startMainActivity();
                    } else {
                        startLoginActivity();
                    }
                    finish();
                });
            } catch (InterruptedException e) {
                Log.e("SplashActivity", "Thread error", e);
            }
        }).start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
