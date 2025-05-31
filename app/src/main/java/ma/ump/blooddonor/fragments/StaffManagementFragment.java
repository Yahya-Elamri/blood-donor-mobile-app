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

public class StaffManagementFragment extends Fragment {
    private TextView tvFullName, tvEmail, tvPosition, tvHospital;
    private Button btnDisconnect;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff, container, false);

        tvFullName = view.findViewById(R.id.tv_full_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPosition = view.findViewById(R.id.tv_position);
        tvHospital = view.findViewById(R.id.tv_hospital);
        btnDisconnect = view.findViewById(R.id.btn_disconnect);
        progressBar = view.findViewById(R.id.progress_bar);

        loadUserProfile();

        btnDisconnect.setOnClickListener(v -> disconnectUser());

        return view;
    }

    private void loadUserProfile() {
        String token = AuthUtils.getAuthToken(requireContext());
        String userId = AuthUtils.getTokenId(requireContext());

        if (token == null ) {
            navigateToLogin();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
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
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    Gson gson = new Gson();
                    HospitalUser user = gson.fromJson(jsonData, HospitalUser.class);

                    requireActivity().runOnUiThread(() -> updateUI(user));
                } else {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updateUI(HospitalUser user) {
        progressBar.setVisibility(View.GONE);
        tvFullName.setText(user.getNom()+" "+user.getPrenom());
        tvEmail.setText(user.getEmail());
        tvPosition.setText(user.getPosition());
        tvHospital.setText(user.getHospital().getNom());
    }

    private void disconnectUser() {
        AuthUtils.clearAuthCredentials(requireContext());
        navigateToLogin();
    }

    private void navigateToLogin() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}
