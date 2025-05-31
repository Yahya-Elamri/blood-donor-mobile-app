package ma.ump.blooddonor.utils;
import android.content.Context;
import android.util.Log;
import java.io.IOException;

import ma.ump.blooddonor.Constants.Constants;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {
    private static final String BASE_URL = Constants.BASE_URL + "/api/";
    private static final OkHttpClient client = new OkHttpClient();

    public static void registerFcmToken(String donorId, String token) throws IOException {
        String url = BASE_URL + "donorsmessage/" + donorId + "/fcm-token";
        System.out.println(donorId + " hna " +token);
        RequestBody formBody = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            Log.i("FCM", "Token registered successfully");
        }
    }

    public static void removeFcmToken(String donorId, String token) throws IOException {
        String url = BASE_URL + "donorsmessage/" + donorId + "/fcm-token";

        RequestBody formBody = new FormBody.Builder()
                .add("token", token)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .delete(formBody) // Using DELETE method
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            Log.i("FCM", "Token removed successfully");
        }
    }

    public static String getBloodRequestDetails(String requestId) throws IOException {
        String url = BASE_URL + "blood-requests/" + requestId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            return response.body().string();
        }
    }
}
