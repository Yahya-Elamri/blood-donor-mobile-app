package ma.ump.blooddonor.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.time.LocalDateTime;
import ma.ump.blooddonor.Constants.Constants;
import ma.ump.blooddonor.R;
import ma.ump.blooddonor.entity.BloodRequest;
import ma.ump.blooddonor.entity.BloodType;
import ma.ump.blooddonor.entity.RequestStatus;
import ma.ump.blooddonor.entity.UrgenceLevel;
import ma.ump.blooddonor.utils.AuthUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BloodStockFragment extends Fragment {

    private Spinner bloodTypeSpinner, urgencySpinner;
    private EditText quantityInput;
    private Button submitButton;
    private ProgressBar progressBar;
    private final Gson gson = new Gson();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blood_stock, container, false);

        bloodTypeSpinner = view.findViewById(R.id.bloodTypeSpinner);
        urgencySpinner = view.findViewById(R.id.urgencySpinner);
        quantityInput = view.findViewById(R.id.quantityInput);
        submitButton = view.findViewById(R.id.submitButton);
        progressBar = view.findViewById(R.id.progressBar);

        setupSpinners();
        setupSubmitButton();

        return view;
    }

    private void setupSpinners() {
        // Blood Type Adapter
        ArrayAdapter<BloodType> bloodTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                BloodType.values()
        );
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);

        // Urgency Level Adapter
        ArrayAdapter<UrgenceLevel> urgencyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                UrgenceLevel.values()
        );
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        urgencySpinner.setAdapter(urgencyAdapter);
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            if (validateInput()) {
                createBloodRequest();
            }
        });
    }

    private boolean validateInput() {
        if (quantityInput.getText().toString().isEmpty()) {
            quantityInput.setError("Quantity is required");
            return false;
        }
        return true;
    }

    private void createBloodRequest() {
        progressBar.setVisibility(View.VISIBLE);
        disableForm(true);

        Context context = requireContext();
        String userId = AuthUtils.getTokenId(context);
        String token = AuthUtils.getAuthToken(context);

        if (token.isEmpty()) {
            showError("User not authenticated");
            progressBar.setVisibility(View.GONE);
            disableForm(false);
            return;
        }

        // Create request object
        BloodRequest request = new BloodRequest();
        request.setGroupeSanguin((BloodType) bloodTypeSpinner.getSelectedItem());
        request.setQuantite(Integer.parseInt(quantityInput.getText().toString()));
        request.setUrgence((UrgenceLevel) urgencySpinner.getSelectedItem());
        request.setStatut(RequestStatus.PENDING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request.setDateDemande(LocalDateTime.now().toString());
        }

        // First fetch hospital ID
        fetchHospitalId(userId, token, request);
    }

    private void fetchHospitalId(String userId, String token, BloodRequest request) {
        OkHttpClient client = new OkHttpClient();
        String url = Constants.BASE_URL + "/api/requests/hospital-users/" + userId;

        Request hospitalRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(hospitalRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    disableForm(false);
                    showError("Network error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // Parse hospital ID directly from JSON
                    JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
                    if (jsonObject.has("hospitalId") && !jsonObject.get("hospitalId").isJsonNull()) {
                        long hospitalId = jsonObject.get("hospitalId").getAsLong();
                        submitBloodRequest(request, hospitalId, userId, token);
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            disableForm(false);
                            showError("Hospital ID not found in user data");
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        disableForm(false);
                        showError("Failed to fetch hospital: " + response.code());
                    });
                }
            }
        });
    }

    private void submitBloodRequest(BloodRequest request, long hospitalId, String userId, String token) {
        OkHttpClient client = new OkHttpClient();
        String url = Constants.BASE_URL + "/api/requests/bloodreq?hospitalId=" + hospitalId + "&createdById=" + userId;

        String json = gson.toJson(request);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request bloodRequest = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(bloodRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    disableForm(false);
                    showError("Request failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    disableForm(false);

                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Request created successfully!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    } else {
                        try {
                            showError("Server error: " + response.code() + " - " + response.body().string());
                        } catch (IOException e) {
                            showError("Server error: " + response.code());
                        }
                    }
                });
            }
        });
    }

    private void disableForm(boolean disabled) {
        bloodTypeSpinner.setEnabled(!disabled);
        urgencySpinner.setEnabled(!disabled);
        quantityInput.setEnabled(!disabled);
        submitButton.setEnabled(!disabled);
    }

    private void clearForm() {
        bloodTypeSpinner.setSelection(0);
        urgencySpinner.setSelection(0);
        quantityInput.setText("");
    }

    private void showError(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}