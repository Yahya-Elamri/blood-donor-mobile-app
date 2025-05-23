package ma.ump.blooddonor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ma.ump.blooddonor.api.ApiClient;
import ma.ump.blooddonor.entity.Donor;
import ma.ump.blooddonor.utils.AuthUtils;

public class DonorSignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etNom, etPrenom, etGroupeSanguin , etLocalisation;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_signup);

        AutoCompleteTextView actvGroupeSanguin = findViewById(R.id.actvGroupeSanguin);

        String[] bloodTypes = {"A_POSITIVE", "A_NEGATIVE",
                "B_POSITIVE", "B_NEGATIVE",
                "AB_POSITIVE", "AB_NEGATIVE",
                "O_POSITIVE", "O_NEGATIVE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, // or getContext() if in a Fragment
                android.R.layout.simple_dropdown_item_1line,
                bloodTypes
        );

        actvGroupeSanguin.setAdapter(adapter);
        actvGroupeSanguin.setOnClickListener(v -> actvGroupeSanguin.showDropDown());

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etGroupeSanguin = findViewById(R.id.actvGroupeSanguin);
        etLocalisation = findViewById(R.id.etLocalisation);

        ImageButton btnBack = findViewById(R.id.arrow_back);
        btnBack.setOnClickListener(v -> startActivity(
                new Intent(this, LoginActivity.class)
        ));
        // Initialisation des vues
        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(v -> performSignup());
    }

    private void performSignup() {
        // Extract and trim input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String groupeSanguin = etGroupeSanguin.getText().toString().trim();
        String localisation = etLocalisation.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty() || password.isEmpty() || nom.isEmpty() ||
                prenom.isEmpty() || groupeSanguin.isEmpty() || localisation.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Adresse email invalide.");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        // Create Donor object
        Donor donor = new Donor(email, password, nom, prenom, groupeSanguin, localisation);

        // Call API
        ApiClient.getInstance(this).registerDonor(donor,
                response -> {
                    Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                },
                error -> {
                    String message = "Erreur d'inscription";
                    if (error != null && error.getMessage() != null) {
                        message += " " + error.getMessage();
                    }
                    showError(message);
                }
        );
    }


    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
