package ma.ump.blooddonor.hospitalActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ma.ump.blooddonor.R;
import ma.ump.blooddonor.fragments.AppointmentsFragment;
import ma.ump.blooddonor.fragments.BloodStockFragment;
import ma.ump.blooddonor.fragments.DashboardFragment;
import ma.ump.blooddonor.fragments.StaffManagementFragment;

public class HospitalActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    private boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        Fragment selectedFragment;
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            selectedFragment = new DashboardFragment();
        } else if (id == R.id.nav_stock) {
            selectedFragment = new BloodStockFragment();
        } else if (id == R.id.nav_appointments) {
            selectedFragment = new AppointmentsFragment();
        } else if (id == R.id.nav_staff) {
            selectedFragment = new StaffManagementFragment();
        } else {
            return false;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();

        return true;
    }
}
