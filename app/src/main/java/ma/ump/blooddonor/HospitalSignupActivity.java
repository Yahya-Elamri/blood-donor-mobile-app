package ma.ump.blooddonor;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HospitalSignupActivity extends AppCompatActivity {
    private Spinner hospitalSpinner, positionSpinner;
    private ArrayAdapter<Hospital> hospitalAdapter;
    private ArrayAdapter<CharSequence> positionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_signup);

        // Setup Hospital Spinner
        List<Hospital> hospitals = new ArrayList<>(); // Load from DB/API
        hospitalAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                hospitals
        );
        // Setup Position Spinner
        positionAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.staff_positions,
                android.R.layout.simple_spinner_item
        );
    }

    // Hospital model class
    public static class Hospital {
        private long id;
        private String name;

        public Hospital(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() { return name; }

        public long getId() { return id; }
    }
}