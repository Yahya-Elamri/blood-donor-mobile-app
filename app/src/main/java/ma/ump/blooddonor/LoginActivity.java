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

import org.json.JSONException;

import ma.ump.blooddonor.api.ApiClient;
import ma.ump.blooddonor.utils.AuthUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignupDonor, btnSignupHospital;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthUtils.isUserLoggedIn(this)) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignupDonor = findViewById(R.id.btnSignupDonor);
        btnSignupHospital = findViewById(R.id.btnSignupHospital);
        progressBar = findViewById(R.id.progressBar);

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

        ApiClient.getInstance(this).login(email, password,
                response -> {
                    showLoading(false);
                    try {
                        String token = response.getString("token");
                        long expiresIn = response.getLong("expiresIn");

                        AuthUtils.saveAuthToken(this, token, expiresIn);
                        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                        navigateToMain();

                    } catch (JSONException e) {
                        Log.e("Login", "Erreur JSON", e);
                        showError("Réponse invalide du serveur");
                    }
                },
                error -> {
                    showLoading(false);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        showError("Email ou mot de passe incorrect");
                    } else {
                        showError("Erreur réseau, veuillez réessayer");
                    }
                });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
