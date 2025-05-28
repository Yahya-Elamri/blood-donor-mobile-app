package ma.ump.blooddonor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import ma.ump.blooddonor.R;

public class AppointmentsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        loadAnalyticsData();

        return view;
    }

    private void loadAnalyticsData() {
        // Fetch data from API and update charts
    }
}
