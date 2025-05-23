package ma.ump.blooddonor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ma.ump.blooddonor.adapter.DonationAdapter;
import ma.ump.blooddonor.entity.Donation;
import ma.ump.blooddonor.utils.AuthUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // FIXED: required layout manager

        progressBar = findViewById(R.id.progressBar);
        sharedPreferences = getSharedPreferences("my_app", MODE_PRIVATE);

        fetchDonations();
    }

    private void fetchDonations() {
        progressBar.setVisibility(View.VISIBLE);
        String token = AuthUtils.getAuthToken(this);

        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String userId = getUserIdFromToken(token);
        if (userId == null) {
            Toast.makeText(this, "Invalid token", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String url = "http://10.0.2.2:8080/api/donors/donation/by-donor/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Empty response from server", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    if (response.isSuccessful()) {
                        String responseData = responseBody.string();
                        List<Donation> donations = parseResponse(responseData);

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (donations.isEmpty()) {
                                Toast.makeText(MainActivity.this, "No donations found", Toast.LENGTH_LONG).show();
                            } else {
                                setupRecyclerView(donations);
                            }
                        });
                    } else {
                        String errorMessage = "Error: " + response.code();
                        try {
                            String errorBody = responseBody.string();
                            if (!errorBody.isEmpty()) {
                                errorMessage += " - " + new JSONObject(errorBody).optString("message", "Unknown error");
                            }
                        } catch (Exception ignored) {}

                        final String finalMessage = errorMessage;
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private String getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(Base64.decode(addBase64Padding(parts[1]), Base64.URL_SAFE), "UTF-8");
            JSONObject payloadJson = new JSONObject(payload);
            return String.valueOf(payloadJson.getInt("id")); // adjust if your claim is named differently
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String addBase64Padding(String base64) {
        int padding = (4 - base64.length() % 4) % 4;
        for (int i = 0; i < padding; i++) base64 += "=";
        return base64;
    }

    private List<Donation> parseResponse(String response) {
        List<Donation> donations = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                donations.add(new Donation(
                        obj.getLong("id"),
                        obj.getString("date"),
                        obj.getString("lieu")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return donations;
    }

    private void setupRecyclerView(List<Donation> donations) {
        DonationAdapter adapter = new DonationAdapter(donations);
        recyclerView.setAdapter(adapter);
    }
}
