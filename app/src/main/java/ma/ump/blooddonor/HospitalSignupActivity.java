package ma.ump.blooddonor;

import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
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
import ma.ump.blooddonor.entity.HospitalUser;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.*;


public class HospitalSignupActivity extends AppCompatActivity {

    // Network
    private final OkHttpClient client = new OkHttpClient();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Adapter
    private HospitalAdapter hospitalAdapter;

    // Views
    private AutoCompleteTextView hospitalDropdown;
    private EditText emailInput, passwordInput, firstNameInput, lastNameInput;
    private MaterialButton signUpButton;
    private AutoCompleteTextView positionInput;
    private TextInputLayout positionLayout;
    // Data
    private Hospital selectedHospital;

    private String selectedPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_signup);

        initializeViews();
        setupHospitalSearch();
        setupPositionDropdown();
        setupSignUpButton();
    }

    private void initializeViews() {
        // Hospital Search
        hospitalDropdown = findViewById(R.id.hospitalDropdown);
        hospitalAdapter = new HospitalAdapter(this,new ArrayList<>());
        hospitalDropdown.setAdapter(hospitalAdapter);
        hospitalDropdown.setThreshold(1);

        // Form Fields
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        positionInput = findViewById(R.id.positionInput);
        positionLayout = findViewById(R.id.positionLayout);
        signUpButton = findViewById(R.id.signUpButton);
    }

    private void setupPositionDropdown() {
        // Get the string array from resources
        String[] positions = getResources().getStringArray(R.array.staff_positions);

        // Create adapter with the string array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                positions
        );

        // Set the adapter to the AutoCompleteTextView
        positionInput.setAdapter(adapter);

        // Store selected position in variable
        positionInput.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = (String) parent.getItemAtPosition(position);
            positionLayout.setError(null);
        });
    }

    public String getSelectedPosition() {
        return selectedPosition;
    }

    private void setupHospitalSearch() {
        hospitalDropdown.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> searchHospitals(s.toString());
                handler.postDelayed(searchRunnable, 300);
            }
        });

        hospitalDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedHospital = hospitalAdapter.getItem(position);
            if (selectedHospital.getId() == -1L) selectedHospital = null; // Handle "no results" item
        });
    }


    private void setupSignUpButton() {
        signUpButton.setOnClickListener(v -> {
            if (validateForm()) {
                registerUser(createHospitalUser());
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (firstNameInput.getText().toString().trim().isEmpty()) {
            firstNameInput.setError("First name required");
            isValid = false;
        }

        if (lastNameInput.getText().toString().trim().isEmpty()) {
            lastNameInput.setError("Last name required");
            isValid = false;
        }

        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.getText()).matches()) {
            emailInput.setError("Invalid email format");
            isValid = false;
        }

        if (passwordInput.getText().toString().trim().isEmpty()) {
            passwordInput.setError("Password required");
            isValid = false;
        }

        if (positionInput.getText().toString().trim().isEmpty()) {
            positionInput.setError("Position required");
            isValid = false;
        }

        if (selectedHospital == null) {
            Toast.makeText(this, "Please select a hospital", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private HospitalUser createHospitalUser() {
        return new HospitalUser(
                emailInput.getText().toString().trim(),
                passwordInput.getText().toString().trim(),
                lastNameInput.getText().toString().trim(),
                firstNameInput.getText().toString().trim(),
                getSelectedPosition(),
                selectedHospital
        );
    }

    private void searchHospitals(String query) {
        if (query.length() < 2) return;

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("10.0.2.2")
                .port(8080)
                .addPathSegments("api/hospitals/search")
                .addQueryParameter("name", query)
                .build();

        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleSearchResponse(response);
            }
        });
    }

    private void handleSearchResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            runOnUiThread(() -> showError("Server error: " + response.code()));
            return;
        }

        String responseData = response.body().string();
        List<Hospital> hospitals = parseHospitalData(responseData);

        runOnUiThread(() -> {
            hospitalAdapter.clear();
            if (hospitals.isEmpty()) {
                hospitals.add(new Hospital(-1L, "No results found", "", ""));
            }
            hospitalAdapter.addAll(hospitals);
            hospitalAdapter.notifyDataSetChanged();
        });
    }

    private List<Hospital> parseHospitalData(String json) {
        List<Hospital> hospitals = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                hospitals.add(new Hospital(
                        obj.getLong("id"),
                        obj.getString("nom"),
                        obj.optString("adresse"),
                        obj.optString("telephone")
                ));
            }
        } catch (JSONException e) {
            Log.e("Hospital", "JSON parsing error", e);
        }
        return hospitals;
    }

    private void registerUser(HospitalUser user) {
        try {
            JSONObject json = new JSONObject()
                    .put("email", user.getEmail())
                    .put("password", user.getPassword())
                    .put("nom", user.getNom())
                    .put("prenom", user.getPrenom())
                    .put("position", user.getPosition())
                    .put("hospital", new JSONObject()
                            .put("id", user.getHospital().getId()));

            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/auth/signup/hospial-user")
                    .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> showError("Registration failed: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    handleRegistrationResponse(response.code(), response.body().string());
                }
            });

        } catch (JSONException e) {
            showError("Error creating registration data");
        }
    }

    private void handleRegistrationResponse(int code, String response) {
        runOnUiThread(() -> {
            Log.d("Registration", "Response code: " + code + " | Body: " + response);

            if (code == 201) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                try {
                    // Handle non-JSON responses
                    if (response.startsWith("{") && response.endsWith("}")) {
                        JSONObject error = new JSONObject(response);
                        showError(error.optString("message", "Registration failed"));
                    } else {
                        showError("Server error: " + response);
                    }
                } catch (JSONException e) {
                    showError("Server returned unexpected format: " + response);
                }
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.dispatcher().cancelAll();
    }

}