package ma.ump.blooddonor.api;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import ma.ump.blooddonor.LoginActivity;
import ma.ump.blooddonor.R;
import ma.ump.blooddonor.donorActivity.MainActivity;
import ma.ump.blooddonor.utils.AuthUtils;
import ma.ump.blooddonor.utils.OkHttpUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Map;

public class FirebaseMessageService extends FirebaseMessagingService {

    private static final String TAG = "";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendTokenToServer(token); // Send raw token without modification
    }

    private void sendTokenToServer(String token) {
        // Get user ID
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId != null) {
            new Thread(() -> {
                try {
                    // Send just the token string
                    OkHttpUtils.registerFcmToken(userId, token);
                } catch (IOException e) {
                    Log.e(TAG, "Token send failed", e);
                }
            }).start();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        String requestId = data.get("bloodRequestId");

        if (title == null) title = "New Blood Request";
        if (body == null) body = "Urgent blood donation needed";

        sendNotification(title, body, requestId);
    }

    private void sendNotification(String title, String body, String requestId) {
        Intent intent;
        if (requestId != null) {
            intent = new Intent(this, LoginActivity.class);
            intent.putExtra("BLOOD_REQUEST_ID", requestId);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_blood_drop)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Blood Requests",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}