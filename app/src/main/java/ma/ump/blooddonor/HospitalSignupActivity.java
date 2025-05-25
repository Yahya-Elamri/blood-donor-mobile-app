package ma.ump.blooddonor;

import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ma.ump.blooddonor.adapter.HospitalAdapter;
import ma.ump.blooddonor.entity.Hospital;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

public class HospitalSignupActivity extends AppCompatActivity {
    private OkHttpClient client = new OkHttpClient();
    private HospitalAdapter hospitalAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private AutoCompleteTextView hospitalDropdown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_signup);

        // Setup AutoCompleteTextView
        hospitalDropdown = findViewById(R.id.hospitalDropdown);

        // Initialize adapter with empty list
        hospitalAdapter = new HospitalAdapter(this, new ArrayList<>());
        hospitalDropdown.setAdapter(hospitalAdapter);

        // Set threshold to trigger dropdown
        hospitalDropdown.setThreshold(1);

        // Optional: Add some test data to verify dropdown works
        addTestData();

        hospitalDropdown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous requests
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                // Debounce search by 300ms
                searchRunnable = () -> searchHospitals(s.toString());
                handler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        hospitalDropdown.setOnItemClickListener((parent, view, position, id) -> {
            Hospital selected = hospitalAdapter.getItem(position);
            if (selected != null) {
                Log.d("Hospital", "Selected: " + selected.getNom());
            }
        });

        // Optional: Handle focus and click to show dropdown
        hospitalDropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && hospitalDropdown.getText().length() >= 1) {
                hospitalDropdown.showDropDown();
            }
        });

        hospitalDropdown.setOnClickListener(v -> {
            if (hospitalDropdown.getText().length() >= 1) {
                hospitalDropdown.showDropDown();
            }
        });
    }

    // Temporary method to test if dropdown works with static data
    private void addTestData() {
        List<Hospital> testHospitals = new ArrayList<>();
        testHospitals.add(new Hospital(1L, "Hôpital Test 1", "Adresse 1", "123456789"));
        testHospitals.add(new Hospital(2L, "Hôpital Test 2", "Adresse 2", "987654321"));

        hospitalAdapter.clear();
        hospitalAdapter.addAll(testHospitals);
        hospitalAdapter.notifyDataSetChanged();
    }

    private void searchHospitals(String query) {
        if (query.length() < 2) {
            // Show test data for short queries or clear
            addTestData();
            return;
        }

        Log.d("Hospital", "Searching for: " + query);

        HttpUrl url = HttpUrl.parse("http://10.0.2.2:8080/api/hospitals/search")
                .newBuilder()
                .addQueryParameter("name", query)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Hospital", "Network error", e);
                runOnUiThread(() -> {
                    Toast.makeText(HospitalSignupActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Show test data on network error
                    addTestData();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("Hospital", "Response: " + responseData);

                    List<Hospital> hospitals = parseHospitalData(responseData);
                    runOnUiThread(() -> {
                        hospitalAdapter.clear();
                        if (hospitals.isEmpty()) {
                            // Add a "no results" item
                            hospitals.add(new Hospital(-1L, "Aucun résultat trouvé", "", ""));
                        }
                        hospitalAdapter.addAll(hospitals);
                        hospitalAdapter.notifyDataSetChanged();

                        // Show dropdown if it's not visible
                        if (hospitalDropdown.hasFocus()) {
                            hospitalDropdown.showDropDown();
                        }
                    });
                } else {
                    Log.e("Hospital", "HTTP Error: " + response.code() + " - " + response.message());
                    runOnUiThread(() -> {
                        Toast.makeText(HospitalSignupActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        // Show test data on HTTP error
                        addTestData();
                    });
                }
            }
        });
    }

    private List<Hospital> parseHospitalData(String json) {
        List<Hospital> hospitals = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            Log.d("Hospital", "Parsing " + jsonArray.length() + " hospitals");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Hospital hospital = new Hospital(
                        obj.getLong("id"),
                        obj.getString("nom"),
                        obj.optString("adresse"),
                        obj.optString("telephone")
                );
                hospitals.add(hospital);
                Log.d("Hospital", "Added: " + hospital.getNom());
            }
        } catch (JSONException e) {
            Log.e("Hospital", "JSON parsing error", e);
        }
        return hospitals;
    }
}