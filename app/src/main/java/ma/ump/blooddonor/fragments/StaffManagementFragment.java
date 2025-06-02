package ma.ump.blooddonor.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.IOException;

import ma.ump.blooddonor.Constants.Constants;
import ma.ump.blooddonor.LoginActivity;
import ma.ump.blooddonor.R;
import ma.ump.blooddonor.entity.HospitalUser;
import ma.ump.blooddonor.utils.AuthUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.os.Build;
import android.widget.LinearLayout;

public class StaffManagementFragment extends Fragment {
    private TextView tvFullName, tvEmail, tvPosition, tvHospital, tvId;
    private LinearLayout btnDisconnect, btnSettings;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff, container, false);

        // Initialize views
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPosition = view.findViewById(R.id.tv_position);
        tvHospital = view.findViewById(R.id.tv_hospital);
        tvId = view.findViewById(R.id.tv_id);
        btnDisconnect = view.findViewById(R.id.btn_disconnect);
        btnSettings = view.findViewById(R.id.btn_settings);
        progressBar = view.findViewById(R.id.progress_bar);

        // Load user profile data
        loadUserProfile();

        // Set click listeners
        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        // Disconnect button click listener
        btnDisconnect.setOnClickListener(v -> showDisconnectDialog());

        // Settings button click listener
        btnSettings.setOnClickListener(v -> navigateToSettings());

        // Add ripple effect and haptic feedback
        addTouchFeedback(btnDisconnect);
        addTouchFeedback(btnSettings);
    }

    private void addTouchFeedback(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Add haptic feedback
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        v.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    } else {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }

                    // Scale down animation
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Scale back to original size
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                    break;
            }
            return false;
        });
    }

    private void loadUserProfile() {
        String token = AuthUtils.getAuthToken(requireContext());
        String userId = AuthUtils.getTokenId(requireContext());

        if (token == null) {
            navigateToLogin();
            return;
        }

        // Show loading state
        showLoadingState(true);

        String url = Constants.BASE_URL + "/api/requests/hospital-users/" + userId;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    showLoadingState(false);
                    showErrorMessage("Network error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    Gson gson = new Gson();
                    HospitalUser user = gson.fromJson(jsonData, HospitalUser.class);

                    requireActivity().runOnUiThread(() -> {
                        showLoadingState(false);
                        updateUI(user);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        showLoadingState(false);
                        handleErrorResponse(response.code());
                    });
                }
            }
        });
    }

    private void updateUI(HospitalUser user) {
        try {
            // Animate the content appearance
            animateContentIn();

            // Update user information
            tvFullName.setText(String.format("%s %s",
                    user.getPrenom() != null ? user.getPrenom() : "",
                    user.getNom() != null ? user.getNom() : "").trim());

            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            tvPosition.setText(user.getPosition() != null ? user.getPosition() : "N/A");

            // Handle hospital information
            if (user.getHospital() != null && user.getHospital().getNom() != null) {
                tvHospital.setText(user.getHospital().getNom());
            } else {
                tvHospital.setText("N/A");
            }

            // Set ID - you might want to format this based on your needs
            if (user.getId() != null) {
                tvId.setText(String.format("ID: %s", user.getId()));
            } else {
                tvId.setText("ID: N/A");
            }

        } catch (Exception e) {
            showErrorMessage("Error displaying user information");
        }
    }

    private void animateContentIn() {
        // Fade in animation for the content
        View rootView = getView();
        if (rootView != null) {
            rootView.setAlpha(0f);
            rootView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void showLoadingState(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Disable buttons during loading
        if (btnDisconnect != null) {
            btnDisconnect.setEnabled(!isLoading);
            btnDisconnect.setAlpha(isLoading ? 0.5f : 1.0f);
        }

        if (btnSettings != null) {
            btnSettings.setEnabled(!isLoading);
            btnSettings.setAlpha(isLoading ? 0.5f : 1.0f);
        }
    }

    private void handleErrorResponse(int responseCode) {
        String errorMessage;
        switch (responseCode) {
            case 401:
                errorMessage = "Session expired. Please login again.";
                navigateToLogin();
                return;
            case 403:
                errorMessage = "Access denied";
                break;
            case 404:
                errorMessage = "User not found";
                break;
            case 500:
                errorMessage = "Server error. Please try again later.";
                break;
            default:
                errorMessage = "Error: " + responseCode;
                break;
        }
        showErrorMessage(errorMessage);
    }

    private void showErrorMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showDisconnectDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Disconnect")
                .setMessage("Are you sure you want to disconnect? You will need to login again.")
                .setPositiveButton("Disconnect", (dialog, which) -> {
                    disconnectUser();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_logout)
                .show();
    }

    private void disconnectUser() {
        // Show loading state
        showLoadingState(true);

        // Add a slight delay for better UX
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AuthUtils.clearAuthCredentials(requireContext());
            navigateToLogin();
        }, 500);
    }

    private void navigateToSettings() {
        // Navigate to settings - implement based on your app structure
        Toast.makeText(getContext(), "Settings coming soon", Toast.LENGTH_SHORT).show();

        // Example navigation to settings activity/fragment:
        // Intent intent = new Intent(getContext(), SettingsActivity.class);
        // startActivity(intent);

        // Or if using Navigation Component:
        // NavController navController = Navigation.findNavController(requireView());
        // navController.navigate(R.id.action_staff_to_settings);
    }

    private void navigateToLogin() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user data when returning to this fragment
        if (AuthUtils.getAuthToken(requireContext()) != null) {
            loadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up references to prevent memory leaks
        tvFullName = null;
        tvEmail = null;
        tvPosition = null;
        tvHospital = null;
        tvId = null;
        btnDisconnect = null;
        btnSettings = null;
        progressBar = null;
    }
}
