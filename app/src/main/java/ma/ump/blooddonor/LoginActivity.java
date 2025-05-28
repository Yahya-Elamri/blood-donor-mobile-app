package ma.ump.blooddonor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import ma.ump.blooddonor.api.ApiClient;
import ma.ump.blooddonor.donorActivity.MainActivity;
import ma.ump.blooddonor.hospitalActivity.HospitalActivity;
import ma.ump.blooddonor.utils.AuthUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignupDonor, btnSignupHospital;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check existing valid login
        if (AuthUtils.isUserLoggedIn(this)) {
            navigateBasedOnRole();
            return;
        }

        setContentView(R.layout.activity_login);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignupDonor = findViewById(R.id.btnSignupDonor);
        btnSignupHospital = findViewById(R.id.btnSignupHospital);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnSignupDonor.setOnClickListener(v ->
                startActivity(new Intent(this, DonorSignupActivity.class)));
        btnSignupHospital.setOnClickListener(v ->
                startActivity(new Intent(this, HospitalSignupActivity.class)));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        showLoading(true);

        // Replace with your actual API call implementation
        ApiClient.getInstance(this).login(email, password,
                response -> {
                    showLoading(false);
                    handleLoginSuccess(response);
                },
                error -> {
                    showLoading(false);
                    handleLoginError(error);
                });
    }

    private void handleLoginSuccess(JSONObject response) {
        try {
            String token = response.getString("token");
            long expiresIn = response.getLong("expiresIn");

            // Save token using your secure AuthUtils
            AuthUtils.saveAuthToken(this, token, expiresIn);

            // Navigate based on role from token
            navigateBasedOnRole();

        } catch (JSONException e) {
            Log.e("Login", "Erreur JSON", e);
            showError("Réponse serveur invalide");
        }
    }

    private void navigateBasedOnRole() {
        String role = AuthUtils.getTokenRole(this);
        Log.d("Login", "Detected role: " + role);

        if (role == null) {
            showError("Problème d'authentification, veuillez vous reconnecter");
            AuthUtils.clearAuthCredentials(this);
            return;
        }

        Intent intent;
        switch (role.toUpperCase()) {
            case "HOSPITAL":
                intent = new Intent(this, HospitalActivity.class);
                break;
            case "DONOR":
            default: // Handle unexpected roles as donors
                intent = new Intent(this, MainActivity.class);
                break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(VolleyError error) {
        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 401:
                    showError("Email ou mot de passe incorrect");
                    break;
                case 403:
                    showError("Compte non vérifié");
                    break;
                default:
                    showError("Erreur serveur (" + error.networkResponse.statusCode + ")");
            }
        } else {
            showError("Erreur de connexion");
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
